package uz.tikoncha_parent.domain.repository

import uz.tikoncha_parent.data.remote.device.LogoutResponse
import uz.tikoncha_parent.domain.model.app_error.Outcome

interface DeviceRepository {
    suspend fun registerDevice(fcmToken: String): Outcome<Unit>
    suspend fun logout(fcmToken: String): Outcome<Unit>
    /** @return öçiriş sanasi (ISO), agar akkaunt 7 kunlik muddat bilan rejalaştirilgan bölsa; darhol öçsa null. */
    suspend fun deleteAccount(): Outcome<String?>
    /** @return rejalaştirilgan öçiriş sanasi (ISO) yoki null. */
    suspend fun scheduledDeletion(): Outcome<String?>
    suspend fun cancelDeletion(): Outcome<Unit>
}