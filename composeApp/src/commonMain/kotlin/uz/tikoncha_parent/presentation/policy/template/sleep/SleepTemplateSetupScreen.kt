package uz.tikoncha_parent.presentation.policy.template.sleep

import androidx.compose.foundation.Image
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.datetime.LocalTime
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel
import tikoncha_parents.composeapp.generated.resources.Res
import tikoncha_parents.composeapp.generated.resources.arrow_down_reg
import tikoncha_parents.composeapp.generated.resources.arrow_right_rounded
import tikoncha_parents.composeapp.generated.resources.bekor_qilish
import tikoncha_parents.composeapp.generated.resources.blocklist
import tikoncha_parents.composeapp.generated.resources.bloklamoqchi_bo_lgan_ilova_yoki_saytlarni_tanlang
import tikoncha_parents.composeapp.generated.resources.bloklash_rejimi
import tikoncha_parents.composeapp.generated.resources.cheklov_vaqti
import tikoncha_parents.composeapp.generated.resources.dialog_failed
import tikoncha_parents.composeapp.generated.resources.dialog_success
import tikoncha_parents.composeapp.generated.resources.dot
import tikoncha_parents.composeapp.generated.resources.farzandingiz_kun_davomida_telefondan_qancha
import tikoncha_parents.composeapp.generated.resources.ilovalar
import tikoncha_parents.composeapp.generated.resources.jadval_muvaffaqiyatli_o_chirildi
import tikoncha_parents.composeapp.generated.resources.jadval_muvaffaqiyatli_tahrirlandi
import tikoncha_parents.composeapp.generated.resources.jadval_muvaffaqiyatli_yaratildi
import tikoncha_parents.composeapp.generated.resources.jadval_nomini_kiriting
import tikoncha_parents.composeapp.generated.resources.jadval_o_chirilsinmi
import tikoncha_parents.composeapp.generated.resources.kamida_1_ta_ilova_kategoriya_yoki_sayt_tanlang
import tikoncha_parents.composeapp.generated.resources.kategoriyalar
import tikoncha_parents.composeapp.generated.resources.kerakli_hafta_kunlarini_tanlang
import tikoncha_parents.composeapp.generated.resources.kerakli_vaqt_oralig_ini_tanlang
import tikoncha_parents.composeapp.generated.resources.message_delete
import tikoncha_parents.composeapp.generated.resources.muvaffaqiyatli
import tikoncha_parents.composeapp.generated.resources.ochirish
import tikoncha_parents.composeapp.generated.resources.ok
import tikoncha_parents.composeapp.generated.resources.oq_royhat
import tikoncha_parents.composeapp.generated.resources.oq_royxat_uchun_kerakli_ilovalar_va_saytlarni_tanlang
import tikoncha_parents.composeapp.generated.resources.qora_ro_yxat
import tikoncha_parents.composeapp.generated.resources.sand_time_policy
import tikoncha_parents.composeapp.generated.resources.saqlash
import tikoncha_parents.composeapp.generated.resources.saytlar
import tikoncha_parents.composeapp.generated.resources.siz_rostdan_ham_ushbu_jadvalni_o_chirmoqchimisiz
import tikoncha_parents.composeapp.generated.resources.tanlangan_ilovalar_ochiq_qoladi
import tikoncha_parents.composeapp.generated.resources.uxlash_vaqti
import tikoncha_parents.composeapp.generated.resources.uyqu_vaqti_rejasi
import tikoncha_parents.composeapp.generated.resources.whitelist
import tikoncha_parents.composeapp.generated.resources.xatolik
import uz.tikoncha_parent.domain.model.WeekDay
import uz.tikoncha_parent.domain.model.policy.PolicyAction
import uz.tikoncha_parent.presentation.base.CustomBottomDialog
import uz.tikoncha_parent.presentation.base.CustomButtonNew
import uz.tikoncha_parent.presentation.base.CustomDialog
import uz.tikoncha_parent.presentation.base.CustomHeader
import uz.tikoncha_parent.presentation.base.LoadingDialog
import uz.tikoncha_parent.presentation.base.LocalToastHost
import uz.tikoncha_parent.presentation.base.TimeRangePicker
import uz.tikoncha_parent.presentation.base.TimeRangePickerDefaults
import uz.tikoncha_parent.presentation.base.ToastData
import uz.tikoncha_parent.presentation.base.ToastProvider
import uz.tikoncha_parent.presentation.base.ToastType
import uz.tikoncha_parent.presentation.base.WheelTimePickerDialog
import uz.tikoncha_parent.presentation.base.simpleShadow
import uz.tikoncha_parent.presentation.base.singleClick
import uz.tikoncha_parent.presentation.policy.app_site_selection.AppWebSelectionScreen
import uz.tikoncha_parent.presentation.policy.common.buildWeekdayChips
import uz.tikoncha_parent.presentation.policy.common.formatDuration
import uz.tikoncha_parent.presentation.policy.policy_setup.PolicyActionScreen
import uz.tikoncha_parent.presentation.policy.shared.PolicySharedEvent
import uz.tikoncha_parent.presentation.policy.shared.PolicySharedModel
import uz.tikoncha_parent.presentation.policy.shared.PolicySharedState
import uz.tikoncha_parent.presentation.policy.time_rule.setup.TimeRuleSetupTimeCard
import uz.tikoncha_parent.presentation.ui_state.ResponseState
import uz.tikoncha_parent.presentation.ui_state.errorText
import uz.tikoncha_parent.ui.Space
import uz.tikoncha_parent.ui.SpaceUltraSmall
import uz.tikoncha_parent.ui.theme.AppColors
import uz.tikoncha_parent.ui.theme.AppTypography
import uz.tikoncha_parent.ui.theme.ThemeMode
import uz.tikoncha_parent.ui.theme.TikonchaParentTheme
import uz.tikoncha_parent.ui.theme.rememberScreenSystemBars

class SleepTemplateSetupScreen : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.current

        val viewModel = koinScreenModel<SleepTemplateSetupViewModel>()
        val state by viewModel.state.collectAsStateWithLifecycle()
        val event = viewModel::onEvent

        val sharedViewModel = koinViewModel<PolicySharedModel>()
        val sharedState by sharedViewModel.state.collectAsStateWithLifecycle()
        val sharedEvent = sharedViewModel::onEvent

        LaunchedEffect(Unit) {
            event(SleepTemplateSetupEvent.Init(sharedState))
        }
        ToastProvider {
            SleepTemplateSetupUi(
                state = state,
                event = event,
                effect = viewModel.effect,
                sharedState = sharedState,
                sharedEvent = sharedEvent,
                onBack = { navigator?.pop() },
                onPickApps = {
                    navigator?.push(AppWebSelectionScreen(appSiteTabIndex = 0))
                },
                onPickSites = {
                    navigator?.push(AppWebSelectionScreen(appSiteTabIndex = 1))
                },

                onPolicyAction = {
                    navigator?.push(PolicyActionScreen())
                }
            )
        }
    }
}

@Composable
fun SleepTemplateSetupUi(
    state: SleepTemplateSetupState,
    event: (SleepTemplateSetupEvent) -> Unit,
    effect: Flow<SleepTemplateSetupEffect>,
    sharedState: PolicySharedState,
    sharedEvent: (PolicySharedEvent) -> Unit,
    onBack: () -> Unit,
    onPickApps: () -> Unit,
    onPickSites: () -> Unit,
    onPolicyAction: () -> Unit,
) {
    val cardColor = AppColors.bg.surface
    val toast = LocalToastHost.current

    val noTitleMsg = stringResource(Res.string.jadval_nomini_kiriting)
    val noDayMsg = stringResource(Res.string.kerakli_hafta_kunlarini_tanlang)
    val noTimeMsg = stringResource(Res.string.kerakli_vaqt_oralig_ini_tanlang)
    val noAppWebMsg = stringResource(Res.string.kamida_1_ta_ilova_kategoriya_yoki_sayt_tanlang)

    LaunchedEffect(Unit) {
        effect.collect { eff ->
            when (eff) {
                SleepTemplateSetupEffect.NoTitleToast ->
                    toast.show(ToastData(ToastType.Warning, noTitleMsg))

                SleepTemplateSetupEffect.NoDaySelectionToast ->
                    toast.show(ToastData(ToastType.Warning, noDayMsg))

                SleepTemplateSetupEffect.NoTimeIntervalSelectionToast ->
                    toast.show(ToastData(ToastType.Warning, noTimeMsg))

                SleepTemplateSetupEffect.NoAppWebSelectedToast ->
                    toast.show(ToastData(ToastType.Warning, noAppWebMsg))
            }
        }
    }

    var showStartPicker by remember { mutableStateOf(false) }
    var showEndPicker by remember { mutableStateOf(false) }
    var showErrorDialog by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var showPopupMenu by remember { mutableStateOf(false) }

    val createError = state.createState.errorText()
    val updateError = state.updateState.errorText()
    val deleteError = state.deleteState.errorText()

    val createSuccess = state.createState is ResponseState.Success
    val updateSuccess = state.updateState is ResponseState.Success
    val deleteSuccess = state.deleteState is ResponseState.Success

    val anyError = createError.ifEmpty { updateError }.ifEmpty { deleteError }

    val scrollState = rememberSaveable(saver = ScrollState.Saver) {
        ScrollState(0)
    }

    LaunchedEffect(anyError) {
        if (anyError.isNotEmpty()) showErrorDialog = true
    }
    LaunchedEffect(createSuccess, updateSuccess, deleteSuccess) {
        if (createSuccess || updateSuccess || deleteSuccess) showSuccessDialog = true
    }


    LaunchedEffect(showSuccessDialog) {
        if (showSuccessDialog) {
            delay(2500)
            showSuccessDialog = false
            event(SleepTemplateSetupEvent.ResetResponseState)
            sharedEvent(PolicySharedEvent.ClearData)
            onBack()
        }
    }

    LoadingDialog(state.isLoading)

    CustomDialog(
        painter = painterResource(Res.drawable.dialog_failed),
        show = showErrorDialog,
        title = stringResource(Res.string.xatolik),
        message = anyError,
        buttonText = stringResource(Res.string.ok),
        showCloseButton = false,
        onDismiss = { showErrorDialog = false },
        onButtonClick = { showErrorDialog = false },
    )

    CustomDialog(
        painter = painterResource(Res.drawable.dialog_success),
        show = showSuccessDialog,
        title = stringResource(Res.string.muvaffaqiyatli),
        message = when {
            createSuccess -> stringResource(Res.string.jadval_muvaffaqiyatli_yaratildi)
            updateSuccess -> stringResource(Res.string.jadval_muvaffaqiyatli_tahrirlandi)
            else -> stringResource(Res.string.jadval_muvaffaqiyatli_o_chirildi)
        },
        buttonText = stringResource(Res.string.ok),
        showCloseButton = false,
        onDismiss = {
            showSuccessDialog = false
            event(SleepTemplateSetupEvent.ResetResponseState)
        },
        onButtonClick = {
            showSuccessDialog = false
            event(SleepTemplateSetupEvent.ResetResponseState)
            sharedEvent(PolicySharedEvent.ClearData)
            onBack()
        },
    )

    CustomBottomDialog(
        show = showDeleteConfirmDialog,
        title = stringResource(Res.string.jadval_o_chirilsinmi),
        message = stringResource(Res.string.siz_rostdan_ham_ushbu_jadvalni_o_chirmoqchimisiz),
        confirmButtonText = stringResource(Res.string.ochirish),
        confirmButtonColor = AppColors.action.accentDanger,
        dismissButtonText = stringResource(Res.string.bekor_qilish),
        showCancelButton = true,
        onDismiss = { showDeleteConfirmDialog = false },
        onConfirm = {
            showDeleteConfirmDialog = false
            state.editingPolicy?.policyId?.let {
                event(SleepTemplateSetupEvent.Delete(it))
            }
        },
    )

    WheelTimePickerDialog(
        show = showStartPicker,
        currentTime = state.startTime,
        onDismiss = { showStartPicker = false },
        onConfirm = {
            event(SleepTemplateSetupEvent.SetStartTime(it))
            showStartPicker = false
        },
    )
    WheelTimePickerDialog(
        show = showEndPicker,
        currentTime = state.endTime,
        onDismiss = { showEndPicker = false },
        onConfirm = {
            event(SleepTemplateSetupEvent.SetEndTime(it))
            showEndPicker = false
        },
    )

    val systemBars = rememberScreenSystemBars(
        statusBarColor = AppColors.bg.secondary,
        navigationBarColor = AppColors.bg.elevated,
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .then(systemBars.modifier)
            .background(AppColors.bg.secondary),
    ) {
        CustomHeader(
            title = stringResource(Res.string.uyqu_vaqti_rejasi),
            modifier = Modifier.fillMaxWidth(),
            showBackButton = true,
            onBackClick = onBack,
            trailingIcon = {
                if (state.isEditMode) {
                    Box {
                        IconButton(
                            onClick = { showDeleteConfirmDialog = true },
                            modifier = Modifier.size(40.dp),
                        ) {
                            Icon(
                                painter = painterResource(Res.drawable.message_delete),
                                contentDescription = "Menu",
                                modifier = Modifier.size(24.dp),
                            )
                        }
                    }
                }
            },
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 10.dp)
                .verticalScroll(scrollState),
        ) {

            Text(
                text = stringResource(Res.string.farzandingiz_kun_davomida_telefondan_qancha),
                style = AppTypography.emphasizedMdMedium,
                color = AppColors.text.primary,
                modifier = Modifier
                    .fillMaxWidth(),
                textAlign = TextAlign.Center
            )
            Space(16.dp)

            TimeRangePicker(
                enabled = true,
                start = state.startTime,
                end = state.endTime,
                stepMinutes = 10,
                onTimeChange = { start, end ->
                    event(SleepTemplateSetupEvent.SetTimeRange(start, end))
                },
                colors = TimeRangePickerDefaults.colors(),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 40.dp),
                startIcon = {
                    Icon(
                        painter = painterResource(Res.drawable.dot),
                        contentDescription = "",
                        tint = AppColors.icon.inverse,
                        modifier = Modifier
                            .size(10.dp)
                    )
                },
                endIcon = {
                    Icon(
                        painter = painterResource(Res.drawable.dot),
                        contentDescription = "",
                        tint = AppColors.icon.inverse,
                        modifier = Modifier
                            .size(10.dp)
                    )
                },
                centerContent = {
                    Image(
                        painter = painterResource(Res.drawable.sand_time_policy),
                        contentDescription = "",
                        modifier = Modifier
                            .size(100.dp)
                    )
                }

            )

            Space(24.dp)



            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                TimeRuleSetupTimeCard(
                    value = "${state.startTime.hour.toString().padStart(2, '0')}:" +
                            state.startTime.minute.toString().padStart(2, '0'),
                    onClick = { showStartPicker = true },
                )
                Space(16.dp)
                Text(
                    text = "—",
                    style = AppTypography.headlineLgSemiBold,
                    color = AppColors.icon.secondary,
                )
                Space(16.dp)
                TimeRuleSetupTimeCard(
                    value = "${state.endTime.hour.toString().padStart(2, '0')}:" +
                            state.endTime.minute.toString().padStart(2, '0'),
                    onClick = { showEndPicker = true },
                )
            }


            Space(24.dp)
            Text(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally),
                text = "${stringResource(Res.string.uxlash_vaqti)} : ${formatDuration(state.intervalMinutes)}",
                style = AppTypography.titleSmSemiBold,
                color = AppColors.text.primary
            )

            Space(36.dp)



            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(Res.string.bloklash_rejimi),
                    style = AppTypography.titleLgSemiBold,
                    color = AppColors.text.primary,
                    modifier = Modifier.weight(1f),
                )
                // Action badge
                val actionText = if (sharedState.policyAction == PolicyAction.DENY)
                    stringResource(Res.string.qora_ro_yxat)
                else stringResource(Res.string.oq_royhat)

                val actionIcon = if (sharedState.policyAction == PolicyAction.DENY)
                    painterResource(Res.drawable.blocklist)
                else
                    painterResource(Res.drawable.whitelist)

                Row(
                    modifier = Modifier
                        .singleClick {
                            if (sharedState.canUpdate) {

                            }
                        }
                        .padding(horizontal = 6.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        painter = actionIcon,
                        contentDescription = "",
                        modifier = Modifier
                            .size(20.dp),
                        tint = AppColors.icon.accentPrimary
                    )
                    Space(4.dp)
                    Text(
                        text = actionText,
                        style = AppTypography.titleLgSemiBold,
                        color = AppColors.text.accentEmphasis
                    )
                    Space(4.dp)

                    Icon(
                        painter = painterResource(Res.drawable.arrow_down_reg),
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = AppColors.icon.accentPrimary,
                    )
                }
            }
            Space(12.dp)

            val blockWhiteText = if (sharedState.policyAction == PolicyAction.DENY)
                stringResource(Res.string.bloklamoqchi_bo_lgan_ilova_yoki_saytlarni_tanlang)
            else
                stringResource(Res.string.tanlangan_ilovalar_ochiq_qoladi)


            Text(
                text = blockWhiteText,
                style = AppTypography.bodyLgMedium,
                color = AppColors.text.primary,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 6.dp),
            )
            Spacer(Modifier.height(20.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .simpleShadow(shape = RoundedCornerShape(20.dp))
                    .background(AppColors.bg.surface, RoundedCornerShape(20.dp))
            ) {


                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .padding(horizontal = 16.dp)
                        .singleClick {
                            onPickApps()
                        },
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(Res.string.ilovalar),
                        style = AppTypography.titleSmMedium,
                        color = AppColors.text.primary,
                        modifier = Modifier.weight(1f),
                    )
                    Text(
                        text = "${sharedState.selectedPkgs.size}",
                        style = AppTypography.titleSmMedium,
                        color = AppColors.text.primary,
                    )
                    SpaceUltraSmall()
                    Icon(
                        painter = painterResource(Res.drawable.arrow_right_rounded),
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = AppColors.icon.secondary,
                    )
                }

                HorizontalDivider(
                    color = AppColors.border.tertiarySubtle,
                    thickness = 1.dp
                )


                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .padding(horizontal = 16.dp)
                        .singleClick {
                            onPickApps()
                        },
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(Res.string.kategoriyalar),
                        style = AppTypography.titleSmMedium,
                        color = AppColors.text.primary,
                        modifier = Modifier.weight(1f),
                    )
                    Text(
                        text = "${sharedState.selectedCategories.size}",
                        style = AppTypography.titleSmMedium,
                        color = AppColors.text.primary,
                    )
                    SpaceUltraSmall()
                    Icon(
                        painter = painterResource(Res.drawable.arrow_right_rounded),
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = AppColors.icon.secondary,
                    )
                }

                HorizontalDivider(
                    color = AppColors.border.tertiarySubtle,
                    thickness = 1.dp
                )


                // Saytlar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .padding(horizontal = 16.dp)
                        .singleClick {
                            onPickSites()
                        },
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(Res.string.saytlar),
                        style = AppTypography.titleSmMedium,
                        color = AppColors.text.primary,
                        modifier = Modifier.weight(1f),
                    )
                    Text(
                        text = "${sharedState.selectedSites.size}",
                        style = AppTypography.titleSmMedium,
                        color = AppColors.text.primary,
                    )
                    SpaceUltraSmall()
                    Icon(
                        painter = painterResource(Res.drawable.arrow_right_rounded),
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = AppColors.icon.secondary,
                    )
                }
            }
            Space(16.dp)
        }



        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    AppColors.bg.elevated,
                    RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                )
                .padding(horizontal = 20.dp, vertical = 12.dp),
        ) {
            CustomButtonNew(
                text = stringResource(Res.string.saqlash),
                onClick = { event(SleepTemplateSetupEvent.Save(sharedState)) },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }


}



@Preview
@Composable
private fun SleepTemplateSetupPreview() {
    TikonchaParentTheme(ThemeMode.LIGHT) {
        ToastProvider {
            SleepTemplateSetupUi(
                state = SleepTemplateSetupState(
                    startTime = LocalTime(22, 0),
                    endTime = LocalTime(7, 0),
                    isInitialized = true,
                ),
                event = {},
                effect = emptyFlow(),
                sharedState = PolicySharedState(
                    selectedPkgs = setOf("com.instagram.android", "com.tiktok"),
                    selectedCategories = setOf("Social"),
                    selectedSites = setOf("youtube.com"),
                ),
                sharedEvent = {},
                onBack = {},
                onPickApps = {},
                onPickSites = {},
                onPolicyAction = {}
            )
        }
    }
}