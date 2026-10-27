package uz.tikoncha_parent.data.repository

import uz.tikoncha_parent.data.local.AppSettings
import uz.tikoncha_parent.data.outbox.ParentOp
import uz.tikoncha_parent.data.outbox.ParentOutbox
import uz.tikoncha_parent.data.remote.newIdempotencyKey

import uz.tikoncha_parent.data.remote.SafetyApiService
import uz.tikoncha_parent.data.remote.app_error.ApiErrorMapper
import uz.tikoncha_parent.domain.model.app_error.ErrorCause
import uz.tikoncha_parent.domain.model.app_error.Outcome
import uz.tikoncha_parent.domain.model.safety.DangerCategory
import uz.tikoncha_parent.domain.model.safety.DangerPlace
import uz.tikoncha_parent.domain.model.safety.DangerPlaces
import uz.tikoncha_parent.domain.model.safety.SafetySwitch
import uz.tikoncha_parent.domain.model.safety.SafetySwitches
import uz.tikoncha_parent.data.remote.model.ApiEnvelope
import uz.tikoncha_parent.data.remote.model.safety.SafetySettingsData
import uz.tikoncha_parent.data.remote.model.safety.SafetySettingsRequest
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

    /** Offline-first: oxirgi ma'lum holat (diskda) + navbatdagi kalitlar. Offline ochilganda ham körinadi. */
    override suspend fun switches(childId: String): Outcome<SafetySwitches> {
        val r = apiCall(TAG) { api.settings(childId).toSwitches() }
        if (r is Outcome.Success) {
            remember(childId, r.data)
            return Outcome.Success(withPending(childId, r.data))
        }
        if (r is Outcome.Failure && r.cause.isUnreachable) cached(childId)?.let { return Outcome.Success(withPending(childId, it)) }
        return r
    }

    override suspend fun setSwitch(childId: String, switch: SafetySwitch, on: Boolean): Outcome<SafetySwitches> {
        val body = when (switch) {
            SafetySwitch.DANGER_ZONES -> SafetySettingsRequest(childId, dangerZones = on)
            SafetySwitch.IMAGE -> SafetySettingsRequest(childId, imageDetection = on)
            SafetySwitch.CONVERSATION -> SafetySettingsRequest(childId, conversation = on)
            SafetySwitch.INCOGNITO -> SafetySettingsRequest(childId, blockIncognito = on)
        }
        val key = newIdempotencyKey()
        if (pending(childId).isEmpty()) {
            val r = apiCall(TAG) { api.putSettings(body, key).toSwitches() }
            if (r is Outcome.Success) remember(childId, r.data)
            if (r !is Outcome.Failure || !r.cause.isUnreachable) return r
        }
        // Server yetib bölmadi — oxirgi holat ustiga qöyib navbatga (keş yöq bölsa — xato)
        val base = cached(childId) ?: return Outcome.Failure(ErrorCause.NoInternet)
        ParentOutbox.add(ParentOp(key = key, kind = ParentOp.Kind.SAFETY, childId = childId, safety = body,
            createdAt = kotlin.time.Clock.System.now().toString()))
        return Outcome.Success(withPending(childId, base))
    }

    override suspend fun flushOutbox(): Outcome<Int> {
        var sent = 0
        for (op in ParentOutbox.current().filter { it.kind == ParentOp.Kind.SAFETY }) {
            val body = op.safety ?: run { ParentOutbox.remove(op.key); null } ?: continue
            when (val r = apiCall(TAG) { api.putSettings(body, op.key).toSwitches() }) {
                is Outcome.Success -> {
                    ParentOutbox.remove(op.key)
                    remember(op.childId, r.data)
                    sent++
                }
                is Outcome.Failure ->
                    if (r.cause == ErrorCause.SessionExpired || r.cause.isUnreachable) return r
                    else ParentOutbox.remove(op.key)   // rad etildi (masalan PLUS tugagan) — server holati ustun
            }
        }
        return Outcome.Success(sent)
    }

    private fun pending(childId: String) = ParentOutbox.current().filter { it.kind == ParentOp.Kind.SAFETY && it.childId == childId }

    private fun withPending(childId: String, base: SafetySwitches): SafetySwitches =
        pending(childId).mapNotNull { it.safety }.fold(base) { s, b ->
            s.copy(
                dangerZones = b.dangerZones ?: s.dangerZones,
                conversation = b.conversation ?: s.conversation,
                image = b.imageDetection ?: s.image,
                incognito = b.blockIncognito ?: s.incognito,
            )
        }

    private fun remember(childId: String, s: SafetySwitches) =
        AppSettings.setOfflineValue("safety_$childId", "${s.dangerZones},${s.conversation},${s.image},${s.incognito}")

    private fun cached(childId: String): SafetySwitches? =
        AppSettings.offlineValue("safety_$childId")?.split(",")?.takeIf { it.size == 4 }?.map { it == "true" }
            ?.let { SafetySwitches(it[0], it[1], it[2], it[3]) }

    private fun ApiEnvelope<SafetySettingsData>.toSwitches(): Outcome<SafetySwitches> {
        val d = data
        return when {
            success && d != null -> Outcome.Success(SafetySwitches(d.dangerZones, d.conversation, d.imageDetection, d.blockIncognito))
            success -> Outcome.Failure(ErrorCause.InvalidResponse)
            else -> Outcome.Failure(ApiErrorMapper.fromCode(code), error)
        }
    }

    private companion object { const val TAG = "SafetyRepository" }
}
