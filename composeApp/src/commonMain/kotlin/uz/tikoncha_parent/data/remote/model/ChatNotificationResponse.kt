package uz.tikoncha_parent.data.remote.model

import kotlinx.serialization.Serializable

@Serializable
data class ChatNotificationRequest(
    val chat_id: String,
    val notification: Boolean,
)

@Serializable
data class ChatNotificationResponse(
    val success: Boolean,
    val data: ChatNotificationData?,
    val error: String?,
    val code: Int
)

@Serializable
data class ChatNotificationData(
    val chat_id: String,
    val notification: Boolean,
)