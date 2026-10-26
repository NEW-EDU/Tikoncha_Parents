package uz.tikoncha_parent.data.repository

import uz.tikoncha_parent.data.remote.SafetyApiService
import uz.tikoncha_parent.data.remote.app_error.ApiErrorMapper
import uz.tikoncha_parent.domain.model.app_error.ErrorCause
import uz.tikoncha_parent.domain.model.app_error.Outcome
import uz.tikoncha_parent.domain.model.safety.DangerCategory
import uz.tikoncha_parent.domain.model.safety.DangerPlace
import uz.tikoncha_parent.domain.model.safety.DangerPlaces
import uz.tikoncha_parent.domain.repository.SafetyRepository

class SafetyRepositoryImpl(private val api: SafetyApiService) : SafetyRepository {

    override suspend fun dangerPlaces(lat: Double, lng: Double, radiusM: Int): Outcome<DangerPlaces> =
        apiCall(TAG) {
            val r = api.nearbyZones(lat, lng, radiusM, DangerCategory.apiQuery)
            val body = r.data
            when {
                r.success && body != null -> Outcome.Success(
                    DangerPlaces(
                        places = body.zones.mapNotNull { z ->
                            // Eski server suv/qurilish/temir yo'lni ham qaytaradi — faqat "joy"larni olamiz
                            val category = DangerCategory.fromApi(z.category) ?: return@mapNotNull null
                            val p = z.geometry.firstOrNull()?.takeIf { z.kind == "point" && it.size >= 2 }
                                ?: return@mapNotNull null
                            DangerPlace(
                                id = z.id,
                                category = category,
                                name = z.name?.trim()?.takeIf { it.isNotEmpty() },
                                lat = p[0],
                                lng = p[1],
                                distanceM = z.distanceM,
                            )
                        },
                        attribution = body.attribution.ifBlank { "© OpenStreetMap" },
                    )
                )
                r.success -> Outcome.Failure(ErrorCause.InvalidResponse)
                else -> Outcome.Failure(ApiErrorMapper.fromCode(r.code), r.error)
            }
        }

    private companion object { const val TAG = "SafetyRepository" }
}
