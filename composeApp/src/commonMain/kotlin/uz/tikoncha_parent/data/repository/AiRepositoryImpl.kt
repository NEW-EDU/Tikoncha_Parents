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
import uz.tikoncha_parent.domain.model.ai.SafetyAlert
import uz.tikoncha_parent.domain.model.ai.LocationCard
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

    override fun ask(turns: List<AssistantTurn>, childId: String?): Flow<AssistantEvent> = flow {
        val request = ParentChatRequest(
            messages = turns.takeLast(MAX_TURNS).map { ParentChatMessageDto(if (it.fromParent) "user" else "model", it.text) },
            child_id = childId,
        )
        var finished = false
        try {
            api.chat(request).collect { ev ->
                val mapped = map(ev) ?: return@collect
                emit(mapped)
                if (mapped is AssistantEvent.Done || mapped is AssistantEvent.Failed) finished = true
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

    override suspend fun applyDraft(draftId: String, coin: Int?): Outcome<Unit> = apiCall(TAG) {
        val env = api.applyDraft(draftId, coin)
        if (env.success) Outcome.Success(Unit) else Outcome.Failure(ApiErrorMapper.fromCode(env.code), env.error)
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
    }
}
