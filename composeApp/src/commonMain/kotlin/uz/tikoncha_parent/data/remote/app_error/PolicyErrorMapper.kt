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
 * Yangi server xato o'ramida `error_code` yuboradi — bor bo'lsa sabab aynan shundan olinadi (CREATE ham, PATCH ham).
 * Eski serverda u yo'q: faqat lokalizatsiyalangan matn va HTTP status — sabab (endpoint + kod + kontekst)
 * bo'yicha taxmin qilinadi.
 */
object PolicyErrorMapper {

    fun from(call: PolicyCall, code: Int?, draft: PolicyDraft? = null, errorCode: String? = null): ErrorCause =
        fromErrorCode(errorCode, code) ?: when (code) {
            401 -> ErrorCause.SessionExpired
            404 -> ErrorCause.NotFound
            422 -> ErrorCause.Validation
            403 -> forbidden(call, draft)
            else -> ErrorCause.Server(code)
        }

    /** `error_code` bo'yicha aniq sabab; kod yo'q yoki bu yerda ma'lum emas — null (eski taxmin ishlaydi). */
    private fun fromErrorCode(errorCode: String?, code: Int?): ErrorCause? = when (errorCode) {
        null, "" -> null
        "policy_allow_mode_paid" -> ErrorCause.PremiumRequired(PaidFeature.ALLOW_MODE)
        "policy_count_limit" -> ErrorCause.PremiumRequired(PaidFeature.POLICY_COUNT)
        "policy_app_count_limit" -> ErrorCause.PremiumRequired(PaidFeature.POLICY_APP_COUNT)
        "quick_block_paid" -> ErrorCause.PremiumRequired(PaidFeature.QUICK_BLOCK)
        "protection_paid" -> ErrorCause.PremiumRequired(PaidFeature.PROTECTION_PACKS)
        "session_ended" -> ErrorCause.SessionExpired
        // Kodli 403, lekin pullik rad emas (policy_edit_forbidden, not_linked_to_child, preset_parent_active…)
        else -> if (code == 403) ErrorCause.Forbidden else null
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