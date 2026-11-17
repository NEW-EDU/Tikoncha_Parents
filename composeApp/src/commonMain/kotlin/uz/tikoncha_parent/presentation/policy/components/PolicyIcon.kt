package uz.tikoncha_parent.presentation.policy.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import uz.tikoncha_parent.ui.theme.AppColors

/**
 * Karta ikonkasi (Student bilan bir xil, 2026-10-07): rang holatni aytadi — brend rangi = yoqiq,
 * kulrang = o'chiq. Kimniki ekanini (farzand, ikkinchi ota-ona, maktab) qulf belgisi va tab aytadi.
 */
enum class IconTone { SOLID, GRAY }

@Composable
fun PolicyIcon(icon: DrawableResource, tone: IconTone, modifier: Modifier = Modifier, size: Dp = 52.dp) {
    val (bg, tint) = when (tone) {
        IconTone.SOLID -> AppColors.bg.primary to AppColors.icon.inverse
        IconTone.GRAY -> AppColors.bg.tertiary to AppColors.icon.disabledTertiary
    }
    Box(
        modifier = modifier.size(size).background(bg, RoundedCornerShape(16.dp)),
        contentAlignment = Alignment.Center,
    ) {
        Icon(painter = painterResource(icon), contentDescription = null, modifier = Modifier.size(size / 2), tint = tint)
    }
}
