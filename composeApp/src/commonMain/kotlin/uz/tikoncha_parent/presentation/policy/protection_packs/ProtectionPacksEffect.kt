package uz.tikoncha_parent.presentation.policy.protection_packs

import uz.tikoncha_parent.domain.model.app_error.Outcome

sealed interface ProtectionPacksEffect {
    data class ShowError(val failure: Outcome.Failure) : ProtectionPacksEffect
}