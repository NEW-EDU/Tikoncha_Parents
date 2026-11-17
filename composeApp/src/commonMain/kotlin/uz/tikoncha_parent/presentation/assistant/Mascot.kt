package uz.tikoncha_parent.presentation.assistant

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import tikoncha_parents.composeapp.generated.resources.Res
import tikoncha_parents.composeapp.generated.resources.ai_confused
import tikoncha_parents.composeapp.generated.resources.ai_done
import tikoncha_parents.composeapp.generated.resources.ai_idle
import tikoncha_parents.composeapp.generated.resources.ai_idle_blink
import tikoncha_parents.composeapp.generated.resources.ai_listen_a
import tikoncha_parents.composeapp.generated.resources.ai_listen_b
import tikoncha_parents.composeapp.generated.resources.ai_talk_a
import tikoncha_parents.composeapp.generated.resources.ai_talk_b
import tikoncha_parents.composeapp.generated.resources.ai_think_a
import tikoncha_parents.composeapp.generated.resources.ai_think_b
import tikoncha_parents.composeapp.generated.resources.ai_wave
import uz.tikoncha_parent.presentation.assistant.voice.isReducedMotion
import uz.tikoncha_parent.ui.theme.AppColors
import kotlin.random.Random

/** Yordamçi tipratikanining holati — ViewModel boşqaradi. */
enum class MascotState { IDLE, WAVE, LISTENING, THINKING, TALKING, DONE, CONFUSED }

/** Holat kadrlari: ikkinçisi — almaşadigan (köz yumiş, quloq, öylaş, gapiriş). */
private fun frames(state: MascotState): Pair<DrawableResource, DrawableResource> = when (state) {
    MascotState.IDLE -> Res.drawable.ai_idle to Res.drawable.ai_idle_blink
    MascotState.WAVE -> Res.drawable.ai_wave to Res.drawable.ai_wave
    MascotState.LISTENING -> Res.drawable.ai_listen_a to Res.drawable.ai_listen_b
    MascotState.THINKING -> Res.drawable.ai_think_a to Res.drawable.ai_think_b
    MascotState.TALKING -> Res.drawable.ai_talk_a to Res.drawable.ai_talk_b
    MascotState.DONE -> Res.drawable.ai_done to Res.drawable.ai_done
    MascotState.CONFUSED -> Res.drawable.ai_confused to Res.drawable.ai_confused
}

/**
 * Tipratikan: kadr almaşiş + yumşoq harakat. IDLE — nafas va 3–5 s'da bir köz yumiş; LISTENING — quloqlar
 * a/b (~2,5 Hz) va [rings] bölsa ovozga qarab kengayadigan halqalar; THINKING — a/b ~0,6 s; TALKING — a/b
 * ~0,18 s; DONE — kiçkina sakraş; CONFUSED — boşini qiyşaytiradi. Tizimda "harakatni kamaytiriş" yoqilgan
 * bölsa — faqat holat kadri, harakatsiz.
 */
@Composable
internal fun Mascot(
    state: MascotState,
    size: Dp,
    modifier: Modifier = Modifier,
    rings: Boolean = false,
    level: () -> Float = { 0f },
) {
    val reduce = remember { isReducedMotion() }
    var alt by remember { mutableStateOf(false) }
    LaunchedEffect(state, reduce) {
        alt = false
        if (reduce) return@LaunchedEffect
        when (state) {
            MascotState.IDLE -> while (true) {
                delay(Random.nextLong(3_000, 5_000))
                alt = true
                delay(BLINK_MS)
                alt = false
            }
            MascotState.LISTENING -> while (true) {
                delay(200)
                alt = !alt
            }
            MascotState.THINKING -> while (true) {
                delay(600)
                alt = !alt
            }
            MascotState.TALKING -> while (true) {
                delay(180)
                alt = !alt
            }
            else -> Unit
        }
    }

    val t = rememberInfiniteTransition(label = "mascot")
    val breath by t.animateFloat(
        0f, 1f, infiniteRepeatable(tween(1_800, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "breath",
    )
    val hop = remember { Animatable(0f) }
    LaunchedEffect(state, reduce) {
        hop.snapTo(0f)
        if (state == MascotState.DONE && !reduce) {
            hop.animateTo(0f, keyframes {
                durationMillis = 650
                -1f at 160 using FastOutSlowInEasing
                0f at 330
                -0.4f at 460
                0f at 650
            })
        }
    }
    val tilt by animateFloatAsState(
        if (state == MascotState.CONFUSED && !reduce) -7f else 0f, spring(dampingRatio = 0.45f), label = "tilt",
    )

    Box(modifier.size(size), contentAlignment = Alignment.Center) {
        if (rings && state == MascotState.LISTENING) ListeningRings(reduce, level)
        Crossfade(targetState = state, animationSpec = tween(if (reduce) 0 else 160), label = "frame") { s ->
            val (a, b) = frames(s)
            // Ikkala kadr ham yuklangan tursin — almaşganda bir zum böş qolmasin
            val pa = painterResource(a)
            val pb = painterResource(b)
            Image(
                painter = if (alt && s == state) pb else pa,
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        if (reduce) return@graphicsLayer
                        transformOrigin = TransformOrigin(0.5f, 1f)
                        if (s == MascotState.IDLE || s == MascotState.WAVE || s == MascotState.LISTENING) {
                            scaleY = 1f + 0.018f * breath
                            scaleX = 1f - 0.008f * breath
                        }
                        translationY = hop.value * this.size.height * 0.10f
                        rotationZ = tilt
                    },
            )
        }
    }
}

/** Tinglayotganda: ikkita kengayib yöqoladigan halqa, ovoz baland bölsa kattaroq. */
@Composable
private fun ListeningRings(reduce: Boolean, level: () -> Float) {
    val color = AppColors.action.primary
    val t = rememberInfiniteTransition(label = "rings")
    val p by t.animateFloat(0f, 1f, infiniteRepeatable(tween(1_400, easing = LinearEasing)), label = "p")
    Canvas(Modifier.fillMaxSize()) {
        val base = this.size.minDimension / 2f * 0.80f
        val stroke = 3.dp.toPx()
        for (i in 0..1) {
            val phase = if (reduce) 0.4f + 0.3f * i else (p + i * 0.5f) % 1f
            val radius = base * (1f + 0.30f * phase + 0.22f * level())
            drawCircle(color.copy(alpha = (1f - phase) * 0.45f), radius = radius, style = Stroke(width = stroke))
        }
    }
}

private const val BLINK_MS = 150L
