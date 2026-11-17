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
    /** Telegram köp işlatiladigan tillardan (özbek, rus, Markaziy Osiyo) boşqa til — Apple/Google birinçi, Telegram keyin. */
    val socialFirst: Boolean = false,
    val loadingProvider: SocialProvider? = null,
    val isPhoneLoading: Boolean = false,          // send-otp loading
    val error: Outcome.Failure? = null,             // Telegram inline xato
    /** Telegram orqali kirish natija bermadi — "boşqa usul bilan kiring" degan aniq xabar körsatiladi. */
    val telegramFailed: Boolean = false,
    /** Telegram tugmasi bosildi, lekin heç narsa oçilmadi (masalan Telegram örnatilmagan) — "Telegram oçilmadi" xabari. */
    val telegramNotOpened: Boolean = false,
    val dialogError: Outcome.Failure? = null,        // phone send-otp dialog xato
) {
    val isPhoneNumberValid get() = DefaultCountry.isComplete(number)
    val showPrefixHint    get() = DefaultCountry.hasUnknownPrefix(number)
    val fullNumber get() = "+998$number"
}