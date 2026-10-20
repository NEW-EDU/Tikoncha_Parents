package uz.tikoncha_parent.domain.repository

import kotlinx.coroutines.flow.StateFlow

/**
 * Mahalliy to'lov usullari (Paylov karta avto-to'lovi, Click) shu qurilmaga ko'rsatiladimi —
 * server IP bo'yicha hal qiladi (`GET payments/region`: O'zbekiston IP → true).
 */
interface PaymentRegionRepository {
    /** null — hali noma'lum. */
    val localPayments: StateFlow<Boolean?>
    suspend fun refresh(): Boolean?
}
