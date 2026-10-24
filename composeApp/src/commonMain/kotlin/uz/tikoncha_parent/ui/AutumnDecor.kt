package uz.tikoncha_parent.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import uz.tikoncha_parent.ui.theme.rememberIsDarkTheme

/*
 * "Kuz" bezagi — ko'zga yumshoq, iliq:
 *  • ekran foni — sezilar-sezilmas jigarrang/qaymoq gradient (tepasi ochroq);
 *  • bosh sahifa bloklarida 3D rasm ostida bitta rangdagi sokin doira ([tileBlobColor]).
 *    3D rasmlarning o'ziga tegilmaydi.
 */

/**
 * Rasm ostidagi doira rangi — barcha bloklarda BIR XIL (ota-ona ilovasi: sokin, bolalarcha rang-baranglik yo'q):
 * yorug' mavzuda — iliq qum tusi, qorong'ida — blokdan biroz ochroq jigarrang.
 */
@Composable
fun tileBlobColor(): Color = if (rememberIsDarkTheme()) Color(0xFF46382E) else Color(0xFFF4EADC)

/** Doira qayerda turadi: markazi blok o'lchamiga nisbatan (0..1, chetdan chiqishi mumkin), radiusi — kenglikka nisbatan. */
enum class BlobPlace(val cx: Float, val cy: Float, val r: Float) {
    /** Panjara katagi: pastki chap (rasm o'ngda — doira uning ortidan chiqib turadi). */
    GridBottomStart(0.22f, 1.02f, 0.62f),
    /** Panjara katagi: pastki o'ng, rasm ortida. */
    GridBottomEnd(0.98f, 0.96f, 0.58f),
    /** Panjara katagi: o'ng chekka, o'rtadan pastroq. */
    GridEnd(1.08f, 0.72f, 0.55f),
    /** Ro'yxat qatori: o'ngdagi rasm ortida. */
    RowEnd(0.86f, 0.78f, 0.16f),
}

/** Blok foni ustida (kesilgan shakl ichida) pastel doira — 3D rasmdan oldin chiziladi. */
fun Modifier.tileBlob(color: Color, place: BlobPlace): Modifier = drawBehind {
    val radius = size.width * place.r
    drawCircle(color = color, radius = radius, center = Offset(size.width * place.cx, size.height * place.cy))
}

/** Ekran foni: yuqoridan pastga sezilar-sezilmas iliq gradient. */
@Composable
fun Modifier.autumnBackground(): Modifier {
    val dark = rememberIsDarkTheme()
    val brush = if (dark) {
        Brush.verticalGradient(listOf(Color(0xFF2C231E), Color(0xFF241D18), Color(0xFF1D1713)))
    } else {
        Brush.verticalGradient(listOf(Color(0xFFF9F3EA), Color(0xFFF3EADE), Color(0xFFEDE0CE)))
    }
    return this.drawBehind { drawRect(brush) }
}
