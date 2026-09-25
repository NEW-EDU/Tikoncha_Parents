package uz.tikoncha_parent.presentation.statistic

import kotlinx.datetime.LocalDate
import uz.tikoncha_parent.domain.model.UserInfo
import uz.tikoncha_parent.domain.model.app_error.Outcome
import uz.tikoncha_parent.domain.model.app_usage.UsageHistory
import uz.tikoncha_parent.domain.model.permission_status.PermissionIssue
import uz.tikoncha_parent.domain.model.policy.QuickBadge
import uz.tikoncha_parent.domain.model.policy.QuickBlockSnapshot
import uz.tikoncha_parent.domain.model.policy.QuickLock
import uz.tikoncha_parent.presentation.ui_state.ResponseState
import kotlin.time.Clock
import kotlin.time.Instant

data class StatisticState(
    /* ----- Async holatlar ----- */
    val childrenResponseState: ResponseState<Nothing> = ResponseState.Idle,
    val appUsageResponseState: ResponseState<Nothing> = ResponseState.Idle,
    val isRefreshing: Boolean = false,
    /* ----- Child ----- */
    val childrenList: List<UserInfo> = emptyList(),
    val selectedChild: UserInfo? = null,

    /* ----- Permission ----- */
    val permissionIssueList: List<PermissionIssue> = emptyList(),

    /* ----- Raw data ----- */
    val history: UsageHistory = UsageHistory(emptyList()),
    val today: LocalDate? = null,

    /* ----- Tab / Pager ----- */
    val dateSelectionType: DateSelectionType = DateSelectionType.DAY,
    val pages: List<PagePeriod> = emptyList(),
    val selectedPageIndex: Int = 0,

    /* ----- Tanlangan page uchun derived ----- */
    val bars: List<ChartBarUi> = emptyList(),
    val apps: List<StatAppUi> = emptyList(),

    /* ----- Bar click dialog ----- */
    val usageDetails: UsageDetailsUi? = null,
    val showUsageDetailsDialog: Boolean = false,

    /* ----- Tezkor blok ----- */
    /** Bolaning barcha tezkor bloklari + tarifi — qarorlar shu yerda (domen, test qilingan). */
    val quick: QuickBlockSnapshot = QuickBlockSnapshot(),
    /** Pauza "hozir"ga bog'liq — har daqiqada yangilanadi, pauza tugagach qulf o'zi yashilga qaytadi. */
    val now: Instant = Clock.System.now(),
    /** Server javobini kutayotgan paketlar — qulf tugmasi bloklanadi. */
    val quickBlockInProgress: Set<String> = emptySet(),
    /** Qulf bosilgandagi xato (tizim paketi 422, tarmoq va h.k.). */
    val quickBlockFailure: Outcome.Failure? = null,
    /** 403 — server pullik deb rad etdi (lokal tarif eskirgan bo'lishi mumkin). */
    val quickBlockPremiumFailure: Outcome.Failure? = null,
) {
    val selectedPage: PagePeriod? get() = pages.getOrNull(selectedPageIndex)

    val weekly: Boolean get() = dateSelectionType == DateSelectionType.WEEK

    fun quickLock(packageName: String): QuickLock? = quick.lock(packageName, now)

    fun quickBadge(packageName: String): QuickBadge? = quick.badge(packageName, now)

    /** Qo'shish — Plus; olib tashlash — doim bepul. */
    fun needsPaywall(packageName: String): Boolean = quick.needsPaywall(packageName)

    /** O'chiq blokka qo'shish undagi boshqa ilovalarni ham yopadi — 0 bo'lsa so'ralmaydi. */
    fun reenableCount(packageName: String): Int = quick.reenableCount(packageName)
}
