@file:OptIn(ExperimentalMaterial3Api::class)

package uz.tikoncha_parent.presentation.statistic

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.runtime.key
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import tikoncha_parents.composeapp.generated.resources.stat_lock_hint
import tikoncha_parents.composeapp.generated.resources.stat_quick_paywall_message
import tikoncha_parents.composeapp.generated.resources.stat_quick_paywall_title
import tikoncha_parents.composeapp.generated.resources.stat_reenable_confirm
import tikoncha_parents.composeapp.generated.resources.stat_reenable_message
import tikoncha_parents.composeapp.generated.resources.stat_reenable_title
import uz.tikoncha_parent.presentation.base.CustomBottomDialog
import uz.tikoncha_parent.presentation.base.haptics.ErrorHaptic
import uz.tikoncha_parent.presentation.base.haptics.PagerSwipeHaptic
import uz.tikoncha_parent.presentation.base.haptics.rememberAppHaptics
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinNavigatorScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.Navigator
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import tikoncha_parents.composeapp.generated.resources.Res
import tikoncha_parents.composeapp.generated.resources.dialog_failed
import tikoncha_parents.composeapp.generated.resources.eng_kop_foydalanilgan
import tikoncha_parents.composeapp.generated.resources.farzandingizni_tanlang
import tikoncha_parents.composeapp.generated.resources.farzandlaringiz
import tikoncha_parents.composeapp.generated.resources.haftalik
import tikoncha_parents.composeapp.generated.resources.kunlik
import tikoncha_parents.composeapp.generated.resources.statistika
import tikoncha_parents.composeapp.generated.resources.xatolik
import uz.tikoncha_parent.platform.openUrl
import uz.tikoncha_parent.presentation.add_child.AddChildScreen
import uz.tikoncha_parent.presentation.base.ChildSelectionButton
import uz.tikoncha_parent.presentation.base.CustomDialog
import uz.tikoncha_parent.presentation.base.CustomHeader
import uz.tikoncha_parent.presentation.base.LoadingDialog
import uz.tikoncha_parent.presentation.base.PermissionWarningCard
import uz.tikoncha_parent.presentation.base.PillSegmentedButton
import uz.tikoncha_parent.presentation.base.PillSegmentedItem
import uz.tikoncha_parent.presentation.base.PullToRefreshBox
import uz.tikoncha_parent.presentation.base.SubscriptionBottomDialog
import uz.tikoncha_parent.presentation.base.ToastProvider
import uz.tikoncha_parent.presentation.base.asText
import uz.tikoncha_parent.presentation.new_home.SelectionChildBottomSheet
import uz.tikoncha_parent.presentation.profile.subscription.subscription_payment.SubscriptionPaymentScreen
import uz.tikoncha_parent.presentation.ui_state.ResponseState
import uz.tikoncha_parent.presentation.ui_state.errorText
import uz.tikoncha_parent.ui.ContainerPadding
import uz.tikoncha_parent.ui.Space
import uz.tikoncha_parent.ui.SpaceLarge
import uz.tikoncha_parent.ui.SpaceMedium
import uz.tikoncha_parent.ui.SpaceSmall
import uz.tikoncha_parent.ui.TextFieldCornerRadius
import uz.tikoncha_parent.ui.theme.AppColors
import uz.tikoncha_parent.ui.theme.AppTypography
import uz.tikoncha_parent.ui.theme.ThemeMode
import uz.tikoncha_parent.ui.theme.TikonchaParentTheme
import uz.tikoncha_parent.ui.theme.rememberScreenSystemBars

class StatisticScreen : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.current ?: return
        val viewModel = navigator.koinNavigatorScreenModel<StatisticViewModel>()
        val state by viewModel.state.collectAsStateWithLifecycle()
        ToastProvider {
            StatisticUi(navigator, state, viewModel::onEvent)
        }
    }
}

@Composable
fun StatisticUi(
    navigator: Navigator?,
    state: StatisticState,
    event: (StatisticEvent) -> Unit,
) {
    var showChildSheet by remember { mutableStateOf(false) }
    var showErrorDialog by remember { mutableStateOf(false) }
    var showQuickPaywall by remember { mutableStateOf(false) }
    /** Qayta yoqish so'ralayotgan ilova va u bilan birga yopiladigan boshqa ilovalar soni. */
    var reenableFor by remember { mutableStateOf<Pair<String, Int>?>(null) }
    val haptics = rememberAppHaptics()
    val appUsageErrorText = state.appUsageResponseState.errorText()
    val appUsageLoading = state.appUsageResponseState is ResponseState.Loading

    val systemBars = rememberScreenSystemBars(
        statusBarColor = AppColors.bg.secondary,
        navigationBarColor = AppColors.bg.secondary,
    )

    LoadingDialog(appUsageLoading && !state.isRefreshing)

    // Ekran har ko'ringanda (Voyager'da orqaga qaytganda ham) — bolalar, foydalanish, tarif
    LaunchedEffect(Unit) { event(StatisticEvent.GetChildren) }
    // Ilova fondan qaytdi (masalan, to'lovdan) — tarif va tezkor bloklar
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { event(StatisticEvent.Resumed) }

    LaunchedEffect(appUsageErrorText) {
        if (appUsageErrorText.isNotEmpty()) showErrorDialog = true
    }
    ErrorHaptic(appUsageErrorText.takeIf { it.isNotEmpty() })
    ErrorHaptic(state.quickBlockFailure)

    // Qaror State'da (domen): olib tashlash doim bepul, qo'shish — Plus, o'chiq blok — so'raladi
    val onQuickBlock: (String) -> Unit = { packageName ->
        val others = state.reenableCount(packageName)
        when {
            state.needsPaywall(packageName) -> showQuickPaywall = true
            others > 0 -> reenableFor = packageName to others
            else -> event(StatisticEvent.ToggleQuickBlock(packageName))
        }
    }

    // Child selection sheet
    if (showChildSheet) {
        SelectionChildBottomSheet(
            navigator = navigator,
            items = state.childrenList,
            selectedItem = state.selectedChild,
            onDismiss = { showChildSheet = false },
            title = stringResource(Res.string.farzandlaringiz),
            onItemSelected = {
                event(StatisticEvent.OnChildSelected(it))
                showChildSheet = false
            }
        )
    }

    // Error dialog
    CustomDialog(
        painter = painterResource(Res.drawable.dialog_failed),
        onDismiss = { showErrorDialog = false },
        show = showErrorDialog,
        title = stringResource(Res.string.xatolik),
        message = appUsageErrorText,
        onButtonClick = { showErrorDialog = false }
    )

    // Tezkor blok xatosi (tizim paketi, tarmoq)
    CustomDialog(
        painter = painterResource(Res.drawable.dialog_failed),
        onDismiss = { event(StatisticEvent.DismissQuickBlockFailure) },
        show = state.quickBlockFailure != null,
        title = stringResource(Res.string.xatolik),
        message = state.quickBlockFailure?.asText().orEmpty(),
        onButtonClick = { event(StatisticEvent.DismissQuickBlockFailure) },
    )

    // Tezkor blok pullik: oldindan (lokal tarif) yoki server 403 bergandan keyin
    SubscriptionBottomDialog(
        show = showQuickPaywall || state.quickBlockPremiumFailure != null,
        title = stringResource(Res.string.stat_quick_paywall_title),
        message = stringResource(Res.string.stat_quick_paywall_message),
        onConfirm = {
            showQuickPaywall = false
            event(StatisticEvent.DismissQuickBlockFailure)
            navigator?.push(SubscriptionPaymentScreen())
        },
        onDismiss = {
            showQuickPaywall = false
            event(StatisticEvent.DismissQuickBlockFailure)
        },
    )

    // O'chiq tezkor blokka qo'shish uni qayta yoqadi — boshqa ilovalar ham yopiladi
    val reenable = reenableFor
    CustomBottomDialog(
        show = reenable != null,
        title = stringResource(Res.string.stat_reenable_title),
        message = stringResource(Res.string.stat_reenable_message, reenable?.second ?: 0),
        confirmButtonText = stringResource(Res.string.stat_reenable_confirm),
        onDismiss = { reenableFor = null },
        onConfirm = {
            reenable?.let { event(StatisticEvent.ToggleQuickBlock(it.first)) }
            reenableFor = null
        },
    )

    // Bar click dialog
    UsageDetailsDialog(
        details = state.usageDetails,
        show = state.showUsageDetailsDialog,
        onDismiss = { event(StatisticEvent.DismissUsageDetailsDialog) }
    )

    PullToRefreshBox(
        isRefreshing = state.isRefreshing,
        onRefresh = { event(StatisticEvent.PullRefresh) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .then(systemBars.modifier)
                .background(AppColors.bg.secondary)
        ) {
            CustomHeader(
                title = stringResource(Res.string.statistika),
                showBackButton = true,
                onBackClick = { navigator?.pop() },
                trailingIcon = {
                    ChildSelectionButton(
                        modifier = Modifier.widthIn(120.dp, 160.dp),
                        text = state.selectedChild?.name ?: "",
                        imageUrl = state.selectedChild?.avatarUrl ?: "",
                        label = stringResource(Res.string.farzandingizni_tanlang),
                        userInfo = state.selectedChild,
                        onClick = {
                            if (state.childrenList.isEmpty()) navigator?.push(AddChildScreen())
                            else showChildSheet = true
                        }
                    )
                }
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Permission warnings
                if (state.permissionIssueList.isNotEmpty()) {
                    Space(12.dp)
                    state.permissionIssueList.forEach { issue ->
                        PermissionWarningCard(
                            title = issue.title,
                            body = issue.body,
                            videoUrl = issue.videoUrl,
                            onVideoClick = { openUrl(it) }
                        )
                        Space(12.dp)
                    }
                } else {
                    Space(12.dp)
                }

                /* ============ Chart card ============ */
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(AppColors.bg.surface, RoundedCornerShape(TextFieldCornerRadius))
                        .padding(ContainerPadding)
                ) {
                    // Toggle DAILY / WEEKLY
                    val selectedIdx = if (state.dateSelectionType == DateSelectionType.DAY) 0 else 1
                    PillSegmentedButton(
                        items = listOf(
                            PillSegmentedItem(label = stringResource(Res.string.kunlik)),
                            PillSegmentedItem(label = stringResource(Res.string.haftalik)),
                        ),
                        selectedIndex = selectedIdx,
                        modifier = Modifier.fillMaxWidth(),
                        onSelected = { idx ->
                            if (idx != selectedIdx) haptics.tick()
                            val mode = if (idx == 0) DateSelectionType.DAY else DateSelectionType.WEEK
                            event(StatisticEvent.ChangeMode(mode))
                        }
                    )

                    SpaceMedium()

                    // Pager
                    PagerBlock(
                        state = state,
                        onPageChange = { event(StatisticEvent.PageChanged(it)) }
                    )

                    SpaceLarge()

                    // Chart
                    UsageBarChart(
                        bars = state.bars,
                        mode = state.dateSelectionType,
                        chartSubtitle = state.selectedPage?.chartSubtitle,
                        onBarClick = { event(StatisticEvent.BarClicked(it)) },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                SpaceMedium()

                /* ============ Ilovalar ============ */
                if (state.apps.isNotEmpty()) {
                    Text(
                        text = stringResource(Res.string.eng_kop_foydalanilgan),
                        color = AppColors.text.primary,
                        style = AppTypography.titleMdSemiBold
                    )
                    SpaceSmall()

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(AppColors.bg.surface, RoundedCornerShape(TextFieldCornerRadius))
                            .padding(horizontal = ContainerPadding)
                    ) {
                        state.apps.forEachIndexed { i, app ->
                            key(app.packageName) {
                                StatAppRow(
                                    app = app,
                                    weekly = state.weekly,
                                    showDivider = i < state.apps.lastIndex,
                                    badge = state.quickBadge(app.packageName),
                                    lock = state.quickLock(app.packageName),
                                    isBlockBusy = app.packageName in state.quickBlockInProgress,
                                    onQuickBlockClick = { onQuickBlock(app.packageName) },
                                )
                            }
                        }
                    }
                    LockHint()
                }

                SpaceLarge()
            }
        }
    }
}

/** Ro'yxat ostida bir qator: qulf nima qilishini aytadi. */
@Composable
private fun LockHint() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp, bottom = 8.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = LockOpenRounded,
            contentDescription = null,
            modifier = Modifier.size(14.dp),
            tint = AppColors.text.tertiary,
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = stringResource(Res.string.stat_lock_hint),
            style = AppTypography.emphasizedXsRegular,
            color = AppColors.text.tertiary,
        )
    }
}

@Composable
private fun PagerBlock(
    state: StatisticState,
    onPageChange: (Int) -> Unit,
) {
    if (state.pages.isEmpty()) {
        Spacer(Modifier.height(64.dp))
        return
    }

    val pagerState = rememberPagerState(
        initialPage = state.selectedPageIndex.coerceIn(0, state.pages.lastIndex),
        pageCount = { state.pages.size }
    )

    // state → pager
    LaunchedEffect(state.selectedPageIndex, state.pages.size) {
        val target = state.selectedPageIndex.coerceIn(0, state.pages.lastIndex)
        if (pagerState.currentPage != target) pagerState.scrollToPage(target)
    }

    // Barmoq bilan sahifa almashganda — tik (dasturiy o'tish jim)
    PagerSwipeHaptic(pagerState)

    // pager → state
    LaunchedEffect(pagerState.settledPage) {
        if (pagerState.settledPage != state.selectedPageIndex) onPageChange(pagerState.settledPage)
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        PagerDots(
            total = state.pages.size,
            current = pagerState.currentPage,
            modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp)
        )

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxWidth(),
            beyondViewportPageCount = 1,
        ) { idx ->
            val page = state.pages[idx]
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = pageTitleString(page.title),
                    style = AppTypography.titleMdMedium,
                    color = AppColors.text.secondary,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = durationString(page.subtitle),
                    style = AppTypography.headlineMdSemiBold,
                    color = AppColors.text.primary,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}


@Preview
@Composable
private fun StatisticScreenPreview_Daily_WithData() {
    TikonchaParentTheme(ThemeMode.LIGHT) {
        ToastProvider {
            StatisticUi(
                navigator = null,
                state = previewStateDaily(),
                event = {},
            )
        }
    }
}

@Preview
@Composable
private fun StatisticScreenPreview_Weekly_WithData() {
    TikonchaParentTheme(ThemeMode.LIGHT) {
        StatisticUi(
            navigator = null,
            state = previewStateWeekly(),
            event = {},
        )
    }
}

@Preview
@Composable
private fun StatisticScreenPreview_Empty() {
    TikonchaParentTheme(ThemeMode.LIGHT) {
        StatisticUi(
            navigator = null,
            state = previewStateEmpty(),
            event = {},
        )
    }
}

@Preview
@Composable
private fun StatisticScreenPreview_Dark() {
    TikonchaParentTheme(ThemeMode.DARK) {
        StatisticUi(
            navigator = null,
            state = previewStateDaily(),
            event = {},
        )
    }
}
