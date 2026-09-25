package uz.tikoncha_parent.presentation.statistic

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.EnterExitState
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import tikoncha_parents.composeapp.generated.resources.Res
import tikoncha_parents.composeapp.generated.resources.stat_quick_add_cd
import tikoncha_parents.composeapp.generated.resources.stat_quick_remove_cd
import uz.tikoncha_parent.presentation.base.haptics.rememberAppHaptics
import uz.tikoncha_parent.ui.theme.AppColors
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.TimeSource

/**
 * Ilovani bir bosishda yopish/ochish tugmasi — iOS uslubida (Student bilan bir xil, PARENTS_UX §3.1).
 *
 * Tugma obunasiz ota-onaga ham ko'rinadi — obuna faqat bosilgandan keyin so'raladi:
 * yashirilgan tugma xususiyatning borligini ko'rsatmaydi.
 *
 * Harakat:
 *  · barmoq bosib turganda tugma biroz kichrayadi (prujina)
 *  · so'rov ketayotganda ikonka xira, atrofida halqa aylanadi
 *  · server javob bergach — holat HAQIQATAN o'zgarganda: eski ikonka kichrayib
 *    yo'qoladi, yangisi burilib prujina bilan chiqadi, tugma "pop" qiladi,
 *    tashqariga halqa to'lqini tarqaladi, fon rangi almashadi; shu paytda haptika.
 *  Sinxronizatsiya bilan kelgan o'zgarish animatsiya bilan, lekin tebranishsiz.
 */
@Composable
fun QuickBlockButton(
    isBlocked: Boolean,
    isBusy: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    /** Ro'yxatda bor, lekin ishlamayapti (obuna yo'q / to'xtatilgan) — kulrang doira, sariq qulf. */
    inactive: Boolean = false,
) {
    val haptics = rememberAppHaptics()
    val filled = isBlocked && !inactive

    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()

    // Faqat foydalanuvchi bosgandan keyingi o'zgarish "qurollangan" — sinxronizatsiya jim o'tadi
    var armed by remember { mutableStateOf(false) }
    var lastClick by remember { mutableStateOf<TimeSource.Monotonic.ValueTimeMark?>(null) }
    var shown by remember { mutableStateOf(isBlocked) }
    val pop = remember { Animatable(1f) }
    val ring = remember { Animatable(0f) }      // 0 — yo'q, 1 — to'liq tarqalgan

    LaunchedEffect(isBlocked) {
        if (isBlocked == shown) return@LaunchedEffect       // birinchi kompozitsiya
        shown = isBlocked
        if (armed) {
            haptics.toggle(isBlocked)
            armed = false
        }
        launch {
            ring.snapTo(0f)
            ring.animateTo(1f, tween(durationMillis = 560, easing = FastOutSlowInEasing))
            ring.snapTo(0f)
        }
        pop.snapTo(1f)
        pop.animateTo(1.16f, tween(durationMillis = 110))
        pop.animateTo(1f, spring(dampingRatio = 0.32f, stiffness = Spring.StiffnessMediumLow))
    }
    LaunchedEffect(armed) {
        // Javob kelmadi yoki holat o'zgarmadi (xato) — keyingi sinxronizatsiya titratmasin
        if (armed) {
            delay(ARM_TIMEOUT_MS)
            armed = false
        }
    }

    val pressScale by animateFloatAsState(
        targetValue = if (pressed && !isBusy) 0.86f else 1f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = Spring.StiffnessMedium),
        label = "lockPress",
    )
    val background by animateColorAsState(
        targetValue = if (filled) AppColors.action.primary else AppColors.bg.tertiary,
        animationSpec = tween(durationMillis = 260),
        label = "lockBg",
    )
    val tint by animateColorAsState(
        targetValue = when {
            filled -> AppColors.icon.inverse
            isBlocked -> AppColors.icon.accentWarning
            else -> AppColors.icon.secondary
        },
        animationSpec = tween(durationMillis = 260),
        label = "lockTint",
    )
    val ringColor = if (filled) AppColors.action.primary else AppColors.icon.secondary
    val description = stringResource(if (isBlocked) Res.string.stat_quick_remove_cd else Res.string.stat_quick_add_cd)

    Box(
        modifier = modifier
            .size(40.dp)
            .graphicsLayer {
                val s = pop.value * pressScale
                scaleX = s
                scaleY = s
            }
            // Halqa to'lqini — doira tashqarisida, shuning uchun clip'dan OLDIN
            .drawBehind {
                val p = ring.value
                if (p > 0f) {
                    drawCircle(
                        color = ringColor.copy(alpha = 0.5f * (1f - p)),
                        radius = size.minDimension / 2f * (1f + 0.6f * p),
                        style = Stroke(width = 2.5.dp.toPx() * (1f - 0.4f * p)),
                    )
                }
            }
            .clip(CircleShape)
            .background(background)
            .semantics {
                contentDescription = description
                role = Role.Button
            }
            .clickable(interactionSource = interaction, indication = null, enabled = !isBusy) {
                val previous = lastClick
                if (previous != null && previous.elapsedNow() < CLICK_DEBOUNCE) return@clickable
                lastClick = TimeSource.Monotonic.markNow()
                armed = true
                onClick()
            },
        contentAlignment = Alignment.Center,
    ) {
        AnimatedContent(
            targetState = isBlocked,
            transitionSpec = {
                (scaleIn(spring(dampingRatio = 0.42f, stiffness = Spring.StiffnessMedium), initialScale = 0.3f) + fadeIn(tween(120)))
                    .togetherWith(scaleOut(tween(110), targetScale = 0.3f) + fadeOut(tween(110)))
            },
            label = "lockIcon",
        ) { blocked ->
            // Kirayotgan ikonka burilib keladi: yopilayotgan qulf chapdan, ochilayotgan o'ngdan
            val turn by transition.animateFloat(
                transitionSpec = { spring(dampingRatio = 0.5f, stiffness = Spring.StiffnessMedium) },
                label = "lockTurn",
            ) { s -> if (s == EnterExitState.Visible) 0f else if (blocked) -40f else 40f }
            Icon(
                imageVector = if (blocked) Icons.Rounded.Lock else LockOpenRounded,
                contentDescription = null,
                tint = tint,
                modifier = Modifier
                    .size(20.dp)
                    .graphicsLayer { rotationZ = turn }
                    .alpha(if (isBusy) 0.4f else 1f),
            )
        }
        if (isBusy) {
            CircularProgressIndicator(
                modifier = Modifier.matchParentSize(),
                color = if (filled) AppColors.icon.inverse else AppColors.action.primary,
                strokeWidth = 2.dp,
            )
        }
    }
}

private const val ARM_TIMEOUT_MS = 5_000L
private val CLICK_DEBOUNCE = 400.milliseconds
