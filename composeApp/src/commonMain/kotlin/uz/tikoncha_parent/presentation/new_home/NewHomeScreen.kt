package uz.tikoncha_parent.presentation.new_home

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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleStartEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinNavigatorScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.Navigator
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import tikoncha_parents.composeapp.generated.resources.Res
import tikoncha_parents.composeapp.generated.resources.bugun_sarfladi
import tikoncha_parents.composeapp.generated.resources.cheklovlar
import tikoncha_parents.composeapp.generated.resources.diqqat
import tikoncha_parents.composeapp.generated.resources.faol_vazifa
import tikoncha_parents.composeapp.generated.resources.farzand_malumotlari_keyin_korinadi
import tikoncha_parents.composeapp.generated.resources.farzand_qo_shish
import tikoncha_parents.composeapp.generated.resources.farzandlaringiz
import tikoncha_parents.composeapp.generated.resources.home_table
import tikoncha_parents.composeapp.generated.resources.home_task
import tikoncha_parents.composeapp.generated.resources.ilova_cheklangan
import tikoncha_parents.composeapp.generated.resources.instagram_icon
import tikoncha_parents.composeapp.generated.resources.linkedin_icon
import tikoncha_parents.composeapp.generated.resources.media_play
import tikoncha_parents.composeapp.generated.resources.support_icon
import tikoncha_parents.composeapp.generated.resources.topshiriqlar
import tikoncha_parents.composeapp.generated.resources.whatsapp_icon
import uz.tikoncha_parent.domain.model.HourMinute
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
import uz.tikoncha_parent.presentation.policy.policy_list.PolicyListScreen
import uz.tikoncha_parent.presentation.profile.ProfileScreen
import uz.tikoncha_parent.presentation.protection.ProtectionScreen
import uz.tikoncha_parent.presentation.statistic.StatisticScreen
import uz.tikoncha_parent.presentation.statistic.durationString
import uz.tikoncha_parent.presentation.task.TaskScreen
import uz.tikoncha_parent.presentation.tracking.TrackingScreen
import uz.tikoncha_parent.presentation.video_tutorial.TutorialType
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

    val systemBars = rememberScreenSystemBars(
        statusBarColor = AppColors.bg.page,
        navigationBarColor = AppColors.bg.page
    )

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
        message = stringResource(Res.string.farzand_malumotlari_keyin_korinadi),
        onDismiss = { showChildDialog = false },
        onButtonClick = {
            navigator?.push(AddChildScreen())
            showChildDialog = false
        }
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
                .then(systemBars.modifier)
                .background(AppColors.bg.page)
        ) {
            Row(
                modifier = Modifier
                    .background(Color.Transparent)
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {

                if (!showTikonchaTutorialCard) {
                    IconButton(
                        onClick = {
                            navigator?.push(VideoTutorialYoutubeScreen(TutorialType.TIKONCHA))
                        },
                        modifier = Modifier.size(44.dp),
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = AppColors.bg.surfaceTertiary,
                            contentColor = AppColors.icon.accentPrimary
                        )
                    ) {
                        Icon(
                            painter = painterResource(Res.drawable.media_play),
                            contentDescription = "",
                            modifier = Modifier.size(NormalIconSize)
                        )
                    }
                    SpaceUltraSmall()
                }
                IconButton(
                    onClick = {
                        openUrl("https://t.me/tikoncha_support")
                    },
                    modifier = Modifier.size(44.dp),
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = AppColors.bg.surfaceTertiary,
                        contentColor = AppColors.icon.accentPrimary
                    )
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.support_icon),
                        contentDescription = "",
                        modifier = Modifier.size(NormalIconSize)
                    )
                }
                Spacer(Modifier.weight(1f))

                ProfileCard(
                    modifier = Modifier.widthIn(140.dp, 160.dp),
                    name = state.userName,
                    imageUrl = state.userImageUrl?:"",
                    onClick = {
                        navigator?.push(ProfileScreen())
                    }
                )
            }

            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(0.dp),
            ) {

                item {
                    ChildSelectionButton(
                        text = state.selectedChild?.name ?: "",
                        imageUrl = state.selectedChild?.avatarUrl ?: "",
                        label = stringResource(Res.string.farzand_qo_shish),
                        trailingIcon = state.childrenList.isNotEmpty(),
                        userInfo = state.selectedChild,
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            if (state.childrenList.isEmpty()) {
                                navigator?.push(AddChildScreen())
                            } else {
                                showDialog = true
                            }
                        },
                    )
                    Space(16.dp)
                }

                item {
                    if (showTikonchaTutorialCard) {
                        TikonchaTutorialCard(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = {
                                navigator?.push(VideoTutorialYoutubeScreen(TutorialType.TIKONCHA))
                            }
                        )
                        Space(12.dp)
                    }
                }

                // NewHomeUi'da — hozirgi "Farzandingiz so'rovlari" item'i o'rnida:
                item {
                    ChildProtectionCard(
                        permissionOffCount = state.protectionPermissionOffCount,
                        pendingRequestCount = state.protectionPendingRequestCount,
                        onClick = {
                            if (state.childrenList.isEmpty()) {
                                internetCheck.check { showChildDialog = true }
                            } else {
                                navigator?.push(ProtectionScreen())
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Space(16.dp)
                }

//                item {
//                    if (parentRequestCount > 0) {
//                        Row(
//                            modifier = Modifier
//                                .fillMaxWidth()
//                                .simpleShadow(RoundedCornerShape(CardCornerRadius))
//                                .background(
//                                    MaterialTheme.extendedColor.cardColor,
//                                    RoundedCornerShape(CardCornerRadius)
//                                )
//                                .clip(RoundedCornerShape(CardCornerRadius))
//                                .clickable {
//                                    navigator?.push(ParentRequestScreen())
//                                }
//                                .padding(horizontal = CardCornerPadding, vertical = 8.dp),
//                            verticalAlignment = Alignment.CenterVertically
//                        ) {
//                            Text(
//                                text = stringResource(Res.string.farzandingiz_sorovlari),
//                                style = AppTypography.titleSmMedium,
//                                color = AppColors.text.primary,
//                                modifier = Modifier.weight(1f)
//                            )
//                            if (parentRequestCount > 0) {
//                                Box(
//                                    modifier = Modifier
//                                        .background(AppColors.bg.accentWarning, CircleShape)
//                                        .size(24.dp),
//                                    contentAlignment = Alignment.Center
//                                ) {
//                                    Text(
//                                        text = if (parentRequestCount > 99) "99" else parentRequestCount.toString(),
//                                        style = AppTypography.titleSmMedium,
//                                        color = AppColors.text.inverse,
//                                        maxLines = 1,
//                                    )
//                                }
//                            }
//                        }
//                        Space(12.dp)
//                    }
//                }

                item {
                    InAppUpdateCard(
                        modifier = Modifier.padding(bottom = 16.dp),
                        state = appUpdateState,
                        event = appUpdateEvent
                    )
                }

                /* ============ STATISTIKA card ============ */
                item {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(LargeCardCornerRadius))
                            .fillMaxWidth()
                            .height(HomeItemHeight)
                            .background(
                                AppColors.section.tertiary,
                                RoundedCornerShape(LargeCardCornerRadius)
                            )
                            .singleClick {
                                if (state.childrenList.isEmpty()) {
                                    internetCheck.check { showChildDialog = true }
                                } else {
                                    navigator?.push(StatisticScreen())
                                }
                            }
                            .padding(CardCornerPadding),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = durationString(state.todayUsage),
                                color = AppColors.text.primary,
                                style = AppTypography.displaySmRegular
                            )
                            Text(
                                text = stringResource(Res.string.bugun_sarfladi),
                                color = AppColors.text.secondary,
                                style = AppTypography.titleSmMedium,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.Bottom,
                            modifier = Modifier.padding(end = 14.5.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight(0.5f)
                                    .background(
                                        MaterialTheme.extendedColor.backgroundColor,
                                        RoundedCornerShape(LargeCardCornerRadius)
                                    )
                                    .padding(6.dp),
                                contentAlignment = Alignment.BottomCenter
                            ) {
                                Image(
                                    painter = painterResource(Res.drawable.linkedin_icon),
                                    contentDescription = "",
                                    modifier = Modifier.size(SmallIconSize),
                                    alignment = Alignment.BottomCenter,
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight(0.65f)
                                    .background(
                                        MaterialTheme.extendedColor.backgroundColor,
                                        RoundedCornerShape(TextFieldCornerRadius)
                                    )
                                    .padding(6.dp),
                                contentAlignment = Alignment.BottomCenter
                            ) {
                                Image(
                                    painter = painterResource(Res.drawable.whatsapp_icon),
                                    contentDescription = "",
                                    modifier = Modifier.size(SmallIconSize),
                                    alignment = Alignment.BottomCenter,
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight(0.8f)
                                    .background(
                                        MaterialTheme.extendedColor.backgroundColor,
                                        RoundedCornerShape(TextFieldCornerRadius)
                                    )
                                    .padding(6.dp),
                                contentAlignment = Alignment.BottomCenter
                            ) {
                                Image(
                                    painter = painterResource(Res.drawable.instagram_icon),
                                    contentDescription = "",
                                    modifier = Modifier.size(SmallIconSize),
                                    alignment = Alignment.BottomCenter,
                                )
                            }
                        }
                    }
                    Space(12.dp)
                }

                /* ============ CHEKLOVLAR card ============ */
                item {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(LargeCardCornerRadius))
                            .fillMaxWidth()
                            .height(HomeItemHeight)
                            .background(
                                AppColors.section.tertiary,
                                RoundedCornerShape(LargeCardCornerRadius)
                            )
                            .singleClick {
                                if (state.childrenList.isEmpty()) {
                                    internetCheck.check { showChildDialog = true }
                                } else {
                                    navigator?.push(PolicyListScreen())
                                }
                            }
                            .padding(horizontal = CardCornerPadding, vertical = ContainerPadding),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(Res.string.cheklovlar),
                                color = AppColors.text.primary,
                                style = AppTypography.displaySmRegular
                            )
                            Text(
                                text = stringResource(Res.string.ilova_cheklangan, tableCount),
                                color = AppColors.text.secondary,
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
                    Space(12.dp)
                }

                item {
                    NewHomeItem(
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
                            .clip(RoundedCornerShape(LargeCardCornerRadius))
                            .fillMaxWidth()
                            .height(HomeItemHeight)
                            .background(
                                AppColors.section.tertiary,
                                RoundedCornerShape(LargeCardCornerRadius)
                            )
                            .singleClick {
                                if (state.childrenList.isEmpty()) {
                                    internetCheck.check { showChildDialog = true }
                                } else {
                                    navigator?.push(TaskScreen())
                                }
                            }
                            .padding(horizontal = CardCornerPadding, vertical = ContainerPadding),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(Res.string.topshiriqlar),
                                color = AppColors.text.primary,
                                style = AppTypography.displaySmRegular
                            )
                            Text(
                                text = stringResource(Res.string.faol_vazifa, taskCount),
                                color = AppColors.text.secondary,
                                style = AppTypography.titleSmMedium,
                            )
                        }

                        Image(
                            painter = painterResource(Res.drawable.home_task),
                            contentDescription = "",
                            modifier = Modifier.size(HomeIconSize)
                        )
                    }
                    Space(12.dp)
                }
            }
        }
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