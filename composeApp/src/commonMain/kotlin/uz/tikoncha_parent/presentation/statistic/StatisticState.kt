package uz.tikoncha_parent.presentation.statistic

import kotlinx.datetime.LocalDate
import uz.tikoncha_parent.domain.model.SubscriptionLimit
import uz.tikoncha_parent.domain.model.UserInfo
import uz.tikoncha_parent.domain.model.app_error.Outcome
import uz.tikoncha_parent.domain.model.app_usage.AppUsage
import uz.tikoncha_parent.domain.model.permission_status.PermissionIssue
import uz.tikoncha_parent.domain.model.policy.QuickBlockEntry
import uz.tikoncha_parent.presentation.ui_state.ResponseState

data class StatisticState(
    /* ----- Async holatlar ----- */
    val childrenResponseState: ResponseState<Nothing> = ResponseState.Idle,
    val appUsageResponseState: ResponseState<Nothing> = ResponseState.Idle,
    val isRefreshing: Boolean = false,
    /* ----- Child ----- */
    val childrenList: List<UserInfo> = emptyList(),
    val selectedChild: UserInfo? = null,

    /* ----- Permission / Subscription ----- */
    val permissionIssueList: List<PermissionIssue> = emptyList(),
    val subscriptionLimit: SubscriptionLimit? = null,
    val showBlur: Boolean = false,

    /* ----- Raw data ----- */
    val appUsageList: List<AppUsage> = emptyList(),
    val today: LocalDate? = null,

    /* ----- Tab / Pager ----- */
    val dateSelectionType: DateSelectionType = DateSelectionType.DAY,   // default DAILY
    val pages: List<PagePeriod> = emptyList(),
    val selectedPageIndex: Int = 0,

    /* ----- Tanlangan page uchun derived ----- */
    val bars: List<ChartBarUi> = emptyList(),
    val topApps: List<TopAppUi> = emptyList(),

    /* ----- Bar click dialog ----- */
    val usageDetails: UsageDetailsUi? = null,
    val showUsageDetailsDialog: Boolean = false,


    /* ----- Tezkor blok ----- */
    val myUserId: String = "",
    val quickBlocks: List<QuickBlockEntry> = emptyList(),
    /** Server javobini kutayotgan paketlar — qulf tugmasi bloklanadi. */
    val quickBlockInProgress: Set<String> = emptySet(),
    /** Qulf bosilgandagi xato (tizim paketi 422, tarmoq va h.k.). */
    val quickBlockFailure: Outcome.Failure? = null,
    /** 403 — tezkor blok pullik imkoniyat. */
    val quickBlockPremiumFailure: Outcome.Failure? = null,
) {
    val selectedPage: PagePeriod? get() = pages.getOrNull(selectedPageIndex)

    /** Paketni men bloklaganmanmi. */
    fun blockedByMe(packageName: String): Boolean =
        quickBlocks.any { it.isMine(myUserId) && packageName in it.targets.packages }

    /** Paketni bola o'zi yoki boshqa ota-ona bloklaganmi. */
    fun blockedByOthers(packageName: String): Boolean =
        quickBlocks.any { !it.isMine(myUserId) && packageName in it.targets.packages }

    /** Paketni bolaning o'zi bloklaganmi. */
    fun blockedByChild(packageName: String): Boolean =
        quickBlocks.any { it.isChildOwner && packageName in it.targets.packages }
}