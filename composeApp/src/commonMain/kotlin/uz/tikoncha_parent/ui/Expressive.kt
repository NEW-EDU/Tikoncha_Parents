package uz.tikoncha_parent.ui

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Dp
import androidx.compose.animation.core.animateDpAsState
import kotlin.time.Clock
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.clip
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonShapes
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer

/*
 * Material 3 Expressive yordamchilari — butun ilova bir xil "his" qilishi uchun:
 *  • tugmalar dam olishda to'liq yumaloq, bosilganda burchaklari kichrayib "siqiladi" (shape morph);
 *  • kartalar va bosiladigan yuzalar bosilganda prujina bilan biroz kichrayadi (MotionScheme fast spatial);
 * Barcha harakat mavzudagi `MotionScheme.expressive()` dan olinadi — qo'lda davomiylik/egri chiziq yo'q.
 */

/**
 * Tugma shakllari: [rest] — dam olishdagi shakl (berilmasa, M3E bo'yicha to'liq yumaloq),
 * bosilganda — o'rta tugmaning "pressed" shakli. Morf faqat burchakli shakllar orasida ishlaydi.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun expressiveButtonShapes(rest: Shape? = null): ButtonShapes {
    val base = (rest as? CornerBasedShape) ?: CircleShape
    return ButtonDefaults.shapes(shape = base, pressedShape = ButtonDefaults.mediumPressedShape)
}

/**
 * Bosilganda prujina bilan biroz kichrayish (kartalar, katta bosiladigan bloklar uchun).
 * [interactionSource] — shu elementning `clickable` ida ishlatilgan manba.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun Modifier.expressivePress(
    interactionSource: InteractionSource,
    pressedScale: Float = 0.97f,
    /** Bosilgan zahoti taktil zarba ([Haptics]) — student ilovasidagi tugmalar kabi. */
    haptic: Boolean = true,
): Modifier {
    val pressed by interactionSource.collectIsPressedAsState()
    if (haptic) LaunchedEffect(pressed) { if (pressed) Haptics.tap() }
    val scale by animateFloatAsState(
        targetValue = if (pressed) pressedScale else 1f,
        animationSpec = MaterialTheme.motionScheme.fastSpatialSpec(),
        label = "expressivePress",
    )
    return this.graphicsLayer { scaleX = scale; scaleY = scale }
}

/**
 * M3 Expressive karta: butun karta (fon bilan birga) bosilganda prujina bilan siqiladi; ikki marta tez bosish
 * e'tiborsiz qoldiriladi (avvalgi `singleClick` kabi). Zanjirda o'lcham modifikatorlaridan keyin, ichki
 * `padding` dan oldin qo'yiladi.
 */
@Composable
fun Modifier.expressiveCard(
    color: Color,
    shape: Shape = RoundedCornerShape(LargeCardCornerRadius),
    debounceMs: Long = 500L,
    haptic: Boolean = true,
    onClick: () -> Unit,
): Modifier {
    val source = remember { MutableInteractionSource() }
    var lastClick by remember { mutableLongStateOf(0L) }
    return this
        .expressivePress(source, haptic = haptic)
        .clip(shape)
        .background(color, shape)
        .clickable(interactionSource = source, indication = null) {
            val now = Clock.System.now().toEpochMilliseconds()
            if (now - lastClick >= debounceMs) {
                lastClick = now
                onClick()
            }
        }
}

/** M3 Expressive guruhlangan ro'yxat: elementlar orasidagi bo'shliq (bir butun blok bo'lib ko'rinadi). */
val SegmentGap = 3.dp

/**
 * M3 Expressive guruhlangan (segmented) ro'yxat elementi — Android 16 sozlamalari uslubi:
 * guruhning faqat tashqi burchaklari katta ([outer]), ichki burchaklar kichik ([inner]), elementlar orasida
 * [SegmentGap] — bir necha element bitta blok bo'lib ko'rinadi. Bosilganda ichki burchaklar ham kattalashadi
 * (shakl morfi) va element prujina bilan biroz siqiladi.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun Modifier.expressiveSegment(
    index: Int,
    count: Int,
    color: Color,
    outer: Dp = 28.dp,
    inner: Dp = 6.dp,
    /** true — elementlar yonma-yon (chap/o'ng tashqi burchaklar katta). */
    horizontal: Boolean = false,
    /** Tanlangan element — barcha burchaklari katta (M3E: tanlov shakl bilan ham bildiriladi). */
    selected: Boolean = false,
    debounceMs: Long = 500L,
    haptic: Boolean = true,
    onClick: () -> Unit,
): Modifier {
    val source = remember { MutableInteractionSource() }
    val pressed by source.collectIsPressedAsState()
    var lastClick by remember { mutableLongStateOf(0L) }
    val spec = MaterialTheme.motionScheme.fastSpatialSpec<Dp>()
    val first = index == 0
    val last = index == count - 1
    val lead by animateDpAsState(if (first || pressed || selected) outer else inner, spec, label = "segLead")
    val trail by animateDpAsState(if (last || pressed || selected) outer else inner, spec, label = "segTrail")
    val shape = if (horizontal) {
        RoundedCornerShape(topStart = lead, bottomStart = lead, topEnd = trail, bottomEnd = trail)
    } else {
        RoundedCornerShape(topStart = lead, topEnd = lead, bottomEnd = trail, bottomStart = trail)
    }
    return this
        .expressivePress(source, pressedScale = 0.985f, haptic = haptic)
        .clip(shape)
        .background(color, shape)
        .clickable(interactionSource = source, indication = null) {
            val now = Clock.System.now().toEpochMilliseconds()
            if (now - lastClick >= debounceMs) {
                lastClick = now
                onClick()
            }
        }
}

/** Guruhlangan ro'yxat elementining (bosilmaydigan) shakli: tashqi burchaklar [outer], ichkilari [inner]. */
fun segmentShape(index: Int, count: Int, outer: Dp = 28.dp, inner: Dp = 6.dp): RoundedCornerShape {
    val top = if (index == 0) outer else inner
    val bottom = if (index == count - 1) outer else inner
    return RoundedCornerShape(topStart = top, topEnd = top, bottomEnd = bottom, bottomStart = bottom)
}

/**
 * Panjara (grid) ko'rinishidagi guruh elementi: guruhning faqat to'rt tashqi burchagi katta ([outer]), qolganlari
 * kichik ([inner]) — 2×3 bloklar bitta yaxlit plitka bo'lib ko'rinadi. Bosilganda barcha burchaklar kattalashadi.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun Modifier.expressiveGridSegment(
    row: Int,
    col: Int,
    rows: Int,
    cols: Int,
    color: Color,
    outer: Dp = 28.dp,
    inner: Dp = 6.dp,
    debounceMs: Long = 500L,
    onClick: () -> Unit,
): Modifier {
    val source = remember { MutableInteractionSource() }
    val pressed by source.collectIsPressedAsState()
    var lastClick by remember { mutableLongStateOf(0L) }
    val spec = MaterialTheme.motionScheme.fastSpatialSpec<Dp>()
    val top = row == 0
    val bottom = row == rows - 1
    val start = col == 0
    val end = col == cols - 1
    val ts by animateDpAsState(if (pressed || (top && start)) outer else inner, spec, label = "gTs")
    val te by animateDpAsState(if (pressed || (top && end)) outer else inner, spec, label = "gTe")
    val bs by animateDpAsState(if (pressed || (bottom && start)) outer else inner, spec, label = "gBs")
    val be by animateDpAsState(if (pressed || (bottom && end)) outer else inner, spec, label = "gBe")
    val shape = RoundedCornerShape(topStart = ts, topEnd = te, bottomStart = bs, bottomEnd = be)
    return this
        .expressivePress(source, pressedScale = 0.975f)
        .clip(shape)
        .background(color, shape)
        .clickable(interactionSource = source, indication = null) {
            val now = Clock.System.now().toEpochMilliseconds()
            if (now - lastClick >= debounceMs) {
                lastClick = now
                onClick()
            }
        }
}
