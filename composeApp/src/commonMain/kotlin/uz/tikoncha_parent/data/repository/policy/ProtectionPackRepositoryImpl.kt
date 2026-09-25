package uz.tikoncha_parent.data.repository.policy

import kotlinx.coroutines.flow.MutableStateFlow
import uz.tikoncha_parent.data.mapper.policy.toDomain
import uz.tikoncha_parent.data.remote.app_error.PolicyCall
import uz.tikoncha_parent.data.remote.app_error.PolicyErrorMapper
import uz.tikoncha_parent.data.remote.policy.PolicyApiService
import uz.tikoncha_parent.data.repository.apiCall
import uz.tikoncha_parent.domain.model.app_error.ErrorCause
import uz.tikoncha_parent.domain.model.app_error.Outcome
import uz.tikoncha_parent.domain.model.policy.PackCatalog
import uz.tikoncha_parent.domain.repository.policy.ProtectionPackRepository

class ProtectionPackRepositoryImpl(
    private val api: PolicyApiService,
) : ProtectionPackRepository {

    /** Katalog 3 ta paket — diskka saqlash kerak emas, seans davomida yetarli. */
    private val cached = MutableStateFlow<PackCatalog?>(null)

    override fun cachedCatalog(): PackCatalog? = cached.value

    override suspend fun catalog(force: Boolean): Outcome<PackCatalog> {
        if (!force) cached.value?.let { return Outcome.Success(it) }

        return apiCall(TAG) {
            // `since` yuborilmaydi — server u holda faqat o'zgarganlarni beradi, bizga to'liq ro'yxat kerak.
            val r = api.packs(since = null)
            val body = r.data
            when {
                r.success && body != null -> {
                    val catalog = body.toDomain()
                    cached.value = catalog
                    Outcome.Success(catalog)
                }

                r.success -> Outcome.Failure(ErrorCause.InvalidResponse)
                else -> Outcome.Failure(PolicyErrorMapper.from(PolicyCall.PACKS, r.code), r.error)
            }
        }
    }

    private companion object { const val TAG = "ProtectionPackRepository" }
}