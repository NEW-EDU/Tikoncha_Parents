package uz.tikoncha_parent.presentation.statistic

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import tikoncha_parents.composeapp.generated.resources.Res
import tikoncha_parents.composeapp.generated.resources.stat_blocked_by_child
import tikoncha_parents.composeapp.generated.resources.stat_blocked_by_child_inactive
import tikoncha_parents.composeapp.generated.resources.stat_blocked_by_child_paused
import tikoncha_parents.composeapp.generated.resources.stat_blocked_by_coparent
import tikoncha_parents.composeapp.generated.resources.stat_blocked_by_coparent_inactive
import tikoncha_parents.composeapp.generated.resources.stat_blocked_by_coparent_paused
import tikoncha_parents.composeapp.generated.resources.stat_hour_only
import tikoncha_parents.composeapp.generated.resources.stat_per_day
import tikoncha_parents.composeapp.generated.resources.stat_quick_blocked
import tikoncha_parents.composeapp.generated.resources.stat_quick_blocked_inactive
import tikoncha_parents.composeapp.generated.resources.stat_quick_blocked_paused
import tikoncha_parents.composeapp.generated.resources.stat_row_hour_min
import tikoncha_parents.composeapp.generated.resources.stat_row_min
import tikoncha_parents.composeapp.generated.resources.stat_row_sec
import uz.tikoncha_parent.domain.model.policy.QuickBadge
import uz.tikoncha_parent.domain.model.policy.QuickLock
import uz.tikoncha_parent.domain.model.policy.QuickOwner
import uz.tikoncha_parent.domain.model.policy.QuickStatus
import uz.tikoncha_parent.presentation.base.ChildAppIcon
import uz.tikoncha_parent.ui.theme.AppColors
import uz.tikoncha_parent.ui.theme.AppTypography

/**
 * Statistika ro'yxatidagi ilova qatori (kunlik va haftalik bitta) — Student'dagi `StatAppRow`.
 *
 *   [ikonka]  Nomi ...................... 2 soat 20 daq   [qulf]
 *             ███████████████░░░░░░  ← eng ko'p ilovaga nisbatan ulush
 *             🔒 Tezkor blokda / Farzand o'zi bloklagan / kuniga ~2 soat
 *
 * Qatorda hisob yo'q: ulush va blok holati tayyor keladi — ro'yxat aylantirilganda
 * faqat chiziladi. Chiziq animatsiyasiz.
 */
@Composable
fun StatAppRow(
    app: StatAppUi,
    weekly: Boolean,
    showDivider: Boolean,
    /** Qator ostidagi yozuv — [StatisticState.quickBadge]; null — yo'q. */
    badge: QuickBadge?,
    /** Qulf tugmasi; null — ko'rsatilmaydi ([StatisticState.quickLock]). */
    lock: QuickLock?,
    isBlockBusy: Boolean,
    onQuickBlockClick: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            ChildAppIcon(iconUrl = app.iconUrl, size = StatIconSize)

            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = app.name,
                        modifier = Modifier.weight(1f),
                        style = AppTypography.titleMdMedium,
                        color = AppColors.text.primary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = rowDuration(app.millis),
                        style = AppTypography.titleSmSemiBold,
                        color = AppColors.text.primary,
                        maxLines = 1,
                    )
                }

                ShareBar(share = app.share)

                // Yozuv silliq paydo bo'ladi / yo'qoladi — qator balandligi sakramaydi
                AnimatedContent(
                    targetState = badge,
                    transitionSpec = {
                        (fadeIn(tween(220, delayMillis = 60)) togetherWith fadeOut(tween(120)))
                            .using(SizeTransform(clip = false))
                    },
                    label = "quickBadge",
                ) { b ->
                    if (b != null) BlockBadge(stringResource(b.text()), b.color())
                }
                // Kuniga 1 daqiqadan kam bo'lsa "kuniga ~<1 daq" ma'nosiz — ko'rsatilmaydi
                if (weekly && app.perDayMillis >= 60_000L && app.perDayMillis != app.millis) {
                    Text(
                        text = stringResource(Res.string.stat_per_day, rowDuration(app.perDayMillis)),
                        style = AppTypography.emphasizedXsRegular,
                        color = AppColors.text.tertiary,
                        maxLines = 1,
                    )
                }
            }

            if (lock != null) {
                QuickBlockButton(
                    isBlocked = lock != QuickLock.OFF,
                    isBusy = isBlockBusy,
                    inactive = lock == QuickLock.INACTIVE,
                    onClick = onQuickBlockClick,
                )
            }
        }

        if (showDivider) {
            HorizontalDivider(
                modifier = Modifier.padding(start = StatIconSize + 12.dp),
                thickness = 1.dp,
                color = AppColors.border.secondary.copy(alpha = 0.12f),
            )
        }
    }
}

private val StatIconSize = 40.dp

/** Ulush chizig'i: 4dp, animatsiyasiz. Juda kichik ulush ham nuqta bo'lib ko'rinadi. */
@Composable
private fun ShareBar(share: Float) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(4.dp)
            .clip(RoundedCornerShape(2.dp))
            .background(AppColors.bg.tertiary),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(share.coerceIn(MIN_SHARE, 1f))
                .fillMaxHeight()
                .clip(RoundedCornerShape(2.dp))
                .background(AppColors.action.primary),
        )
    }
}

private const val MIN_SHARE = 0.02f

@Composable
private fun BlockBadge(text: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Icon(
            imageVector = Icons.Rounded.Lock,
            contentDescription = null,
            modifier = Modifier.size(14.dp),
            tint = color,
        )
        Text(text = text, style = AppTypography.emphasizedXsMedium, color = color, maxLines = 1)
    }
}

private fun QuickBadge.text(): StringResource = when (owner) {
    QuickOwner.ME -> when (status) {
        QuickStatus.ACTIVE -> Res.string.stat_quick_blocked
        QuickStatus.PAUSED -> Res.string.stat_quick_blocked_paused
        QuickStatus.UNPAID -> Res.string.stat_quick_blocked_inactive
    }
    QuickOwner.CHILD -> when (status) {
        QuickStatus.ACTIVE -> Res.string.stat_blocked_by_child
        QuickStatus.PAUSED -> Res.string.stat_blocked_by_child_paused
        QuickStatus.UNPAID -> Res.string.stat_blocked_by_child_inactive
    }
    QuickOwner.CO_PARENT -> when (status) {
        QuickStatus.ACTIVE -> Res.string.stat_blocked_by_coparent
        QuickStatus.PAUSED -> Res.string.stat_blocked_by_coparent_paused
        QuickStatus.UNPAID -> Res.string.stat_blocked_by_coparent_inactive
    }
}

/** Yashil — o'zim yopyapman; sariq — ro'yxatda, lekin obuna yo'q; kulrang — boshqalar yoki pauza. */
@Composable
private fun QuickBadge.color(): Color = when {
    status == QuickStatus.UNPAID -> AppColors.text.accentWarning
    owner == QuickOwner.ME && status == QuickStatus.ACTIVE -> AppColors.text.accentEmphasis
    else -> AppColors.text.secondary
}

/** "2 soat 20 daq" — qatorga sig'adigan qisqa ko'rinish; bir daqiqadan kam — aniq soniya. */
@Composable
internal fun rowDuration(ms: Long): String {
    val seconds = (ms / 1000L).toInt()
    if (seconds < 60) return stringResource(Res.string.stat_row_sec, seconds.coerceAtLeast(1))
    val total = seconds / 60
    val h = total / 60
    val m = total % 60
    return when {
        h > 0 && m > 0 -> stringResource(Res.string.stat_row_hour_min, h, m)
        h > 0 -> stringResource(Res.string.stat_hour_only, h)
        else -> stringResource(Res.string.stat_row_min, m)
    }
}
