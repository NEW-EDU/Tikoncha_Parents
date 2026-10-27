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

/** Akkauntni öçiriş (App Store 5.1.1(v), Google Play): server öçiradi, so'ng lokal sessiya tozalanadi. */
class DeleteAccountViewModel(
    private val deviceRepository: DeviceRepository,
) : ScreenModel {

    /** Success.data — rejalaştirilgan öçiriş sanasi (yyyy-MM-dd) yoki null (darhol öçdi). */
    private val _state = MutableStateFlow<ResponseState<String>>(ResponseState.Idle)
    val state = _state.asStateFlow()

    fun delete() {
        if (_state.value is ResponseState.Loading) return
        _state.value = ResponseState.Loading
        screenModelScope.launch {
            when (val res = deviceRepository.deleteAccount()) {
                is Outcome.Failure -> _state.value = ResponseState.Error(failure = res)
                is Outcome.Success -> {
                    AppSettings.clearSession()
                    _state.value = ResponseState.Success(res.data?.take(10))
                }
            }
        }
    }

    fun clearError() {
        _state.value = ResponseState.Idle
    }
}
