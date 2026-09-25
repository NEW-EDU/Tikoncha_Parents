package uz.tikoncha_parent.presentation.policy.editor

import uz.tikoncha_parent.domain.model.LimitWindow
import uz.tikoncha_parent.domain.model.app_error.Outcome
import uz.tikoncha_parent.domain.model.apps.InstalledApp
import uz.tikoncha_parent.domain.model.policy.PolicyDraft
import uz.tikoncha_parent.domain.model.policy.TimeCondition
import uz.tikoncha_parent.domain.model.policy.UsageLimit
import uz.tikoncha_parent.domain.policy.PresetDefaults
import uz.tikoncha_parent.presentation.policy.location.ChildPin
import uz.tikoncha_parent.presentation.policy.location.LocationPickerEvent
import uz.tikoncha_parent.presentation.policy.location.LocationPickerState
import uz.tikoncha_parent.presentation.policy.targets.TargetsEditorEvent
import uz.tikoncha_parent.presentation.policy.targets.TargetsEditorState
import kotlin.time.Instant

/** Asosiy ekran ustidagi sahifalar: Vaqt va Limit o'z "Saqlash"i bilan. */
enum class EditorPage { MAIN, TIME, LIMIT }
enum class EditorSheet { ADD_CONDITION, TIME_START, TIME_END, LIMIT, DAYS, PAUSE }
enum class EditorDialog { NAME, DELETE }
enum class ConditionKind { TIME, LIMIT, LOCATION }

/**
 * "O'zim sozlayman" jadvali (Student `PolicyEditorState` bilan bir xil): yaratish (`policyId == null`)
 * va tahrirlash. Boshqasining jadvali (bola, ikkinchi ota-ona, maktab) — faqat ko'rish ([readOnly]).
 *
 * Ikki qism: nimalar yopiladi (majburiy) va qachon (ixtiyoriy — bo'lmasa doim).
 * `timeEdit` / `limitEdit` — sahifadagi vaqtinchalik nusxa; sahifa "Saqlash"i `draft`ga tushiradi,
 * ekran "Saqlash"i serverga yuboradi.
 */
data class PolicyEditorState(
    val childId: String = "",
    val policyId: String? = null,
    val readOnly: Boolean = false,
    val isEnabled: Boolean = false,
    val pausedUntil: Instant? = null,
    val now: Instant = Instant.DISTANT_PAST,
    val draft: PolicyDraft? = null,
    val saved: PolicyDraft? = null,
    val childApps: List<InstalledApp> = emptyList(),
    /** Oxirgi 7 kun ishlatilishi — nishon tanlashda tartib uchun. */
    val appUsage: Map<String, Long> = emptyMap(),
    val child: ChildPin? = null,
    /** Bola obunasi pullikmi; null — noma'lum. "Faqat tanlanganlar ochiq" (ALLOW) — Plus. */
    val paid: Boolean? = null,
    val page: EditorPage = EditorPage.MAIN,
    val timeEdit: TimeCondition? = null,
    val limitEdit: UsageLimit? = null,
    val sheet: EditorSheet? = null,
    val dialog: EditorDialog? = null,
    val targets: TargetsEditorState? = null,
    val location: LocationPickerState? = null,
    val menuOpen: Boolean = false,
    val saving: Boolean = false,
    val deleting: Boolean = false,
    val busy: Boolean = false,
    /** Switch so'rovi ketmoqda: yuborilgan qiymat. Kesh yangi holatni olib kelguncha spinner turadi. */
    val pendingEnabled: Boolean? = null,
    val payWall: Boolean = false,
    val error: Outcome.Failure? = null,
) {
    val isDetail: Boolean get() = policyId != null
    val isLoaded: Boolean get() = draft != null
    val isPaused: Boolean get() = pausedUntil != null && pausedUntil > now

    val hasTargets: Boolean get() = draft?.targets?.isEmpty == false

    /** Faqat "Saqlash" bilan ketadigan maydonlar solishtiriladi (switch/pauza darhol ketadi). */
    val hasChanges: Boolean get() = draft != null && draft.editable() != saved?.editable()

    /** Yaratishda nishon bo'lsa yetadi; tahrirlashda o'zgarish ham kerak. */
    val canSave: Boolean get() = !readOnly && hasTargets && (!isDetail || hasChanges) && !saving && !deleting

    val conditions: List<ConditionKind>
        get() = listOfNotNull(
            draft?.conditions?.time?.let { ConditionKind.TIME },
            draft?.limits?.usage?.let { ConditionKind.LIMIT },
            draft?.conditions?.location?.let { ConditionKind.LOCATION },
        )

    companion object {
        val DEFAULT_TIME = TimeCondition(days = PresetDefaults.WORK_DAYS, startMin = 8 * 60, endMin = 14 * 60)
        val DEFAULT_LIMIT = UsageLimit(days = PresetDefaults.ALL_DAYS, window = LimitWindow.DAY, minutes = 60)
    }
}

private fun PolicyDraft.editable() = copy(isActive = true, pausedUntil = null, expiresAt = null)

sealed interface PolicyEditorEvent {
    /** `defaultTitle` — "Yangi jadval" (Compose'dan); tahrirlashda e'tiborsiz. */
    data class Init(val childId: String, val policyId: String?, val defaultTitle: String) : PolicyEditorEvent

    data class EnabledToggled(val enabled: Boolean) : PolicyEditorEvent
    data object PauseClicked : PolicyEditorEvent
    data class PauseSelected(val minutes: Int) : PolicyEditorEvent
    data object ResumeClicked : PolicyEditorEvent

    // ── Nimalar yopiladi ──
    data object TargetsClicked : PolicyEditorEvent
    data class Targets(val e: TargetsEditorEvent) : PolicyEditorEvent
    data object TargetsDone : PolicyEditorEvent
    data object TargetsClosed : PolicyEditorEvent

    // ── Qachon ──
    data object AddConditionClicked : PolicyEditorEvent
    data class ConditionPicked(val kind: ConditionKind) : PolicyEditorEvent
    data class ConditionClicked(val kind: ConditionKind) : PolicyEditorEvent
    data class ConditionRemoved(val kind: ConditionKind) : PolicyEditorEvent

    // ── Vaqt sahifasi ──
    data object TimeStartClicked : PolicyEditorEvent
    data object TimeEndClicked : PolicyEditorEvent
    data class TimeSet(val minuteOfDay: Int) : PolicyEditorEvent
    data class TimeIncludeChanged(val include: Boolean) : PolicyEditorEvent

    // ── Limit sahifasi ──
    data class LimitWindowSelected(val window: LimitWindow) : PolicyEditorEvent
    data object LimitTileClicked : PolicyEditorEvent
    data class LimitSet(val minutes: Int) : PolicyEditorEvent
    data class QuickLimitSelected(val minutes: Int) : PolicyEditorEvent

    /** Ochiq sahifa (TIME / LIMIT) kunlari. */
    data object DaysClicked : PolicyEditorEvent
    data class DaysSet(val days: List<Int>) : PolicyEditorEvent
    data object PageSaved : PolicyEditorEvent
    data object PageClosed : PolicyEditorEvent

    // ── Joylashuv ──
    data class Location(val e: LocationPickerEvent) : PolicyEditorEvent
    data object LocationDone : PolicyEditorEvent
    data object LocationClosed : PolicyEditorEvent

    // ── Nom, menyu, o'chirish ──
    data object MenuToggled : PolicyEditorEvent
    data object RenameClicked : PolicyEditorEvent
    data class NameConfirmed(val name: String) : PolicyEditorEvent
    data object DeleteClicked : PolicyEditorEvent
    data object DeleteConfirmed : PolicyEditorEvent
    data object DialogDismissed : PolicyEditorEvent
    data object SheetDismissed : PolicyEditorEvent

    /** "Faqat tanlanganlar ochiq" bepul obunada — paywall. */
    data object AllowLockedClicked : PolicyEditorEvent
    data object PayWallDismissed : PolicyEditorEvent

    data object SaveClicked : PolicyEditorEvent
    data object ErrorDismissed : PolicyEditorEvent
}

sealed interface PolicyEditorEffect {
    data object Saved : PolicyEditorEffect
    data object Deleted : PolicyEditorEffect
}
