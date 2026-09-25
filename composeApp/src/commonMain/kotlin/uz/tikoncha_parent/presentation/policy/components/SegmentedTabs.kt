package uz.tikoncha_parent.presentation.policy.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import uz.tikoncha_parent.presentation.base.haptics.rememberAppHaptics
import uz.tikoncha_parent.ui.theme.AppColors

/**
 * M3 Expressive connected button group — jadval ekranlaridagi hamma tanlov shu:
 * ro'yxat tablari, Ilovalar · Kategoriyalar · Saytlar, Kunlik | Soatlik, Hudud ichida | tashqarisida.
 * Student ilovasidagi komponent bilan bir xil.
 *
 *  · har tugma alohida, oralig'i 2dp; chetdagi burchaklar to'liq yumaloq, ichkilari 8dp
 *  · tanlangan — to'liq yumaloq bo'ladi (shape morph) va primary bilan to'ladi
 *  · bosib turilgan — ~15% kengayadi, qo'shnilari torayadi, ichki burchaklari 4dp ga siqiladi
 *
 * Barcha yorliqlar BIR XIL o'lchamda: eng uzuni sig'adigan eng katta shrift (14sp → 11sp).
 * [containerColor] — tanlanmagan tugmalar foni: kulrang karta ichida sahifa rangi bering.
 * Tanlov o'zgarganda o'zi tik beradi — chaqiruvchi haptika qo'shmasin.
 */
@Composable
fun SegmentedTabs(
    labels: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = AppColors.bg.section,
) {
    val base = PolicyText.tab
    val haptics = rememberAppHaptics()
    val measurer = rememberTextMeasurer()
    val density = LocalDensity.current
    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val count = labels.size.coerceAtLeast(1)
        // tugmalar orasi 2dp + har tugma ichida 8dp padding; bosilgan qo'shnisi biroz torayadi
        val itemTextWidthPx = with(density) {
            ((maxWidth - GAP * (count - 1)) / count * 0.9f - 16.dp).toPx()
        }
        val fontSize = remember(labels, itemTextWidthPx, base) {
            var size = base.fontSize.value
            while (size > MIN_SEGMENT_SP) {
                val fits = labels.all { label ->
                    measurer.measure(label, base.copy(fontSize = size.sp), maxLines = 1).size.width <= itemTextWidthPx
                }
                if (fits) break
                size -= 0.5f
            }
            size.sp
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(GAP)) {
            labels.forEachIndexed { index, label ->
                val active = index == selectedIndex
                val interaction = remember { MutableInteractionSource() }
                val pressed by interaction.collectIsPressedAsState()
                val squeeze = pressed && !active
                val weight by animateFloatAsState(
                    if (squeeze) PRESSED_WEIGHT else 1f,
                    spring(dampingRatio = 0.6f, stiffness = Spring.StiffnessMedium),
                    label = "tabWeight",
                )
                val inner = when {
                    active -> FULL
                    squeeze -> INNER_PRESSED
                    else -> INNER
                }
                val shapeSpring = spring<androidx.compose.ui.unit.Dp>(dampingRatio = 0.7f, stiffness = Spring.StiffnessMediumLow)
                val start by animateDpAsState(if (index == 0) FULL else inner, shapeSpring, label = "tabStart")
                val end by animateDpAsState(if (index == labels.lastIndex) FULL else inner, shapeSpring, label = "tabEnd")
                val bg by animateColorAsState(
                    when {
                        active -> AppColors.action.primary
                        squeeze -> AppColors.button.secondaryPressed
                        else -> containerColor
                    },
                    label = "tabBg",
                )
                val fg by animateColorAsState(if (active) AppColors.text.inverse else AppColors.text.primary, label = "tabFg")
                Box(
                    modifier = Modifier
                        .weight(weight)
                        .height(44.dp)
                        // clip qatlami yo'q: Android 10 (Galaxy A31) da to'liq yumaloq burchakli kesilgan
                        // qatlam matni bilan birga chizilmay qolardi — shakl faqat fonga beriladi
                        .background(bg, RoundedCornerShape(topStart = start, bottomStart = start, topEnd = end, bottomEnd = end))
                        .selectable(selected = active, interactionSource = interaction, indication = null, role = Role.Tab) {
                            if (index != selectedIndex) haptics.tick()
                            onSelect(index)
                        }
                        .padding(horizontal = 8.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = label,
                        style = base.copy(fontSize = fontSize, fontWeight = if (active) FontWeight.SemiBold else FontWeight.Medium),
                        color = fg,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

private val GAP = 2.dp
private val FULL = 22.dp          // 44dp balandlikning yarmi — to'liq yumaloq
private val INNER = 8.dp
private val INNER_PRESSED = 4.dp
private const val PRESSED_WEIGHT = 1.15f
private const val MIN_SEGMENT_SP = 11f
