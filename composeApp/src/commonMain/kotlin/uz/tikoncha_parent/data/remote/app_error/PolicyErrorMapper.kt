package uz.tikoncha_parent.data.remote.app_error

import uz.tikoncha_parent.domain.model.app_error.ErrorCause
import uz.tikoncha_parent.domain.model.app_error.PaidFeature
import uz.tikoncha_parent.domain.model.policy.PolicyAction
import uz.tikoncha_parent.domain.model.policy.PolicyDraft
import uz.tikoncha_parent.domain.model.policy.PolicyPreset

/** Qaysi chaqiruv xato berdi — 403 ni to'g'ri talqin qilish uchun kerak. */
enum class PolicyCall {
    LIST, CREATE, PATCH, DELETE,
    QUICK_BLOCK_ADD, QUICK_BLOCK_REMOVE,
    PACKS, EVENTS, EVALUATE,
}

/**
 * Server javobida mashina o'qiydigan xato kodi yo'q — faqat lokalizatsiyalangan matn
 * va HTTP status. Shuning uchun sabab (endpoint + kod + kontekst) bo'yicha aniqlanadi.
 */
object PolicyErrorMapper {

    fun from(call: PolicyCall, code: Int?, draft: PolicyDraft? = null): ErrorCause = when (code) {
        401 -> ErrorCause.SessionExpired
        404 -> ErrorCause.NotFound
        422 -> ErrorCause.Validation
        403 -> forbidden(call, draft)
        else -> ErrorCause.Server(code)
    }

    private fun forbidden(call: PolicyCall, draft: PolicyDraft?): ErrorCause = when (call) {
        PolicyCall.QUICK_BLOCK_ADD -> ErrorCause.PremiumRequired(PaidFeature.QUICK_BLOCK)

        PolicyCall.CREATE -> when {
            draft?.preset == PolicyPreset.PROTECTION ->
                ErrorCause.PremiumRequired(PaidFeature.PROTECTION_PACKS)

            draft?.action == PolicyAction.ALLOW ->
                ErrorCause.PremiumRequired(PaidFeature.ALLOW_MODE)

            else -> ErrorCause.PremiumRequired(PaidFeature.POLICY_COUNT)
        }

        // PATCH/DELETE 403 — boshqa ota-onaning yoki bolaning jadvali. Pullik emas.
        else -> ErrorCause.Forbidden
    }
}