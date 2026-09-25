package uz.tikoncha_parent.presentation.task

import androidx.compose.animation.core.animate
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleStartEffect
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.Navigator
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import tikoncha_parents.composeapp.generated.resources.Res
import tikoncha_parents.composeapp.generated.resources.bajarilgan
import tikoncha_parents.composeapp.generated.resources.bekor_qilish
import tikoncha_parents.composeapp.generated.resources.circle_clock
import tikoncha_parents.composeapp.generated.resources.dialog_failed
import tikoncha_parents.composeapp.generated.resources.farzand_vazifalari_desc
import tikoncha_parents.composeapp.generated.resources.farzandim
import tikoncha_parents.composeapp.generated.resources.farzandlaringiz
import tikoncha_parents.composeapp.generated.resources.hali_vazifa_yoq
import tikoncha_parents.composeapp.generated.resources.home_task
import tikoncha_parents.composeapp.generated.resources.hozir_vazifalar_yo_q
import tikoncha_parents.composeapp.generated.resources.jarayonda
import tikoncha_parents.composeapp.generated.resources.muddati_otgan
import tikoncha_parents.composeapp.generated.resources.ochirish
import tikoncha_parents.composeapp.generated.resources.ota_ona_vazifalari_desc
import tikoncha_parents.composeapp.generated.resources.ozim
import tikoncha_parents.composeapp.generated.resources.vazifa_ochirilsinmi
import tikoncha_parents.composeapp.generated.resources.vazifa_ochirish_tasdiq
import tikoncha_parents.composeapp.generated.resources.vazifa_qo_shish
import tikoncha_parents.composeapp.generated.resources.vazifalar
import tikoncha_parents.composeapp.generated.resources.xatolik
import uz.tikoncha_parent.domain.model.app_error.Outcome
import uz.tikoncha_parent.presentation.add_child.AddChildScreen
import uz.tikoncha_parent.presentation.base.ChildSelectionButton
import uz.tikoncha_parent.presentation.base.ConfirmationBottomSheet
import uz.tikoncha_parent.presentation.base.CustomButton
import uz.tikoncha_parent.presentation.base.CustomDialog
import uz.tikoncha_parent.presentation.base.CustomHeader
import uz.tikoncha_parent.presentation.base.ErrorRetryState
import uz.tikoncha_parent.presentation.base.PullToRefreshBox
import uz.tikoncha_parent.presentation.base.asText
import uz.tikoncha_parent.presentation.base.singleClick
import uz.tikoncha_parent.presentation.new_home.SelectionChildBottomSheet
import uz.tikoncha_parent.presentation.task.completed_task.CompletedTaskScreen
import uz.tikoncha_parent.presentation.task.create_task.CreateTaskScreen
import uz.tikoncha_parent.presentation.task.detail.TaskDetailScreen
import uz.tikoncha_parent.presentation.task.model.Task
import uz.tikoncha_parent.presentation.task.model.rememberSharedScreenModel
import uz.tikoncha_parent.ui.ButtonHeight
import uz.tikoncha_parent.ui.ContainerPadding
import uz.tikoncha_parent.ui.theme.AppColors
import uz.tikoncha_parent.ui.theme.AppTypography
import uz.tikoncha_parent.ui.theme.ThemeMode
import uz.tikoncha_parent.ui.theme.TikonchaParentTheme
import uz.tikoncha_parent.ui.theme.rememberScreenSystemBars

class TaskScreen : Screen {
    @Composable
    override fun Content() {

        val viewModel = rememberSharedScreenModel<TaskListViewModel>()
        val state by viewModel.state.collectAsState()
        val event = viewModel::onEvent
        val navigator = LocalNavigator.current ?: return

        // ✅ Effect'larni collect qilish — error/success uchun
        var localFailure by remember { mutableStateOf<Outcome.Failure?>(null) }

        LaunchedEffect(Unit) {
            viewModel.effect.collect { effect ->
                when (effect) {
                    is TaskListEffect.ShowFailure -> localFailure = effect.failure
                    TaskListEffect.TaskDeleted -> {}
                    TaskListEffect.TaskMarkedAsCompleted -> {}
                }
            }
        }

        LifecycleStartEffect(Unit) {
            event(TaskListEvent.SyncChildren)
            event(TaskListEvent.LoadTasks)
            onStopOrDispose {}
        }

        val shownError = localFailure?.asText()

        CustomDialog(
            painter = painterResource(Res.drawable.dialog_failed),
            show = shownError != null,
            title = stringResource(Res.string.xatolik),
            message = shownError.orEmpty(),
            onDismiss = { localFailure = null },
            onButtonClick = { localFailure = null }
        )

        TaskUi(
            navigator = navigator,
            state = state,
            event = event
        )
    }
}

@Composable
fun TaskUi(
    navigator: Navigator?,
    state: TaskListState,
    event: (TaskListEvent) -> Unit
) {
    val isParentTab = state.taskIndex == 0
    var showChildSelector by remember { mutableStateOf(false) }
    var taskToDelete by remember { mutableStateOf<Task?>(null) }
    val loadFailure = state.error

    // ── Bottom sheetlar ─────────────────────────────────────
    if (showChildSelector) {
        SelectionChildBottomSheet(
            navigator = navigator,
            items = state.childrenList,
            selectedItem = state.selectedChild,
            onDismiss = { showChildSelector = false },
            title = stringResource(Res.string.farzandlaringiz),
            onItemSelected = {
                event(TaskListEvent.OnChildSelected(it))
                showChildSelector = false
            }
        )
    }

    taskToDelete?.let { task ->
        ConfirmationBottomSheet(
            title = stringResource(Res.string.vazifa_ochirilsinmi),
            subtitle = stringResource(Res.string.vazifa_ochirish_tasdiq),
            confirmText = stringResource(Res.string.ochirish),
            cancelText = stringResource(Res.string.bekor_qilish),
            onDismiss = { taskToDelete = null },
            onConfirm = {
                event(TaskListEvent.OnDeleteTask(task))
                taskToDelete = null
            }
        )
    }

    // ── System bars ─────────────────────────────────────────
    val systemBars = rememberScreenSystemBars(
        statusBarColor = AppColors.bg.secondary,
        navigationBarColor = AppColors.bg.elevated
    )

    // ── List state + pagination ─────────────────────────────
    val listState = rememberLazyListState()

    LaunchedEffect(listState, state.taskList.size, state.hasMore, state.isPaginating) {
        snapshotFlow {
            val layoutInfo = listState.layoutInfo
            val total = layoutInfo.totalItemsCount
            val lastVisible = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: -1
            val taskItemsExist = state.taskList.isNotEmpty()
            val nearEnd = total > 2 && lastVisible >= total - 3
            taskItemsExist && nearEnd && state.hasMore && !state.isPaginating
        }
            .distinctUntilChanged()
            .filter { it }
            .collect { event(TaskListEvent.OnLoadMore) }
    }

    // ── Collapsing header setup ─────────────────────────────
    val density = LocalDensity.current
    val headerHeight = 56.dp
    val headerHeightPx = with(density) { headerHeight.toPx() }
    val headerOffsetPx = remember { mutableFloatStateOf(0f) }
    val pullState = rememberPullToRefreshState()

    val collapseConnection = remember(headerHeightPx, pullState, listState) {
        object : NestedScrollConnection {

            // Joriy tortishda header biror lahza yashirin bo'lganmi.
            // Bo'lgan bo'lsa — shu tortish faqat header'ni ochadi, refresh keyingi tortishda.
            private var headerHiddenInThisDrag = false

            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                val dy = available.y
                // Indikator tortilgan bo'lsa, yuqoriga harakatni avval u qaytarib olsin
                if (dy < 0 && pullState.distanceFraction > 0f) return Offset.Zero

                val isUserDrag = source == NestedScrollSource.UserInput
                val old = headerOffsetPx.floatValue
                if (isUserDrag && old < 0f) headerHiddenInThisDrag = true

                // Header ro'yxat va pull-to-refresh'dan OLDIN o'zgaradi (yig'iladi yoki ochiladi)
                val new = (old + dy).coerceIn(-headerHeightPx, 0f)
                headerOffsetPx.floatValue = new
                val byHeader = new - old

                // Header yashirin holatdan ochilayotgan tortishda refresh tushmasin:
                // ro'yxat tepaga yetgach, qolgan pastga harakatni o'zimiz yutamiz — PTR'ga yetmaydi.
                if (isUserDrag && headerHiddenInThisDrag && dy > 0 && listState.firstVisibleItemIndex == 0) {
                    val leftAfterHeader = dy - byHeader
                    val listCanTake = listState.firstVisibleItemScrollOffset.toFloat()
                    val swallow = (leftAfterHeader - listCanTake).coerceAtLeast(0f)
                    return Offset(0f, byHeader + swallow)
                }
                return Offset(0f, byHeader)
            }

            // Barmoq qo'yib yuborildi — keyingi tortish yangidan baholanadi
            override suspend fun onPreFling(available: Velocity): Velocity {
                headerHiddenInThisDrag = false
                return Velocity.Zero
            }

            // Header yarim holatda qolmasin — yaqin chetiga silliq suriladi
            override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
                val current = headerOffsetPx.floatValue
                if (current != 0f && current != -headerHeightPx) {
                    val target = if (current > -headerHeightPx / 2) 0f else -headerHeightPx
                    animate(initialValue = current, targetValue = target) { value, _ ->
                        headerOffsetPx.floatValue = value
                    }
                }
                return Velocity.Zero
            }
        }
    }

    val headerCurrentHeight = with(density) {
        (headerHeightPx + headerOffsetPx.floatValue).toDp()
    }

    val headerAlpha by remember {
        derivedStateOf {
            ((headerHeightPx + headerOffsetPx.floatValue) / headerHeightPx).coerceIn(0f, 1f)
        }
    }

    // ── UI ──────────────────────────────────────────────────
    Box(
        modifier = Modifier
            .fillMaxSize()
            .then(systemBars.modifier)
            .background(AppColors.bg.secondary)
    ) {
        // Yangilash indikatori butun ekran tepasidan — header ustidan tushadi
        PullToRefreshBox(
            isRefreshing = state.isRefreshing,
            onRefresh = { event(TaskListEvent.OnRefresh) },
            state = pullState,
            modifier = Modifier.fillMaxSize(),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .nestedScroll(collapseConnection)
            ) {
            // ── Collapsing Header ──
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(headerCurrentHeight)
                    .graphicsLayer { alpha = headerAlpha }
                    .clipToBounds()
            ) {
                CustomHeader(
                    showBackButton = true,
                    onBackClick = { navigator?.pop() },
                    title = stringResource(Res.string.vazifalar),
                    trailingIcon = {
                        Icon(
                            painter = painterResource(Res.drawable.circle_clock),
                            contentDescription = null,
                            tint = AppColors.icon.accentPrimary,
                            modifier = Modifier
                                .size(24.dp)
                                .singleClick {
                                    navigator?.push(CompletedTaskScreen())
                                }
                        )
                    }
                )
            }

            // ── Sticky Toggle ──
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = ContainerPadding)
            ) {
                Spacer(Modifier.height(8.dp))
                TaskSegmentedToggle(
                    selectedIndex = state.taskIndex,
                    modifier = Modifier.fillMaxWidth(),
                    onOptionSelected = { event(TaskListEvent.OnTaskSelected(it)) },
                    options = listOf(
                        stringResource(Res.string.ozim) to null,
                        stringResource(Res.string.farzandim) to null
                    )
                )
            }

            // ── Refilter loading indicator ──
            if (state.isRefiltering) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.dp),
                    color = AppColors.icon.accentPrimary,
                    trackColor = Color.Transparent,
                )
            } else {
                Spacer(Modifier.height(2.dp))
            }

            // ── Scrollable: ChildSelector + StatusChips + List ──
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                    state = listState,
                    contentPadding = PaddingValues(
                        start = ContainerPadding,
                        end = ContainerPadding,
                        top = 12.dp,
                        bottom = ButtonHeight + ContainerPadding * 2,
                    ),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    item(key = "child-selector") {
                        ChildSelectionButton(
                            text = state.selectedChild?.name.orEmpty(),
                            label = stringResource(Res.string.farzandlaringiz),
                            imageUrl = state.selectedChild?.avatarUrl.orEmpty(),
                            modifier = Modifier.fillMaxWidth(),
                            userInfo = state.selectedChild,
                            onClick = {
                                if (state.childrenList.isEmpty()) navigator?.push(AddChildScreen())
                                else showChildSelector = true
                            }
                        )
                    }

                    item(key = "status-chips") {
                        StatusChips(
                            items = listOf(
                                TaskFilterChip.IN_PROGRESS to stringResource(Res.string.jarayonda),
                                TaskFilterChip.DONE_BY_CHILD to stringResource(Res.string.bajarilgan),
                                TaskFilterChip.OVERDUE to stringResource(Res.string.muddati_otgan),
                            ),
                            selected = state.activeChip,
                            onChipClick = { chip -> event(TaskListEvent.OnFilterChipToggled(chip)) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    when {
                        state.isInitialLoading -> {
                            items(count = 5, key = { "shimmer-$it" }) {
                                TaskCardItemShimmer()
                            }
                        }

                        state.selectedChild == null -> {
                            item(key = "no-child") {
                                EmptyTaskState(
                                    title = stringResource(Res.string.farzandlaringiz),
                                    subtitle = stringResource(Res.string.farzand_vazifalari_desc),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .fillParentMaxHeight(0.7f),
                                )
                            }
                        }

                        loadFailure != null && state.taskList.isEmpty() -> {
                            item(key = "load-failed") {
                                ErrorRetryState(
                                    failure = loadFailure,
                                    onRetry = { event(TaskListEvent.OnRetry) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .fillParentMaxHeight(0.7f),
                                )
                            }
                        }

                        state.taskList.isEmpty() -> {
                            item(key = "empty") {
                                EmptyTaskState(
                                    title = if (isParentTab)
                                        stringResource(Res.string.hali_vazifa_yoq)
                                    else
                                        stringResource(Res.string.hozir_vazifalar_yo_q),
                                    subtitle = if (isParentTab)
                                        stringResource(Res.string.ota_ona_vazifalari_desc)
                                    else
                                        stringResource(Res.string.farzand_vazifalari_desc),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .fillParentMaxHeight(0.7f),
                                )
                            }
                        }

                        else -> {
                            items(items = state.taskList, key = { it.id }) { task ->
                                TaskCardItem(
                                    task = task,
                                    onDeleteClick = { taskToDelete = it },
                                    isDeleting = task.id in state.deletingIds,
                                    isCompleting = task.id in state.completingIds,
                                    onDetailsIconClick = {
                                        navigator?.push(TaskDetailScreen(task))
                                    },
                                    onDoneButtonClick = {
                                        event(TaskListEvent.OnCompletedTask(task))
                                    },
                                    onEditIconClick = {
                                        navigator?.push(CreateTaskScreen(task))
                                    }
                                )
                            }

                            if (state.isPaginating) {
                                item(key = "pagination-loader") {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 16.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator(
                                            strokeWidth = 2.dp,
                                            modifier = Modifier.size(24.dp),
                                            color = AppColors.icon.accentPrimary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // ── Pastki tugma (fixed) ────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    AppColors.bg.elevated,
                    RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                )
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .height(ButtonHeight)
                .align(Alignment.BottomCenter)
        ) {
            CustomButton(
                enabled = state.selectedChild != null,
                text = stringResource(Res.string.vazifa_qo_shish),
                onClick = { navigator?.push(CreateTaskScreen()) },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun EmptyTaskState(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(Res.drawable.home_task),
            contentDescription = null,
            modifier = Modifier.size(90.dp)
        )
        Spacer(Modifier.height(26.dp))

        Text(
            text = title,
            style = AppTypography.titleSmSemiBold,
            color = AppColors.text.primary,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(12.dp))

        Text(
            text = subtitle,
            style = AppTypography.emphasizedMdMedium,
            color = AppColors.text.secondary,
            textAlign = TextAlign.Center
        )
    }
}

@Preview
@Composable
private fun Preview() {
    TikonchaParentTheme(
        ThemeMode.LIGHT
    ) {
        TaskUi(
            state = TaskListState(),
            event = {},
            navigator = LocalNavigator.current
        )
    }
}