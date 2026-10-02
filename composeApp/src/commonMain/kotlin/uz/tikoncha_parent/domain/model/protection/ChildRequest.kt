package uz.tikoncha_parent.domain.model.protection

data class ChildRequest(
    val id: String?,
    val status: String?,
    val childUserId: String?,
    val childName: String?,
    val createdAt: String?,
    val decidedAt: String?,
    val expiresAt: String?,
)

data class AccountRequest(
    val id: String?,
    val userId: String?,
    val action: String?,
    val status: String?,
    val packages: List<String>?,
    val createdAt: String?,
    val modifiedAt: String?,
)

data class ProtectionStatus(
    val currentMode: String?,
    val strictMethod: String?,
    val unlockData: String?,
    val enabledKeys: List<String>,
    val disabledKeys: List<String>,
    val lastSyncAt: String?,
    val policiesSyncedAt: String? = null,
    val policiesUpToDate: Boolean? = null,
    val strictDisableRequest: ChildRequest?,
    val logoutRequest: AccountRequest?,
    val deleteRequest: AccountRequest?,
)