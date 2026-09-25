package uz.tikoncha_parent.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.request.parameter
import io.ktor.client.request.setBody
import io.ktor.http.HttpMethod
import uz.tikoncha_parent.data.remote.model.ChatListResponse
import uz.tikoncha_parent.data.remote.model.ChatMessagesResponse
import uz.tikoncha_parent.data.remote.model.ChatNotificationRequest
import uz.tikoncha_parent.data.remote.model.ChatNotificationResponse
import uz.tikoncha_parent.data.remote.model.ChatStatusResponse
import uz.tikoncha_parent.data.remote.model.ChatUnreadCountResponse
import uz.tikoncha_parent.data.remote.model.SendMessageRequest
import uz.tikoncha_parent.data.remote.model.SendMessageResponse
import uz.tikoncha_parent.domain.model.chat.DeleteMessageResponse

class ChatApiService (
    private val httpClient: HttpClient
) {
    suspend fun chatList(): ChatListResponse =
        httpClient.safeRequest(
            method = HttpMethod.Get,
            url = "/chat/chats",
            block = {}
        )

    suspend fun chatMessages(params: Map<String, Any>): ChatMessagesResponse =
        httpClient.safeRequest(
            method = HttpMethod.Get,
            url = "chat/messages",
            block = {
                params.forEach {
                    parameter(it.key, it.value)
                }
            }
        )

    suspend fun chatSendMessage(request: SendMessageRequest): SendMessageResponse =
        httpClient.safeRequest(
            method = HttpMethod.Post,
            url = "chat/messages",
            block = {
                setBody(request)
            }
        )

    suspend fun chatStatus(chatId: String): ChatStatusResponse =
        httpClient.safeRequest(
            method = HttpMethod.Get,
            url = "chat/status",
            block = {
                parameter("chat_id", chatId)
            }
        )

    suspend fun chatUnreadCount(): ChatUnreadCountResponse =
        httpClient.safeRequest(
            method = HttpMethod.Get,
            url = "chat/unread-counts",
            block = {

            }
        )

    suspend fun chatNotification(chatId: String): ChatNotificationResponse =
        httpClient.safeRequest(
            method = HttpMethod.Get,
            url = "/chat/chats/$chatId/notification",
            block = {}
        )

    suspend fun updateChatNotification(request: ChatNotificationRequest): ChatNotificationResponse =
        httpClient.safeRequest(
            method = HttpMethod.Post,
            url = "/chat/chats/notification",
            block = {
                setBody(request)
            }
        )

    suspend fun deleteMessage(messageId: String): DeleteMessageResponse =
        httpClient.safeRequest(
            method = HttpMethod.Delete,
            url = "/chat/messages/$messageId",
            block = {}
        )
}