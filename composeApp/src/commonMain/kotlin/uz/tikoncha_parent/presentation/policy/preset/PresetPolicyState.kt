package uz.tikoncha_parent.presentation.policy.preset

import uz.tikoncha_parent.domain.model.LimitWindow
import uz.tikoncha_parent.domain.model.app_error.Outcome
import uz.tikoncha_parent.domain.model.app_usage.UsageHistory
import uz.tikoncha_parent.domain.model.apps.InstalledApp
import uz.tikoncha_parent.domain.model.policy.PolicyDraft
import uz.tikoncha_parent.domain.model.policy.PolicyTargets
import uz.tikoncha_parent.presentation.policy.location.ChildPin
import uz.tikoncha_parent.presentation.policy.location.LocationPickerEvent
import uz.tikoncha_parent.presentation.policy.location.LocationPickerState
import uz.tikoncha_parent.presentation.policy.model.PresetKind
import uz.tikoncha_parent.presentation.policy.targets.TargetsEditorEvent
import uz.tikoncha_parent.presentation.policy.targets.TargetsEditorState
import kotlinx.datetime.LocalDate
import kotlin.time.Instant

/** Ekran ustidagi varaqlar. */
enum class PresetSheet { TIME_START, TIME_END, LIMIT, DAYS, PAUSE, APPS }

/** "Ochiq qoladi" ro'yxatidagi ilova — nomi va ikonkasi bolaning ilovalari ro'yxatidan. */
data class ExceptionApp(val packageName: String, val label: String, val iconUrl: String?)

/**
 * Tayyor jadval ichki ekrani (Uyqu vaqti / Vaqt limiti / Dars vaqti) — Student bilan bir xil.
 * `draft` — tahrirlanayotgan nusxa, `saved` — server (yoki standart) nusxa; farq bo'lsa
 * "Saqlash" yoqiladi. `policyId == null` — bazada hali yo'q.
 */
data class PresetPolicyState(
    val childId: String = "",
    val kind: PresetKind = PresetKind.SLEEP,
    val policyId: String? = null,
    val isEnabled: Boolean = false,
    val pausedUntil: Instant? = null,
    val now: Instant = Instant.DISTANT_PAST,
    val draft: PolicyDraft? = null,
    val saved: PolicyDraft? = null,
    val childApps: List<InstalledApp> = emptyList(),
    /** Vaqt limiti: joriy oynada sarflangan daqiqa (server statistikasidan); null — noma'lum. */
    val usedMinutes: Int? = null,
    val sheet: PresetSheet? = null,
    /** Ochiq nishon muharriri / xarita — ekran ustida to'liq sahifa. */
    val targets: TargetsEditorState? = null,
    val location: LocationPickerState? = null,
    /** Oxirgi 7 kun: paket → daqiqa (nishon tanlashda tartib uchun). */
    val appUsage: Map<String, Long> = emptyMap(),
    /** Bolaning oxirgi joylashuvi — xarita shu yerdan boshlanadi. */
    val child: ChildPin? = null,
    val saving: Boolean = false,
    /** Pauza so'rovi ketmoqda. */
    val busy: Boolean = false,
    /** Switch so'rovi ketmoqda: yuborilgan qiymat. Kesh yangi holatni olib kelguncha spinner turadi. */
    val pendingEnabled: Boolean? = null,
    val error: Outcome.Failure? = null,
) {
    val isLoaded: Boolean get() = draft != null
    /** Faqat "Saqlash" bilan ketadigan maydonlar solishtiriladi (switch/pauza darhol ketadi). */
    val hasChanges: Boolean get() = draft != null && draft.editable() != saved?.editable()
    val isPaused: Boolean get() = pausedUntil != null && pausedUntil > now

    /** Parent'da hamma tayyor jadvalda istisno = `excludePackages` (ochiq qoladi / hisoblanmaydi). */
    val exceptions: List<ExceptionApp>
        get() = draft?.targets?.excludePackages.orEmpty().map { pkg ->
            val app = childApps.firstOrNull { it.packageName == pkg }
            ExceptionApp(pkg, app?.name ?: pkg, app?.iconUrl)
        }
}

private fun PolicyDraft.editable() = copy(isActive = true, pausedUntil = null, expiresAt = null)

sealed interface PresetPolicyEvent {
    /** `title` — lokalizatsiya qilingan nom; bazada yo'q jadval shu nom bilan yaratiladi. */
    data class Init(val childId: String, val kind: PresetKind, val title: String) : PresetPolicyEvent

    data class EnabledToggled(val enabled: Boolean) : PresetPolicyEvent
    data object PauseClicked : PresetPolicyEvent
    data class PauseSelected(val minutes: Int) : PresetPolicyEvent
    data object ResumeClicked : PresetPolicyEvent

    data object StartClicked : PresetPolicyEvent
    data object EndClicked : PresetPolicyEvent
    data class TimeSet(val minuteOfDay: Int) : PresetPolicyEvent
    data class QuickTimeSelected(val startMin: Int, val endMin: Int) : PresetPolicyEvent

    data class LimitWindowSelected(val window: LimitWindow) : PresetPolicyEvent
    data object LimitClicked : PresetPolicyEvent
    data class LimitSet(val minutes: Int) : PresetPolicyEvent
    data class QuickLimitSelected(val minutes: Int) : PresetPolicyEvent

    data object DaysClicked : PresetPolicyEvent
    data class DaysSet(val days: List<Int>) : PresetPolicyEvent

    data object AddExceptionClicked : PresetPolicyEvent
    data class ExceptionsAdded(val packages: List<String>) : PresetPolicyEvent
    data class ExceptionRemoved(val packageName: String) : PresetPolicyEvent

    // ── Nishonlar muharriri ──
    data object TargetsClicked : PresetPolicyEvent
    data class Targets(val e: TargetsEditorEvent) : PresetPolicyEvent
    data object TargetsDone : PresetPolicyEvent
    data object TargetsClosed : PresetPolicyEvent

    // ── Maktab hududi ──
    data class LocationToggled(val enabled: Boolean) : PresetPolicyEvent
    data object LocationClicked : PresetPolicyEvent
    data class Location(val e: LocationPickerEvent) : PresetPolicyEvent
    data object LocationDone : PresetPolicyEvent
    data object LocationClosed : PresetPolicyEvent

    data object ResetClicked : PresetPolicyEvent
    data object SheetDismissed : PresetPolicyEvent
    data object SaveClicked : PresetPolicyEvent
    data object ErrorDismissed : PresetPolicyEvent
}

sealed interface PresetPolicyEffect {
    /** Saqlandi — ro'yxatga qaytiladi. */
    data object Saved : PresetPolicyEffect
}

/**
 * Vaqt limitining joriy oynasida sarflangan daqiqa — server statistikasidan (bola ilovasi
 * yuborgan soatlik ma'lumot; bir necha daqiqa kechikishi mumkin). Server qoidasi bilan bir xil:
 * aniq ko'rsatilgan paket doim hisoblanadi; `"*"` va kategoriya orqali kelganlardan
 * `excludePackages` chiqariladi. [categoryOf] — bolaning ilovalari ro'yxatidan (paket → kategoriya).
 */
fun UsageHistory.usedMinutes(
    targets: PolicyTargets,
    categoryOf: Map<String, String?>,
    today: LocalDate,
    hour: Int,
    hourly: Boolean,
): Int {
    val categories = targets.categories.map { it.uppercase() }.toSet()
    fun counted(pkg: String): Boolean =
        pkg in targets.packages || (pkg !in targets.excludePackages &&
            (targets.isAllApps || categoryOf[pkg]?.uppercase()?.let { it in categories } == true))
    val ms = apps.filter { counted(it.packageName) }.sumOf { app ->
        val day = app.usage[today].orEmpty()
        if (hourly) (day[hour] ?: 0L).coerceAtLeast(0L) else day.values.sumOf { it.coerceAtLeast(0L) }
    }
    return (ms / 60_000L).toInt()
}
