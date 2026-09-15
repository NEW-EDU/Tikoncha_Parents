package uz.tikoncha_parent.presentation.policy.policy_setup

sealed interface PolicySetupEffect {
    data object NoTitleToast: PolicySetupEffect
    data object NoRuleSelectedToast: PolicySetupEffect
    data object NoAppWebSelectedToast: PolicySetupEffect
    data object NothingChangedToast: PolicySetupEffect
}