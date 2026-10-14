package uz.tikoncha_parent.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.request.setBody
import io.ktor.http.HttpMethod
import uz.tikoncha_parent.data.remote.model.live.IdentityKeyRequest
import uz.tikoncha_parent.data.remote.model.live.IdentityKeyResponse
import uz.tikoncha_parent.data.remote.model.live.PeerKeysResponse
import uz.tikoncha_parent.data.remote.model.live.SessionInitiateRequest
import uz.tikoncha_parent.data.remote.model.live.SessionInitiateResponse
import uz.tikoncha_parent.data.remote.model.live.SessionResponse
import uz.tikoncha_parent.data.remote.model.live.TurnCredentialsResponse

/**
 * `/live` REST endpointlari — ota-ona (viewer) tomoni. Signalizatsiyaning o'zi WebSocket
 * (`LiveSignalingService`) orqali ketadi; bu yerda sessiya hayot sikli + TURN + identlik kalitlari.
 */
class LiveApiService(
    private val httpClient: HttpClient,
) {
    suspend fun turnCredentials(): TurnCredentialsResponse =
        httpClient.safeRequest(method = HttpMethod.Get, url = "/live/turn-credentials")

    /** Ring the child (parent request). Capture starts only after the child accepts. */
    suspend fun ringChild(childUserId: String): SessionInitiateResponse =
        httpClient.safeRequest(method = HttpMethod.Post, url = "/live/sessions") {
            setBody(SessionInitiateRequest(kind = "PARENT_REQUEST", child_user_id = childUserId))
        }

    suspend fun cancel(sessionId: String): SessionResponse =
        httpClient.safeRequest(method = HttpMethod.Post, url = "/live/sessions/$sessionId/cancel")

    suspend fun end(sessionId: String): SessionResponse =
        httpClient.safeRequest(method = HttpMethod.Post, url = "/live/sessions/$sessionId/end")

    suspend fun registerIdentityKey(body: IdentityKeyRequest): IdentityKeyResponse =
        httpClient.safeRequest(method = HttpMethod.Post, url = "/live/identity-keys") {
            setBody(body)
        }

    /** The child's active public keys — to verify its offer/ICE end-to-end. */
    suspend fun peerKeys(peerUserId: String): PeerKeysResponse =
        httpClient.safeRequest(method = HttpMethod.Get, url = "/live/identity-keys/$peerUserId")
}
