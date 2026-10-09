package uz.tikoncha_parent.data.repository.autopay

import uz.tikoncha_parent.data.remote.AutopayApiService
import uz.tikoncha_parent.data.remote.app_error.ApiErrorMapper
import uz.tikoncha_parent.data.remote.model.autopay.AutopayCardRequest
import uz.tikoncha_parent.data.remote.model.autopay.AutopayDto
import uz.tikoncha_parent.data.remote.model.autopay.AutopayEnableRequest
import uz.tikoncha_parent.data.remote.model.autopay.CardConfirmRequest
import uz.tikoncha_parent.data.remote.model.autopay.CardDto
import uz.tikoncha_parent.data.remote.model.autopay.CardStartRequest
import uz.tikoncha_parent.data.remote.model.autopay.ChargeResultDto
import uz.tikoncha_parent.data.repository.apiCall
import uz.tikoncha_parent.domain.model.app_error.Outcome
import uz.tikoncha_parent.domain.model.autopay.AutopayInfo
import uz.tikoncha_parent.domain.model.autopay.CardStart
import uz.tikoncha_parent.domain.model.autopay.CardVendor
import uz.tikoncha_parent.domain.model.autopay.ChargeResult
import uz.tikoncha_parent.domain.model.autopay.ChargeState
import uz.tikoncha_parent.domain.model.autopay.ChargeStatus
import uz.tikoncha_parent.domain.model.autopay.MandateStatus
import uz.tikoncha_parent.domain.model.autopay.PayCard
import uz.tikoncha_parent.domain.model.autopay.PlanPeriod
import uz.tikoncha_parent.domain.repository.AutopayRepository
import kotlin.time.Instant

class AutopayRepositoryImpl(
    private val api: AutopayApiService,
) : AutopayRepository {

    override suspend fun state(childId: String): Outcome<AutopayInfo> = apiCall(TAG) {
        val r = api.state(childId)
        val d = r.data
        if (r.success && d != null) Outcome.Success(d.toDomain()) else fail(r.code, r.error)
    }

    override suspend fun cards(): Outcome<List<PayCard>> = apiCall(TAG) {
        val r = api.cards()
        if (r.success) Outcome.Success(r.data.orEmpty().map { it.toDomain() }) else fail(r.code, r.error)
    }

    override suspend fun startCard(cardNumber: String, expire: String): Outcome<CardStart> = apiCall(TAG) {
        val r = api.startCard(CardStartRequest(cardNumber = cardNumber.filter { it.isDigit() }, expire = expire.filter { it.isDigit() }))
        val d = r.data
        if (r.success && d != null) Outcome.Success(CardStart(d.id, d.maskedPan, CardVendor.of(d.vendor), d.phoneMask))
        else fail(r.code, r.error)
    }

    override suspend fun confirmCard(cardId: String, otp: String): Outcome<PayCard> = apiCall(TAG) {
        val r = api.confirmCard(cardId, CardConfirmRequest(otp.trim()))
        val d = r.data
        if (r.success && d != null) Outcome.Success(d.toDomain()) else fail(r.code, r.error)
    }

    override suspend fun removeCard(cardId: String): Outcome<Unit> = apiCall(TAG) {
        val r = api.removeCard(cardId)
        if (r.success) Outcome.Success(Unit) else fail(r.code, r.error)
    }

    override suspend fun makePrimary(cardId: String): Outcome<List<PayCard>> = apiCall(TAG) {
        val r = api.makePrimary(cardId)
        if (r.success) Outcome.Success(r.data.orEmpty().map { it.toDomain() }) else fail(r.code, r.error)
    }

    override suspend fun enable(
        childId: String, planId: String, period: PlanPeriod, cardId: String, consent: Boolean, promocode: String?,
    ): Outcome<ChargeResult> = apiCall(TAG) {
        val r = api.enable(
            AutopayEnableRequest(
                planId = planId, planDuration = period.wire, cardId = cardId, consent = consent,
                promocode = promocode?.takeIf { it.isNotBlank() }, childUserId = childId,
            )
        )
        val d = r.data
        if (r.success && d != null) Outcome.Success(d.toDomain()) else fail(r.code, r.error)
    }

    override suspend fun changeCard(childId: String, cardId: String): Outcome<AutopayInfo> = apiCall(TAG) {
        val r = api.changeCard(AutopayCardRequest(cardId = cardId, childUserId = childId))
        val d = r.data
        if (r.success && d != null) Outcome.Success(d.toDomain()) else fail(r.code, r.error)
    }

    override suspend fun retry(childId: String): Outcome<ChargeResult> = apiCall(TAG) {
        val r = api.retry(childId)
        val d = r.data
        if (r.success && d != null) Outcome.Success(d.toDomain()) else fail(r.code, r.error)
    }

    override suspend fun disable(childId: String): Outcome<AutopayInfo> = apiCall(TAG) {
        val r = api.disable(childId)
        val d = r.data
        if (r.success && d != null) Outcome.Success(d.toDomain()) else fail(r.code, r.error)
    }

    override suspend fun chargeState(transactionId: String): Outcome<ChargeState> = apiCall(TAG) {
        val r = api.chargeStatus(transactionId)
        val d = r.data
        if (r.success && d != null) Outcome.Success(ChargeState.of(d.status)) else fail(r.code, r.error)
    }

    private fun fail(code: Int?, error: String?): Outcome.Failure =
        Outcome.Failure(ApiErrorMapper.fromCode(code), error)

    private companion object {
        const val TAG = "AutopayRepository"
    }
}

private fun CardDto.toDomain() = PayCard(
    id = id, maskedPan = maskedPan, vendor = CardVendor.of(vendor), expire = expire, inUse = inUse, isPrimary = isPrimary,
)

private fun AutopayDto.toDomain() = AutopayInfo(
    available = available,
    enabled = enabled,
    mine = mine,
    payerIsChild = payerIsChild,
    status = MandateStatus.of(status),
    planId = planId,
    period = PlanPeriod.of(planDuration),
    amount = amount,
    card = card?.toDomain(),
    nextChargeAt = nextChargeAt.toInstantOrNull(),
    lastErrorText = lastErrorText,
    failCount = failCount,
    paidUntil = paidUntil.toInstantOrNull(),
)

private fun ChargeResultDto.toDomain() = ChargeResult(
    status = ChargeStatus.of(status),
    errorText = errorText,
    transactionId = transactionId,
    autopay = autopay.toDomain(),
)

/** Server ISO vaqti; zonasiz kelsa UTC deb olinadi. */
internal fun String?.toInstantOrNull(): Instant? {
    if (this.isNullOrBlank()) return null
    return runCatching { Instant.parse(this) }.getOrNull()
        ?: runCatching { Instant.parse(this + "Z") }.getOrNull()
}
