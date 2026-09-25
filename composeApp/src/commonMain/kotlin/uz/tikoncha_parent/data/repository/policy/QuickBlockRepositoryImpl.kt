package uz.tikoncha_parent.data.repository.policy

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import uz.tikoncha_parent.data.mapper.policy.toDomain
import uz.tikoncha_parent.data.mapper.policy.toDto
import uz.tikoncha_parent.data.remote.app_error.PolicyCall
import uz.tikoncha_parent.data.remote.app_error.PolicyErrorMapper
import uz.tikoncha_parent.data.remote.policy.PolicyApiService
import uz.tikoncha_parent.data.repository.apiCall
import uz.tikoncha_parent.domain.model.app_error.ErrorCause
import uz.tikoncha_parent.domain.model.app_error.Outcome
import uz.tikoncha_parent.domain.model.policy.QuickBlockEntry
import uz.tikoncha_parent.domain.model.policy.QuickBlockResult
import uz.tikoncha_parent.domain.model.policy.QuickBlockTarget
import uz.tikoncha_parent.domain.repository.policy.QuickBlockRepository

class QuickBlockRepositoryImpl(
    private val api: PolicyApiService,
) : QuickBlockRepository {

    private val entries = MutableStateFlow<Map<String, List<QuickBlockEntry>>>(emptyMap())

    override fun observeQuickBlocks(childId: String): Flow<List<QuickBlockEntry>> =
        entries.map { it[childId].orEmpty() }.distinctUntilChanged()

    override fun cachedQuickBlocks(childId: String): List<QuickBlockEntry> =
        entries.value[childId].orEmpty()

    override suspend fun refresh(childId: String): Outcome<List<QuickBlockEntry>> =
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

    override suspend fun add(childId: String, target: QuickBlockTarget): Outcome<QuickBlockResult> =
        apiCall(TAG) {
            val r = api.quickBlockAdd(target.toDto(childId))
            val body = r.data
            when {
                r.success && body != null -> {
                    refresh(childId)
                    Outcome.Success(QuickBlockResult.Companion.from(body.result))
                }

                r.success -> Outcome.Failure(ErrorCause.InvalidResponse)
                else -> Outcome.Failure(
                    PolicyErrorMapper.from(PolicyCall.QUICK_BLOCK_ADD, r.code),
                    r.error,
                )
            }
        }

    override suspend fun remove(childId: String, target: QuickBlockTarget): Outcome<QuickBlockResult> =
        apiCall(TAG) {
            val r = api.quickBlockRemove(childId, target)
            val body = r.data
            when {
                r.success && body != null -> {
                    refresh(childId)
                    Outcome.Success(QuickBlockResult.Companion.from(body.result))
                }

                // Tezkor blok jadvali hali yaratilmagan — olib tashlash kerak bo'lgan narsa yo'q.
                r.code == 404 -> Outcome.Success(QuickBlockResult.ABSENT)

                r.success -> Outcome.Failure(ErrorCause.InvalidResponse)
                else -> Outcome.Failure(
                    PolicyErrorMapper.from(PolicyCall.QUICK_BLOCK_REMOVE, r.code),
                    r.error,
                )
            }
        }

    private companion object { const val TAG = "QuickBlockRepository" }
}