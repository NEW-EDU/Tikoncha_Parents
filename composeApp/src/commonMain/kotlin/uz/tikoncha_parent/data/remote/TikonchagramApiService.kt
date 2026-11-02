package uz.tikoncha_parent.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.request.setBody
import io.ktor.http.HttpMethod
import uz.tikoncha_parent.data.remote.model.tikonchagram.TgPolicyDto
import uz.tikoncha_parent.data.remote.model.tikonchagram.TgPolicyResponse

class TikonchagramApiService(private val client: HttpClient) {

    suspend fun policy(childId: String): TgPolicyResponse =
        client.safeRequest(
            method = HttpMethod.Get,
            url = "/users/children/$childId/tikonchagram-policy",
        )

    suspend fun savePolicy(childId: String, body: TgPolicyDto): TgPolicyResponse =
        client.safeRequest(
            method = HttpMethod.Put,
            url = "/users/children/$childId/tikonchagram-policy",
            block = { setBody(body) },
        )
}
