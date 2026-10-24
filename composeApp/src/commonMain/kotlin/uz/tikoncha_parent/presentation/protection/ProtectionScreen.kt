package uz.tikoncha_parent.presentation.protection

import uz.tikoncha_parent.presentation.base.island.ChildPickerSpec
import androidx.compose.foundation.background
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import tikoncha_parents.composeapp.generated.resources.qalqonni_ochirish
import tikoncha_parents.composeapp.generated.resources.rad_etish
import tikoncha_parents.composeapp.generated.resources.ruxsat_berish
import tikoncha_parents.composeapp.generated.resources.tasdiqlash
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import uz.tikoncha_parent.presentation.base.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.Navigator
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import tikoncha_parents.composeapp.generated.resources.Res
import tikoncha_parents.composeapp.generated.resources.dialog_failed
import tikoncha_parents.composeapp.generated.resources.dialog_warning
import tikoncha_parents.composeapp.generated.resources.farzand_chiqish_ruxsat
import tikoncha_parents.composeapp.generated.resources.farzand_ilovani_ochirmoqchi
import tikoncha_parents.composeapp.generated.resources.ha
import tikoncha_parents.composeapp.generated.resources.himoya
import tikoncha_parents.composeapp.generated.resources.hisobdan_chiqish
import tikoncha_parents.composeapp.generated.resources.hisobdan_chiqishni_tasdiqlaysizmi
import tikoncha_parents.composeapp.generated.resources.ilovani_ochirish
import tikoncha_parents.composeapp.generated.resources.ilovani_ochirishni_tasdiqlaysizmi
import tikoncha_parents.composeapp.generated.resources.ok
import tikoncha_parents.composeapp.generated.resources.qalqon_ochirishga_ruxsat
import tikoncha_parents.composeapp.generated.resources.qalqon_ochirish_ruxsat_desc
import tikoncha_parents.composeapp.generated.resources.sorovlar
import tikoncha_parents.composeapp.generated.resources.xatolik
import tikoncha_parents.composeapp.generated.resources.yoq
import uz.tikoncha_parent.data.local.AppSettings
import uz.tikoncha_parent.domain.model.protection.AccountRequestAction
import uz.tikoncha_parent.presentation.add_child.AddChildScreen
import uz.tikoncha_parent.presentation.base.CustomDialog
import uz.tikoncha_parent.presentation.base.CustomHeader
import uz.tikoncha_parent.presentation.base.Loading
import uz.tikoncha_parent.presentation.ui_state.ResponseState
import uz.tikoncha_parent.presentation.ui_state.errorText
import uz.tikoncha_parent.ui.ContainerPadding
import uz.tikoncha_parent.ui.theme.AppColors
import uz.tikoncha_parent.ui.theme.ThemeMode
import uz.tikoncha_parent.ui.theme.TikonchaParentTheme
import uz.tikoncha_parent.ui.theme.rememberScreenSystemBars

class ProtectionScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.current ?: return

        val viewModel = koinScreenModel<ProtectionViewModel>()
        val state by viewModel.state.collectAsStateWithLifecycle()
        val remainingSeconds by viewModel.remainingSeconds.collectAsStateWithLifecycle()
        val event = viewModel::onEvent

        val selectedChild = AppSettings.selectedChild
        val childId = selectedChild?.userId ?: ""
        val childName = selectedChild?.name ?: ""

        LaunchedEffect(childId) {
            if (childId.isNotEmpty()) event(ProtectionEvent.LoadStatus(childId))
            event(ProtectionEvent.GetChildren)
        }

        ProtectionUi(
            navigator = navigator,
            state = state,
            remainingSeconds = remainingSeconds,
            event = event,
            childId = childId,
            childName = childName,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProtectionUi(
    navigator: Navigator?,
    state: ProtectionState,
    remainingSeconds: Int,
    event: (ProtectionEvent) -> Unit,
    childId: String,
    childName: String,
) {
    // ─────────── Dialog holatlari ───────────
    var approveStrictDialog by remember { mutableStateOf(false) }
    var allowLogoutDialog by remember { mutableStateOf(false) }
    var allowDeleteDialog by remember { mutableStateOf(false) }
    var showErrorDialog by remember { mutableStateOf(false) }

    val loadErrorText = state.responseState.errorText()
    val actionErrorText = state.actionResponseState.errorText()

    LaunchedEffect(loadErrorText, actionErrorText) {
        if (loadErrorText.isNotEmpty() || actionErrorText.isNotEmpty()) {
            showErrorDialog = true
        }
    }

    Loading(state.responseState is ResponseState.Loading)

    // ─────────── Error ───────────
    CustomDialog(
        painter = painterResource(Res.drawable.dialog_failed),
        show = showErrorDialog,
        title = stringResource(Res.string.xatolik),
        message = actionErrorText.ifEmpty { loadErrorText },
        buttonText = stringResource(Res.string.ok),
        showCloseButton = false,
        onDismiss = {
            showErrorDialog = false
            event(ProtectionEvent.ActionErrorDismissed)
        },
        onButtonClick = {
            showErrorDialog = false
            event(ProtectionEvent.ActionErrorDismissed)
        },
    )

    // ─────────── Qalqon o'chirishni tasdiqlash ───────────
    CustomDialog(
        painter = painterResource(Res.drawable.dialog_warning),
        show = approveStrictDialog,
        title = stringResource(Res.string.qalqon_ochirishga_ruxsat),
        message = stringResource(Res.string.qalqon_ochirish_ruxsat_desc),
        buttonText = stringResource(Res.string.ha),
        buttonText2 = stringResource(Res.string.yoq),
        showCloseButton = true,
        onDismiss = { approveStrictDialog = false },
        onButtonClick = {
            approveStrictDialog = false
            state.strictDisableRequest?.id?.let {
                event(ProtectionEvent.ApproveStrictRequest(it))
            }
        },
    )

    // ─────────── Hisobdan chiqishga ruxsat ───────────
    CustomDialog(
        painter = painterResource(Res.drawable.dialog_warning),
        show = allowLogoutDialog,
        title = stringResource(Res.string.hisobdan_chiqishni_tasdiqlaysizmi),
        message = stringResource(Res.string.farzand_chiqish_ruxsat),
        buttonText = stringResource(Res.string.ha),
        buttonText2 = stringResource(Res.string.yoq),
        showCloseButton = true,
        onDismiss = { allowLogoutDialog = false },
        onButtonClick = {
            allowLogoutDialog = false
            event(ProtectionEvent.AllowAccountRequest(AccountRequestAction.LOGOUT))
        },
    )

    // ─────────── Ilovani o'chirishga ruxsat ───────────
    CustomDialog(
        painter = painterResource(Res.drawable.dialog_warning),
        show = allowDeleteDialog,
        title = stringResource(Res.string.ilovani_ochirishni_tasdiqlaysizmi),
        message = stringResource(Res.string.farzand_ilovani_ochirmoqchi),
        buttonText = stringResource(Res.string.ha),
        buttonText2 = stringResource(Res.string.yoq),
        showCloseButton = true,
        onDismiss = { allowDeleteDialog = false },
        onButtonClick = {
            allowDeleteDialog = false
            event(ProtectionEvent.AllowAccountRequest(AccountRequestAction.DELETE))
        },
    )

    val systemBars = rememberScreenSystemBars(
        statusBarColor = AppColors.bg.secondary,
        navigationBarColor = AppColors.bg.secondary,
    )

    // So'rovlar: kutilayotganlari — pastdagi dokda, hal bo'lganlari — ro'yxatda
    val allowText = stringResource(Res.string.ruxsat_berish)
    val approveText = stringResource(Res.string.tasdiqlash)
    val denyText = stringResource(Res.string.rad_etish)
    val requests = buildList {
        state.strictDisableRequest?.let { request ->
            add(
                strictRequestUi(
                    request = request,
                    childName = request.childName ?: childName,
                    isPending = state.isStrictRequestPending,
                    remainingSeconds = remainingSeconds,
                    isProcessing = state.actionInProgressId == request.id,
                    title = stringResource(Res.string.qalqonni_ochirish),
                    allowText = approveText,
                    denyText = denyText,
                    onApprove = { approveStrictDialog = true },
                    onReject = { request.id?.let { event(ProtectionEvent.RejectStrictRequest(it)) } },
                )
            )
        }
        state.logoutRequest?.let { logout ->
            add(
                accountRequestUi(
                    request = logout,
                    childName = childName,
                    title = stringResource(Res.string.hisobdan_chiqish),
                    isProcessing = state.actionInProgressId == logout.id,
                    allowText = allowText,
                    denyText = denyText,
                    onAllow = { allowLogoutDialog = true },
                    onDeny = { event(ProtectionEvent.DenyAccountRequest(AccountRequestAction.LOGOUT)) },
                )
            )
        }
        state.deleteRequest?.let { delete ->
            add(
                accountRequestUi(
                    request = delete,
                    childName = childName,
                    title = stringResource(Res.string.ilovani_ochirish),
                    isProcessing = state.actionInProgressId == delete.id,
                    allowText = allowText,
                    denyText = denyText,
                    onAllow = { allowDeleteDialog = true },
                    onDeny = { event(ProtectionEvent.DenyAccountRequest(AccountRequestAction.DELETE)) },
                )
            )
        }
    }
    val pending = requests.filter { it.pending }
    val resolved = requests.filterNot { it.pending }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .then(systemBars.modifier)
            .background(AppColors.bg.secondary),
    ) {
        CustomHeader(
            title = stringResource(Res.string.himoya),
            showBackButton = true,
            onBackClick = { navigator?.pop() },
            childPicker = ChildPickerSpec(
                children = state.childrenList,
                selected = state.selectedChild,
                onSelect = { event(ProtectionEvent.OnChildSelected(it)) },
                onAddChild = { navigator?.push(AddChildScreen()) },
            ),
        )

        BoxWithConstraints(modifier = Modifier.fillMaxWidth().weight(1f)) {
            val dockMax = maxHeight * 0.6f
            Column(modifier = Modifier.fillMaxSize()) {
                PullToRefreshBox(
                    isRefreshing = state.isRefreshing,
                    onRefresh = {
                        if (childId.isNotEmpty()) event(ProtectionEvent.Refresh(childId))
                        event(ProtectionEvent.GetChildren)
                    },
                    modifier = Modifier.fillMaxWidth().weight(1f),
                ) {
                    // Kontent qisqa bo'lsa — pastga (bosh barmoqqa) yaqin turadi
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(start = ContainerPadding, end = ContainerPadding, top = 8.dp, bottom = 12.dp),
                        verticalArrangement = Arrangement.Bottom,
                    ) {
                        item(key = "status", contentType = "status") {
                            ProtectionStatusGroup(state = state, event = event)
                        }
                        item(key = "permissions", contentType = "permissions") {
                            ProtectionPermissionsGroup(state = state)
                        }
                        if (resolved.isNotEmpty()) {
                            item(key = "resolved", contentType = "requests") {
                                ProtectionSectionLabel(stringResource(Res.string.sorovlar))
                                ProtectionResolvedRequests(resolved, childName)
                            }
                        }
                    }
                }

                AnimatedVisibility(
                    visible = pending.isNotEmpty(),
                    enter = expandVertically(expandFrom = Alignment.Top) + fadeIn(),
                    exit = shrinkVertically(shrinkTowards = Alignment.Top) + fadeOut(),
                ) {
                    ProtectionPendingDock(
                        requests = pending,
                        childName = childName,
                        modifier = Modifier
                            .heightIn(max = dockMax)
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = ContainerPadding)
                            .padding(top = 4.dp, bottom = 4.dp),
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun Pre(){
    TikonchaParentTheme(
        ThemeMode.DARK
    ){
        ProtectionUi(
            navigator = null,
            state = ProtectionState(),
            event = {},
            childId = "",
            childName = "Abbos",
            remainingSeconds = 2
        )
    }
}