package uz.tikoncha_parent.presentation.add_child

import uz.tikoncha_parent.presentation.base.multi_phone_input.Country

sealed interface AddChildEvent {
    data class PhoneChanged(val value: String) : AddChildEvent
    data class OnCountryChange(val country: Country) : AddChildEvent
    data object RequestCode : AddChildEvent
    data object RefreshCode : AddChildEvent
    data object CodeCopied : AddChildEvent
    data object OpenAppLink : AddChildEvent
    data object ShareLink : AddChildEvent
    data object PlayTutorial : AddChildEvent
    data object DismissError : AddChildEvent
    data object DismissSnackbar : AddChildEvent
    data object SuccessAcknowledged : AddChildEvent
    data object NavigateBack : AddChildEvent
}