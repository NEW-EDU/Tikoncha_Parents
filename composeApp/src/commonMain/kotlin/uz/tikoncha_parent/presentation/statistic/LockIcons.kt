package uz.tikoncha_parent.presentation.statistic

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

/**
 * Material `Icons.Rounded.LockOpen` — aynan o'sha yo'l (material-icons-extended 1.7.3 dan).
 * Butun extended kutubxona iOS build'ini sezilarli sekinlashtirgani uchun faqat shu bitta
 * ikonka ko'chirildi. Yopiq qulf — core'dagi `Icons.Rounded.Lock`. O'zimiz chizgan
 * qulf ishlatilmasin (foydalanuvchi rad etgan).
 */
internal val LockOpenRounded: ImageVector by lazy {
    ImageVector.Builder(
        name = "Rounded.LockOpen",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).apply {
        path(fill = SolidColor(Color.Black)) {
            moveTo(12f, 13f)
            curveToRelative(-1.1f, 0f, -2f, 0.9f, -2f, 2f)
            reflectiveCurveToRelative(0.9f, 2f, 2f, 2f)
            reflectiveCurveToRelative(2f, -0.9f, 2f, -2f)
            reflectiveCurveToRelative(-0.9f, -2f, -2f, -2f)
            close()
            moveTo(18f, 8f)
            horizontalLineToRelative(-1f)
            lineTo(17f, 6f)
            curveToRelative(0f, -2.76f, -2.24f, -5f, -5f, -5f)
            curveToRelative(-2.28f, 0f, -4.27f, 1.54f, -4.84f, 3.75f)
            curveToRelative(-0.14f, 0.54f, 0.18f, 1.08f, 0.72f, 1.22f)
            curveToRelative(0.53f, 0.14f, 1.08f, -0.18f, 1.22f, -0.72f)
            curveTo(9.44f, 3.93f, 10.63f, 3f, 12f, 3f)
            curveToRelative(1.65f, 0f, 3f, 1.35f, 3f, 3f)
            verticalLineToRelative(2f)
            lineTo(6f, 8f)
            curveToRelative(-1.1f, 0f, -2f, 0.9f, -2f, 2f)
            verticalLineToRelative(10f)
            curveToRelative(0f, 1.1f, 0.9f, 2f, 2f, 2f)
            horizontalLineToRelative(12f)
            curveToRelative(1.1f, 0f, 2f, -0.9f, 2f, -2f)
            lineTo(20f, 10f)
            curveToRelative(0f, -1.1f, -0.9f, -2f, -2f, -2f)
            close()
            moveTo(18f, 19f)
            curveToRelative(0f, 0.55f, -0.45f, 1f, -1f, 1f)
            lineTo(7f, 20f)
            curveToRelative(-0.55f, 0f, -1f, -0.45f, -1f, -1f)
            verticalLineToRelative(-8f)
            curveToRelative(0f, -0.55f, 0.45f, -1f, 1f, -1f)
            horizontalLineToRelative(10f)
            curveToRelative(0.55f, 0f, 1f, 0.45f, 1f, 1f)
            verticalLineToRelative(8f)
            close()
        }
    }.build()
}
