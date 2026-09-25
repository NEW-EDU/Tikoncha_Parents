package uz.tikoncha_parent.domain.model.policy

import kotlinx.serialization.Serializable

@Serializable
enum class EvalDecision {
    ALLOW, BLOCK;

    companion object {
        fun from(raw: String?): EvalDecision = entries.firstOrNull { it.name == raw } ?: ALLOW
    }
}

/** POST /v2/policies/evaluate — nima uchun shunday qaror qabul qilindi. */
@Serializable
enum class EvalReason {
    NO_POLICIES, NO_MATCH,
    DENY_GATE, DENY_LIMIT, DENY_WITHIN,
    ALLOW_GATE, ALLOW_LIMIT, ALLOW_WITHIN,
    NOT_IN_ALLOWLIST, UNKNOWN;

    companion object {
        fun from(raw: String?): EvalReason = entries.firstOrNull { it.name == raw } ?: UNKNOWN
    }
}