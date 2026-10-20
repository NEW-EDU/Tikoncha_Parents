package uz.tikoncha_parent.domain.model.auth

enum class SocialProvider { APPLE, GOOGLE }

/** Apple / Google tizim oynasidan qaytgan natija (platforma uni [uz.tikoncha_parent.data.remote.social.SocialAuthBus] ga yozadi). */
sealed interface SocialAuthResult {
    /** [token] — Apple identity token yoki Google ID token; [fullName] faqat Apple birinchi kirishda beradi. */
    data class Success(val provider: SocialProvider, val token: String, val fullName: String? = null) : SocialAuthResult
    data class Error(val provider: SocialProvider, val message: String) : SocialAuthResult
    data class Cancelled(val provider: SocialProvider) : SocialAuthResult
}
