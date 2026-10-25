package uz.tikoncha_parent.data.remote.model.ai

import kotlinx.serialization.Serializable

@Serializable
data class ParentChatMessageDto(val role: String, val text: String)

@Serializable
data class ParentChatRequest(
    val messages: List<ParentChatMessageDto>,
    /** Ekranda tanlangan farzand — ism aytilmagan savollar shu farzand haqida. */
    val child_id: String? = null,
)

/** GET /ai/parent/children/{id}/report — raqamlar (facts) bazadan, matn (summary) AI'dan. */
@Serializable
data class ChildReportDto(
    val child_id: String = "",
    val period: String = "day",
    val facts: ReportFactsDto = ReportFactsDto(),
    val summary: String? = null,
    /** Ota-onaning AI tangaçalari — yordamçi sarlavhasida. */
    val coins: Int? = null,
)

@Serializable
data class ReportFactsDto(
    val date_from: String = "",
    val date_to: String = "",
    val screen_time: ScreenTimeDto = ScreenTimeDto(),
    val previous_period: PreviousPeriodDto = PreviousPeriodDto(),
    val tasks: TasksDto = TasksDto(),
    val activity: ActivityDto = ActivityDto(),
    val tutor: TutorDto = TutorDto(),
)

@Serializable
data class ScreenTimeDto(
    val total_minutes: Int = 0,
    val total_text: String = "",
    val avg_per_day_text: String = "",
    val top_apps: List<AppTimeDto> = emptyList(),
    val night: NightDto = NightDto(),
    val has_data: Boolean = false,
)

@Serializable
data class AppTimeDto(val name: String = "", val minutes: Int = 0, val text: String = "")

@Serializable
data class NightDto(val nights_with_use: Int = 0, val minutes: Int = 0, val text: String = "")

@Serializable
data class PreviousPeriodDto(val total_minutes: Int = 0, val change_percent: Int? = null)

@Serializable
data class TasksDto(val assigned: Int = 0, val done: Int = 0, val overdue: Int = 0)

@Serializable
data class ActivityDto(val status: String = "active", val inactive_days: Int = 0)

@Serializable
data class TutorDto(val questions: Int = 0, val days: Int = 0)
