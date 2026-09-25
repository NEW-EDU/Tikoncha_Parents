package uz.tikoncha_parent.domain.use_case.policy

import uz.tikoncha_parent.domain.model.app_error.ErrorCause
import uz.tikoncha_parent.domain.model.app_error.Outcome
import uz.tikoncha_parent.domain.model.policy.QuickBlockResult
import uz.tikoncha_parent.domain.model.policy.QuickBlockTarget
import uz.tikoncha_parent.domain.model.policy.TargetType
import uz.tikoncha_parent.domain.policy.ProtectedPackages
import uz.tikoncha_parent.domain.repository.policy.QuickBlockRepository

class AddQuickBlockUseCase(
    private val repository: QuickBlockRepository,
) {
    suspend operator fun invoke(childId: String, target: QuickBlockTarget): Outcome<QuickBlockResult> {
        if (childId.isBlank()) return Outcome.Failure(ErrorCause.ChildNotSelected)
        if (target.key.isBlank()) return Outcome.Failure(ErrorCause.Validation)
        // Tikoncha va himoyalangan ilovalar — server baribir rad etadi, so'rov yubormaymiz
        if (target.type == TargetType.APP && !ProtectedPackages.canBlock(target.key)) {
            return Outcome.Failure(ErrorCause.Validation)
        }
        return repository.add(childId, target)
    }
}