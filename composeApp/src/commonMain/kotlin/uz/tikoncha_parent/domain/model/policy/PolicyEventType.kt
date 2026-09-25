package uz.tikoncha_parent.domain.model.policy

enum class PolicyEventType {
    CREATED, UPDATED, ENABLED, DISABLED, PAUSED, DELETED,
    QUICK_BLOCK_ADD, QUICK_BLOCK_REMOVE, UNKNOWN;

    companion object {
        fun from(raw: String?): PolicyEventType = entries.firstOrNull { it.name == raw } ?: UNKNOWN
    }
}