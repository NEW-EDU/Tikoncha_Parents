package uz.tikoncha_parent.domain.use_case.policy

import uz.tikoncha_parent.domain.model.app_error.ErrorCause
import uz.tikoncha_parent.domain.model.app_error.Outcome
import uz.tikoncha_parent.domain.model.policy.PolicyAuditPage
import uz.tikoncha_parent.domain.repository.policy.PolicyAuditRepository

class GetPolicyEventsUseCase(
    private val repository: PolicyAuditRepository,
) {
    suspend operator fun invoke(
        childId: String,
        limit: Int = 50,
        offset: Int = 0,
    ): Outcome<PolicyAuditPage> {
        if (childId.isBlank()) return Outcome.Failure(ErrorCause.ChildNotSelected)
        return repository.events(childId, limit, offset)
    }
}