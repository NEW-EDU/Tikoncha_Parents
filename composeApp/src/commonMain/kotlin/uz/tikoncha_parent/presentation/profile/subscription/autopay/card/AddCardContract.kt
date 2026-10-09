package uz.tikoncha_parent.presentation.profile.subscription.autopay.card

import uz.tikoncha_parent.domain.model.app_error.Outcome
import uz.tikoncha_parent.domain.model.autopay.CardVendor
import uz.tikoncha_parent.domain.model.autopay.PayCard

enum class AddCardStep { FORM, OTP }

data class AddCardState(
    val step: AddCardStep = AddCardStep.FORM,
    /** Faqat raqamlar, ko'pi bilan 16 ta. Ekranda 4 tadan guruhlab ko'rsatiladi. */
    val number: String = "",
    /** Faqat raqamlar "MMYY", ko'pi bilan 4 ta. */
    val expire: String = "",
    val vendor: CardVendor = CardVendor.OTHER,
    val numberInvalid: Boolean = false,
    val expireInvalid: Boolean = false,
    /** UZCARD/HUMO emas (Visa, Mastercard …). */
    val notSupported: Boolean = false,
    val cardId: String? = null,
    val phoneMask: String = "",
    val otp: String = "",
    val resendIn: Int = 0,
    val submitting: Boolean = false,
    val error: Outcome.Failure? = null,
) {
    val formReady: Boolean get() = number.length == 16 && expire.length == 4 && !notSupported
    val otpReady: Boolean get() = otp.length == 6
}

sealed interface AddCardEvent {
    data class NumberChanged(val value: String) : AddCardEvent
    data class ExpireChanged(val value: String) : AddCardEvent
    data object SendSms : AddCardEvent
    data class OtpChanged(val value: String) : AddCardEvent
    data object Confirm : AddCardEvent
    data object Resend : AddCardEvent
    data object Back : AddCardEvent
    data object ErrorShown : AddCardEvent
}

sealed interface AddCardEffect {
    data class Added(val card: PayCard) : AddCardEffect
    data object Close : AddCardEffect
}
