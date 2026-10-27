@file:OptIn(androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class)

package uz.tikoncha_parent.presentation.new_home

import tikoncha_parents.composeapp.generated.resources.profil
import tikoncha_parents.composeapp.generated.resources.del_acc_scheduled_title
import tikoncha_parents.composeapp.generated.resources.del_acc_keep_msg
import tikoncha_parents.composeapp.generated.resources.del_acc_keep_button
import tikoncha_parents.composeapp.generated.resources.dialog_warning
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.foundation.clickable
import tikoncha_parents.composeapp.generated.resources.parent_avatar_default
import uz.tikoncha_parent.presentation.base.island.ChildAvatar
import uz.tikoncha_parent.ui.homeSubtitleColor
import uz.tikoncha_parent.ui.homeBarColors
import uz.tikoncha_parent.ui.homeChipColor
import uz.tikoncha_parent.ui.homeNameAccent
import uz.tikoncha_parent.ui.homeTileColor
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.SpanStyle
import uz.tikoncha_parent.ui.autumnBackground
import uz.tikoncha_parent.ui.tileBlob
import uz.tikoncha_parent.ui.BlobPlace
import uz.tikoncha_parent.ui.tileBlobColor
import uz.tikoncha_parent.ui.rememberHapticSource
import uz.tikoncha_parent.presentation.base.island.roundedContentEdges
import kotlinx.datetime.minus
import kotlinx.datetime.todayIn
import uz.tikoncha_parent.common.DateTimeUtil
import tikoncha_parents.composeapp.generated.resources.sana_sarfladi
import tikoncha_parents.composeapp.generated.resources.kecha_sarfladi
import uz.tikoncha_parent.platform.BuildConfig
import tikoncha_parents.composeapp.generated.resources.home_greeting
import uz.tikoncha_parent.presentation.base.island.ProvideIsland
import uz.tikoncha_parent.presentation.base.island.IslandSpec
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.collectAsState
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.WindowInsets
import uz.tikoncha_parent.ui.rememberCachedImage
import uz.tikoncha_parent.ui.SegmentGap
import uz.tikoncha_parent.ui.expressiveSegment
import tikoncha_parents.composeapp.generated.resources.add
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.ui.text.font.FontWeight
import uz.tikoncha_parent.ui.expressiveCard
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleStartEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinNavigatorScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.Navigator
import coil3.compose.AsyncImage
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import tikoncha_parents.composeapp.generated.resources.Res
import tikoncha_parents.composeapp.generated.resources.android
import tikoncha_parents.composeapp.generated.resources.bugun_sarfladi
import tikoncha_parents.composeapp.generated.resources.cheklovlar
import tikoncha_parents.composeapp.generated.resources.diqqat
import tikoncha_parents.composeapp.generated.resources.faol_vazifa
import tikoncha_parents.composeapp.generated.resources.farzand_malumotlari_keyin_korinadi
import tikoncha_parents.composeapp.generated.resources.farzand_qo_shish
import tikoncha_parents.composeapp.generated.resources.farzandlaringiz
import tikoncha_parents.composeapp.generated.resources.home_table
import tikoncha_parents.composeapp.generated.resources.home_task
import tikoncha_parents.composeapp.generated.resources.apps_restricted
import tikoncha_parents.composeapp.generated.resources.media_play
import tikoncha_parents.composeapp.generated.resources.support_icon
import tikoncha_parents.composeapp.generated.resources.topshiriqlar
import uz.tikoncha_parent.domain.model.HourMinute
import uz.tikoncha_parent.domain.model.app_usage.TopApp
import uz.tikoncha_parent.platform.HandleUpdateEffect
import uz.tikoncha_parent.platform.Logger
import uz.tikoncha_parent.platform.openUrl
import uz.tikoncha_parent.presentation.add_child.AddChildScreen
import uz.tikoncha_parent.presentation.base.ChildSelectionButton
import uz.tikoncha_parent.presentation.base.CustomDialog
import uz.tikoncha_parent.presentation.base.NoInternetDialog
import uz.tikoncha_parent.presentation.base.PullToRefreshBox
import uz.tikoncha_parent.presentation.base.rememberInternetCheck
import uz.tikoncha_parent.presentation.base.singleClick
import uz.tikoncha_parent.presentation.chat.chat_list.ChatScreen
import uz.tikoncha_parent.presentation.in_app_update.InAppUpdateCard
import uz.tikoncha_parent.presentation.in_app_update.InAppUpdateDialog
import uz.tikoncha_parent.presentation.in_app_update.UpdateEvent
import uz.tikoncha_parent.presentation.in_app_update.UpdateUiState
import uz.tikoncha_parent.presentation.in_app_update.UpdateViewModel
import uz.tikoncha_parent.presentation.policy.list.PolicyListScreen
import uz.tikoncha_parent.presentation.profile.ProfileScreen
import uz.tikoncha_parent.presentation.protection.ProtectionScreen
import uz.tikoncha_parent.presentation.statistic.StatisticScreen
import uz.tikoncha_parent.presentation.statistic.durationStringCompact
import uz.tikoncha_parent.presentation.task.TaskScreen
import uz.tikoncha_parent.presentation.tracking.TrackingScreen
import uz.tikoncha_parent.presentation.support.SupportSheet
import uz.tikoncha_parent.presentation.video_tutorial.TutorialType
import uz.tikoncha_parent.presentation.video_tutorial.openTutorial
import uz.tikoncha_parent.presentation.video_tutorial.VideoTutorialYoutubeScreen
import uz.tikoncha_parent.ui.CardCornerPadding
import uz.tikoncha_parent.ui.ContainerPadding
import uz.tikoncha_parent.ui.HomeIconSize
import uz.tikoncha_parent.ui.HomeItemHeight
import uz.tikoncha_parent.ui.LargeCardCornerRadius
import uz.tikoncha_parent.ui.NormalIconSize
import uz.tikoncha_parent.ui.SmallIconSize
import uz.tikoncha_parent.ui.Space
import uz.tikoncha_parent.ui.SpaceUltraSmall
import uz.tikoncha_parent.ui.TextFieldCornerRadius
import uz.tikoncha_parent.ui.theme.AppColors
import uz.tikoncha_parent.ui.theme.AppTypography
import uz.tikoncha_parent.ui.theme.ThemeMode
import uz.tikoncha_parent.ui.theme.TikonchaParentTheme
import uz.tikoncha_parent.ui.theme.extendedColor
import uz.tikoncha_parent.ui.theme.rememberScreenSystemBars

class NewHomeScreen : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.current ?: return

        val viewModel = navigator.koinNavigatorScreenModel<HomeViewModel>()
        val state = viewModel.state.collectAsStateWithLifecycle()
        val event = viewModel::onEvent

        val updateViewModel = koinViewModel<UpdateViewModel>()
        val updateState by updateViewModel.state.collectAsStateWithLifecycle()
        val updateEvent = updateViewModel::onEvent

        LifecycleStartEffect(Unit) {
            event(HomeEvent.SyncSelectedChildFromSettings)
            event(HomeEvent.GetChildren)
            onStopOrDispose {}
        }

        HandleUpdateEffect(updateViewModel)

        Logger.d("NewHomeScreen", "Content")

        NewHomeUi(
            navigator = navigator,
            state = state.value,
            event = event,
            appUpdateState = updateState,
            appUpdateEvent = updateEvent
        )
    }
}

@Composable
fun NewHomeUi(
    navigator: Navigator?,
    state: HomeState,
    event: (HomeEvent) -> Unit,
    appUpdateState: UpdateUiState = UpdateUiState(),
    appUpdateEvent: (UpdateEvent) -> Unit = {},
) {
    LaunchedEffect(Unit) {
        event(HomeEvent.ReloadUserInfo)
        event(HomeEvent.RefreshParentRequest)
    }

    val showTikonchaTutorialCard = state.showTikonchaTutorialCard
    val taskCount = state.activeTaskCount
    val tableCount = state.parentPolicyCount
    val refreshScope = rememberCoroutineScope()
    var showDialog by remember { mutableStateOf(false) }
    var showChildDialog by remember { mutableStateOf(false) }
    var showSupport by remember { mutableStateOf(false) }
    if (showSupport) SupportSheet(onDismiss = { showSupport = false })

    // Kontent orol ustida tugaydi — boshqa ekranlardagidek yumaloq chekka bilan (orol atrofi ham ekran fonida)
    // Fon — iliq gradient (kuz): tizim zonalari shaffof, gradient butun ekran bo'ylab
    val systemBars = rememberScreenSystemBars(
        statusBarColor = Color.Transparent,
        navigationBarColor = Color.Transparent,
    )
    val homeLayout by HomeLayoutController.layout.collectAsState()

    val internetCheck = rememberInternetCheck(refreshScope)
    NoInternetDialog(internetCheck)

    if (showDialog) {
        SelectionChildBottomSheet(
            navigator = navigator,
            items = state.childrenList,
            selectedItem = state.selectedChild,
            onDismiss = { showDialog = false },
            title = stringResource(Res.string.farzandlaringiz),
            onItemSelected = {
                event(HomeEvent.OnChildSelected(it))
                showDialog = false
            }
        )
    }

    CustomDialog(
        show = showChildDialog,
        title = stringResource(Res.string.diqqat),
        buttonText = stringResource(Res.string.farzand_qo_shish),
        buttonIcon = painterResource(Res.drawable.add),
        message = stringResource(Res.string.farzand_malumotlari_keyin_korinadi),
        onDismiss = { showChildDialog = false },
        onButtonClick = {
            navigator?.push(AddChildScreen())
            showChildDialog = false
        }
    )

    // Akkaunt öçirişga navbatda: sana + "Akkauntni saqlaş" (7 kunlik muddat ichida bir tugma bilan bekor)
    CustomDialog(
        show = state.deletionScheduledFor != null,
        painter = painterResource(Res.drawable.dialog_warning),
        title = stringResource(Res.string.del_acc_scheduled_title),
        message = stringResource(Res.string.del_acc_keep_msg, state.deletionScheduledFor.orEmpty()),
        buttonText = stringResource(Res.string.del_acc_keep_button),
        showCloseButton = true,
        onDismiss = { event(HomeEvent.DismissDeletionNotice) },
        onButtonClick = { event(HomeEvent.KeepAccount) },
    )

    InAppUpdateDialog(
        show = appUpdateState.showUpdateDialog,
        state = appUpdateState,
        onDismiss = { appUpdateEvent(UpdateEvent.DismissUpdateDialog) },
        onConfirm = { type -> appUpdateEvent(UpdateEvent.StartUpdateClicked(type)) }
    )

    PullToRefreshBox(
        isRefreshing = state.isRefreshing,
        onRefresh = {
            internetCheck.check { event(HomeEvent.PullRefresh) }
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .autumnBackground()
                .then(systemBars.modifier)
        ) {
            val nameAccent = homeNameAccent()
            // Tepada — faqat salomlashuv (o'qish joyi). Boshqaruv (yordam, farzand, profil) — pastki orolda.
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 16.dp, top = 14.dp, bottom = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    // Ism — urg'u rangida ("Salom, Dilmurod")
                    text = buildAnnotatedString {
                        val full = stringResource(Res.string.home_greeting, state.userName)
                        append(full)
                        val at = if (state.userName.isNotBlank()) full.lastIndexOf(state.userName) else -1
                        if (at >= 0) addStyle(SpanStyle(color = nameAccent), at, at + state.userName.length)
                    },
                    style = AppTypography.headlineSmSemiBold.copy(fontWeight = FontWeight.ExtraBold),
                    color = AppColors.text.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                // Profil — tepada, o'ng burçakda (pastki orolda AI keng tugma)
                // Kiçkina tişli belgi — rasm profil va sozlamalarga olib borişini bildiradi
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                        ) { navigator?.push(ProfileScreen()) },
                ) {
                    ChildAvatar(
                        state.userImageUrl,
                        Modifier.size(42.dp).align(Alignment.Center),
                        fallback = Res.drawable.parent_avatar_default,
                    )
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(18.dp)
                            .clip(CircleShape)
                            .background(homeChipColor())
                            .padding(2.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Settings,
                            contentDescription = stringResource(Res.string.profil),
                            tint = AppColors.icon.accentPrimary,
                            modifier = Modifier.size(13.dp),
                        )
                    }
                }
                Spacer(Modifier.width(8.dp))
                if (!showTikonchaTutorialCard) {
                    FilledTonalIconButton(
                        interactionSource = rememberHapticSource(),
                        onClick = {
                            navigator?.openTutorial(TutorialType.TIKONCHA)
                        },
                        modifier = Modifier.size(44.dp),
                        shapes = IconButtonDefaults.shapes(),
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = homeChipColor(),
                            contentColor = AppColors.icon.accentPrimary
                        )
                    ) {
                        Icon(
                            painter = painterResource(Res.drawable.media_play),
                            contentDescription = "",
                            modifier = Modifier.size(NormalIconSize)
                        )
                    }
                }
            }

            // Pastki orol: [profil] [yordam] [farzand ▴] — farzand tanlagich har ekrandagidek o'ng burchakda
            val pickerOpen = remember { MutableTransitionState("-hoglt.picker" in BuildConfig.debugLaunchArgs) }
            val latestState by rememberUpdatedState(state)
            val islandSpec = remember {
                IslandSpec(content = {
                    HomeIslandContent(
                        state = latestState,
                        pickerOpen = pickerOpen,
                        onSupport = { showSupport = true },
                        onAssistant = { navigator?.push(uz.tikoncha_parent.presentation.assistant.AssistantScreen()) },
                        onProfile = { navigator?.push(ProfileScreen()) },
                        onAddChild = { navigator?.push(AddChildScreen()) },
                        onChildSelected = { event(HomeEvent.OnChildSelected(it)) },
                    )
                }, roundTop = false)
            }
            ProvideIsland(islandSpec)

            // Ro'yxat va panjara uchun umumiy tepa qism: farzand tanlagich, "Hoglt nima?" kartasi, yangilanish kartasi
            val topSection: @Composable () -> Unit = {
                Column(Modifier.fillMaxWidth()) {
                    if (showTikonchaTutorialCard) {
                        TikonchaTutorialCard(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = {
                                navigator?.openTutorial(TutorialType.TIKONCHA)
                            }
                        )
                        Space(10.dp)
                    }
                    InAppUpdateCard(
                        modifier = Modifier.padding(bottom = 16.dp),
                        state = appUpdateState,
                        event = appUpdateEvent
                    )
                }
            }

            if (homeLayout == HomeLayout.GRID) {
                // Panjara: ekranning qolgan balandligini to'liq egallaydi (bo'sh joy qolmaydi); kichik ekranda — scroll.
                BoxWithConstraints(Modifier.fillMaxWidth().weight(1f).roundedContentEdges()) {
                    val viewport = maxHeight
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                            .heightIn(min = viewport)
                            .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 8.dp),
                    ) {
                        topSection()
                        HomeGridBlocks(
                        modifier = Modifier.weight(1f).heightIn(min = GridMinHeight),
                    state = state,
                    taskCount = taskCount,
                    tableCount = tableCount,
                    guard = { action ->
                        if (state.childrenList.isEmpty()) internetCheck.check { showChildDialog = true } else action()
                    },
                    onProtection = { navigator?.push(ProtectionScreen()) },
                    onStatistics = { navigator?.push(StatisticScreen()) },
                    onRestrictions = { navigator?.push(PolicyListScreen()) },
                    onMap = { navigator?.push(TrackingScreen()) },
                    onChat = { navigator?.push(ChatScreen()) },
                    onTasks = { navigator?.push(TaskScreen()) },
                )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().weight(1f).roundedContentEdges(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(0.dp),
                ) {
                    item { topSection() }
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(HomeItemHeight)
                                .expressiveSegment(0, HomeBlockCount, color = homeTileColor()) {
                                    if (state.childrenList.isEmpty()) {
                                        internetCheck.check { showChildDialog = true }
                                    } else {
                                        navigator?.push(TaskScreen())
                                    }
                                }
                                .tileBlob(tileBlobColor(), BlobPlace.RowTasks)
                                .padding(horizontal = CardCornerPadding, vertical = HomeItemVerticalPadding),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = stringResource(Res.string.topshiriqlar),
                                    color = AppColors.text.primary,
                                    style = AppTypography.displaySmRegular.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = stringResource(Res.string.faol_vazifa, taskCount),
                                    color = homeSubtitleColor(),
                                    style = AppTypography.titleSmMedium,
                                )
                            }

                            Image(
                                painter = painterResource(Res.drawable.home_task),
                                contentDescription = "",
                                modifier = Modifier.size(HomeIconSize)
                            )
                        }
                        Space(SegmentGap)
                    }
                    item {
                        NewHomeItem(
                            firstIndex = 1,
                            items = listOf(HomeSelectionItem.SIHBAT),
                            total = HomeBlockCount,
                            onSettingSelected = { selectionItem ->
                                when (selectionItem) {
                                    HomeSelectionItem.XARITA -> navigator?.push(TrackingScreen())
                                    HomeSelectionItem.SIHBAT -> navigator?.push(ChatScreen())
                                }
                            }
                        )
                    }

                    /* ============ TOPSHIRIQLAR card ============ */
                    item {
                        ChildProtectionCard(
                            segmentIndex = 2,
                            segmentCount = HomeBlockCount,
                            isLoaded = state.protectionLoaded,
                            modifier = Modifier.fillMaxWidth(),
                            hasChild = state.childrenList.isNotEmpty(),
                            permissionOffCount = state.protectionPermissionOffCount,
                            pendingRequestCount = state.protectionPendingRequestCount,
                            onClick = {
                                if (state.childrenList.isEmpty()) {
                                    internetCheck.check { showChildDialog = true }
                                } else {
                                    navigator?.push(ProtectionScreen())
                                }
                            }
                        )
                        Space(SegmentGap)
                    }

                    /* ============ STATISTIKA card ============ */
                    item {
                        NewHomeItem(
                            firstIndex = 3,
                            items = listOf(HomeSelectionItem.XARITA),
                            total = HomeBlockCount,
                            onSettingSelected = { selectionItem ->
                                when (selectionItem) {
                                    HomeSelectionItem.XARITA -> navigator?.push(TrackingScreen())
                                    HomeSelectionItem.SIHBAT -> navigator?.push(ChatScreen())
                                }
                            }
                        )
                    }

                    /* ============ TOPSHIRIQLAR card ============ */
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(HomeItemHeight)
                                .expressiveSegment(4, HomeBlockCount, color = homeTileColor()) {
                                    if (state.childrenList.isEmpty()) {
                                        internetCheck.check { showChildDialog = true }
                                    } else {
                                        navigator?.push(PolicyListScreen())
                                    }
                                }
                                .tileBlob(tileBlobColor(), BlobPlace.RowRestrictions)
                                .padding(horizontal = CardCornerPadding, vertical = HomeItemVerticalPadding),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = stringResource(Res.string.cheklovlar),
                                    color = AppColors.text.primary,
                                    style = AppTypography.displaySmRegular.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = pluralStringResource(Res.plurals.apps_restricted, tableCount, tableCount),
                                    color = homeSubtitleColor(),
                                    style = AppTypography.titleSmMedium,
                                )
                            }

                            Image(
                                painter = painterResource(Res.drawable.home_table),
                                contentDescription = "",
                                modifier = Modifier
                                    .padding(end = 4.dp)
                                    .size(HomeIconSize)
                            )
                        }
                        Space(SegmentGap)
                    }

                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(HomeItemHeight)
                                .expressiveSegment(5, HomeBlockCount, color = homeTileColor()) {
                                    if (state.childrenList.isEmpty()) {
                                        internetCheck.check { showChildDialog = true }
                                    } else {
                                        navigator?.push(StatisticScreen())
                                    }
                                }
                                .padding(horizontal = CardCornerPadding, vertical = HomeItemVerticalPadding),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = durationStringCompact(state.todayUsage),
                                    color = AppColors.text.primary,
                                    style = AppTypography.displaySmRegular.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = usageSubtitle(state.usageShownDay),
                                    color = homeSubtitleColor(),
                                    style = AppTypography.titleSmMedium,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }

                            // Bugun bo'sh bo'lsa oxirgi kunlarning ilovalari xira chiqadi; umuman yo'q bo'lsa — placeholder.
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.Bottom,
                                modifier = Modifier.padding(end = 14.5.dp)
                            ) {
                                val barHeights = listOf(0.5f, 0.65f, 0.8f)
                                val barColors = homeBarColors()
                                val iconAlpha =
                                    if (state.topAppsFromRecentDays) RecentTopAppAlpha else 1f
                                barHeights.forEachIndexed { index, heightFraction ->
                                    val app = state.topApps.getOrNull(barHeights.lastIndex - index)
                                    Box(
                                        modifier = Modifier
                                            .fillMaxHeight(heightFraction)
                                            .background(
                                                barColors[index],
                                                RoundedCornerShape(TextFieldCornerRadius)
                                            )
                                            .padding(6.dp),
                                        contentAlignment = Alignment.BottomCenter
                                    ) {
                                        if (app != null) {
                                            TopAppIcon(
                                                app = app,
                                                modifier = Modifier.size(SmallIconSize).alpha(iconAlpha)
                                            )
                                        } else {
                                            TopAppPlaceholder(modifier = Modifier.size(SmallIconSize))
                                        }
                                    }
                                }
                            }
                        }
                        Space(SegmentGap)
                    }

                    /* ============ CHEKLOVLAR card ============ */
                }
            }
        }
    }
}

/**
 * Bosh sahifaning "Panjara" ko'rinishi: o'sha 6 blok 2×3 katakda (M3 Expressive guruh — faqat tashqi to'rt burchak
 * katta). Qatorlar bo'sh joyni teng bo'lib to'ldiradi; har katakda katta, chegaradan chiqib kesilgan 3D rasm.
 */
@Composable
private fun HomeGridBlocks(
    state: HomeState,
    taskCount: Int,
    tableCount: Int,
    guard: (() -> Unit) -> Unit,
    onProtection: () -> Unit,
    onStatistics: () -> Unit,
    onRestrictions: () -> Unit,
    onMap: () -> Unit,
    onChat: () -> Unit,
    onTasks: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // Eng ko'p bosiladiganlari pastda (bosh barmoqqa yaqin): pastki chap — Statistika
    Column(modifier, verticalArrangement = Arrangement.spacedBy(SegmentGap)) {
        HomeGridRow(Modifier.weight(1f)) {
            HomeGridTile(
                cell = GridCell(0, 0),
                title = stringResource(HomeSelectionItem.SIHBAT.title),
                onClick = onChat,
                subtitle = { HomeGridSubtitle(stringResource(HomeSelectionItem.SIHBAT.subtitle)) },
                blobbed = true,
                blob = BlobPlace.GridChat,
                art = { TileArt(painterResource(HomeSelectionItem.SIHBAT.iconId), rotation = 10f) },
            )
            HomeGridTile(
                cell = GridCell(0, 1),
                title = stringResource(Res.string.topshiriqlar),
                onClick = { guard(onTasks) },
                subtitle = { HomeGridSubtitle(stringResource(Res.string.faol_vazifa, taskCount)) },
                blobbed = true,
                blob = BlobPlace.GridTasks,
                art = { TileArt(painterResource(Res.drawable.home_task), rotation = -8f) },
            )
        }
        HomeGridRow(Modifier.weight(1f)) {
            HomeGridTile(
                cell = GridCell(1, 0),
                title = stringResource(HomeSelectionItem.XARITA.title),
                onClick = onMap,
                subtitle = { HomeGridSubtitle(stringResource(HomeSelectionItem.XARITA.subtitle)) },
                blobbed = true,
                blob = BlobPlace.GridMap,
                art = { TileArt(painterResource(HomeSelectionItem.XARITA.iconId), rotation = -6f) },
            )
            ChildProtectionGridTile(
                cell = GridCell(1, 1),
                permissionOffCount = state.protectionPermissionOffCount,
                pendingRequestCount = state.protectionPendingRequestCount,
                hasChild = state.childrenList.isNotEmpty(),
                isLoaded = state.protectionLoaded,
                onClick = { guard(onProtection) },
            )
        }
        HomeGridRow(Modifier.weight(1f)) {
            HomeGridTile(
                cell = GridCell(2, 0),
                title = durationStringCompact(state.todayUsage),
                onClick = { guard(onStatistics) },
                subtitle = { HomeGridSubtitle(usageSubtitle(state.usageShownDay)) },
                art = { UsageBarsArt(state) },
            )
            HomeGridTile(
                cell = GridCell(2, 1),
                title = stringResource(Res.string.cheklovlar),
                onClick = { guard(onRestrictions) },
                subtitle = { HomeGridSubtitle(pluralStringResource(Res.plurals.apps_restricted, tableCount, tableCount)) },
                blobbed = true,
                blob = BlobPlace.GridRestrictions,
                art = { TileArt(painterResource(Res.drawable.home_table), rotation = 8f) },
            )
        }
    }
}

/**
 * Statistika katagi rasmi: katakning pastki chetidan "o'sib chiqqan" uchta ustun (grafik), har birining tepasida
 * bugungi eng ko'p ishlatilgan ilova ikonkasi. Ustunlar pastdan kesiladi — rasm katakdan davom etayotgandek.
 */
@Composable
private fun BoxScope.UsageBarsArt(state: HomeState) {
    val barColors = homeBarColors()
    val iconAlpha = if (state.topAppsFromRecentDays) RecentTopAppAlpha else 1f
    val heights = listOf(72.dp, 104.dp, 136.dp)
    Row(
        modifier = Modifier
            .align(Alignment.BottomEnd)
            .offset(x = 10.dp, y = 18.dp)
            .padding(end = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Bottom,
    ) {
        heights.forEachIndexed { index, h ->
            // Eng balandi — eng ko'p ishlatilgan (o'ngda)
            val app = state.topApps.getOrNull(heights.lastIndex - index)
            Box(
                modifier = Modifier
                    .width(46.dp)
                    .height(h)
                    .background(
                        barColors[index],
                        RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp),
                    )
                    .padding(top = 8.dp),
                contentAlignment = Alignment.TopCenter,
            ) {
                if (app != null) TopAppIcon(app, Modifier.size(30.dp).alpha(iconAlpha))
                else TopAppPlaceholder(Modifier.size(30.dp))
            }
        }
    }
}

/** "Bugun sarfladi" / "Kecha sarfladi" / "23-oktabr sarfladi" — statistika bloki qaysi kunni ko'rsatayotgani. */
@Composable
private fun usageSubtitle(day: kotlinx.datetime.LocalDate?): String {
    if (day == null) return stringResource(Res.string.bugun_sarfladi)
    val today = kotlin.time.Clock.System.todayIn(kotlinx.datetime.TimeZone.currentSystemDefault())
    return if (day == today.minus(1, kotlinx.datetime.DateTimeUnit.DAY)) stringResource(Res.string.kecha_sarfladi)
    else stringResource(Res.string.sana_sarfladi, DateTimeUtil.formatLongDate(day).substringBeforeLast(' '))
}

/** Bloklar ichidagi vertikal bo'shliq (ixcham). */
private val HomeItemVerticalPadding = 14.dp

/** Bugun bo'sh bo'lganda oxirgi kunlar ilovalari shu shaffoflikda chiziladi */
private const val RecentTopAppAlpha = 0.35f
private const val TopAppPlaceholderAlpha = 0.3f

/** Ma'lumot yo'q yoki hali kelmagan — ustun bo'sh qolmasligi uchun xira kulrang doira */
@Composable
private fun TopAppPlaceholder(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .alpha(TopAppPlaceholderAlpha)
            .background(AppColors.icon.secondary, CircleShape)
    )
}

@Composable
private fun TopAppIcon(app: TopApp, modifier: Modifier = Modifier) {
    val iconModifier = modifier.clip(RoundedCornerShape(4.dp))
    if (!app.iconUrl.isNullOrBlank()) {
        AsyncImage(
            model = rememberCachedImage(app.iconUrl),
            contentDescription = app.name,
            modifier = iconModifier,
            contentScale = ContentScale.Fit,
            error = painterResource(Res.drawable.android),
        )
    } else {
        Image(
            painter = painterResource(Res.drawable.android),
            contentDescription = app.name,
            modifier = iconModifier,
        )
    }
}

@Preview
@Composable
private fun Pre() {
    TikonchaParentTheme(ThemeMode.LIGHT) {
        NewHomeUi(
            navigator = null,
            state = HomeState(
                showTikonchaTutorialCard = true,
                todayUsage = HourMinute(1, 22),
            ),
            event = {},
        )
    }
}

/** Bosh sahifadagi guruhlangan blok: himoya, statistika, cheklovlar, xarita, suhbat, topshiriqlar. */
private const val HomeBlockCount = 6
