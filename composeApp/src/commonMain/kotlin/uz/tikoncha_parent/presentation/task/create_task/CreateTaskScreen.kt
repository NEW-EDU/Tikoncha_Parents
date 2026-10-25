package uz.tikoncha_parent.presentation.task.create_task

import uz.tikoncha_parent.ui.segmentShape
import uz.tikoncha_parent.ui.rememberHapticSource
import uz.tikoncha_parent.ui.expressiveSegment
import uz.tikoncha_parent.ui.expressiveButtonShapes
import uz.tikoncha_parent.ui.SegmentGap
import uz.tikoncha_parent.ui.ContainerPadding
import uz.tikoncha_parent.presentation.profile.coins.CoinNumberPicker
import uz.tikoncha_parent.presentation.profile.ProfileSectionLabel
import uz.tikoncha_parent.presentation.base.island.BottomAnchoredColumn
import tikoncha_parents.composeapp.generated.resources.vazifa_tangalari
import tikoncha_parents.composeapp.generated.resources.vazifa_muddati
import tikoncha_parents.composeapp.generated.resources.tangachalar
import tikoncha_parents.composeapp.generated.resources.ta
import tikoncha_parents.composeapp.generated.resources.ozingiz_qoshing
import tikoncha_parents.composeapp.generated.resources.o_rtacha
import tikoncha_parents.composeapp.generated.resources.muhimligi
import tikoncha_parents.composeapp.generated.resources.muhim
import tikoncha_parents.composeapp.generated.resources.juda_muhim
import tikoncha_parents.composeapp.generated.resources.coin
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.alpha
import androidx.compose.material3.Icon
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Button
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.Icons
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.clickable
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.Navigator
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import tikoncha_parents.composeapp.generated.resources.Res
import tikoncha_parents.composeapp.generated.resources.calendar_2
import tikoncha_parents.composeapp.generated.resources.davom_etish
import tikoncha_parents.composeapp.generated.resources.dialog_failed
import tikoncha_parents.composeapp.generated.resources.dialog_success
import tikoncha_parents.composeapp.generated.resources.izoh_ixtiyoriy
import tikoncha_parents.composeapp.generated.resources.muvaffaqiyatli
import tikoncha_parents.composeapp.generated.resources.time_square
import tikoncha_parents.composeapp.generated.resources.tugash_vaqti
import tikoncha_parents.composeapp.generated.resources.tugatish_sanasi
import tikoncha_parents.composeapp.generated.resources.vazifa_haqida_qisqacha_ma_lumot
import tikoncha_parents.composeapp.generated.resources.vazifa_nomi
import tikoncha_parents.composeapp.generated.resources.vazifa_qo_shish
import tikoncha_parents.composeapp.generated.resources.vazifani_tahrirlash
import tikoncha_parents.composeapp.generated.resources.xatolik
import uz.tikoncha_parent.common.DateTimeUtil.formatTime
import uz.tikoncha_parent.common.DateTimeUtil.reformattedDayMonthWithWeekdayForTask
import uz.tikoncha_parent.common.Util
import uz.tikoncha_parent.domain.model.app_error.Outcome
import uz.tikoncha_parent.platform.Logger
import uz.tikoncha_parent.presentation.base.CalendarBottomSheet
import uz.tikoncha_parent.presentation.base.CustomButton
import uz.tikoncha_parent.presentation.base.CustomDialog
import uz.tikoncha_parent.presentation.base.CustomHeader
import uz.tikoncha_parent.presentation.base.CustomSelectionButton
import uz.tikoncha_parent.presentation.base.CustomTextField
import uz.tikoncha_parent.presentation.base.CustomTextFieldTask
import uz.tikoncha_parent.presentation.base.WheelTimePickerDialog
import uz.tikoncha_parent.presentation.base.asText
import uz.tikoncha_parent.presentation.base.bottomShadow
import uz.tikoncha_parent.presentation.task.create_task_check.CreateTaskCheckScreen
import uz.tikoncha_parent.presentation.task.model.CollectEffects
import uz.tikoncha_parent.presentation.task.model.ImportanceType
import uz.tikoncha_parent.presentation.task.model.Task
import uz.tikoncha_parent.presentation.task.model.rememberSharedScreenModel
import uz.tikoncha_parent.presentation.task.success.TaskSuccessScreen
import uz.tikoncha_parent.ui.ButtonCornerRadius
import uz.tikoncha_parent.ui.ButtonHeight
import uz.tikoncha_parent.ui.CardCornerPadding
import uz.tikoncha_parent.ui.Space
import uz.tikoncha_parent.ui.theme.AppColors
import uz.tikoncha_parent.ui.theme.AppTypography
import uz.tikoncha_parent.ui.theme.ThemeMode
import uz.tikoncha_parent.ui.theme.TikonchaParentTheme
import uz.tikoncha_parent.ui.theme.rememberScreenSystemBars


class CreateTaskScreen(
    private val taskToEdit: Task? = null
) : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.current
        val viewModel = rememberSharedScreenModel<CreateTaskViewModel>()
        val state by viewModel.state.collectAsStateWithLifecycle()
        val event = viewModel::onEvent

        var successDialogMessage by remember { mutableStateOf<String?>(null) }
        var failure by remember { mutableStateOf<Outcome.Failure?>(null) }
        val failureText = failure?.asText()

        // ✅ Edit yoki yangi vazifa — har holatda form holatini to'g'ri o'rnatish.
        // taskToEdit = null bo'lsa, eski edit holatini reset qilamiz.
        LaunchedEffect(taskToEdit?.id) {
            if (taskToEdit != null) {
                event(CreateTaskEvent.OnEditTask(taskToEdit))
            } else if (state.isEditing) {
                Logger.d("CreateTaskScreen", "onResetLaunch:${event(CreateTaskEvent.OnReset)}")
                event(CreateTaskEvent.OnReset)
            }
            // Coin balansini har safar yangilash
            event(CreateTaskEvent.LoadParentCoins)
        }

        CollectEffects(viewModel.effect) { effect ->
            when (effect) {
                is CreateTaskEffect.ShowSuccessDialog -> {
                    successDialogMessage = effect.message
                }
                CreateTaskEffect.NavigateBack -> {
                    navigator?.pop()
                    Logger.d("CreateTaskScreen", "onResetCollect:${event(CreateTaskEvent.OnReset)}")
                    event(CreateTaskEvent.OnReset)
                }
                is CreateTaskEffect.ShowFailure -> { failure = effect.failure }
                // ✅ Agar user CheckScreen'dan back qaytsa, lekin request muvaffaqiyatli bo'lsa,
                // bu yerda success ekraniga o'tkazamiz
                CreateTaskEffect.NavigateToSuccess -> {
                    navigator?.push(TaskSuccessScreen())
                }
            }
        }

        CustomDialog(
            painter = painterResource(Res.drawable.dialog_success),
            show = successDialogMessage != null,
            title = stringResource(Res.string.muvaffaqiyatli),
            message = successDialogMessage.orEmpty(),
            onDismiss = { successDialogMessage = null },
            onButtonClick = { successDialogMessage = null }
        )

        CustomDialog(
            painter = painterResource(Res.drawable.dialog_failed),
            message = failureText.orEmpty(),
            show = failureText != null,
            title = stringResource(Res.string.xatolik),
            onDismiss = { failure = null },
            onButtonClick = { failure = null }
        )

        CreateTaskUI(
            navigator = navigator,
            state = state,
            event = event
        )
    }
}


@OptIn(ExperimentalMaterial3Api::class, androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun CreateTaskUI(
    navigator: Navigator?,
    state: CreateTaskState,
    event: (CreateTaskEvent) -> Unit
) {
    val focusManager = LocalFocusManager.current

    val hidKeyboard = rememberHideKeyboard()
    val dateText =
        state.date?.let { reformattedDayMonthWithWeekdayForTask(date = it) } ?: "--.--.----"
    val timeText = state.time?.let { formatTime(it) } ?: "--:--"

    var showDialogData by remember { mutableStateOf(false) }
    var showDialogTime by remember { mutableStateOf(false) }

    // ✅ description ixtiyoriy — formdan olib tashladik
    val isFormValid = state.title.isNotBlank() &&
            state.date != null &&
            state.time != null &&
            state.importance != ImportanceType.NONE

    val systemBars = rememberScreenSystemBars(
        statusBarColor = AppColors.bg.secondary,
        navigationBarColor = AppColors.bg.elevated
    )

    val headerText = if (state.isEditing) stringResource(Res.string.vazifani_tahrirlash)
    else stringResource(Res.string.vazifa_qo_shish)

    CalendarBottomSheet(
        show = showDialogData,
        selectedDate = state.date,
        onDismissRequest = {
            showDialogData = false
            focusManager.clearFocus()
            hidKeyboard()
        },
        onDateSelected = {
            event(CreateTaskEvent.OnDateChange(it))
            showDialogData = false
            focusManager.clearFocus()
            hidKeyboard()
        }
    )

    WheelTimePickerDialog(
        show = showDialogTime,
        currentTime = state.time ?: Util.getCurrentTime(),
        onDismiss = {
            showDialogTime = false
            focusManager.clearFocus()
            hidKeyboard()
        },
        onConfirm = {
            event(CreateTaskEvent.OnTimeChange(it))
            showDialogTime = false
            focusManager.clearFocus()
            hidKeyboard()
        }
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .then(systemBars.modifier)
            .background(AppColors.bg.secondary)
            .imePadding()
            .pointerInput(Unit) {
                detectTapGestures {
                    focusManager.clearFocus()
                    hidKeyboard()
                }
            }
    ) {
        CustomHeader(
            title = headerText,
            showBackButton = true,
            onBackClick = {
                event(CreateTaskEvent.OnReset)
                navigator?.pop()
            }
        )

        // Bo'sh joy bo'lsa — pastga (bosh barmoqqa yaqin); klaviatura ochilsa — uning ustida
        BottomAnchoredColumn(
            modifier = Modifier.fillMaxWidth().weight(1f),
            contentPadding = PaddingValues(start = ContainerPadding, end = ContainerPadding, top = 8.dp, bottom = 8.dp),
        ) {
            // ── Vazifa: nomi + izoh — bitta guruh
            Column(verticalArrangement = Arrangement.spacedBy(SegmentGap)) {
                TaskTextSegment(
                    index = 0, count = 2,
                    value = state.title,
                    placeholder = stringResource(Res.string.vazifa_nomi),
                    style = AppTypography.titleLgSemiBold,
                    singleLine = true,
                    onValueChange = { event(CreateTaskEvent.OnTitleChange(it)) },
                )
                TaskTextSegment(
                    index = 1, count = 2,
                    value = state.desc,
                    placeholder = stringResource(Res.string.vazifa_haqida_qisqacha_ma_lumot),
                    style = AppTypography.bodyLgRegular,
                    singleLine = false,
                    minHeight = 96.dp,
                    onValueChange = { event(CreateTaskEvent.OnDescChange(it)) },
                )
            }

            // ── Muddati: sana | vaqt — yonma-yon bog'langan
            ProfileSectionLabel(stringResource(Res.string.vazifa_muddati))
            Row(horizontalArrangement = Arrangement.spacedBy(SegmentGap)) {
                PickerSegment(
                    modifier = Modifier.weight(1.4f),
                    index = 0, count = 2,
                    icon = Icons.Rounded.CalendarMonth,
                    label = stringResource(Res.string.tugatish_sanasi),
                    value = state.date?.let { dateText },
                    placeholder = "--.--.----",
                    onClick = { showDialogData = true },
                )
                PickerSegment(
                    modifier = Modifier.weight(1f),
                    index = 1, count = 2,
                    icon = Icons.Rounded.Schedule,
                    label = stringResource(Res.string.tugash_vaqti),
                    value = state.time?.let { timeText },
                    placeholder = "--:--",
                    onClick = { showDialogTime = true },
                )
            }

            // ── Muhimligi: bog'langan uch tugma; rang — kichik nuqtada
            ProfileSectionLabel(stringResource(Res.string.muhimligi))
            Row(horizontalArrangement = Arrangement.spacedBy(SegmentGap)) {
                val levels = listOf(ImportanceType.MEDIUM, ImportanceType.IMPORTANT, ImportanceType.MOST_IMPORTANT)
                levels.forEachIndexed { i, level ->
                    ImportanceSegment(
                        modifier = Modifier.weight(1f),
                        level = level,
                        index = i,
                        count = levels.size,
                        selected = state.importance == level,
                        onClick = { event(CreateTaskEvent.OnImportanceChange(level)) },
                    )
                }
            }

            // ── Mukofot: tangalar
            RewardSection(state = state, event = event)
        }

        // Davom etish — orol ustida, bosh barmoq yetadigan joyda (og'ir pastki panel o'rniga)
        Button(
            onClick = { navigator?.push(CreateTaskCheckScreen()) },
            enabled = isFormValid,
            interactionSource = rememberHapticSource(),
            shapes = expressiveButtonShapes(),
            colors = ButtonDefaults.buttonColors(
                containerColor = AppColors.button.primary,
                contentColor = AppColors.text.inverse,
                disabledContainerColor = AppColors.action.primary.copy(alpha = 0.14f),
                disabledContentColor = AppColors.text.tertiary,
            ),
            modifier = Modifier
                .padding(start = ContainerPadding, end = ContainerPadding, top = 4.dp, bottom = 8.dp)
                .fillMaxWidth()
                .height(56.dp),
        ) {
            Text(stringResource(Res.string.davom_etish), style = AppTypography.titleMdSemiBold)
        }
    }
}

/** Matn maydoni — guruh elementi ichida, ramkasiz (placeholder — xira). */
@Composable
private fun TaskTextSegment(
    index: Int,
    count: Int,
    value: String,
    placeholder: String,
    style: TextStyle,
    singleLine: Boolean,
    onValueChange: (String) -> Unit,
    minHeight: Dp = 0.dp,
) {
    val shape = segmentShape(index, count)
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = singleLine,
        textStyle = style.copy(color = AppColors.text.primary),
        cursorBrush = SolidColor(AppColors.action.primary),
        keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.Sentences,
            autoCorrectEnabled = true,
            keyboardType = KeyboardType.Text,
            imeAction = if (singleLine) ImeAction.Next else ImeAction.Default,
        ),
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = maxOf(minHeight, 64.dp))
            .clip(shape)
            .background(AppColors.bg.surface, shape),
        decorationBox = { inner ->
            Box(Modifier.padding(horizontal = 20.dp, vertical = 18.dp)) {
                if (value.isEmpty()) Text(placeholder, style = style, color = AppColors.text.tertiary)
                inner()
            }
        },
    )
}

/** Sana / vaqt tanlash — tonal doiradagi ikonka, xira yorliq va qiymat. */
@Composable
private fun PickerSegment(
    modifier: Modifier,
    index: Int,
    count: Int,
    icon: ImageVector,
    label: String,
    value: String?,
    placeholder: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = modifier
            .heightIn(min = 72.dp)
            .expressiveSegment(index, count, color = AppColors.bg.surface, horizontal = true, onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier.size(40.dp).clip(CircleShape).background(AppColors.action.primary.copy(alpha = 0.14f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, tint = AppColors.icon.accentPrimary, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(label, style = AppTypography.bodySmMedium, color = AppColors.text.tertiary, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(
                text = value ?: placeholder,
                style = AppTypography.titleMdSemiBold,
                color = if (value != null) AppColors.text.primary else AppColors.text.tertiary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

/** Muhimlik darajasi — tanlangani tonal va to'liq yumaloq; ma'nosi kichik rangli nuqtada. */
@Composable
private fun ImportanceSegment(
    modifier: Modifier,
    level: ImportanceType,
    index: Int,
    count: Int,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val (label, dot) = when (level) {
        ImportanceType.MEDIUM -> stringResource(Res.string.o_rtacha) to AppColors.text.accentSuccess
        ImportanceType.IMPORTANT -> stringResource(Res.string.muhim) to AppColors.text.accentWarning
        ImportanceType.MOST_IMPORTANT -> stringResource(Res.string.juda_muhim) to AppColors.text.accentDanger
        ImportanceType.NONE -> "" to AppColors.text.tertiary
    }
    Row(
        modifier = modifier
            .heightIn(min = 56.dp)
            .expressiveSegment(
                index, count,
                color = if (selected) AppColors.action.primary.copy(alpha = 0.18f) else AppColors.bg.surface,
                horizontal = true,
                selected = selected,
                debounceMs = 150L,
                onClick = onClick,
            )
            .padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Box(Modifier.size(8.dp).clip(CircleShape).background(dot))
        Spacer(Modifier.width(8.dp))
        Text(
            text = label,
            style = AppTypography.titleSmSemiBold,
            color = AppColors.text.primary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/** Mukofot: tayyor miqdorlar (pill), o'z miqdori va jami — hammasi bitta oqimda. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun RewardSection(state: CreateTaskState, event: (CreateTaskEvent) -> Unit) {
    val available = state.availableCoins
    val total = state.totalCoin
    val enabled = available > 0
    val maxExtra = (available - state.selectedChips.sum()).coerceAtLeast(0)

    Row(
        modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(Res.string.tangachalar),
            style = AppTypography.titleSmMedium,
            color = AppColors.text.tertiary,
            modifier = Modifier.weight(1f),
        )
        Image(painterResource(Res.drawable.coin), contentDescription = null, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(6.dp))
        Text(
            text = "${state.remainingCoins} ${stringResource(Res.string.ta)}",
            style = AppTypography.titleSmMedium,
            color = AppColors.text.tertiary,
        )
    }

    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        maxItemsInEachRow = 3,
        modifier = Modifier.fillMaxWidth(),
    ) {
        listOf(10, 25, 50, 100, 300, 500).forEach { amount ->
            val selected = amount in state.selectedChips
            val canSelect = enabled && (selected || total + amount <= available)
            Row(
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 48.dp)
                    .alpha(if (canSelect) 1f else 0.4f)
                    .clip(CircleShape)
                    .background(if (selected) AppColors.action.primary.copy(alpha = 0.18f) else AppColors.bg.surface)
                    .clickable(enabled = canSelect, interactionSource = rememberHapticSource(), indication = null) {
                        event(CreateTaskEvent.OnChipToggle(amount))
                    }
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                Image(painterResource(Res.drawable.coin), contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(6.dp))
                Text("$amount", style = AppTypography.titleSmSemiBold, color = AppColors.text.primary)
                if (selected) {
                    Spacer(Modifier.width(6.dp))
                    Icon(Icons.Rounded.Check, contentDescription = null, tint = AppColors.icon.accentPrimary, modifier = Modifier.size(16.dp))
                }
            }
        }
    }

    Spacer(Modifier.height(SegmentGap * 3))
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 72.dp)
            .alpha(if (enabled) 1f else 0.5f)
            .clip(RoundedCornerShape(28.dp))
            .background(AppColors.bg.surface)
            .padding(start = 20.dp, end = 12.dp, top = 12.dp, bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(Res.string.ozingiz_qoshing),
            style = AppTypography.titleMdSemiBold,
            color = AppColors.text.primary,
            modifier = Modifier.weight(1f),
        )
        CoinNumberPicker(
            value = state.extraCoin,
            max = maxExtra,
            onValueChanged = { event(CreateTaskEvent.OnExtraCoinChange(it)) },
        )
    }
    Text(
        text = "${stringResource(Res.string.vazifa_tangalari)}: $total ${stringResource(Res.string.ta)}",
        style = AppTypography.bodyMdMedium,
        color = AppColors.text.secondary,
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 10.dp),
    )
}

@Preview
@Composable
private fun Preview() {
    TikonchaParentTheme(
        ThemeMode.LIGHT
    ) {
        CreateTaskUI(
            navigator = null,
            state = CreateTaskState(),
            event = {}
        )
    }
}