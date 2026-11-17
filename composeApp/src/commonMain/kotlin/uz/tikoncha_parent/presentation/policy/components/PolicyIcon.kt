package uz.tikoncha_parent.presentation.policy.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
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

/**
 * 3D rasm ölçami (Bosh sahifadagi `HomeIconSize` 76 ga yaqin): rasmda mayda detal köp — kiçik qatorda
 * (44dp) körinmay qolardi. Rasm fayllari 144px — bundan kattaroq qilinsa, xiralaşadi.
 */
val Policy3dIconSize: Dp = 68.dp

/**
 * Qoida blokining 3D rasmi (2026-11, Himoya ekranidagi `PermissionIcon` kabi): rasm matndan oldin öqiladi.
 * Tint ham, fon ham yöq — rangli rasm buziladi. Öçiq holat ([dimmed]) xiraroq körinadi.
 * Faqat joyi bor katta blokda ([PolicyCardBody]) işlatiladi — tor qatorga qaytarilmaydi.
 */
@Composable
fun Policy3dIcon(icon: DrawableResource, dimmed: Boolean, modifier: Modifier = Modifier, size: Dp = Policy3dIconSize) {
    Image(
        painter = painterResource(icon),
        contentDescription = null,
        modifier = modifier.size(size).alpha(if (dimmed) DimmedAlpha else 1f),
    )
}

private const val DimmedAlpha = 0.45f
