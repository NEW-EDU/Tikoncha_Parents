package uz.tikoncha_parent.presentation.profile.subscription.autopay.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CreditCard
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime
import coil3.compose.AsyncImage
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import tikoncha_parents.composeapp.generated.resources.Res
import tikoncha_parents.composeapp.generated.resources.ap_change
import tikoncha_parents.composeapp.generated.resources.ap_for_whom
import tikoncha_parents.composeapp.generated.resources.ap_sum
import tikoncha_parents.composeapp.generated.resources.profile_hedgehog_img
import tikoncha_parents.composeapp.generated.resources.ic_humo
import tikoncha_parents.composeapp.generated.resources.ic_uzcard
import uz.tikoncha_parent.domain.model.autopay.CardVendor
import uz.tikoncha_parent.domain.model.autopay.PayCard
import uz.tikoncha_parent.ui.theme.AppColors
import uz.tikoncha_parent.ui.theme.AppTypography
import kotlin.time.Instant

/*
 * To'lov ekranlari uchun umumiy bo'laklar (Student bilan bir xil; aksent — Parent tillasi).
 * Logolar rasmiy (UI_DIZAYN_TOLOV/README.md §3): rangini va nisbatini o'zgartirmang;
 * UZCARD va Click belgisi oq plashka ustida turadi.
 */

private val White = Color.White

/** UZCARD (oq plashka ichida) yoki HUMO (karta shaklidagi rasmiy belgi). Balandlik — [height]. */
@Composable
fun BrandMark(vendor: CardVendor, height: Dp = 18.dp, modifier: Modifier = Modifier) {
    when (vendor) {
        CardVendor.HUMO -> Image(
            painter = painterResource(Res.drawable.ic_humo),
            contentDescription = "HUMO",
            contentScale = ContentScale.Fit,
            modifier = modifier
                .height(height)
                .width(height * 1.67f)
                .clip(RoundedCornerShape(3.dp)),
        )
        CardVendor.UZCARD -> Box(
            modifier = modifier
                .height(height)
                .width(height * 1.45f)
                .background(White, RoundedCornerShape(4.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                painter = painterResource(Res.drawable.ic_uzcard),
                contentDescription = "UZCARD",
                modifier = Modifier.height(height * 0.68f),
            )
        }
        CardVendor.OTHER -> Unit
    }
}

/** "UZCARD · HUMO" — qabul qilinadigan kartalar. */
@Composable
fun AcceptedCards(height: Dp = 18.dp, modifier: Modifier = Modifier) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(5.dp), verticalAlignment = Alignment.CenterVertically) {
        BrandMark(CardVendor.UZCARD, height)
        BrandMark(CardVendor.HUMO, height)
    }
}

/** Belgi + "•• 4821". */
@Composable
fun CardMask(card: PayCard, modifier: Modifier = Modifier, color: Color = AppColors.text.secondary) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        BrandMark(card.vendor)
        Text(text = "•• ${card.last4}", style = AppTypography.titleSmMedium, color = color)
    }
}

/** Oq fon ustidagi logo plitkasi (Click). */
@Composable
fun WhiteLogoTile(painter: androidx.compose.ui.graphics.painter.Painter, contentDescription: String, iconSize: Dp = 26.dp) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .background(White, RoundedCornerShape(12.dp))
            .border(1.dp, AppColors.border.secondary.copy(alpha = 0.18f), RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center,
    ) {
        Image(painter = painter, contentDescription = contentDescription, modifier = Modifier.size(iconSize))
    }
}

/** Aksent fondagi ikonka plitkasi (bank kartasi). */
@Composable
fun AccentIconTile(icon: ImageVector = Icons.Rounded.CreditCard) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .background(AppColors.action.primary.copy(alpha = 0.16f), RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center,
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = AppColors.action.primary, modifier = Modifier.size(22.dp))
    }
}

/** Radio: tanlangan — to'liq aksent doira + oq ✓; tanlanmagan — halqa. */
@Composable
fun RadioMark(selected: Boolean) {
    if (selected) {
        Box(
            modifier = Modifier.size(24.dp).background(AppColors.action.primary, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(imageVector = Icons.Rounded.Check, contentDescription = null, tint = AppColors.icon.inverse, modifier = Modifier.size(16.dp))
        }
    } else {
        Box(modifier = Modifier.size(24.dp).border(2.dp, AppColors.text.tertiary, CircleShape))
    }
}

/** Kichik yorliq ("Asosiy", "Avto-to'lov"). */
@Composable
fun Tag(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = AppTypography.bodySmMedium,
        color = AppColors.action.primary,
        modifier = modifier
            .background(AppColors.action.primary.copy(alpha = 0.14f), RoundedCornerShape(7.dp))
            .padding(horizontal = 7.dp, vertical = 3.dp),
    )
}

/**
 * Tanlangan kartaning belgisi — chegara burchagida (o'ng yuqori), chegara ustida.
 * Radio o'rniga: matnga to'liq joy qoladi (egasining qarori, 2026-10-08).
 */
@Composable
fun CornerCheck(modifier: Modifier = Modifier, ringColor: Color = AppColors.bg.page, fill: Color = AppColors.action.primary, tick: Color = AppColors.icon.inverse) {
    Box(
        modifier = modifier
            .size(24.dp)
            .background(ringColor, CircleShape)
            .padding(3.dp)
            .background(fill, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Icon(imageVector = Icons.Rounded.Check, contentDescription = null, tint = tick, modifier = Modifier.size(13.dp))
    }
}

/**
 * To'lov usuli qatori: 1-qator — nom va narx, 2-qator — izoh va tejash.
 * Tanlangan — 2 dp aksent chegara va burchakdagi belgi; tanlanmagan — ingichka chegara.
 */
@Composable
fun PayOptionRow(
    selected: Boolean,
    onClick: () -> Unit,
    leading: @Composable () -> Unit,
    title: String,
    price: String?,
    note: String? = null,
    subtitle: @Composable RowScope.() -> Unit,
) {
    val border by animateColorAsState(
        if (selected) AppColors.action.primary else AppColors.border.secondary.copy(alpha = 0.25f),
        tween(180), label = "border",
    )
    val shape = RoundedCornerShape(18.dp)
    Box(Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(76.dp)
                .clip(shape)
                .background(AppColors.bg.surface)
                .border(if (selected) 2.dp else 1.dp, border, shape)
                .selectable(selected = selected, onClick = onClick, role = Role.RadioButton)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Box(Modifier.width(40.dp)) { leading() }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = title, style = AppTypography.titleMdSemiBold, color = AppColors.text.primary, maxLines = 1, modifier = Modifier.weight(1f))
                    if (price != null) Text(text = price, style = AppTypography.titleMdSemiBold, color = AppColors.text.primary, maxLines = 1)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Row(Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) { subtitle() }
                    if (note != null) Text(text = note, style = AppTypography.bodyMdSemiBold, color = AppColors.action.primary, maxLines = 1)
                }
            }
        }
        if (selected) CornerCheck(Modifier.align(Alignment.TopEnd).offset(x = 8.dp, y = (-8).dp))
    }
}

/** Farzand avatari; rasm bo'lmasa — kirpi. */
@Composable
fun ChildAvatar(avatarUrl: String?, size: Dp = 40.dp) {
    AsyncImage(
        model = avatarUrl,
        contentDescription = null,
        error = painterResource(Res.drawable.profile_hedgehog_img),
        placeholder = painterResource(Res.drawable.profile_hedgehog_img),
        contentScale = ContentScale.Crop,
        modifier = Modifier.size(size).clip(CircleShape).background(AppColors.bg.tertiary),
    )
}

/** "Kim uchun" — qaysi farzandning PLUS'iga to'lanayotgani (Parent'ning asosiy farqi). */
@Composable
fun ForWhomRow(name: String, avatarUrl: String?, subtitle: String?, modifier: Modifier = Modifier, onChange: (() -> Unit)? = null) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(AppColors.bg.surface, RoundedCornerShape(18.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        ChildAvatar(avatarUrl)
        Column(Modifier.weight(1f)) {
            Text(stringResource(Res.string.ap_for_whom), style = AppTypography.bodySmMedium, color = AppColors.text.tertiary)
            Text(name, style = AppTypography.titleMdSemiBold, color = AppColors.text.primary, maxLines = 1)
            if (subtitle != null) Text(subtitle, style = AppTypography.bodySmMedium, color = AppColors.text.tertiary)
        }
        if (onChange != null) {
            Text(
                text = stringResource(Res.string.ap_change),
                style = AppTypography.titleSmSemiBold,
                color = AppColors.action.primary,
                modifier = Modifier.clip(RoundedCornerShape(8.dp)).selectable(selected = false, onClick = onChange).padding(4.dp),
            )
        }
    }
}

/** 120000 → "120 000 so'm". */
@Composable
fun sumText(amount: Int): String = stringResource(Res.string.ap_sum, formatSum(amount))

fun formatSum(amount: Int): String =
    amount.toString().reversed().chunked(3).joinToString(" ").reversed()

private val TASHKENT = TimeZone.of("Asia/Tashkent")

/** Sana Toshkent vaqtida: "08.10.2027". */
fun formatDate(instant: Instant): String {
    val d = instant.toLocalDateTime(TASHKENT).date
    return "${d.day.toString().padStart(2, '0')}.${d.month.number.toString().padStart(2, '0')}.${d.year}"
}
