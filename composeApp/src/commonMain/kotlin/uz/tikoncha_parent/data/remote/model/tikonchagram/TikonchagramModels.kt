package uz.tikoncha_parent.data.remote.model.tikonchagram

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TgPolicyResponse(
    val success: Boolean = false,
    val data: TgPolicyDto? = null,
    val error: String? = null,
    val code: Int? = null,
)

/** `GET/PUT /users/children/{child_id}/tikonchagram-policy`. Yo'q maydon — qat'iy (STRICT) qiymat. */
@Serializable
data class TgPolicyDto(
    val enabled: Boolean = true,
    @SerialName("hide_channels") val hideChannels: Boolean = true,
    @SerialName("hide_groups") val hideGroups: Boolean = true,
    @SerialName("hide_bots") val hideBots: Boolean = true,
    @SerialName("contacts_only") val contactsOnly: Boolean = false,
    @SerialName("allow_new_chats") val allowNewChats: Boolean = false,
    @SerialName("allow_stickers") val allowStickers: Boolean = false,
    @SerialName("allow_gifs") val allowGifs: Boolean = false,
    @SerialName("allow_photos") val allowPhotos: Boolean = true,
    @SerialName("allow_videos") val allowVideos: Boolean = false,
    @SerialName("allow_voice") val allowVoice: Boolean = false,
    @SerialName("allow_video_notes") val allowVideoNotes: Boolean = false,
    @SerialName("allow_documents") val allowDocuments: Boolean = false,
    @SerialName("allow_links") val allowLinks: Boolean = false,
    @SerialName("allow_forwards") val allowForwards: Boolean = false,
    @SerialName("show_blocked_placeholder") val showBlockedPlaceholder: Boolean = true,
    @SerialName("max_chats") val maxChats: Int = 30,
    @SerialName("daily_max_minutes") val dailyMaxMinutes: Int? = null,
    /** Faqat javobda. */
    @SerialName("is_default") val isDefault: Boolean? = null,
)
