package uz.tikoncha_parent.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.request.parameter
import io.ktor.client.request.header
import io.ktor.client.request.setBody
import io.ktor.http.HttpMethod
import uz.tikoncha_parent.data.remote.model.ApiEnvelope
import uz.tikoncha_parent.data.remote.model.safety.NearbyZonesData
import uz.tikoncha_parent.data.remote.model.safety.SafetySettingsData
import uz.tikoncha_parent.data.remote.model.safety.SafetySettingsRequest

/** Farzandim xavfsizlik moduli (`/safety`). */
class SafetyApiService(
    private val httpClient: HttpClient,
) {
    /**
     * Nuqta atrofidagi xavfli joylar. [categories] — vergul bilan (masalan "nightlife,gambling");
     * eski server bu parametrni e'tiborsiz qoldiradi, shuning uchun mijoz ham o'zi saralaydi.
     */
    suspend fun nearbyZones(
        lat: Double,
        lng: Double,
        radiusM: Int,
        categories: String,
    ): ApiEnvelope<NearbyZonesData> =
        httpClient.safeRequest(
            method = HttpMethod.Get,
            url = "safety/zones/nearby",
            block = {
                parameter("lat", lat)
                parameter("lng", lng)
                parameter("radius_m", radiusM)
                parameter("categories", categories)
            }
        )

    /** Farzandning maxsus himoyalari (ota-ona yoqqanlari). */
    suspend fun settings(childId: String): ApiEnvelope<SafetySettingsData> =
        httpClient.safeRequest(method = HttpMethod.Get, url = "safety/settings", block = { parameter("child_id", childId) })

    suspend fun putSettings(body: SafetySettingsRequest, key: String? = null): ApiEnvelope<SafetySettingsData> =
        httpClient.safeRequest(method = HttpMethod.Put, url = "safety/settings", block = {
            setBody(body)
            key?.let { header(IDEMPOTENCY_KEY, it) }
        })
}
