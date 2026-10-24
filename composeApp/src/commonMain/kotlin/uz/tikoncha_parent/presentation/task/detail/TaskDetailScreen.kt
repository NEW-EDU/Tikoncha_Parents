package uz.tikoncha_parent.presentation.task.detail

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
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import tikoncha_parents.composeapp.generated.resources.bekor_qilish
import tikoncha_parents.composeapp.generated.resources.dialog_failed
import tikoncha_parents.composeapp.generated.resources.farzandlaringiz
import tikoncha_parents.composeapp.generated.resources.izoh
import tikoncha_parents.composeapp.generated.resources.juda_muhim
import tikoncha_parents.composeapp.generated.resources.message_delete
import tikoncha_parents.composeapp.generated.resources.message_edit
import tikoncha_parents.composeapp.generated.resources.muhim
import tikoncha_parents.composeapp.generated.resources.muhimlilik_darajasi
import tikoncha_parents.composeapp.generated.resources.o_rtacha
import tikoncha_parents.composeapp.generated.resources.ochirish
import tikoncha_parents.composeapp.generated.resources.ragbatlantirish_tangachalari
import tikoncha_parents.composeapp.generated.resources.ta
import tikoncha_parents.composeapp.generated.resources.tahrirlash
import tikoncha_parents.composeapp.generated.resources.tugatish_sanasi
import tikoncha_parents.composeapp.generated.resources.tugatish_vaqti
import tikoncha_parents.composeapp.generated.resources.vazifa_nomi
import tikoncha_parents.composeapp.generated.resources.vazifa_ochirilsinmi
import tikoncha_parents.composeapp.generated.resources.vazifa_ochirish_tasdiq
import tikoncha_parents.composeapp.generated.resources.vazifani_tekshirish
import tikoncha_parents.composeapp.generated.resources.xatolik
import uz.tikoncha_parent.common.DateTimeUtil.formatTime
import uz.tikoncha_parent.common.DateTimeUtil.reformattedDayMonthWithWeekdayForTask
import uz.tikoncha_parent.common.Util.millisToLocalDate
import uz.tikoncha_parent.common.Util.millisToLocalTime
import uz.tikoncha_parent.domain.model.UserInfo
import uz.tikoncha_parent.domain.model.app_error.Outcome
import uz.tikoncha_parent.presentation.base.ChildSelectionButton
import uz.tikoncha_parent.presentation.base.ConfirmationBottomSheet
import uz.tikoncha_parent.presentation.base.CustomButton
import uz.tikoncha_parent.presentation.base.CustomDialog
import uz.tikoncha_parent.presentation.base.CustomHeader
import uz.tikoncha_parent.presentation.base.LoadingDialog
import uz.tikoncha_parent.presentation.base.asText
import uz.tikoncha_parent.presentation.base.bottomShadow
import uz.tikoncha_parent.presentation.domain.model.Subscription
import uz.tikoncha_parent.presentation.task.TaskListEffect
import uz.tikoncha_parent.presentation.task.TaskListEvent
import uz.tikoncha_parent.presentation.task.TaskListViewModel
import uz.tikoncha_parent.presentation.task.create_task.CreateTaskScreen
import uz.tikoncha_parent.presentation.task.model.ImportanceType
import uz.tikoncha_parent.presentation.task.model.Task
import uz.tikoncha_parent.presentation.task.model.rememberSharedScreenModel
import uz.tikoncha_parent.ui.ButtonCornerRadius
import uz.tikoncha_parent.ui.ButtonHeight
import uz.tikoncha_parent.ui.Space
import uz.tikoncha_parent.ui.TextFieldHeight
import uz.tikoncha_parent.ui.theme.AppColors
import uz.tikoncha_parent.ui.theme.AppTypography
import uz.tikoncha_parent.ui.theme.ThemeMode
import uz.tikoncha_parent.ui.theme.TikonchaParentTheme
import uz.tikoncha_parent.ui.theme.rememberScreenSystemBars

class TaskDetailScreen(
    private val task: Task
): Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.current
        val listViewModel = rememberSharedScreenModel<TaskListViewModel>()
        val listState by listViewModel.state.collectAsStateWithLifecycle()
        val listEvent = listViewModel::onEvent

        val currentTask = remember(listState.taskList, task.id) {
            listState.taskList.firstOrNull { it.id == task.id } ?: task
        }
        val taskChild = remember(listState.childrenList, currentTask.targetUserId) {
            listState.childrenList.firstOrNull { it.userId == currentTask.targetUserId }
        }

        var taskToDelete by remember { mutableStateOf<Task?>(null) }
        var failure by remember { mutableStateOf<Outcome.Failure?>(null) }
        val isDeleting = currentTask.id in listState.deletingIds
        LoadingDialog(isDeleting)

        // O'chirish muvaffaqiyatli bo'lsa — orqaga qaytamiz
        LaunchedEffect(Unit) {
            listViewModel.effect.collect { effect ->
                when (effect) {
                    TaskListEffect.TaskDeleted -> navigator?.pop()
                    is TaskListEffect.ShowFailure -> failure = effect.failure
                    else -> Unit
                }
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

        taskToDelete?.let { t ->
            ConfirmationBottomSheet(
                title = stringResource(Res.string.vazifa_ochirilsinmi),
                subtitle = stringResource(Res.string.vazifa_ochirish_tasdiq),
                confirmText = stringResource(Res.string.ochirish),
                cancelText = stringResource(Res.string.bekor_qilish),
                onDismiss = { taskToDelete = null },
                onConfirm = {
                    listEvent(TaskListEvent.OnDeleteTask(t))
                    taskToDelete = null
                }
            )
        }

        TaskDetailUI(
            navigator = navigator,
            task = currentTask,
            subscription = listState.selectedChild,
            childName = taskChild?.name.orEmpty(),
            childAvatarUrl = taskChild?.avatarUrl.orEmpty(),
            onEditClick = {
                navigator?.push(CreateTaskScreen(currentTask))
            },
            onDeleteClick = { taskToDelete = currentTask }
        )
    }
}

@Composable
fun TaskDetailUI(
    task: Task,
    childName: String,
    childAvatarUrl: String,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    navigator: Navigator? = null,
    subscription: UserInfo? = null
) {
    val systemBars = rememberScreenSystemBars(
        statusBarColor = AppColors.bg.secondary,
        navigationBarColor = AppColors.bg.elevated
    )
    val date = remember(task.dateTime) { millisToLocalDate(task.dateTime) }
    val time = remember(task.dateTime) { millisToLocalTime(task.dateTime) }

    val dateText = reformattedDayMonthWithWeekdayForTask(date) ?: "—"
    val timeText = formatTime(time) ?: "—"

    val importanceText = when (task.importance) {
        ImportanceType.MOST_IMPORTANT -> stringResource(Res.string.juda_muhim)
        ImportanceType.IMPORTANT      -> stringResource(Res.string.muhim)
        ImportanceType.MEDIUM         -> stringResource(Res.string.o_rtacha)
        ImportanceType.NONE           -> "—"
    }

    val importanceColor = when (task.importance) {
        ImportanceType.MOST_IMPORTANT -> AppColors.text.accentDanger
        ImportanceType.IMPORTANT      -> AppColors.text.accentWarning
        ImportanceType.MEDIUM         -> AppColors.text.accentSuccess
        ImportanceType.NONE           -> Color.Transparent
    }

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
                    text = childName,
                    imageUrl = childAvatarUrl,
                    label = stringResource(Res.string.farzandlaringiz),
                    userInfo = subscription,
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
                        text = task.title,
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
                        text = task.description.ifBlank { "—" },
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
                                .background(importanceColor)
                        )
                        Space(4.dp)

                        Text(
                            text = importanceText,
                            style = AppTypography.titleMdMedium,
                            color = importanceColor
                        )
                    }
                }
                Space(8.dp)

                if(task.coin != 0) {
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
                            text = "${task.coin} ${stringResource(Res.string.ta)}",
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                CustomButton(
                    color = AppColors.section.secondary,
                    textColor = AppColors.text.primary,
                    modifier = Modifier.weight(1f),
                    text = stringResource(Res.string.tahrirlash),
                    onClick = onEditClick,
                    leadingIcon = {
                        Icon(
                            painter = painterResource(Res.drawable.message_edit),
                            contentDescription = null,
                            tint = AppColors.icon.secondary
                        )
                    }
                )
                CustomButton(
                    color = AppColors.text.accentDanger,
                    modifier = Modifier.weight(1f),
                    text = stringResource(Res.string.ochirish),
                    onClick = onDeleteClick,
                    leadingIcon = {
                        Icon(
                            painter = painterResource(Res.drawable.message_delete),
                            contentDescription = null,
                            tint = AppColors.icon.inverse
                        )
                    }
                )
            }
        }
    }
}

@Preview
@Composable
fun TaskDetailPreview() {
    TikonchaParentTheme(
        ThemeMode.DARK
    ) {
        TaskDetailUI(
            task = Task(
                title = "Test",
                description = "Test",
                dateTime = 56516516,
                importance = ImportanceType.MOST_IMPORTANT,
                isCompleted = false,
                canUpdate = false,
                authorId = "",
                targetUserId = "",
                createdAt = 56516516,
                coin = 100
            ),
            childName = "Farrux",
            childAvatarUrl = "",
            onEditClick = {},
            onDeleteClick = {}
        )
    }
}