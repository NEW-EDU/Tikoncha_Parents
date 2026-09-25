package uz.tikoncha_parent.domain.use_case.policy

import kotlin.time.Clock
import kotlin.time.Duration.Companion.minutes
import uz.tikoncha_parent.domain.model.app_error.ErrorCause
import uz.tikoncha_parent.domain.model.app_error.Outcome
import uz.tikoncha_parent.domain.model.policy.Policy
import uz.tikoncha_parent.domain.model.policy.PolicyAction
import uz.tikoncha_parent.domain.model.policy.PolicyDraft
import uz.tikoncha_parent.domain.model.policy.PolicyTargets
import uz.tikoncha_parent.domain.repository.policy.PolicyRepository

/**
 * ⚠️ UI'da YOQILMAGAN (FeatureFlags.BONUS_TIME = false).
 *
 * Server baholash mantig'ida jonli ALLOW jadvali ro'yxatda YO'Q barcha ilovalarni
 * `NOT_IN_ALLOWLIST` bilan yopadi. Ya'ni "PUBG ga 30 daqiqa" bergan ota-ona
 * o'sha 30 daqiqada qolgan hamma ilovani bloklab qo'yadi. Backend tomonida
 * `exclusive` / `kind = EXCEPTION` qarori chiqmaguncha ishlatilmaydi.
 */
class GrantBonusTimeUseCase(
    private val repository: PolicyRepository,
) {
    suspend operator fun invoke(
        childId: String,
        packages: List<String>,
        minutes: Int,
        name: String,
    ): Outcome<Policy> {
        if (childId.isBlank()) return Outcome.Failure(ErrorCause.ChildNotSelected)
        if (packages.isEmpty()) return Outcome.Failure(ErrorCause.Validation)
        if (minutes < 1) return Outcome.Failure(ErrorCause.Validation)

        return repository.createPolicy(
            childId = childId,
            draft = PolicyDraft(
                name = name,
                action = PolicyAction.ALLOW,
                targets = PolicyTargets(packages = packages),
                priority = BONUS_PRIORITY,
                expiresAt = Clock.System.now() + minutes.minutes,
            ),
        )
    }

    private companion object { const val BONUS_PRIORITY = 200 }
}