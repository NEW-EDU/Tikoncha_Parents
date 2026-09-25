package uz.tikoncha_parent.data.policy

import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import uz.tikoncha_parent.data.mapper.policy.toCreateDto
import uz.tikoncha_parent.data.mapper.policy.toDomain
import uz.tikoncha_parent.data.mapper.policy.toJsonObject
import uz.tikoncha_parent.data.mapper.policy.toQuickBlockEntry
import uz.tikoncha_parent.data.remote.app_error.ApiErrorMapper
import uz.tikoncha_parent.data.remote.model.policy.ConditionsDto
import uz.tikoncha_parent.data.remote.model.policy.PolicyCreateDto
import uz.tikoncha_parent.data.remote.model.policy.PolicyOutDto
import uz.tikoncha_parent.data.remote.model.policy.QuickBlockListOutDto
import uz.tikoncha_parent.data.remote.model.policy.QuickBlockOutDto
import uz.tikoncha_parent.domain.model.GeoType
import uz.tikoncha_parent.domain.model.LimitWindow
import uz.tikoncha_parent.domain.model.PolicyType
import uz.tikoncha_parent.domain.model.WeekDay
import uz.tikoncha_parent.domain.model.app_error.ErrorCause
import uz.tikoncha_parent.domain.model.policy.Patch
import uz.tikoncha_parent.domain.model.policy.PolicyAction
import uz.tikoncha_parent.domain.model.policy.PolicyConditions
import uz.tikoncha_parent.domain.model.policy.PolicyDraft
import uz.tikoncha_parent.domain.model.policy.PolicyLimits
import uz.tikoncha_parent.domain.model.policy.PolicyPatch
import uz.tikoncha_parent.domain.model.policy.PolicyPreset
import uz.tikoncha_parent.domain.model.policy.PolicyTargets
import uz.tikoncha_parent.domain.model.policy.TimeCondition
import uz.tikoncha_parent.domain.model.policy.UsageLimit
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Instant

class PolicyMapperTest {

    @Test
    fun readsSingleObjectRulesFromServer() {
        val p = wireJson.decodeFromString(PolicyOutDto.serializer(), Wire.SCHOOL_POLICY).toDomain()

        assertEquals(PolicyPreset.SCHOOL, p.preset)
        assertTrue(p.targets.isAllApps)
        assertEquals(listOf("com.android.dialer"), p.targets.excludePackages)

        val time = p.conditions.time!!
        assertEquals(setOf(WeekDay.MON, WeekDay.TUE, WeekDay.WED, WeekDay.THU, WeekDay.FRI), time.days)
        assertEquals(480 to 780, time.startMin to time.endMin)

        val loc = p.conditions.location!!
        assertEquals(GeoType.CIRCLE, loc.geoType)
        assertEquals(300, loc.radiusMeters)
        assertFalse(loc.reverse)

        val usage = p.limits.usage!!
        assertEquals(LimitWindow.HOUR, usage.window)
        assertEquals(20, usage.minutes)
        assertNull(p.limits.launch)
    }

    @Test
    fun readsEmptyConditionsAsNull() {
        val out = wireJson.decodeFromString(QuickBlockOutDto.serializer(), Wire.QUICK_BLOCK_ADDED)
        val p = out.policy!!.toDomain()
        assertTrue(p.conditions.isEmpty)
        assertTrue(p.limits.isEmpty)
        assertTrue(p.isQuickBlock)
    }

    /** Eski (v2.0) ro'yxat shakli — dekoder yiqiladi va bu "noma'lum" emas, InvalidResponse. */
    @Test
    fun oldListShapeIsInvalidResponse() {
        val e = assertFailsWith<SerializationException> {
            wireJson.decodeFromString(ConditionsDto.serializer(), Wire.V20_CONDITIONS)
        }
        assertEquals(ErrorCause.InvalidResponse, ApiErrorMapper.from(e))
    }

    @Test
    fun ktorConvertErrorIsInvalidResponse() {
        val e = io.ktor.serialization.JsonConvertException("Illegal input", SerializationException("x"))
        assertEquals(ErrorCause.InvalidResponse, ApiErrorMapper.from(e))
    }

    @Test
    fun createSendsObjectsNotLists() {
        val draft = PolicyDraft(
            name = " Uyqu vaqti ",
            action = PolicyAction.DENY,
            preset = PolicyPreset.SLEEP,
            targets = PolicyTargets(packages = listOf(PolicyTargets.ALL_APPS), excludePackages = listOf("com.android.deskclock")),
            conditions = PolicyConditions(time = TimeCondition(days = WeekDay.entries.toSet(), startMin = 1320, endMin = 420)),
            limits = PolicyLimits(usage = UsageLimit(days = setOf(WeekDay.SAT), window = LimitWindow.DAY, minutes = 90)),
        )
        val json = wireJson.encodeToJsonElement(PolicyCreateDto.serializer(), draft.toCreateDto(Wire.CHILD_ID)).jsonObject

        assertEquals("Uyqu vaqti", json["name"]!!.jsonPrimitive.content)
        assertEquals("PARENT_CHILD", json["scope_type"]!!.jsonPrimitive.content)
        val time = json["conditions"]!!.jsonObject["time"]
        assertIs<JsonObject>(time)
        assertEquals("1320", time["start_min"]!!.jsonPrimitive.content)
        assertIs<JsonObject>(json["limits"]!!.jsonObject["usage"])
        assertEquals(
            listOf("com.android.deskclock"),
            json["targets"]!!.jsonObject["exclude_packages"]!!.jsonArray.map { it.jsonPrimitive.content },
        )
    }

    /** PATCH targets istisnolarni qaytarib yuboradi — aks holda server ularni o'chirardi. */
    @Test
    fun patchTargetsKeepsExcludePackages() {
        val patch = PolicyPatch(
            targets = Patch.Value(PolicyTargets(packages = listOf("*"), excludePackages = listOf("com.android.dialer"))),
        )
        val body = patch.toJsonObject()
        assertEquals(setOf("targets"), body.keys)
        val excl = body["targets"]!!.jsonObject["exclude_packages"]
        assertIs<JsonArray>(excl)
        assertEquals("com.android.dialer", excl.single().jsonPrimitive.content)
    }

    @Test
    fun brokenTimeIsDroppedNotSentAsEmptyWindow() {
        val c = PolicyConditions(time = TimeCondition(days = setOf(WeekDay.MON), startMin = 600, endMin = 600))
        val body = PolicyPatch(conditions = Patch.Value(c)).toJsonObject()
        assertEquals("null", body["conditions"]!!.jsonObject["time"].toString())
    }

    @Test
    fun quickBlockEntryCarriesActiveAndPause() {
        val list = wireJson.decodeFromString(QuickBlockListOutDto.serializer(), Wire.QUICK_BLOCK_LIST)
        val (mine, child) = list.items.map { it.toDomain() }
        val now = Instant.parse("2026-09-25T11:00:00Z")

        assertTrue(mine.isMine(Wire.PARENT_ID))
        assertFalse(mine.isActive)
        assertFalse(mine.isEnforced(now))

        assertTrue(child.isChildOwner)
        assertTrue(child.isPaused(now))
        assertFalse(child.isEnforced(now))
        assertTrue(child.isEnforced(Instant.parse("2026-09-25T12:00:01Z")))
    }

    @Test
    fun quickBlockPolicyBecomesEntry() {
        val out = wireJson.decodeFromString(QuickBlockOutDto.serializer(), Wire.QUICK_BLOCK_ADDED)
        val entry = out.policy!!.toDomain().toQuickBlockEntry()
        assertEquals(out.policy_id, entry.policyId)
        assertEquals(PolicyType.PARENT_CHILD, entry.scope)
        assertEquals(listOf("com.instagram.android"), entry.targets.packages)
        assertTrue(entry.isActive)
    }
}
