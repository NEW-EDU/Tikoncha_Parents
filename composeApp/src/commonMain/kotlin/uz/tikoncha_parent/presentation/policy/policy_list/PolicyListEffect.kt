package uz.tikoncha_parent.presentation.policy.policy_list

import uz.tikoncha_parent.domain.model.app_error.Outcome
import uz.tikoncha_parent.domain.model.app_error.PaidFeature

sealed interface PolicyListEffect {
    data class ShowError(val failure: Outcome.Failure) : PolicyListEffect
    /** 403 — pullik imkoniyat; [failure] dagi server matni dialogda ko'rsatiladi. */
    data class ShowPremium(val feature: PaidFeature, val failure: Outcome.Failure) : PolicyListEffect
}