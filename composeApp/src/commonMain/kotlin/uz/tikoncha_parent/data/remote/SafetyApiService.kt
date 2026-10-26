package uz.tikoncha_parent.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.request.parameter
import io.ktor.http.HttpMethod
import uz.tikoncha_parent.data.remote.model.ApiEnvelope
import uz.tikoncha_parent.data.remote.model.safety.NearbyZonesData

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
}
