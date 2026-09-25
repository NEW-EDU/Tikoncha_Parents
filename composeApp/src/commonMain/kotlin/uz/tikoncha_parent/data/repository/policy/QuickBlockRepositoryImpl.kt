package uz.tikoncha_parent.data.repository.policy

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import uz.tikoncha_parent.data.mapper.policy.toDomain
import uz.tikoncha_parent.data.mapper.policy.toDto
import uz.tikoncha_parent.data.mapper.policy.toJsonObject
import uz.tikoncha_parent.data.mapper.policy.toQuickBlockEntry
import uz.tikoncha_parent.data.remote.app_error.PolicyCall
import uz.tikoncha_parent.data.remote.app_error.PolicyErrorMapper
import uz.tikoncha_parent.data.remote.model.ApiEnvelope
import uz.tikoncha_parent.data.remote.model.policy.QuickBlockOutDto
import uz.tikoncha_parent.data.remote.policy.PolicyApiService
import uz.tikoncha_parent.data.repository.apiCall
import uz.tikoncha_parent.domain.model.app_error.ErrorCause
import uz.tikoncha_parent.domain.model.app_error.Outcome
import uz.tikoncha_parent.domain.model.policy.PolicyPatch
import uz.tikoncha_parent.domain.model.policy.QuickBlockEntry
import uz.tikoncha_parent.domain.model.policy.QuickBlockResult
import uz.tikoncha_parent.domain.model.policy.QuickBlockTarget
import uz.tikoncha_parent.domain.repository.policy.QuickBlockRepository

class QuickBlockRepositoryImpl(
    private val api: PolicyApiService,
) : QuickBlockRepository {

    private val entries = MutableStateFlow<Map<String, List<QuickBlockEntry>>>(emptyMap())

    /**
     * Barcha tezkor blok so'rovlari (ro'yxat ham) navbat bilan, bosilgan tartibda.
     * Aks holda bir necha ilovani tez bossa javoblar aralash kelib, eski ro'yxat
     * yangisini bosib qo'yardi. Server qatorni qulflaydi — yo'qolish bo'lmaydi,
     * lekin keshga javoblar tartibi mijozda ta'minlanadi.
     */
    private val lock = Mutex()

    override fun observeQuickBlocks(childId: String): Flow<List<QuickBlockEntry>> =
        entries.map { it[childId].orEmpty() }.distinctUntilChanged()

    override suspend fun refresh(childId: String): Outcome<List<QuickBlockEntry>> =
        lock.withLock {
            apiCall(TAG) {
                val r = api.quickBlocks(childId)
                val body = r.data
                when {
                    r.success && body != null -> {
                        val items = body.items.map { it.toDomain() }
                        entries.update { it + (childId to items) }
                        Outcome.Success(items)
                    }

                    r.success -> Outcome.Failure(ErrorCause.InvalidResponse)
                    else -> Outcome.Failure(PolicyErrorMapper.from(PolicyCall.LIST, r.code), r.error)
                }
            }
        }

    override suspend fun add(childId: String, target: QuickBlockTarget): Outcome<QuickBlockResult> =
        lock.withLock {
            apiCall(TAG) { apply(childId, PolicyCall.QUICK_BLOCK_ADD, api.quickBlockAdd(target.toDto(childId))) }
        }

    override suspend fun remove(childId: String, target: QuickBlockTarget): Outcome<QuickBlockResult> =
        lock.withLock {
            apiCall(TAG) {
                val r = api.quickBlockRemove(childId, target)
                // Mening tezkor blokim hali yaratilmagan — olib tashlanadigan narsa yo'q.
                if (!r.success && r.code == 404) Outcome.Success(QuickBlockResult.ABSENT)
                else apply(childId, PolicyCall.QUICK_BLOCK_REMOVE, r)
            }
        }

    override suspend fun update(childId: String, policyId: String, patch: PolicyPatch): Outcome<QuickBlockEntry> =
        lock.withLock {
            apiCall(TAG) {
                val r = api.patch(policyId, patch.toJsonObject())
                val dto = r.data
                when {
                    r.success && dto != null -> {
                        val entry = dto.toDomain().toQuickBlockEntry()
                        upsert(childId, entry)
                        Outcome.Success(entry)
                    }

                    r.success -> Outcome.Failure(ErrorCause.InvalidResponse)
                    else -> Outcome.Failure(PolicyErrorMapper.from(PolicyCall.PATCH, r.code), r.error)
                }
            }
        }

    /** Javobdagi butun jadval keshdagi o'z yozuvi o'rniga qo'yiladi — qayta so'rov yo'q. */
    private fun apply(childId: String, call: PolicyCall, r: ApiEnvelope<QuickBlockOutDto>): Outcome<QuickBlockResult> {
        val body = r.data
        return when {
            r.success && body != null -> {
                body.policy?.toDomain()?.toQuickBlockEntry()?.let { entry -> upsert(childId, entry) }
                Outcome.Success(QuickBlockResult.from(body.result))
            }

            r.success -> Outcome.Failure(ErrorCause.InvalidResponse)
            else -> Outcome.Failure(PolicyErrorMapper.from(call, r.code), r.error)
        }
    }

    private fun upsert(childId: String, entry: QuickBlockEntry) {
        entries.update { map ->
            val list = map[childId] ?: return@update map        // ro'yxat hali olinmagan — refresh olib keladi
            val next = if (list.any { it.policyId == entry.policyId }) {
                list.map { if (it.policyId == entry.policyId) entry else it }
            } else {
                list + entry
            }
            map + (childId to next)
        }
    }

    private companion object { const val TAG = "QuickBlockRepository" }
}
