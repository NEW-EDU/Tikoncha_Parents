package uz.tikoncha_parent.domain.repository

import kotlinx.coroutines.flow.Flow
import uz.tikoncha_parent.data.remote.model.ChatDto
import uz.tikoncha_parent.data.remote.model.ChatMemberDto
import uz.tikoncha_parent.data.remote.model.ChatMessagesData
import uz.tikoncha_parent.data.remote.model.ChatWsEvent
import uz.tikoncha_parent.data.remote.model.SendMessageRequest
import uz.tikoncha_parent.data.remote.model.WSSendMessage
import uz.tikoncha_parent.domain.model.app_error.Outcome

interface ChatRepository {

    // ---- HTTP ----
    suspend fun chatList(): Outcome<List<ChatDto>>
    suspend fun chatMessages(params: Map<String, Any>): Outcome<ChatMessagesData>
    suspend fun sendMessageApi(request: SendMessageRequest): Outcome<Unit>
    suspend fun chatStatus(chatId: String): Outcome<List<ChatMemberDto>>
    suspend fun chatUnreadCount(): Outcome<Int>
    suspend fun deleteMessage(messageId: String): Outcome<Unit>

    /** Shu suhbat uchun push-bildirishnoma yoqilganmi (false — ovozsiz). */
    suspend fun chatNotification(chatId: String): Outcome<Boolean>
    suspend fun setChatNotification(chatId: String, enabled: Boolean): Outcome<Boolean>

    // ---- WS boshqaruv ----
    fun connect()
    fun disconnect()
    fun observeEvents(): Flow<ChatWsEvent>

    // ---- WS amallar ----
    suspend fun sendMessage(message: WSSendMessage): Outcome<Unit>
    suspend fun editMessage(messageId: String, newText: String): Outcome<Unit>
    suspend fun markRead(chatId: String, messageId: String): Outcome<Unit>
}