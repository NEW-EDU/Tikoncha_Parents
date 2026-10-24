package uz.tikoncha_parent.presentation.onboarding

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import tikoncha_parents.composeapp.generated.resources.Res
import tikoncha_parents.composeapp.generated.resources.maktab
import tikoncha_parents.composeapp.generated.resources.ob_chat_child_msg
import tikoncha_parents.composeapp.generated.resources.ob_day_games
import tikoncha_parents.composeapp.generated.resources.ob_day_study
import tikoncha_parents.composeapp.generated.resources.ob_day_today
import tikoncha_parents.composeapp.generated.resources.ob_day_video
import tikoncha_parents.composeapp.generated.resources.ob_min_fmt
import tikoncha_parents.composeapp.generated.resources.ob_notif_message
import tikoncha_parents.composeapp.generated.resources.ob_notif_request
import tikoncha_parents.composeapp.generated.resources.ob_notif_task
import tikoncha_parents.composeapp.generated.resources.ob_rule_limit
import tikoncha_parents.composeapp.generated.resources.ob_rule_limit_value
import tikoncha_parents.composeapp.generated.resources.ob_rule_school
import tikoncha_parents.composeapp.generated.resources.ob_rule_sleep
import tikoncha_parents.composeapp.generated.resources.ob_task_sample
import tikoncha_parents.composeapp.generated.resources.ob_total_fmt
import tikoncha_parents.composeapp.generated.resources.qalqonni_ochirish_sorovi
import tikoncha_parents.composeapp.generated.resources.rad_etish
import tikoncha_parents.composeapp.generated.resources.ruxsat_berish
import tikoncha_parents.composeapp.generated.resources.tikoncha_logo
import kotlin.math.PI
import kotlin.math.roundToInt
import kotlin.math.sin

/*
 * Sahna rasmlari — haqiqiy ilova ekranlarining soddalashtirilgan, jonli "miniatyuralari".
 * Har biri [p] (0..1, sahna vaqti) bo'yicha o'ynaydi; ranglar faqat karamel + neytral.
 */

// ───────────────────────── 1. Kunini ko'ring ─────────────────────────

@Composable
internal fun ScreenTimeIllustration(p: Float, modifier: Modifier = Modifier) {
    val minutes = (192 * easeOut(stage(p, 0.10f, 0.70f))).roundToInt()
    GlassCard(modifier) {
        Column(Modifier.fillMaxWidth()) {
            Text(stringResource(Res.string.ob_day_today), style = labelStyle(13, FontWeight.SemiBold, Night.inkDim))
            Text(
                stringResource(Res.string.ob_total_fmt, minutes / 60, minutes % 60),
                style = labelStyle(28, FontWeight.ExtraBold),
            )
            Spacer(Modifier.height(12.dp))
            // Hafta ustunlari — bugungisi karamel
            val week = listOf(0.55f, 0.72f, 0.48f, 0.80f, 0.62f, 0.40f, 0.68f)
            Row(
                Modifier.fillMaxWidth().height(56.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.Bottom,
            ) {
                week.forEachIndexed { i, h ->
                    val t = backOut(stage(p, 0.08f + i * 0.05f, 0.40f + i * 0.05f))
                    Box(
                        Modifier
                            .weight(1f)
                            .fillMaxHeight(fraction = (h * t).coerceIn(0.02f, 1f))
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (i == week.lastIndex) Night.caramel else Night.caramel.copy(alpha = 0.28f))
                    )
                }
            }
            Spacer(Modifier.height(14.dp))
            val apps = listOf(
                Triple(Res.string.ob_day_video, 74, 1.0f),
                Triple(Res.string.ob_day_games, 58, 0.78f),
                Triple(Res.string.ob_day_study, 40, 0.54f),
            )
            apps.forEachIndexed { i, (label, mins, share) ->
                val t = easeOut(stage(p, 0.40f + i * 0.08f, 0.80f + i * 0.06f))
                Row(
                    Modifier.fillMaxWidth().padding(vertical = 5.dp).graphicsLayer { alpha = stage(p, 0.36f + i * 0.08f, 0.5f + i * 0.08f) },
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    AppGlyph(i)
                    Spacer(Modifier.width(10.dp))
                    Column(Modifier.weight(1f)) {
                        Row(Modifier.fillMaxWidth()) {
                            Text(stringResource(label), style = labelStyle(13), modifier = Modifier.weight(1f))
                            Text(stringResource(Res.string.ob_min_fmt, (mins * t).roundToInt()), style = labelStyle(12, FontWeight.Medium, Night.inkDim))
                        }
                        Spacer(Modifier.height(5.dp))
                        Box(Modifier.fillMaxWidth().height(5.dp).clip(CircleShape).background(Night.track)) {
                            Box(Modifier.fillMaxWidth(share * t).fillMaxHeight().clip(CircleShape).background(Night.caramel))
                        }
                    }
                }
            }
        }
    }
}

/** Ilova belgisi o'rnida neytral glif: video / o'yin / kitob. */
@Composable
private fun AppGlyph(kind: Int) {
    Box(
        Modifier.size(30.dp).clip(RoundedCornerShape(9.dp)).background(Night.caramel.copy(alpha = 0.18f)),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(Modifier.size(14.dp)) {
            val c = Night.caramelSoft
            when (kind) {
                0 -> drawPath(Path().apply { moveTo(size.width * 0.2f, 0f); lineTo(size.width, size.height / 2); lineTo(size.width * 0.2f, size.height); close() }, c)
                1 -> {
                    drawRoundRect(c, size = Size(size.width, size.height * 0.62f), topLeft = Offset(0f, size.height * 0.2f), cornerRadius = CornerRadius(size.height * 0.3f))
                    drawCircle(Night.top, radius = size.width * 0.09f, center = Offset(size.width * 0.72f, size.height * 0.5f))
                }
                else -> {
                    drawRoundRect(c, size = Size(size.width * 0.46f, size.height), cornerRadius = CornerRadius(2f))
                    drawRoundRect(c, topLeft = Offset(size.width * 0.54f, 0f), size = Size(size.width * 0.46f, size.height), cornerRadius = CornerRadius(2f))
                }
            }
        }
    }
}

// ───────────────────────── 2. Qoidalar ─────────────────────────

@Composable
internal fun RulesIllustration(p: Float, modifier: Modifier = Modifier) {
    val haptic = LocalHapticFeedback.current
    val rows = listOf(
        Triple(Res.string.ob_rule_sleep, "21:30 – 07:00", 0),
        Triple(Res.string.ob_rule_school, "08:00 – 14:00", 1),
        Triple(Res.string.ob_rule_limit, null, 2),
    )
    val limitValue = stringResource(Res.string.ob_rule_limit_value)
    GlassCard(modifier) {
        Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            rows.forEachIndexed { i, (title, time, glyph) ->
                val enter = easeOut(stage(p, 0.04f + i * 0.08f, 0.30f + i * 0.08f))
                val on = stage(p, 0.42f + i * 0.16f, 0.52f + i * 0.16f)
                val switched = on >= 1f
                LaunchedEffect(switched) { if (switched) haptic.performHapticFeedback(HapticFeedbackType.ToggleOn) }
                Row(
                    Modifier
                        .fillMaxWidth()
                        .graphicsLayer { alpha = enter; translationX = (1f - enter) * 30.dp.toPx() }
                        .clip(RoundedCornerShape(18.dp))
                        .background(lerpColor(Night.card, Night.caramel.copy(alpha = 0.14f), on))
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    RuleGlyph(glyph, on)
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(stringResource(title), style = labelStyle(15, FontWeight.Bold))
                        Text(time ?: limitValue, style = labelStyle(12, FontWeight.Medium, Night.inkDim))
                    }
                    MiniSwitch(on)
                }
            }
        }
    }
}

@Composable
private fun RuleGlyph(kind: Int, on: Float) {
    Canvas(Modifier.size(28.dp)) {
        val c = lerpColor(Night.inkFaint, Night.caramel, on)
        val r = size.minDimension / 2
        val center = Offset(size.width / 2, size.height / 2)
        when (kind) {
            0 -> { // oy
                drawCircle(c, r * 0.8f, center)
                drawCircle(Night.top, r * 0.66f, center + Offset(r * 0.38f, -r * 0.30f))
            }
            1 -> { // soat
                drawCircle(c, r * 0.8f, center, style = Stroke(r * 0.18f))
                drawLine(c, center, center + Offset(0f, -r * 0.48f), r * 0.16f, StrokeCap.Round)
                drawLine(c, center, center + Offset(r * 0.36f, 0f), r * 0.16f, StrokeCap.Round)
            }
            else -> { // qum soat
                val w = r * 1.1f; val h = r * 1.5f
                val top = center.y - h / 2; val bottom = center.y + h / 2
                drawPath(Path().apply {
                    moveTo(center.x - w / 2, top); lineTo(center.x + w / 2, top); lineTo(center.x, center.y); close()
                    moveTo(center.x - w / 2, bottom); lineTo(center.x + w / 2, bottom); lineTo(center.x, center.y); close()
                }, c)
            }
        }
    }
}

@Composable
private fun MiniSwitch(on: Float) {
    Box(
        Modifier
            .size(width = 44.dp, height = 26.dp)
            .clip(CircleShape)
            .background(lerpColor(Night.track, Night.caramel, on))
            .padding(3.dp),
    ) {
        Box(
            Modifier
                .offset(x = 18.dp * backOut(on).coerceAtMost(1.08f))
                .size(20.dp)
                .clip(CircleShape)
                .background(Color.White)
        )
    }
}

// ───────────────────────── 3. Xarita ─────────────────────────

@Composable
internal fun MapIllustration(p: Float, modifier: Modifier = Modifier) {
    val haptic = LocalHapticFeedback.current
    val pulse by rememberInfiniteTransition().animateFloat(0f, 1f, infiniteRepeatable(tween(1800, easing = LinearEasing)))
    val drop = stage(p, 0.30f, 0.62f)
    val landed = drop >= 1f
    LaunchedEffect(landed) { if (landed) haptic.performHapticFeedback(HapticFeedbackType.Confirm) }
    val schoolLabel = stringResource(Res.string.maktab)

    Box(modifier.clip(RoundedCornerShape(28.dp)).border(1.dp, Night.cardBorder, RoundedCornerShape(28.dp))) {
        Canvas(Modifier.fillMaxSize()) {
            drawRect(Color(0xFF221A12))
            // Mahalla bloklari
            val block = Color(0xFF2C2218)
            val cols = 4; val rows = 5
            val gw = size.width / cols; val gh = size.height / rows
            for (cx in 0 until cols) for (cy in 0 until rows) {
                drawRoundRect(block, topLeft = Offset(cx * gw + 7f, cy * gh + 7f), size = Size(gw - 14f, gh - 14f), cornerRadius = CornerRadius(14f))
            }
            // Bog'
            drawRoundRect(Night.leaf.copy(alpha = 0.22f), topLeft = Offset(gw * 2 + 7f, gh * 3 + 7f), size = Size(gw * 2 - 14f, gh - 14f), cornerRadius = CornerRadius(22f))
            // Katta ko'cha
            val road = Path().apply {
                moveTo(-20f, size.height * 0.78f)
                cubicTo(size.width * 0.3f, size.height * 0.62f, size.width * 0.55f, size.height * 0.92f, size.width + 20f, size.height * 0.48f)
            }
            drawPath(road, Color(0xFF3A2E22), style = Stroke(26f, cap = StrokeCap.Round))
            drawPath(road, Night.caramel.copy(alpha = 0.35f), style = Stroke(2f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(14f, 14f))))

            // Maktab hududi
            val zone = Offset(size.width * 0.36f, size.height * 0.36f)
            val zr = size.minDimension * 0.25f * easeOut(stage(p, 0.04f, 0.34f))
            drawCircle(Night.caramel.copy(alpha = 0.12f), zr, zone)
            drawCircle(Night.caramel.copy(alpha = 0.85f), zr, zone, style = Stroke(3f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 10f))))

            // Farzand belgisi: tepadan tushadi, keyin to'lqin tarqatadi
            val pin = Offset(size.width * 0.66f, size.height * 0.58f)
            if (landed) {
                for (k in 0..1) {
                    val t = (pulse + k * 0.5f) % 1f
                    drawCircle(Night.caramel.copy(alpha = 0.45f * (1f - t)), 14.dp.toPx() + t * 34.dp.toPx(), pin, style = Stroke(2.dp.toPx()))
                }
            }
            if (drop > 0f) {
                val y = pin.y - (1f - backOut(drop)) * 120.dp.toPx()
                drawPinShadow(pin, drop)
                drawCircle(Color.White, 15.dp.toPx(), Offset(pin.x, y))
                drawCircle(Night.caramel, 12.dp.toPx(), Offset(pin.x, y))
                drawCircle(Color.White.copy(alpha = 0.9f), 4.5.dp.toPx(), Offset(pin.x, y - 2.5.dp.toPx()))
                drawCircle(Color.White.copy(alpha = 0.9f), 6.5.dp.toPx(), Offset(pin.x, y + 7.dp.toPx()), style = Stroke(2.2.dp.toPx()))
            }
        }
        // Maktab yorlig'i
        Box(
            Modifier
                .align(Alignment.TopStart)
                .padding(start = 18.dp, top = 18.dp)
                .graphicsLayer { alpha = stage(p, 0.2f, 0.4f) }
                .clip(CircleShape)
                .background(Night.caramel)
                .padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
            Text(schoolLabel, style = labelStyle(12, FontWeight.ExtraBold, Color.White))
        }
    }
}

private fun DrawScope.drawPinShadow(at: Offset, drop: Float) {
    drawOval(
        Color.Black.copy(alpha = 0.35f * drop),
        topLeft = Offset(at.x - 12.dp.toPx() * drop, at.y + 13.dp.toPx()),
        size = Size(24.dp.toPx() * drop, 6.dp.toPx()),
    )
}

// ───────────────────────── 4. Doim yonida ─────────────────────────

@Composable
internal fun ChatIllustration(p: Float, modifier: Modifier = Modifier) {
    val haptic = LocalHapticFeedback.current
    val press = stage(p, 0.74f, 0.84f)
    val pressed = press >= 1f
    LaunchedEffect(pressed) { if (pressed) haptic.performHapticFeedback(HapticFeedbackType.Confirm) }

    Column(modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Farzand xabari
        val m = backOut(stage(p, 0.06f, 0.30f))
        Box(
            Modifier
                .graphicsLayer { alpha = stage(p, 0.06f, 0.18f); scaleX = m; scaleY = m; transformOrigin = androidx.compose.ui.graphics.TransformOrigin(0f, 1f) }
                .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomEnd = 20.dp, bottomStart = 6.dp))
                .background(Night.card)
                .border(1.dp, Night.cardBorder, RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomEnd = 20.dp, bottomStart = 6.dp))
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Text(stringResource(Res.string.ob_chat_child_msg), style = labelStyle(15, FontWeight.SemiBold))
        }
        // So'rov kartasi
        val c = easeOut(stage(p, 0.32f, 0.58f))
        GlassCard(Modifier.fillMaxWidth().graphicsLayer { alpha = c; translationY = (1f - c) * 40.dp.toPx() }) {
            Column(Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    ShieldGlyph()
                    Spacer(Modifier.width(10.dp))
                    Text(stringResource(Res.string.qalqonni_ochirish_sorovi), style = labelStyle(15, FontWeight.Bold), modifier = Modifier.weight(1f))
                }
                Spacer(Modifier.height(14.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    ChoicePill(stringResource(Res.string.rad_etish), filled = false, press = 0f, modifier = Modifier.weight(1f))
                    ChoicePill(stringResource(Res.string.ruxsat_berish), filled = true, press = press, modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun ShieldGlyph() {
    Canvas(Modifier.size(24.dp)) {
        val w = size.width; val h = size.height
        drawPath(Path().apply {
            moveTo(w / 2, 0f)
            lineTo(w * 0.92f, h * 0.16f)
            cubicTo(w * 0.92f, h * 0.62f, w * 0.7f, h * 0.86f, w / 2, h)
            cubicTo(w * 0.3f, h * 0.86f, w * 0.08f, h * 0.62f, w * 0.08f, h * 0.16f)
            close()
        }, Night.caramel)
    }
}

@Composable
private fun ChoicePill(text: String, filled: Boolean, press: Float, modifier: Modifier = Modifier) {
    // Bosilish: biroz kichrayib qaytadi, keyin to'liq karamel
    val squeeze = if (press in 0.01f..0.99f) 1f - sin(press * PI).toFloat() * 0.06f else 1f
    Box(
        modifier
            .graphicsLayer { scaleX = squeeze; scaleY = squeeze }
            .clip(RoundedCornerShape(14.dp))
            .background(if (filled) lerpColor(Night.caramel.copy(alpha = 0.35f), Night.caramel, press) else Night.track)
            .padding(vertical = 11.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(text, style = labelStyle(13, FontWeight.Bold, if (filled) Color.White else Night.inkDim))
    }
}

// ───────────────────────── 5. Vazifa — mukofot ─────────────────────────

@Composable
internal fun TasksIllustration(p: Float, modifier: Modifier = Modifier) {
    val haptic = LocalHapticFeedback.current
    val check = stage(p, 0.38f, 0.56f)
    val done = check >= 1f
    LaunchedEffect(done) { if (done) haptic.performHapticFeedback(HapticFeedbackType.Confirm) }
    val fly = stage(p, 0.56f, 0.92f)
    val coins = 120 + (20 * easeOut(stage(p, 0.70f, 0.95f))).roundToInt()

    Column(modifier, horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(14.dp)) {
        // Tangalar hisobi
        Row(
            Modifier
                .graphicsLayer { alpha = stage(p, 0.0f, 0.15f) }
                .clip(CircleShape)
                .background(Night.card)
                .border(1.dp, Night.cardBorder, CircleShape)
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Coin(18)
            Spacer(Modifier.width(8.dp))
            Text("$coins", style = labelStyle(17, FontWeight.ExtraBold))
        }
        Box {
            GlassCard(Modifier.fillMaxWidth().graphicsLayer { val e = easeOut(stage(p, 0.06f, 0.30f)); alpha = e; translationY = (1f - e) * 30.dp.toPx() }) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CheckCircle(check)
                    Spacer(Modifier.width(12.dp))
                    Text(
                        stringResource(Res.string.ob_task_sample),
                        style = labelStyle(16, FontWeight.Bold, lerpColor(Night.ink, Night.inkDim, check)),
                        modifier = Modifier.weight(1f),
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("+20", style = labelStyle(15, FontWeight.ExtraBold, Night.caramelSoft))
                        Spacer(Modifier.width(4.dp))
                        Coin(16)
                    }
                }
            }
            // Uchib ketayotgan tangalar
            if (fly in 0.001f..0.999f) {
                repeat(3) { k ->
                    val t = stage(fly, k * 0.12f, 0.7f + k * 0.12f)
                    Box(
                        Modifier
                            .align(Alignment.CenterEnd)
                            .padding(end = 24.dp)
                            .graphicsLayer {
                                translationY = -t * 70.dp.toPx()
                                translationX = sin(t * PI).toFloat() * (k - 1) * 18.dp.toPx()
                                alpha = 1f - t
                            }
                    ) { Coin(16) }
                }
            }
        }
    }
}

@Composable
private fun Coin(sizeDp: Int) {
    Canvas(Modifier.size(sizeDp.dp)) {
        drawCircle(Night.caramel)
        drawCircle(Night.caramelSoft, radius = size.minDimension * 0.34f, style = Stroke(size.minDimension * 0.08f))
    }
}

@Composable
private fun CheckCircle(t: Float) {
    Canvas(Modifier.size(28.dp)) {
        val r = size.minDimension / 2
        drawCircle(lerpColor(Night.track, Night.caramel, t), r)
        if (t > 0f) {
            val a = Offset(size.width * 0.28f, size.height * 0.52f)
            val b = Offset(size.width * 0.44f, size.height * 0.68f)
            val c = Offset(size.width * 0.74f, size.height * 0.36f)
            val first = (t / 0.4f).coerceAtMost(1f)
            drawLine(Color.White, a, a + (b - a) * first, 3.dp.toPx(), StrokeCap.Round)
            if (t > 0.4f) {
                val second = ((t - 0.4f) / 0.6f).coerceAtMost(1f)
                drawLine(Color.White, b, b + (c - b) * second, 3.dp.toPx(), StrokeCap.Round)
            }
        }
    }
}

// ───────────────────────── Bildirishnomalar ─────────────────────────

@Composable
internal fun NotificationStack(p: Float, modifier: Modifier = Modifier) {
    val haptic = LocalHapticFeedback.current
    val items = listOf(
        Res.string.ob_notif_request to Res.string.qalqonni_ochirish_sorovi,
        Res.string.ob_notif_message to Res.string.ob_chat_child_msg,
        Res.string.ob_notif_task to Res.string.ob_task_sample,
    )
    Column(modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        items.forEachIndexed { i, (title, body) ->
            val t = stage(p, 0.10f + i * 0.18f, 0.40f + i * 0.18f)
            val arrived = t >= 1f
            LaunchedEffect(arrived) { if (arrived) haptic.performHapticFeedback(HapticFeedbackType.SegmentTick) }
            Row(
                Modifier
                    .fillMaxWidth()
                    .graphicsLayer {
                        val e = backOut(t)
                        alpha = t.coerceAtMost(1f)
                        translationY = (1f - e) * (-50).dp.toPx()
                        scaleX = 0.92f + 0.08f * e; scaleY = scaleX
                    }
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFF2A2119))
                    .border(1.dp, Night.cardBorder, RoundedCornerShape(20.dp))
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(Modifier.size(36.dp).clip(RoundedCornerShape(10.dp)).background(Night.caramel), contentAlignment = Alignment.Center) {
                    Icon(painterResource(Res.drawable.tikoncha_logo), contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(stringResource(title), style = labelStyle(14, FontWeight.ExtraBold))
                    Text(stringResource(body), style = labelStyle(13, FontWeight.Medium, Night.inkDim), maxLines = 1)
                }
            }
        }
    }
}

/** Salomlashuv sahnasi: tipratikon ortidan tarqaluvchi halqalar. */
@Composable
internal fun HelloRings(modifier: Modifier = Modifier) {
    val t by rememberInfiniteTransition().animateFloat(0f, 1f, infiniteRepeatable(tween(3600, easing = LinearEasing), RepeatMode.Restart))
    Canvas(modifier) {
        val center = Offset(size.width / 2, size.height / 2)
        val maxR = size.minDimension / 2
        for (k in 0..2) {
            val f = (t + k / 3f) % 1f
            drawCircle(Night.caramel.copy(alpha = 0.32f * (1f - f)), maxR * (0.45f + 0.55f * f), center, style = Stroke(1.5.dp.toPx()))
        }
    }
}
