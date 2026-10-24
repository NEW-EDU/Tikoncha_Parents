package uz.tikoncha_parent.presentation.task.create_task

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


@OptIn(ExperimentalMaterial3Api::class)
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .then(systemBars.modifier)
            .background(AppColors.bg.secondary)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
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

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(CardCornerPadding)
                    .verticalScroll(rememberScrollState())
            ) {

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(AppColors.modal.primary, RoundedCornerShape(24.dp))
                        .padding(12.dp)
                ) {
                    Text(
                        text = stringResource(Res.string.vazifa_nomi),
                        style = AppTypography.bodyMdMedium,
                        color = AppColors.text.primary,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                    Space(8.dp)

                    CustomTextField(
                        value = state.title,
                        style = AppTypography.titleSmMedium,
                        shape = RoundedCornerShape(20.dp),
                        label = stringResource(Res.string.vazifa_nomi),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        onValueChange = {
                            event(CreateTaskEvent.OnTitleChange(it))
                        },
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Sentences,
                            autoCorrectEnabled = true,
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Next
                        )
                    )
                    Space(12.dp)

                    Text(
                        text = stringResource(Res.string.izoh_ixtiyoriy),
                        style = AppTypography.bodyMdMedium,
                        color = AppColors.text.primary,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                    Space(8.dp)

                    CustomTextFieldTask(
                        minLine = true,
                        singleLine = false,
                        hasBorder = true,
                        value = state.desc,
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.fillMaxWidth().height(120.dp),
                        label = stringResource(Res.string.vazifa_haqida_qisqacha_ma_lumot),
                        onValueChange = {
                            event(CreateTaskEvent.OnDescChange(it))
                        }
                    )
                    Space(12.dp)

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth(0.65f)
                        ) {
                            Text(
                                text = stringResource(Res.string.tugatish_sanasi),
                                style = AppTypography.bodyMdMedium,
                                color = AppColors.text.primary,
                                modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
                            )

                            CustomSelectionButton(
                                text = dateText,
                                label = "--.--.----",
                                showTrailingIcon = false,
                                style = AppTypography.titleSmMedium,
                                tint = AppColors.icon.secondary,
                                shape = RoundedCornerShape(20.dp),
                                painter = painterResource(Res.drawable.calendar_2),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                onClick = {
                                    showDialogData = true
                                },
                            )
                        }
                        Space(12.dp)

                        Column(
                            modifier = Modifier
                                .fillMaxWidth(1f)
                        ) {
                            Text(
                                text = stringResource(Res.string.tugash_vaqti),
                                style = AppTypography.bodyMdMedium,
                                color = AppColors.text.primary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
                            )

                            CustomSelectionButton(
                                text = timeText,
                                label = "--:--",
                                tint = AppColors.icon.secondary,
                                style = AppTypography.titleSmMedium,
                                shape = RoundedCornerShape(20.dp),
                                painter = painterResource(Res.drawable.time_square),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                onClick = {
                                    showDialogTime = true
                                },
                                showTrailingIcon = false,
                            )
                        }
                    }
                }
                Space(16.dp)

                ImportanceSelector(
                    selected = state.importance,
                    onSelect = {
                        event(CreateTaskEvent.OnImportanceChange(it))
                    }
                )
                Space(16.dp)

                RewardCard(
                    state = state,
                    onEvent = event
                )
                Space(ButtonHeight + 24.dp)
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .bottomShadow(
                    shape = RoundedCornerShape(
                        topStart = ButtonCornerRadius,
                        topEnd = ButtonCornerRadius
                    ),
                    color = AppColors.bg.secondary
                )
                .background(
                    AppColors.bg.elevated,
                    RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                )
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .height(ButtonHeight)
                .align(Alignment.BottomCenter)
        ) {
            CustomButton(
                enabled = isFormValid,
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(Res.string.davom_etish),
                onClick = {
                    navigator?.push(CreateTaskCheckScreen())
                }
            )
        }
    }
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