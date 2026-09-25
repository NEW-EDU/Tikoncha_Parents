package uz.tikoncha_parent.presentation.policy.preset

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime
import uz.tikoncha_parent.data.local.AppSettings
import uz.tikoncha_parent.domain.model.LimitWindow
import uz.tikoncha_parent.domain.model.WeekDay
import uz.tikoncha_parent.domain.model.app_error.Outcome
import uz.tikoncha_parent.domain.model.app_error.getOrNull
import uz.tikoncha_parent.domain.model.policy.PolicyDraft
import uz.tikoncha_parent.domain.model.policy.TimeCondition
import uz.tikoncha_parent.domain.model.policy.UsageLimit
import uz.tikoncha_parent.domain.policy.PresetDefaults
import uz.tikoncha_parent.domain.use_case.app_usage.GetUsageHistoryUseCase
import uz.tikoncha_parent.domain.use_case.policy.GetBlockableChildAppsUseCase
import uz.tikoncha_parent.domain.use_case.policy.GetChildLocationUseCase
import uz.tikoncha_parent.domain.use_case.policy.ObservePoliciesUseCase
import uz.tikoncha_parent.domain.use_case.policy.PausePolicyUseCase
import uz.tikoncha_parent.domain.use_case.policy.SavePolicyDraftUseCase
import uz.tikoncha_parent.domain.use_case.policy.TogglePolicyUseCase
import uz.tikoncha_parent.domain.use_case.policy.toDraft
import uz.tikoncha_parent.presentation.policy.location.LocationPickerState
import uz.tikoncha_parent.presentation.policy.location.reduce as reduceLocation
import uz.tikoncha_parent.presentation.policy.location.toPin
import uz.tikoncha_parent.presentation.policy.model.PresetKind
import uz.tikoncha_parent.presentation.policy.targets.TargetsFlavor
import uz.tikoncha_parent.presentation.policy.targets.appCategory
import uz.tikoncha_parent.presentation.policy.targets.applyTo
import uz.tikoncha_parent.presentation.policy.targets.reduce
import uz.tikoncha_parent.presentation.policy.targets.targetsEditorFrom
import kotlin.time.Clock
import kotlin.time.Duration.Companion.minutes

/**
 * Tayyor jadval ichki ekrani (Student `PresetPolicyViewModel` bilan bir xil).
 * Holat = keshdagi jadval (mening preset'im) + lokal `draft`. Server nusxasi o'zgarsa
 * `saved` yangilanadi; lokal tahrir bo'lmasa `draft` ham. Switch va pauza darhol serverga
 * ketadi; qolgan sozlamalar "Saqlash" bilan.
 */
class PresetPolicyViewModel(
    private val observePolicies: ObservePoliciesUseCase,
    private val getApps: GetBlockableChildAppsUseCase,
    private val getUsageHistory: GetUsageHistoryUseCase,
    private val saveDraft: SavePolicyDraftUseCase,
    private val togglePolicy: TogglePolicyUseCase,
    private val pausePolicy: PausePolicyUseCase,
    private val getChildLocation: GetChildLocationUseCase,
) : ScreenModel {

    private val _state = MutableStateFlow(PresetPolicyState())
    val state = _state.asStateFlow()

    private val _effect = Channel<PresetPolicyEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    private var started = false
    private var pendingJob: Job? = null
    private var title = ""

    fun onEvent(e: PresetPolicyEvent) {
        when (e) {
            is PresetPolicyEvent.Init -> start(e.childId, e.kind, e.title)

            is PresetPolicyEvent.EnabledToggled -> toggleEnabled(e.enabled)
            PresetPolicyEvent.PauseClicked -> openSheet(PresetSheet.PAUSE)
            is PresetPolicyEvent.PauseSelected -> pause(Clock.System.now() + e.minutes.minutes)
            PresetPolicyEvent.ResumeClicked -> pause(null)

            PresetPolicyEvent.StartClicked -> openSheet(PresetSheet.TIME_START)
            PresetPolicyEvent.EndClicked -> openSheet(PresetSheet.TIME_END)
            is PresetPolicyEvent.TimeSet -> setTime(e.minuteOfDay)
            is PresetPolicyEvent.QuickTimeSelected -> editTime { it.copy(startMin = e.startMin, endMin = e.endMin) }

            is PresetPolicyEvent.LimitWindowSelected -> setLimitWindow(e.window)
            PresetPolicyEvent.LimitClicked -> openSheet(PresetSheet.LIMIT)
            is PresetPolicyEvent.LimitSet -> closeSheet { editLimit { it.copy(minutes = e.minutes) } }
            is PresetPolicyEvent.QuickLimitSelected -> editLimit { it.copy(minutes = e.minutes) }

            PresetPolicyEvent.DaysClicked -> openSheet(PresetSheet.DAYS)
            is PresetPolicyEvent.DaysSet -> closeSheet { setDays(e.days) }

            PresetPolicyEvent.AddExceptionClicked -> openSheet(PresetSheet.APPS)
            is PresetPolicyEvent.ExceptionsAdded -> closeSheet { editExceptions { it + e.packages } }
            is PresetPolicyEvent.ExceptionRemoved -> editExceptions { it - e.packageName }

            PresetPolicyEvent.TargetsClicked -> _state.update { s ->
                s.draft?.let { s.copy(targets = targetsEditorFrom(it, TargetsFlavor.Preset(s.kind))) } ?: s
            }
            is PresetPolicyEvent.Targets -> _state.update { s -> s.targets?.let { s.copy(targets = it.reduce(e.e)) } ?: s }
            PresetPolicyEvent.TargetsDone -> _state.update { s ->
                val editor = s.targets ?: return@update s
                val draft = s.draft ?: return@update s
                s.copy(draft = editor.applyTo(draft, TargetsFlavor.Preset(s.kind)), targets = null)
            }
            PresetPolicyEvent.TargetsClosed -> _state.update { it.copy(targets = null) }

            is PresetPolicyEvent.LocationToggled ->
                if (e.enabled) openLocation() else edit { it.copy(conditions = it.conditions.copy(location = null)) }
            PresetPolicyEvent.LocationClicked -> openLocation()
            is PresetPolicyEvent.Location -> _state.update { s -> s.location?.let { s.copy(location = it.reduceLocation(e.e)) } ?: s }
            PresetPolicyEvent.LocationDone -> _state.update { s ->
                val rule = s.location?.toRule() ?: return@update s.copy(location = null)
                s.copy(location = null, draft = s.draft?.let { it.copy(conditions = it.conditions.copy(location = rule)) })
            }
            PresetPolicyEvent.LocationClosed -> _state.update { it.copy(location = null) }

            PresetPolicyEvent.ResetClicked -> reset()
            PresetPolicyEvent.SheetDismissed -> _state.update { it.copy(sheet = null) }
            PresetPolicyEvent.SaveClicked -> save()
            PresetPolicyEvent.ErrorDismissed -> _state.update { it.copy(error = null) }
        }
    }

    // ═══ Yuklash ═══════════════════════════════════════════════

    private fun start(childId: String, kind: PresetKind, title: String) {
        if (started) return
        started = true
        this.title = title
        _state.update { it.copy(childId = childId, kind = kind, now = Clock.System.now()) }

        screenModelScope.launch {
            observePolicies(childId).collect { list ->
                val policy = list.firstOrNull { it.isStandard && it.preset == kind.preset && it.isMine(AppSettings.userId) }
                val serverDraft = policy?.toDraft() ?: PresetDefaults.of(kind.preset, title)!!
                _state.update { s ->
                    s.copy(
                        policyId = policy?.id,
                        isEnabled = policy?.isActive ?: false,
                        pendingEnabled = s.pendingEnabled?.takeIf { it != (policy?.isActive ?: false) },
                        pausedUntil = policy?.pausedUntil,
                        now = Clock.System.now(),
                        saved = serverDraft,
                        // lokal tahrir bo'lsa uni yo'qotmaymiz; jadval endi yaratilgan bo'lsa server nusxasi
                        draft = if (s.hasChanges && s.policyId == policy?.id) s.draft else serverDraft,
                    )
                }
                refreshUsage()
            }
        }
        screenModelScope.launch {
            val pin = getChildLocation(childId)?.toPin()
            _state.update { s -> s.copy(child = pin, location = s.location?.let { it.copy(child = pin) }) }
        }
        screenModelScope.launch {
            val local = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
            val history = getUsageHistory(childId, local, days = USAGE_DAYS).getOrNull() ?: return@launch
            val usage = history.appTotals(local.minus(USAGE_DAYS - 1, DateTimeUnit.DAY), local)
                .associate { it.packageName to it.millis / 60_000L }
            _state.update { it.copy(appUsage = usage) }
        }
        screenModelScope.launch {
            getApps(childId).getOrNull()?.let { apps ->
                _state.update { it.copy(childApps = apps) }
                refreshUsage()          // kategoriyalar endi ma'lum
            }
        }
    }

    /** Vaqt limiti: joriy oynada qancha ishlatilgani — server statistikasidan. */
    private fun refreshUsage() {
        val s = _state.value
        if (s.kind != PresetKind.LIMIT || s.policyId == null) return
        val draft = s.draft ?: return
        val hourly = draft.limits.usage?.window == LimitWindow.HOUR
        screenModelScope.launch {
            val local = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
            val history = getUsageHistory(s.childId, local.date, days = 1).getOrNull() ?: return@launch
            val categoryOf = _state.value.childApps.associate { it.packageName to it.appCategory.id }
            val used = history.usedMinutes(draft.targets, categoryOf, local.date, local.hour, hourly)
            _state.update { it.copy(usedMinutes = used) }
        }
    }

    // ═══ Darhol serverga ketadiganlar ══════════════════════════

    /** Bazada yo'q jadval yoqilsa — joriy `draft` bilan yaratiladi (tahrirlar yo'qolmaydi). */
    private fun toggleEnabled(enabled: Boolean) {
        val s = _state.value
        val draft = s.draft ?: return
        if (s.pendingEnabled != null) return
        _state.update { it.copy(pendingEnabled = enabled) }
        pendingJob?.cancel()
        pendingJob = screenModelScope.launch {
            val r = when {
                s.policyId != null -> togglePolicy(s.policyId, enabled)
                enabled -> saveDraft(s.childId, null, draft.copy(isActive = true), null)
                else -> Outcome.Success(Unit)
            }
            finishPending(enabled, r)
        }
    }

    /** Xato — switch avvalgi holatda qoladi; muvaffaqiyat — kesh yangi holatni olib keladi. */
    private suspend fun finishPending(enabled: Boolean, result: Outcome<*>) {
        if (result is Outcome.Failure) {
            _state.update { it.copy(pendingEnabled = null, error = result) }
            return
        }
        _state.update { if (it.isEnabled == enabled) it.copy(pendingEnabled = null) else it }
        delay(PENDING_TIMEOUT_MS)        // kesh kelmay qolsa spinner osilib qolmasin
        _state.update { it.copy(pendingEnabled = null) }
    }

    private fun pause(until: kotlin.time.Instant?) {
        val id = _state.value.policyId ?: return
        _state.update { it.copy(sheet = null) }
        if (_state.value.busy) return
        screenModelScope.launch {
            _state.update { it.copy(busy = true) }
            val r = pausePolicy(id, until)
            _state.update { it.copy(busy = false, error = r as? Outcome.Failure ?: it.error, now = Clock.System.now()) }
        }
    }

    // ═══ Qoralama tahriri ══════════════════════════════════════

    private fun edit(f: (PolicyDraft) -> PolicyDraft) = _state.update { s -> s.draft?.let { s.copy(draft = f(it)) } ?: s }

    private fun editTime(f: (TimeCondition) -> TimeCondition) = edit { d ->
        val rule = d.conditions.time ?: TimeCondition(days = PresetDefaults.ALL_DAYS, startMin = 22 * 60, endMin = 7 * 60)
        d.copy(conditions = d.conditions.copy(time = f(rule)))
    }

    private fun editLimit(f: (UsageLimit) -> UsageLimit) = edit { d ->
        val rule = d.limits.usage ?: UsageLimit(days = PresetDefaults.ALL_DAYS, window = LimitWindow.DAY, minutes = 180)
        d.copy(limits = d.limits.copy(usage = f(rule)))
    }

    /** Uyqu / Limit / Dars — istisnolar `excludePackages` da. */
    private fun editExceptions(f: (List<String>) -> List<String>) = edit { d ->
        d.copy(targets = d.targets.copy(excludePackages = f(d.targets.excludePackages).distinct()))
    }

    private fun setTime(minuteOfDay: Int) {
        val which = _state.value.sheet
        closeSheet {
            when (which) {
                PresetSheet.TIME_START -> editTime { it.copy(startMin = minuteOfDay) }
                PresetSheet.TIME_END -> editTime { it.copy(endMin = minuteOfDay) }
                else -> Unit
            }
        }
    }

    /** Kunlik ↔ soatlik: daqiqa o'z oynasiga sig'maydigan bo'lsa standartga tushadi. */
    private fun setLimitWindow(window: LimitWindow) {
        editLimit { rule ->
            if (rule.window == window) rule
            else rule.copy(
                window = window,
                minutes = when (window) {
                    LimitWindow.HOUR -> if (rule.minutes in 1..59) rule.minutes else 20
                    LimitWindow.DAY -> if (rule.minutes >= 30) rule.minutes else 180
                },
            )
        }
        refreshUsage()
    }

    private fun setDays(days: List<Int>) {
        if (days.isEmpty()) return
        val set = days.mapNotNull { n -> WeekDay.entries.firstOrNull { it.num == n } }.toSet()
        when (_state.value.kind) {
            PresetKind.LIMIT -> editLimit { it.copy(days = set) }
            else -> editTime { it.copy(days = set) }
        }
    }

    private fun reset() {
        val s = _state.value
        val fresh = PresetDefaults.of(s.kind.preset, s.saved?.name ?: title) ?: return
        _state.update { it.copy(draft = fresh) }
    }

    private fun openLocation() = _state.update { s ->
        s.copy(location = LocationPickerState.from(s.draft?.conditions?.location, s.child))
    }

    private fun openSheet(sheet: PresetSheet) = _state.update { it.copy(sheet = sheet) }

    private inline fun closeSheet(block: () -> Unit) {
        block()
        _state.update { it.copy(sheet = null) }
    }

    // ═══ Saqlash ══════════════════════════════════════════════

    private fun save() {
        val s = _state.value
        val draft = s.draft ?: return
        if (s.saving || !s.hasChanges) return
        screenModelScope.launch {
            _state.update { it.copy(saving = true) }
            when (val r = saveDraft(s.childId, s.policyId, draft, s.saved)) {
                is Outcome.Success -> {
                    _state.update { it.copy(saving = false, saved = draft) }
                    _effect.trySend(PresetPolicyEffect.Saved)
                }
                is Outcome.Failure -> _state.update { it.copy(saving = false, error = r) }
            }
        }
    }

    private companion object {
        const val PENDING_TIMEOUT_MS = 3_000L
        const val USAGE_DAYS = 7
    }
}
