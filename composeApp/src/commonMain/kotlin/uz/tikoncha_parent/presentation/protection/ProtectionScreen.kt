package uz.tikoncha_parent.presentation.protection

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import uz.tikoncha_parent.presentation.base.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import tikoncha_parents.composeapp.generated.resources.farzand_qo_shish
import tikoncha_parents.composeapp.generated.resources.farzandlaringiz
import tikoncha_parents.composeapp.generated.resources.ha
import tikoncha_parents.composeapp.generated.resources.himoya
import tikoncha_parents.composeapp.generated.resources.hisobdan_chiqish
import tikoncha_parents.composeapp.generated.resources.hisobdan_chiqishni_tasdiqlaysizmi
import tikoncha_parents.composeapp.generated.resources.ilovani_ochirish
import tikoncha_parents.composeapp.generated.resources.ilovani_ochirishni_tasdiqlaysizmi
import tikoncha_parents.composeapp.generated.resources.ok
import tikoncha_parents.composeapp.generated.resources.qalqon_ochirishga_ruxsat
import tikoncha_parents.composeapp.generated.resources.qalqon_ochirish_ruxsat_desc
import tikoncha_parents.composeapp.generated.resources.ruxsatlar
import tikoncha_parents.composeapp.generated.resources.sorovlar
import tikoncha_parents.composeapp.generated.resources.xatolik
import tikoncha_parents.composeapp.generated.resources.yoq
import uz.tikoncha_parent.data.local.AppSettings
import uz.tikoncha_parent.domain.model.protection.AccountRequestAction
import uz.tikoncha_parent.presentation.add_child.AddChildScreen
import uz.tikoncha_parent.presentation.base.ChildSelectionButton
import uz.tikoncha_parent.presentation.base.CustomDialog
import uz.tikoncha_parent.presentation.base.CustomHeader
import uz.tikoncha_parent.presentation.base.Loading
import uz.tikoncha_parent.presentation.new_home.SelectionChildBottomSheet
import uz.tikoncha_parent.presentation.ui_state.ResponseState
import uz.tikoncha_parent.presentation.ui_state.errorText
import uz.tikoncha_parent.ui.ContainerPadding
import uz.tikoncha_parent.ui.theme.AppColors
import uz.tikoncha_parent.ui.theme.AppTypography
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
    var showDialog by remember { mutableStateOf(false) }

    val loadErrorText = state.responseState.errorText()
    val actionErrorText = state.actionResponseState.errorText()

    if (showDialog) {
        SelectionChildBottomSheet(
            navigator = navigator,
            items = state.childrenList,
            selectedItem = state.selectedChild,
            onDismiss = { showDialog = false },
            title = stringResource(Res.string.farzandlaringiz),
            onItemSelected = {
                event(ProtectionEvent.OnChildSelected(it))
                showDialog = false
            }
        )
    }

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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .then(systemBars.modifier)
            .background(AppColors.bg.secondary),
    ) {
        CustomHeader(
            title = stringResource(Res.string.himoya),
            showBackButton = true,
            onBackClick = { navigator?.pop() }
        )

        PullToRefreshBox(
            isRefreshing = state.isRefreshing,
            onRefresh = {
                if (childId.isNotEmpty()) event(ProtectionEvent.Refresh(childId))
                event(ProtectionEvent.GetChildren)
            },
            modifier = Modifier.fillMaxSize(),
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(ContainerPadding),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                // ════════ CHILD SELECTED ════════
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
                }

                // ════════ HERO ════════
                item(key = "hero", contentType = "hero") {
                    ProtectionHeroCard(state = state, event = event)
                }

                // ════════ SO'ROVLAR sarlavhasi ════════
                if (state.showRequestsCard) {
                    item(key = "requests_header", contentType = "header") {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 6.dp, start = 2.dp, end = 2.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = stringResource(Res.string.sorovlar),
                                style = AppTypography.emphasizedSmSemiBold,
                                color = AppColors.text.secondary,
                                modifier = Modifier.weight(1f),
                            )
                            if (state.pendingRequestCount > 0) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(100.dp))
                                        .background(AppColors.bg.accentDanger)
                                        .padding(horizontal = 8.dp, vertical = 2.dp),
                                ) {
                                    Text(
                                        text = state.pendingRequestCount.toString(),
                                        style = AppTypography.bodyMdMedium,
                                        color = AppColors.text.inverse,
                                    )
                                }
                            }
                        }
                    }
                }

                // ── Qalqonni o'chirish so'rovi ──
                state.strictDisableRequest?.let { request ->
                    item(key = "strict_request", contentType = "request") {
                        ProtectionStrictRequestCard(
                            request = request,
                            childName = request.childName ?: childName,
                            isPending = state.isStrictRequestPending,
                            remainingSeconds = remainingSeconds,
                            isProcessing = state.actionInProgressId == request.id,
                            onApprove = { approveStrictDialog = true },
                            onReject = {
                                request.id?.let { event(ProtectionEvent.RejectStrictRequest(it)) }
                            },
                        )
                    }
                }

                // ── Hisobdan chiqish ──
                state.logoutRequest?.let { logout ->
                    item(key = "logout_request", contentType = "request") {
                        ProtectionAccountRequestCard(
                            request = logout,
                            childName = childName,
                            title = stringResource(Res.string.hisobdan_chiqish),
                            isProcessing = state.actionInProgressId == logout.id,
                            onAllow = { allowLogoutDialog = true },
                            onDeny = {
                                event(ProtectionEvent.DenyAccountRequest(AccountRequestAction.LOGOUT))
                            },
                        )
                    }
                }

                // ── Ilovani o'chirish ──
                state.deleteRequest?.let { delete ->
                    item(key = "delete_request", contentType = "request") {
                        ProtectionAccountRequestCard(
                            request = delete,
                            childName = childName,
                            title = stringResource(Res.string.ilovani_ochirish),
                            isProcessing = state.actionInProgressId == delete.id,
                            onAllow = { allowDeleteDialog = true },
                            onDeny = {
                                event(ProtectionEvent.DenyAccountRequest(AccountRequestAction.DELETE))
                            },
                        )
                    }
                }

                // ════════ WARNING BANNER ════════
                if (state.missingRequiredPermissions.isNotEmpty()) {
                    item(key = "warning", contentType = "warning") {
                        Spacer(Modifier.height(6.dp))
                        MissingPermissionsBanner(missing = state.missingRequiredPermissions)
                    }
                }

                // ════════ RUXSATLAR ════════
                item(key = "permissions_header", contentType = "header") {
                    Text(
                        text = stringResource(Res.string.ruxsatlar),
                        style = AppTypography.emphasizedSmSemiBold,
                        color = AppColors.text.secondary,
                        modifier = Modifier.padding(top = 6.dp, start = 2.dp, end = 2.dp),
                    )
                }
                item(key = "permissions", contentType = "permissions") {
                    ProtectionPermissionsCard(state = state)
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