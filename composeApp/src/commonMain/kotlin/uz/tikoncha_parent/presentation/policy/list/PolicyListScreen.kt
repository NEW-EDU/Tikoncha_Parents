package uz.tikoncha_parent.presentation.policy.list

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.core.annotation.InternalVoyagerApi
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.internal.BackHandler
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import tikoncha_parents.composeapp.generated.resources.Res
import tikoncha_parents.composeapp.generated.resources.dialog_failed
import tikoncha_parents.composeapp.generated.resources.farzandingizni_tanlang
import tikoncha_parents.composeapp.generated.resources.farzandlaringiz
import tikoncha_parents.composeapp.generated.resources.jadval_yaratish
import tikoncha_parents.composeapp.generated.resources.jadvallar
import tikoncha_parents.composeapp.generated.resources.policy_empty_mine
import tikoncha_parents.composeapp.generated.resources.policy_enabled_count
import tikoncha_parents.composeapp.generated.resources.policy_paywall_count
import tikoncha_parents.composeapp.generated.resources.policy_paywall_protection
import tikoncha_parents.composeapp.generated.resources.policy_quick_blocks
import tikoncha_parents.composeapp.generated.resources.stat_quick_paywall_message
import tikoncha_parents.composeapp.generated.resources.stat_quick_paywall_title
import tikoncha_parents.composeapp.generated.resources.xatolik
import uz.tikoncha_parent.presentation.add_child.AddChildScreen
import uz.tikoncha_parent.presentation.base.ChildSelectionButton
import uz.tikoncha_parent.presentation.base.CustomButtonNew
import uz.tikoncha_parent.presentation.base.CustomDialog
import uz.tikoncha_parent.presentation.base.CustomHeader
import uz.tikoncha_parent.presentation.base.PullToRefreshBox
import uz.tikoncha_parent.presentation.base.SubscriptionBottomDialog
import uz.tikoncha_parent.presentation.base.asText
import uz.tikoncha_parent.presentation.base.haptics.ErrorHaptic
import uz.tikoncha_parent.presentation.new_home.SelectionChildBottomSheet
import uz.tikoncha_parent.presentation.policy.components.ContentProtectionCard
import uz.tikoncha_parent.presentation.policy.components.EmptyHint
import uz.tikoncha_parent.presentation.policy.components.PolicyCard
import uz.tikoncha_parent.presentation.policy.components.PolicyInfoBox
import uz.tikoncha_parent.presentation.policy.components.PolicyText
import uz.tikoncha_parent.presentation.policy.components.PresetPolicyCard
import uz.tikoncha_parent.presentation.policy.components.QuickBlockCard
import uz.tikoncha_parent.presentation.policy.components.SectionTitle
import uz.tikoncha_parent.presentation.policy.components.SegmentedTabs
import uz.tikoncha_parent.presentation.policy.components.lines
import uz.tikoncha_parent.presentation.policy.components.title
import uz.tikoncha_parent.presentation.policy.model.PayWallReason
import uz.tikoncha_parent.presentation.policy.model.PolicyCardUi
import uz.tikoncha_parent.presentation.policy.model.PolicyTab
import uz.tikoncha_parent.presentation.policy.protection_packs.ProtectionPacksScreen
import uz.tikoncha_parent.presentation.policy.quick.QuickBlockScreen
import uz.tikoncha_parent.presentation.profile.subscription.subscription_payment.SubscriptionPaymentScreen
import uz.tikoncha_parent.ui.ContainerPadding
import uz.tikoncha_parent.ui.theme.AppColors
import uz.tikoncha_parent.ui.theme.rememberScreenSystemBars

/**
 * Jadvallar — Student ilovasidagi ro'yxat bilan bir xil: hisob, izoh qutisi, yopishqoq
 * tablar (M3 Expressive), shablonlar / meniki / farzand / ikkinchi ota-ona / maktab.
 * Parent'ga xos: sarlavha o'ngida farzand tanlash.
 */
@OptIn(InternalVoyagerApi::class)
class PolicyListScreen : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.current ?: return
        val viewModel = koinScreenModel<PolicyListViewModel>()
        val state by viewModel.state.collectAsStateWithLifecycle()

        LaunchedEffect(Unit) { viewModel.onEvent(PolicyListEvent.Load) }
        LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { viewModel.onEvent(PolicyListEvent.Resumed) }
        BackHandler(true) { navigator.pop() }

        LaunchedEffect(Unit) {
            viewModel.effect.collect { e ->
                when (e) {
                    is PolicyListEffect.OpenQuickBlock -> navigator.push(QuickBlockScreen(e.childId))
                    is PolicyListEffect.OpenProtection -> navigator.push(ProtectionPacksScreen())
                    // Tayyor jadval va o'zim yaratgan jadval ekranlari keyingi qadamlarda ulanadi
                    is PolicyListEffect.OpenPreset, is PolicyListEffect.OpenPolicy, is PolicyListEffect.OpenCreate -> Unit
                }
            }
        }

        PolicyListUi(navigator = navigator, state = state, event = viewModel::onEvent)
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PolicyListUi(navigator: Navigator?, state: PolicyListState, event: (PolicyListEvent) -> Unit) {
    val systemBars = rememberScreenSystemBars(statusBarColor = AppColors.bg.page, navigationBarColor = AppColors.bg.page)
    var showChildSheet by remember { mutableStateOf(false) }

    if (showChildSheet) {
        SelectionChildBottomSheet(
            navigator = navigator,
            items = state.children,
            selectedItem = state.selectedChild,
            title = stringResource(Res.string.farzandlaringiz),
            onDismiss = { showChildSheet = false },
            onItemSelected = {
                event(PolicyListEvent.ChildSelected(it))
                showChildSheet = false
            },
        )
    }

    Column(modifier = Modifier.fillMaxSize().background(AppColors.bg.page).then(systemBars.modifier)) {
        CustomHeader(
            title = stringResource(Res.string.jadvallar),
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
                        if (state.children.isEmpty()) navigator?.push(AddChildScreen()) else showChildSheet = true
                    },
                )
            },
        )

        PullToRefreshBox(
            isRefreshing = state.isRefreshing,
            onRefresh = { event(PolicyListEvent.Refresh) },
            modifier = Modifier.weight(1f),
        ) {
            // Hisob va izoh ro'yxat bilan birga yuqoriga ketadi; tablar sarlavha ostida yopishib qoladi
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = ContainerPadding, end = ContainerPadding, bottom = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                item(key = "count") {
                    Text(
                        text = stringResource(Res.string.policy_enabled_count, state.enabledCount),
                        style = PolicyText.count,
                        color = AppColors.text.tertiary,
                    )
                }
                item(key = "info") { PolicyInfoBox(title = state.info.title(), lines = state.info.lines()) }
                stickyHeader(key = "tabs") {
                    SegmentedTabs(
                        labels = state.tabs.map { it.title() },
                        selectedIndex = state.tabs.indexOf(state.tab),
                        onSelect = { event(PolicyListEvent.TabSelected(state.tabs[it])) },
                        modifier = Modifier.fillMaxWidth().background(AppColors.bg.page).padding(top = 4.dp, bottom = 8.dp),
                    )
                }

                when (state.tab) {
                    PolicyTab.TEMPLATES -> templates(state, event)
                    PolicyTab.MINE -> {
                        if (state.mine.isEmpty()) item(key = "empty") { EmptyHint(stringResource(Res.string.policy_empty_mine)) }
                        cards(state.mine, state, event)
                    }
                    PolicyTab.CHILD -> othersTab(state.child, state.childQuick, state, event)
                    PolicyTab.COPARENT -> othersTab(state.coParent, state.coParentQuick, state, event)
                    PolicyTab.SCHOOL -> cards(state.school, state, event)
                }
            }
        }

        if (state.tab == PolicyTab.MINE) {
            CustomButtonNew(
                text = stringResource(Res.string.jadval_yaratish),
                modifier = Modifier.fillMaxWidth().padding(horizontal = ContainerPadding, vertical = 12.dp),
                onClick = { event(PolicyListEvent.CreateClicked) },
            )
        }
    }

    SubscriptionBottomDialog(
        show = state.payWall != null,
        title = stringResource(Res.string.stat_quick_paywall_title),
        message = stringResource(
            when (state.payWall) {
                PayWallReason.QUICK_BLOCK -> Res.string.stat_quick_paywall_message
                PayWallReason.PROTECTION -> Res.string.policy_paywall_protection
                else -> Res.string.policy_paywall_count
            }
        ),
        onConfirm = {
            event(PolicyListEvent.PayWallDismissed)
            navigator?.push(SubscriptionPaymentScreen())
        },
        onDismiss = { event(PolicyListEvent.PayWallDismissed) },
    )

    val error = state.error
    ErrorHaptic(error)
    CustomDialog(
        painter = painterResource(Res.drawable.dialog_failed),
        show = error != null,
        title = stringResource(Res.string.xatolik),
        message = error?.asText().orEmpty(),
        onDismiss = { event(PolicyListEvent.ErrorDismissed) },
        onButtonClick = { event(PolicyListEvent.ErrorDismissed) },
    )
}

private fun LazyListScope.templates(state: PolicyListState, event: (PolicyListEvent) -> Unit) {
    item(key = "protection") {
        ContentProtectionCard(
            ui = state.protection,
            busy = state.isBusy(PolicyListViewModel.KEY_PROTECTION),
            onToggle = { event(PolicyListEvent.ProtectionToggled(it)) },
            onClick = { event(PolicyListEvent.ProtectionClicked) },
        )
    }
    items(state.presets, key = { it.kind.name }) { preset ->
        val title = preset.kind.title()
        PresetPolicyCard(
            ui = preset,
            busy = state.isBusy(PolicyListViewModel.presetKey(preset.kind)),
            onToggle = { event(PolicyListEvent.PresetToggled(preset.kind, it, title)) },
            onClick = { event(PolicyListEvent.PresetClicked(preset.kind)) },
        )
    }
    item(key = "quick") {
        QuickBlockCard(
            ui = state.quickBlock,
            busy = state.isBusy(PolicyListViewModel.KEY_QUICK),
            onToggle = { event(PolicyListEvent.QuickBlockToggled(it)) },
            onClick = { event(PolicyListEvent.QuickBlockClicked) },
        )
    }
}

/** Boshqa odamniki: jadvallar, keyin "Tezkor bloklar" bo'limi (Student'dagi ota-ona tabi kabi). */
private fun LazyListScope.othersTab(policies: List<PolicyCardUi>, quick: List<PolicyCardUi>, state: PolicyListState, event: (PolicyListEvent) -> Unit) {
    cards(policies, state, event)
    if (quick.isNotEmpty()) {
        item(key = "others-quick") { SectionTitle(stringResource(Res.string.policy_quick_blocks)) }
        items(quick, key = { "q-" + it.policyId }) { card ->
            PolicyCard(ui = card, busy = false, onToggle = {}, onClick = { event(PolicyListEvent.QuickBlockClicked) })
        }
    }
}

private fun LazyListScope.cards(list: List<PolicyCardUi>, state: PolicyListState, event: (PolicyListEvent) -> Unit) {
    items(list, key = { it.policyId }) { card ->
        PolicyCard(
            ui = card,
            busy = state.isBusy(card.policyId),
            onToggle = { event(PolicyListEvent.PolicyToggled(card.policyId, it)) },
            onClick = { event(PolicyListEvent.PolicyClicked(card.policyId)) },
        )
    }
}
