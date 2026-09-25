package uz.tikoncha_parent.domain.use_case.policy

import uz.tikoncha_parent.domain.model.app_error.ErrorCause
import uz.tikoncha_parent.domain.model.app_error.Outcome
import uz.tikoncha_parent.domain.model.policy.PolicyAction
import uz.tikoncha_parent.domain.model.policy.PolicyDraft
import uz.tikoncha_parent.domain.model.policy.PolicyPatch
import uz.tikoncha_parent.domain.model.policy.PolicyPreset
import uz.tikoncha_parent.domain.model.policy.PolicyTargets
import uz.tikoncha_parent.domain.model.app_error.map
import uz.tikoncha_parent.domain.repository.policy.PolicyRepository

class ToggleProtectionPackUseCase(
    private val repository: PolicyRepository,
) {
    suspend operator fun invoke(
        childId: String,
        myUserId: String,
        packCode: String,
        enabled: Boolean,
        title: String,
    ): Outcome<Unit> {
        if (childId.isBlank()) return Outcome.Failure(ErrorCause.ChildNotSelected)

        val myPolicy = repository.cachedPolicies(childId)
            .firstOrNull { it.isProtection && it.packCode == packCode && it.isMine(myUserId) }

        return when {
            // Jadval bor — faqat yoqib/o'chirib qo'yamiz.
            myPolicy != null ->
                repository.patchPolicy(myPolicy.id, PolicyPatch.toggle(enabled)).map { }

            // Yo'q va yoqish kerak — server idempotent, mavjud bo'lsa o'shanisini qaytaradi.
            enabled -> repository.createPolicy(
                childId = childId,
                draft = PolicyDraft(
                    name = title,
                    action = PolicyAction.DENY,
                    preset = PolicyPreset.PROTECTION,
                    targets = PolicyTargets(packs = listOf(packCode)),
                ),
            ).map { }

            // Yo'q va o'chirish kerak — qiladigan ish yo'q.
            else -> Outcome.Success(Unit)
        }
    }
}