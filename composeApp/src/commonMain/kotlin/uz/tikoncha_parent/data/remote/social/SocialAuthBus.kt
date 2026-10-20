package uz.tikoncha_parent.data.remote.social

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import uz.tikoncha_parent.domain.model.auth.SocialAuthResult

/** Apple/Google kirish natijasi — TelegramAuthBus bilan bir xil: platformalar yozadi, LoginViewModel o'qiydi. */
object SocialAuthBus {
    private val _pendingResult = MutableStateFlow<SocialAuthResult?>(null)
    val pendingResult: StateFlow<SocialAuthResult?> = _pendingResult.asStateFlow()

    fun emitResult(result: SocialAuthResult) { _pendingResult.value = result }
    fun clearPending() { _pendingResult.value = null }
}
