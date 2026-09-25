package uz.tikoncha_parent.domain.use_case.policy

import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours
import uz.tikoncha_parent.domain.model.app_error.ErrorCause
import uz.tikoncha_parent.domain.model.app_error.Outcome
import uz.tikoncha_parent.domain.model.policy.Policy
import uz.tikoncha_parent.domain.model.policy.PolicyAction
import uz.tikoncha_parent.domain.model.policy.PolicyDraft
import uz.tikoncha_parent.domain.model.policy.PolicyTargets
import uz.tikoncha_parent.domain.repository.policy.PolicyRepository

class CreateTimedBlockUseCase(
    private val repository: PolicyRepository,
) {
    /** "2 soatga blokla" — muddati tugagach server jadvalni o'zi e'tibordan chiqaradi. */
    suspend operator fun invoke(
        childId: String,
        targets: PolicyTargets,
        hours: Int,
        name: String,
    ): Outcome<Policy> {
        if (childId.isBlank()) return Outcome.Failure(ErrorCause.ChildNotSelected)
        if (targets.isEmpty) return Outcome.Failure(ErrorCause.Validation)
        if (hours < 1) return Outcome.Failure(ErrorCause.Validation)

        return repository.createPolicy(
            childId = childId,
            draft = PolicyDraft(
                name = name,
                action = PolicyAction.DENY,
                targets = targets,
                expiresAt = Clock.System.now() + hours.hours,
            ),
        )
    }
}