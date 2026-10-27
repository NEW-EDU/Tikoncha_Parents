package uz.tikoncha_parent.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.request.parameter
import io.ktor.client.request.setBody
import io.ktor.http.HttpMethod
import uz.tikoncha_parent.data.remote.device.LogoutResponse
import uz.tikoncha_parent.data.remote.model.DeviceRegisterRequest
import uz.tikoncha_parent.data.remote.model.DeviceRegisterResponse

class DeviceApiService (
    private val httpClient: HttpClient
) {
    suspend fun registerDevice(request: DeviceRegisterRequest): DeviceRegisterResponse =
        httpClient.safeRequest(
            method = HttpMethod.Post,
            url = "devices/register",
            block = {
                setBody(request)
            }
        )

    suspend fun logout(fcmToken: String): LogoutResponse =
        httpClient.safeRequest(
            method = HttpMethod.Post,
            url = "devices/logout",
            block = {
                parameter("fcm_token", fcmToken)
            }
        )

    /** Akkauntni butunlay öçiriş (`DELETE /users/`): profil va unga boğliq hamma narsa kaskad bilan öçadi. */
    suspend fun deleteAccount(): uz.tikoncha_parent.data.remote.device.DeleteAccountResponse =
        httpClient.safeRequest(
            method = HttpMethod.Delete,
            url = "users/",
            block = {}
        )

    suspend fun deletionStatus(): uz.tikoncha_parent.data.remote.device.DeletionStatusResponse =
        httpClient.safeRequest(method = HttpMethod.Get, url = "users/me/deletion", block = {})

    suspend fun contactPhone(): uz.tikoncha_parent.data.remote.device.ContactPhoneResponse =
        httpClient.safeRequest(method = HttpMethod.Get, url = "users/me/contact-phone", block = {})

    suspend fun setContactPhone(phone: String): uz.tikoncha_parent.data.remote.device.ContactPhoneResponse =
        httpClient.safeRequest(method = HttpMethod.Put, url = "users/me/contact-phone", block = {
            setBody(uz.tikoncha_parent.data.remote.device.ContactPhoneRequest(phone))
        })

    /** Rejalaştirilgan öçirişni bekor qiliş ("Akkauntni saqlaş"). */
    suspend fun cancelDeletion(): uz.tikoncha_parent.data.remote.device.DeletionStatusResponse =
        httpClient.safeRequest(method = HttpMethod.Post, url = "users/me/cancel-deletion", block = {})




}