package uz.tikoncha_parent.domain.model.ai

/** Hisobot davri — server `period` qiymatlari. */
enum class ReportPeriod(val key: String) { TODAY("day"), YESTERDAY("yesterday"), WEEK("week") }

data class ChildReport(
    val period: ReportPeriod,
    /** AI yozgan qisqa xulosa; AI ishlamasa null — raqamlar baribir ko'rsatiladi. */
    val summary: String?,
    val hasData: Boolean,
    val totalText: String,
    val avgPerDayText: String,
    val changePercent: Int?,
    val totalMinutes: Int,
    val topApps: List<TopApp>,
    val nightsWithUse: Int,
    val nightText: String,
    val tasksAssigned: Int,
    val tasksDone: Int,
    val activityStatus: String,
    val tutorQuestions: Int = 0,
    val coins: Int? = null,
)

data class TopApp(val name: String, val text: String, val minutes: Int)

/**
 * Ota-ona tasdiqlashi kerak bo'lgan özgariş (server qoralamasi): yangi jadval, jadvalni to'xtatiş / öçiriş,
 * topşiriq, farzand sörovi javobi. [confirm] — tugma: enable · confirm · delete · approve · reject.
 */
data class RuleDraft(
    val draftId: String,
    val childName: String,
    val title: String,
    val lines: List<String>,
    val kind: String = "policy_create",
    val confirm: String = "enable",
    val childId: String = "",
    /** Topşiriq kartasi: mukofot (ota-ona kartada özgartira oladi). */
    val coins: Int? = null,
)

/** Farzand telefonining oxirgi joylaşuvi (yordamçi javobi ostidagi karta). */
data class LocationCard(
    val childId: String,
    val childName: String,
    val lat: Double,
    val lng: Double,
    val minutesAgo: Int,
    val gpsOff: Boolean = false,
)

/** Himoya holati: rejim va yetişmagan ruxsatlar (yordamçi javobi ostidagi karta). */
data class ProtectionCard(
    val childId: String,
    val childName: String,
    /** DEFAULT · GUJANAK · STRICT; noma'lum bo'lsa null. */
    val mode: String?,
    val problems: List<String>,
)

sealed interface AssistantEvent {
    data class Status(val text: String) : AssistantEvent
    data class Token(val text: String) : AssistantEvent
    data class Draft(val draft: RuleDraft) : AssistantEvent
    data class Location(val card: LocationCard) : AssistantEvent
    data class Protection(val card: ProtectionCard) : AssistantEvent
    data class Done(val coins: Int?) : AssistantEvent
    /** Oxirgi hodisa. [code]: rate_limited · no_coins · ai_unavailable · bad_request · network. */
    data class Failed(val code: String, val text: String?) : AssistantEvent
}

data class AssistantTurn(val fromParent: Boolean, val text: String)
