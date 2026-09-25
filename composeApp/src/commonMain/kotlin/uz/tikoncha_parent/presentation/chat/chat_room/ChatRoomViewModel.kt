package uz.tikoncha_parent.presentation.chat.chat_room

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import tikoncha_parents.composeapp.generated.resources.Res
import tikoncha_parents.composeapp.generated.resources.suhbat_bildirishnomalari_ochirildi
import tikoncha_parents.composeapp.generated.resources.suhbat_bildirishnomalari_yoqildi
import uz.tikoncha_parent.common.DateTimeUtil
import uz.tikoncha_parent.data.mapper.buildOptimisticTextMessage
import uz.tikoncha_parent.data.mapper.stableKey
import uz.tikoncha_parent.data.mapper.toChatMessageUi
import uz.tikoncha_parent.data.remote.model.ChatMessageDto
import uz.tikoncha_parent.data.remote.model.ChatWsEvent
import uz.tikoncha_parent.domain.model.app_error.Outcome
import uz.tikoncha_parent.domain.repository.ChatRepository
import uz.tikoncha_parent.domain.use_case.chat.GetChatMessagesFromServerUseCase
import uz.tikoncha_parent.domain.use_case.chat.ObserveChatStatusUseCase
import uz.tikoncha_parent.domain.use_case.chat.SendMessageUseCase
import uz.tikoncha_parent.platform.Logger
import uz.tikoncha_parent.platform.randomUUID
import uz.tikoncha_parent.presentation.chat.ChatConnectionManager
import uz.tikoncha_parent.presentation.chat.ChatDateTimeUtil
import uz.tikoncha_parent.presentation.chat.model.DeliveryStatus
import uz.tikoncha_parent.presentation.model.ChatMessageType
import uz.tikoncha_parent.presentation.model.ChatMessageUi
import uz.tikoncha_parent.presentation.model.ChatType
import uz.tikoncha_parent.presentation.profile.language.LanguagePrefs

class ChatRoomViewModel(
    private val getChatMessagesPage: GetChatMessagesFromServerUseCase,
    private val sendMessageUseCase: SendMessageUseCase,
    private val observeChatStatusUseCase: ObserveChatStatusUseCase,
    private val connectionManager: ChatConnectionManager,
    private val repository: ChatRepository
) : ScreenModel {

    private val langType = LanguagePrefs.loadOrDefault()
    private val TAG = "ChatRoomViewModel"

    private val _state = MutableStateFlow(ChatRoomState())
    val state = _state.asStateFlow()

    private var wsJob: Job? = null
    private var notificationLoadJob: Job? = null

    private val mutableChatTypes = setOf(ChatType.PARENT_CHILD, ChatType.SIBLINGS)
    private var chatStatusJob: Job? = null
    private val mergeMutex = Mutex()

    // ✅ LoadMore loop guard
    private var lastRequestedSinceId: String? = null

    fun onEvent(e: ChatRoomEvent) {
        when (e) {
            is ChatRoomEvent.Open -> {
                connectionManager.acquire(e.tag)
                startObserver()
            }

            is ChatRoomEvent.Close -> {
                connectionManager.release(e.tag)
                wsJob?.cancel()
                wsJob = null
            }

            is ChatRoomEvent.SetChatListData -> {
                _state.update {
                    it.copy(
                        chatId = e.chatId,
                        chatTitle = e.chatTitle,
                        chatAvatar = e.chatAvatar,
                        chatType = e.chatType,
                        notificationEnabled = null,
                    )
                }
                loadInitial()
                observeChatStatus()
                loadNotification()
            }

            ChatRoomEvent.LoadInitial -> loadInitial()
            ChatRoomEvent.LoadMore -> loadMore()

            is ChatRoomEvent.OnTextChange -> _state.update { it.copy(text = e.text) }
            ChatRoomEvent.SendMessage -> {
                if (state.value.selectedMessageForEdit == null) {
                    sendMessage()
                } else {
                    editMessage()
                }
            }

            ChatRoomEvent.OnReachedBottom -> markLastRead()
            is ChatRoomEvent.SelectMessageForReply -> {
                _state.update { it.copy(replyToMessage = e.message) }
            }

            is ChatRoomEvent.SelectedMessageForEdit -> {
                _state.update {
                    it.copy(
                        selectedMessageForEdit = e.message,
                        text = e.message.message
                    )
                }
            }

            is ChatRoomEvent.Retry -> {
                if (e.message.id.isBlank()) {
                    retrySendMessage(e.message)
                } else {
                    retryEditMessage(e.message)
                }
            }

            is ChatRoomEvent.SelectMessageForDelete -> {
                _state.update { it.copy(selectedMessageForDelete = e.message) }
            }

            is ChatRoomEvent.DeleteFailedMessage -> {
                if (e.message.id.isBlank()) {
                    deleteFailedMessage(e.message)
                } else {
                    _state.update {
                        it.copy(
                            selectedMessageForDelete = e.message
                        )
                    }
                    deleteMessage()
                }
            }

            ChatRoomEvent.CancelEdit -> {
                _state.update { it.copy(selectedMessageForEdit = null, text = "") }
            }

            ChatRoomEvent.CancelReply -> {
                _state.update { it.copy(replyToMessage = null) }
            }

            ChatRoomEvent.CancelDelete -> {
                _state.update { it.copy(selectedMessageForDelete = null) }
            }

            ChatRoomEvent.ConfirmDelete -> {
                deleteMessage()
            }


            ChatRoomEvent.ToggleNotification -> toggleNotification()

            ChatRoomEvent.NotificationNoticeShown -> {
                _state.update { it.copy(notificationNotice = null, notificationError = null) }
            }
        }
    }

    private fun loadNotification() {
        val chatId = state.value.chatId
        if (chatId.isBlank() || state.value.chatType !in mutableChatTypes) return

        notificationLoadJob?.cancel()
        notificationLoadJob = screenModelScope.launch {
            when (val res = repository.chatNotification(chatId)) {
                is Outcome.Success -> _state.update { it.copy(notificationEnabled = res.data) }
                // Holat olinmasa tugma ko'rinmaydi, suhbat odatdagidek ishlayveradi.
                is Outcome.Failure -> Unit
            }
        }
    }

    private fun toggleNotification() {
        val current = state.value.notificationEnabled ?: return
        if (state.value.isNotificationUpdating) return

        val chatId = state.value.chatId
        _state.update { it.copy(isNotificationUpdating = true) }
        screenModelScope.launch {
            when (val res = repository.setChatNotification(chatId, enabled = !current)) {
                is Outcome.Success -> _state.update {
                    it.copy(
                        notificationEnabled = res.data,
                        isNotificationUpdating = false,
                        notificationNotice = if (res.data) Res.string.suhbat_bildirishnomalari_yoqildi
                        else Res.string.suhbat_bildirishnomalari_ochirildi,
                    )
                }

                is Outcome.Failure -> _state.update {
                    it.copy(isNotificationUpdating = false, notificationError = res)
                }
            }
        }
    }

    private fun startObserver() {
        if (wsJob?.isActive == true) return
        wsJob = screenModelScope.launch {
            repository.observeEvents().collect { ev ->
                when (ev) {
                    is ChatWsEvent.MessageCreated -> onSocketMessageCreated(ev.message)
                    is ChatWsEvent.MessageUpdated -> onSocketMessageUpdated(ev.message)
                    is ChatWsEvent.ReadUpdate -> onSocketReadUpdate(ev.chatId, ev.messageId)
                    else -> Unit
                }
            }
        }
    }

    private fun loadInitial() {
        val chatId = state.value.chatId
        if (chatId.isBlank()) return

        lastRequestedSinceId = null

        _state.update {
            it.copy(
                isInitialLoading = true,
                isPagingLoading = false,
                error = null,
                allMessages = emptyList(),
                messages = emptyList(),
                lastMessage = null,
                canLoadMore = true,
                sinceId = null
            )
        }

        screenModelScope.launch {
            when (val res =
                getChatMessagesPage(chatId, sinceId = null, sinceTs = null, limit = 40)) {
                is Outcome.Success -> {
                    val pageDesc = res.data.items
                        .map { it.toChatMessageUi() }
                        .distinctBy { it.stableKey() }
                        .sortedByDescending { it.createdAt }

                    // cursor: backend bersa o'sha, bo'lmasa oldest id
                    val cursor = res.data.sinceId ?: pageDesc.lastOrNull()?.id

                    _state.update {
                        it.copy(
                            isInitialLoading = false,
                            canLoadMore = res.data.hasMore && !cursor.isNullOrBlank(),
                            sinceId = cursor
                        )
                    }

                    applyAllMessagesDesc(pageDesc)
                }

                is Outcome.Failure -> _state.update {
                    it.copy(isInitialLoading = false, error = res)
                }
            }
        }
    }

    private fun loadMore() {
        val s = state.value
        if (s.isInitialLoading || s.isPagingLoading || !s.canLoadMore) return

        val cursor = s.sinceId
        if (cursor.isNullOrBlank()) {
            _state.update { it.copy(canLoadMore = false) }
            return
        }

        // loop guard
        if (lastRequestedSinceId == cursor) return
        lastRequestedSinceId = cursor

        _state.update { it.copy(isPagingLoading = true) }

        screenModelScope.launch {
            when (val res =
                getChatMessagesPage(s.chatId, sinceId = cursor, sinceTs = null, limit = 40)) {
                is Outcome.Success -> {
                    val olderDesc = res.data.items
                        .map { it.toChatMessageUi() }
                        .distinctBy { it.stableKey() }
                        .sortedByDescending { it.createdAt }

                    mergeMutex.withLock {
                        val current = state.value.allMessages

                        // ✅ server DESC + sinceId => bu older bo'lishi kerak
                        // current (newest..oldest) + older (newest..oldest older-part)
                        val merged = (current + olderDesc)
                            .distinctBy { it.stableKey() }
                            .sortedByDescending { it.createdAt }

                        val newCursor = res.data.sinceId ?: merged.lastOrNull()?.id

                        val noProgress = olderDesc.isEmpty() || merged.size == current.size

                        _state.update {
                            it.copy(
                                isPagingLoading = false,
                                canLoadMore = !noProgress && res.data.hasMore && !newCursor.isNullOrBlank(),
                                sinceId = newCursor
                            )
                        }

                        applyAllMessagesDesc(merged)

                        if (noProgress) lastRequestedSinceId = null
                    }
                }

                is Outcome.Failure -> {
                    // Qulfni ochamiz — aks holda internet qaytsa ham
                    // shu kursor bilan qayta urinib bo'lmaydi.
                    lastRequestedSinceId = null
                    _state.update {
                        it.copy(isPagingLoading = false, error = res)
                    }
                }
            }
        }
    }

    private fun descComparator(): Comparator<ChatMessageUi> =
        compareByDescending<ChatMessageUi> { it.createdAt }
            .thenByDescending { it.id.isBlank() }
            .thenByDescending { it.stableKey() }

    private fun sendMessage() {
        val text = state.value.text.trim()
        if (text.isEmpty()) return
        val chatId = state.value.chatId
        if (chatId.isBlank()) return

        val clientMsgId = randomUUID()

        val replyToMessage = state.value.replyToMessage

        val optimisticTs = DateTimeUtil.nowMillis()
        val optimistic = buildOptimisticTextMessage(
            text = text,
            clientMsgId = clientMsgId,
            nowMillis = optimisticTs,
            replyToId = replyToMessage?.id,
            replyToMessageTex = replyToMessage?.message,
            replyToMessageOwner = replyToMessage?.senderName
        )

        screenModelScope.launch {
            mergeMutex.withLock {
                val next = (listOf(optimistic) + state.value.allMessages)
                    .distinctBy { it.stableKey() }
                    .sortedWith(descComparator())

                _state.update {
                    it.copy(
                        text = "",
                        replyToMessage = null
                    )
                }
                applyAllMessagesDesc(next, scrollToBottom = true)
            }

            val res = sendMessageUseCase(
                chatId = chatId,
                text = text,
                type = ChatMessageType.TEXT,
                clientMsgId = clientMsgId,
                replyToId = replyToMessage?.id
            )

            if (res is Outcome.Failure) {
                markLocalFailed(clientMsgId)
            }
        }
    }

    private fun editMessage() {
        val text = state.value.text.trim()
        if (text.isEmpty()) return
        val chatId = state.value.chatId
        val selectedMessage = state.value.selectedMessageForEdit ?: return
        if (chatId.isBlank()) return
        if (selectedMessage.id.isBlank()) return

        val optimisticTs = DateTimeUtil.nowMillis()

        screenModelScope.launch {
            mergeMutex.withLock {
                val next = state.value.allMessages.map { m ->
                    if (m.id == selectedMessage.id) {
                        m.copy(
                            message = text,
                            updatedAt = optimisticTs,
                            status = DeliveryStatus.SENDING,
                        )
                    } else {
                        m
                    }
                }.sortedWith(descComparator())

                _state.update {
                    it.copy(
                        text = "",
                        selectedMessageForEdit = null
                    )
                }
                applyAllMessagesDesc(next)
            }
            val res = repository.editMessage(
                messageId = selectedMessage.id,
                newText = text
            )
            if (res is Outcome.Failure) {
                markLocalFailed(selectedMessage.id)
            }
        }
    }

    private fun deleteMessage() {
        val selectedMessage = state.value.selectedMessageForDelete ?: return
        if (!selectedMessage.isMine) return

        screenModelScope.launch {
            if (selectedMessage.id.isNotBlank() && !selectedMessage.clientMsgId.isNullOrBlank()) {
                deleteLocalByClientMsgId(selectedMessage.clientMsgId)
                _state.update {
                    it.copy(
                        selectedMessageForDelete = null
                    )
                }
                return@launch
            }
            when (repository.deleteMessage(selectedMessage.id)) {
                is Outcome.Failure -> {
                    markLocalFailed(selectedMessage.clientMsgId ?: "")
                    _state.update {
                        it.copy(
                            selectedMessageForDelete = null
                        )
                    }
                }
                is Outcome.Success -> {
                    deleteLocalByClientMsgId(selectedMessage.clientMsgId ?: "")
                    _state.update {
                        it.copy(
                            selectedMessageForDelete = null
                        )
                    }
                }
            }
        }
    }

    private fun retrySendMessage(messageUi: ChatMessageUi) {
        val chatId = state.value.chatId
        if (chatId.isBlank()) return
        val clientMsgId = messageUi.clientMsgId ?: return
        val text = messageUi.message
        if (text.isBlank()) return

        screenModelScope.launch {
            mergeMutex.withLock {
                val next = state.value.allMessages.map { m ->
                    if (m.clientMsgId == clientMsgId) {
                        m.copy(
                            status = DeliveryStatus.SENDING,
                        )
                    } else {
                        m
                    }
                }.sortedWith(descComparator())
                applyAllMessagesDesc(next)
            }

            val res = sendMessageUseCase(
                chatId = chatId,
                text = text,
                type = ChatMessageType.TEXT,
                clientMsgId = clientMsgId,
                replyToId = messageUi.replyToId
            )
            if (res is Outcome.Failure) {
                markLocalFailed(clientMsgId)
            }
        }
    }

    private fun retryEditMessage(messageUi: ChatMessageUi) {
        val messageId = messageUi.id
        val text = messageUi.message

        screenModelScope.launch {
            mergeMutex.withLock {
                val next = state.value.allMessages.map { m ->
                    if (m.id == messageId) {
                        m.copy(
                            status = DeliveryStatus.SENDING,
                        )
                    } else {
                        m
                    }
                }.sortedWith(descComparator())
                applyAllMessagesDesc(next)
            }
            val res = repository.editMessage(
                messageId = messageId,
                newText = text
            )
            if (res is Outcome.Failure) {
                markEditFailed(messageId)
            }
        }
    }

    private fun deleteFailedMessage(messageUi: ChatMessageUi) {
        screenModelScope.launch {
            mergeMutex.withLock {
                val next = if (messageUi.id.isNotBlank()) {
                    state.value.allMessages.filterNot { it.id == messageUi.id }
                } else {
                    state.value.allMessages.filterNot { it.clientMsgId == messageUi.clientMsgId }
                }.sortedWith(descComparator())
                applyAllMessagesDesc(next)
            }
        }
    }

    private fun deleteLocalByClientMsgId(clientMsgId: String) {
        val next = state.value.allMessages
            .filterNot { it.clientMsgId == clientMsgId }
            .sortedWith(descComparator())
        applyAllMessagesDesc(next)
    }

    private fun markLocalFailed(clientMsgId: String) {
        screenModelScope.launch {
            mergeMutex.withLock {
                val next = state.value.allMessages.map { m ->
                    if (m.isMine && m.clientMsgId == clientMsgId && m.id.isBlank()) {
                        m.copy(status = DeliveryStatus.FAILED)
                    } else m
                }
                applyAllMessagesDesc(next)
            }
        }
    }

    private fun markEditFailed(messageId: String) {
        screenModelScope.launch {
            mergeMutex.withLock {
                val next = state.value.allMessages.map { m ->
                    if (m.id == messageId) {
                        m.copy(status = DeliveryStatus.FAILED)
                    } else m
                }.sortedWith(descComparator())
                applyAllMessagesDesc(next)
            }
        }
    }

    private fun onSocketMessageCreated(dto: ChatMessageDto) {
        // faqat shu chat
        if (dto.chat_id != state.value.chatId) return

        val serverUi = dto.toChatMessageUi()

        screenModelScope.launch {
            mergeMutex.withLock {
                val current = state.value.allMessages

                val merged: List<ChatMessageUi> =
                    if (serverUi.isMine && !serverUi.clientMsgId.isNullOrBlank()) {

                        var replaced = false

                        val updated = current.map { local ->
                            if (!replaced && local.isMine && local.clientMsgId == serverUi.clientMsgId) {
                                replaced = true
                                // ✅ to‘liq server message bilan almashtiramiz (status ham servernikidek qoladi)
                                serverUi
                            } else {
                                local
                            }
                        }

                        // Agar topilmasa (masalan app restart bo‘ldi) — server msgni list boshiga qo‘shamiz
                        if (!replaced) listOf(serverUi) + current else updated

                    } else {
                        listOf(serverUi) + current
                    }

                val normalized = merged
                    .distinctBy { it.stableKey() }
                    .sortedByDescending { it.createdAt }

                applyAllMessagesDesc(normalized, true)

            }
        }
    }

    private fun onSocketMessageUpdated(dto: ChatMessageDto) {
        // faqat shu chat
        if (dto.chat_id != state.value.chatId) return

        val serverUi = dto.toChatMessageUi()
        if (serverUi.id.isBlank()) return

        screenModelScope.launch {
            mergeMutex.withLock {
                var found = false

                val next = state.value.allMessages.map { local ->
                    if (local.id == serverUi.id) {
                        found = true
                        // Tahrir faqat matnni o'zgartiradi. is_read/is_mine maydonlari
                        // tahrir hodisasida kelmasligi mumkin, shuning uchun ularni
                        // lokal holatdan saqlab qolamiz.
                        local.copy(
                            message = serverUi.message,
                            isEdited = serverUi.isEdited,
                            messageType = serverUi.messageType,
                            remoteUrl = serverUi.remoteUrl,
                        )
                    } else {
                        local
                    }
                }

                // Xabar hozirgi ro'yxatda bo'lmasa (masalan eski sahifada) — tegmaymiz
                if (found) applyAllMessagesDesc(next)
            }
        }
    }

    private suspend fun onSocketReadUpdate(chatId: String, messageId: String) {
        if (chatId != state.value.chatId) return

        mergeMutex.withLock {
            val next = state.value.allMessages.map { m ->
                if (m.id == messageId) m.copy(isRead = true, status = DeliveryStatus.READ)
                else m
            }
            applyAllMessagesDesc(next)
        }
    }

    private fun markLastRead() {
        val chatId = state.value.chatId
        // Eng oxirgi xabar o'zimniki bo'lsa ham undan oldingi o'qilmagan xabarlar qolib ketmasin —
        // eng yangi o'qilmagan kiruvchi xabarni belgilaymiz (allMessages yangidan eskiga tartiblangan).
        val target = state.value.allMessages.firstOrNull { !it.isMine && !it.isRead } ?: return

        screenModelScope.launch {
            repository.markRead(chatId = chatId, messageId = target.id)
        }
    }


    var regroupJob: Job? = null
    private fun applyAllMessagesDesc(
        allDesc: List<ChatMessageUi>,
        scrollToBottom: Boolean = false
    ) {
        val normalized = allDesc
            .distinctBy { it.stableKey() }
            .sortedByDescending { it.createdAt }

        _state.update {
            it.copy(
                allMessages = normalized,
                lastMessage = normalized.firstOrNull()
            )
        }

        regroupJob?.cancel()
        regroupJob = screenModelScope.launch(Dispatchers.Default) {
            val grouped = ChatGrouper.groupDescForReverseLayout(
                allDesc = normalized,
                langType = langType
            )
            withContext(Dispatchers.Main) {
                _state.update {
                    it.copy(
                        messages = grouped,
                        scrollToBottomTick = if (scrollToBottom) DateTimeUtil.nowMillis() else it.scrollToBottomTick
                    )
                }
            }
        }
    }

    private fun observeChatStatus() {
        chatStatusJob?.cancel()
        val chatId = state.value.chatId
        if (chatId.isBlank()) return
        chatStatusJob = screenModelScope.launch(Dispatchers.Default) {
            observeChatStatusUseCase.invoke(chatId).collect { result ->
                when (result) {
                    is Outcome.Failure -> Unit
                    is Outcome.Success -> {
                        Logger.d(
                            "observeChatStatus",
                            "lastSeen=${result.data.firstOrNull()?.last_seen}  data=${result.data.joinToString()}"
                        )
                        val lastTimeOnlineMillis =
                            DateTimeUtil.toMillisUtc(result.data.firstOrNull()?.last_seen)
                        val lastTimeOnline = ChatDateTimeUtil.formatChatDate(
                            millis = lastTimeOnlineMillis,
                            langType = langType
                        )

                        Logger.d(TAG, "observeChatStatus: count=${result.data.size}")
                        _state.update {
                            it.copy(
                                isUserOnline = result.data.firstOrNull()?.online ?: false,
                                lastTimeOnline = lastTimeOnline,
                                chatMembersCount = result.data.size
                            )
                        }
                    }
                }
            }
        }
    }
}
