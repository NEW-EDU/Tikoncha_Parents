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
    suspend fun deleteAccount(): LogoutResponse =
        httpClient.safeRequest(
            method = HttpMethod.Delete,
            url = "users/",
            block = {}
        )




}