package uz.tikoncha_parent.domain.repository

import uz.tikoncha_parent.data.remote.device.LogoutResponse
import uz.tikoncha_parent.domain.model.app_error.Outcome

interface DeviceRepository {
    suspend fun registerDevice(fcmToken: String): Outcome<Unit>
    suspend fun logout(fcmToken: String): Outcome<Unit>
    suspend fun deleteAccount(): Outcome<Unit>
}