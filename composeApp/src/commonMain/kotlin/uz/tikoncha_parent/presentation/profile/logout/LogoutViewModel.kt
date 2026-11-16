package uz.tikoncha_parent.presentation.profile.logout

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import uz.tikoncha_parent.data.local.AppSettings
import uz.tikoncha_parent.data.remote.AuthEventBus
import uz.tikoncha_parent.domain.repository.DeviceRepository
import uz.tikoncha_parent.presentation.ui_state.ResponseState

class LogoutViewModel(
    private val deviceRepository: DeviceRepository,
) : ScreenModel {

    private val _state = MutableStateFlow<ResponseState<Nothing>>(ResponseState.Idle)
    val state = _state.asStateFlow()

    /**
     * Qurilmani serverdan chiqaradi, so'ng lokal sessiyani tozalaydi.
     * Çiqiş har doim lokal bajariladi: server javob bermasa yoki sessiya allaqaçon yaroqsiz bölsa ham
     * (akkaunt saytdan öçirilgan — "Invalid authentication credentials") foydalanuvçi ilovada qamalib qolmaydi.
     */
    fun logout() {
        if (_state.value is ResponseState.Loading) return
        _state.value = ResponseState.Loading

        screenModelScope.launch {
            AuthEventBus.muted = true
            try {
                deviceRepository.logout(AppSettings.fcmToken)
            } finally {
                AppSettings.clearSession()
                AuthEventBus.muted = false
            }
            _state.value = ResponseState.Success()
        }
    }

    fun clearError() {
        _state.value = ResponseState.Idle
    }
}