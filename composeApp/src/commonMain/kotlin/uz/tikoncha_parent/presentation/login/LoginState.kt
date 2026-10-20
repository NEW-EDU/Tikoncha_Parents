package uz.tikoncha_parent.presentation.login

import uz.tikoncha_parent.domain.model.app_error.Outcome
import uz.tikoncha_parent.domain.model.auth.SocialProvider
import uz.tikoncha_parent.presentation.base.multi_phone_input.DefaultCountry

data class LoginState(
    val number: String = "",
    val showPhone: Boolean = false,
    val isTelegramLoading: Boolean = false,
    /** Apple tugmasi (faqat iOS). */
    val appleAvailable: Boolean = false,
    /** Ilova tili uz/ru emas — Apple/Google birinchi, Telegram keyin. */
    val socialFirst: Boolean = false,
    val loadingProvider: SocialProvider? = null,
    val isPhoneLoading: Boolean = false,          // send-otp loading
    val error: Outcome.Failure? = null,             // Telegram inline xato
    val dialogError: Outcome.Failure? = null,        // phone send-otp dialog xato
) {
    val isPhoneNumberValid get() = DefaultCountry.isComplete(number)
    val showPrefixHint    get() = DefaultCountry.hasUnknownPrefix(number)
    val fullNumber get() = "+998$number"
}