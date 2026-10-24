package uz.tikoncha_parent.data.repository

import uz.tikoncha_parent.platform.Logger
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
        val r = runCatching { api.paymentRegion() }
            .onFailure { Logger.d("PaymentRegion", "request failed: ${it.message}") }
            .getOrNull()
        val local = r?.data?.localPayments?.takeIf { r.success }
        Logger.d("PaymentRegion", "success=${r?.success} country=${r?.data?.country} local=$local")
        if (local != null) _local.value = local
        return _local.value
    }
}
