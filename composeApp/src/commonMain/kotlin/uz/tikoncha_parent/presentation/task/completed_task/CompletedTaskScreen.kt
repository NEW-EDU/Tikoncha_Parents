package uz.tikoncha_parent.presentation.task.completed_task

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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import uz.tikoncha_parent.presentation.base.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleStartEffect
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.Navigator
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import tikoncha_parents.composeapp.generated.resources.Res
import tikoncha_parents.composeapp.generated.resources.bajarilgan_vazifalar
import tikoncha_parents.composeapp.generated.resources.dialog_failed
import tikoncha_parents.composeapp.generated.resources.farzand_vazifalari_desc
import tikoncha_parents.composeapp.generated.resources.farzandim
import tikoncha_parents.composeapp.generated.resources.farzandlaringiz
import tikoncha_parents.composeapp.generated.resources.hali_vazifa_yoq
import tikoncha_parents.composeapp.generated.resources.home_task
import tikoncha_parents.composeapp.generated.resources.ozim
import tikoncha_parents.composeapp.generated.resources.xatolik
import uz.tikoncha_parent.domain.model.app_error.Outcome
import uz.tikoncha_parent.presentation.base.CustomDialog
import uz.tikoncha_parent.presentation.base.CustomHeader
import uz.tikoncha_parent.presentation.base.ErrorRetryState
import uz.tikoncha_parent.presentation.base.asText
import uz.tikoncha_parent.presentation.task.TaskSegmentedToggle
import uz.tikoncha_parent.ui.ContainerPadding
import uz.tikoncha_parent.ui.theme.AppColors
import uz.tikoncha_parent.ui.theme.AppTypography
import uz.tikoncha_parent.ui.theme.ThemeMode
import uz.tikoncha_parent.ui.theme.TikonchaParentTheme
import uz.tikoncha_parent.ui.theme.rememberScreenSystemBars

class CompletedTaskScreen : Screen {
    @Composable
    override fun Content() {

        val viewModel = koinScreenModel<CompletedTaskViewModel>()
        val state by viewModel.state.collectAsState()
        val event = viewModel::onEvent
        val navigator = LocalNavigator.current

        // ── Effect'lar (error dialog) ──────────────────────
        var localFailure by remember { mutableStateOf<Outcome.Failure?>(null) }

        LaunchedEffect(Unit) {
            viewModel.effect.collect { eff ->
                when (eff) {
                    is CompletedTaskEffect.ShowFailure -> localFailure = eff.failure
                }
            }
        }

        LifecycleStartEffect(Unit) {
            event(CompletedTaskEvent.LoadTasks)
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

        CompletedTaskUi(
            state = state,
            event = event,
            navigator = navigator
        )
    }
}

@Composable
fun CompletedTaskUi(
    navigator: Navigator?,
    state: CompletedTaskState,
    event: (CompletedTaskEvent) -> Unit
) {
    val loadFailure = state.error
    val systemBars = rememberScreenSystemBars(
        statusBarColor = AppColors.bg.secondary,
        navigationBarColor = AppColors.bg.secondary
    )

    // ── Pagination ──
    val listState = rememberLazyListState()
    LaunchedEffect(listState, state.taskList.size, state.hasMore, state.isPaginating) {
        snapshotFlow {
            val layoutInfo = listState.layoutInfo
            val total = layoutInfo.totalItemsCount
            val lastVisible = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: -1
            val taskItemsExist = state.taskList.isNotEmpty()
            val nearEnd = total > 0 && lastVisible >= total - 3
            taskItemsExist && nearEnd && state.hasMore && !state.isPaginating
        }
            .distinctUntilChanged()
            .filter { it }
            .collect { event(CompletedTaskEvent.OnLoadMore) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .then(systemBars.modifier)
            .background(AppColors.bg.secondary)
    ) {
        CustomHeader(
            showBackButton = true,
            onBackClick = { navigator?.pop() },
            title = stringResource(Res.string.bajarilgan_vazifalar),
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = ContainerPadding)
        ) {
            Spacer(Modifier.height(10.dp))
            TaskSegmentedToggle(
                selectedIndex = state.taskIndex,
                modifier = Modifier.fillMaxWidth(),
                onOptionSelected = { event(CompletedTaskEvent.OnTabSelected(it)) },
                options = listOf(
                    stringResource(Res.string.ozim) to null,
                    stringResource(Res.string.farzandim) to null
                )
            )
            Spacer(Modifier.height(12.dp))
        }

        // ── List ──
        PullToRefreshBox(
            isRefreshing = state.isRefreshing,
            onRefresh = { event(CompletedTaskEvent.OnRefresh) },
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                state = listState,
                contentPadding = PaddingValues(
                    start = ContainerPadding,
                    end = ContainerPadding,
                    top = 4.dp,
                    bottom = ContainerPadding,
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                when {
                    state.isInitialLoading -> {
                        items(count = 5, key = { "shimmer-$it" }) {
                            CompletedTaskItemShimmer()
                        }
                    }
                    state.selectedChild == null -> {
                        item(key = "no-child") {
                            EmptyState(
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
                                onRetry = { event(CompletedTaskEvent.LoadTasks) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .fillParentMaxHeight(0.7f),
                            )
                        }
                    }
                    state.taskList.isEmpty() -> {
                        item(key = "empty") {
                            EmptyState(
                                title = stringResource(Res.string.hali_vazifa_yoq),
                                subtitle = stringResource(Res.string.bajarilgan_vazifalar),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .fillParentMaxHeight(0.7f),
                            )
                        }
                    }
                    else -> {
                        items(items = state.taskList, key = { it.id }) { task ->
                            CompletedTaskItem(task = task)
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
                                        color = AppColors.icon.accentPrimary,
                                        strokeWidth = 2.dp,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyState(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        androidx.compose.foundation.Image(
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
    TikonchaParentTheme(ThemeMode.DARK) {
        CompletedTaskUi(
            event = {},
            state = CompletedTaskState(),
            navigator = LocalNavigator.current
        )
    }
}