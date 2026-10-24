package uz.tikoncha_parent.presentation.base.island

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import uz.tikoncha_parent.ui.theme.AppColors
import uz.tikoncha_parent.ui.theme.AppTypography

/**
 * Ekranning asosiy "+ qo'shish" amali — orol bilan QO'SHILMAYDI: orol ustida, chap pastda alohida suzuvchi tugma
 * (M3 Expressive extended FAB, soyasiz). Kontent ostidan o'tadi — ro'yxat pastiga [IslandFabSpace] qo'shing.
 */
@Composable
fun IslandFab(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    ExtendedFloatingActionButton(
        onClick = { if (enabled) onClick() },
        icon = { Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(24.dp)) },
        text = { Text(text, style = AppTypography.titleMdSemiBold) },
        containerColor = if (enabled) AppColors.action.primary else AppColors.action.primary.copy(alpha = 0.35f),
        contentColor = Color.White,
        shape = RoundedCornerShape(20.dp),
        elevation = FloatingActionButtonDefaults.elevation(0.dp, 0.dp, 0.dp, 0.dp),
        modifier = modifier.padding(start = 16.dp, bottom = 8.dp).height(56.dp),
    )
}

/** Ro'yxat pastida FAB uchun bo'sh joy (oxirgi element tugma ostida qolmasin). */
val IslandFabSpace = 76.dp
