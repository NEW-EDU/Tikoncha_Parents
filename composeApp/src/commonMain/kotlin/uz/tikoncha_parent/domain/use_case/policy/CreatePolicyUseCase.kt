package uz.tikoncha_parent.domain.use_case.policy

import uz.tikoncha_parent.domain.model.app_error.ErrorCause
import uz.tikoncha_parent.domain.model.app_error.Outcome
import uz.tikoncha_parent.domain.model.policy.Policy
import uz.tikoncha_parent.domain.model.policy.PolicyConditions
import uz.tikoncha_parent.domain.model.policy.PolicyDraft
import uz.tikoncha_parent.domain.model.policy.PolicyLimits
import uz.tikoncha_parent.domain.repository.policy.PolicyRepository

class CreatePolicyUseCase(
    private val repository: PolicyRepository,
) {
    /** Serverga bormasdan oldin 422 ga olib keladigan holatlarni ushlaydi. */
    suspend operator fun invoke(childId: String, draft: PolicyDraft): Outcome<Policy> {
        if (childId.isBlank()) return Outcome.Failure(ErrorCause.ChildNotSelected)
        if (draft.name.isBlank()) return Outcome.Failure(ErrorCause.EmptyTitle)
        if (draft.targets.isEmpty) return Outcome.Failure(ErrorCause.Validation)

        if (!draft.conditions.isValid() || !draft.limits.isValid()) {
            return Outcome.Failure(ErrorCause.Validation)
        }

        return repository.createPolicy(childId, draft.withSharedDays())
    }
}

/**
 * Vaqt ham, limit ham bo'lsa — kunlar BITTA: limit kunlari vaqt kunlariga teng.
 * Qurilma limitsiz kunda jadvalni umuman qo'llamaydi, ikki xil kun esa ota-ona
 * kutmagan natija berardi (vaqt oynasida, lekin limit kuni bo'lmagan kunda).
 */
fun PolicyDraft.withSharedDays(): PolicyDraft {
    val time = conditions.time ?: return this
    val usage = limits.usage ?: return this
    if (usage.days == time.days) return this
    return copy(limits = limits.copy(usage = usage.copy(days = time.days)))
}

/**
 * Yaratish ham, tahrirlash ham shu tekshiruvdan o'tadi. Muhim: `PolicyMapper`
 * boshlanishi = tugashi bo'lgan vaqt shartini JIMGINA tashlaydi — tekshiruvsiz
 * tahrir PATCH'da `time = null` ketib, jadval 24/7 blokka aylanardi.
 */
internal fun PolicyConditions.isValid(): Boolean {
    val t = time ?: return true
    return t.days.isNotEmpty() && t.startMin != t.endMin
}

internal fun PolicyLimits.isValid(): Boolean {
    val u = usage ?: return true
    return u.days.isNotEmpty() && u.minutes >= 1
}