package uz.tikoncha_parent.data.remote.model

import kotlinx.serialization.Serializable


@Serializable
data class UserInfoDto(
    val user_id: String? = null,
    val phone_number: String? = null,
    val first_name: String?,
    val last_name: String?,
    val patronymic: String?,
    val age: Int?,
    val gender: String?,
    val school_id: String? = null,
    val school_name: String? = null,
    val school_class_name: String? = null,
    val school_class_id: String? = null,
    val shift: String? = null,
    val passport_id: String? = null,
    val avatar_url: String? = null,
    val last_seen: String? = null,
    val subscription: String? = null,
    val subscription_end_date: String? = null,
    /** "phone" | "google" | "apple" — telefon raqami yo'q farzandni tanish uchun. */
    val sign_in_method: String? = null,
    /** Google / Apple email; Apple "Hide My Email" bo'lsa null va email_hidden = true. */
    val email: String? = null,
    val email_hidden: Boolean? = null,
)
