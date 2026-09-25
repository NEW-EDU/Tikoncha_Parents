package uz.tikoncha_parent.presentation.policy.protection_packs

import uz.tikoncha_parent.domain.model.UserInfo

sealed interface ProtectionPacksEvent {
    data object Load : ProtectionPacksEvent
    data object PullRefresh : ProtectionPacksEvent
    data class OnChildSelected(val child: UserInfo) : ProtectionPacksEvent
    data class Toggle(val code: String, val enabled: Boolean) : ProtectionPacksEvent
    data object DismissPremium : ProtectionPacksEvent
}