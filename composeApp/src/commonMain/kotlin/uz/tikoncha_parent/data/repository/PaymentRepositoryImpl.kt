package uz.tikoncha_parent.data.repository

import uz.tikoncha_parent.data.local.AppSettings
import uz.tikoncha_parent.data.mapper.toDomain
import uz.tikoncha_parent.data.mapper.toSubscriptionLimit
import uz.tikoncha_parent.data.remote.PaymentApiService
import uz.tikoncha_parent.data.remote.app_error.ApiErrorMapper
import uz.tikoncha_parent.data.remote.model.SubscriptionPaymentRequest
import uz.tikoncha_parent.data.remote.model.SubscriptionPlansData
import uz.tikoncha_parent.data.remote.model.SubscriptionPurchaseData
import uz.tikoncha_parent.data.remote.model.subscription.PromoCodeValidationData
import uz.tikoncha_parent.data.remote.model.subscription.PromoCodeValidationRequest
import uz.tikoncha_parent.domain.model.PaymentStatus
import uz.tikoncha_parent.domain.model.SubscriptionLimit
import uz.tikoncha_parent.domain.model.app_error.ErrorCause
import uz.tikoncha_parent.domain.model.app_error.Outcome
import uz.tikoncha_parent.domain.model.subscription.CoinPackageListWrapper
import uz.tikoncha_parent.domain.model.subscription.PurchaseCoinData
import uz.tikoncha_parent.domain.model.subscription.PurchaseCoinRequest
import uz.tikoncha_parent.domain.model.subscription.SubscriptionStatus
import uz.tikoncha_parent.domain.model.transaction.TransactionPage
import uz.tikoncha_parent.domain.repository.PaymentRepository

class PaymentRepositoryImpl(
    private val api: PaymentApiService
) : PaymentRepository {

    override suspend fun subscriptionPayment(
        subscriptionPaymentRequest: SubscriptionPaymentRequest
    ): Outcome<SubscriptionPurchaseData> = apiCall(TAG) {
        val r = api.subscriptionPayment(subscriptionPaymentRequest)
        val data = r.data
        if (r.success && data != null) Outcome.Success(data)
        else Outcome.Failure(ApiErrorMapper.fromCode(r.code), r.error)
    }

    override fun cachedSubscriptionLimit(childId: String): SubscriptionLimit? =
        AppSettings.subscriptionLimitList.firstOrNull { it.childId == childId }

    override suspend fun syncSubscriptionLimits(): Outcome<List<SubscriptionLimit>> =
        apiCall(TAG) {
            val r = api.subscriptionLimits()
            val body = r.data
            when {
                r.success && body != null -> {
                    val limits = body.children.map { it.toSubscriptionLimit() }
                    AppSettings.subscriptionLimitList = limits      // kesh — endi shu yerda
                    Outcome.Success(limits)
                }

                r.success -> Outcome.Failure(ErrorCause.InvalidResponse)
                else -> Outcome.Failure(ApiErrorMapper.fromCode(r.code), r.error)
            }
        }

    override suspend fun subscriptionPlans(): Outcome<List<SubscriptionPlansData>> = apiCall(TAG) {
        val r = api.subscriptionPlans()
        val data = r.data
        if (r.success && data != null) Outcome.Success(data)
        else Outcome.Failure(ApiErrorMapper.fromCode(r.code), r.error)
    }

    override suspend fun paymentStatus(merchantTransId: String): Outcome<PaymentStatus> =
        apiCall(TAG) {
            val r = api.paymentStatus(merchantTransId)
            val data = r.data
            if (r.success && data != null) Outcome.Success(PaymentStatus.fromString(data.status))
            else Outcome.Failure(ApiErrorMapper.fromCode(r.code), r.error)
        }

    override suspend fun promoCodeValidation(
        promoCodeValidationRequest: PromoCodeValidationRequest
    ): Outcome<PromoCodeValidationData> = apiCall(TAG) {
        val r = api.promoCodeValidation(promoCodeValidationRequest)
        val data = r.data
        if (r.success && data != null) Outcome.Success(data)
        else Outcome.Failure(ApiErrorMapper.fromCode(r.code), r.error)
    }

    override suspend fun coinPackages(): Outcome<CoinPackageListWrapper> = apiCall(TAG) {
        val r = api.coinPackageList()
        val data = r.data
        if (r.success && data != null) Outcome.Success(data)
        else Outcome.Failure(ApiErrorMapper.fromCode(r.code), r.error)
    }

    override suspend fun purchaseCoin(
        purchaseCoinRequest: PurchaseCoinRequest
    ): Outcome<PurchaseCoinData> = apiCall(TAG) {
        val r = api.purchaseCoin(purchaseCoinRequest)
        val data = r.data
        if (r.success && data != null) Outcome.Success(data)
        else Outcome.Failure(ApiErrorMapper.fromCode(r.code), r.error)
    }

    override suspend fun paymentTransactions(limit: Int, offset: Int): Outcome<TransactionPage> =
        apiCall(TAG) {
            val r = api.paymentTransactions(limit = limit, offset = offset)
            val data = r.data
            if (r.success && data != null) Outcome.Success(data.toDomain())
            else Outcome.Failure(ApiErrorMapper.fromCode(r.code), r.error)
        }


    override suspend fun getSubscriptionStatus(userId: String?): Outcome<SubscriptionStatus> =
        apiCall(TAG) {
            val r = api.getSubscriptionStatus(userId)
            val data = r.data
            if (r.success && data != null) Outcome.Success(data.toDomain())
            else Outcome.Failure(ApiErrorMapper.fromCode(r.code), r.error)
        }

    private companion object {
        const val TAG = "PaymentRepository"
    }
}