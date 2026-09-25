package uz.tikoncha_parent.data.repository.policy

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.datetime.LocalDateTime
import uz.tikoncha_parent.data.mapper.policy.toCreateDto
import uz.tikoncha_parent.data.mapper.policy.toDomain
import uz.tikoncha_parent.data.mapper.policy.toDto
import uz.tikoncha_parent.data.mapper.policy.toInstantOrNow
import uz.tikoncha_parent.data.mapper.policy.toJsonObject
import uz.tikoncha_parent.data.remote.app_error.ApiErrorMapper
import uz.tikoncha_parent.data.remote.app_error.PolicyCall
import uz.tikoncha_parent.data.remote.app_error.PolicyErrorMapper
import uz.tikoncha_parent.data.remote.model.AppDto
import uz.tikoncha_parent.data.remote.policy.PolicyApiService
import uz.tikoncha_parent.data.repository.apiCall
import uz.tikoncha_parent.domain.model.app_error.ErrorCause
import uz.tikoncha_parent.domain.model.app_error.Outcome
import uz.tikoncha_parent.domain.model.policy.EvalResult
import uz.tikoncha_parent.domain.model.policy.EvalTargetRef
import uz.tikoncha_parent.domain.model.policy.Policy
import uz.tikoncha_parent.domain.model.policy.PolicyDraft
import uz.tikoncha_parent.domain.model.policy.PolicyListSnapshot
import uz.tikoncha_parent.domain.model.policy.PolicyPatch
import uz.tikoncha_parent.domain.repository.policy.PolicyRepository

class PolicyRepositoryImpl(
    private val api: PolicyApiService,
) : PolicyRepository {

    /**
     * Bola bo'yicha kesh. Bitta StateFlow — hosila oqimlar undan `map` bilan olinadi,
     * shuning uchun umumiy o'zgaruvchan kolleksiya yo'q va qulf kerak emas.
     */
    private val snapshots = MutableStateFlow<Map<String, PolicyListSnapshot>>(emptyMap())

    override fun observePolicies(childId: String): Flow<List<Policy>> =
        snapshots.map { it[childId]?.items.orEmpty() }.distinctUntilChanged()

    override fun cachedPolicies(childId: String): List<Policy> =
        snapshots.value[childId]?.items.orEmpty()

    override suspend fun refreshPolicies(childId: String, force: Boolean): Outcome<PolicyListSnapshot> =
        apiCall(TAG) {
            val previous = if (force) null else snapshots.value[childId]
            val r = api.list(childId, since = previous?.asOf?.toString())
            val body = r.data
            when {
                r.success && body != null -> {
                    val incoming = body.items.map { it.toDomain() }
                    val merged =
                        if (previous == null) incoming.filter { it.deletedAt == null }
                        else applyDelta(previous.items, incoming)

                    val snapshot = PolicyListSnapshot(
                        childId = childId,
                        items = merged,
                        asOf = body.as_of.toInstantOrNow(),
                        packsVersion = body.packs_version,
                    )
                    snapshots.update { it + (childId to snapshot) }
                    Outcome.Success(snapshot)
                }

                r.success -> Outcome.Failure(ErrorCause.InvalidResponse)
                else -> Outcome.Failure(PolicyErrorMapper.from(PolicyCall.LIST, r.code), r.error)
            }
        }

    override suspend fun getPolicy(policyId: String): Outcome<Policy> =
        apiCall(TAG) {
            val r = api.get(policyId)
            val dto = r.data
            when {
                r.success && dto != null -> Outcome.Success(dto.toDomain())
                r.success -> Outcome.Failure(ErrorCause.InvalidResponse)
                else -> Outcome.Failure(PolicyErrorMapper.from(PolicyCall.LIST, r.code), r.error)
            }
        }

    override suspend fun createPolicy(childId: String, draft: PolicyDraft): Outcome<Policy> =
        apiCall(TAG) {
            val r = api.create(draft.toCreateDto(childId))
            val dto = r.data
            when {
                r.success && dto != null -> {
                    val policy = dto.toDomain()
                    upsertLocal(childId, policy)
                    Outcome.Success(policy)
                }

                r.success -> Outcome.Failure(ErrorCause.InvalidResponse)
                else -> Outcome.Failure(
                    PolicyErrorMapper.from(PolicyCall.CREATE, r.code, draft),
                    r.error
                )
            }
        }

    override suspend fun patchPolicy(policyId: String, patch: PolicyPatch): Outcome<Policy> =
        apiCall(TAG) {
            val r = api.patch(policyId, patch.toJsonObject())
            val dto = r.data
            when {
                r.success && dto != null -> {
                    val policy = dto.toDomain()
                    upsertLocal(policy.scopeId, policy)
                    Outcome.Success(policy)
                }

                r.success -> Outcome.Failure(ErrorCause.InvalidResponse)
                else -> Outcome.Failure(PolicyErrorMapper.from(PolicyCall.PATCH, r.code), r.error)
            }
        }

    override suspend fun deletePolicy(policyId: String): Outcome<Unit> =
        apiCall(TAG) {
            val r = api.delete(policyId)
            if (r.success) {
                removeLocal(policyId)
                Outcome.Success(Unit)
            } else {
                Outcome.Failure(PolicyErrorMapper.from(PolicyCall.DELETE, r.code), r.error)
            }
        }

    override suspend fun evaluate(
        childId: String,
        target: EvalTargetRef,
        at: LocalDateTime?,
    ): Outcome<EvalResult> =
        apiCall(TAG) {
            val r = api.evaluate(target.toDto(childId = childId, at = at?.toString()))
            val dto = r.data
            when {
                r.success && dto != null -> Outcome.Success(dto.toDomain())
                r.success -> Outcome.Failure(ErrorCause.InvalidResponse)
                else -> Outcome.Failure(
                    PolicyErrorMapper.from(PolicyCall.EVALUATE, r.code),
                    r.error
                )
            }
        }

    // ── Kesh yordamchilari ────────────────────────────────────

    /** `deleted_at` bor satr keshdan chiqadi, qolgani id bo'yicha upsert bo'ladi. */
    private fun applyDelta(current: List<Policy>, delta: List<Policy>): List<Policy> {
        val byId = current.associateBy { it.id }.toMutableMap()
        delta.forEach { policy ->
            if (policy.deletedAt != null) byId.remove(policy.id) else byId[policy.id] = policy
        }
        return byId.values.sortedByDescending { it.updatedAt }
    }

    /** Kesh hali yo'q bo'lsa hech narsa qilmaymiz — keyingi refresh o'zi olib keladi. */
    private fun upsertLocal(childId: String, policy: Policy) {
        snapshots.update { map ->
            val snapshot = map[childId] ?: return@update map
            val items = (snapshot.items.filterNot { it.id == policy.id } + policy)
                .sortedByDescending { it.updatedAt }
            map + (childId to snapshot.copy(items = items))
        }
    }

    private fun removeLocal(policyId: String) {
        snapshots.update { map ->
            map.mapValues { (_, snapshot) ->
                snapshot.copy(items = snapshot.items.filterNot { it.id == policyId })
            }
        }
    }

    // ── Eski v1 metodlari — 2-qadamda o'chadi ─────────────────

    override suspend fun childApps(userId: String): Outcome<List<AppDto>> =
        apiCall(TAG) {
            val r = api.childApps(userId)
            val body = r.data
            when {
                r.success && body != null -> Outcome.Success(body.items)
                r.success -> Outcome.Failure(ErrorCause.InvalidResponse)
                else -> Outcome.Failure(ApiErrorMapper.fromCode(r.code), r.error)
            }
        }

    private companion object { const val TAG = "PolicyRepository" }
}