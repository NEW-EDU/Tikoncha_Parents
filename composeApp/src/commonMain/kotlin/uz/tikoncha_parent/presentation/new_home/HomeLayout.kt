package uz.tikoncha_parent.presentation.new_home

import uz.tikoncha_parent.ui.tileBlob
import uz.tikoncha_parent.ui.BlobPlace
import uz.tikoncha_parent.ui.TileTint
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import uz.tikoncha_parent.data.local.AppSettings
import uz.tikoncha_parent.ui.SegmentGap
import uz.tikoncha_parent.ui.expressiveGridSegment
import uz.tikoncha_parent.ui.theme.AppColors
import uz.tikoncha_parent.ui.theme.AppTypography

/** Bosh sahifa ko'rinishi: katta bloklar ro'yxati yoki 2×3 panjara. "Mavzu" sahifasida tanlanadi. */
enum class HomeLayout { LIST, GRID }

object HomeLayoutController {
    private val _layout = MutableStateFlow(
        HomeLayout.entries.firstOrNull { it.name == AppSettings.homeLayoutName } ?: HomeLayout.LIST
    )
    val layout: StateFlow<HomeLayout> = _layout.asStateFlow()

    fun set(layout: HomeLayout) {
        if (_layout.value == layout) return
        AppSettings.homeLayoutName = layout.name
        _layout.value = layout
    }
}

/** Panjaradagi katakning o'rni (guruhning qaysi burchaklari katta bo'lishi shunga bog'liq). */
data class GridCell(val row: Int, val col: Int, val rows: Int = 3, val cols: Int = 2)

/** Panjaraning eng kichik balandligi (kichik ekranlarda — scroll). Katta ekranda panjara bo'sh joyni to'liq egallaydi. */
val GridMinHeight = 152.dp * 3 + SegmentGap * 2

/** Panjara qatori: ikki katak yonma-yon, orasida [SegmentGap]; balandlikni ota-ona beradi (weight). */
@Composable
fun HomeGridRow(modifier: Modifier = Modifier, content: @Composable RowScope.() -> Unit) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(SegmentGap),
        content = content,
    )
}

/**
 * Panjara katagi: tepada katta sarlavha va xira izoh; pastki o'ng burchakda KATTA 3D rasm — biroz qiyshaygan va
 * katak chegarasidan chiqib kesilgan (matn uning ustidan o'tadi). Rasmni [art] chizadi ([TileArt] yoki o'zinikini).
 */
@Composable
fun RowScope.HomeGridTile(
    cell: GridCell,
    title: String,
    onClick: () -> Unit,
    subtitle: @Composable ColumnScope.() -> Unit = {},
    /** Rasm ostidagi pastel doira (null — yo'q). */
    tint: TileTint? = null,
    blob: BlobPlace = BlobPlace.GridBottomEnd,
    art: @Composable BoxScope.() -> Unit,
) {
    val blobColor = tint?.color()
    Box(
        modifier = Modifier
            .weight(1f)
            .fillMaxHeight()
            .expressiveGridSegment(cell.row, cell.col, cell.rows, cell.cols, color = AppColors.section.tertiary, onClick = onClick)
            .then(if (blobColor != null) Modifier.tileBlob(blobColor, blob) else Modifier),
    ) {
        art()
        Column(Modifier.fillMaxWidth().padding(start = 16.dp, end = 14.dp, top = 16.dp)) {
            Text(
                text = title,
                style = AppTypography.headlineSmSemiBold.copy(fontWeight = FontWeight.ExtraBold),
                color = AppColors.text.primary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            subtitle()
        }
    }
}

/** Katakning katta rasmi: pastki o'ng burchakka yopishgan, qiyshaygan, chegaradan chiqib kesiladi. */
@Composable
fun BoxScope.TileArt(painter: Painter, size: Dp = 148.dp, rotation: Float = -10f) {
    Image(
        painter = painter,
        contentDescription = null,
        modifier = Modifier
            .align(Alignment.BottomEnd)
            .offset(x = size * 0.22f, y = size * 0.18f)
            .size(size)
            .graphicsLayer { rotationZ = rotation },
    )
}
/** Panjara katagining kichik, xira izohi. */
@Composable
fun HomeGridSubtitle(text: String, color: androidx.compose.ui.graphics.Color = AppColors.text.secondary) {
    Text(
        text = text,
        style = AppTypography.titleSmMedium,
        color = color,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier.padding(top = 2.dp),
    )
}
