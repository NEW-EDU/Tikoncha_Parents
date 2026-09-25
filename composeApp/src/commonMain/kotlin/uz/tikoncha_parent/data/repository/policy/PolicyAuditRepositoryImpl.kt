package uz.tikoncha_parent.data.repository.policy

import uz.tikoncha_parent.data.mapper.policy.toDomain
import uz.tikoncha_parent.data.remote.app_error.PolicyCall
import uz.tikoncha_parent.data.remote.app_error.PolicyErrorMapper
import uz.tikoncha_parent.data.remote.policy.PolicyApiService
import uz.tikoncha_parent.data.repository.apiCall
import uz.tikoncha_parent.domain.model.app_error.ErrorCause
import uz.tikoncha_parent.domain.model.app_error.Outcome
import uz.tikoncha_parent.domain.model.policy.PolicyAuditPage
import uz.tikoncha_parent.domain.repository.policy.PolicyAuditRepository

class PolicyAuditRepositoryImpl(
    private val api: PolicyApiService,
) : PolicyAuditRepository {

    override suspend fun events(childId: String, limit: Int, offset: Int): Outcome<PolicyAuditPage> =
        apiCall(TAG) {
            val r = api.events(childId = childId, limit = limit, offset = offset)
            val body = r.data
            when {
                r.success && body != null -> Outcome.Success(body.toDomain())
                r.success -> Outcome.Failure(ErrorCause.InvalidResponse)
                else -> Outcome.Failure(PolicyErrorMapper.from(PolicyCall.EVENTS, r.code), r.error)
            }
        }

    private companion object { const val TAG = "PolicyAuditRepository" }
}