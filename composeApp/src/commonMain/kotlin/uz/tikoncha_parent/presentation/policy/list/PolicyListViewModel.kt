package uz.tikoncha_parent.presentation.policy.list

import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.MutableSharedFlow
import uz.tikoncha_parent.presentation.policy.model.TakeOverUi
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
import uz.tikoncha_parent.presentation.policy.model.PolicyTab
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

    // Birinchi kadrdanoq saqlangan farzandlar va oxirgi tablar bilan — "Farzand qo'shish" / 2 ta tab "lip" etmasin
    private val _state = MutableStateFlow(
        AppSettings.selectedChild.let { selected ->
            PolicyListState(
                children = AppSettings.children,
                selectedChild = selected,
                tabs = selected?.userId?.let(::rememberedTabs) ?: listOf(PolicyTab.TEMPLATES, PolicyTab.MINE),
            )
        }
    )
    val state = _state.asStateFlow()

    /** Server javobi kelguncha tablar qisqarmaydi (kesh bo'sh/eski bo'lishi mumkin); keyin — server bo'yicha. */
    private var serverFresh = false
    private var computedTabs: List<PolicyTab> = emptyList()
    private var firstLoad = true

    private val _effect = Channel<PolicyListEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    private val paid = MutableStateFlow<Boolean?>(null)
    private val location = MutableStateFlow<Pair<Double, Double>?>(null)

    private var dataJob: Job? = null
    private var policies: List<Policy> = emptyList()
    private var quick: QuickBlockSnapshot = QuickBlockSnapshot()
    private var packs: List<ProtectionPackStatus> = emptyList()

    /** "Shablonlar" tabidan yaratish boshlanganda "Meniki"dagi jadvallar — yangisi paydo bo'lsa "Meniki" tabiga o'tiladi. */
    private var mineBeforeCreate: Set<String>? = null

    private val myUserId: String get() = AppSettings.userId
    private val childId: String? get() = _state.value.selectedChild?.userId

    /** Paketlar katalogi kelganda holat qayta hisoblanadi (katalog oqimni ushlab turmaydi). */
    private val packsTick = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    private var catalogJob: Job? = null

    /** "Hozir amalda" har daqiqada qayta hisoblanadi. */
    private val ticker = merge(
        flow {
            while (true) {
                emit(Unit)
                delay(TICK_MS)
            }
        },
        packsTick,
    )

    fun onEvent(e: PolicyListEvent) {
        when (e) {
            PolicyListEvent.Load -> load()
            PolicyListEvent.Refresh -> childId?.let { refresh(it, pull = true) }
            PolicyListEvent.Resumed -> childId?.let { refreshPaid(it) }
            is PolicyListEvent.ChildSelected -> select(e.child)
            is PolicyListEvent.TabSelected -> {
                mineBeforeCreate = null
                _state.update { it.copy(tab = e.tab) }
            }

            is PolicyListEvent.PresetToggled -> togglePresetFor(e.kind, e.enabled, e.title)
            is PolicyListEvent.PresetClicked -> childId?.let { _effect.trySend(PolicyListEffect.OpenPreset(it, e.kind)) }

            is PolicyListEvent.QuickBlockToggled -> toggleQuick(e.enabled)
            PolicyListEvent.QuickBlockClicked -> childId?.let { _effect.trySend(PolicyListEffect.OpenQuickBlock(it)) }

            is PolicyListEvent.ProtectionToggled -> toggleProtectionAll(e.enabled)
            PolicyListEvent.ProtectionClicked -> childId?.let { _effect.trySend(PolicyListEffect.OpenProtection(it)) }

            is PolicyListEvent.PolicyToggled -> control(e.policyId, PayWallReason.POLICY_COUNT) { togglePolicy(e.policyId, e.enabled) }
            is PolicyListEvent.PolicyClicked -> childId?.let { _effect.trySend(PolicyListEffect.OpenPolicy(it, e.policyId)) }

            PolicyListEvent.CreateClicked -> childId?.let { id ->
                val s = _state.value
                mineBeforeCreate = if (s.tab == PolicyTab.TEMPLATES) s.mine.map { it.policyId }.toSet() else null
                _effect.trySend(PolicyListEffect.OpenCreate(id))
            }
            PolicyListEvent.PayWallDismissed -> _state.update { it.copy(payWall = null) }
            PolicyListEvent.TakeOverConfirmed -> _state.value.takeOver?.let { t ->
                _state.update { it.copy(takeOver = null) }
                togglePresetFor(t.kind, enabled = true, title = t.title, confirmed = true)
            }
            PolicyListEvent.TakeOverDismissed -> _state.update { it.copy(takeOver = null) }
            PolicyListEvent.ErrorDismissed -> _state.update { it.copy(error = null) }
        }
    }

    // ═══ Farzand ═══════════════════════════════════════════════

    init {
        // Kesh darhol o'qiladi (LaunchedEffect'ni — birinchi kadrdan keyingi — kutmaydi)
        _state.value.selectedChild?.let { observe(it.userId) }
    }

    private fun load() {
        val selected = AppSettings.selectedChild
        _state.update { it.copy(children = AppSettings.children.ifEmpty { it.children }, selectedChild = selected ?: it.selectedChild) }
        when {
            selected != null && dataJob?.isActive != true -> observe(selected.userId)
            firstLoad -> Unit // init'dagi observe allaqachon yangilayapti
            else -> selected?.let { refresh(it.userId, pull = false) }
        }
        firstLoad = false
        screenModelScope.launch {
            val list = childRepository.children().getOrNull() ?: return@launch
            _state.update { it.copy(children = list) }
            if (_state.value.selectedChild == null) list.firstOrNull()?.let { select(it) }
        }
    }

    private fun select(child: UserInfo) {
        if (child.userId == childId && dataJob?.isActive == true) return
        mineBeforeCreate = null
        AppSettings.selectedChildId = child.userId
        AppSettings.selectedChild = child
        _state.update {
            PolicyListState(
                children = it.children,
                selectedChild = child,
                tab = it.tab,
                tabs = rememberedTabs(child.userId) ?: listOf(PolicyTab.TEMPLATES, PolicyTab.MINE),
            )
        }
        observe(child.userId)
    }

    private fun observe(childId: String) {
        dataJob?.cancel()
        serverFresh = false
        paid.value = childPaidStatus(childId)
        location.value = null
        // Kesh ma'lum bo'lsa — to'liq holat SHU ZAHOTI (birinchi kadrdan): kartalar, tablar, holatlar
        if (observePolicies.isCached(childId)) {
            apply(childId, input(childId, observePolicies.cached(childId), observeQuickSnapshot.cached(childId, myUserId), paid.value, null))
        }
        dataJob = screenModelScope.launch {
            combine(observePolicies(childId), observeQuickSnapshot(childId, myUserId), paid, location, ticker) { p, q, paidNow, loc, _ ->
                input(childId, p, q, paidNow, loc)
            }.collect { input ->
                apply(childId, input)
                showNewMine()
            }
        }
        refresh(childId, pull = false)
    }

    /** Barcha keshlardan kirish ma'lumoti — tarmoq kutilmaydi (katalog yo'q bo'lsa fonda olinadi). */
    private fun input(
        childId: String,
        p: List<Policy>,
        q: QuickBlockSnapshot,
        paidNow: Boolean?,
        loc: Pair<Double, Double>?,
    ): PolicyListInput {
        policies = p
        quick = q.copy(paid = paidNow)
        packs = getPackStatuses.cached(childId, myUserId) ?: packs.also { fetchCatalog(childId) }
        val now = Clock.System.now()
        val local = now.toLocalDateTime(TimeZone.currentSystemDefault())
        return PolicyListInput(
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
    }

    /** Holatga qo'llash: tablar kesh bosqichida qisqarmaydi; "yuklandi" — faqat jadvallar haqiqatan ma'lum bo'lsa. */
    private fun apply(childId: String, input: PolicyListInput) {
        _state.update { prev ->
            val next = input.reduce(prev)
            computedTabs = next.tabs
            val tabs = if (serverFresh) next.tabs else stableTabs(prev.tabs, next.tabs)
            next.copy(
                tabs = tabs,
                tab = if (prev.tab in tabs) prev.tab else next.tab,
                loaded = prev.loaded || (observePolicies.isCached(childId) && getPackStatuses.cached(childId, myUserId) != null),
            )
        }
    }

    private fun fetchCatalog(childId: String) {
        if (catalogJob?.isActive == true) return
        catalogJob = screenModelScope.launch {
            if (getPackStatuses(childId, myUserId) is Outcome.Success) packsTick.tryEmit(Unit)
            else _state.update { it.copy(loaded = it.loaded || observePolicies.isCached(childId)) }
        }
    }

    private fun showNewMine() {
        val before = mineBeforeCreate ?: return
        if (_state.value.mine.none { it.policyId !in before }) return
        mineBeforeCreate = null
        _state.update { it.copy(tab = PolicyTab.MINE) }
    }

    /** Ekran ochilganda jim; pastga tortilganda xato ko'rsatiladi. */
    private fun refresh(childId: String, pull: Boolean) {
        if (pull) _state.update { it.copy(isRefreshing = true) }
        screenModelScope.launch {
            val res = refreshPolicies(childId, force = true)
            if (res !is Outcome.Failure) settleTabs(childId)
            _state.update {
                it.copy(
                    isRefreshing = false,
                    error = if (pull && res is Outcome.Failure) res else it.error,
                    // Tarmoq xato bo'lsa ham kontent yashirin qolib ketmasin
                    loaded = it.loaded || res is Outcome.Failure,
                )
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

    /** Server ma'lumoti keldi: tablar endi aniq — saqlanadi (keyingi ochilishda shu bilan boshlanadi). */
    private fun settleTabs(childId: String) {
        if (childId != this.childId) return
        serverFresh = true
        val tabs = computedTabs.ifEmpty { _state.value.tabs }
        AppSettings.setPolicyTabs(childId, tabs.joinToString(",") { it.name })
        _state.update { it.copy(tabs = tabs, tab = if (it.tab in tabs) it.tab else PolicyTab.TEMPLATES) }
    }

    private fun refreshPaid(childId: String) {
        screenModelScope.launch { paid.value = childPaidStatus.refresh(childId) }
    }

    // ═══ Yozuvlar ═══════════════════════════════════════════════

    private fun togglePresetFor(kind: PresetKind, enabled: Boolean, title: String, confirmed: Boolean = false) {
        val child = childId ?: return
        // Farzand yoki ikkinchi ota-ona yoqqan — yoqsam uniki o'chadi (server), avval so'raymiz
        val ui = _state.value.presets.firstOrNull { it.kind == kind }
        if (enabled && !confirmed && ui?.activeBy != null && ui.otherSummary != null) {
            _state.update { it.copy(takeOver = TakeOverUi(kind, ui.activeBy, ui.otherSummary, title)) }
            return
        }
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

    private fun rememberedTabs(childId: String): List<PolicyTab>? =
        AppSettings.policyTabs(childId)?.split(',')?.mapNotNull { n -> PolicyTab.entries.firstOrNull { it.name == n } }
            ?.takeIf { it.isNotEmpty() }

    /** Kesh bosqichida: ko'rsatilgan tablar yo'qolmaydi, yangilari qo'shiladi (tartib — enum bo'yicha). */
    private fun stableTabs(shown: List<PolicyTab>, computed: List<PolicyTab>): List<PolicyTab> =
        PolicyTab.entries.filter { it in shown || it in computed }

    companion object {
        const val KEY_PROTECTION = "protection"
        const val KEY_QUICK = "quick"
        fun presetKey(kind: PresetKind) = "preset:${kind.name}"
        private const val TICK_MS = 60_000L
    }
}
