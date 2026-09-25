package uz.tikoncha_parent.presentation.profile.logout

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import uz.tikoncha_parent.data.local.AppSettings
import uz.tikoncha_parent.domain.model.app_error.Outcome
import uz.tikoncha_parent.domain.repository.DeviceRepository
import uz.tikoncha_parent.presentation.ui_state.ResponseState

class LogoutViewModel(
    private val deviceRepository: DeviceRepository,
) : ScreenModel {

    private val _state = MutableStateFlow<ResponseState<Nothing>>(ResponseState.Idle)
    val state = _state.asStateFlow()

    /** Qurilmani serverdan chiqaradi, so'ng lokal sessiyani tozalaydi. */
    fun logout() {
        if (_state.value is ResponseState.Loading) return
        _state.value = ResponseState.Loading

        screenModelScope.launch {
            when (val res = deviceRepository.logout(AppSettings.fcmToken)) {
                is Outcome.Failure -> _state.value = ResponseState.Error(failure = res)
                is Outcome.Success -> {
                    AppSettings.clearSession()
                    _state.value = ResponseState.Success()
                }
            }
        }
    }

    fun clearError() {
        _state.value = ResponseState.Idle
    }
}