package uz.tikoncha_parent.data.remote.model

import kotlinx.serialization.Serializable

@Serializable
data class SubscriptionPlansResponse(
    val success: Boolean,
    val data: List<SubscriptionPlansData>? = null,
    val error: String? = null,
    val code: Int? = null
)

@Serializable
data class SubscriptionPlansData(
    val id: String,
    val name: String,
    val monthly: SubscriptionPlansDto,
    val annual: SubscriptionPlansDto,
    /** To'lov usuli bo'yicha narxlar. `monthly/annual.price` — Click narxi (eski versiyalar uchun). */
    val prices: List<PlanPriceDto> = emptyList(),
)

@Serializable
data class PlanPriceDto(
    /** CARD (karta orqali avto-to'lov) / CLICK / GOOGLE / APPLE */
    val method: String,
    /** MONTHLY / ANNUAL */
    val duration: String,
    val price: Int? = null,
    @kotlinx.serialization.SerialName("store_product_id") val storeProductId: String? = null,
)

@Serializable
data class SubscriptionPlansDto(
    val price: Int,
    val coin: Int,
    val feature: List<String>,
    val bonus: List<String>,
)
