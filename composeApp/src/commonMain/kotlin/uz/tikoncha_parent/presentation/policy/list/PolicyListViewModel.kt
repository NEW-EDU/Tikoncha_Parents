package uz.tikoncha_parent.presentation.policy.list

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.toLocalDateTime
import uz.tikoncha_parent.data.local.AppSettings
import uz.tikoncha_parent.domain.model.UserInfo
import uz.tikoncha_parent.domain.model.app_error.ErrorCause
import uz.tikoncha_parent.domain.model.app_error.Outcome
import uz.tikoncha_parent.domain.model.app_error.getOrNull
import uz.tikoncha_parent.domain.model.policy.Policy
import uz.tikoncha_parent.domain.model.policy.ProtectionPackStatus
import uz.tikoncha_parent.domain.model.policy.QuickBlockSnapshot
import uz.tikoncha_parent.domain.repository.ChildRepository
import uz.tikoncha_parent.domain.use_case.policy.ChildPaidStatusUseCase
import uz.tikoncha_parent.domain.use_case.policy.GetProtectionPackStatusesUseCase
import uz.tikoncha_parent.domain.use_case.policy.ObservePoliciesUseCase
import uz.tikoncha_parent.domain.use_case.policy.ObserveQuickBlockSnapshotUseCase
import uz.tikoncha_parent.domain.use_case.policy.RefreshPoliciesUseCase
import uz.tikoncha_parent.domain.use_case.policy.RefreshQuickBlocksUseCase
import uz.tikoncha_parent.domain.use_case.policy.ToggleContentProtectionUseCase
import uz.tikoncha_parent.domain.use_case.policy.TogglePolicyUseCase
import uz.tikoncha_parent.domain.use_case.policy.TogglePresetUseCase
import uz.tikoncha_parent.domain.use_case.policy.UpdateOwnQuickBlockUseCase
import uz.tikoncha_parent.presentation.policy.model.PayWallReason
import uz.tikoncha_parent.presentation.policy.model.PresetKind
import uz.tikoncha_parent.presentation.profile.language.LanguagePrefs
import kotlin.time.Clock

/**
 * Jadvallar ro'yxati (Student `PolicyListViewModel` bilan bir xil tuzilish).
 * Holat = jadvallar keshi × tezkor bloklar × tarif × joy × har daqiqalik tick → `reduce`.
 * Yozuvlar `control` orqali: nishon `busy` ga tushadi, xato `error` ga; muvaffaqiyatda
 * holat keshdan o'zi yangilanadi (repozitoriylar javobni keshga yozadi).
 */
class PolicyListViewModel(
    private val childRepository: ChildRepository,
    private val observePolicies: ObservePoliciesUseCase,
    private val refreshPolicies: RefreshPoliciesUseCase,
    private val observeQuickSnapshot: ObserveQuickBlockSnapshotUseCase,
    private val refreshQuickBlocks: RefreshQuickBlocksUseCase,
    private val getPackStatuses: GetProtectionPackStatusesUseCase,
    private val togglePreset: TogglePresetUseCase,
    private val togglePolicy: TogglePolicyUseCase,
    private val toggleProtection: ToggleContentProtectionUseCase,
    private val updateOwnQuick: UpdateOwnQuickBlockUseCase,
    private val childPaidStatus: ChildPaidStatusUseCase,
) : ScreenModel {

    private val _state = MutableStateFlow(PolicyListState())
    val state = _state.asStateFlow()

    private val _effect = Channel<PolicyListEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    private val paid = MutableStateFlow<Boolean?>(null)
    private val location = MutableStateFlow<Pair<Double, Double>?>(null)

    private var dataJob: Job? = null
    private var policies: List<Policy> = emptyList()
    private var quick: QuickBlockSnapshot = QuickBlockSnapshot()
    private var packs: List<ProtectionPackStatus> = emptyList()

    private val myUserId: String get() = AppSettings.userId
    private val childId: String? get() = _state.value.selectedChild?.userId

    /** "Hozir amalda" har daqiqada qayta hisoblanadi. */
    private val ticker = flow {
        while (true) {
            emit(Unit)
            delay(TICK_MS)
        }
    }

    fun onEvent(e: PolicyListEvent) {
        when (e) {
            PolicyListEvent.Load -> load()
            PolicyListEvent.Refresh -> childId?.let { refresh(it, pull = true) }
            PolicyListEvent.Resumed -> childId?.let { refreshPaid(it) }
            is PolicyListEvent.ChildSelected -> select(e.child)
            is PolicyListEvent.TabSelected -> _state.update { it.copy(tab = e.tab) }

            is PolicyListEvent.PresetToggled -> togglePresetFor(e.kind, e.enabled, e.title)
            is PolicyListEvent.PresetClicked -> childId?.let { _effect.trySend(PolicyListEffect.OpenPreset(it, e.kind)) }

            is PolicyListEvent.QuickBlockToggled -> toggleQuick(e.enabled)
            PolicyListEvent.QuickBlockClicked -> childId?.let { _effect.trySend(PolicyListEffect.OpenQuickBlock(it)) }

            is PolicyListEvent.ProtectionToggled -> toggleProtectionAll(e.enabled)
            PolicyListEvent.ProtectionClicked -> childId?.let { _effect.trySend(PolicyListEffect.OpenProtection(it)) }

            is PolicyListEvent.PolicyToggled -> control(e.policyId, PayWallReason.POLICY_COUNT) { togglePolicy(e.policyId, e.enabled) }
            is PolicyListEvent.PolicyClicked -> childId?.let { _effect.trySend(PolicyListEffect.OpenPolicy(it, e.policyId)) }

            PolicyListEvent.CreateClicked -> childId?.let { _effect.trySend(PolicyListEffect.OpenCreate(it)) }
            PolicyListEvent.PayWallDismissed -> _state.update { it.copy(payWall = null) }
            PolicyListEvent.ErrorDismissed -> _state.update { it.copy(error = null) }
        }
    }

    // ═══ Farzand ═══════════════════════════════════════════════

    private fun load() {
        val selected = AppSettings.selectedChild
        _state.update { it.copy(children = AppSettings.children, selectedChild = selected) }
        if (selected != null && dataJob?.isActive != true) observe(selected.userId) else selected?.let { refresh(it.userId, pull = false) }
        screenModelScope.launch {
            val list = childRepository.children().getOrNull() ?: return@launch
            _state.update { it.copy(children = list) }
            if (_state.value.selectedChild == null) list.firstOrNull()?.let { select(it) }
        }
    }

    private fun select(child: UserInfo) {
        if (child.userId == childId && dataJob?.isActive == true) return
        AppSettings.selectedChildId = child.userId
        AppSettings.selectedChild = child
        _state.update { PolicyListState(children = it.children, selectedChild = child, tab = it.tab) }
        observe(child.userId)
    }

    private fun observe(childId: String) {
        dataJob?.cancel()
        paid.value = childPaidStatus(childId)
        location.value = null
        dataJob = screenModelScope.launch {
            combine(observePolicies(childId), observeQuickSnapshot(childId, myUserId), paid, location, ticker) { p, q, paidNow, loc, _ ->
                policies = p
                quick = q.copy(paid = paidNow)
                // Katalog repozitoriyda keshlanadi, jadvallar ham kesh — arzon
                packs = getPackStatuses(childId, myUserId).getOrNull() ?: packs
                val now = Clock.System.now()
                val local = now.toLocalDateTime(TimeZone.currentSystemDefault())
                PolicyListInput(
                    policies = p,
                    quick = quick,
                    packs = packs,
                    myUserId = myUserId,
                    paid = paidNow,
                    weekDay = local.dayOfWeek.isoDayNumber,
                    minuteOfDay = local.hour * 60 + local.minute,
                    now = now,
                    lastLat = loc?.first,
                    lastLng = loc?.second,
                )
            }.collect { input -> _state.update { input.reduce(it) } }
        }
        refresh(childId, pull = false)
    }

    /** Ekran ochilganda jim; pastga tortilganda xato ko'rsatiladi. */
    private fun refresh(childId: String, pull: Boolean) {
        if (pull) _state.update { it.copy(isRefreshing = true) }
        screenModelScope.launch {
            val res = refreshPolicies(childId, force = true)
            _state.update {
                it.copy(isRefreshing = false, error = if (pull && res is Outcome.Failure) res else it.error)
            }
        }
        screenModelScope.launch { refreshQuickBlocks(childId) }
        screenModelScope.launch {
            val loc = childRepository.childrenLocation().getOrNull()?.firstOrNull { it.childId == childId }
            val lat = loc?.latitude
            val lng = loc?.longitude
            if (lat != null && lng != null) location.value = lat to lng
        }
        refreshPaid(childId)
    }

    private fun refreshPaid(childId: String) {
        screenModelScope.launch { paid.value = childPaidStatus.refresh(childId) }
    }

    // ═══ Yozuvlar ═══════════════════════════════════════════════

    private fun togglePresetFor(kind: PresetKind, enabled: Boolean, title: String) {
        val child = childId ?: return
        val existing = policies.firstOrNull {
            it.isStandard && it.preset == kind.preset && it.isMine(myUserId)
        }
        control(presetKey(kind), PayWallReason.POLICY_COUNT) { togglePreset(child, kind.preset, existing, enabled, title) }
    }

    /**
     * Tezkor blok pullik. Ro'yxat bo'sh bo'lsa yoqadigan narsa yo'q — ilova tanlash
     * uchun ekran ochiladi. Aks holda butun ro'yxat bitta `is_active` bilan.
     */
    private fun toggleQuick(enabled: Boolean) {
        val child = childId ?: return
        if (enabled && paid.value == false) {
            _state.update { it.copy(payWall = PayWallReason.QUICK_BLOCK) }
            return
        }
        val own = quick.mine
        if (own == null || own.targets.packages.isEmpty()) {
            if (enabled) _effect.trySend(PolicyListEffect.OpenQuickBlock(child))
            return
        }
        control(KEY_QUICK, PayWallReason.QUICK_BLOCK) { updateOwnQuick.setEnabled(child, own, enabled) }
    }

    /** Yoqish: men yoqmagan paketlarning hammasi. O'chirish: faqat o'zim yoqqanlarim. */
    private fun toggleProtectionAll(enabled: Boolean) {
        val child = childId ?: return
        if (enabled && paid.value == false) {
            _state.update { it.copy(payWall = PayWallReason.PROTECTION) }
            return
        }
        val lang = LanguagePrefs.loadOrDefault().languageCode
        control(KEY_PROTECTION, PayWallReason.PROTECTION) {
            toggleProtection(child, myUserId, packs, enabled) { it.pack.title(lang) }
        }
    }

    /** Bitta nishon uchun bitta so'rov: `busy` ga qo'yadi; pullik rad — paywall, boshqa xato — dialog. */
    private fun control(key: String, reason: PayWallReason, action: suspend () -> Outcome<*>) {
        if (_state.value.isBusy(key)) return
        screenModelScope.launch {
            _state.update { it.copy(busy = it.busy + key) }
            val result = action()
            _state.update {
                val failure = result as? Outcome.Failure
                when {
                    failure == null -> it.copy(busy = it.busy - key)
                    failure.cause is ErrorCause.PremiumRequired -> it.copy(busy = it.busy - key, payWall = reason)
                    else -> it.copy(busy = it.busy - key, error = failure)
                }
            }
        }
    }

    companion object {
        const val KEY_PROTECTION = "protection"
        const val KEY_QUICK = "quick"
        fun presetKey(kind: PresetKind) = "preset:${kind.name}"
        private const val TICK_MS = 60_000L
    }
}
