package uz.tikoncha_parent.presentation.chat.chat_list

import uz.tikoncha_parent.presentation.model.ChatUi
import uz.tikoncha_parent.data.local.SessionCache
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import uz.tikoncha_parent.common.DateTimeUtil
import uz.tikoncha_parent.data.local.AppSettings
import uz.tikoncha_parent.data.mapper.toChatUi
import uz.tikoncha_parent.data.remote.model.ChatMessageDto
import uz.tikoncha_parent.data.remote.model.ChatWsEvent
import uz.tikoncha_parent.domain.model.app_error.Outcome
import uz.tikoncha_parent.domain.repository.ChatRepository
import uz.tikoncha_parent.presentation.chat.ChatConnectionManager
import uz.tikoncha_parent.presentation.chat.ChatDateTimeUtil
import uz.tikoncha_parent.presentation.profile.language.LanguagePrefs

class ChatViewModel(
    private val repository: ChatRepository,
    private val connectionManager: ChatConnectionManager
) : ScreenModel {

    private var chatListJob: Job? = null
    private var eventsJob: Job? = null
    val langType = LanguagePrefs.loadOrDefault()
    private val _state = MutableStateFlow(
        SessionCache.get<List<ChatUi>>(CACHE_KEY)
            ?.let { ChatState(chats = it, hasLoadedOnce = true) }
            ?: ChatState()
    )
    val state = _state.asStateFlow()

    init {
        syncChild()
        // Ro'yxat (WS yangilanishlari bilan birga) keshda — keyingi ochilishda darhol ko'rinadi.
        screenModelScope.launch {
            _state.collect { if (it.hasLoadedOnce) SessionCache.put(CACHE_KEY, it.chats) }
        }
    }

    fun onEvent(event: ChatEvent) {
        when (event) {
            ChatEvent.Refresh -> {
                syncChild()
                getChatList(isRefresh = true)
            }

            is ChatEvent.OnScreenOpened -> {
                syncChild()
                connectionManager.acquire(event.screen)
                startObserveEvents()
                getChatList(isRefresh = false)
            }

            is ChatEvent.OnScreenClosed -> {
                connectionManager.release(event.screen)
            }

            ChatEvent.ClearError -> {
                _state.update {
                    it.copy(
                        error = null
                    )
                }
            }
        }
    }

    private fun startObserveEvents() {
        if (eventsJob?.isActive == true) return
        eventsJob = screenModelScope.launch {
            repository.observeEvents().collect { ev ->
                when (ev) {
                    is ChatWsEvent.MessageCreated -> applyMessageCreated(ev.message)
                    is ChatWsEvent.ReadUpdate -> applyReadUpdate(ev.chatId, ev.messageId)
                    else -> Unit
                }
            }
        }
    }


    private fun getChatList(isRefresh: Boolean) {

        _state.update {
            if (isRefresh) {
                it.copy(
                    isRefreshing = true,
                    error = null
                )
            }
            else {
                it.copy(
                    loading = true,
                    error = null
                )
            }
        }

        chatListJob?.cancel()
        chatListJob = screenModelScope.launch {
            when (val res = repository.chatList()) {
                is Outcome.Failure -> {
                    _state.update {
                        it.copy(
                            loading = false,
                            isRefreshing = false,
                            hasLoadedOnce = true,
                            error = res
                        )
                    }
                }

                is Outcome.Success -> {
                    val chats = res.data.map { it.toChatUi() }
                    _state.update {
                        it.copy(
                            error = null,
                            chats = chats,
                            loading = false,
                            hasLoadedOnce = true,
                            isRefreshing = false
                        )
                    }
                }
            }
        }
    }

    private fun applyMessageCreated(dto: ChatMessageDto) {
        _state.update { s ->
            val updated = s.chats.map { chat ->
                if (chat.chatId != dto.chat_id) return@map chat

                val isMine = dto.is_mine == true
                val isRead = dto.is_read == true
                val newUnread = if (isMine) chat.unreadCount else chat.unreadCount + 1

                chat.copy(
                    lastMessage = dto.text,
                    lastMessageIsMine = isMine,
                    lastMessageIsRead = isRead,
                    unreadCount = newUnread,
                    dateTime = runCatching {
                        val millis = DateTimeUtil.toMillis(dto.created_at)
                        ChatDateTimeUtil.formatChatDate(millis = millis, langType = langType)
                    }.getOrDefault(chat.dateTime)
                )
            }
            s.copy(chats = updated)
        }
    }

    private fun applyReadUpdate(chatId: String, messageId: String) {
        // ChatList uchun odatda: unread_count=0 qilish yoki lastMessageIsRead=true qilish.
        // Backend read_update faqat bitta messageId bo‘lsa ham,
        // chatList’da "o‘qildi" belgisi last message uchun bo‘lsa yangilaymiz:
        _state.update { s ->
            s.copy(
                chats = s.chats.map { chat ->
                    if (chat.chatId == chatId) chat.copy(lastMessageIsRead = true) else chat
                }
            )
        }
    }

    private fun syncChild() {
        _state.update {
            it.copy(
                hasChild = AppSettings.children.isNotEmpty()
            )
        }
    }

    private companion object {
        const val CACHE_KEY = "chat_list"
    }
}
