package uz.tikoncha_parent.data.repository

import kotlinx.coroutines.flow.Flow
import uz.tikoncha_parent.data.remote.ChatApiService
import uz.tikoncha_parent.data.remote.ChatSocketService
import uz.tikoncha_parent.data.remote.app_error.ApiErrorMapper
import uz.tikoncha_parent.data.remote.model.ChatDto
import uz.tikoncha_parent.data.remote.model.ChatMemberDto
import uz.tikoncha_parent.data.remote.model.ChatMessagesData
import uz.tikoncha_parent.data.remote.model.ChatWsEvent
import uz.tikoncha_parent.data.remote.model.SendMessageRequest
import uz.tikoncha_parent.data.remote.model.WSSendMessage
import uz.tikoncha_parent.domain.model.app_error.Outcome
import uz.tikoncha_parent.domain.repository.ChatRepository
import uz.tikoncha_parent.data.remote.model.ChatNotificationRequest

class ChatRepositoryImpl(
    private val socket: ChatSocketService,
    private val api: ChatApiService
) : ChatRepository {

    override suspend fun chatList(): Outcome<List<ChatDto>> = apiCall(TAG) {
        val res = api.chatList()
        val data = res.data
        if (res.success && data != null) Outcome.Success(data.chats)
        else Outcome.Failure(ApiErrorMapper.fromCode(res.code), res.error)
    }

    override suspend fun chatMessages(params: Map<String, Any>): Outcome<ChatMessagesData> = apiCall(TAG) {
        val res = api.chatMessages(params)
        val data = res.data
        if (res.success && data != null) Outcome.Success(data)
        else Outcome.Failure(ApiErrorMapper.fromCode(res.code), res.error)
    }

    override suspend fun sendMessageApi(request: SendMessageRequest): Outcome<Unit> = apiCall(TAG) {
        val res = api.chatSendMessage(request)
        if (res.success) Outcome.Success(Unit)
        else Outcome.Failure(ApiErrorMapper.fromCode(res.code), res.error)
    }

    override suspend fun chatStatus(chatId: String): Outcome<List<ChatMemberDto>> = apiCall(TAG) {
        val res = api.chatStatus(chatId)
        val data = res.data
        if (res.success && data != null) Outcome.Success(data.members)
        else Outcome.Failure(ApiErrorMapper.fromCode(res.code), res.error)
    }

    override suspend fun chatUnreadCount(): Outcome<Int> = apiCall(TAG) {
        val res = api.chatUnreadCount()
        val data = res.data
        if (res.success && data != null) Outcome.Success(data.items.sumOf { it.count })
        else Outcome.Failure(ApiErrorMapper.fromCode(res.code), res.error)
    }

    override suspend fun deleteMessage(messageId: String): Outcome<Unit> = apiCall(TAG) {
        val res = api.deleteMessage(messageId)
        if (res.success) Outcome.Success(Unit)
        else Outcome.Failure(ApiErrorMapper.fromCode(res.code), res.error)
    }

    override suspend fun chatNotification(chatId: String): Outcome<Boolean> = apiCall(TAG) {
        val res = api.chatNotification(chatId)
        val data = res.data
        if (res.success && data != null) Outcome.Success(data.notification)
        else Outcome.Failure(ApiErrorMapper.fromCode(res.code), res.error)
    }

    override suspend fun setChatNotification(chatId: String, enabled: Boolean): Outcome<Boolean> = apiCall(TAG) {
        val res = api.updateChatNotification(ChatNotificationRequest(chat_id = chatId, notification = enabled))
        val data = res.data
        if (res.success && data != null) Outcome.Success(data.notification)
        else Outcome.Failure(ApiErrorMapper.fromCode(res.code), res.error)
    }


    // ---- WS boshqaruv: Outcome'siz, chunki fire-and-forget / Flow ----

    override fun connect() = socket.connect()

    override fun disconnect() = socket.disconnect()

    override fun observeEvents(): Flow<ChatWsEvent> = socket.events

    // ---- WS amallar: istisno otishi mumkin, shuning uchun Outcome ----

    override suspend fun sendMessage(message: WSSendMessage): Outcome<Unit> = apiCall(TAG) {
        socket.sendMessage(message)
        Outcome.Success(Unit)
    }

    override suspend fun editMessage(messageId: String, newText: String): Outcome<Unit> = apiCall(TAG) {
        socket.editMessage(messageId, newText)
        Outcome.Success(Unit)
    }

    override suspend fun markRead(chatId: String, messageId: String): Outcome<Unit> = apiCall(TAG) {
        socket.markRead(chatId, messageId)
        Outcome.Success(Unit)
    }

    private companion object { const val TAG = "ChatRepository" }
}