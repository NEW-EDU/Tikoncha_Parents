package uz.tikoncha_parent.presentation.profile.coins

import androidx.compose.animation.SharedTransitionDefaults
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.Navigator
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.stringResource
import tikoncha_parents.composeapp.generated.resources.Res
import tikoncha_parents.composeapp.generated.resources.davom_etish
import tikoncha_parents.composeapp.generated.resources.farzandingizni_tanlang
import tikoncha_parents.composeapp.generated.resources.farzandlaringiz
import tikoncha_parents.composeapp.generated.resources.narx
import tikoncha_parents.composeapp.generated.resources.ozingiz_qoshing
import tikoncha_parents.composeapp.generated.resources.qanday_qilib_ishlab_topish
import tikoncha_parents.composeapp.generated.resources.qanday_qilib_ishlatish
import tikoncha_parents.composeapp.generated.resources.tanga_s
import tikoncha_parents.composeapp.generated.resources.tangachalar
import tikoncha_parents.composeapp.generated.resources.tavsiya_etilgan_paketlar
import tikoncha_parents.composeapp.generated.resources.tolov
import uz.tikoncha_parent.common.Util.toCurrency
import uz.tikoncha_parent.presentation.add_child.AddChildScreen
import uz.tikoncha_parent.presentation.base.ChildSelectionButton
import uz.tikoncha_parent.presentation.base.CustomButton
import uz.tikoncha_parent.presentation.base.CustomHeader
import uz.tikoncha_parent.presentation.base.ErrorRetryState
import uz.tikoncha_parent.presentation.base.PullToRefreshBox
import uz.tikoncha_parent.presentation.base.simpleShadow
import uz.tikoncha_parent.presentation.new_home.SelectionChildBottomSheet
import uz.tikoncha_parent.presentation.profile.coin_purchase.CoinPurchaseScreen
import uz.tikoncha_parent.ui.ContainerPadding
import uz.tikoncha_parent.ui.MainCornerRadius
import uz.tikoncha_parent.ui.SpaceMedium
import uz.tikoncha_parent.ui.theme.AppColors
import uz.tikoncha_parent.ui.theme.AppTypography
import uz.tikoncha_parent.ui.theme.ThemeMode
import uz.tikoncha_parent.ui.theme.TikonchaParentTheme
import uz.tikoncha_parent.ui.theme.rememberScreenSystemBars

class CoinsScreen : Screen {
    @Composable
    override fun Content() {

        val navigator = LocalNavigator.current ?: return

        val viewModel = koinScreenModel<CoinsViewModel>()
        val state = viewModel.state.collectAsStateWithLifecycle()
        val event = viewModel::onEvent

        LaunchedEffect(Unit) {
            event(CoinsEvent.LoadCoinList)
        }

        LaunchedEffect(Unit) {
            event(CoinsEvent.GetChildren)
        }

        CoinsUi(
            navigator = navigator,
            state = state.value,
            event = event
        )

    }
}

@Composable
fun CoinsUi(
    navigator: Navigator?,
    state: CoinsState,
    event: (CoinsEvent) -> Unit
) {
    val borderColor = AppColors.border.tertiary
    var helpType by remember { mutableStateOf<CoinsHelpType?>(null) }
    val currentLocale = SharedTransitionDefaults
    var showDialog by remember { mutableStateOf(false) }
    var retrying by remember { mutableStateOf(false) }

    LaunchedEffect(retrying) {
        if (retrying) {
            delay(900)
            retrying = false
        }
    }

    if (showDialog) {
        SelectionChildBottomSheet(
            navigator = navigator,
            items = state.childrenList,
            selectedItem = state.selectedChild,
            onDismiss = { showDialog = false },
            title = stringResource(Res.string.farzandlaringiz),
            onItemSelected = {
                event(CoinsEvent.OnChildSelected(it))
                showDialog = false
            }
        )
    }

    key(currentLocale) {
        if (helpType != null) {
            helpType?.let { type ->
                CoinsHelpBottomSheet(
                    type = type,
                    onDismiss = { helpType = null },
                )
            }
        }
    }

    val systemBars = rememberScreenSystemBars(
        statusBarColor = AppColors.bg.page,
        navigationBarColor = AppColors.bg.surface
    )

    PullToRefreshBox(
        isRefreshing = state.isRefreshing,
        onRefresh = { event(CoinsEvent.PullRefresh) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .then(systemBars.modifier)
                .imePadding()
                .background(AppColors.bg.page)
        ) {
            CustomHeader(
                title = stringResource(Res.string.tangachalar),
                showBackButton = true,
                onBackClick = {
                    navigator!!.pop()
                },
                trailingIcon = {
                    ChildSelectionButton(
                        modifier = Modifier
                            .widthIn(120.dp, 160.dp),
                        text = state.selectedChild?.name ?: "",
                        imageUrl = state.selectedChild?.avatarUrl ?: "",
                        label = stringResource(Res.string.farzandingizni_tanlang),
                        userInfo = state.selectedChild,
                        onClick = {
                            if (state.childrenList.isEmpty()) {
                                navigator?.push(AddChildScreen())
                            } else {
                                showDialog = true
                            }
                        },
                    )
                }
            )
            Spacer(Modifier.height(12.dp))

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
                    .padding(
                        top = ContainerPadding,
                        start = ContainerPadding,
                        end = ContainerPadding
                    )
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(72.dp)
                        .background(AppColors.bg.surface, RoundedCornerShape(24.dp))
                        .padding(horizontal = 20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(Res.string.ozingiz_qoshing),
                        color = AppColors.text.primary,
                        style = AppTypography.titleMdMedium,
                        modifier = Modifier
                            .weight(1f)
                    )

                    CoinNumberPicker(
                        value = state.coinsToBuy,
                        onValueChanged = {
                            event(CoinsEvent.OnCoinsChanged(it))
                        }
                    )
                }
                Spacer(Modifier.height(24.dp))

                Text(
                    text = stringResource(Res.string.tavsiya_etilgan_paketlar),
                    color = AppColors.text.secondary,
                    style = AppTypography.titleMdSemiBold
                )
                Spacer(Modifier.height(16.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val failure = state.error
                    if (failure != null && state.coinPackageList.isEmpty()) {
                        ErrorRetryState(
                            failure = failure,
                            isRetrying = retrying || state.isLoading,
                            onRetry = {
                                retrying = true
                                event(CoinsEvent.LoadCoinList)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 24.dp)
                        )
                    } else {
                        state.coinPackageList.forEachIndexed { index, item ->
                            CoinPackageItem(
                                coinPackageUi = item,
                                hasBorder = state.selectedPackageIndex == index,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        event(CoinsEvent.OnPackageSelected(index))
                                        navigator?.push(
                                            CoinPurchaseScreen(
                                                coins = item.coins,
                                                totalPrice = item.price.toInt(),
                                                discountPrice = item.discountedPrice.toInt()
                                            )
                                        )
                                    }
                            )
                        }
                    }
                }
                Spacer(Modifier.height(24.dp))

                CoinQuestionItem(
                    title = stringResource(Res.string.qanday_qilib_ishlatish),
                    onClick = {
                        helpType = CoinsHelpType.USE
                    }
                )
                Spacer(Modifier.height(8.dp))

                CoinQuestionItem(
                    title = stringResource(Res.string.qanday_qilib_ishlab_topish),
                    onClick = {
                        helpType = CoinsHelpType.EARN
                    }
                )
                SpaceMedium()
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .simpleShadow(RoundedCornerShape(16.dp))
                    .background(
                        AppColors.bg.surface,
                        shape = RoundedCornerShape(topEnd = 16.dp, topStart = 16.dp)
                    )
                    .drawWithContent {
                        drawContent()
                        val strokeWidth = 2.dp.toPx()
                        val halfStroke = strokeWidth / 2f
                        val cornerRadius = MainCornerRadius.toPx()
                        val segments = 100 // ko'p bo'lsa, silliqroq

                        // Chap burchak yoyi — 180°→270°, shaffofdan to'liq rangga
                        repeat(segments) { i ->
                            val progress =
                                i.toFloat() / segments       // 0.0 → 1.0 (astar shaffof)
                            val nextProgress = (i + 1f) / segments
                            val midProgress = (progress + nextProgress) / 2f

                            val alpha = midProgress                     // 0.0 → 1.0
                            val width =
                                strokeWidth * midProgress       // ingichkadan qalinlikka

                            val startAngle = 180f + progress * 90f
                            val sweepAngle = 90f / segments

                            drawArc(
                                color = borderColor.copy(alpha = alpha),
                                startAngle = startAngle,
                                sweepAngle = sweepAngle + 0.5f,         // overlap — bo'shliqsiz
                                useCenter = false,
                                topLeft = Offset(halfStroke, halfStroke),
                                size = Size(
                                    cornerRadius * 2 - strokeWidth,
                                    cornerRadius * 2 - strokeWidth
                                ),
                                style = Stroke(width = width)
                            )
                        }

                        // Yuqori to'g'ri chiziq — to'liq rang
                        drawLine(
                            color = borderColor,
                            start = Offset(cornerRadius, halfStroke),
                            end = Offset(size.width - cornerRadius, halfStroke),
                            strokeWidth = strokeWidth
                        )

                        // O'ng burchak yoyi — 270°→360°, to'liq rangdan shaffofga
                        repeat(segments) { i ->
                            val progress = i.toFloat() / segments       // 0.0 → 1.0
                            val nextProgress = (i + 1f) / segments
                            val midProgress = (progress + nextProgress) / 2f

                            val alpha = 1f - midProgress                // 1.0 → 0.0
                            val width =
                                strokeWidth * (1f - midProgress) // qalindan ingichkaga

                            val startAngle = 270f + progress * 90f
                            val sweepAngle = 90f / segments

                            drawArc(
                                color = borderColor.copy(alpha = alpha),
                                startAngle = startAngle,
                                sweepAngle = sweepAngle + 0.5f,
                                useCenter = false,
                                topLeft = Offset(
                                    size.width - cornerRadius * 2 + halfStroke,
                                    halfStroke
                                ),
                                size = Size(
                                    cornerRadius * 2 - strokeWidth,
                                    cornerRadius * 2 - strokeWidth
                                ),
                                style = Stroke(width = width)
                            )
                        }
                    }
            ) {
                Row(
                    modifier = Modifier
                        .padding(top = 12.dp, start = 24.dp, end = 24.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${stringResource(Res.string.tangachalar)}:",
                        color = AppColors.text.secondary,
                        style = AppTypography.bodyMdMedium
                    )
                    Text(
                        text = stringResource(Res.string.tanga_s, state.coinsToBuy.toCurrency()),
                        color = AppColors.text.secondary,
                        style = AppTypography.bodyMdMedium
                    )
                }
                Row(
                    modifier = Modifier
                        .padding(top = 5.dp, start = 24.dp, end = 24.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${stringResource(Res.string.narx)}:",
                        color = AppColors.text.secondary,
                        style = AppTypography.bodyMdMedium
                    )
                    Text(
                        text = "${state.coinPrice.toCurrency()} UZS",
                        color = AppColors.text.secondary,
                        style = AppTypography.bodyMdMedium
                    )
                }

                Row(
                    modifier = Modifier
                        .padding(top = 12.dp, start = 24.dp, end = 24.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${stringResource(Res.string.tolov)}:",
                        color = AppColors.text.primary,
                        style = AppTypography.titleLgSemiBold
                    )
                    Text(
                        text = "${state.totalPrice.toCurrency()} UZS",
                        color = AppColors.text.accentEmphasis,
                        style = AppTypography.titleLgSemiBold
                    )
                }

                CustomButton(
                    enabled = state.continueButtonEnabled,
                    text = stringResource(Res.string.davom_etish),
                    onClick = {
                        navigator?.push(
                            CoinPurchaseScreen(
                                coins = state.coinsToBuy,
                                totalPrice = state.totalPrice,
                                discountPrice = state.coinPrice
                            )
                        )
                    },
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth()
                        .height(52.dp),
                )
            }
        }
    }
}

@Preview
@Composable
private fun PreviewCoinsScreen() {
    TikonchaParentTheme(
        ThemeMode.DARK
    ) {
        CoinsUi(
            navigator = null,
            state = CoinsState(
                coinPackageList = listOf(
                    CoinPackageUi(
                        coins = 10,
                        priceInString = "10 000 UZS",
                        discountPercent = 10,
                        priceWithDiscountInString = "8 000 UZS",
                        price = 10000,
                        priceWithDiscount = 8000,
                        discountedPrice = 2000
                    )
                )
            ),
            event = {}
        )
    }
}