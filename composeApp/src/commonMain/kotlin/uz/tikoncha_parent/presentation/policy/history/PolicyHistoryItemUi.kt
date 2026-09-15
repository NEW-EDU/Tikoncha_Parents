package uz.tikoncha_parent.presentation.policy.history

import kotlinx.datetime.LocalDateTime
import uz.tikoncha_parent.domain.model.policy.PolicyEventType

/** Amalni kim bajargani — joriy ota-ona va bola id si bilan solishtiriladi. */
enum class HistoryActor { ME, CHILD, OTHER_PARENT, UNKNOWN }

data class PolicyHistoryItemUi(
    val id: String,
    val event: PolicyEventType,
    val policyName: String?,
    val actor: HistoryActor,
    val createdAt: LocalDateTime,
    val pausedUntil: LocalDateTime?,
    val renamedFrom: String?,
    val renamedTo: String?,
    val quickBlockTarget: String? = null,
    val isResume: Boolean = false,
)