package uz.tikoncha_parent.data.mapper.protection

import uz.tikoncha_parent.data.remote.model.protection.AccountRequestDto
import uz.tikoncha_parent.data.remote.model.protection.ChildRequestDto
import uz.tikoncha_parent.data.remote.model.protection.ProtectionStatusData
import uz.tikoncha_parent.domain.model.protection.AccountRequest
import uz.tikoncha_parent.domain.model.protection.ChildRequest
import uz.tikoncha_parent.domain.model.protection.ProtectionStatus

fun ChildRequestDto.toChildRequest() = ChildRequest(
    id = id, status = status, childUserId = childUserId, childName = childName,
    createdAt = createdAt, decidedAt = decidedAt, expiresAt = expiresAt,
)

fun AccountRequestDto.toAccountRequest() = AccountRequest(
    id = id, userId = userId, action = action, status = status,
    packages = packages, createdAt = createdAt, modifiedAt = modifiedAt,
)

fun ProtectionStatusData.toProtectionStatus() = ProtectionStatus(
    currentMode = modeStatus?.currentMode,
    strictMethod = modeStatus?.strictMethod,
    unlockData = modeStatus?.unlockData,
    enabledKeys = modeStatus?.enabled.orEmpty(),
    disabledKeys = modeStatus?.disabled.orEmpty(),
    lastSyncAt = lastSyncAt,
    policiesSyncedAt = policiesSyncedAt,
    policiesUpToDate = policiesUpToDate,
    strictDisableRequest = strictDisableRequest?.toChildRequest(),
    logoutRequest = logoutRequest?.toAccountRequest(),
    deleteRequest = deleteRequest?.toAccountRequest(),
)