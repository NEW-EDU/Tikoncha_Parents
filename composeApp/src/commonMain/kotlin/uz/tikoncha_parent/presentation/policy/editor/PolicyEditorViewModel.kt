package uz.tikoncha_parent.presentation.policy.editor

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
import uz.tikoncha_parent.domain.model.app_error.ErrorCause
import uz.tikoncha_parent.domain.model.app_error.Outcome
import uz.tikoncha_parent.domain.model.app_error.getOrNull
import uz.tikoncha_parent.domain.model.policy.PolicyAction
import uz.tikoncha_parent.domain.model.policy.PolicyDraft
import uz.tikoncha_parent.domain.model.policy.PolicyTargets
import uz.tikoncha_parent.domain.model.policy.TimeCondition
import uz.tikoncha_parent.domain.model.policy.UsageLimit
import uz.tikoncha_parent.domain.use_case.app_usage.GetUsageHistoryUseCase
import uz.tikoncha_parent.domain.use_case.policy.ChildPaidStatusUseCase
import uz.tikoncha_parent.domain.use_case.policy.DeletePolicyUseCase
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
import uz.tikoncha_parent.presentation.policy.targets.TargetsFlavor
import uz.tikoncha_parent.presentation.policy.targets.applyTo
import uz.tikoncha_parent.presentation.policy.targets.reduce
import uz.tikoncha_parent.presentation.policy.targets.targetsEditorFrom
import kotlin.time.Clock
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant

/**
 * "O'zim sozlayman" jadvali: yaratish va tahrirlash (Student `PolicyEditorViewModel` bilan bir xil).
 * Tahrirlashda jadval kesh oqimidan `policyId` bo'yicha bog'lanadi; yo'qolsa (o'chirilgan) ekran yopiladi.
 */
class PolicyEditorViewModel(
    private val observePolicies: ObservePoliciesUseCase,
    private val getApps: GetBlockableChildAppsUseCase,
    private val getUsageHistory: GetUsageHistoryUseCase,
    private val saveDraft: SavePolicyDraftUseCase,
    private val deletePolicy: DeletePolicyUseCase,
    private val togglePolicy: TogglePolicyUseCase,
    private val pausePolicy: PausePolicyUseCase,
    private val getChildLocation: GetChildLocationUseCase,
    private val paidStatus: ChildPaidStatusUseCase,
) : ScreenModel {

    private val _state = MutableStateFlow(PolicyEditorState())
    val state = _state.asStateFlow()

    private val _effect = Channel<PolicyEditorEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    private var started = false
    private var pendingJob: Job? = null
    private var boundOnce = false

    fun onEvent(e: PolicyEditorEvent) {
        if (_state.value.readOnly && e.isEdit()) return
        when (e) {
            is PolicyEditorEvent.Init -> start(e.childId, e.policyId, e.defaultTitle)

            is PolicyEditorEvent.EnabledToggled -> toggleEnabled(e.enabled)
            PolicyEditorEvent.PauseClicked -> _state.update { it.copy(sheet = EditorSheet.PAUSE, menuOpen = false) }
            is PolicyEditorEvent.PauseSelected -> pause(Clock.System.now() + e.minutes.minutes)
            PolicyEditorEvent.ResumeClicked -> pause(null)

            PolicyEditorEvent.TargetsClicked -> _state.update { s ->
                s.draft?.let { s.copy(targets = targetsEditorFrom(it, TargetsFlavor.Custom), menuOpen = false) } ?: s
            }
            is PolicyEditorEvent.Targets -> _state.update { s -> s.targets?.let { s.copy(targets = it.reduce(e.e)) } ?: s }
            PolicyEditorEvent.TargetsDone -> _state.update { s ->
                val editor = s.targets ?: return@update s
                val draft = s.draft ?: return@update s
                s.copy(draft = editor.applyTo(draft, TargetsFlavor.Custom), targets = null)
            }
            PolicyEditorEvent.TargetsClosed -> _state.update { it.copy(targets = null) }
            PolicyEditorEvent.TargetsViewClicked -> _state.update { if (it.canViewTargets) it.copy(viewTargets = true) else it }
            PolicyEditorEvent.TargetsViewClosed -> _state.update { it.copy(viewTargets = false) }

            PolicyEditorEvent.AddConditionClicked -> _state.update { it.copy(sheet = EditorSheet.ADD_CONDITION, menuOpen = false) }
            is PolicyEditorEvent.ConditionPicked -> openCondition(e.kind)
            is PolicyEditorEvent.ConditionClicked -> openCondition(e.kind)
            is PolicyEditorEvent.ConditionRemoved -> edit { d ->
                when (e.kind) {
                    ConditionKind.TIME -> d.copy(conditions = d.conditions.copy(time = null))
                    ConditionKind.LIMIT -> d.copy(limits = d.limits.copy(usage = null))
                    ConditionKind.LOCATION -> d.copy(conditions = d.conditions.copy(location = null))
                }
            }

            PolicyEditorEvent.TimeStartClicked -> _state.update { it.copy(sheet = EditorSheet.TIME_START) }
            PolicyEditorEvent.TimeEndClicked -> _state.update { it.copy(sheet = EditorSheet.TIME_END) }
            is PolicyEditorEvent.TimeSet -> _state.update { s ->
                val rule = s.timeEdit ?: return@update s.copy(sheet = null)
                val updated = when (s.sheet) {
                    EditorSheet.TIME_START -> rule.copy(startMin = e.minuteOfDay)
                    EditorSheet.TIME_END -> rule.copy(endMin = e.minuteOfDay)
                    else -> rule
                }
                s.copy(timeEdit = updated, sheet = null)
            }
            is PolicyEditorEvent.TimeIncludeChanged -> editTime { it.copy(include = e.include) }

            is PolicyEditorEvent.LimitWindowSelected -> editLimit { rule ->
                if (rule.window == e.window) rule
                else rule.copy(
                    window = e.window,
                    minutes = when (e.window) {
                        LimitWindow.HOUR -> if (rule.minutes in 1..59) rule.minutes else 20
                        LimitWindow.DAY -> if (rule.minutes >= 30) rule.minutes else 60
                    },
                )
            }
            PolicyEditorEvent.LimitTileClicked -> _state.update { it.copy(sheet = EditorSheet.LIMIT) }
            is PolicyEditorEvent.LimitSet -> {
                editLimit { it.copy(minutes = e.minutes) }
                _state.update { it.copy(sheet = null) }
            }
            is PolicyEditorEvent.QuickLimitSelected -> editLimit { it.copy(minutes = e.minutes) }

            PolicyEditorEvent.DaysClicked -> _state.update { it.copy(sheet = EditorSheet.DAYS) }
            is PolicyEditorEvent.DaysSet -> {
                val days = e.days.mapNotNull { n -> WeekDay.entries.firstOrNull { it.num == n } }.toSet()
                if (days.isNotEmpty()) {
                    when (_state.value.page) {
                        EditorPage.TIME -> editTime { it.copy(days = days) }
                        EditorPage.LIMIT -> editLimit { it.copy(days = days) }
                        EditorPage.MAIN -> Unit
                    }
                }
                _state.update { it.copy(sheet = null) }
            }
            PolicyEditorEvent.PageSaved -> _state.update { s ->
                val draft = s.draft ?: return@update s
                when (s.page) {
                    EditorPage.TIME -> s.copy(
                        draft = draft.copy(conditions = draft.conditions.copy(time = s.timeEdit ?: draft.conditions.time)),
                        page = EditorPage.MAIN,
                        timeEdit = null,
                    )
                    EditorPage.LIMIT -> s.copy(
                        draft = draft.copy(limits = draft.limits.copy(usage = s.limitEdit ?: draft.limits.usage)),
                        page = EditorPage.MAIN,
                        limitEdit = null,
                    )
                    EditorPage.MAIN -> s
                }
            }
            PolicyEditorEvent.PageClosed -> _state.update { it.copy(page = EditorPage.MAIN, timeEdit = null, limitEdit = null, sheet = null) }

            is PolicyEditorEvent.Location -> _state.update { s -> s.location?.let { s.copy(location = it.reduceLocation(e.e)) } ?: s }
            PolicyEditorEvent.LocationDone -> _state.update { s ->
                val rule = s.location?.toRule() ?: return@update s.copy(location = null)
                s.copy(location = null, draft = s.draft?.let { it.copy(conditions = it.conditions.copy(location = rule)) })
            }
            PolicyEditorEvent.LocationClosed -> _state.update { it.copy(location = null) }
            PolicyEditorEvent.LocationViewClicked -> _state.update { s ->
                s.draft?.conditions?.location?.let { s.copy(location = LocationPickerState.from(it, s.child).copy(readOnly = true)) } ?: s
            }

            PolicyEditorEvent.MenuToggled -> _state.update { it.copy(menuOpen = !it.menuOpen) }
            PolicyEditorEvent.RenameClicked -> _state.update { it.copy(dialog = EditorDialog.NAME, menuOpen = false) }
            is PolicyEditorEvent.NameConfirmed -> {
                val name = e.name.trim()
                if (name.isNotEmpty()) edit { it.copy(name = name) }
                _state.update { it.copy(dialog = null) }
            }
            PolicyEditorEvent.DeleteClicked -> _state.update { it.copy(dialog = EditorDialog.DELETE, menuOpen = false) }
            PolicyEditorEvent.DeleteConfirmed -> delete()
            PolicyEditorEvent.DialogDismissed -> _state.update { it.copy(dialog = null) }
            PolicyEditorEvent.SheetDismissed -> _state.update { it.copy(sheet = null) }

            PolicyEditorEvent.AllowLockedClicked -> _state.update { it.copy(payWall = true) }
            PolicyEditorEvent.PayWallDismissed -> _state.update { it.copy(payWall = false) }

            PolicyEditorEvent.SaveClicked -> save()
            PolicyEditorEvent.ErrorDismissed -> _state.update { it.copy(error = null) }
        }
    }

    /** Faqat ko'rish rejimida o'tkazilmaydigan hodisalar. */
    private fun PolicyEditorEvent.isEdit(): Boolean = when (this) {
        is PolicyEditorEvent.Init, PolicyEditorEvent.ErrorDismissed, PolicyEditorEvent.SheetDismissed,
        PolicyEditorEvent.DialogDismissed, PolicyEditorEvent.MenuToggled, PolicyEditorEvent.PayWallDismissed,
        PolicyEditorEvent.TargetsViewClicked, PolicyEditorEvent.TargetsViewClosed,
        PolicyEditorEvent.LocationViewClicked, PolicyEditorEvent.LocationClosed, is PolicyEditorEvent.Location -> false
        else -> true
    }

    // ═══════════════════════════════════════════
    //  Yuklash
    // ═══════════════════════════════════════════

    private fun start(childId: String, policyId: String?, defaultTitle: String) {
        if (started) return
        started = true
        _state.update { it.copy(childId = childId, now = Clock.System.now(), paid = paidStatus(childId)) }

        if (policyId == null) {
            val draft = PolicyDraft(name = defaultTitle, action = PolicyAction.DENY, targets = PolicyTargets())
            _state.update { it.copy(draft = draft) }
        } else {
            screenModelScope.launch {
                observePolicies(childId).collect { list ->
                    val policy = list.firstOrNull { it.id == policyId }
                    if (policy == null) {
                        // Serverdan o'chirilgan — bog'langan bo'lsak yopamiz
                        if (boundOnce) _effect.trySend(PolicyEditorEffect.Deleted)
                        return@collect
                    }
                    boundOnce = true
                    val serverDraft = policy.toDraft()
                    _state.update { s ->
                        s.copy(
                            policyId = policyId,
                            readOnly = !policy.canEdit(AppSettings.userId),
                            scope = policy.scope,
                            isEnabled = policy.isActive,
                            pendingEnabled = s.pendingEnabled?.takeIf { it != policy.isActive },
                            pausedUntil = policy.pausedUntil,
                            now = Clock.System.now(),
                            saved = serverDraft,
                            draft = if (s.hasChanges) s.draft else serverDraft,
                        )
                    }
                }
            }
        }
        screenModelScope.launch {
            getApps.all(childId).getOrNull()?.let { all ->
                _state.update { it.copy(allChildApps = all, childApps = getApps.blockable(all)) }
            }
        }
        screenModelScope.launch {
            val pin = getChildLocation(childId)?.toPin()
            _state.update { s -> s.copy(child = pin, location = s.location?.let { it.copy(child = pin) }) }
        }
        screenModelScope.launch {
            val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
            val history = getUsageHistory(childId, today, days = USAGE_DAYS).getOrNull() ?: return@launch
            val usage = history.appTotals(today.minus(USAGE_DAYS - 1, DateTimeUnit.DAY), today)
                .associate { it.packageName to it.millis / 60_000L }
            _state.update { it.copy(appUsage = usage) }
        }
        screenModelScope.launch {
            val paid = paidStatus.refresh(childId)
            _state.update { it.copy(paid = paid ?: it.paid) }
        }
    }

    // ═══════════════════════════════════════════
    //  Qoralama
    // ═══════════════════════════════════════════

    private fun edit(f: (PolicyDraft) -> PolicyDraft) = _state.update { s -> s.draft?.let { s.copy(draft = f(it)) } ?: s }

    private fun editTime(f: (TimeCondition) -> TimeCondition) =
        _state.update { s -> s.timeEdit?.let { s.copy(timeEdit = f(it)) } ?: s }

    private fun editLimit(f: (UsageLimit) -> UsageLimit) =
        _state.update { s -> s.limitEdit?.let { s.copy(limitEdit = f(it)) } ?: s }

    /** Shart sahifasi: mavjud qoida nusxasi yoki standart qiymat. */
    private fun openCondition(kind: ConditionKind) = _state.update { s ->
        val draft = s.draft ?: return@update s
        when (kind) {
            ConditionKind.TIME -> s.copy(page = EditorPage.TIME, timeEdit = draft.conditions.time ?: PolicyEditorState.DEFAULT_TIME, sheet = null)
            ConditionKind.LIMIT -> s.copy(page = EditorPage.LIMIT, limitEdit = draft.limits.usage ?: PolicyEditorState.DEFAULT_LIMIT, sheet = null)
            ConditionKind.LOCATION -> s.copy(location = LocationPickerState.from(draft.conditions.location, s.child), sheet = null)
        }
    }

    // ═══════════════════════════════════════════
    //  Server
    // ═══════════════════════════════════════════

    private fun toggleEnabled(enabled: Boolean) {
        val id = _state.value.policyId ?: return
        if (_state.value.pendingEnabled != null) return
        _state.update { it.copy(pendingEnabled = enabled) }
        pendingJob?.cancel()
        pendingJob = screenModelScope.launch {
            when (val r = togglePolicy(id, enabled)) {
                is Outcome.Failure -> _state.update { it.withFailure(r).copy(pendingEnabled = null) }
                is Outcome.Success -> {
                    _state.update { if (it.isEnabled == enabled) it.copy(pendingEnabled = null) else it }
                    delay(PENDING_TIMEOUT_MS)      // kesh kelmay qolsa spinner osilib qolmasin
                    _state.update { it.copy(pendingEnabled = null) }
                }
            }
        }
    }

    private fun pause(until: Instant?) {
        val id = _state.value.policyId ?: return
        _state.update { it.copy(sheet = null) }
        if (_state.value.busy) return
        screenModelScope.launch {
            _state.update { it.copy(busy = true) }
            val r = pausePolicy(id, until)
            _state.update { s -> (if (r is Outcome.Failure) s.withFailure(r) else s).copy(busy = false, now = Clock.System.now()) }
        }
    }

    private fun save() {
        val s = _state.value
        val draft = s.draft ?: return
        if (!s.canSave) return
        screenModelScope.launch {
            _state.update { it.copy(saving = true) }
            when (val r = saveDraft(s.childId, s.policyId, draft, s.saved)) {
                is Outcome.Success -> {
                    _state.update { it.copy(saving = false, saved = draft) }
                    _effect.trySend(PolicyEditorEffect.Saved)
                }
                is Outcome.Failure -> _state.update { it.withFailure(r).copy(saving = false) }
            }
        }
    }

    private fun delete() {
        val id = _state.value.policyId ?: return
        if (_state.value.deleting) return
        screenModelScope.launch {
            _state.update { it.copy(deleting = true, dialog = null) }
            when (val r = deletePolicy(id)) {
                is Outcome.Success -> _effect.trySend(PolicyEditorEffect.Deleted)
                is Outcome.Failure -> _state.update { it.withFailure(r).copy(deleting = false) }
            }
        }
    }

    /** Pullik rad — paywall, boshqa xato — dialog. */
    private fun PolicyEditorState.withFailure(f: Outcome.Failure): PolicyEditorState =
        if (f.cause is ErrorCause.PremiumRequired) copy(payWall = true) else copy(error = f)

    private companion object {
        const val PENDING_TIMEOUT_MS = 3_000L
        const val USAGE_DAYS = 7
    }
}
