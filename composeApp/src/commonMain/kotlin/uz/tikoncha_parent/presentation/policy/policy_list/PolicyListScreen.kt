package uz.tikoncha_parent.presentation.policy.policy_list

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.core.annotation.InternalVoyagerApi
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.internal.BackHandler
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import tikoncha_parents.composeapp.generated.resources.Res
import tikoncha_parents.composeapp.generated.resources.cheklovlar
import tikoncha_parents.composeapp.generated.resources.dialog_failed
import tikoncha_parents.composeapp.generated.resources.farzand_qo_shish
import tikoncha_parents.composeapp.generated.resources.farzandingiz
import tikoncha_parents.composeapp.generated.resources.farzandlaringiz
import tikoncha_parents.composeapp.generated.resources.jadval_limit_tugadi_plus
import tikoncha_parents.composeapp.generated.resources.jadval_qoshish
import tikoncha_parents.composeapp.generated.resources.limit_tugadi
import tikoncha_parents.composeapp.generated.resources.maktab_umumiy
import tikoncha_parents.composeapp.generated.resources.media_play
import tikoncha_parents.composeapp.generated.resources.siz
import tikoncha_parents.composeapp.generated.resources.xatolik
import uz.tikoncha_parent.domain.model.app_error.Outcome
import uz.tikoncha_parent.domain.model.permission_status.PermissionIssue
import uz.tikoncha_parent.domain.model.policy.PolicyEffectiveState
import uz.tikoncha_parent.platform.openUrl
import uz.tikoncha_parent.presentation.add_child.AddChildScreen
import uz.tikoncha_parent.presentation.base.ChildSelectionButton
import uz.tikoncha_parent.presentation.base.CustomButtonNew
import uz.tikoncha_parent.presentation.base.CustomDialog
import uz.tikoncha_parent.presentation.base.CustomHeader
import uz.tikoncha_parent.presentation.base.LoadingDialog
import uz.tikoncha_parent.presentation.base.NoInternetDialog
import uz.tikoncha_parent.presentation.base.PermissionWarningCard
import uz.tikoncha_parent.presentation.base.PillSegmentedButton
import uz.tikoncha_parent.presentation.base.PillSegmentedButtonDefaults
import uz.tikoncha_parent.presentation.base.PillSegmentedItem
import uz.tikoncha_parent.presentation.base.SubscriptionBottomDialog
import uz.tikoncha_parent.presentation.base.asText
import uz.tikoncha_parent.presentation.base.rememberInternetCheck
import uz.tikoncha_parent.presentation.base.simpleShadow
import uz.tikoncha_parent.presentation.new_home.SelectionChildBottomSheet
import uz.tikoncha_parent.presentation.policy.policy_setup.PolicySetupScreen
import uz.tikoncha_parent.presentation.policy.protection_packs.ProtectionEntrySection
import uz.tikoncha_parent.presentation.policy.protection_packs.ProtectionPacksScreen
import uz.tikoncha_parent.presentation.policy.shared.PolicySharedEvent
import uz.tikoncha_parent.presentation.policy.shared.PolicySharedModel
import uz.tikoncha_parent.presentation.policy.shared.PolicySharedState
import uz.tikoncha_parent.presentation.profile.subscription.subscription_payment.SubscriptionPaymentScreen
import uz.tikoncha_parent.presentation.ui_state.ResponseState
import uz.tikoncha_parent.presentation.ui_state.errorText
import uz.tikoncha_parent.presentation.video_tutorial.TutorialType
import uz.tikoncha_parent.presentation.video_tutorial.VideoTutorialYoutubeScreen
import uz.tikoncha_parent.ui.NormalIconSize
import uz.tikoncha_parent.ui.Space
import uz.tikoncha_parent.ui.SpaceUltraSmall
import uz.tikoncha_parent.ui.theme.AppColors
import uz.tikoncha_parent.ui.theme.ThemeMode
import uz.tikoncha_parent.ui.theme.TikonchaParentTheme
import uz.tikoncha_parent.ui.theme.rememberScreenSystemBars

@OptIn(InternalVoyagerApi::class)
class PolicyListScreen : Screen {
    @Composable
    override fun Content() {

        val navigator = LocalNavigator.current?:return


        val sharedViewModel = koinViewModel<PolicySharedModel>()
        val sharedState by sharedViewModel.state.collectAsStateWithLifecycle()
        val sharedEvent = sharedViewModel::onEvent


        val viewModel = koinScreenModel<PolicyViewModel>()
        val event = viewModel::onEvent
        val state by viewModel.state.collectAsStateWithLifecycle()

        LaunchedEffect(Unit) {
            event(PolicyEvent.RefreshPolicies)
            event(PolicyEvent.GetChildren)
        }

        BackHandler(true) {
            navigator.pop()
        }


        PolicyListUi(
            navigator = navigator,
            sharedEvent = sharedEvent,
            event = event,
            effect = viewModel.effect,
            state = state,
            sharedState = sharedState,
        )
    }
}

@Composable
fun PolicyListUi(
    navigator: Navigator?,
    state: PolicyState,
    sharedState: PolicySharedState,
    event: (PolicyEvent) -> Unit = {},
    effect: Flow<PolicyListEffect> = emptyFlow(),
    sharedEvent: (PolicySharedEvent) -> Unit = {},
) {
    val errorText = state.policyResponseState.errorText()
    var showErrorText by remember { mutableStateOf(false) }
    val loading = state.policyResponseState is ResponseState.Loading
    var showPolicyLimitDialog by remember { mutableStateOf(false) }
    var isRefreshing by remember { mutableStateOf(false) }
    val refreshScope = rememberCoroutineScope()
    val internetCheck = rememberInternetCheck(refreshScope)
    var showDialog by remember { mutableStateOf(false) }

    var actionFailure by remember { mutableStateOf<Outcome.Failure?>(null) }
    var premiumFailure by remember { mutableStateOf<Outcome.Failure?>(null) }

    LaunchedEffect(Unit) {
        effect.collect { eff ->
            when (eff) {
                is PolicyListEffect.ShowError -> actionFailure = eff.failure
                is PolicyListEffect.ShowPremium -> premiumFailure = eff.failure
            }
        }
    }

    val systemBars = rememberScreenSystemBars(
        statusBarColor = AppColors.bg.secondary,
        navigationBarColor = AppColors.bg.surface
    )


    LoadingDialog(loading)

    NoInternetDialog(internetCheck)


    LaunchedEffect(errorText) { showErrorText = errorText.isNotEmpty() }

    if (showDialog) {
        SelectionChildBottomSheet(
            navigator = navigator,
            items = state.childrenList,
            onDismiss = { showDialog = false },
            selectedItem = state.selectedChild,
            title = stringResource(Res.string.farzandlaringiz),
            onItemSelected = {
                event(PolicyEvent.OnChildSelected(it))
                showDialog = false
            }
        )
    }

    CustomDialog(
        painter = painterResource(Res.drawable.dialog_failed),
        title = stringResource(Res.string.xatolik),
        message = errorText,
        show = showErrorText,
        onDismiss = {
            showErrorText = false
        },
        onButtonClick = {
            showErrorText = false
        }
    )

    SubscriptionBottomDialog(
        show = showPolicyLimitDialog,
        title = stringResource(Res.string.limit_tugadi),
        message = stringResource(Res.string.jadval_limit_tugadi_plus),
        onConfirm = {
            showPolicyLimitDialog = false
            navigator?.push(SubscriptionPaymentScreen())
        },
        onDismiss = {
            showPolicyLimitDialog = false
        }
    )

    // Toggle / pauza xatosi — server matni asText() orqali
    CustomDialog(
        painter = painterResource(Res.drawable.dialog_failed),
        title = stringResource(Res.string.xatolik),
        message = actionFailure?.asText().orEmpty(),
        show = actionFailure != null,
        onDismiss = { actionFailure = null },
        onButtonClick = { actionFailure = null },
    )

    SubscriptionBottomDialog(
        show = premiumFailure != null,
        message = premiumFailure?.asText().orEmpty(),
        onConfirm = {
            premiumFailure = null
            navigator?.push(SubscriptionPaymentScreen())
        },
        onDismiss = { premiumFailure = null },
    )

    state.pauseSheetFor?.let { policyId ->
        val policy = state.policies.firstOrNull { it.policyId == policyId }
        PausePolicySheet(
            isPaused = policy?.effectiveState == PolicyEffectiveState.PAUSED,
            onSelect = { option -> event(PolicyEvent.PausePolicy(policyId, option)) },
            onResume = { event(PolicyEvent.ResumePolicy(policyId)) },
            onDismiss = { event(PolicyEvent.OpenPauseSheet(null)) },
        )
    }

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = {
            internetCheck.check {
                isRefreshing = true
                event(PolicyEvent.RefreshPolicies)
                delay(500)
                isRefreshing = false
            }
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .then(systemBars.modifier)
                .background(AppColors.bg.secondary)
        ) {

            CustomHeader(
                title = stringResource(Res.string.cheklovlar),
                showBackButton = true,
                onBackClick = {
                    navigator?.pop()
                },
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    if (!state.showPolicyTutorialCard){
                        IconButton(
                            onClick = {
                                navigator?.push(VideoTutorialYoutubeScreen(TutorialType.POLICY))
                            },
                            modifier = Modifier
                                .size(44.dp),
                            colors = IconButtonDefaults.iconButtonColors(
                                containerColor = AppColors.bg.surfaceTertiary,
                                contentColor = AppColors.icon.accentPrimary
                            )
                        ) {
                            Icon(
                                painter = painterResource(Res.drawable.media_play),
                                contentDescription = "",
                                modifier = Modifier
                                    .size(NormalIconSize)
                            )
                        }
                        SpaceUltraSmall()
                    }
                }
            )

            val siz = stringResource(Res.string.siz)
            val farzandingiz = stringResource(Res.string.farzandingiz)
            val maktab = stringResource(Res.string.maktab_umumiy)
            val options by remember {
                mutableStateOf(
                    listOf(
                        PillSegmentedItem(
                            label = siz
                        ),
                        PillSegmentedItem(
                            label = farzandingiz
                        ),
                        PillSegmentedItem(
                            label = maktab
                        )

                    )
                )
            }
            PillSegmentedButton(
                items = options,
                selectedIndex = state.selectedTypeIndex,
                onSelected = {
                    event(PolicyEvent.OnTypeSelected(it))
                },
                height = 36.dp,
                colors = PillSegmentedButtonDefaults.colors(
                    trackColor = Color.Transparent,
                ),
                trackBorder = BorderStroke(
                    1.dp,
                    color = AppColors.border.secondarySubtle
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = 16.dp,
                        end = 16.dp,
                        top = 10.dp
                    )
            )



            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(10.dp),
                verticalArrangement = Arrangement.spacedBy(15.dp)
            ) {

                item {

                    if (state.showPolicyTutorialCard){
                        Space(16.dp)
                        PolicyTutorialCard {
                            navigator?.push(VideoTutorialYoutubeScreen(TutorialType.POLICY))
                        }
                    }

                    Space(16.dp)
                    ChildSelectionButton(
                        text = state.selectedChild?.name ?: "",
                        imageUrl = state.selectedChild?.avatarUrl ?: "",
                        label = stringResource(Res.string.farzand_qo_shish),
                        trailingIcon = state.childrenList.isNotEmpty(),
                        userInfo = state.selectedChild,
                        onClick = {
                            if (state.childrenList.isEmpty()) {
                                navigator?.push(AddChildScreen())
                            } else {
                                showDialog = true
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(42.dp)
                    )
                }

                item {

                    if (state.permissionIssueList.isNotEmpty()) {
                        val issue = state.permissionIssueList.first()
                        PermissionWarningCard(
                            title = issue.title,
                            body = issue.body,
                            videoUrl = issue.videoUrl,
                            onVideoClick = { url ->
                                openUrl(url)
                            }
                        )
                        Space(15.dp)
                    }

                    if (state.filteredPolicies.isEmpty() && !loading && state.selectedTypeIndex == 0) {
                        CreatePolicyCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .simpleShadow(),
                            onClick = {
                                if (state.canCreatePolicy) {
                                    sharedEvent(PolicySharedEvent.ClearData)
                                    sharedEvent(PolicySharedEvent.SetSubscriptionLimit(state.subscriptionLimit))
                                    state.selectedChild?.let { child ->
                                        sharedEvent(
                                            PolicySharedEvent.SetSelectedChild(child)
                                        )
                                    }
                                    navigator?.push(PolicySetupScreen())
                                } else {
                                    showPolicyLimitDialog = true
                                }
                            }
                        )
                    }
                }

                items(
                    items = state.filteredPolicies,
                    key = { it.policyId }
                ) {
                    PolicyListItem(
                        modifier = Modifier,
                        policy = it,
                        busy = it.policyId in state.actionInProgress,
                        onClick = {
                            sharedEvent(PolicySharedEvent.ClearData)
                            state.selectedChild?.let { child ->
                                sharedEvent(PolicySharedEvent.SetSelectedChild(child))
                            }
                            sharedEvent(PolicySharedEvent.SetSubscriptionLimit(state.subscriptionLimit))
                            sharedEvent(PolicySharedEvent.SetPolicy(it))
                            navigator?.push(PolicySetupScreen())
                        },
                        onToggle = { enabled ->
                            event(PolicyEvent.TogglePolicy(it.policyId, enabled))
                        },
                        onMoreClick = {
                            event(PolicyEvent.OpenPauseSheet(it.policyId))
                        },
                    )
                }


                if (state.visibleQuickBlocks.isNotEmpty()) {
                    item(key = "quick_blocks") {
                        QuickBlockSection(
                            entries = state.visibleQuickBlocks,
                            myUserId = state.myUserId,
                            apps = state.childApps,
                            isBusy = { pkg -> state.isQuickBlockBusy(pkg) },
                            onRemove = { pkg -> event(PolicyEvent.RemoveQuickBlock(pkg)) },
                        )
                    }
                }

                item(key = "protection") {
                    ProtectionEntrySection(
                        enabledNames = state.enabledProtectionNames,
                        onClick = { navigator?.push(ProtectionPacksScreen()) },
                    )
                }
            }

            if (state.policies.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            AppColors.bg.elevated,
                            RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                        )
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                ) {
                    CustomButtonNew(
                        modifier = Modifier.fillMaxWidth(),
                        text = stringResource(Res.string.jadval_qoshish),
                        onClick = {
                            if (state.canCreatePolicy) {
                                sharedEvent(PolicySharedEvent.ClearData)
                                state.selectedChild?.let { child ->
                                    sharedEvent(PolicySharedEvent.SetSelectedChild(child))
                                }
                                sharedEvent(PolicySharedEvent.SetSubscriptionLimit(state.subscriptionLimit))
                                navigator?.push(PolicySetupScreen())
                            } else {
                                showPolicyLimitDialog = true
                            }
                        },
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun Pre() {
    TikonchaParentTheme(
        ThemeMode.LIGHT
    ) {
        PolicyListUi(
            navigator = null,
            state = PolicyState(
                permissionIssueList = listOf(
                    PermissionIssue(
                        state = "",
                        missingPermissions = emptyList(),
                        title = "Issue",
                        body = "Big issue",
                        videoUrl = "sdsds"
                    )
                )
            ),
            sharedState = PolicySharedState(),
            event = {},
        )
    }
}