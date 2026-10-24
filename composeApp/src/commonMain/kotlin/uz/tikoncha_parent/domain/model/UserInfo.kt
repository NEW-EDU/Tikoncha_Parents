package uz.tikoncha_parent.domain.model

import cafe.adriel.voyager.core.lifecycle.JavaSerializable
import kotlinx.serialization.Serializable

@Serializable
data class UserInfo(
    val userId: String,
    val phoneNumber: String,
    val fullName: String,
    val name: String,
    val lastName: String,
    val patronymic: String,
    val genderType: GenderType,
    val passportId: String? = null,
    val age: Int? = null,
    val schoolId: String? = null,
    val schoolName: String? = null,
    val schoolClassName: String? = null,
    val schoolClassId: String? = null,
    val shift: String? = null,
    val avatarUrl: String? = null,
    val last_seen: String? = null,
    val subscription: String? = null,
    val subscription_end_date: String? = null,
    val signInMethod: String? = null,
    val email: String? = null,
    val emailHidden: Boolean = false,
): JavaSerializable{
    override fun toString(): String {
        return name
    }
}
