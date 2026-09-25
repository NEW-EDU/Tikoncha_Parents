package uz.tikoncha_parent.domain.repository.policy

import uz.tikoncha_parent.domain.model.app_error.Outcome
import uz.tikoncha_parent.domain.model.policy.PolicyAuditPage

interface PolicyAuditRepository {
    suspend fun events(childId: String, limit: Int = 50, offset: Int = 0): Outcome<PolicyAuditPage>
}