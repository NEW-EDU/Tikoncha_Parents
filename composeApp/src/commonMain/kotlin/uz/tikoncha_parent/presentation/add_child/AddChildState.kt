package uz.tikoncha_parent.presentation.add_child

import uz.tikoncha_parent.domain.model.UserInfo
import uz.tikoncha_parent.domain.model.app_error.Outcome
import uz.tikoncha_parent.presentation.base.multi_phone_input.Country
import uz.tikoncha_parent.presentation.base.multi_phone_input.DefaultCountry

data class AddChildState(
    val phoneNumber: String = "",            // raw digits, max 9
    val selectedCountry: Country = DefaultCountry,
    val requestedPhone: String = "",         // phone used in last successful request
    val code: String? = null,                // OTP code from server
    val codeExpiresAt: Long = 0L,
    val remainingSeconds: Int = 0,
    val isLoading: Boolean = false,          // requesting / refreshing
    val showBindChildTutorial: Boolean = false,  // tutorial card -> header icon
    val error: Outcome.Failure? = null,    // error from Resource.Error
    val showCopiedSnackbar: Boolean = false,  // "Kod nusxalandi" toast
    val linkedChild: UserInfo? = null        // farzand kodni tasdiqladi -> muvaffaqiyat oynasi
) {

    val fullPhoneNumber : String get() =
        "${selectedCountry.dial}${phoneNumber}"
    val isPhoneValid: Boolean
        get() = selectedCountry.isComplete(phoneNumber)

    val isCodeExpired: Boolean get() = code != null && codeExpiresAt > 0L && remainingSeconds <= 0

    val remainingText: String get() = "${remainingSeconds / 60}:${(remainingSeconds % 60).toString().padStart(2,'0')}"

    val showPrefixHint: Boolean
        get() = selectedCountry.hasUnknownPrefix(phoneNumber)

    /** Code is shown only when phone matches the phone we requested for. */
    val showCodeCard: Boolean
        get() = !code.isNullOrEmpty() && phoneNumber == requestedPhone

    val canRequestCode: Boolean
        get() = isPhoneValid && !isLoading && !showCodeCard
}