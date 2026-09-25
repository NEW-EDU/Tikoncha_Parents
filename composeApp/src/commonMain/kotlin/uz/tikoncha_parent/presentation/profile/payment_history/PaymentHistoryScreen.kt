package uz.tikoncha_parent.presentation.profile.payment_history

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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import uz.tikoncha_parent.presentation.base.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import kotlinx.datetime.LocalDateTime
import org.jetbrains.compose.resources.stringResource
import tikoncha_parents.composeapp.generated.resources.Res
import tikoncha_parents.composeapp.generated.resources.boshqa_yozuvlar_yoq
import tikoncha_parents.composeapp.generated.resources.qayta_urinish
import tikoncha_parents.composeapp.generated.resources.tolovlar_tarixi
import tikoncha_parents.composeapp.generated.resources.tolovlar_tarixi_bos
import tikoncha_parents.composeapp.generated.resources.tolovlar_tarixi_bos_hint
import tikoncha_parents.composeapp.generated.resources.xatolik_yuz_berdi
import uz.tikoncha_parent.domain.model.transaction.PlanDuration
import uz.tikoncha_parent.domain.model.transaction.PurchaseType
import uz.tikoncha_parent.domain.model.transaction.Transaction
import uz.tikoncha_parent.domain.model.transaction.TransactionStatus
import uz.tikoncha_parent.presentation.base.CustomHeader
import uz.tikoncha_parent.presentation.base.asText
import uz.tikoncha_parent.presentation.base.singleClick
import uz.tikoncha_parent.presentation.profile.payment_history.components.TransactionItem
import uz.tikoncha_parent.ui.ContainerPadding
import uz.tikoncha_parent.ui.theme.AppColors
import uz.tikoncha_parent.ui.theme.AppTypography
import uz.tikoncha_parent.ui.theme.ThemeMode
import uz.tikoncha_parent.ui.theme.TikonchaParentTheme
import uz.tikoncha_parent.ui.theme.rememberScreenSystemBars

class PaymentHistoryScreen : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.current
        val screenModel = koinScreenModel<PaymentHistoryScreenModel>()
        val state by screenModel.state.collectAsStateWithLifecycle()

        PaymentHistoryUi(
            state = state,
            event = screenModel::onEvent,
            onBackClick = { navigator?.pop() },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PaymentHistoryUi(
    state: PaymentHistoryState,
    event: (PaymentHistoryEvent) -> Unit,
    onBackClick: () -> Unit,
) {
    val systemBars = rememberScreenSystemBars(
        statusBarColor = AppColors.bg.page,
        navigationBarColor = AppColors.bg.page,
    )

    val listState = rememberLazyListState()

    // ===== Infinite scroll trigger =====
    // derivedStateOf — har scroll piksel'da emas, faqat oxirgi visible item
    // chegaradan o'tganda recompose bo'ladi. Performance uchun muhim.
    val shouldLoadMore by remember {
        derivedStateOf {
            val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index
                ?: return@derivedStateOf false
            val total = listState.layoutInfo.totalItemsCount
            total > 0 && lastVisible >= total - PaymentHistoryState.PREFETCH_THRESHOLD
        }
    }

    LaunchedEffect(shouldLoadMore, state.isLoadingNext, state.hasNext, state.paginationError) {
        if (shouldLoadMore &&
            !state.isLoadingNext &&
            state.hasNext &&
            state.paginationError == null
        ) {
            event(PaymentHistoryEvent.LoadNext)
        }
    }

    PullToRefreshBox(
        isRefreshing = state.isRefreshing,
        onRefresh = { event(PaymentHistoryEvent.Refresh) },
        modifier = Modifier.fillMaxSize(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .then(systemBars.modifier)
                .background(AppColors.bg.page),
        ) {
            CustomHeader(
                showBackButton = true,
                onBackClick = onBackClick,
                title = stringResource(Res.string.tolovlar_tarixi)
            )

            when {
                state.isInitialLoading -> FullScreenLoader()

                state.showInitialError -> FullScreenError(
                    message = state.initialError?.asText().orEmpty(),
                    onRetry = { event(PaymentHistoryEvent.RetryInitial) },
                )

                state.showEmpty -> EmptyState()

                else -> {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            start = ContainerPadding,
                            end = ContainerPadding,
                            top = 12.dp,
                            bottom = 24.dp,
                        ),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        items(
                            items = state.items,
                            key = { it.id },
                        ) { transaction ->
                            TransactionItem(transaction = transaction)
                        }

                        if (state.isLoadingNext) {
                            item(key = "loading_footer") { PaginationLoader() }
                        }

                        if (state.paginationError != null) {
                            item(key = "pagination_error") {
                                PaginationErrorFooter(
                                    message = state.paginationError.asText(),
                                    onRetry = { event(PaymentHistoryEvent.RetryPagination) },
                                )
                            }
                        }

                        if (state.showEndOfList) {
                            item(key = "end_of_list") { EndOfListFooter() }
                        }
                    }
                }
            }
        }
    }

}

/* =========================================================================
                       SUPPORTING COMPOSABLES
   ========================================================================= */

@Composable
private fun FullScreenLoader() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(
            color = AppColors.bg.primary,
            trackColor = AppColors.bg.primary.copy(alpha = 0.2f),
        )
    }
}

@Composable
private fun FullScreenError(
    message: String,
    onRetry: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(Res.string.xatolik_yuz_berdi),
            style = AppTypography.titleMdSemiBold,
            color = AppColors.text.primary,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = message,
            style = AppTypography.bodyMdRegular,
            color = AppColors.text.secondary,
        )
        Spacer(Modifier.height(20.dp))
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
private fun EmptyState() {
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
                .background(AppColors.bg.primary.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "₸",
                style = AppTypography.display2xlSemiBold,
                color = AppColors.text.accentEmphasis,
            )
        }
        Spacer(Modifier.height(16.dp))
        Text(
            text = stringResource(Res.string.tolovlar_tarixi_bos),
            style = AppTypography.titleMdSemiBold,
            color = AppColors.text.primary,
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = stringResource(Res.string.tolovlar_tarixi_bos_hint),
            style = AppTypography.bodyMdRegular,
            color = AppColors.text.tertiary,
        )
    }
}

@Composable
private fun PaginationLoader() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(28.dp),
            color = AppColors.bg.primary,
            trackColor = AppColors.bg.primary.copy(alpha = 0.2f),
            strokeWidth = 3.dp,
        )
    }
}

@Composable
private fun PaginationErrorFooter(
    message: String,
    onRetry: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(AppColors.text.accentDanger.copy(alpha = 0.10f))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = message,
            style = AppTypography.bodyMdRegular,
            color = AppColors.text.accentDanger,
        )
        Spacer(Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .background(AppColors.text.accentDanger)
                .singleClick(onClick = onRetry)
                .padding(horizontal = 16.dp, vertical = 8.dp),
        ) {
            Text(
                text = stringResource(Res.string.qayta_urinish),
                style = AppTypography.bodyMdMedium,
                color = AppColors.text.onPrimary,
            )
        }
    }
}

@Composable
private fun EndOfListFooter() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = stringResource(Res.string.boshqa_yozuvlar_yoq),
            style = AppTypography.bodySmRegular,
            color = AppColors.text.tertiary,
        )
    }
}


private fun previewTx(
    id: String,
    purchaseType: PurchaseType = PurchaseType.SUBSCRIPTION,
    planDuration: PlanDuration? = PlanDuration.ANNUAL,
    status: TransactionStatus = TransactionStatus.COMPLETED,
    amount: Long = 199_000L,
    coins: Long = 600L,
    firstName: String = "Akmal",
    lastName: String = "Karimov",
    phone: String = "+998 99 123 45 67",
    isRegistered: Boolean = true,
    expiredAt: LocalDateTime? = null,
): Transaction = Transaction(
    id = id,
    userId = "user-1",
    childUserId = null,
    userFirstName = firstName,
    userLastName = lastName,
    userPhone = phone,
    isUserRegistered = isRegistered,
    merchantTransId = "TX-$id",
    amount = amount,
    originalAmount = null,
    coins = coins,
    status = status,
    purchaseType = purchaseType,
    planName = if (purchaseType == PurchaseType.SUBSCRIPTION) "PLUS" else null,
    planDuration = if (purchaseType == PurchaseType.SUBSCRIPTION) planDuration else null,
    expiredAt = expiredAt,
    createdAt = LocalDateTime(2026, 5, 14, 10, 8),
)

private fun previewSampleItems(): List<Transaction> = listOf(
    previewTx(
        id = "1",
        status = TransactionStatus.COMPLETED,
        expiredAt = LocalDateTime(2027, 5, 14, 10, 8),
    ),
    previewTx(
        id = "2",
        purchaseType = PurchaseType.COINS,
        planDuration = null,
        status = TransactionStatus.COMPLETED,
        amount = 12_000L,
        coins = 600L,
    ),
    previewTx(
        id = "3",
        status = TransactionStatus.PENDING,
        planDuration = PlanDuration.MONTHLY,
        amount = 29_000L,
        firstName = "",
        lastName = "",
        isRegistered = false,
    ),
    previewTx(
        id = "4",
        status = TransactionStatus.FAILED,
        firstName = "Diyor",
        lastName = "Mansurov",
        phone = "+998 90 222 33 44",
    ),
    previewTx(
        id = "5",
        purchaseType = PurchaseType.COINS,
        planDuration = null,
        status = TransactionStatus.CANCELLED,
        amount = 6_000L,
        coins = 300L,
    ),
)

/* =========================================================================
                          STATE PREVIEWS
   ========================================================================= */

@Preview
@Composable
private fun Preview_InitialLoading() {
    TikonchaParentTheme(ThemeMode.LIGHT) {
        PaymentHistoryUi(
            state = PaymentHistoryState(isInitialLoading = true),
            event = {},
            onBackClick = {},
        )
    }
}

@Preview
@Composable
private fun Preview_InitialError() {
    TikonchaParentTheme(ThemeMode.LIGHT) {
        PaymentHistoryUi(
            state = PaymentHistoryState(
                initialError = null,
                hasLoadedOnce = true,
            ),
            event = {},
            onBackClick = {},
        )
    }
}

@Preview
@Composable
private fun Preview_EmptyState() {
    TikonchaParentTheme(ThemeMode.LIGHT) {
        PaymentHistoryUi(
            state = PaymentHistoryState(
                items = emptyList(),
                hasLoadedOnce = true,
                hasNext = false,
            ),
            event = {},
            onBackClick = {},
        )
    }
}

@Preview
@Composable
private fun Preview_ListWithItems_Light() {
    TikonchaParentTheme(ThemeMode.LIGHT) {
        PaymentHistoryUi(
            state = PaymentHistoryState(
                items = previewSampleItems(),
                hasLoadedOnce = true,
                hasNext = true,
            ),
            event = {},
            onBackClick = {},
        )
    }
}

@Preview
@Composable
private fun Preview_ListWithItems_Dark() {
    TikonchaParentTheme(ThemeMode.DARK) {
        PaymentHistoryUi(
            state = PaymentHistoryState(
                items = previewSampleItems(),
                hasLoadedOnce = true,
                hasNext = true,
            ),
            event = {},
            onBackClick = {},
        )
    }
}

@Preview
@Composable
private fun Preview_PaginationLoading() {
    TikonchaParentTheme(ThemeMode.LIGHT) {
        PaymentHistoryUi(
            state = PaymentHistoryState(
                items = previewSampleItems(),
                hasLoadedOnce = true,
                hasNext = true,
                isLoadingNext = true,
            ),
            event = {},
            onBackClick = {},
        )
    }
}

@Preview
@Composable
private fun Preview_PaginationError() {
    TikonchaParentTheme(ThemeMode.LIGHT) {
        PaymentHistoryUi(
            state = PaymentHistoryState(
                items = previewSampleItems(),
                hasLoadedOnce = true,
                hasNext = true,
                paginationError = null,
            ),
            event = {},
            onBackClick = {},
        )
    }
}

@Preview
@Composable
private fun Preview_EndOfList() {
    TikonchaParentTheme(ThemeMode.LIGHT) {
        PaymentHistoryUi(
            state = PaymentHistoryState(
                items = previewSampleItems(),
                hasLoadedOnce = true,
                hasNext = false,
            ),
            event = {},
            onBackClick = {},
        )
    }
}