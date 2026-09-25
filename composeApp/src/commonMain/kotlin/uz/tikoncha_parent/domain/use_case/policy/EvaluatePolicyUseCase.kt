package uz.tikoncha_parent.domain.use_case.policy

import kotlinx.datetime.LocalDateTime
import uz.tikoncha_parent.domain.model.app_error.ErrorCause
import uz.tikoncha_parent.domain.model.app_error.Outcome
import uz.tikoncha_parent.domain.model.policy.EvalResult
import uz.tikoncha_parent.domain.model.policy.EvalTargetRef
import uz.tikoncha_parent.domain.repository.policy.PolicyRepository

class EvaluatePolicyUseCase(
    private val repository: PolicyRepository,
) {
    /**
     * [at] — bolaning MAHALLIY vaqti. Ota-ona boshqa vaqt mintaqasida bo'lsa
     * yubormaslik afzal: server o'zi Asia/Tashkent `now` ni oladi.
     */
    suspend operator fun invoke(
        childId: String,
        target: EvalTargetRef,
        at: LocalDateTime? = null,
    ): Outcome<EvalResult> {
        if (childId.isBlank()) return Outcome.Failure(ErrorCause.ChildNotSelected)
        return repository.evaluate(childId, target, at)
    }
}