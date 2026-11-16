package uz.tikoncha_parent.domain.model.push

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FcmPayload(
    val title: String = "",
    val message: String = "",
    val type: PayloadType = PayloadType.GENERAL,
    val body: FcmPayloadBody? = null,
)

@Serializable
data class FcmPayloadBody(
    val message: MessageBlock? = null,
    val live: LiveBlock? = null,
    @SerialName("permission_off") val permissionOff: PermissionOffBlock? = null,
)

@Serializable
data class PermissionOffBlock(
    @SerialName("child_user_id") val childUserId: String? = null,
    @SerialName("child_name") val childName: String? = null,
    val permissions: List<String>? = null,
)

@Serializable
data class LiveBlock(
    @SerialName("session_id") val sessionId: String? = null,
    val kind: String? = null,                               // "SOS" | "PARENT_REQUEST"
    val from: String? = null,                               // initiator user id
    @SerialName("child_user_id") val childUserId: String? = null,
)

@Serializable
data class MessageBlock(
    @SerialName("message_id") val messageId: String? = null,
    @SerialName("sender_id") val senderId: String? = null,
    @SerialName("chat_id") val chatId: String? = null,
    @SerialName("chat_title") val chatTitle: String? = null,
    @SerialName("chat_type") val chatType: String? = null,
    val text: String? = null,
)