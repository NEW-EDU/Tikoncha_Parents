package uz.tikoncha_parent.presentation.policy.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import uz.tikoncha_parent.presentation.base.ChildAppIcon
import uz.tikoncha_parent.presentation.base.haptics.rememberAppHaptics
import uz.tikoncha_parent.presentation.base.singleClick
import uz.tikoncha_parent.ui.theme.AppColors

/*
 * Jadval ichidagi ekranlarning bloklari — Student `PresetPolicyComponents` bilan bir xil.
 */

private val GroupShape = RoundedCornerShape(24.dp)

/** Bo'lim sarlavhasi: "Qachon", "Limit", … o'ngida ixtiyoriy izoh. */
@Composable
fun SectionLabel(text: String, hint: String? = null, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth().padding(horizontal = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = text, style = PolicyText.section, color = AppColors.text.primary)
        if (hint != null) Text(text = hint, style = PolicyText.hint, color = AppColors.text.tertiary)
    }
}

/** Qatorlar guruhi — yumaloq karta, orasida ajratgich. */
@Composable
fun SettingGroup(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Column(modifier = modifier.fillMaxWidth().clip(GroupShape).background(AppColors.bg.section)) { content() }
}

/** Sahifa foni rangida — guruhlangan ro'yxatlardagidek yumshoq. */
@Composable
fun GroupDivider() {
    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 1.dp, color = AppColors.bg.page)
}

/** Standart qator: sarlavha · qiymat/izoh · o'ngda switch, chevron yoki boshqa. `onClick == null` — bosilmaydi. */
@Composable
fun SettingRow(
    title: String,
    modifier: Modifier = Modifier,
    value: String? = null,
    subtitle: String? = null,
    leading: (@Composable () -> Unit)? = null,
    onClick: (() -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
    titleStyle: TextStyle = PolicyText.row,
    valueStyle: TextStyle = PolicyText.value,
    titleColor: Color = AppColors.text.primary,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.singleClick(onClick = onClick) else Modifier)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        leading?.invoke()
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(text = title, style = titleStyle, color = titleColor, maxLines = 1, overflow = TextOverflow.Ellipsis)
            if (!subtitle.isNullOrBlank()) {
                Text(text = subtitle, style = PolicyText.subtitle, color = AppColors.text.tertiary, maxLines = 2)
            }
        }
        if (value != null) Text(text = value, style = valueStyle, color = AppColors.text.tertiary, maxLines = 1)
        trailing?.invoke()
    }
}

@Composable
fun Chevron() {
    Icon(
        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
        contentDescription = null,
        modifier = Modifier.size(24.dp),
        tint = AppColors.icon.secondary,
    )
}

/** Ikki katta vaqt katagi: Boshlanadi · Tugaydi. */
@Composable
fun TimeHero(startMin: Int, endMin: Int, startLabel: String, endLabel: String, onStart: () -> Unit, onEnd: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        HeroBox(label = startLabel, value = startMin.asClock(), onClick = onStart, modifier = Modifier.weight(1f))
        HeroBox(label = endLabel, value = endMin.asClock(), onClick = onEnd, modifier = Modifier.weight(1f))
    }
}

@Composable
fun HeroBox(label: String, value: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(AppColors.bg.primaryContainer)
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(text = label, style = PolicyText.heroLabel, color = AppColors.text.tertiary)
        Text(text = value, style = PolicyText.hero, color = AppColors.text.accentEmphasis, maxLines = 1)
    }
}

/** Tez tanlov chiplari: tanlangani to'ldirilgan; tor ekranda shrift 13sp dan 10sp gacha kichrayadi. */
@Composable
fun QuickChips(labels: List<String>, selectedIndex: Int, onSelect: (Int) -> Unit) {
    val haptics = rememberAppHaptics()
    Row(
        modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        labels.forEachIndexed { i, label ->
            val active = i == selectedIndex
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(40.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(if (active) AppColors.action.primary else AppColors.bg.tertiary)
                    .clickable {
                        if (i != selectedIndex) haptics.tick()
                        onSelect(i)
                    }
                    .padding(horizontal = 6.dp),
                contentAlignment = Alignment.Center,
            ) {
                BasicText(
                    text = label,
                    style = PolicyText.chip.copy(color = if (active) AppColors.text.inverse else AppColors.text.secondary),
                    maxLines = 1,
                    autoSize = TextAutoSize.StepBased(minFontSize = 10.sp, maxFontSize = PolicyText.chip.fontSize, stepSize = 0.5.sp),
                )
            }
        }
    }
}

/** Limit katagi: "Kuniga" / "Har soatda" + katta qiymat. */
@Composable
fun LimitTile(label: String, value: String, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 12.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(AppColors.bg.primaryContainer)
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(text = label, style = PolicyText.heroLabel, color = AppColors.text.tertiary)
        Text(
            text = value,
            style = if (value.length > 8) PolicyText.heroCompact else PolicyText.hero,
            color = AppColors.text.accentEmphasis,
            maxLines = 1,
        )
    }
}

/** "Bugun · 1 soat 12 daqiqa" + progress. */
@Composable
fun UsageBar(headline: String, trailing: String, usedMinutes: Int, limitMinutes: Int) {
    val done = usedMinutes >= limitMinutes
    val fraction = if (limitMinutes <= 0) 1f else (usedMinutes.toFloat() / limitMinutes).coerceIn(0f, 1f)
    val color = if (done) AppColors.icon.accentWarning else AppColors.action.primary
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Text(text = headline, style = PolicyText.value, color = AppColors.text.primary)
            Text(text = trailing, style = PolicyText.hint, color = AppColors.text.tertiary)
        }
        Box(modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)).background(AppColors.bg.tertiary)) {
            Box(modifier = Modifier.fillMaxWidth(fraction).height(6.dp).background(color, RoundedCornerShape(3.dp)))
        }
    }
}

/** Ro'yxatdagi ilova: ikonka · nom · − tugma (so'rov paytida aylanuvchi). */
@Composable
fun ExceptionRow(label: String, iconUrl: String?, onRemove: () -> Unit, busy: Boolean = false) {
    SettingRow(
        title = label,
        leading = { ChildAppIcon(iconUrl = iconUrl) },
        trailing = {
            Box(modifier = Modifier.size(32.dp), contentAlignment = Alignment.Center) {
                if (busy) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), color = AppColors.action.primary, strokeWidth = 2.dp)
                } else {
                    IconButton(onClick = onRemove, modifier = Modifier.size(32.dp)) {
                        Icon(imageVector = RemoveIcon, contentDescription = null, modifier = Modifier.size(18.dp), tint = AppColors.icon.accentDanger)
                    }
                }
            }
        },
    )
}

/** "+ Ilova qo'shish". [busy] — tanlanganlar serverga ketmoqda. */
@Composable
fun AddRow(text: String, onClick: () -> Unit, busy: Boolean = false) {
    SettingRow(
        title = text,
        onClick = if (busy) null else onClick,
        titleColor = AppColors.text.accentEmphasis,
        leading = {
            Box(
                modifier = Modifier.size(40.dp).background(AppColors.bg.primaryContainer, RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center,
            ) {
                if (busy) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = AppColors.action.primary, strokeWidth = 2.dp)
                } else {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(20.dp), tint = AppColors.icon.accentPrimary)
                }
            }
        },
    )
}

/** To'xtatilgan banner: sariq nuqta · matn · "Davom ettirish". */
@Composable
fun PausedBanner(text: String, actionText: String, onResume: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().background(AppColors.bg.accentWarningContainer).padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Box(modifier = Modifier.size(8.dp).background(AppColors.icon.accentWarning, RoundedCornerShape(4.dp)))
        Text(text = text, style = PolicyText.bannerStrong, color = AppColors.text.primary, modifier = Modifier.weight(1f), maxLines = 2)
        Text(text = actionText, style = PolicyText.action, color = AppColors.text.accentEmphasis, modifier = Modifier.clickable(onClick = onResume))
    }
}

/** 0..1440 daqiqa → "HH:mm" (1439 va 1440 — "23:59"). */
fun Int.asClock(): String {
    val m = if (this >= 1439) 1439 else this.coerceAtLeast(0)
    val h = m / 60
    val mm = m % 60
    return "${if (h < 10) "0" else ""}$h:${if (mm < 10) "0" else ""}$mm"
}

/** Material `Icons.Default.Remove` (extended kutubxonada — shu bitta yo'l ko'chirildi). */
val RemoveIcon: ImageVector by lazy {
    ImageVector.Builder(name = "Filled.Remove", defaultWidth = 24.dp, defaultHeight = 24.dp, viewportWidth = 24f, viewportHeight = 24f)
        .apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(19f, 13f)
                horizontalLineTo(5f)
                verticalLineToRelative(-2f)
                horizontalLineToRelative(14f)
                verticalLineToRelative(2f)
                close()
            }
        }.build()
}
