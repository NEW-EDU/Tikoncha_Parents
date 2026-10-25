package uz.tikoncha_parent.presentation.profile.coins

import androidx.compose.foundation.layout.Box
import uz.tikoncha_parent.ui.tileBlobColor
import uz.tikoncha_parent.ui.tileBlob
import uz.tikoncha_parent.ui.rememberHapticSource
import uz.tikoncha_parent.ui.homeTileColor
import uz.tikoncha_parent.ui.expressiveSegment
import uz.tikoncha_parent.ui.expressiveButtonShapes
import uz.tikoncha_parent.ui.SegmentGap
import uz.tikoncha_parent.ui.BlobPlace
import uz.tikoncha_parent.presentation.profile.ProfileSectionLabel
import uz.tikoncha_parent.presentation.profile.ProfileSectionItem
import uz.tikoncha_parent.presentation.base.island.BottomAnchoredColumn
import tikoncha_parents.composeapp.generated.resources.sizda_mavjud_tangachalar
import tikoncha_parents.composeapp.generated.resources.coin
import org.jetbrains.compose.resources.painterResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.draw.clip
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Button
import androidx.compose.material.icons.rounded.Savings
import androidx.compose.material.icons.automirrored.rounded.HelpOutline
import androidx.compose.material.icons.Icons
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.Image
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.fadeOut
import androidx.compose.animation.fadeIn
import androidx.compose.animation.expandVertically
import androidx.compose.animation.AnimatedVisibility
import uz.tikoncha_parent.presentation.base.island.ChildPickerSpec
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
        navigationBarColor = AppColors.bg.page,
    )

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
            onBackClick = { navigator?.pop() },
            childPicker = ChildPickerSpec(
                children = state.childrenList,
                selected = state.selectedChild,
                onSelect = { event(CoinsEvent.OnChildSelected(it)) },
                onAddChild = { navigator?.push(AddChildScreen()) },
            ),
        )

        PullToRefreshBox(
            isRefreshing = state.isRefreshing,
            onRefresh = { event(CoinsEvent.PullRefresh) },
            modifier = Modifier.fillMaxWidth().weight(1f),
        ) {
            // Bo'sh joy bo'lsa — pastga (bosh barmoqqa yaqin); paketlar va o'z miqdori — bitta sokin oqim
            BottomAnchoredColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = ContainerPadding, end = ContainerPadding, top = 8.dp, bottom = 8.dp),
            ) {
                CoinBalanceHero(coins = state.myCoins, known = state.coinsKnown)

                ProfileSectionLabel(stringResource(Res.string.tavsiya_etilgan_paketlar))
                val failure = state.error
                if (failure != null && state.coinPackageList.isEmpty()) {
                    ErrorRetryState(
                        failure = failure,
                        isRetrying = retrying || state.isLoading,
                        onRetry = {
                            retrying = true
                            event(CoinsEvent.LoadCoinList)
                        },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(SegmentGap)) {
                        state.coinPackageList.forEachIndexed { index, item ->
                            CoinPackageRow(
                                pack = item,
                                index = index,
                                count = state.coinPackageList.size,
                                onClick = {
                                    event(CoinsEvent.OnPackageSelected(index))
                                    navigator?.push(
                                        CoinPurchaseScreen(
                                            coins = item.coins,
                                            totalPrice = item.price.toInt(),
                                            discountPrice = item.discountedPrice.toInt(),
                                        )
                                    )
                                },
                            )
                        }
                    }
                }

                ProfileSectionLabel(stringResource(Res.string.ozingiz_qoshing))
                CustomAmountRow(
                    value = state.coinsToBuy,
                    pricePerCoin = state.coinPrice,
                    onValueChanged = { event(CoinsEvent.OnCoinsChanged(it)) },
                )

                Spacer(Modifier.height(20.dp))
                Column(verticalArrangement = Arrangement.spacedBy(SegmentGap)) {
                    ProfileSectionItem(
                        icon = rememberVectorPainter(Icons.AutoMirrored.Rounded.HelpOutline),
                        title = stringResource(Res.string.qanday_qilib_ishlatish),
                        onItemClick = { helpType = CoinsHelpType.USE },
                        segmentIndex = 0,
                        segmentCount = 2,
                    )
                    ProfileSectionItem(
                        icon = rememberVectorPainter(Icons.Rounded.Savings),
                        title = stringResource(Res.string.qanday_qilib_ishlab_topish),
                        onItemClick = { helpType = CoinsHelpType.EARN },
                        segmentIndex = 1,
                        segmentCount = 2,
                    )
                }
            }
        }

        // O'z miqdori tanlansa — pastda to'lov paneli (orol ustida, bosh barmoq yetadigan joyda)
        AnimatedVisibility(
            visible = state.continueButtonEnabled,
            enter = expandVertically(expandFrom = Alignment.Top) + fadeIn(),
            exit = shrinkVertically(shrinkTowards = Alignment.Top) + fadeOut(),
        ) {
            CheckoutBar(
                coins = state.coinsToBuy,
                total = state.totalPrice,
                onContinue = {
                    navigator?.push(
                        CoinPurchaseScreen(
                            coins = state.coinsToBuy,
                            totalPrice = state.totalPrice,
                            discountPrice = state.coinPrice,
                        )
                    )
                },
            )
        }
    }
}

/** Ota-onaning tangalari: katta raqam, o'ngda tanga (ortida sokin doira — Bosh sahifadagidek). */
@Composable
private fun CoinBalanceHero(coins: Int, known: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(homeTileColor())
            .tileBlob(tileBlobColor(), BlobPlace.RowChat)
            .padding(start = 20.dp, end = 16.dp, top = 20.dp, bottom = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                text = stringResource(Res.string.sizda_mavjud_tangachalar),
                style = AppTypography.titleSmMedium,
                color = AppColors.text.tertiary,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = if (known) stringResource(Res.string.tanga_s, coins.toCurrency()) else "—",
                style = AppTypography.displaySmRegular.copy(fontWeight = FontWeight.Bold),
                color = AppColors.text.primary,
            )
        }
        Image(painter = painterResource(Res.drawable.coin), contentDescription = null, modifier = Modifier.size(64.dp))
    }
}

/** Paket: tanga soni, eski narx (chizilgan) + chegirma, o'ngda yangi narx. Bosilsa — to'lov. */
@Composable
private fun CoinPackageRow(pack: CoinPackageUi, index: Int, count: Int, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 72.dp)
            .expressiveSegment(index, count, color = AppColors.bg.surface, onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier.size(44.dp).clip(CircleShape).background(AppColors.action.primary.copy(alpha = 0.14f)),
            contentAlignment = Alignment.Center,
        ) {
            Image(painter = painterResource(Res.drawable.coin), contentDescription = null, modifier = Modifier.size(26.dp))
        }
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = stringResource(Res.string.tanga_s, pack.coins.toCurrency()),
                style = AppTypography.titleMdSemiBold,
                color = AppColors.text.primary,
                maxLines = 1,
            )
            if (pack.discountPercent > 0) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = pack.priceInString,
                        style = AppTypography.bodySmMedium,
                        color = AppColors.text.tertiary,
                        textDecoration = TextDecoration.LineThrough,
                        maxLines = 1,
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "−${pack.discountPercent}%",
                        style = AppTypography.bodySmSemiBold,
                        color = AppColors.text.accentEmphasis,
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(AppColors.action.primary.copy(alpha = 0.14f))
                            .padding(horizontal = 7.dp, vertical = 1.dp),
                    )
                }
            }
        }
        Spacer(Modifier.width(10.dp))
        Text(
            text = pack.priceWithDiscountInString,
            style = AppTypography.titleMdSemiBold,
            color = AppColors.text.accentEmphasis,
            maxLines = 1,
        )
    }
}

/** O'z miqdori: tanga narxi izohi va ± tanlagich — bitta blok. */
@Composable
private fun CustomAmountRow(value: Int, pricePerCoin: Int, onValueChanged: (Int) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 72.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(AppColors.bg.surface)
            .padding(start = 20.dp, end = 12.dp, top = 12.dp, bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "${stringResource(Res.string.tanga_s, "1")} = ${pricePerCoin.toCurrency()} UZS",
            style = AppTypography.bodyMdMedium,
            color = AppColors.text.tertiary,
            modifier = Modifier.weight(1f),
        )
        CoinNumberPicker(value = value, onValueChanged = onValueChanged)
    }
}

/** To'lov paneli: chapda jami (kichik izoh + katta summa), o'ngda "Davom etish". */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun CheckoutBar(coins: Int, total: Int, onContinue: () -> Unit) {
    Row(
        modifier = Modifier
            .padding(start = ContainerPadding, end = ContainerPadding, top = 4.dp, bottom = 8.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(AppColors.bg.surface)
            .padding(start = 20.dp, end = 10.dp, top = 10.dp, bottom = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                text = stringResource(Res.string.tanga_s, coins.toCurrency()),
                style = AppTypography.bodySmMedium,
                color = AppColors.text.tertiary,
            )
            Text(
                text = "${total.toCurrency()} UZS",
                style = AppTypography.titleLgSemiBold,
                color = AppColors.text.accentEmphasis,
            )
        }
        Button(
            onClick = onContinue,
            interactionSource = rememberHapticSource(),
            shapes = expressiveButtonShapes(),
            colors = ButtonDefaults.buttonColors(
                containerColor = AppColors.button.primary,
                contentColor = AppColors.text.inverse,
            ),
            contentPadding = PaddingValues(horizontal = 22.dp),
            modifier = Modifier.height(52.dp),
        ) {
            Text(stringResource(Res.string.davom_etish), style = AppTypography.titleMdSemiBold)
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