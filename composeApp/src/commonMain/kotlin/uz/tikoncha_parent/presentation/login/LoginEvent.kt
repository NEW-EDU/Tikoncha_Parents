package uz.tikoncha_parent.presentation.login

import uz.tikoncha_parent.domain.model.auth.SocialProvider

sealed interface LoginEvent {
    data class OnNumberInsert(val number: String) : LoginEvent
    data object OnTelegramClicked : LoginEvent
    data class OnSocialClicked(val provider: SocialProvider) : LoginEvent
    data object OnPhoneContinue : LoginEvent          // YANGI
    data object OnErrorDismissed : LoginEvent
    data object OnDialogErrorDismissed : LoginEvent    // YANGI
    data object OnTelegramReturned : LoginEvent
}