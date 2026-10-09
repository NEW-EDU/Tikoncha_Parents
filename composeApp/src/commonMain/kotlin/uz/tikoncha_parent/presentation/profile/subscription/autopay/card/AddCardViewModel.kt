package uz.tikoncha_parent.presentation.profile.subscription.autopay.card

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime
import uz.tikoncha_parent.domain.model.app_error.Outcome
import uz.tikoncha_parent.domain.model.autopay.CardVendor
import uz.tikoncha_parent.domain.repository.AutopayRepository
import kotlin.time.Clock

/**
 * Karta qo'shish: raqam + muddat → SMS kod → karta ulanadi. Bu ekran PUL OLMAYDI —
 * pul faqat keyingi "Tasdiqlash" ekranida, foydalanuvchi rozilik bergandan keyin yechiladi.
 * Karta raqami faqat xotirada turadi va so'rov bilan to'g'ridan-to'g'ri serverga ketadi.
 * Karta ota-onaniki — bir karta bir nechta farzand uchun ishlatiladi.
 */
class AddCardViewModel(
    private val autopay: AutopayRepository,
) : ScreenModel {

    private val _state = MutableStateFlow(AddCardState())
    val state = _state.asStateFlow()

    private val _effect = Channel<AddCardEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    private var timer: Job? = null

    fun onEvent(event: AddCardEvent) {
        when (event) {
            is AddCardEvent.NumberChanged -> onNumber(event.value)
            is AddCardEvent.ExpireChanged -> onExpire(event.value)
            AddCardEvent.SendSms -> sendSms()
            is AddCardEvent.OtpChanged -> onOtp(event.value)
            AddCardEvent.Confirm -> confirm()
            AddCardEvent.Resend -> sendSms()
            AddCardEvent.Back -> back()
            AddCardEvent.ErrorShown -> _state.update { it.copy(error = null) }
        }
    }

    private fun onNumber(raw: String) {
        val digits = raw.filter { it.isDigit() }.take(16)
        val vendor = CardVendor.guess(digits)
        _state.update {
            it.copy(
                number = digits,
                vendor = vendor,
                // 4 raqamdan keyin aniq: UZCARD (8600/5614/6262) yoki HUMO (9860) emas
                notSupported = digits.length >= 4 && vendor == CardVendor.OTHER,
                numberInvalid = digits.length == 16 && !luhn(digits),
            )
        }
    }

    private fun onExpire(raw: String) {
        val digits = raw.filter { it.isDigit() }.take(4)
        _state.update { it.copy(expire = digits, expireInvalid = digits.length == 4 && !expireOk(digits)) }
    }

    private fun onOtp(raw: String) {
        val digits = raw.filter { it.isDigit() }.take(6)
        _state.update { it.copy(otp = digits) }
        if (digits.length == 6) confirm()
    }

    private fun sendSms() {
        val s = _state.value
        if (s.submitting || !s.formReady || s.numberInvalid || s.expireInvalid) return
        if (s.step == AddCardStep.OTP && s.resendIn > 0) return
        _state.update { it.copy(submitting = true, error = null) }
        screenModelScope.launch {
            when (val r = autopay.startCard(s.number, s.expire)) {
                is Outcome.Success -> {
                    _state.update {
                        it.copy(submitting = false, step = AddCardStep.OTP, cardId = r.data.id, phoneMask = r.data.phoneMask, otp = "")
                    }
                    startTimer()
                }
                is Outcome.Failure -> _state.update { it.copy(submitting = false, error = r) }
            }
        }
    }

    private fun confirm() {
        val s = _state.value
        val id = s.cardId ?: return
        if (s.submitting || !s.otpReady) return
        _state.update { it.copy(submitting = true, error = null) }
        screenModelScope.launch {
            when (val r = autopay.confirmCard(id, s.otp)) {
                is Outcome.Success -> {
                    timer?.cancel()
                    // Raqam endi kerak emas — xotiradan tozalanadi
                    _state.update { it.copy(submitting = false, number = "", expire = "") }
                    _effect.send(AddCardEffect.Added(r.data))
                }
                is Outcome.Failure -> _state.update { it.copy(submitting = false, error = r, otp = "") }
            }
        }
    }

    private fun back() {
        if (_state.value.step == AddCardStep.OTP) {
            timer?.cancel()
            _state.update { it.copy(step = AddCardStep.FORM, otp = "", cardId = null, resendIn = 0, error = null) }
        } else {
            _effect.trySend(AddCardEffect.Close)
        }
    }

    private fun startTimer() {
        timer?.cancel()
        timer = screenModelScope.launch {
            for (left in RESEND_SECONDS downTo 0) {
                _state.update { it.copy(resendIn = left) }
                if (left > 0) delay(1_000)
            }
        }
    }

    override fun onDispose() {
        _state.update { it.copy(number = "", expire = "", otp = "") }
    }

    companion object {
        const val RESEND_SECONDS = 60

        fun luhn(digits: String): Boolean {
            var sum = 0
            digits.reversed().forEachIndexed { i, ch ->
                var d = ch - '0'
                if (i % 2 == 1) { d *= 2; if (d > 9) d -= 9 }
                sum += d
            }
            return sum % 10 == 0
        }

        /** "MMYY": oy 1..12, o'tib ketmagan (Toshkent vaqti). */
        fun expireOk(
            mmyy: String,
            today: LocalDate = Clock.System.now().toLocalDateTime(TimeZone.of("Asia/Tashkent")).date,
        ): Boolean {
            val month = mmyy.take(2).toIntOrNull() ?: return false
            val year = 2000 + (mmyy.drop(2).toIntOrNull() ?: return false)
            if (month !in 1..12) return false
            return year > today.year || (year == today.year && month >= today.month.number)
        }
    }
}
