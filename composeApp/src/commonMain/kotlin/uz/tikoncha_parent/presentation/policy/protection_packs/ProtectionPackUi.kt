package uz.tikoncha_parent.presentation.policy.protection_packs

data class ProtectionPackUi(
    val code: String,
    val title: String,
    val description: String?,
    val packageCount: Int,
    val siteCount: Int,
    val enabledByMe: Boolean,
    val enabledByCoParent: Boolean,
    val enabledByChild: Boolean,
    val inProgress: Boolean = false,
)