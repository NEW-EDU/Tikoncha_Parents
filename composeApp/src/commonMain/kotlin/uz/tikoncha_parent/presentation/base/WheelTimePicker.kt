@file:Suppress("unused")

package uz.tikoncha_parent.presentation.base

/*
 * WheelTimePicker — Compose Multiplatform uchun smooth wheel time picker.
 *
 * v3 yangiliklari:
 *  • Ranglar WheelTimePickerColors klassiga yig'ildi
 *  • WheelTimePickerDefaults object — Material 3 idiom (Defaults.colors(), Defaults.ItemHeight, ...)
 *  • Hamma default qiymatlar bitta joyda — osongina themada override qilish mumkin
 *
 * Avvalgi versiyadan qolganlar:
 *  • 3D cylinder effect (rotationX + sinusoidal easing)
 *  • Indicator (corner radius bilan)
 *  • Smooth scroll + snap
 *  • Int va kotlinx.datetime.LocalTime overloadlari
 *
 * Figma variable'lari:
 *    accent-emphasis:    #C3955B  (selected, label)
 *    disabled-tertiary:  #94979C  (unselected)
 *    icon-secondary:     #85888E  (separator ":")
 *    modal-primary:      #FFFFFF  (background)
 *
 * Gradle:
 *   implementation(compose.foundation)
 *   implementation(compose.material3)
 *   implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.6.0")
 *   implementation(compose.components.uiToolingPreview)
 *   debugImplementation(compose.uiTooling)
 */

import uz.tikoncha_parent.presentation.base.haptics.rememberAppHaptics
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.unit.sp
import kotlinx.datetime.LocalTime
import org.jetbrains.compose.resources.stringResource
import kotlin.math.PI
import kotlin.math.absoluteValue
import kotlin.math.sin
import org.jetbrains.compose.ui.tooling.preview.Preview
import tikoncha_parents.composeapp.generated.resources.Res
import tikoncha_parents.composeapp.generated.resources.daqiqa
import tikoncha_parents.composeapp.generated.resources.soat
import uz.tikoncha_parent.ui.theme.AppColors

// ============================================================================
//                       C O L O R S   C L A S S
// ============================================================================

/**
 * Barcha `WheelTimePicker` ranglari uchun bitta konteyner.
 * Faqat kerakli ranglarni override qilish uchun [WheelTimePickerDefaults.colors]
 * factory'sidan foydalaning.
 */
@Immutable
data class WheelTimePickerColors(
    val selectedTextColor: Color,
    val unselectedTextColor: Color,
    val labelColor: Color,
    val separatorColor: Color,
    val backgroundColor: Color,
    val indicatorColor: Color,
)

// ============================================================================
//                       D E F A U L T S   O B J E C T
// ============================================================================

/**
 * `WheelTimePicker` uchun barcha default qiymatlar shu yerda.
 *
 * Oddiy ishlatish:
 * ```
 * WheelTimePicker(
 *     colors = WheelTimePickerDefaults.colors(),         // hamma figma ranglari
 * )
 * ```
 *
 * Faqat bitta rangni o'zgartirish:
 * ```
 * WheelTimePicker(
 *     colors = WheelTimePickerDefaults.colors(
 *         selectedTextColor = MaterialTheme.colorScheme.primary,
 *     ),
 * )
 * ```
 */
object WheelTimePickerDefaults {

    // ---- Shape & layout defaults ----
    val ContainerShape: Shape = RoundedCornerShape(24.dp)
    val IndicatorShape: Shape = RoundedCornerShape(16.dp)
    val ContentPadding: PaddingValues = PaddingValues(bottom = 16.dp)
    val IndicatorPadding: PaddingValues = PaddingValues(horizontal = 8.dp)
    val ItemHeight: Dp = 48.dp

    // ---- Behaviour defaults ----
    const val VisibleItemCount: Int = 5
    const val MaxWheelRotationDegrees: Float = 40f

    // ---- Color token defaults (Figma) ----


    /**
     * Barcha rang slotlari uchun default [WheelTimePickerColors] qaytaradi.
     * Har bir slot alohida override qilinishi mumkin.
     */
    @Composable
    fun colors(
        selectedTextColor: Color = AppColors.text.accentEmphasis,
        unselectedTextColor: Color = AppColors.text.tertiary,
        labelColor: Color = AppColors.text.accentEmphasis,
        separatorColor: Color = AppColors.icon.secondary,
        backgroundColor: Color = AppColors.modal.primary,
        indicatorColor: Color = AppColors.bg.primaryContainer,
    ): WheelTimePickerColors = WheelTimePickerColors(
        selectedTextColor = selectedTextColor,
        unselectedTextColor = unselectedTextColor,
        labelColor = labelColor,
        separatorColor = separatorColor,
        backgroundColor = backgroundColor,
        indicatorColor = indicatorColor,
    )

    // ---- Text style defaults ----
    val SelectedTextStyle: TextStyle = TextStyle(
        fontSize = 44.sp,
        fontWeight = FontWeight.SemiBold,
        textAlign = TextAlign.Center,
    )

    val UnselectedTextStyle: TextStyle = TextStyle(
        fontSize = 28.sp,
        fontWeight = FontWeight.SemiBold,
        textAlign = TextAlign.Center,
    )

    val LabelTextStyle: TextStyle = TextStyle(
        fontSize = 14.sp,
        fontWeight = FontWeight.Medium,
        textAlign = TextAlign.Center,
    )
}

// ============================================================================
//                      W H E E L   T I M E   P I C K E R
// ============================================================================

/**
 * Smooth wheel-style time picker with 3D cylinder effect.
 *
 * @param colors Barcha ranglar ([WheelTimePickerDefaults.colors] orqali yarating).
 * @param shape Picker konteyner shakli.
 * @param indicatorShape Indicator shakli (corner radius uchun).
 * @param contentPadding Picker ichki otstuplari.
 * @param indicatorPadding Indicator chap/o'ng otstuplari.
 * @param itemHeight Bitta wheel elementi balandligi.
 * @param visibleItemCount Bir vaqtda ko'rinadigan elementlar soni (toq, ≥3).
 * @param maxWheelRotationDegrees 3D cylinder bukilishi (°). `0f` — tekis ro'yxat.
 * @param showIndicator Tanlangan qator ortidagi indicator ko'rsatilsinmi.
 * @param selectedTextStyle Tanlangan matn stili.
 * @param unselectedTextStyle Boshqa matnlar stili.
 * @param labelTextStyle Label stili.
 * @param showHours / showMinutes / showLabels / showSeparator Ko'rinishni boshqarish.
 * @param hourLabel / minuteLabel / separator Matnlar.
 * @param hourRange / minuteRange Qabul qilinadigan oraliqlar.
 * @param initialHour / initialMinute Boshlang'ich qiymatlar.
 * @param onTimeChanged Vaqt o'zgarganda chaqiriladi.
 */
@Composable
fun WheelTimePicker(
    modifier: Modifier = Modifier,
    initialHour: Int = 0,
    initialMinute: Int = 0,
    visibleItemCount: Int = WheelTimePickerDefaults.VisibleItemCount,
    showHours: Boolean = true,
    showMinutes: Boolean = true,
    showLabels: Boolean = true,
    hourLabel: String = stringResource(Res.string.soat),
    minuteLabel: String = stringResource(Res.string.daqiqa),
    showSeparator: Boolean = true,
    separator: String = ":",
    hourRange: IntRange = 0..23,
    minuteRange: IntRange = 0..59,
    itemHeight: Dp = WheelTimePickerDefaults.ItemHeight,
    maxWheelRotationDegrees: Float = WheelTimePickerDefaults.MaxWheelRotationDegrees,
    showIndicator: Boolean = true,
    shape: Shape = WheelTimePickerDefaults.ContainerShape,
    indicatorShape: Shape = WheelTimePickerDefaults.IndicatorShape,
    contentPadding: PaddingValues = WheelTimePickerDefaults.ContentPadding,
    indicatorPadding: PaddingValues = WheelTimePickerDefaults.IndicatorPadding,
    colors: WheelTimePickerColors = WheelTimePickerDefaults.colors(),
    selectedTextStyle: TextStyle = WheelTimePickerDefaults.SelectedTextStyle,
    unselectedTextStyle: TextStyle = WheelTimePickerDefaults.UnselectedTextStyle,
    labelTextStyle: TextStyle = WheelTimePickerDefaults.LabelTextStyle,
    onTimeChanged: (hour: Int, minute: Int) -> Unit = { _, _ -> },
) {
    require(visibleItemCount >= 3) { "visibleItemCount kamida 3 bo'lishi kerak" }
    require(visibleItemCount % 2 == 1) { "visibleItemCount toq son bo'lishi kerak" }
    require(!hourRange.isEmpty()) { "hourRange bo'sh bo'lmasligi kerak" }
    require(!minuteRange.isEmpty()) { "minuteRange bo'sh bo'lmasligi kerak" }

    val hours = remember(hourRange) { hourRange.toList() }
    val minutes = remember(minuteRange) { minuteRange.toList() }

    var selectedHour by rememberSaveable { mutableStateOf(initialHour.coerceIn(hourRange)) }
    var selectedMinute by rememberSaveable { mutableStateOf(initialMinute.coerceIn(minuteRange)) }

    LaunchedEffect(selectedHour, selectedMinute) {
        onTimeChanged(selectedHour, selectedMinute)
    }

    Column(
        modifier = modifier
            .clip(shape)
            .background(colors.backgroundColor)
            .padding(contentPadding),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        // -------- Header (labels) --------
        if (showLabels && (showHours || showMinutes)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(34.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (showHours) {
                    Text(
                        text = hourLabel,
                        color = colors.labelColor,
                        style = labelTextStyle,
                        modifier = Modifier.weight(1f),
                    )
                }
                // Labellar picker ustunlari bilan tekislanishi uchun invisible ":"
                if (showHours && showMinutes && showSeparator) {
                    Text(
                        text = separator,
                        color = Color.Transparent,
                        style = selectedTextStyle,
                        maxLines = 1,
                    )
                }
                if (showMinutes) {
                    Text(
                        text = minuteLabel,
                        color = colors.labelColor,
                        style = labelTextStyle,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }

        // -------- Wheel area (indicator ortda, wheels oldda) --------
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center,
        ) {
            if (showIndicator) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(indicatorPadding)
                        .height(itemHeight)
                        .background(colors.indicatorColor, indicatorShape),
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (showHours) {
                    WheelNumberPicker(
                        items = hours,
                        initialIndex = hours.indexOf(selectedHour).coerceAtLeast(0),
                        visibleItemCount = visibleItemCount,
                        itemHeight = itemHeight,
                        maxWheelRotationDegrees = maxWheelRotationDegrees,
                        selectedTextColor = colors.selectedTextColor,
                        unselectedTextColor = colors.unselectedTextColor,
                        selectedTextStyle = selectedTextStyle,
                        unselectedTextStyle = unselectedTextStyle,
                        modifier = Modifier.weight(1f),
                        onSelectedIndexChange = { idx -> selectedHour = hours[idx] },
                    )
                }

                if (showHours && showMinutes && showSeparator) {
                    Text(
                        text = separator,
                        color = colors.separatorColor,
                        style = selectedTextStyle,
                    )
                }

                if (showMinutes) {
                    WheelNumberPicker(
                        items = minutes,
                        initialIndex = minutes.indexOf(selectedMinute).coerceAtLeast(0),
                        visibleItemCount = visibleItemCount,
                        itemHeight = itemHeight,
                        maxWheelRotationDegrees = maxWheelRotationDegrees,
                        selectedTextColor = colors.selectedTextColor,
                        unselectedTextColor = colors.unselectedTextColor,
                        selectedTextStyle = selectedTextStyle,
                        unselectedTextStyle = unselectedTextStyle,
                        modifier = Modifier.weight(1f),
                        onSelectedIndexChange = { idx -> selectedMinute = minutes[idx] },
                    )
                }
            }
        }
    }
}

/**
 * `kotlinx.datetime.LocalTime` overloadi.
 */
@Composable
fun WheelTimePicker(
    initialTime: LocalTime,
    modifier: Modifier = Modifier,
    visibleItemCount: Int = WheelTimePickerDefaults.VisibleItemCount,
    showHours: Boolean = true,
    showMinutes: Boolean = true,
    showLabels: Boolean = true,
    hourLabel: String = stringResource(Res.string.soat),
    minuteLabel: String = stringResource(Res.string.daqiqa),
    showSeparator: Boolean = true,
    separator: String = ":",
    hourRange: IntRange = 0..23,
    minuteRange: IntRange = 0..59,
    itemHeight: Dp = WheelTimePickerDefaults.ItemHeight,
    maxWheelRotationDegrees: Float = WheelTimePickerDefaults.MaxWheelRotationDegrees,
    showIndicator: Boolean = false,
    shape: Shape = WheelTimePickerDefaults.ContainerShape,
    indicatorShape: Shape = WheelTimePickerDefaults.IndicatorShape,
    contentPadding: PaddingValues = WheelTimePickerDefaults.ContentPadding,
    indicatorPadding: PaddingValues = WheelTimePickerDefaults.IndicatorPadding,
    colors: WheelTimePickerColors = WheelTimePickerDefaults.colors(),
    selectedTextStyle: TextStyle = WheelTimePickerDefaults.SelectedTextStyle,
    unselectedTextStyle: TextStyle = WheelTimePickerDefaults.UnselectedTextStyle,
    labelTextStyle: TextStyle = WheelTimePickerDefaults.LabelTextStyle,
    onTimeChanged: (LocalTime) -> Unit = {},
) {
    WheelTimePicker(
        modifier = modifier,
        initialHour = initialTime.hour,
        initialMinute = initialTime.minute,
        visibleItemCount = visibleItemCount,
        showHours = showHours,
        showMinutes = showMinutes,
        showLabels = showLabels,
        hourLabel = hourLabel,
        minuteLabel = minuteLabel,
        showSeparator = showSeparator,
        separator = separator,
        hourRange = hourRange,
        minuteRange = minuteRange,
        itemHeight = itemHeight,
        maxWheelRotationDegrees = maxWheelRotationDegrees,
        showIndicator = showIndicator,
        shape = shape,
        indicatorShape = indicatorShape,
        contentPadding = contentPadding,
        indicatorPadding = indicatorPadding,
        colors = colors,
        selectedTextStyle = selectedTextStyle,
        unselectedTextStyle = unselectedTextStyle,
        labelTextStyle = labelTextStyle,
        onTimeChanged = { h, m -> onTimeChanged(LocalTime(h, m)) },
    )
}

// ============================================================================
//                       I N T E R N A L   W H E E L
// ============================================================================

/**
 * Yagona raqamli wheel:
 *   • infinite scroll (Int.MAX_VALUE + modulo)
 *   • rememberSnapFlingBehavior orqali markazga snap
 *   • har bir element uchun rotationX + alpha + color + fontSize
 *     realtime derivedStateOf bilan hisoblanadi — smooth frame-by-frame.
 */
@Composable
private fun WheelNumberPicker(
    items: List<Int>,
    initialIndex: Int,
    visibleItemCount: Int,
    itemHeight: Dp,
    maxWheelRotationDegrees: Float,
    selectedTextColor: Color,
    unselectedTextColor: Color,
    selectedTextStyle: TextStyle,
    unselectedTextStyle: TextStyle,
    onSelectedIndexChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val count = items.size
    val halfCount = visibleItemCount / 2

    val startingCenter = remember(count, initialIndex) {
        val mid = Int.MAX_VALUE / 2
        mid - (mid % count) + initialIndex
    }
    val initialFirstVisibleItem = (startingCenter - halfCount).coerceAtLeast(0)

    val listState = rememberLazyListState(initialFirstVisibleItemIndex = initialFirstVisibleItem)
    val flingBehavior = rememberSnapFlingBehavior(listState)
    val density = LocalDensity.current
    val itemHeightPx = with(density) { itemHeight.toPx() }

    val selectedRealIndex by remember(count, halfCount) {
        derivedStateOf {
            val firstVisible = listState.firstVisibleItemIndex
            val offset = listState.firstVisibleItemScrollOffset
            val rounded = firstVisible + if (offset > itemHeightPx / 2f) 1 else 0
            ((rounded + halfCount) % count + count) % count
        }
    }

    // Markazdan har bir element o'tganda yengil tik — iOS picker kabi (birinchi joylashuvda jim)
    val haptics = rememberAppHaptics()
    var tickedIndex by remember { mutableIntStateOf(-1) }
    LaunchedEffect(selectedRealIndex) {
        if (tickedIndex != -1 && tickedIndex != selectedRealIndex) haptics.wheelTick()
        tickedIndex = selectedRealIndex
        onSelectedIndexChange(selectedRealIndex)
    }

    LazyColumn(
        state = listState,
        flingBehavior = flingBehavior,
        modifier = modifier.height(itemHeight * visibleItemCount),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        items(Int.MAX_VALUE) { absoluteIndex ->
            val realIndex = absoluteIndex % count
            val item = items[realIndex]

            // -1f (yuqori chet) .. 0f (markaz) .. +1f (pastki chet)
            val signedDistance by remember(absoluteIndex, halfCount) {
                derivedStateOf {
                    val info = listState.layoutInfo.visibleItemsInfo
                        .firstOrNull { it.index == absoluteIndex }
                        ?: return@derivedStateOf 0f
                    val viewportCenter = (listState.layoutInfo.viewportStartOffset +
                            listState.layoutInfo.viewportEndOffset) / 2f
                    val itemCenter = info.offset + info.size / 2f
                    val raw = (itemCenter - viewportCenter) / (halfCount * info.size.toFloat())
                    raw.coerceIn(-1f, 1f)
                }
            }

            val centerFraction = (1f - signedDistance.absoluteValue).coerceIn(0f, 1f)

            // Sinusoidal easing: markazda sekin, chetga qarab tezroq buriladi.
            val rotation = -sin(signedDistance * (PI.toFloat() / 2f)) * maxWheelRotationDegrees

            val itemAlpha = 0.35f + 0.65f * centerFraction
            val textColor = lerp(unselectedTextColor, selectedTextColor, centerFraction)
            val fontSize = lerp(
                unselectedTextStyle.fontSize,
                selectedTextStyle.fontSize,
                centerFraction,
            )
            val baseStyle = if (centerFraction > 0.5f) selectedTextStyle else unselectedTextStyle

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(itemHeight)
                    .graphicsLayer {
                        rotationX = rotation
                        alpha = itemAlpha
                        cameraDistance = 8f * this.density
                    },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = item.toString().padStart(2, '0'),
                    color = textColor,
                    style = baseStyle.copy(fontSize = fontSize),
                )
            }
        }
    }
}

// ============================================================================
//                              P R E V I E W S
// ============================================================================

private val PreviewBg = Color(0xFFF5F2EC)

@Preview
@Composable
private fun WheelTimePicker_Default_Preview() {
    Box(Modifier.background(PreviewBg).padding(16.dp)) {
        WheelTimePicker(
            initialHour = 4,
            initialMinute = 10,
            modifier = Modifier.width(320.dp),
        )
    }
}

@Preview
@Composable
private fun WheelTimePicker_FromLocalTime_Preview() {
    Box(Modifier.background(PreviewBg).padding(16.dp)) {
        WheelTimePicker(
            initialTime = LocalTime(hour = 7, minute = 25),
            modifier = Modifier.width(320.dp),
        )
    }
}

@Preview
@Composable
private fun WheelTimePicker_CustomColors_Preview() {
    // Shu yerda faqat kerakli ranglarni override qilyapmiz.
    Box(Modifier.background(PreviewBg).padding(16.dp)) {
        WheelTimePicker(
            initialHour = 11,
            initialMinute = 55,
            colors = WheelTimePickerDefaults.colors(
                selectedTextColor = Color(0xFF0A7AFF),
                labelColor = Color(0xFF0A7AFF),
                indicatorColor = Color(0x1F0A7AFF),
            ),
            modifier = Modifier.width(320.dp),
        )
    }
}

@Preview
@Composable
private fun WheelTimePicker_DarkTheme_Preview() {
    Box(Modifier.background(Color(0xFF121212)).padding(16.dp)) {
        WheelTimePicker(
            initialHour = 21,
            initialMinute = 0,
            colors = WheelTimePickerDefaults.colors(
                selectedTextColor = Color(0xFF7BD88F),
                unselectedTextColor = Color(0xFF6B6B6B),
                labelColor = Color(0xFF7BD88F),
                separatorColor = Color(0xFF6B6B6B),
                backgroundColor = Color(0xFF2A2A2A),
                indicatorColor = Color(0x147BD88F),
            ),
            modifier = Modifier.width(320.dp),
        )
    }
}

@Preview
@Composable
private fun WheelTimePicker_FlatNoWheel_Preview() {
    Box(Modifier.background(PreviewBg).padding(16.dp)) {
        WheelTimePicker(
            initialHour = 4,
            initialMinute = 10,
            maxWheelRotationDegrees = 0f,
            modifier = Modifier.width(320.dp),
        )
    }
}

@Preview
@Composable
private fun WheelTimePicker_DeeperWheel_Preview() {
    Box(Modifier.background(PreviewBg).padding(16.dp)) {
        WheelTimePicker(
            initialHour = 4,
            initialMinute = 10,
            maxWheelRotationDegrees = 60f,
            visibleItemCount = 7,
            modifier = Modifier.width(320.dp),
        )
    }
}

@Preview
@Composable
private fun WheelTimePicker_WithoutLabels_Preview() {
    Box(Modifier.background(PreviewBg).padding(16.dp)) {
        WheelTimePicker(
            initialHour = 9,
            initialMinute = 30,
            showLabels = false,
            modifier = Modifier.width(320.dp),
        )
    }
}

@Preview
@Composable
private fun WheelTimePicker_HoursOnly_Preview() {
    Box(Modifier.background(PreviewBg).padding(16.dp)) {
        WheelTimePicker(
            initialHour = 12,
            showMinutes = false,
            modifier = Modifier.width(240.dp),
        )
    }
}

@Preview
@Composable
private fun WheelTimePicker_MinutesOnly_Preview() {
    Box(Modifier.background(PreviewBg).padding(16.dp)) {
        WheelTimePicker(
            initialMinute = 15,
            showHours = false,
            modifier = Modifier.width(240.dp),
        )
    }
}

@Preview
@Composable
private fun WheelTimePicker_12HourFormat_Preview() {
    Box(Modifier.background(PreviewBg).padding(16.dp)) {
        WheelTimePicker(
            initialHour = 5,
            initialMinute = 30,
            hourRange = 1..12,
            hourLabel = "hour",
            minuteLabel = "min",
            modifier = Modifier.width(320.dp),
        )
    }
}

@Preview
@Composable
private fun WheelTimePicker_NoIndicator_Preview() {
    Box(Modifier.background(PreviewBg).padding(16.dp)) {
        WheelTimePicker(
            initialHour = 8,
            initialMinute = 20,
            showIndicator = false,
            modifier = Modifier.width(320.dp),
        )
    }
}