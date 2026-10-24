package uz.tikoncha_parent.presentation.task.create_task_check

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.Navigator
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import tikoncha_parents.composeapp.generated.resources.Res
import tikoncha_parents.composeapp.generated.resources.dialog_failed
import tikoncha_parents.composeapp.generated.resources.farzandlaringiz
import tikoncha_parents.composeapp.generated.resources.izoh
import tikoncha_parents.composeapp.generated.resources.juda_muhim
import tikoncha_parents.composeapp.generated.resources.muhim
import tikoncha_parents.composeapp.generated.resources.muhimlilik_darajasi
import tikoncha_parents.composeapp.generated.resources.o_rtacha
import tikoncha_parents.composeapp.generated.resources.ragbatlantirish_tangachalari
import tikoncha_parents.composeapp.generated.resources.ta
import tikoncha_parents.composeapp.generated.resources.tugatish_sanasi
import tikoncha_parents.composeapp.generated.resources.tugatish_vaqti
import tikoncha_parents.composeapp.generated.resources.vazifa_nomi
import tikoncha_parents.composeapp.generated.resources.vazifani_saqlash
import tikoncha_parents.composeapp.generated.resources.vazifani_tekshirish
import tikoncha_parents.composeapp.generated.resources.xatolik
import uz.tikoncha_parent.common.DateTimeUtil.formatTime
import uz.tikoncha_parent.common.DateTimeUtil.reformattedDayMonthWithWeekdayForTask
import uz.tikoncha_parent.domain.model.app_error.Outcome
import uz.tikoncha_parent.presentation.base.ChildSelectionButton
import uz.tikoncha_parent.presentation.base.CustomButtonNew
import uz.tikoncha_parent.presentation.base.CustomDialog
import uz.tikoncha_parent.presentation.base.CustomHeader
import uz.tikoncha_parent.presentation.base.LoadingDialog
import uz.tikoncha_parent.presentation.base.asText
import uz.tikoncha_parent.presentation.base.bottomShadow
import uz.tikoncha_parent.presentation.task.create_task.CreateTaskEffect
import uz.tikoncha_parent.presentation.task.create_task.CreateTaskEvent
import uz.tikoncha_parent.presentation.task.create_task.CreateTaskState
import uz.tikoncha_parent.presentation.task.create_task.CreateTaskViewModel
import uz.tikoncha_parent.presentation.task.model.CollectEffects
import uz.tikoncha_parent.presentation.task.model.ImportanceType
import uz.tikoncha_parent.presentation.task.model.rememberSharedScreenModel
import uz.tikoncha_parent.presentation.task.success.TaskSuccessScreen
import uz.tikoncha_parent.presentation.ui_state.ResponseState
import uz.tikoncha_parent.ui.ButtonCornerRadius
import uz.tikoncha_parent.ui.ButtonHeight
import uz.tikoncha_parent.ui.Space
import uz.tikoncha_parent.ui.theme.AppColors
import uz.tikoncha_parent.ui.theme.AppTypography
import uz.tikoncha_parent.ui.theme.ThemeMode
import uz.tikoncha_parent.ui.theme.TikonchaParentTheme
import uz.tikoncha_parent.ui.theme.rememberScreenSystemBars

class CreateTaskCheckScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.current
        val viewModel = rememberSharedScreenModel<CreateTaskViewModel>()
        val state by viewModel.state.collectAsStateWithLifecycle()
        val event = viewModel::onEvent

        var failure by remember { mutableStateOf<Outcome.Failure?>(null) }
        val taskLoading = state.taskResponseState is ResponseState.Loading
        LoadingDialog(taskLoading)

        CollectEffects(viewModel.effect) { effect ->
            when (effect) {
                CreateTaskEffect.NavigateToSuccess -> {
                    navigator?.push(TaskSuccessScreen())
                }
                is CreateTaskEffect.ShowFailure -> {
                    failure = effect.failure
                }
                CreateTaskEffect.NavigateBack -> {
                    navigator?.pop()
                }
                is CreateTaskEffect.ShowSuccessDialog -> { /* bu yerga kelmaydi */ }
            }
        }

        val errorText = failure?.asText()

        CustomDialog(
            painter = painterResource(Res.drawable.dialog_failed),
            onDismiss = { failure = null },
            show = errorText != null,
            title = stringResource(Res.string.xatolik),
            message = errorText.orEmpty(),
            onButtonClick = { failure = null }
        )

        CreateTaskCheckUI(
            navigator = navigator,
            state = state,
            event = event
        )
    }
}

@Composable
fun CreateTaskCheckUI(
    state: CreateTaskState,
    event: (CreateTaskEvent) -> Unit,
    navigator: Navigator? = null
) {
    val systemBars = rememberScreenSystemBars(
        statusBarColor = AppColors.bg.secondary,
        navigationBarColor = AppColors.bg.elevated
    )
    val dateText = state.date?.let { reformattedDayMonthWithWeekdayForTask(it) } ?: "—"
    val timeText = state.time?.let { formatTime(it) } ?: "—"

    val importanceText = when (state.importance) {
        ImportanceType.MOST_IMPORTANT -> stringResource(Res.string.juda_muhim)
        ImportanceType.IMPORTANT -> stringResource(Res.string.muhim)
        ImportanceType.MEDIUM -> stringResource(Res.string.o_rtacha)
        ImportanceType.NONE -> "—"
    }

    val importanceTextColor = when (state.importance) {
        ImportanceType.MOST_IMPORTANT -> AppColors.text.accentDanger
        ImportanceType.IMPORTANT -> AppColors.text.accentWarning
        ImportanceType.MEDIUM -> AppColors.text.accentSuccess
        ImportanceType.NONE -> Color.Transparent
    }

    // ✅ Saqlash tugmasi shartlari
    val canSave = state.title.isNotBlank() &&
            state.date != null &&
            state.time != null &&
            state.importance != ImportanceType.NONE &&
            state.selectedChild != null &&
            state.taskResponseState !is ResponseState.Loading

    Box(
        modifier = Modifier
            .fillMaxSize()
            .then(systemBars.modifier)
            .background(AppColors.bg.secondary)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            CustomHeader(
                title = stringResource(Res.string.vazifani_tekshirish),
                showBackButton = true,
                onBackClick = { navigator?.pop() }
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                ChildSelectionButton(
                    onClick = {},
                    trailingIcon = false,
                    text = state.selectedChild?.name.orEmpty(),
                    imageUrl = state.selectedChild?.avatarUrl.orEmpty(),
                    label = stringResource(Res.string.farzandlaringiz),
                    userInfo = state.selectedChild,
                    modifier = Modifier.fillMaxWidth()
                )
                Space(12.dp)

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(AppColors.modal.primary, RoundedCornerShape(24.dp))
                        .padding(20.dp)
                ) {
                    Text(
                        text = stringResource(Res.string.vazifa_nomi),
                        style = AppTypography.bodyMdMedium,
                        color = AppColors.text.secondary
                    )
                    Space(8.dp)

                    Text(
                        text = state.title,
                        style = AppTypography.titleMdMedium,
                        color = AppColors.text.primary
                    )

                    Space(16.dp)
                    HorizontalDivider()
                    Space(16.dp)

                    Text(
                        text = stringResource(Res.string.izoh),
                        style = AppTypography.bodyMdMedium,
                        color = AppColors.text.secondary
                    )
                    Space(8.dp)

                    Text(
                        text = state.desc,
                        style = AppTypography.titleMdMedium,
                        color = AppColors.text.primary
                    )

                    Space(16.dp)
                    HorizontalDivider()
                    Space(16.dp)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = stringResource(Res.string.tugatish_sanasi),
                                style = AppTypography.bodyMdMedium,
                                color = AppColors.text.secondary
                            )
                            Space(8.dp)

                            Text(
                                text = dateText,
                                style = AppTypography.titleMdMedium,
                                color = AppColors.text.primary
                            )
                        }

                        Column {
                            Text(
                                text = stringResource(Res.string.tugatish_vaqti),
                                style = AppTypography.bodyMdMedium,
                                color = AppColors.text.secondary
                            )
                            Space(8.dp)

                            Text(
                                text = timeText,
                                style = AppTypography.titleMdMedium,
                                color = AppColors.text.primary
                            )
                        }
                    }

                    Space(16.dp)
                    HorizontalDivider()
                    Space(16.dp)
                    Text(
                        text = stringResource(Res.string.muhimlilik_darajasi),
                        style = AppTypography.bodyMdMedium,
                        color = AppColors.text.secondary
                    )
                    Space(8.dp)

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(importanceTextColor)
                        )
                        Space(4.dp)

                        Text(
                            text = importanceText,
                            style = AppTypography.titleMdMedium,
                            color = importanceTextColor
                        )
                    }
                }
                Space(8.dp)

                if(state.totalCoin != 0){
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(AppColors.modal.primary, RoundedCornerShape(24.dp))
                            .padding(20.dp)
                    ) {
                        Text(
                            text = stringResource(Res.string.ragbatlantirish_tangachalari),
                            style = AppTypography.bodyMdMedium,
                            color = AppColors.text.secondary
                        )
                        Space(8.dp)

                        Text(
                            text = "${state.totalCoin} ${stringResource(Res.string.ta)}",
                            style = AppTypography.titleMdMedium,
                            color = AppColors.text.primary
                        )
                    }
                }
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
            CustomButtonNew(
                enabled = canSave,
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(Res.string.vazifani_saqlash),
                onClick = {
                    event(CreateTaskEvent.OnConfirmClicked)
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
        CreateTaskCheckUI(
            state = CreateTaskState(),
            event = {}
        )
    }
}