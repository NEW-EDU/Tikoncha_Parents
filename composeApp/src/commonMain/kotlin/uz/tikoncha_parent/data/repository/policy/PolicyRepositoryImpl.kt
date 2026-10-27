package uz.tikoncha_parent.data.repository.policy

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.Serializable
import uz.tikoncha_parent.data.local.AppSettings
import uz.tikoncha_parent.data.outbox.LOCAL_POLICY_PREFIX
import uz.tikoncha_parent.data.outbox.ParentOp
import uz.tikoncha_parent.data.outbox.ParentOutbox
import uz.tikoncha_parent.data.outbox.PolicyOverlay
import uz.tikoncha_parent.data.outbox.offlineJson
import uz.tikoncha_parent.data.remote.model.policy.PolicyOutDto
import uz.tikoncha_parent.data.remote.newIdempotencyKey
import kotlin.time.Clock
import uz.tikoncha_parent.data.mapper.policy.toCreateDto
import uz.tikoncha_parent.data.mapper.policy.toDomain
import uz.tikoncha_parent.data.mapper.policy.toDto
import uz.tikoncha_parent.data.mapper.policy.toInstantOrNow
import uz.tikoncha_parent.data.mapper.policy.toJsonObject
import uz.tikoncha_parent.data.remote.app_error.ApiErrorMapper
import uz.tikoncha_parent.data.remote.app_error.PolicyCall
import uz.tikoncha_parent.data.remote.app_error.PolicyErrorMapper
import uz.tikoncha_parent.data.mapper.toDomain
import uz.tikoncha_parent.domain.model.apps.InstalledApp
import uz.tikoncha_parent.data.remote.policy.PolicyApiService
import uz.tikoncha_parent.data.repository.apiCall
import uz.tikoncha_parent.domain.model.app_error.ErrorCause
import uz.tikoncha_parent.domain.model.app_error.Outcome
import uz.tikoncha_parent.domain.model.policy.Policy
import uz.tikoncha_parent.domain.model.policy.PolicyDraft
import uz.tikoncha_parent.domain.model.policy.PolicyListSnapshot
import uz.tikoncha_parent.domain.model.policy.PolicyPatch
import uz.tikoncha_parent.domain.repository.policy.PolicyRepository

class PolicyRepositoryImpl(
    private val api: PolicyApiService,
) : PolicyRepository {

    /**
     * Bola bo'yicha kesh. Bitta StateFlow — hosila oqimlar undan `map` bilan olinadi.
     * Offline-first: ekrandagi röyxat = serverning oxirgi holati ([raw], diskda ham saqlanadi) + navbatdagi
     * özgarişlar (ParentOutbox). Ilova offline ochilsa ham oxirgi röyxat körinadi.
     */
    private val snapshots = MutableStateFlow<Map<String, PolicyListSnapshot>>(emptyMap())

    @Serializable
    private data class RawCache(val items: List<PolicyOutDto>, val asOf: String, val packsVersion: Int)

    private val raw = mutableMapOf<String, RawCache>()
    private val rawLock = Mutex()

    override fun observePolicies(childId: String): Flow<List<Policy>> {
        ensureLoaded(childId)
        return snapshots.map { it[childId]?.items.orEmpty() }.distinctUntilChanged()
    }

    override fun cachedPolicies(childId: String): List<Policy> {
        ensureLoaded(childId)
        return snapshots.value[childId]?.items.orEmpty()
    }

    override fun isCached(childId: String): Boolean {
        ensureLoaded(childId)
        return snapshots.value.containsKey(childId)
    }

    override suspend fun refreshPolicies(childId: String, force: Boolean): Outcome<PolicyListSnapshot> =
        apiCall(TAG) {
            ensureLoaded(childId)
            val previous = if (force) null else raw[childId]
            val r = api.list(childId, since = previous?.asOf)
            val body = r.data
            when {
                r.success && body != null -> {
                    val merged =
                        if (previous == null) body.items.filter { it.deleted_at == null }
                        else applyDelta(previous.items, body.items)
                    storeRaw(childId, RawCache(merged, body.as_of, body.packs_version))
                    Outcome.Success(snapshots.value.getValue(childId))
                }

                r.success -> Outcome.Failure(ErrorCause.InvalidResponse)
                else -> Outcome.Failure(PolicyErrorMapper.from(PolicyCall.LIST, r.code), r.error)
            }
        }

    override suspend fun createPolicy(childId: String, draft: PolicyDraft): Outcome<Policy> {
        val key = newIdempotencyKey()
        val op = ParentOp(
            key = key, kind = ParentOp.Kind.POLICY_CREATE, childId = childId,
            policyId = LOCAL_POLICY_PREFIX + key, create = draft.toCreateDto(childId), createdAt = nowIso(),
        )
        return when (val r = submit(op, PolicyCall.CREATE, draft)) {
            is Outcome.Success -> r.data?.let { Outcome.Success(it.toDomain()) } ?: queuedPolicy(childId, op.policyId!!)
            is Outcome.Failure -> r
        }
    }

    override suspend fun patchPolicy(policyId: String, patch: PolicyPatch): Outcome<Policy> {
        val childId = childOf(policyId) ?: return directPatch(policyId, patch)
        val op = ParentOp(
            key = newIdempotencyKey(), kind = ParentOp.Kind.POLICY_PATCH, childId = childId,
            policyId = policyId, patch = patch.toJsonObject(), createdAt = nowIso(),
        )
        return when (val r = submit(op, PolicyCall.PATCH, null)) {
            is Outcome.Success -> r.data?.let { Outcome.Success(it.toDomain()) } ?: queuedPolicy(childId, policyId)
            is Outcome.Failure -> r
        }
    }

    override suspend fun deletePolicy(policyId: String): Outcome<Unit> {
        val childId = childOf(policyId)
        if (policyId.startsWith(LOCAL_POLICY_PREFIX)) {
            // Serverga hali bormagan jadval: navbatdan olib tashlaş kifoya
            ParentOutbox.dropLocal(policyId)
            childId?.let { publish(it) }
            return Outcome.Success(Unit)
        }
        if (childId == null) return directDelete(policyId)
        val op = ParentOp(
            key = newIdempotencyKey(), kind = ParentOp.Kind.POLICY_DELETE, childId = childId,
            policyId = policyId, createdAt = nowIso(),
        )
        return when (val r = submit(op, PolicyCall.DELETE, null)) {
            is Outcome.Success -> Outcome.Success(Unit)
            is Outcome.Failure -> r
        }
    }

    // ── Offline-first: navbat ─────────────────────────────────

    /**
     * Shu bola uçun navbat böş bölsa — darhol serverga. Server yetib bölmasa (internet, 5xx, texnik ishlar) yoki
     * navbatda shu bolaning boşqa özgarişi kutayotgan bölsa — ekranda darhol körinadi va navbatga tuşadi.
     * Server RAD etsa — xato qaytadi, hech narsa özgarmaydi.
     * @return server javobi (yuborilgan bölsa) yoki null (navbatga tuşgan bölsa)
     */
    private suspend fun submit(op: ParentOp, call: PolicyCall, draft: PolicyDraft?): Outcome<PolicyOutDto?> {
        if (ParentOutbox.current().none { it.childId == op.childId && it.kind != ParentOp.Kind.SAFETY }) {
            when (val r = send(op, call, draft)) {
                is Outcome.Success -> {
                    onSent(op, r.data)
                    return r
                }
                is Outcome.Failure -> if (!r.cause.isUnreachable) return r
            }
        }
        if (!canQueue(op)) return Outcome.Failure(ErrorCause.NoInternet)
        ParentOutbox.add(op)
        publish(op.childId)
        return Outcome.Success(null)
    }

    /** Offline faqat keşi bor bolaga va faqat özi yaratgan jadvalga (server ham shunday ruxsat beradi). */
    private fun canQueue(op: ParentOp): Boolean {
        if (raw[op.childId] == null) return false
        if (op.kind == ParentOp.Kind.POLICY_CREATE) return true
        val id = op.policyId ?: return false
        return snapshots.value[op.childId]?.items?.firstOrNull { it.id == id }?.canEdit(AppSettings.userId) == true
    }

    private suspend fun send(op: ParentOp, call: PolicyCall, draft: PolicyDraft?): Outcome<PolicyOutDto?> =
        apiCall(TAG) {
            when (op.kind) {
                ParentOp.Kind.POLICY_CREATE -> {
                    val r = api.create(op.create ?: return@apiCall Outcome.Failure(ErrorCause.InvalidResponse), op.key)
                    if (r.success && r.data != null) Outcome.Success(r.data)
                    else if (r.success) Outcome.Failure(ErrorCause.InvalidResponse)
                    else Outcome.Failure(PolicyErrorMapper.from(call, r.code, draft), r.error)
                }
                ParentOp.Kind.POLICY_PATCH -> {
                    val id = op.policyId?.takeUnless { it.startsWith(LOCAL_POLICY_PREFIX) }
                        ?: return@apiCall Outcome.Failure(ErrorCause.NotFound)
                    val r = api.patch(id, op.patch ?: return@apiCall Outcome.Failure(ErrorCause.InvalidResponse), op.key)
                    if (r.success && r.data != null) Outcome.Success(r.data)
                    else if (r.success) Outcome.Failure(ErrorCause.InvalidResponse)
                    else Outcome.Failure(PolicyErrorMapper.from(call, r.code), r.error)
                }
                ParentOp.Kind.POLICY_DELETE -> {
                    val id = op.policyId?.takeUnless { it.startsWith(LOCAL_POLICY_PREFIX) }
                        ?: return@apiCall Outcome.Success(null)
                    val r = api.delete(id, op.key)
                    if (r.success) Outcome.Success(null)
                    else Outcome.Failure(PolicyErrorMapper.from(call, r.code), r.error)
                }
                ParentOp.Kind.SAFETY -> Outcome.Success(null)
            }
        }

    private suspend fun onSent(op: ParentOp, dto: PolicyOutDto?) {
        if (op.kind == ParentOp.Kind.POLICY_CREATE && dto != null) op.policyId?.let { ParentOutbox.replaceId(it, dto.id) }
        rawLock.withLock {
            val cur = raw[op.childId] ?: return@withLock
            val items = when {
                dto != null -> cur.items.filterNot { it.id == dto.id } + dto
                op.kind == ParentOp.Kind.POLICY_DELETE -> cur.items.filterNot { it.id == op.policyId }
                else -> cur.items
            }
            persist(op.childId, cur.copy(items = items))
        }
        publish(op.childId)
    }

    override suspend fun flushOutbox(): Outcome<Int> {
        var sent = 0
        val rejected = mutableSetOf<String>()
        for (queued in ParentOutbox.current().filter { it.kind != ParentOp.Kind.SAFETY }) {
            val op = ParentOutbox.current().firstOrNull { it.key == queued.key } ?: continue
            val call = when (op.kind) {
                ParentOp.Kind.POLICY_CREATE -> PolicyCall.CREATE
                ParentOp.Kind.POLICY_PATCH -> PolicyCall.PATCH
                else -> PolicyCall.DELETE
            }
            when (val r = send(op, call, null)) {
                is Outcome.Success -> {
                    ParentOutbox.remove(op.key)
                    onSent(op, r.data)
                    sent++
                }
                is Outcome.Failure -> when {
                    r.cause == ErrorCause.SessionExpired || r.cause.isUnreachable -> return r
                    else -> {
                        // Server rad etdi (jadval öçirilgan, ruxsat yöq…) — server holati ustun
                        ParentOutbox.remove(op.key)
                        rejected += op.childId
                    }
                }
            }
        }
        rejected.forEach { refreshPolicies(it, force = true) }
        return Outcome.Success(sent)
    }

    private fun queuedPolicy(childId: String, id: String): Outcome<Policy> =
        snapshots.value[childId]?.items?.firstOrNull { it.id == id }?.let { Outcome.Success(it) }
            ?: Outcome.Failure(ErrorCause.NoInternet)

    private fun childOf(policyId: String): String? =
        raw.entries.firstOrNull { (_, c) -> c.items.any { it.id == policyId } }?.key
            ?: ParentOutbox.current().firstOrNull { it.policyId == policyId }?.childId

    /** Keşda yöq jadval (kamdan-kam): eski yöl — faqat server orqali. */
    private suspend fun directPatch(policyId: String, patch: PolicyPatch): Outcome<Policy> =
        apiCall(TAG) {
            val r = api.patch(policyId, patch.toJsonObject())
            val dto = r.data
            when {
                r.success && dto != null -> Outcome.Success(dto.toDomain())
                r.success -> Outcome.Failure(ErrorCause.InvalidResponse)
                else -> Outcome.Failure(PolicyErrorMapper.from(PolicyCall.PATCH, r.code), r.error)
            }
        }

    private suspend fun directDelete(policyId: String): Outcome<Unit> =
        apiCall(TAG) {
            val r = api.delete(policyId)
            if (r.success) Outcome.Success(Unit)
            else Outcome.Failure(PolicyErrorMapper.from(PolicyCall.DELETE, r.code), r.error)
        }

    // ── Kesh yordamchilari ────────────────────────────────────

    private fun ensureLoaded(childId: String) {
        if (raw.containsKey(childId)) return
        val cached = AppSettings.offlineValue(cacheKey(childId))
            ?.let { runCatching { offlineJson.decodeFromString(RawCache.serializer(), it) }.getOrNull() }
            ?: return
        raw[childId] = cached
        publish(childId)
    }

    private suspend fun storeRaw(childId: String, cache: RawCache) {
        rawLock.withLock { persist(childId, cache) }
        publish(childId)
    }

    private fun persist(childId: String, cache: RawCache) {
        raw[childId] = cache
        AppSettings.setOfflineValue(cacheKey(childId), offlineJson.encodeToString(RawCache.serializer(), cache))
    }

    private fun publish(childId: String) {
        val cache = raw[childId] ?: return
        val items = PolicyOverlay.apply(cache.items, ParentOutbox.current(), childId, AppSettings.userId, nowIso())
            .map { it.toDomain() }
            .sortedByDescending { it.updatedAt }
        snapshots.update {
            it + (childId to PolicyListSnapshot(childId, items, cache.asOf.toInstantOrNow(), cache.packsVersion))
        }
    }

    /** `deleted_at` bor satr keshdan chiqadi, qolgani id bo'yicha upsert bo'ladi. */
    private fun applyDelta(current: List<PolicyOutDto>, delta: List<PolicyOutDto>): List<PolicyOutDto> {
        val byId = current.associateBy { it.id }.toMutableMap()
        delta.forEach { dto -> if (dto.deleted_at != null) byId.remove(dto.id) else byId[dto.id] = dto }
        return byId.values.toList()
    }

    private fun cacheKey(childId: String) = "policies_$childId"

    private fun nowIso(): String = Clock.System.now().toString()

    override suspend fun childApps(userId: String): Outcome<List<InstalledApp>> =
        apiCall(TAG) {
            val r = api.childApps(userId)
            val body = r.data
            when {
                r.success && body != null -> Outcome.Success(body.items.map { it.toDomain() })
                r.success -> Outcome.Failure(ErrorCause.InvalidResponse)
                else -> Outcome.Failure(ApiErrorMapper.fromCode(r.code), r.error)
            }
        }

    private companion object { const val TAG = "PolicyRepository" }
}