package uz.tikoncha_parent.presentation.base

import uz.tikoncha_parent.ui.Haptics
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import tikoncha_parents.composeapp.generated.resources.Res
import tikoncha_parents.composeapp.generated.resources.father_icon
import tikoncha_parents.composeapp.generated.resources.mather_icon
import uz.tikoncha_parent.ui.theme.AppColors
import uz.tikoncha_parent.ui.theme.AppTypography
import uz.tikoncha_parent.ui.theme.ThemeMode
import uz.tikoncha_parent.ui.theme.TikonchaParentTheme

// ═════════════════════════════════════════════════════════════
// Data
// ═════════════════════════════════════════════════════════════

@Immutable
data class PillSegmentedItem(
    val label: String,
    val icon: Painter? = null,
)

@Immutable
data class PillSegmentedColors(
    val trackColor: Color,
    val indicatorColor: Color,
    val selectedTextColor: Color,
    val unselectedTextColor: Color,
    val selectedIconColor: Color,
    val unselectedIconColor: Color,
)

object PillSegmentedButtonDefaults {

    @Composable
    fun colors(
        trackColor: Color = AppColors.section.secondary,
        indicatorColor: Color = AppColors.modal.primary,
        selectedTextColor: Color = AppColors.text.primary,
        unselectedTextColor: Color = AppColors.text.primary,
        selectedIconColor: Color = AppColors.icon.primary,
        unselectedIconColor: Color = AppColors.icon.primary,
    ): PillSegmentedColors = PillSegmentedColors(
        trackColor = trackColor,
        indicatorColor = indicatorColor,
        selectedTextColor = selectedTextColor,
        unselectedTextColor = unselectedTextColor,
        selectedIconColor = selectedIconColor,
        unselectedIconColor = unselectedIconColor,
    )

    val Height: Dp = 36.dp
    val TrackPadding: Dp = 2.dp
    val ItemGap: Dp = 4.dp
    val IconSize: Dp = 16.dp
    val IconTextGap: Dp = 6.dp
    val BorderWidth: Dp = 1.dp
    const val AnimationDurationMs: Int = 250
}

// ═════════════════════════════════════════════════════════════
// Component
// ═════════════════════════════════════════════════════════════

/**
 * Pill-shaped segmented button with animated indicator.
 *
 * Indicator joylashuvi har bir item ning **actual measured** offset va
 * width'idan olinadi (pikselda). Bu Dp arifmetikasi natijasidagi rounding
 * drift'ni yo'q qiladi — indicator har doim aynan tanlangan item ustida turadi,
 * chetlarda bo'sh joy qolmaydi.
 *
 * Borderless yoki bordered:
 *  - Borderless: `trackBorder = null`, `indicatorBorder = null`
 *  - Bordered: BorderStroke uzating
 *
 * Item label uzun bo'lsa (kichik ekran yoki til o'zgargan holatda)
 * avtomatik ravishda ellipsis (`…`) qo'yiladi.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun PillSegmentedButton(
    items: List<PillSegmentedItem>,
    selectedIndex: Int,
    onSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    height: Dp = PillSegmentedButtonDefaults.Height,
    textStyle: TextStyle = AppTypography.bodyMdMedium,
    colors: PillSegmentedColors = PillSegmentedButtonDefaults.colors(),
    trackBorder: BorderStroke? = null,
    indicatorBorder: BorderStroke? = null,
    animationDurationMs: Int = PillSegmentedButtonDefaults.AnimationDurationMs,
) {
    if (items.size < 2) return

    val safeIndex = selectedIndex.coerceIn(0, items.lastIndex)
    val trackPadding = PillSegmentedButtonDefaults.TrackPadding
    val itemGap = PillSegmentedButtonDefaults.ItemGap

    val outerShape: Shape = RoundedCornerShape(percent = 50)
    val indicatorShape: Shape = RoundedCornerShape(percent = 50)

    // Har bir item o'z actual offset va width'ini (px da) xabar qiladi.
    // Indicator shu qiymatlarni to'g'ridan-to'g'ri ishlatadi → no rounding drift.
    var tabPositions by remember(items.size) {
        mutableStateOf(List(items.size) { TabPx(0, 0) })
    }
    val current = tabPositions[safeIndex]

    // M3 Expressive: indikator mavzudagi prujina bilan siljiydi va kengligini o'zgartiradi (default spatial)
    val spatialSpec = MaterialTheme.motionScheme.defaultSpatialSpec<Float>()
    val indicatorOffsetAnim = remember { Animatable(0f) }
    val indicatorWidthAnim = remember { Animatable(0f) }

    LaunchedEffect(current) {
        if (current.width == 0) return@LaunchedEffect
        if (indicatorWidthAnim.value == 0f) {
            // Birinchi o'lchov — animatsiyasiz
            indicatorOffsetAnim.snapTo(current.offset.toFloat())
            indicatorWidthAnim.snapTo(current.width.toFloat())
        } else {
            launch {
                indicatorOffsetAnim.animateTo(
                    current.offset.toFloat(),
                    spatialSpec,
                )
            }
            launch {
                indicatorWidthAnim.animateTo(
                    current.width.toFloat(),
                    spatialSpec,
                )
            }
        }
    }

    Box(
        modifier = modifier
            .height(height)
            .clip(outerShape)
            .background(colors.trackColor)
            .then(
                if (trackBorder != null) Modifier.border(trackBorder, outerShape)
                else Modifier
            )
            .padding(trackPadding),
    ) {
        // ── Sliding indicator (pixel-precise) ─────────────────
        if (current.width > 0) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .offset {
                        IntOffset(indicatorOffsetAnim.value.roundToInt(), 0)
                    }
                    .layout { measurable, constraints ->
                        val w = indicatorWidthAnim.value.roundToInt().coerceAtLeast(0)
                        val placeable = measurable.measure(
                            constraints.copy(minWidth = w, maxWidth = w)
                        )
                        layout(placeable.width, placeable.height) {
                            placeable.place(0, 0)
                        }
                    }
                    .clip(indicatorShape)
                    .background(colors.indicatorColor)
                    .then(
                        if (indicatorBorder != null) Modifier.border(indicatorBorder, indicatorShape)
                        else Modifier
                    ),
            )
        }

        // ── Items row ─────────────────────────
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(itemGap),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            items.forEachIndexed { index, item ->
                val isSelected = index == selectedIndex
                PillSegmentedItemContent(
                    item = item,
                    isSelected = isSelected,
                    enabled = enabled,
                    textStyle = textStyle,
                    colors = colors,
                    onClick = { Haptics.tick(); onSelected(index) },
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .onGloballyPositioned { coords ->
                            val newPos = TabPx(
                                offset = coords.positionInParent().x.roundToInt(),
                                width = coords.size.width,
                            )
                            if (tabPositions.getOrNull(index) != newPos) {
                                tabPositions = tabPositions.toMutableList().also {
                                    if (index < it.size) it[index] = newPos
                                }
                            }
                        },
                )
            }
        }
    }
}

@Composable
private fun PillSegmentedItemContent(
    item: PillSegmentedItem,
    isSelected: Boolean,
    enabled: Boolean,
    textStyle: TextStyle,
    colors: PillSegmentedColors,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }

    val textColor = if (isSelected) colors.selectedTextColor else colors.unselectedTextColor
    val iconColor = if (isSelected) colors.selectedIconColor else colors.unselectedIconColor

    Row(
        modifier = modifier
            .clickable(
                enabled = enabled,
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
            )
            .padding(horizontal = 6.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        if (item.icon != null) {
            Icon(
                painter = item.icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(PillSegmentedButtonDefaults.IconSize),
            )
            Spacer(Modifier.width(PillSegmentedButtonDefaults.IconTextGap))
        }
        Text(
            text = item.label,
            color = textColor,
            style = textStyle,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f, fill = false),
        )
    }
}

private data class TabPx(val offset: Int, val width: Int)

// ═════════════════════════════════════════════════════════════
// Preview
// ═════════════════════════════════════════════════════════════

@Preview
@Composable
private fun PillSegmentedButtonPreviewLight() {
    TikonchaParentTheme(ThemeMode.LIGHT) {
        PillSegmentedButtonPreviewContent()
    }
}

@Preview
@Composable
private fun PillSegmentedButtonPreviewDark() {
    TikonchaParentTheme(ThemeMode.DARK) {
        PillSegmentedButtonPreviewContent()
    }
}

@Composable
private fun PillSegmentedButtonPreviewContent() {
    var selected2 by remember { mutableStateOf(0) }
    var selected3 by remember { mutableStateOf(2) }   // oxirgi item — to'g'ri turishini tekshirish uchun
    var selected4 by remember { mutableStateOf(3) }   // 4 ta item, oxirgi
    var selectedLong by remember { mutableStateOf(1) }
    var selectedIcon by remember { mutableStateOf(1) }

    val borderColor = AppColors.border.primary

    androidx.compose.foundation.layout.Column(
        modifier = Modifier
            .background(AppColors.bg.secondary)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // 2 items (borderless)
        PillSegmentedButton(
            items = listOf(
                PillSegmentedItem("Kunlik"),
                PillSegmentedItem("Soatlik"),
            ),
            selectedIndex = selected2,
            onSelected = { selected2 = it },
            modifier = Modifier.fillMaxWidth(),
        )

        // 3 items, oxirgisi tanlangan — pixel-precise tekshirish
        PillSegmentedButton(
            items = listOf(
                PillSegmentedItem("Kun"),
                PillSegmentedItem("Hafta"),
                PillSegmentedItem("Oy"),
            ),
            selectedIndex = selected3,
            onSelected = { selected3 = it },
            modifier = Modifier.fillMaxWidth(),
        )

        // 4 items, oxirgisi tanlangan
        PillSegmentedButton(
            items = listOf(
                PillSegmentedItem("XS"),
                PillSegmentedItem("S"),
                PillSegmentedItem("M"),
                PillSegmentedItem("L"),
            ),
            selectedIndex = selected4,
            onSelected = { selected4 = it },
            modifier = Modifier.fillMaxWidth(),
        )

        // Uzun matn — ellipsis tekshirish
        PillSegmentedButton(
            items = listOf(
                PillSegmentedItem("Farzandingiz"),
                PillSegmentedItem("Maktabingiz"),
                PillSegmentedItem("Sozlamalaringiz"),
            ),
            selectedIndex = selectedLong,
            onSelected = { selectedLong = it },
            modifier = Modifier.fillMaxWidth(),
            trackBorder = BorderStroke(
                PillSegmentedButtonDefaults.BorderWidth,
                borderColor,
            ),
        )

        // Iconlar bilan
        PillSegmentedButton(
            items = listOf(
                PillSegmentedItem("Otasi", painterResource(Res.drawable.father_icon)),
                PillSegmentedItem("Onasi", painterResource(Res.drawable.mather_icon)),
            ),
            selectedIndex = selectedIcon,
            onSelected = { selectedIcon = it },
            modifier = Modifier.fillMaxWidth(),
        )

        // Disabled
        PillSegmentedButton(
            items = listOf(
                PillSegmentedItem("Kunlik"),
                PillSegmentedItem("Soatlik"),
            ),
            selectedIndex = 0,
            onSelected = {},
            enabled = false,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}