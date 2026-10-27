package uz.tikoncha_parent.data.outbox

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonObject
import uz.tikoncha_parent.data.local.AppSettings
import uz.tikoncha_parent.data.remote.model.policy.PolicyCreateDto
import uz.tikoncha_parent.data.remote.model.policy.PolicyOutDto
import uz.tikoncha_parent.data.remote.model.safety.SafetySettingsRequest

/**
 * Ota-ona offline paytda qilgan özgariş (jadval, maxsus himoya kaliti). Internet qaytgaç tartib bilan yuboriladi;
 * [key] — Idempotency-Key, qayta yuborilsa ham server bir marta bajaradi.
 */
@Serializable
data class ParentOp(
    val key: String,
    val kind: Kind,
    val childId: String,
    val policyId: String? = null,
    val create: PolicyCreateDto? = null,
    val patch: JsonObject? = null,
    val safety: SafetySettingsRequest? = null,
    val createdAt: String,
    val owner: String = "",
) {
    @Serializable
    enum class Kind { POLICY_CREATE, POLICY_PATCH, POLICY_DELETE, SAFETY }
}

const val LOCAL_POLICY_PREFIX = "local-"

val offlineJson = Json {
    ignoreUnknownKeys = true
    encodeDefaults = true
    coerceInputValues = true
}

/** Navbat AppSettings'da (JSON) — ilova yopilsa ham saqlanadi, logout'da tozalanadi. */
object ParentOutbox {
    private const val KEY = "outbox_v1"
    private val lock = Mutex()
    private val _ops = MutableStateFlow(load())
    val ops: StateFlow<List<ParentOp>> = _ops.asStateFlow()

    private fun load(): List<ParentOp> = runCatching {
        AppSettings.offlineValue(KEY)?.let { offlineJson.decodeFromString(ListSerializer(ParentOp.serializer()), it) }
    }.getOrNull().orEmpty()

    private fun save(list: List<ParentOp>) {
        _ops.value = list
        AppSettings.setOfflineValue(KEY, if (list.isEmpty()) null else offlineJson.encodeToString(ListSerializer(ParentOp.serializer()), list))
    }

    /** Joriy akkauntniki: boşqa akkaunt bilan kirilsa eski navbat yuborilmaydi. */
    fun current(): List<ParentOp> = _ops.value.filter { it.owner.isEmpty() || it.owner == AppSettings.userId }

    suspend fun add(op: ParentOp) = lock.withLock { save(_ops.value + op.copy(owner = AppSettings.userId)) }
    suspend fun remove(key: String) = lock.withLock { save(_ops.value.filterNot { it.key == key }) }
    suspend fun dropLocal(localId: String) = lock.withLock { save(_ops.value.filterNot { it.policyId == localId }) }
    suspend fun replaceId(localId: String, serverId: String) = lock.withLock {
        save(_ops.value.map { if (it.policyId == localId) it.copy(policyId = serverId) else it })
    }
    /** Logout'dan keyin xotiradagi nusxa ham böşaşi uçun. */
    fun reload() { _ops.value = load() }
}

/**
 * Serverning jadvallari + navbatdagi özgarişlar → ekranda körinadigan röyxat. JSON darajasida: PATCH tanasi
 * serverning öz maydon nomlarida, shuning uçun DTO'ga to'g'ridan-to'g'ri qöşiladi (sof funksiya, testlanadi).
 */
object PolicyOverlay {
    fun apply(server: List<PolicyOutDto>, ops: List<ParentOp>, childId: String, me: String, nowIso: String): List<PolicyOutDto> {
        val byId = LinkedHashMap<String, PolicyOutDto>()
        server.filter { it.deleted_at == null }.forEach { byId[it.id] = it }
        for (op in ops) {
            if (op.childId != childId) continue
            when (op.kind) {
                ParentOp.Kind.POLICY_CREATE -> {
                    val create = op.create ?: continue
                    val id = op.policyId ?: continue
                    val base = offlineJson.encodeToJsonElement(PolicyCreateDto.serializer(), create).jsonObject
                    val obj = JsonObject(base + mapOf(
                        "id" to JsonPrimitive(id),
                        "kind" to JsonPrimitive("STANDARD"),
                        "actor_user_id" to JsonPrimitive(me),
                        "created_by" to JsonPrimitive(me),
                        "created_at" to JsonPrimitive(op.createdAt),
                        "updated_at" to JsonPrimitive(op.createdAt),
                    ))
                    byId[id] = offlineJson.decodeFromJsonElement(PolicyOutDto.serializer(), obj)
                }
                ParentOp.Kind.POLICY_PATCH -> {
                    val id = op.policyId ?: continue
                    val cur = byId[id] ?: continue
                    val patch = op.patch ?: continue
                    val base = offlineJson.encodeToJsonElement(PolicyOutDto.serializer(), cur).jsonObject
                    val merged = JsonObject(base + patch + ("updated_at" to JsonPrimitive(nowIso)))
                    byId[id] = offlineJson.decodeFromJsonElement(PolicyOutDto.serializer(), merged)
                }
                ParentOp.Kind.POLICY_DELETE -> op.policyId?.let { byId.remove(it) }
                ParentOp.Kind.SAFETY -> Unit
            }
        }
        return byId.values.toList()
    }
}
