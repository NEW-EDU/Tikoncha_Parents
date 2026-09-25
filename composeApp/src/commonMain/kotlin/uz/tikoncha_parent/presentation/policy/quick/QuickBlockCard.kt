package uz.tikoncha_parent.presentation.policy.quick

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import tikoncha_parents.composeapp.generated.resources.Res
import tikoncha_parents.composeapp.generated.resources.quick_card_count
import tikoncha_parents.composeapp.generated.resources.quick_card_off
import tikoncha_parents.composeapp.generated.resources.quick_card_on
import tikoncha_parents.composeapp.generated.resources.quick_card_others
import tikoncha_parents.composeapp.generated.resources.quick_card_paused
import tikoncha_parents.composeapp.generated.resources.quick_no_apps
import tikoncha_parents.composeapp.generated.resources.quick_title
import uz.tikoncha_parent.domain.model.policy.QuickBlockSnapshot
import uz.tikoncha_parent.ui.TextFieldCornerRadius
import uz.tikoncha_parent.ui.theme.AppColors
import uz.tikoncha_parent.ui.theme.AppTypography
import kotlin.time.Instant

/**
 * Jadvallar ro'yxatidagi "Tezkor blok" kartasi: mening blokim (nechta ilova, yoqilgan /
 * o'chiq / to'xtatilgan) va bola hamda ikkinchi ota-ona nechta ilovani yopgani. Bosilsa — ekran.
 */
@Composable
fun QuickBlockCard(quick: QuickBlockSnapshot, now: Instant, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val mine = quick.mine
    val count = mine?.targets?.packages?.size ?: 0
    val enforced = mine != null && count > 0 && mine.isEnforced(now)
    val status = when {
        mine == null || count == 0 -> stringResource(Res.string.quick_no_apps)
        !mine.isActive -> "${stringResource(Res.string.quick_card_count, count)} · ${stringResource(Res.string.quick_card_off)}"
        mine.isPaused(now) -> "${stringResource(Res.string.quick_card_count, count)} · ${stringResource(Res.string.quick_card_paused)}"
        else -> "${stringResource(Res.string.quick_card_count, count)} · ${stringResource(Res.string.quick_card_on)}"
    }
    val byChild = quick.entries.filter { it.isChildOwner && it.isActive }.sumOf { it.targets.packages.size }
    val byOther = quick.entries.filter { !it.isChildOwner && !it.isMine(quick.myUserId) && it.isActive }
        .sumOf { it.targets.packages.size }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(TextFieldCornerRadius))
            .background(AppColors.bg.surface)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(if (enforced) AppColors.action.primary else AppColors.bg.tertiary),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Rounded.Lock,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = if (enforced) AppColors.icon.inverse else AppColors.icon.secondary,
            )
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(text = stringResource(Res.string.quick_title), style = AppTypography.titleMdSemiBold, color = AppColors.text.primary)
            Text(text = status, style = AppTypography.emphasizedXsRegular, color = AppColors.text.secondary)
            if (byChild > 0 || byOther > 0) {
                Text(
                    text = stringResource(Res.string.quick_card_others, byChild, byOther),
                    style = AppTypography.emphasizedXsRegular,
                    color = AppColors.text.tertiary,
                )
            }
        }
        Icon(Icons.Rounded.KeyboardArrowRight, contentDescription = null, tint = AppColors.icon.secondary)
    }
}
