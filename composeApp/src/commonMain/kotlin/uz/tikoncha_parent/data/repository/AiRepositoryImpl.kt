package uz.tikoncha_parent.data.repository

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import uz.tikoncha_parent.data.remote.AiApiService
import uz.tikoncha_parent.data.remote.app_error.ApiErrorMapper
import uz.tikoncha_parent.data.remote.model.ai.ChildReportDto
import uz.tikoncha_parent.data.remote.model.ai.ParentChatMessageDto
import uz.tikoncha_parent.data.remote.model.ai.ParentChatRequest
import uz.tikoncha_parent.domain.model.ai.AssistantEvent
import uz.tikoncha_parent.domain.model.ai.AssistantTurn
import uz.tikoncha_parent.domain.model.ai.ChildReport
import uz.tikoncha_parent.domain.model.ai.DraftApplyError
import uz.tikoncha_parent.domain.model.ai.Transcript
import uz.tikoncha_parent.domain.model.ai.SafetyAlert
import uz.tikoncha_parent.domain.model.ai.LocationCard
import uz.tikoncha_parent.domain.model.ai.NavScreen
import uz.tikoncha_parent.domain.model.ai.NavTarget
import uz.tikoncha_parent.domain.model.ai.ProtectionCard
import uz.tikoncha_parent.domain.model.ai.TopApp
import uz.tikoncha_parent.domain.model.ai.ReportPeriod
import uz.tikoncha_parent.domain.model.ai.RuleDraft
import uz.tikoncha_parent.domain.model.app_error.ErrorCause
import uz.tikoncha_parent.domain.model.app_error.Outcome
import uz.tikoncha_parent.domain.repository.AiRepository
import uz.tikoncha_parent.platform.Logger

class AiRepositoryImpl(private val api: AiApiService) : AiRepository {

    override suspend fun report(childId: String, period: ReportPeriod): Outcome<ChildReport> = apiCall(TAG) {
        val env = api.report(childId, period.key)
        val data = env.data
        if (env.success && data != null) Outcome.Success(data.toDomain(period))
        else Outcome.Failure(ApiErrorMapper.fromCode(env.code), env.error)
    }

    override fun ask(turns: List<AssistantTurn>, childId: String?, inputLang: String?): Flow<AssistantEvent> = flow {
        val request = ParentChatRequest(
            messages = turns.takeLast(MAX_TURNS).map { ParentChatMessageDto(if (it.fromParent) "user" else "model", it.text) },
            child_id = childId,
            input_lang = inputLang?.takeIf { it.isNotBlank() },
        )
        var finished = false
        var received = false
        var attempt = 0
        try {
            while (true) {
                try {
                    api.chat(request).collect { ev ->
                        val mapped = map(ev) ?: return@collect
                        received = true
                        emit(mapped)
                        if (mapped is AssistantEvent.Done || mapped is AssistantEvent.Failed) finished = true
                    }
                    break
                } catch (e: AiApiService.EnvelopeException) {
                    // Server yangilanayotganda nginx ~30 s 502/503 qaytaradi: so'rov ilovaga yetmagan — tanga
                    // olinmagan, karta yaratilmagan. Hech narsa kelmagan bo'lsa jimgina qayta urinamiz.
                    if (received || e.code !in RETRY_CODES || attempt >= RETRY_DELAYS_MS.size) throw e
                    kotlinx.coroutines.delay(RETRY_DELAYS_MS[attempt++])
                }
            }
            if (!finished) emit(AssistantEvent.Done(null))
        } catch (e: CancellationException) {
            throw e
        } catch (e: AiApiService.EnvelopeException) {
            emit(AssistantEvent.Failed(if (e.code == 401) "session" else "server", e.serverMessage))
        } catch (e: Throwable) {
            Logger.e(TAG, e.message ?: "ask failed", e)
            val cause = ApiErrorMapper.from(e)
            emit(AssistantEvent.Failed(if (cause == ErrorCause.NoInternet || cause == ErrorCause.Timeout) "network" else "server", null))
        }
    }

    override suspend fun applyDraft(draftId: String, coin: Int?): DraftApplyError? {
        var errorCode: String? = null
        val r = apiCall(TAG) {
            val env = api.applyDraft(draftId, coin)
            errorCode = env.errorCode
            if (env.success) Outcome.Success(Unit) else Outcome.Failure(ApiErrorMapper.fromCode(env.code), env.error)
        }
        return (r as? Outcome.Failure)?.let { DraftApplyError(it, errorCode) }
    }

    override suspend fun remind(childId: String, kind: String): Outcome<Unit> = apiCall(TAG) {
        val env = api.remind(childId, kind)
        if (env.success) Outcome.Success(Unit) else Outcome.Failure(ApiErrorMapper.fromCode(env.code), env.error)
    }

    override suspend fun alerts(childId: String?): Outcome<List<SafetyAlert>> = apiCall(TAG) {
        val env = api.alerts(childId)
        if (env.success) {
            Outcome.Success(env.data.orEmpty().map {
                SafetyAlert(
                    id = it.id, childId = it.child_id, childName = it.child_name, title = it.title, text = it.text,
                    imageUrl = it.image_url, createdAt = it.created_at,
                )
            })
        } else Outcome.Failure(ApiErrorMapper.fromCode(env.code), env.error)
    }

    @OptIn(kotlin.time.ExperimentalTime::class)
    override suspend fun markChatRead(chatId: String) {
        runCatching { api.markChatRead(chatId, kotlin.time.Clock.System.now().toString()) }
    }

    override suspend fun discardDraft(draftId: String) {
        runCatching { api.discardDraft(draftId) }
    }

    override suspend fun transcribe(audio: ByteArray, fileName: String, mimeType: String, lang: String): Outcome<Transcript> =
        apiCall(TAG) {
            val env = api.transcribe(audio, fileName, mimeType, lang)
            val data = env.data
            if (env.success && data != null) Outcome.Success(Transcript(data.text.trim(), data.language?.takeIf { it.isNotBlank() }))
            else Outcome.Failure(ApiErrorMapper.fromCode(env.code), env.error)
        }

    override var ttsUnsupported: Boolean = false
        private set

    override suspend fun speech(text: String, lang: String): Outcome<ByteArray> = apiCall(TAG) {
        val r = api.speech(text, lang)
        val audio = r.audio
        if (audio != null && audio.isNotEmpty()) {
            Outcome.Success(audio)
        } else {
            // 404 / 405 — serverda yöq, 503 — ovoz xizmati sozlanmagan: tugma yaşiriladi (429 — faqat bugun)
            if (r.code == 404 || r.code == 405 || r.code == 503) ttsUnsupported = true
            Outcome.Failure(ApiErrorMapper.fromCode(r.code), null)
        }
    }

    private fun map(ev: JsonObject): AssistantEvent? {
        val text = ev["text"]?.jsonPrimitive?.contentOrNull
        return when (ev["type"]?.jsonPrimitive?.contentOrNull) {
            "token" -> text?.takeIf { it.isNotEmpty() }?.let { AssistantEvent.Token(it) }
            "status" -> text?.let { AssistantEvent.Status(it) }
            "draft" -> ev["data"]?.jsonObject?.let { d ->
                AssistantEvent.Draft(
                    RuleDraft(
                        draftId = d["draft_id"]?.jsonPrimitive?.contentOrNull ?: return null,
                        childName = d["child_name"]?.jsonPrimitive?.contentOrNull.orEmpty(),
                        title = d["title"]?.jsonPrimitive?.contentOrNull.orEmpty(),
                        lines = d["lines"]?.jsonArray?.mapNotNull { it.jsonPrimitive.contentOrNull }.orEmpty(),
                        kind = d["kind"]?.jsonPrimitive?.contentOrNull ?: "policy_create",
                        confirm = d["confirm"]?.jsonPrimitive?.contentOrNull ?: "enable",
                        childId = d["child_id"]?.jsonPrimitive?.contentOrNull.orEmpty(),
                        coins = d["coins"]?.jsonPrimitive?.intOrNull,
                        warning = d["warning"]?.jsonPrimitive?.contentOrNull?.trim()?.takeIf { it.isNotEmpty() },
                    )
                )
            }
            "card" -> ev["data"]?.jsonObject?.takeIf { it["kind"]?.jsonPrimitive?.contentOrNull == "protection" }?.let { d ->
                AssistantEvent.Protection(
                    ProtectionCard(
                        childId = d["child_id"]?.jsonPrimitive?.contentOrNull.orEmpty(),
                        childName = d["child_name"]?.jsonPrimitive?.contentOrNull.orEmpty(),
                        mode = d["mode"]?.jsonPrimitive?.contentOrNull,
                        problems = d["problems"]?.jsonArray?.mapNotNull { it.jsonPrimitive.contentOrNull }.orEmpty(),
                    )
                )
            } ?: ev["data"]?.jsonObject?.takeIf { it["kind"]?.jsonPrimitive?.contentOrNull == "location" }?.let { d ->
                AssistantEvent.Location(
                    LocationCard(
                        childId = d["child_id"]?.jsonPrimitive?.contentOrNull.orEmpty(),
                        childName = d["child_name"]?.jsonPrimitive?.contentOrNull.orEmpty(),
                        lat = d["lat"]?.jsonPrimitive?.doubleOrNull ?: return null,
                        lng = d["lng"]?.jsonPrimitive?.doubleOrNull ?: return null,
                        minutesAgo = d["minutes_ago"]?.jsonPrimitive?.intOrNull ?: 0,
                        gpsOff = d["gps_off"]?.jsonPrimitive?.booleanOrNull ?: false,
                    )
                )
            }
            // {type: nav, screen, child_id?} — maydonlar tepada yoki `data` ichida; noma'lum ekran — e'tiborsiz
            "nav" -> runCatching {
                val d = ev["data"] as? JsonObject ?: ev
                NavScreen.from(d["screen"]?.jsonPrimitive?.contentOrNull)?.let { screen ->
                    AssistantEvent.Nav(NavTarget(screen, d["child_id"]?.jsonPrimitive?.contentOrNull?.takeIf { it.isNotBlank() }))
                }
            }.getOrNull()
            // {type: setting, data: {key, value}} — noma'lum kalit/qiymatni ilova özi taşlab yuboradi
            "setting" -> runCatching {
                val d = ev["data"] as? JsonObject ?: ev
                val key = d["key"]?.jsonPrimitive?.contentOrNull?.trim().orEmpty()
                val value = d["value"]?.jsonPrimitive?.contentOrNull?.trim().orEmpty()
                if (key.isEmpty() || value.isEmpty()) null else AssistantEvent.Setting(key, value)
            }.getOrNull()
            "done" -> AssistantEvent.Done(ev["coins"]?.jsonPrimitive?.intOrNull)
            "error" -> AssistantEvent.Failed(ev["code"]?.jsonPrimitive?.contentOrNull ?: "server", text)
            else -> null
        }
    }

    private fun ChildReportDto.toDomain(period: ReportPeriod): ChildReport {
        val st = facts.screen_time
        return ChildReport(
            period = period,
            summary = summary?.takeIf { it.isNotBlank() },
            hasData = st.has_data,
            totalText = st.total_text,
            avgPerDayText = st.avg_per_day_text,
            changePercent = facts.previous_period.change_percent,
            totalMinutes = st.total_minutes,
            topApps = st.top_apps.take(4).map { TopApp(it.name, it.text, it.minutes) },
            nightsWithUse = st.night.nights_with_use,
            nightText = st.night.text,
            tasksAssigned = facts.tasks.assigned,
            tasksDone = facts.tasks.done,
            activityStatus = facts.activity.status,
            tutorQuestions = facts.tutor.questions,
            coins = coins,
        )
    }

    private companion object {
        const val TAG = "AiRepository"
        const val MAX_TURNS = 16
        val RETRY_CODES = setOf(502, 503)
        /** Jami ~33 s — server qayta işga tuşişi (~25–35 s) qamrab olinadi. */
        val RETRY_DELAYS_MS = longArrayOf(3_000, 5_000, 8_000, 8_000, 9_000)
    }
}
