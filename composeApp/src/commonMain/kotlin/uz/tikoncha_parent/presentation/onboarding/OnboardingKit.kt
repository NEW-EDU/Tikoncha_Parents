package uz.tikoncha_parent.presentation.onboarding

import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import uz.tikoncha_parent.ui.expressiveButtonShapes
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Button
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import uz.tikoncha_parent.ui.theme.NunitoFontFamily
import kotlin.math.PI
import kotlin.math.sin

/**
 * Tanishtiruv (onboarding) dunyosi: ota-ona brendining "tungi" varianti — qahva-qora fon, karamel nur
 * (#C3955B — ilovaning yagona aksenti) va tipratikonning tikanlari kabi sekin uchib tushayotgan barglar.
 * Sahnalar shu to'plamdan quriladi: [NightBackdrop], [MascotActor], [KineticTitle], [PageDots], [CtaButton].
 */
internal object Night {
    val top = Color(0xFF19120B)
    val bottom = Color(0xFF0B0805)
    val caramel = Color(0xFFC3955B)
    val caramelSoft = Color(0xFFE2C49B)
    val leaf = Color(0xFF6E9A55)
    val ink = Color(0xFFFFF6EA)
    val inkDim = Color(0xFFFFF6EA).copy(alpha = 0.66f)
    val inkFaint = Color(0xFFFFF6EA).copy(alpha = 0.34f)
    val card = Color(0xFFFFF6EA).copy(alpha = 0.07f)
    val cardBorder = Color(0xFFFFF6EA).copy(alpha = 0.10f)
    val track = Color(0xFFFFF6EA).copy(alpha = 0.10f)
}

/** 0..1 oralig'idagi umumiy jarayondan [start]..[end] bo'lagini 0..1 ga cho'zadi. */
internal fun stage(p: Float, start: Float, end: Float): Float =
    ((p - start) / (end - start)).coerceIn(0f, 1f)

/** Yumshoq chiqish (ease-out cubic). */
internal fun easeOut(t: Float): Float = 1f - (1f - t) * (1f - t) * (1f - t)

/** Biroz "otilib" chiqish (back-out) — elementlar o'z joyiga sakrab tushadi. */
internal fun backOut(t: Float): Float {
    val c1 = 1.70158f
    val c3 = c1 + 1f
    val x = t - 1f
    return 1f + c3 * x * x * x + c1 * x * x
}

/** Sahna faol bo'lganda 0→1 ga boradigan "kino vaqti". Har safar qaytadan o'ynaydi. */
@Composable
internal fun rememberScenePlay(active: Boolean, durationMs: Int = 1800): Float {
    val anim = remember { Animatable(0f) }
    LaunchedEffect(active) {
        if (active) {
            anim.snapTo(0f)
            anim.animateTo(1f, tween(durationMs, easing = LinearEasing))
        }
    }
    return anim.value
}

private data class Leaf(val x: Float, val speed: Float, val size: Float, val sway: Float, val phase: Float, val spin: Float, val alpha: Float)

/**
 * Tungi fon: gradient, sahnaga qarab siljiydigan karamel nur ([glowX] — 0..1) va uchayotgan barglar.
 */
@Composable
internal fun NightBackdrop(glowX: Float, modifier: Modifier = Modifier, content: @Composable BoxScope.() -> Unit) {
    val leaves = remember {
        // Deterministik "tasodif" — har ochilishda bir xil kompozitsiya
        List(16) { i ->
            val r = { k: Int -> ((sin((i * 12.9898 + k * 78.233)) * 43758.5453) % 1.0).let { if (it < 0) it + 1 else it }.toFloat() }
            Leaf(
                x = r(1),
                speed = 0.018f + r(2) * 0.03f,
                size = 7f + r(3) * 11f,
                sway = 10f + r(4) * 26f,
                phase = r(5),
                spin = (r(6) - 0.5f) * 2f,
                alpha = 0.18f + r(7) * 0.32f,
            )
        }
    }
    val time by rememberInfiniteTransition().animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(tween(1_000_000, easing = LinearEasing)),
    )
    val breathe by rememberInfiniteTransition().animateFloat(
        initialValue = 0.85f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(3200, easing = FastOutSlowInEasing), RepeatMode.Reverse),
    )

    Box(
        modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Night.top, Night.bottom)))
    ) {
        Canvas(Modifier.fillMaxSize()) {
            // Karamel nur — sahna markazi orqasida
            val center = Offset(size.width * glowX, size.height * 0.30f)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Night.caramel.copy(alpha = 0.30f * breathe), Night.caramel.copy(alpha = 0.08f), Color.Transparent),
                    center = center,
                    radius = size.width * 0.85f,
                ),
                radius = size.width * 0.85f,
                center = center,
            )
            // Barglar
            leaves.forEach { leaf ->
                val t = (leaf.phase + time * leaf.speed) % 1f
                val y = -40f + t * (size.height + 80f)
                val x = leaf.x * size.width + sin((t * 2 * PI * 1.3 + leaf.phase * 6).toFloat()) * leaf.sway
                val angle = t * 360f * leaf.spin + leaf.phase * 180f
                val s = leaf.size * density / 2.4f
                rotate(angle, pivot = Offset(x, y)) {
                    val path = Path().apply {
                        moveTo(x, y - s)
                        quadraticTo(x + s * 0.62f, y, x, y + s)
                        quadraticTo(x - s * 0.62f, y, x, y - s)
                        close()
                    }
                    drawPath(path, Night.leaf.copy(alpha = leaf.alpha))
                }
            }
        }
        content()
    }
}

/**
 * Sahnalar bo'ylab yuradigan tipratikon "aktyor". [pose] almashganda sakrab almashadi, doim sekin nafas oladi.
 */
@Composable
internal fun MascotActor(pose: DrawableResource, size: Dp, modifier: Modifier = Modifier) {
    val bob by rememberInfiniteTransition().animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(2600, easing = LinearEasing)),
    )
    AnimatedContent(
        targetState = pose,
        transitionSpec = {
            (scaleIn(spring(dampingRatio = 0.55f, stiffness = Spring.StiffnessMediumLow), initialScale = 0.6f) + fadeIn(tween(180)))
                .togetherWith(scaleOut(tween(160), targetScale = 0.8f) + fadeOut(tween(140)))
        },
        modifier = modifier,
    ) { p ->
        Image(
            painter = painterResource(p),
            contentDescription = null,
            modifier = Modifier
                .size(size)
                .graphicsLayer {
                    val w = sin(bob * 2 * PI).toFloat()
                    translationY = w * 5.dp.toPx()
                    scaleX = 1f + w * 0.012f
                    scaleY = 1f - w * 0.012f
                },
        )
    }
}

/** So'zlar birin-ketin pastdan ko'tarilib chiqadigan sarlavha. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun KineticTitle(text: String, play: Float, modifier: Modifier = Modifier, style: TextStyle = titleStyle()) {
    val words = remember(text) { text.split(' ').filter { it.isNotBlank() } }
    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center,
    ) {
        words.forEachIndexed { i, word ->
            val t = easeOut(stage(play, 0.04f + i * 0.07f, 0.34f + i * 0.07f))
            Text(
                text = if (i < words.lastIndex) "$word " else word,
                style = style,
                modifier = Modifier.graphicsLayer {
                    alpha = t
                    translationY = (1f - t) * 18.dp.toPx()
                },
            )
        }
    }
}

@Composable
internal fun titleStyle() = TextStyle(
    fontFamily = NunitoFontFamily(),
    fontWeight = FontWeight.ExtraBold,
    fontSize = 30.sp,
    lineHeight = 36.sp,
    color = Night.ink,
    textAlign = TextAlign.Center,
)

@Composable
internal fun bodyStyle() = TextStyle(
    fontFamily = NunitoFontFamily(),
    fontWeight = FontWeight.Medium,
    fontSize = 16.sp,
    lineHeight = 23.sp,
    color = Night.inkDim,
    textAlign = TextAlign.Center,
)

@Composable
internal fun labelStyle(size: Int = 13, weight: FontWeight = FontWeight.SemiBold, color: Color = Night.ink) = TextStyle(
    fontFamily = NunitoFontFamily(),
    fontWeight = weight,
    fontSize = size.sp,
    lineHeight = (size + 5).sp,
    color = color,
)

/** Sahifa nuqtalari: faoli cho'ziladi, siljish paytida silliq oqadi. */
@Composable
internal fun PageDots(count: Int, position: Float, modifier: Modifier = Modifier) {
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
        repeat(count) { i ->
            val near = (1f - kotlin.math.abs(position - i)).coerceIn(0f, 1f)
            Box(
                Modifier
                    .height(6.dp)
                    .width(6.dp + 16.dp * near)
                    .clip(CircleShape)
                    .background(lerpColor(Night.inkFaint, Night.caramel, near))
            )
        }
    }
}

internal fun lerpColor(a: Color, b: Color, t: Float) = Color(
    red = a.red + (b.red - a.red) * t,
    green = a.green + (b.green - a.green) * t,
    blue = a.blue + (b.blue - a.blue) * t,
    alpha = a.alpha + (b.alpha - a.alpha) * t,
)

/** Karamel asosiy tugma. */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun CtaButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true) {
    // M3 Expressive tugma: dam olishda to'liq yumaloq, bosilganda burchaklari siqiladi (shape morph)
    Button(
        onClick = onClick,
        enabled = enabled,
        shapes = expressiveButtonShapes(),
        colors = ButtonDefaults.buttonColors(containerColor = Night.caramel, contentColor = Color.White),
        modifier = modifier.fillMaxWidth().height(ButtonDefaults.MediumContainerHeight),
    ) {
        Text(text, style = labelStyle(17, FontWeight.ExtraBold, Color.White), modifier = Modifier.padding(horizontal = 16.dp))
    }
}

/** Sahna rasmlari turadigan shisha karta. */
@Composable
internal fun GlassCard(modifier: Modifier = Modifier, content: @Composable BoxScope.() -> Unit) {
    Box(
        modifier
            .clip(RoundedCornerShape(28.dp))
            .background(Night.card)
            .border(1.dp, Night.cardBorder, RoundedCornerShape(28.dp))
            .padding(18.dp),
        content = content,
    )
}
