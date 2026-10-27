package uz.tikoncha_parent.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.request.parameter
import io.ktor.client.request.setBody
import io.ktor.http.HttpMethod
import uz.tikoncha_parent.data.remote.model.PaymentStatusResponse
import uz.tikoncha_parent.data.remote.model.SubscriptionLimitResponse
import uz.tikoncha_parent.data.remote.model.SubscriptionPaymentRequest
import uz.tikoncha_parent.data.remote.model.SubscriptionPaymentResponse
import uz.tikoncha_parent.data.remote.model.SubscriptionPlansResponse
import uz.tikoncha_parent.data.remote.model.SubscriptionStatusResponse
import uz.tikoncha_parent.data.remote.model.subscription.PromoCodeValidationRequest
import uz.tikoncha_parent.data.remote.model.subscription.PromoCodeValidationResponse
import uz.tikoncha_parent.data.remote.model.transaction.TransactionHistoryResponse
import uz.tikoncha_parent.domain.model.subscription.CoinPackageListResponse
import uz.tikoncha_parent.domain.model.subscription.PurchaseCoinRequest
import uz.tikoncha_parent.domain.model.subscription.PurchaseCoinResponse

class PaymentApiService(private val client: HttpClient) {

    /** O'zbekiston IP → mahalliy to'lovlar (Paylov avto-to'lov, Click); aks holda iPhone'da App Store. */
    suspend fun paymentRegion(): uz.tikoncha_parent.data.remote.model.payment.PaymentRegionResponse =
        client.safeRequest(
            method = HttpMethod.Get,
            url = "payments/region",
            block = {}
        )

    /** App Store xaridi (StoreKit 2 JWS) — server tekshiradi va PLUS'ni farzandga yoqadi. */
    suspend fun reportAppleTransaction(
        body: uz.tikoncha_parent.data.remote.model.payment.AppleTransactionRequest,
    ): uz.tikoncha_parent.data.remote.model.ApiEnvelope<uz.tikoncha_parent.data.remote.model.payment.AppleSubscriptionData> =
        client.safeRequest(
            method = HttpMethod.Post,
            url = "subscriptions/apple/transactions",
            block = { setBody(body) }
        )

    suspend fun subscriptionLimits(): SubscriptionLimitResponse =
        client.safeRequest(
            method = HttpMethod.Get,
            url = "subscriptions/limits/children",
            block = {}
        )

    suspend fun subscriptionPlans(): SubscriptionPlansResponse =
        client.safeRequest(
            method = HttpMethod.Get,
            url = "/subscription-plans",
            block = {}
        )

    suspend fun paymentStatus(merchantTransId: String): PaymentStatusResponse =
        client.safeRequest(
            method = HttpMethod.Companion.Get,
            url = "payments/status/$merchantTransId",
            block = {}
        )



    suspend fun subscriptionPayment(subscriptionPaymentRequest: SubscriptionPaymentRequest): SubscriptionPaymentResponse =
        client.safeRequest(
            method = HttpMethod.Companion.Post,
            url = "/subscriptions/purchase-intent",
            block = {
                setBody(subscriptionPaymentRequest)
            }
        )

    suspend fun promoCodeValidation(promoCodeValidationRequest: PromoCodeValidationRequest): PromoCodeValidationResponse =
        client.safeRequest(
            method = HttpMethod.Companion.Post,
            url = "/payments/promocode/validate",
            block = {
                setBody(promoCodeValidationRequest)
            }
        )

    suspend fun coinPackageList(): CoinPackageListResponse =
        client.safeRequest(
            method = HttpMethod.Companion.Get,
            url = "/payments/packages",
            block = {}
        )

    suspend fun purchaseCoin(purchaseCoinRequest: PurchaseCoinRequest): PurchaseCoinResponse =
        client.safeRequest(
            method = HttpMethod.Companion.Post,
            url = "/payments/create-intent",
            block = {
                setBody(purchaseCoinRequest)
            }
        )


    suspend fun paymentTransactions(
        limit: Int,
        offset: Int,
    ): TransactionHistoryResponse =
        client.safeRequest(
            method = HttpMethod.Get,
            url = "/payments/transactions",
            block = {
                parameter("limit", limit)
                parameter("offset", offset)
            }
        )

    suspend fun getSubscriptionStatus(userId: String? = null): SubscriptionStatusResponse =
        client.safeRequest(
            method = HttpMethod.Get,
            url = "/subscriptions/status",
            block = {
                userId?.let { parameter("user_id", it) }
            }
        )
}