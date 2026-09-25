package uz.tikoncha_parent.presentation.base

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.ui.draw.alpha
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.toggleableState
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.ui.tooling.preview.Preview
import uz.tikoncha_parent.ui.theme.AppColors
import uz.tikoncha_parent.ui.theme.TikonchaParentTheme

@Composable
fun CustomSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,

    // O'lchamlar
    width: Dp = 52.dp,
    height: Dp = 28.dp,
    padding: Dp = 2.dp,

    // Ranglar
    trackOnColor: Color = AppColors.section.secondary,
    trackOffColor: Color = AppColors.section.secondary,
    trackBorderOnColor: Color = Color.Transparent,
    trackBorderOffColor: Color = AppColors.section.secondary,

    thumbOnColor: Color = AppColors.icon.accentPrimary,
    thumbOffColor: Color = AppColors.icon.secondary,

    /** Yoqilganda thumb ichida ✓ (jadval ekranlari — Student bilan bir xil). */
    showCheckIcon: Boolean = false,
    checkIconColor: Color = AppColors.icon.tertiary,
) {
    val trackShape = RoundedCornerShape(percent = 50)
    val thumbSize = height - padding * 2
    val maxOffset = width - thumbSize - padding * 2

    // Bitta umumiy spec — hamma animation'lar sinxron
    val colorSpec = tween<Color>(durationMillis = 220, easing = FastOutSlowInEasing)
    val thumbSpec = spring<Dp>(
        dampingRatio = Spring.DampingRatioLowBouncy,
        stiffness = Spring.StiffnessMedium
    )

    val trackColor by animateColorAsState(
        targetValue = if (checked) trackOnColor else trackOffColor,
        animationSpec = colorSpec,
        label = "trackColor"
    )
    val borderColor by animateColorAsState(
        targetValue = if (checked) trackBorderOnColor else trackBorderOffColor,
        animationSpec = colorSpec,
        label = "borderColor"
    )
    val thumbColor by animateColorAsState(
        targetValue = if (checked) thumbOnColor else thumbOffColor,
        animationSpec = colorSpec,
        label = "thumbColor"
    )
    val animatedOffset by animateDpAsState(
        targetValue = if (checked) maxOffset else 0.dp,
        animationSpec = thumbSpec,
        label = "thumbOffset"
    )

    // Ripple'ni o'chirish uchun
    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = modifier
            .size(width, height)
            .clip(trackShape)
            .background(trackColor, trackShape)
            .border(1.dp, borderColor, trackShape)  // Har doim chizilgan
            .semantics(mergeDescendants = true) {
                toggleableState = if (checked) ToggleableState.On else ToggleableState.Off
            }
            .clickable(
                enabled = enabled,
                role = Role.Switch,
                interactionSource = interactionSource,
                indication = null,  // iOS-uslub: ripple yo'q
                onClick = { onCheckedChange(!checked) }
            ),
        contentAlignment = Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .padding(horizontal = padding)
                .offset { IntOffset(animatedOffset.roundToPx(), 0) }
                .size(thumbSize)
                .clip(CircleShape)
                .background(thumbColor),
            contentAlignment = Alignment.Center,
        ) {
            if (showCheckIcon) {
                val iconAlpha by animateFloatAsState(targetValue = if (checked) 1f else 0f, label = "checkIcon")
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = checkIconColor,
                    modifier = Modifier
                        .size(thumbSize * 0.7f)
                        .alpha(iconAlpha),
                )
            }
        }
    }
}

@Preview
@Composable
fun CustomSwitchPreview() {
    TikonchaParentTheme {
        var checked by remember { mutableStateOf(false) }
        CustomSwitch(
            checked = checked,
            onCheckedChange = { checked = it }
        )
    }
}