package uz.tikoncha_parent.presentation.profile.subscription.autopay.cards

import cafe.adriel.voyager.koin.koinScreenModel
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CreditCard
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.StarOutline
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.unit.dp
import tikoncha_parents.composeapp.generated.resources.*
import uz.tikoncha_parent.domain.model.autopay.PayCard
import uz.tikoncha_parent.presentation.base.asText
import uz.tikoncha_parent.presentation.base.CustomButton
import uz.tikoncha_parent.presentation.base.CustomDialog
import uz.tikoncha_parent.presentation.base.CustomHeader
import uz.tikoncha_parent.presentation.base.EmptyState
import uz.tikoncha_parent.presentation.base.LoadingDialog
import uz.tikoncha_parent.presentation.profile.subscription.autopay.components.BrandMark
import uz.tikoncha_parent.presentation.profile.subscription.autopay.components.CardMask
import uz.tikoncha_parent.presentation.profile.subscription.autopay.components.Tag
import uz.tikoncha_parent.presentation.profile.subscription.autopay.confirm.Group
import uz.tikoncha_parent.presentation.profile.subscription.autopay.confirm.RowDivider
import uz.tikoncha_parent.ui.theme.rememberScreenSystemBars
import uz.tikoncha_parent.ui.theme.AppColors
import uz.tikoncha_parent.ui.theme.AppTypography

/**
 * Saqlangan kartalar (tasdiqlangan maket: UI_DIZAYN_TOLOV/maketlar/kartalarim_taklif.png).
 * Asosiy karta tepada "Asosiy" belgisi bilan; ⋮ → "Asosiy qilish" / "Kartani o'chirish".
 * Avto-to'lov yechadigan kartani o'chirib bo'lmaydi (amal xira, sababi yozilgan).
 */
@Composable
fun MyCardsUi(state: MyCardsState, event: (MyCardsEvent) -> Unit, onBack: () -> Unit, onAddCard: () -> Unit) {
    val systemBars = rememberScreenSystemBars(statusBarColor = AppColors.bg.page, navigationBarColor = AppColors.bg.page)
    Column(
        modifier = Modifier.fillMaxSize().then(systemBars.modifier).background(AppColors.bg.page),
    ) {
        CustomHeader(showBackButton = true, title = stringResource(Res.string.ap_my_cards), onBackClick = onBack)
        Box(Modifier.weight(1f).fillMaxWidth()) {
            when {
                state.loading -> CircularProgressIndicator(color = AppColors.action.primary, modifier = Modifier.align(Alignment.Center))
                state.cards.isEmpty() -> EmptyState(
                    icon = Icons.Rounded.CreditCard,
                    title = stringResource(Res.string.ap_cards_empty_title),
                    subtitle = stringResource(Res.string.ap_cards_empty_sub),
                    modifier = Modifier.align(Alignment.Center),
                )
                else -> Column(
                    Modifier.verticalScroll(rememberScrollState()).padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Group {
                        state.cards.forEachIndexed { i, card ->
                            if (i > 0) RowDivider()
                            CardRow(card, enabled = !state.busy) { event(MyCardsEvent.OpenActions(card)) }
                        }
                    }
                    Row(Modifier.padding(horizontal = 4.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Rounded.StarOutline, null, tint = AppColors.text.tertiary, modifier = Modifier.size(15.dp).padding(top = 1.dp))
                        Text(stringResource(Res.string.ap_primary_note_parent), style = AppTypography.bodyMdMedium, color = AppColors.text.tertiary)
                    }
                }
            }
        }
        CustomButton(
            text = stringResource(Res.string.ap_add_new_card),
            onClick = onAddCard,
            leadingIcon = { Icon(Icons.Rounded.Add, null, tint = AppColors.icon.inverse, modifier = Modifier.size(18.dp)) },
            modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 22.dp).height(54.dp),
        )
    }

    state.actionsFor?.let { CardActionsSheet(it, event) }

    LoadingDialog(state.busy)

    val removing = state.confirmRemove
    CustomDialog(
        show = removing != null,
        painter = painterResource(Res.drawable.dialog_warning),
        showCloseButton = true,
        title = stringResource(Res.string.ap_card_remove_title),
        message = removing?.let {
            stringResource(if (it.inUse) Res.string.ap_card_remove_in_use_body else Res.string.ap_card_remove_body, "•• ${it.last4}")
        }.orEmpty(),
        buttonText = stringResource(Res.string.ap_card_remove),
        onDismiss = { event(MyCardsEvent.DismissRemove) },
        onButtonClick = { removing?.let { event(MyCardsEvent.ConfirmRemove(it)) } },
    )
    CustomDialog(
        show = state.error != null,
        painter = painterResource(Res.drawable.dialog_failed),
        title = stringResource(Res.string.xatolik),
        message = state.error?.asText().orEmpty(),
        buttonText = stringResource(Res.string.yopish),
        onDismiss = { event(MyCardsEvent.DismissError) },
        onButtonClick = { event(MyCardsEvent.DismissError) },
    )
}

@Composable
private fun CardRow(card: PayCard, enabled: Boolean, onMore: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().padding(start = 16.dp, end = 4.dp, top = 8.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        BrandMark(card.vendor, height = 24.dp)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("•• ${card.last4}", style = AppTypography.titleSmSemiBold, color = AppColors.text.primary)
                if (card.isPrimary) Tag(stringResource(Res.string.ap_primary))
            }
            Text(
                text = if (card.inUse) "${card.expire} · ${stringResource(Res.string.ap_card_in_use_sub)}" else card.expire,
                style = AppTypography.bodyMdMedium,
                color = AppColors.text.tertiary,
            )
        }
        IconButton(onClick = onMore, enabled = enabled) {
            Icon(Icons.Rounded.MoreVert, stringResource(Res.string.ap_card_actions), tint = AppColors.text.secondary)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CardActionsSheet(card: PayCard, event: (MyCardsEvent) -> Unit) {
    ModalBottomSheet(
        onDismissRequest = { event(MyCardsEvent.DismissActions) },
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = AppColors.bg.elevated,
    ) {
        Column(Modifier.padding(start = 20.dp, end = 20.dp, bottom = 28.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                CardMask(card, color = AppColors.text.primary)
                Text(card.expire, style = AppTypography.bodyMdMedium, color = AppColors.text.tertiary)
            }
            Spacer(Modifier.height(6.dp))
            if (!card.isPrimary) {
                ActionRow(
                    icon = Icons.Rounded.StarOutline,
                    title = stringResource(Res.string.ap_make_primary),
                    subtitle = stringResource(Res.string.ap_make_primary_sub),
                    tint = AppColors.action.primary,
                    onClick = { event(MyCardsEvent.MakePrimary(card)) },
                )
                RowDivider()
            }
            ActionRow(
                icon = Icons.Rounded.DeleteOutline,
                title = stringResource(Res.string.ap_card_remove),
                subtitle = if (card.inUse) stringResource(Res.string.ap_card_remove_stops_autopay) else null,
                tint = AppColors.text.accentDanger,
                onClick = { event(MyCardsEvent.AskRemove(card)) },
            )
            Spacer(Modifier.height(14.dp))
            OutlinedButton(
                onClick = { event(MyCardsEvent.DismissActions) },
                border = BorderStroke(1.dp, AppColors.border.secondary.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth().height(48.dp),
            ) {
                Text(stringResource(Res.string.yopish), style = AppTypography.titleSmSemiBold, color = AppColors.text.primary)
            }
        }
    }
}

@Composable
private fun ActionRow(
    icon: ImageVector,
    title: String,
    subtitle: String?,
    tint: Color,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .alpha(if (enabled) 1f else 0.45f)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Box(Modifier.size(40.dp).background(tint.copy(alpha = 0.14f), RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
            Icon(icon, null, tint = tint, modifier = Modifier.size(20.dp))
        }
        Column(Modifier.weight(1f)) {
            Text(title, style = AppTypography.titleSmSemiBold, color = if (tint == AppColors.text.accentDanger) tint else AppColors.text.primary)
            if (subtitle != null) Text(subtitle, style = AppTypography.bodyMdMedium, color = AppColors.text.tertiary)
        }
    }
}


/** Voyager ekrani — Kartalarim (Profil va Obuna ekranidan). Qaytganda ro'yxat yangilanadi. */
class AutopayCardsScreen : cafe.adriel.voyager.core.screen.Screen {
    @Composable
    override fun Content() {
        val navigator = cafe.adriel.voyager.navigator.LocalNavigator.current
        val vm = koinScreenModel<MyCardsViewModel>()
        val state = vm.state.collectAsStateWithLifecycle().value
        androidx.compose.runtime.LaunchedEffect(Unit) { vm.onEvent(MyCardsEvent.Load) }
        MyCardsUi(
            state = state,
            event = vm::onEvent,
            onBack = { navigator?.pop() },
            onAddCard = { navigator?.push(uz.tikoncha_parent.presentation.profile.subscription.autopay.card.AutopayAddCardScreen()) },
        )
    }
}
