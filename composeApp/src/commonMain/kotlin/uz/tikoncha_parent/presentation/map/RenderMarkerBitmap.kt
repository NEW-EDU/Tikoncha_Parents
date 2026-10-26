package uz.tikoncha_parent.presentation.map

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import uz.tikoncha_parent.platform.Logger

object MarkerDimensions {
    const val WIDTH_DP = 74f
    const val HEIGHT_DP = 113f
    const val CIRCLE_DIAMETER_DP = 66f
    const val CIRCLE_TOP_INSET_DP = 4f
    const val BORDER_WIDTH_DP = 4f
    const val PIN_BOTTOM_DP = 85f
    const val PIN_HALF_WIDTH_DP = 10f
    const val INDICATOR_DIAMETER_DP = 24f
    const val INDICATOR_TOP_DP = 89f
    const val INDICATOR_INNER_DIAMETER_DP = 16f

    val CIRCLE_CENTER_Y_DP = CIRCLE_TOP_INSET_DP + CIRCLE_DIAMETER_DP / 2
    val INDICATOR_CENTER_Y_DP = INDICATOR_TOP_DP + INDICATOR_DIAMETER_DP / 2

    val ANCHOR_X = 0.5f
    val ANCHOR_Y = INDICATOR_CENTER_Y_DP / HEIGHT_DP
}

fun renderMarkerBitmap(
    style: MarkerStyle,
    density: Density,
    avatarBitmap: ImageBitmap? = null,
    textMeasurer: TextMeasurer? = null  // ← matn uchun
): ImageBitmap {


    if (style is MarkerStyle.Self) {
        return renderUserLocationBitmap(style, density)
    }
    if (style is MarkerStyle.Danger) {
        return renderDangerBitmap(style, density)
    }

    val scale = if (style.isSelected) 1.15f else 1.0f
    val widthPx = with(density) { (MarkerDimensions.WIDTH_DP * scale).dp.toPx() }.toInt()
    val heightPx = with(density) { (MarkerDimensions.HEIGHT_DP * scale).dp.toPx() }.toInt()

    val image = ImageBitmap(widthPx, heightPx)
    val canvas = Canvas(image)
    val drawScope = CanvasDrawScope()

    drawScope.draw(
        density = density,
        layoutDirection = LayoutDirection.Ltr,
        canvas = canvas,
        size = Size(widthPx.toFloat(), heightPx.toFloat())
    ) {
        val cx = widthPx / 2f
        val borderColor = Color(style.backgroundColor)
        val whiteColor = Color.White

        val circleRPx = with(density) { (MarkerDimensions.CIRCLE_DIAMETER_DP / 2 * scale).dp.toPx() }
        val borderWidthPx = with(density) { (MarkerDimensions.BORDER_WIDTH_DP * scale).dp.toPx() }
        val cy = with(density) { (MarkerDimensions.CIRCLE_CENTER_Y_DP * scale).dp.toPx() }

        // 1. Border ring
        drawCircle(
            color = borderColor,
            radius = circleRPx + borderWidthPx / 2,
            center = Offset(cx, cy),
            style = Stroke(width = borderWidthPx)
        )

        // 2. Oq fon
        drawCircle(
            color = whiteColor,
            radius = circleRPx,
            center = Offset(cx, cy)
        )

        // 3. Avatar yoki MATN
        if (style.showText && textMeasurer != null && style.text.isNotBlank()) {
            // ⬇️ MATN CHIZISH
            val fontSizeSp = (16f * scale).sp
            val annotatedText = buildAnnotatedString {
                withStyle(
                    SpanStyle(
                        color = borderColor,
                        fontSize = fontSizeSp,
                        fontWeight = FontWeight.Bold
                    )
                ) {
                    append(style.text)
                }
            }
            val textLayout = textMeasurer.measure(annotatedText)
            val textWidth = textLayout.size.width.toFloat()
            val textHeight = textLayout.size.height.toFloat()

            drawText(
                textLayoutResult = textLayout,
                topLeft = Offset(
                    x = cx - textWidth / 2,
                    y = cy - textHeight / 2
                )
            )
        } else if (avatarBitmap != null) {
            // ⬇️ AVATAR CHIZISH (avval)
            canvas.save()
            val clipPath = Path().apply {
                addOval(
                    Rect(
                        left = cx - circleRPx,
                        top = cy - circleRPx,
                        right = cx + circleRPx,
                        bottom = cy + circleRPx
                    )
                )
            }
            canvas.clipPath(clipPath)
            drawImage(
                image = avatarBitmap,
                srcOffset = IntOffset.Zero,
                srcSize = IntSize(avatarBitmap.width, avatarBitmap.height),
                dstOffset = IntOffset(
                    (cx - circleRPx).toInt(),
                    (cy - circleRPx).toInt()
                ),
                dstSize = IntSize(
                    (circleRPx * 2).toInt(),
                    (circleRPx * 2).toInt()
                )
            )
            canvas.restore()
        }

        // 4. PIN
        val pinTop = cy + circleRPx + borderWidthPx / 2
        val pinBottom = with(density) { (MarkerDimensions.PIN_BOTTOM_DP * scale).dp.toPx() }
        val pinHalfWidth = with(density) { (MarkerDimensions.PIN_HALF_WIDTH_DP * scale).dp.toPx() }

        val pinPath = Path().apply {
            moveTo(cx - pinHalfWidth, pinTop)
            lineTo(cx + pinHalfWidth, pinTop)
            lineTo(cx, pinBottom)
            close()
        }
        drawPath(pinPath, color = borderColor)

        // 5. INDIKATOR
        val indicatorOuterR = with(density) {
            (MarkerDimensions.INDICATOR_DIAMETER_DP / 2 * scale).dp.toPx()
        }
        val indicatorInnerR = with(density) {
            (MarkerDimensions.INDICATOR_INNER_DIAMETER_DP / 2 * scale).dp.toPx()
        }
        val indicatorCy = with(density) {
            (MarkerDimensions.INDICATOR_CENTER_Y_DP * scale).dp.toPx()
        }

        drawCircle(
            color = whiteColor,
            radius = indicatorOuterR,
            center = Offset(cx, indicatorCy)
        )
        drawCircle(
            color = borderColor,
            radius = indicatorInnerR,
            center = Offset(cx, indicatorCy)
        )
    }

    return image
}

private fun renderUserLocationBitmap(
    style: MarkerStyle.Self,
    density: Density,
): ImageBitmap {
    val sizeDp = 60f
    val sizePx = with(density) { sizeDp.dp.toPx() }.toInt()

    val image = ImageBitmap(sizePx, sizePx)
    val canvas = Canvas(image)
    val drawScope = CanvasDrawScope()

    drawScope.draw(
        density = density,
        layoutDirection = LayoutDirection.Ltr,
        canvas = canvas,
        size = Size(sizePx.toFloat(), sizePx.toFloat())
    ) {
        val cx = sizePx / 2f
        val dotR = with(density) { 14f.dp.toPx() }
        val borderPx = with(density) { 5f.dp.toPx() }
        val gap = with(density) { 4f.dp.toPx() }
        val dotCy = sizePx - dotR - gap

        val accentColor = Color(style.accentColor)
        val borderColor = Color(style.borderColor)

        // Outer white circle (border)
        drawCircle(
            color = borderColor,
            radius = dotR,
            center = Offset(cx, dotCy)
        )

        // Inner accent dot
        drawCircle(
            color = accentColor,
            radius = dotR - borderPx,
            center = Offset(cx, dotCy)
        )

        // Top arrow (qizil chevron)
        val arrowH = with(density) { 14f.dp.toPx() }
        val arrowW = with(density) { 12f.dp.toPx() }
        val arrowGap = with(density) { 2f.dp.toPx() }
        val arrowBottomY = dotCy - dotR - arrowGap
        val arrowTopY = arrowBottomY - arrowH

        val path = Path().apply {
            moveTo(cx, arrowTopY)
            lineTo(cx - arrowW / 2f, arrowBottomY)
            lineTo(cx + arrowW / 2f, arrowBottomY)
            close()
        }
        drawPath(path, color = accentColor)
    }

    return image
}

/** Xavfli joy belgisi: oq doira + ingichka [tint] halqa + bo'yalgan belgi; tanlanganda to'la doira, oq belgi. */
private fun renderDangerBitmap(
    style: MarkerStyle.Danger,
    density: Density,
): ImageBitmap {
    val diameterDp = if (style.isSelected) 46f else 34f
    val haloDp = 3f
    val sizePx = with(density) { (diameterDp + haloDp * 2).dp.toPx() }.toInt()

    val image = ImageBitmap(sizePx, sizePx)
    val canvas = Canvas(image)
    CanvasDrawScope().draw(
        density = density,
        layoutDirection = LayoutDirection.Ltr,
        canvas = canvas,
        size = Size(sizePx.toFloat(), sizePx.toFloat())
    ) {
        val center = Offset(sizePx / 2f, sizePx / 2f)
        val r = with(density) { (diameterDp / 2).dp.toPx() }
        val tint = Color(style.tint)
        val glyphColor: Color
        if (style.isSelected) {
            drawCircle(color = Color.White, radius = r + with(density) { haloDp.dp.toPx() }, center = center)
            drawCircle(color = tint, radius = r, center = center)
            glyphColor = Color.White
        } else {
            drawCircle(color = tint, radius = r, center = center)
            drawCircle(color = Color.White, radius = r - with(density) { 2.5f.dp.toPx() }, center = center)
            glyphColor = tint
        }
        style.glyph?.let { glyph ->
            val g = with(density) { (if (style.isSelected) 24f else 18f).dp.toPx() }.toInt()
            drawImage(
                image = glyph,
                srcOffset = IntOffset.Zero,
                srcSize = IntSize(glyph.width, glyph.height),
                dstOffset = IntOffset((center.x - g / 2f).toInt(), (center.y - g / 2f).toInt()),
                dstSize = IntSize(g, g),
                colorFilter = ColorFilter.tint(glyphColor),
                filterQuality = FilterQuality.High,
            )
        }
    }
    return image
}

suspend fun createMarkerIcon(
    style: MarkerStyle,
    density: Density,
    textMeasurer: TextMeasurer? = null
): NativeMarkerIcon {
    var avatar: ImageBitmap? = null

    if (!style.showText) {
        avatar = style.avatarUrl?.let { url ->
            val loaded = runCatching { loadImageBitmap(url) }.getOrNull()
            Logger.d("MARKER_DBG", "url=$url loaded=${loaded != null} size=${loaded?.width}x${loaded?.height}")
            loaded
        }
        if (avatar == null) {
            avatar = style.placeholderBitmap
            Logger.d("MARKER_DBG", "PLACEHOLDER size=${avatar?.width}x${avatar?.height}")
        }
    }

    val imageBitmap = renderMarkerBitmap(style, density, avatar, textMeasurer)
    Logger.d("MARKER_DBG", "rendered=${imageBitmap.width}x${imageBitmap.height}")
    return imageBitmap.toNativeMarkerIcon()
}