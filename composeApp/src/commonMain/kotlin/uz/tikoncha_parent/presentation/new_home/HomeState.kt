package uz.tikoncha_parent.presentation.new_home

import uz.tikoncha_parent.domain.model.HourMinute
import uz.tikoncha_parent.domain.model.UserInfo
import uz.tikoncha_parent.domain.model.app_usage.TopApp
import uz.tikoncha_parent.presentation.ui_state.ResponseState

data class HomeState(
    val childrenResponseState: ResponseState<Nothing> = ResponseState.Idle,
    val isRefreshing: Boolean = false,
    val childrenList: List<UserInfo> = emptyList(),
    val selectedChild: UserInfo? = null,

    val userName: String = "",
    val userImageUrl: String? = null,
    val showTikonchaTutorialCard: Boolean = false,

    val activeTaskCount: Int = 0,
    val parentPolicyCount: Int = 0,

    /* ----- HomeScreen card uchun bugungi usage ----- */
    val todayUsage: HourMinute = HourMinute(0, 0),
    val topApps: List<TopApp> = emptyList(),
    val topAppsFromRecentDays: Boolean = false,
    /** Statistika bloki qaysi kunni ko'rsatyapti: null — bugun; aks holda bugun bo'sh bo'lgani uchun so'nggi kun. */
    val usageShownDay: kotlinx.datetime.LocalDate? = null,
    val protectionPendingRequestCount: Int = 0,
    val protectionPermissionOffCount: Int = 0,
    val protectionLoaded: Boolean = false,

    /** Akkaunt öçirişga navbatda (sayt/bot/ilova, 7 kun): sana (YYYY-MM-DD) — "Akkauntni saqlaş" oynasi. */
    val deletionScheduledFor: String? = null,

    /** Google/Apple bilan kirgan ota-ona: farzand SOS'i qönğiroq qila oladigan raqam söraladi. */
    val askContactPhone: Boolean = false,
    val contactPhoneError: String? = null,
)