package uz.tikoncha_parent.data.remote.policy

import io.ktor.client.HttpClient
import io.ktor.client.request.parameter
import io.ktor.client.request.setBody
import io.ktor.http.HttpMethod
import kotlinx.serialization.json.JsonObject
import uz.tikoncha_parent.data.remote.model.AppsResponse
import uz.tikoncha_parent.data.remote.model.policy.ApiEnvelope
import uz.tikoncha_parent.data.remote.model.policy.EvaluateInDto
import uz.tikoncha_parent.data.remote.model.policy.EvaluateOutDto
import uz.tikoncha_parent.data.remote.model.policy.PacksListOutDto
import uz.tikoncha_parent.data.remote.model.policy.PolicyCreateDto
import uz.tikoncha_parent.data.remote.model.policy.PolicyDeleteOutDto
import uz.tikoncha_parent.data.remote.model.policy.PolicyEventListOutDto
import uz.tikoncha_parent.data.remote.model.policy.PolicyListOutDto
import uz.tikoncha_parent.data.remote.model.policy.PolicyOutDto
import uz.tikoncha_parent.data.remote.model.policy.QuickBlockInDto
import uz.tikoncha_parent.data.remote.model.policy.QuickBlockListOutDto
import uz.tikoncha_parent.data.remote.model.policy.QuickBlockOutDto
import uz.tikoncha_parent.data.remote.safeRequest
import uz.tikoncha_parent.domain.model.policy.QuickBlockTarget
import uz.tikoncha_parent.domain.model.policy.TargetType

class PolicyApiService(private val client: HttpClient) {

    /** Bola qurilmasidagi ilovalar — v2 ga aloqasi yo'q, o'zgarmaydi. */
    suspend fun childApps(userId: String): AppsResponse =
        client.safeRequest(HttpMethod.Get, "/installed-apps") {
            parameter("user_id", userId)
        }

    /** [since] berilsa server faqat o'zgarganlarni qaytaradi (o'chirilganlar `deleted_at` bilan). */
    suspend fun list(childId: String, since: String? = null): ApiEnvelope<PolicyListOutDto> =
        client.safeRequest(HttpMethod.Get, "/v2/policies") {
            parameter("child_id", childId)
            if (since != null) parameter("since", since)
        }

    suspend fun get(policyId: String): ApiEnvelope<PolicyOutDto> =
        client.safeRequest(HttpMethod.Get, "/v2/policies/$policyId")

    suspend fun create(body: PolicyCreateDto): ApiEnvelope<PolicyOutDto> =
        client.safeRequest(HttpMethod.Post, "/v2/policies") { setBody(body) }

    /** Tana JsonObject — sabab PolicyPatchEncoder izohida. */
    suspend fun patch(policyId: String, body: JsonObject): ApiEnvelope<PolicyOutDto> =
        client.safeRequest(HttpMethod.Patch, "/v2/policies/$policyId") { setBody(body) }

    suspend fun delete(policyId: String): ApiEnvelope<PolicyDeleteOutDto> =
        client.safeRequest(HttpMethod.Delete, "/v2/policies/$policyId")

    suspend fun quickBlocks(childId: String): ApiEnvelope<QuickBlockListOutDto> =
        client.safeRequest(HttpMethod.Get, "/v2/policies/quick-block") {
            parameter("child_id", childId)
        }

    suspend fun quickBlockAdd(body: QuickBlockInDto): ApiEnvelope<QuickBlockOutDto> =
        client.safeRequest(HttpMethod.Post, "/v2/policies/quick-block") { setBody(body) }

    /** O'chirishda server tanani emas, query parametrni kutadi. */
    suspend fun quickBlockRemove(
        childId: String,
        target: QuickBlockTarget,
    ): ApiEnvelope<QuickBlockOutDto> =
        client.safeRequest(HttpMethod.Delete, "/v2/policies/quick-block") {
            parameter("child_id", childId)
            when (target.type) {
                TargetType.APP -> parameter("package", target.key)
                TargetType.SITE -> parameter("site", target.key)
                TargetType.FEATURE -> parameter("feature", target.key)
            }
        }

    suspend fun evaluate(body: EvaluateInDto): ApiEnvelope<EvaluateOutDto> =
        client.safeRequest(HttpMethod.Post, "/v2/policies/evaluate") { setBody(body) }

    suspend fun events(childId: String, limit: Int, offset: Int): ApiEnvelope<PolicyEventListOutDto> =
        client.safeRequest(HttpMethod.Get, "/v2/policies/events") {
            parameter("child_id", childId)
            parameter("limit", limit)
            parameter("offset", offset)
        }

    suspend fun packs(since: String? = null): ApiEnvelope<PacksListOutDto> =
        client.safeRequest(HttpMethod.Get, "/v2/packs") {
            if (since != null) parameter("since", since)
        }
}