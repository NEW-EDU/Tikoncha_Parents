package uz.tikoncha_parent.data.repository

import uz.tikoncha_parent.common.AppCode
import uz.tikoncha_parent.data.remote.DeviceApiService
import uz.tikoncha_parent.data.remote.app_error.ApiErrorMapper
import uz.tikoncha_parent.data.remote.device.LogoutResponse
import uz.tikoncha_parent.data.remote.model.DeviceRegisterRequest
import uz.tikoncha_parent.domain.model.app_error.Outcome
import uz.tikoncha_parent.domain.repository.DeviceRepository
import uz.tikoncha_parent.platform.getDeviceInfo

class DeviceRepositoryImpl(
    private val api: DeviceApiService
): DeviceRepository {
    override suspend fun registerDevice(fcmToken: String): Outcome<Unit> =
        apiCall(TAG) {
            val info = getDeviceInfo()
            val r = api.registerDevice(
                DeviceRegisterRequest(
                    manufacturer = info.manufacturer,
                    model_name = info.modelName,
                    os = info.os,
                    os_version = info.osVersion,
                    fcm_token = fcmToken,
                    app_code = AppCode.currentAppCode,
                )
            )
            when {
                r.success -> Outcome.Success(Unit)
                else -> Outcome.Failure(ApiErrorMapper.fromCode(r.code), r.error)
            }
        }

    override suspend fun logout(fcmToken: String): Outcome<Unit> = apiCall(TAG) {
        val r = api.logout(fcmToken)
        if (r.success) Outcome.Success(Unit)
        else Outcome.Failure(ApiErrorMapper.fromCode(null), r.error)
    }

    override suspend fun deleteAccount(): Outcome<String?> = apiCall(TAG) {
        val r = api.deleteAccount()
        if (r.success) Outcome.Success(r.data?.scheduled_for?.takeIf { r.data.status == "scheduled" })
        else Outcome.Failure(ApiErrorMapper.fromCode(null), r.error)
    }

    override suspend fun scheduledDeletion(): Outcome<String?> = apiCall(TAG) {
        val r = api.deletionStatus()
        if (r.success) Outcome.Success(r.data?.scheduled_for)
        else Outcome.Failure(ApiErrorMapper.fromCode(null), r.error)
    }

    override suspend fun cancelDeletion(): Outcome<Unit> = apiCall(TAG) {
        val r = api.cancelDeletion()
        if (r.success) Outcome.Success(Unit)
        else Outcome.Failure(ApiErrorMapper.fromCode(null), r.error)
    }

    private companion object { const val TAG = "DeviceRepository" }
}