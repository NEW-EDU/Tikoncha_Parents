package uz.tikoncha_parent.domain.use_case.policy

import uz.tikoncha_parent.domain.model.app_error.ErrorCause
import uz.tikoncha_parent.domain.model.app_error.Outcome
import uz.tikoncha_parent.domain.model.policy.Policy
import uz.tikoncha_parent.domain.model.policy.PolicyDraft
import uz.tikoncha_parent.domain.repository.policy.PolicyRepository

class CreatePolicyUseCase(
    private val repository: PolicyRepository,
) {
    /** Serverga bormasdan oldin 422 ga olib keladigan holatlarni ushlaydi. */
    suspend operator fun invoke(childId: String, draft: PolicyDraft): Outcome<Policy> {
        if (childId.isBlank()) return Outcome.Failure(ErrorCause.ChildNotSelected)
        if (draft.name.isBlank()) return Outcome.Failure(ErrorCause.EmptyTitle)
        if (draft.targets.isEmpty) return Outcome.Failure(ErrorCause.Validation)

        val hasBrokenTime = draft.conditions.time.any { it.days.isEmpty() || it.startMin == it.endMin }
        if (hasBrokenTime) return Outcome.Failure(ErrorCause.Validation)

        val hasBrokenLimit = draft.limits.usage.any { it.days.isEmpty() || it.minutes < 1 }
        if (hasBrokenLimit) return Outcome.Failure(ErrorCause.Validation)

        return repository.createPolicy(childId, draft)
    }
}