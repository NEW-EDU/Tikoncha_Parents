package uz.tikoncha_parent.domain.model.ai

import uz.tikoncha_parent.domain.model.app_error.Outcome

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
    /**
     * Qoida hozir işlamaydi (masalan, farzand telefoni "Faqat kuzatiş"da yoki ruxsat yetişmaydi) — bitta jumla,
     * kartada sariq rangda. Server bermasa null.
     */
    val warning: String? = null,
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
    /** Yordamçi ilovaning bir ekranini tavsiya qildi: javob ostida "Oçiş: …" tugmasi. */
    data class Nav(val target: NavTarget) : AssistantEvent
    /**
     * Ota-onaning öz telefonidagi ilova sozlamasi (şrift, mavzu, til, tebraniş, bosh sahifa körinişi, ovozli javoblar)
     * — darhol qöllanadi, "Qaytariş" bilan bekor qilinadi. [key]/[value] — server qiymatlari (masalan text_size/large).
     */
    data class Setting(val key: String, val value: String) : AssistantEvent
    data class Done(val coins: Int?) : AssistantEvent
    /** Oxirgi hodisa. [code]: rate_limited · no_coins · ai_unavailable · bad_request · network. */
    data class Failed(val code: String, val text: String?) : AssistantEvent
}

data class AssistantTurn(val fromParent: Boolean, val text: String)

/** SSE `nav` hodisasidagi ekran ([screen] — server qiymati), [childId] — qaysi farzand uçun (bölsa). */
data class NavTarget(val screen: NavScreen, val childId: String?)

enum class NavScreen(val key: String) {
    MAP("map"), PROTECTION("protection"), RULES("rules"), TASKS("tasks"), ADD_CHILD("add_child"),
    SUBSCRIPTION("subscription"), COINS("coins"), LIVE("live"), STATS("stats"), CHAT("chat"), PROFILE("profile"),

    // Ilova sozlamalari va yordam — yordamçi ilovaning hamma joyiga olib bora olsin ("şriftni kattalaştir",
    // "tungi rejim", "tilni rusçaga özgartir", "qanday işlataman — video", "yordam xizmati" …)
    SETTINGS("settings"), TEXT_SIZE("text_size"), THEME("theme"), LANGUAGE("language"),
    NOTIFICATIONS("notifications"), TUTORIALS("tutorials"), PAYMENT_HISTORY("payment_history"),
    CHILDREN("children"), SUPPORT("support"),

    // Farzandning Tikonchagram (Telegram) qoidalari, qoidalar tarixi, bildiriş sozlamalari, tebraniş, ilova haqida
    TIKONCHAGRAM("tikonchagram"), RULES_HISTORY("rules_history"), NOTIFICATION_SETTINGS("notification_settings"),
    HAPTICS("haptics"), ABOUT("about"),

    // Hisob amallari va Plus tölovi — ekranning özi tasdiq söraydi; yordamçi faqat oça oladi (avto-oçilmaydi)
    LOGOUT("logout"), DELETE_ACCOUNT("delete_account"), PLUS_PAYMENT("plus_payment");

    /** Bu ekranlar javobdan keyin özi oçilmaydi — ota-ona tugmani bosişi şart. */
    val needsTap: Boolean get() = this == LOGOUT || this == DELETE_ACCOUNT

    companion object {
        /** Noma'lum ekran — null (eski ilova yangi server qiymatini jimgina ötkazib yuboradi). */
        fun from(key: String?): NavScreen? = entries.firstOrNull { it.key == key }
    }
}


/** Farzand Bilimdonga yuborgan xavotirli xabar (giyohvand modda, qurol, zöravonlik, öziga zarar, jinsiy mazmun). */
data class SafetyAlert(
    val id: String,
    val childId: String,
    val childName: String,
    val title: String,
    val text: String,
    val imageUrl: String?,
    /** Server vaqti (UTC ISO). */
    val createdAt: String,
)

/** Ovozli savol matni: [language] — ISO 639-3 ("uzb", "rus" …), server bermasa null. Özbekça — eski lotinda. */
data class Transcript(val text: String, val language: String?)

/** Qoralamani tasdiqlaş xatosi: [errorCode] — maşina kodi (quick_block_paid, todo_not_child_done …). */
data class DraftApplyError(val failure: Outcome.Failure, val errorCode: String?)
