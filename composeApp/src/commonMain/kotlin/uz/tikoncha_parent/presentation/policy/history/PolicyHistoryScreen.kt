package uz.tikoncha_parent.presentation.policy.history

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.core.annotation.InternalVoyagerApi
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.internal.BackHandler
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import tikoncha_parents.composeapp.generated.resources.Res
import tikoncha_parents.composeapp.generated.resources.boshqa_yozuvlar_yoq
import tikoncha_parents.composeapp.generated.resources.bugun
import tikoncha_parents.composeapp.generated.resources.circle_clock
import tikoncha_parents.composeapp.generated.resources.kecha
import tikoncha_parents.composeapp.generated.resources.ozgarishlar_tarixi
import tikoncha_parents.composeapp.generated.resources.qayta_urinish
import tikoncha_parents.composeapp.generated.resources.tarix_bosh
import tikoncha_parents.composeapp.generated.resources.xatolik_yuz_berdi
import uz.tikoncha_parent.common.DateTimeUtil
import uz.tikoncha_parent.presentation.base.CustomHeader
import uz.tikoncha_parent.presentation.base.asText
import uz.tikoncha_parent.presentation.base.singleClick
import uz.tikoncha_parent.presentation.profile.language.LanguagePrefs
import uz.tikoncha_parent.ui.ContainerPadding
import uz.tikoncha_parent.ui.theme.AppColors
import uz.tikoncha_parent.ui.theme.AppTypography
import uz.tikoncha_parent.ui.theme.rememberScreenSystemBars
import kotlin.time.Clock

@OptIn(InternalVoyagerApi::class)
class PolicyHistoryScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.current ?: return
        val viewModel = koinScreenModel<PolicyHistoryViewModel>()
        val state by viewModel.state.collectAsStateWithLifecycle()

        BackHandler(true) { navigator.pop() }

        PolicyHistoryUi(
            state = state,
            event = viewModel::onEvent,
            onBackClick = { navigator.pop() },
        )
    }
}

@Composable
fun PolicyHistoryUi(
    state: PolicyHistoryState,
    event: (PolicyHistoryEvent) -> Unit = {},
    onBackClick: () -> Unit = {},
) {
    val systemBars = rememberScreenSystemBars(
        statusBarColor = AppColors.bg.secondary,
        navigationBarColor = AppColors.bg.secondary,
    )

    val listState = rememberLazyListState()

    // Faqat oxirgi ko'rinayotgan element chegaradan o'tganda qayta hisoblanadi.
    val shouldLoadMore by remember {
        derivedStateOf {
            val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index
                ?: return@derivedStateOf false
            val total = listState.layoutInfo.totalItemsCount
            total > 0 && lastVisible >= total - PolicyHistoryState.PREFETCH_THRESHOLD
        }
    }

    LaunchedEffect(shouldLoadMore, state.isLoadingMore, state.hasNext, state.paginationError) {
        if (shouldLoadMore && !state.isLoadingMore && state.hasNext && state.paginationError == null) {
            event(PolicyHistoryEvent.LoadMore)
        }
    }

    PullToRefreshBox(
        isRefreshing = state.isRefreshing,
        onRefresh = { event(PolicyHistoryEvent.Refresh) },
        modifier = Modifier.fillMaxSize(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .then(systemBars.modifier)
                .background(AppColors.bg.secondary),
        ) {
            CustomHeader(
                title = stringResource(Res.string.ozgarishlar_tarixi),
                showBackButton = true,
                onBackClick = onBackClick,
                modifier = Modifier.fillMaxWidth(),
            )

            when {
                state.isInitialLoading -> HistoryLoader(Modifier.fillMaxSize())

                state.showInitialError -> HistoryError(
                    message = state.initialError?.asText().orEmpty(),
                    onRetry = { event(PolicyHistoryEvent.RetryInitial) },
                    modifier = Modifier.fillMaxSize(),
                )

                state.showEmpty -> HistoryEmpty()

                else -> {
                    // Server yozuvlarni yangidan eskiga beradi — groupBy kunlar tartibini saqlaydi.
                    // Butun ro'yxat guruhlanadi, shuning uchun keyingi sahifa sarlavhani takrorlamaydi.
                    val days = remember(state.items) { state.items.groupBy { it.createdAt.date } }

                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        // Gorizontal padding kartalarda — sarlavha foni to'liq kenglikni yopishi uchun.
                        contentPadding = PaddingValues(bottom = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        days.forEach { (date, dayItems) ->
                            stickyHeader(key = "day_$date", contentType = "day_header") {
                                HistoryDayHeader(date = date)
                            }

                            items(
                                items = dayItems,
                                key = { it.id },
                                contentType = { "event" },
                            ) { item ->
                                PolicyHistoryItem(
                                    item = item,
                                    modifier = Modifier.padding(horizontal = ContainerPadding),
                                )
                            }
                        }

                        if (state.isLoadingMore) {
                            item(key = "loading_more") {
                                HistoryLoader(
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 16.dp)
                                )
                            }
                        }

                        state.paginationError?.let { failure ->
                            item(key = "pagination_error") {
                                HistoryError(
                                    message = failure.asText(),
                                    onRetry = { event(PolicyHistoryEvent.RetryPagination) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp),
                                )
                            }
                        }

                        if (state.showEndOfList) {
                            item(key = "end_of_list") {
                                Text(
                                    text = stringResource(Res.string.boshqa_yozuvlar_yoq),
                                    style = AppTypography.bodySmRegular,
                                    color = AppColors.text.tertiary,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 16.dp),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/** Kun sarlavhasi: "Bugun", "Kecha", "11 Sentyabr"; o'tgan yillar uchun yil bilan. */
@Composable
private fun HistoryDayHeader(date: LocalDate) {
    val lang = remember { LanguagePrefs.loadOrDefault() }
    val today = stringResource(Res.string.bugun)
    val yesterday = stringResource(Res.string.kecha)
    val currentYear = remember { Clock.System.todayIn(TimeZone.currentSystemDefault()).year }

    val dayMonth = DateTimeUtil.formatDayMonthLocalized(date, lang, today, yesterday)
    val label = if (dayMonth != today && dayMonth != yesterday && date.year != currentYear) {
        "$dayMonth ${date.year}"
    } else {
        dayMonth
    }

    // Fon shaffof bo'lmasligi shart — ostidan o'tayotgan kartalar ko'rinmasin.
    Text(
        text = label,
        style = AppTypography.bodyLgMedium,
        color = AppColors.text.secondary,
        modifier = Modifier
            .fillMaxWidth()
            .background(AppColors.bg.secondary)
            .padding(start = ContainerPadding, end = ContainerPadding, top = 12.dp, bottom = 4.dp),
    )
}

@Composable
private fun HistoryLoader(modifier: Modifier) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(32.dp),
            color = AppColors.bg.primary,
            trackColor = AppColors.bg.primary.copy(alpha = 0.2f),
            strokeWidth = 3.dp,
        )
    }
}

@Composable
private fun HistoryError(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier,
) {
    Column(
        modifier = modifier.padding(horizontal = 32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(Res.string.xatolik_yuz_berdi),
            style = AppTypography.titleMdSemiBold,
            color = AppColors.text.primary,
        )

        if (message.isNotBlank()) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = message,
                style = AppTypography.bodyMdRegular,
                color = AppColors.text.secondary,
                textAlign = TextAlign.Center,
            )
        }

        Spacer(Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(AppColors.bg.primary)
                .singleClick(onClick = onRetry)
                .padding(horizontal = 20.dp, vertical = 12.dp),
        ) {
            Text(
                text = stringResource(Res.string.qayta_urinish),
                style = AppTypography.titleSmMedium,
                color = AppColors.text.onPrimary,
            )
        }
    }
}

@Composable
private fun HistoryEmpty() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(AppColors.bg.primaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(Res.drawable.circle_clock),
                contentDescription = null,
                tint = AppColors.icon.accentPrimary,
                modifier = Modifier.size(36.dp),
            )
        }

        Spacer(Modifier.height(16.dp))

        Text(
            text = stringResource(Res.string.tarix_bosh),
            style = AppTypography.titleMdSemiBold,
            color = AppColors.text.primary,
            textAlign = TextAlign.Center,
        )
    }
}