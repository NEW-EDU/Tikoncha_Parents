package uz.tikoncha_parent.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import uz.tikoncha_parent.ui.theme.AppColors
import uz.tikoncha_parent.ui.theme.rememberIsDarkTheme

/*
 * "Kuz" bezagi (foydalanuvchi bergan namunadan o'lchab olingan, qorong'i mavzu):
 *  • fon: tepada (status bar + salomlashuv) shokolad #2B211A, bloklar boshlanishida to'q #211912 ga o'tadi;
 *  • bloklar #42362E, ularning ostidagi oddiy doiralar — bejroq #574436, har blokda boshqa joyda;
 *  • 3D rasmlarning o'ziga tegilmaydi.
 */

/** Bosh sahifa bloki rangi. */
@Composable
fun homeTileColor(): Color = if (rememberIsDarkTheme()) Color(0xFF42362E) else AppColors.section.tertiary

/** Rasm ostidagi doira — barcha bloklarda bir xil, sokin bej tus. */
@Composable
fun tileBlobColor(): Color = if (rememberIsDarkTheme()) Color(0xFF574436) else Color(0xFFF2E5D3)

/** Bosh sahifadagi kichik doira tugma (video) foni. */
@Composable
fun homeChipColor(): Color = if (rememberIsDarkTheme()) Color(0xFF49392D) else AppColors.bg.surfaceTertiary

/** Salomlashuvdagi ism rangi (qorong'ida — och shaftoli, yorug'da — karamel urg'u). */
@Composable
fun homeNameAccent(): Color = if (rememberIsDarkTheme()) Color(0xFFF7D3AE) else AppColors.text.accentEmphasis

/** Statistika ustunlari (qisqa → baland): qorong'ida karamel tuslar, yorug'da — urg'u rangining shaffof tuslari. */
@Composable
fun homeBarColors(): List<Color> = if (rememberIsDarkTheme()) {
    listOf(Color(0xFF836248), Color(0xFF976947), Color(0xFFB48861))
} else {
    listOf(0.10f, 0.16f, 0.22f).map { AppColors.action.primary.copy(alpha = it) }
}

/**
 * Doira joyi: markazi blok o'lchamiga nisbatan (cx — kenglik, cy — balandlik; chetdan chiqishi mumkin),
 * radiusi — kenglikka nisbatan. Panjara qiymatlari namunadagi doiralarga moslab o'lchangan — har blokda boshqacha.
 */
enum class BlobPlace(val cx: Float, val cy: Float, val r: Float) {
    GridChat(0.09f, 0.94f, 0.46f),
    GridTasks(1.14f, 1.16f, 0.98f),
    GridMap(0.83f, 1.05f, 0.63f),
    GridProtection(0.99f, 0.90f, 0.63f),
    GridRestrictions(-0.10f, 0.99f, 0.61f),

    // Ro'yxat qatorlari (keng bloklar) — rasm ortida, lekin har birida boshqa joyda
    RowTasks(0.80f, 1.15f, 0.20f),
    RowChat(0.93f, 0.30f, 0.16f),
    RowProtection(0.77f, 0.95f, 0.17f),
    RowMap(0.90f, 1.22f, 0.22f),
    RowRestrictions(0.99f, 0.62f, 0.19f),
}

/** Blok foni ustida (kesilgan shakl ichida) doira — 3D rasmdan oldin chiziladi. */
fun Modifier.tileBlob(color: Color, place: BlobPlace): Modifier = drawBehind {
    drawCircle(
        color = color,
        radius = size.width * place.r,
        center = Offset(size.width * place.cx, size.height * place.cy),
    )
}

/** Bosh sahifa foni: tepasi shokolad, bloklar ortida — to'q (namunadagidek). */
@Composable
fun Modifier.autumnBackground(): Modifier {
    val brush = if (rememberIsDarkTheme()) {
        Brush.verticalGradient(
            0f to Color(0xFF2B211A),
            0.12f to Color(0xFF2C221A),
            0.175f to Color(0xFF231A14),
            0.21f to Color(0xFF211912),
            1f to Color(0xFF201812),
        )
    } else {
        Brush.verticalGradient(
            0f to Color(0xFFF8F1E7),
            0.12f to Color(0xFFF6EEE3),
            0.21f to Color(0xFFF0E5D7),
            1f to Color(0xFFEDE1D1),
        )
    }
    return this.drawBehind { drawRect(brush) }
}
