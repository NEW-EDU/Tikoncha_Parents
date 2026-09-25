    package uz.tikoncha_parent.domain.repository

    import uz.tikoncha_parent.data.remote.model.SubscriptionPaymentRequest
    import uz.tikoncha_parent.data.remote.model.SubscriptionPlansData
    import uz.tikoncha_parent.data.remote.model.SubscriptionPurchaseData
    import uz.tikoncha_parent.data.remote.model.subscription.PromoCodeValidationData
    import uz.tikoncha_parent.data.remote.model.subscription.PromoCodeValidationRequest
    import uz.tikoncha_parent.data.remote.model.subscription.PromoCodeValidationResponse
    import uz.tikoncha_parent.domain.model.PaymentStatus
    import uz.tikoncha_parent.domain.model.SubscriptionLimit
    import uz.tikoncha_parent.domain.model.app_error.Outcome
    import uz.tikoncha_parent.domain.model.subscription.CoinPackageListWrapper
    import uz.tikoncha_parent.domain.model.subscription.PurchaseCoinData
    import uz.tikoncha_parent.domain.model.subscription.PurchaseCoinRequest
    import uz.tikoncha_parent.domain.model.subscription.PurchaseCoinResponse
    import uz.tikoncha_parent.domain.model.subscription.SubscriptionStatus
    import uz.tikoncha_parent.domain.model.transaction.TransactionPage

    interface PaymentRepository {
        suspend fun subscriptionPayment(subscriptionPaymentRequest: SubscriptionPaymentRequest): Outcome<SubscriptionPurchaseData>

        suspend fun syncSubscriptionLimits(): Outcome<List<SubscriptionLimit>>

        /** Oxirgi [syncSubscriptionLimits] natijasi; bola hali yo'q bo'lsa `null`. */
        fun cachedSubscriptionLimit(childId: String): SubscriptionLimit?

        suspend fun subscriptionPlans(): Outcome<List<SubscriptionPlansData>>

        suspend fun paymentStatus(merchantTransId: String): Outcome<PaymentStatus>

        suspend fun promoCodeValidation(promoCodeValidationRequest: PromoCodeValidationRequest): Outcome<PromoCodeValidationData>

        suspend fun coinPackages(): Outcome<CoinPackageListWrapper>

        suspend fun purchaseCoin(purchaseCoinRequest: PurchaseCoinRequest): Outcome<PurchaseCoinData>

        suspend fun paymentTransactions(limit: Int, offset: Int): Outcome<TransactionPage>

        suspend fun getSubscriptionStatus(userId: String? = null): Outcome<SubscriptionStatus>
    }