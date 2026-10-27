package uz.tikoncha_parent.data

import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import uz.tikoncha_parent.data.outbox.ParentOp
import uz.tikoncha_parent.data.outbox.PolicyOverlay
import uz.tikoncha_parent.data.remote.model.policy.PolicyCreateDto
import uz.tikoncha_parent.data.remote.model.policy.PolicyOutDto
import uz.tikoncha_parent.data.remote.model.policy.TargetsDto
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull

/** Offline-first (ota-ona): ekrandagi jadvallar = serverning oxirgi holati + navbatdagi özgarişlar. */
class ParentPolicyOverlayTest {

    private val child = "child-1"
    private val me = "parent-1"
    private val now = "2026-10-27T12:00:00Z"

    private fun dto(id: String, child: String = this.child) = PolicyOutDto(
        id = id, name = id, scope_type = "PARENT_CHILD", scope_id = child, actor_user_id = me, action = "DENY",
        paused_until = "2026-10-27T13:00:00Z", created_at = now, updated_at = now,
    )

    private fun op(kind: ParentOp.Kind, id: String?, child: String = this.child, create: PolicyCreateDto? = null,
                   patch: JsonObject? = null) =
        ParentOp(key = "k-$id-$kind", kind = kind, childId = child, policyId = id, create = create, patch = patch, createdAt = now)

    @Test
    fun createdOfflineShowsUpAndPatchesApply() {
        val create = op(ParentOp.Kind.POLICY_CREATE, "local-1", create = PolicyCreateDto(
            name = "Bedtime", scope_type = "PARENT_CHILD", scope_id = child, action = "DENY",
            targets = TargetsDto(packages = listOf("com.tiktok"))))
        val pause = op(ParentOp.Kind.POLICY_PATCH, "s1",
            patch = JsonObject(mapOf("is_active" to JsonPrimitive(false), "paused_until" to JsonNull)))
        val out = PolicyOverlay.apply(listOf(dto("s1")), listOf(create, pause), child, me, now)
        assertEquals(listOf("s1", "local-1"), out.map { it.id })
        assertFalse(out[0].is_active)
        assertNull(out[0].paused_until)
        assertEquals(me, out[1].actor_user_id)
        assertEquals(listOf("com.tiktok"), out[1].targets.packages)
    }

    @Test
    fun deleteAndOtherChildrenAndTombstones() {
        val gone = dto("s2").copy(deleted_at = now)
        val out = PolicyOverlay.apply(
            listOf(dto("s1"), gone),
            listOf(op(ParentOp.Kind.POLICY_DELETE, "s1"), op(ParentOp.Kind.POLICY_DELETE, "x", child = "child-2")),
            child, me, now,
        )
        assertEquals(emptyList(), out.map { it.id })
    }
}
