package uz.tikoncha_parent.data.repository

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import uz.tikoncha_parent.data.remote.PaymentApiService
import uz.tikoncha_parent.domain.repository.PaymentRegionRepository

class PaymentRegionRepositoryImpl(
    private val api: PaymentApiService,
) : PaymentRegionRepository {
    private val _local = MutableStateFlow<Boolean?>(null)
    override val localPayments: StateFlow<Boolean?> = _local.asStateFlow()

    override suspend fun refresh(): Boolean? {
        val r = runCatching { api.paymentRegion() }.getOrNull()
        val local = r?.data?.localPayments?.takeIf { r.success }
        if (local != null) _local.value = local
        return _local.value
    }
}
