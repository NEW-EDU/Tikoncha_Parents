@file:Suppress("unused")

package uz.tikoncha_parent.presentation.statistic

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalDensity
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.pluralStringResource
import tikoncha_parents.composeapp.generated.resources.Res
import tikoncha_parents.composeapp.generated.resources.hours
import tikoncha_parents.composeapp.generated.resources.minutes
import uz.tikoncha_parent.domain.model.HourMinute
import uz.tikoncha_parent.ui.theme.AppColors
import uz.tikoncha_parent.ui.theme.AppTypography
import uz.tikoncha_parent.ui.theme.ThemeMode
import uz.tikoncha_parent.ui.theme.TikonchaParentTheme
import kotlin.math.ceil
import kotlin.math.max

private val MIN_AXIS_WIDTH   = 24.dp
private val GAP_AFTER_AXIS   = 4.dp
private val CHART_HEIGHT     = 150.dp
private val X_LABELS_HEIGHT  = 20.dp

@Composable
fun UsageBarChart(
    bars: List<ChartBarUi>,
    mode: DateSelectionType,
    chartSubtitle: ChartSubtitle?,
    onBarClick: (ChartBarUi) -> Unit,
    modifier: Modifier = Modifier,
    barColor: Color = AppColors.bg.primary,
) {
    val maxMinutes = remember(bars) {
        bars.maxOfOrNull { it.valueMinutes }?.coerceAtLeast(0.0) ?: 0.0
    }
    val axis = rememberAxisModel(maxMinutes)

    Column(modifier = modifier) {
        when (mode) {
            DateSelectionType.WEEK -> WeeklyChartContent(bars, axis, barColor, onBarClick)
            DateSelectionType.DAY  -> DailyChartContent(bars, axis, barColor, onBarClick)
        }

        if (chartSubtitle != null) {
            Spacer(Modifier.height(8.dp))
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text(
                    text = chartSubtitleString(chartSubtitle),
                    style = AppTypography.titleSmMedium,
                    color = AppColors.text.secondary,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

/* ============ WEEKLY ============ */

@Composable
private fun WeeklyChartContent(
    bars: List<ChartBarUi>,
    axis: AxisModel,
    barColor: Color,
    onBarClick: (ChartBarUi) -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth().height(CHART_HEIGHT)) {
            AxisLeft(axis = axis, modifier = Modifier.width(axis.width).fillMaxHeight())
            Spacer(Modifier.width(GAP_AFTER_AXIS))
            Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                BarsCanvas(
                    bars = bars,
                    maxMinutes = axis.maxMinutes,
                    slotWidthOverride = null,
                    barColor = barColor,
                )
                Row(Modifier.fillMaxSize()) {
                    bars.forEach { bar ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clickable(enabled = bar.totalMillis > 0L) { onBarClick(bar) }
                        )
                    }
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(X_LABELS_HEIGHT)
                .padding(start = axis.width + GAP_AFTER_AXIS, top = 2.dp)
        ) {
            bars.forEach { bar ->
                Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    Text(
                        text = barLabel(bar, DateSelectionType.WEEK),
                        style = AppTypography.bodySmRegular,
                        color = AppColors.text.secondary
                    )
                }
            }
        }
    }
}

/* ============ DAILY (scrollable) ============ */

@Composable
private fun DailyChartContent(
    bars: List<ChartBarUi>,
    axis: AxisModel,
    barColor: Color,
    onBarClick: (ChartBarUi) -> Unit,
) {
    val scroll = rememberScrollState()

    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val barArea = (maxWidth - axis.width - GAP_AFTER_AXIS).coerceAtLeast(1.dp)
        val binWidth = (barArea / 8).coerceIn(40.dp, 64.dp)
        val contentWidth = binWidth * bars.size.coerceAtLeast(1)

        Column(modifier = Modifier.fillMaxWidth()) {
            Row(modifier = Modifier.fillMaxWidth().height(CHART_HEIGHT)) {
                AxisLeft(axis = axis, modifier = Modifier.width(axis.width).fillMaxHeight())
                Spacer(Modifier.width(GAP_AFTER_AXIS))
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .horizontalScroll(scroll)
                ) {
                    Box(modifier = Modifier.width(contentWidth).fillMaxHeight()) {
                        BarsCanvas(
                            bars = bars,
                            maxMinutes = axis.maxMinutes,
                            slotWidthOverride = binWidth,
                            barColor = barColor,
                        )
                        Row(Modifier.fillMaxSize()) {
                            bars.forEach { bar ->
                                Box(
                                    modifier = Modifier
                                        .width(binWidth)
                                        .fillMaxHeight()
                                        .clickable(enabled = bar.totalMillis > 0L) { onBarClick(bar) }
                                )
                            }
                        }
                    }
                }
            }

            // X labels — chart bilan birgalikda scroll
            Row(modifier = Modifier.fillMaxWidth().height(X_LABELS_HEIGHT).padding(top = 2.dp)) {
                Spacer(Modifier.width(axis.width + GAP_AFTER_AXIS))
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .horizontalScroll(scroll)
                ) {
                    Row(modifier = Modifier.width(contentWidth).fillMaxHeight()) {
                        bars.forEach { bar ->
                            Box(Modifier.width(binWidth).fillMaxHeight(), contentAlignment = Alignment.Center) {
                                Text(
                                    text = barLabel(bar, DateSelectionType.DAY),
                                    style = AppTypography.bodySmRegular,
                                    color = AppColors.text.secondary
                                )
                            }
                        }
                    }
                }
            }

            // Scroll indicator
            HorizontalScrollIndicator(
                scrollState = scroll,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp, start = axis.width + GAP_AFTER_AXIS)
            )
        }
    }
}

/* ============ AXIS ============ */

private data class AxisModel(
    val maxMinutes: Double,
    val midLabel: String,
    val topLabel: String,
    /** Yorliqlar to'liq so'z bilan ("2 soat", "30 daqiqa") — o'q kengligi ular sig'adigan qilib olinadi. */
    val width: Dp,
)

@Composable
private fun rememberAxisModel(maxMinutes: Double): AxisModel {
    val topHours = remember(maxMinutes) { axisTopHours(maxMinutes) }
    // "2с / 1с" emas — "2 soat / 1 soat" (qisqartma "soniya" deb ham öqiladi)
    val topLabel = pluralStringResource(Res.plurals.hours, topHours, topHours)
    val midLabel = if (topHours == 1) pluralStringResource(Res.plurals.minutes, 30, 30)
    else pluralStringResource(Res.plurals.hours, topHours / 2, topHours / 2)

    val measurer = rememberTextMeasurer()
    val style = AppTypography.bodySmRegular
    val density = LocalDensity.current
    return remember(topHours, topLabel, midLabel, style, density) {
        val labelPx = maxOf(
            measurer.measure(topLabel, style).size.width,
            measurer.measure(midLabel, style).size.width,
        )
        val width = with(density) { labelPx.toDp() } + 2.dp
        AxisModel(
            maxMinutes = topHours * 60.0,
            midLabel = midLabel,
            topLabel = topLabel,
            width = maxOf(MIN_AXIS_WIDTH, width),
        )
    }
}

private fun axisTopHours(maxMinutesRaw: Double): Int {
    val maxHours = max(0.0, maxMinutesRaw / 60.0)
    return when {
        maxHours <= 1.0 -> 1
        maxHours <= 2.0 -> 2
        else            -> (ceil(maxHours / 2.0) * 2.0).toInt()
    }.coerceAtMost(24)
}

@Composable
private fun AxisLeft(axis: AxisModel, modifier: Modifier = Modifier) {
    val color = AppColors.text.tertiary
    val style = AppTypography.bodySmRegular.copy(color = color)
    val measurer = rememberTextMeasurer()

    Canvas(modifier = modifier) {
        val topLayout = measurer.measure(axis.topLabel, style)
        val midLayout = measurer.measure(axis.midLabel, style)
        val h = size.height
        drawText(topLayout, topLeft = Offset(0f, 0f))
        drawText(midLayout, topLeft = Offset(0f, h / 2f - midLayout.size.height / 2f))
    }
}

/* ============ BARS + GRID ============ */

@Composable
private fun BarsCanvas(
    bars: List<ChartBarUi>,
    maxMinutes: Double,
    slotWidthOverride: Dp?,
    barColor: Color,
    cornerRadius: Dp = 10.dp,
) {
    val targets = remember(bars, maxMinutes) {
        val safeMax = maxMinutes.coerceAtLeast(1.0)
        bars.map { bar ->
            if (bar.totalMillis <= 0L) 0f
            else (bar.valueMinutes / safeMax).toFloat().coerceIn(0f, 1f)
        }
    }

    val animatables = remember { mutableStateListOf<Animatable<Float, *>>() }

    LaunchedEffect(targets) {
        while (animatables.size < targets.size) animatables.add(Animatable(0f))
        while (animatables.size > targets.size) animatables.removeAt(animatables.lastIndex)

        targets.forEachIndexed { i, target ->
            launch {
                animatables[i].animateTo(
                    targetValue = target,
                    animationSpec = tween(
                        durationMillis = 500,
                        delayMillis = i * 40,
                        easing = FastOutSlowInEasing
                    )
                )
            }
        }
    }

    val gridColor = AppColors.border.secondary
    val tickColor = AppColors.button.disabled

    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        val dash = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)

        val topY = 10.dp.toPx()    // label uchun joy
        val botY = h
        val midY = (topY + botY) / 2f

        listOf(topY, midY, botY).forEach { y ->
            val yy = if (y == h) h - 0.5f else y
            drawLine(
                color = gridColor,
                start = Offset(0f, yy),
                end = Offset(w, yy),
                strokeWidth = 1.dp.toPx(),
                pathEffect = dash
            )
        }

        val barBottomGap = 2.dp.toPx()
        val tickH        = 3.dp.toPx()
        val tickY        = h - tickH - barBottomGap
        val tickCorner   = CornerRadius(6.dp.toPx(), 6.dp.toPx())
        val minBarH      = 4.dp.toPx()

        val count = bars.size.coerceAtLeast(1)
        val slotW = slotWidthOverride?.toPx() ?: (w / count)
        val barW  = (slotW * 0.60f).coerceAtLeast(6.dp.toPx())
        val corner = CornerRadius(cornerRadius.toPx(), cornerRadius.toPx())

        bars.forEachIndexed { index, _ ->
            val centerX = (index + 0.5f) * slotW
            val left = centerX - barW / 2f
            val fr = animatables.getOrNull(index)?.value ?: 0f

            if (fr <= 0.001f) {
                drawRoundRect(
                    color = tickColor,
                    topLeft = Offset(left, tickY),
                    size = Size(barW, tickH),
                    cornerRadius = tickCorner
                )
            } else {
                val area = botY - topY
                val rawH = area * fr
                val barH = rawH.coerceAtLeast(minBarH)
                val top = botY - barH - barBottomGap

                drawRoundRect(
                    color = barColor,
                    topLeft = Offset(left, top),
                    size = Size(barW, barH),
                    cornerRadius = corner
                )
            }
        }
    }
}

/* ============ SCROLL INDICATOR ============ */

@Composable
private fun HorizontalScrollIndicator(
    scrollState: ScrollState,
    modifier: Modifier = Modifier,
    height: Dp = 3.dp,
    minThumb: Dp = 28.dp,
) {
    val maxScroll = scrollState.maxValue
    if (maxScroll <= 0) return

    val trackColor = AppColors.bg.tertiary
    val thumbColor = AppColors.bg.primary.copy(alpha = 0.4f)

    Canvas(modifier = modifier.height(height)) {
        val w = size.width
        val h = size.height

        drawRoundRect(
            color = trackColor,
            topLeft = Offset.Zero,
            size = Size(w, h),
            cornerRadius = CornerRadius(h, h)
        )

        // Thumb width ≈ proportional, lekin minThumb dan kichik bo'lmasin
        val ratio = 0.4f
        val thumbW = (w * ratio).coerceAtLeast(minThumb.toPx()).coerceAtMost(w)

        val progress = (scrollState.value.toFloat() / maxScroll.toFloat()).coerceIn(0f, 1f)
        val x = (w - thumbW) * progress

        drawRoundRect(
            color = thumbColor,
            topLeft = Offset(x, 0f),
            size = Size(thumbW, h),
            cornerRadius = CornerRadius(h, h)
        )
    }
}

@Preview
@Composable
private fun UsageBarChartPreview_Daily() {
    TikonchaParentTheme(ThemeMode.LIGHT) {
        Box(Modifier.background(AppColors.bg.surface).padding(16.dp)) {
            UsageBarChart(
                bars = previewDailyBars(),
                mode = DateSelectionType.DAY,
                chartSubtitle = null,
                onBarClick = {},
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Preview
@Composable
private fun UsageBarChartPreview_Weekly() {
    TikonchaParentTheme(ThemeMode.LIGHT) {
        Box(Modifier.background(AppColors.bg.surface).padding(16.dp)) {
            UsageBarChart(
                bars = previewWeeklyBars(),
                mode = DateSelectionType.WEEK,
                chartSubtitle = ChartSubtitle.WeeklyAverage(HourMinute(2, 14)),
                onBarClick = {},
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Preview
@Composable
private fun UsageBarChartPreview_Empty() {
    TikonchaParentTheme(ThemeMode.LIGHT) {
        Box(Modifier.background(AppColors.bg.surface).padding(16.dp)) {
            UsageBarChart(
                bars = emptyBars(DateSelectionType.WEEK),
                mode = DateSelectionType.WEEK,
                chartSubtitle = null,
                onBarClick = {},
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Preview
@Composable
private fun UsageBarChartPreview_Dark() {
    TikonchaParentTheme(ThemeMode.DARK) {
        Box(Modifier.background(AppColors.bg.surface).padding(16.dp)) {
            UsageBarChart(
                bars = previewDailyBars(),
                mode = DateSelectionType.DAY,
                chartSubtitle = null,
                onBarClick = {},
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}