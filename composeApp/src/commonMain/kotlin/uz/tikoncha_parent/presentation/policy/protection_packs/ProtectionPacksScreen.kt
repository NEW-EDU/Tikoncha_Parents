package uz.tikoncha_parent.presentation.policy.protection_packs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.core.annotation.InternalVoyagerApi
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.internal.BackHandler
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import tikoncha_parents.composeapp.generated.resources.Res
import tikoncha_parents.composeapp.generated.resources.dialog_failed
import tikoncha_parents.composeapp.generated.resources.farzand_qo_shish
import tikoncha_parents.composeapp.generated.resources.farzandlaringiz
import tikoncha_parents.composeapp.generated.resources.himoya_paketlari
import tikoncha_parents.composeapp.generated.resources.xatolik
import uz.tikoncha_parent.domain.model.app_error.Outcome
import uz.tikoncha_parent.presentation.add_child.AddChildScreen
import uz.tikoncha_parent.presentation.base.ChildSelectionButton
import uz.tikoncha_parent.presentation.base.CustomDialog
import uz.tikoncha_parent.presentation.base.CustomHeader
import uz.tikoncha_parent.presentation.base.LoadingDialog
import uz.tikoncha_parent.presentation.base.NoInternetDialog
import uz.tikoncha_parent.presentation.base.PullToRefreshBox
import uz.tikoncha_parent.presentation.base.SubscriptionBottomDialog
import uz.tikoncha_parent.presentation.base.asText
import uz.tikoncha_parent.presentation.base.rememberInternetCheck
import uz.tikoncha_parent.presentation.new_home.SelectionChildBottomSheet
import uz.tikoncha_parent.presentation.profile.subscription.subscription_payment.SubscriptionPaymentScreen
import uz.tikoncha_parent.presentation.ui_state.ResponseState
import uz.tikoncha_parent.presentation.ui_state.errorText
import uz.tikoncha_parent.ui.theme.AppColors
import uz.tikoncha_parent.ui.theme.rememberScreenSystemBars

@OptIn(InternalVoyagerApi::class)
class ProtectionPacksScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.current ?: return
        val viewModel = koinScreenModel<ProtectionPacksViewModel>()
        val state by viewModel.state.collectAsStateWithLifecycle()

        LaunchedEffect(Unit) { viewModel.onEvent(ProtectionPacksEvent.Load) }
        BackHandler(true) { navigator.pop() }

        ProtectionPacksUi(
            navigator = navigator,
            state = state,
            event = viewModel::onEvent,
            effect = viewModel.effect,
        )
    }
}

@Composable
fun ProtectionPacksUi(
    navigator: Navigator?,
    state: ProtectionPacksState,
    event: (ProtectionPacksEvent) -> Unit = {},
    effect: Flow<ProtectionPacksEffect> = emptyFlow(),
) {
    val refreshScope = rememberCoroutineScope()
    val internetCheck = rememberInternetCheck(refreshScope)
    var showChildSheet by remember { mutableStateOf(false) }
    var showLoadError by remember { mutableStateOf(false) }
    var actionFailure by remember { mutableStateOf<Outcome.Failure?>(null) }

    val loadErrorText = state.responseState.errorText()
    val loading = state.responseState is ResponseState.Loading && !state.isRefreshing

    val systemBars = rememberScreenSystemBars(
        statusBarColor = AppColors.bg.secondary,
        navigationBarColor = AppColors.bg.secondary,
    )

    LoadingDialog(loading)
    NoInternetDialog(internetCheck)

    LaunchedEffect(loadErrorText) { showLoadError = loadErrorText.isNotEmpty() }

    LaunchedEffect(Unit) {
        effect.collect { eff ->
            when (eff) {
                is ProtectionPacksEffect.ShowError -> actionFailure = eff.failure
            }
        }
    }

    if (showChildSheet) {
        SelectionChildBottomSheet(
            navigator = navigator,
            items = state.childrenList,
            selectedItem = state.selectedChild,
            onDismiss = { showChildSheet = false },
            title = stringResource(Res.string.farzandlaringiz),
            onItemSelected = {
                event(ProtectionPacksEvent.OnChildSelected(it))
                showChildSheet = false
            },
        )
    }

    // Yuklash xatosi
    CustomDialog(
        painter = painterResource(Res.drawable.dialog_failed),
        title = stringResource(Res.string.xatolik),
        message = loadErrorText,
        show = showLoadError,
        onDismiss = { showLoadError = false },
        onButtonClick = { showLoadError = false },
    )

    // Yoqish/o'chirish xatosi
    CustomDialog(
        painter = painterResource(Res.drawable.dialog_failed),
        title = stringResource(Res.string.xatolik),
        message = actionFailure?.asText().orEmpty(),
        show = actionFailure != null,
        onDismiss = { actionFailure = null },
        onButtonClick = { actionFailure = null },
    )

    SubscriptionBottomDialog(
        show = state.premiumFailure != null,
        message = state.premiumFailure?.asText().orEmpty(),
        onConfirm = {
            event(ProtectionPacksEvent.DismissPremium)
            navigator?.push(SubscriptionPaymentScreen())
        },
        onDismiss = { event(ProtectionPacksEvent.DismissPremium) },
    )

    PullToRefreshBox(
        isRefreshing = state.isRefreshing,
        onRefresh = {
            internetCheck.check { event(ProtectionPacksEvent.PullRefresh) }
        },
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .then(systemBars.modifier)
                .background(AppColors.bg.secondary),
        ) {
            CustomHeader(
                title = stringResource(Res.string.himoya_paketlari),
                showBackButton = true,
                onBackClick = { navigator?.pop() },
                modifier = Modifier.fillMaxWidth(),
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(10.dp),
                verticalArrangement = Arrangement.spacedBy(15.dp),
            ) {
                item {
                    ChildSelectionButton(
                        text = state.selectedChild?.name ?: "",
                        imageUrl = state.selectedChild?.avatarUrl ?: "",
                        label = stringResource(Res.string.farzand_qo_shish),
                        trailingIcon = state.childrenList.isNotEmpty(),
                        userInfo = state.selectedChild,
                        onClick = {
                            if (state.childrenList.isEmpty()) navigator?.push(AddChildScreen())
                            else showChildSheet = true
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(42.dp),
                    )
                }

                items(items = state.packs, key = { it.code }) { pack ->
                    ProtectionPackItem(
                        pack = pack,
                        showPremiumBadge = !state.isPaid,
                        onToggle = { enabled ->
                            event(ProtectionPacksEvent.Toggle(pack.code, enabled))
                        },
                    )
                }
            }
        }
    }
}