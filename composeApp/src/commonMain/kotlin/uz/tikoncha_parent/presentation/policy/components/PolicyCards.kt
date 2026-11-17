package uz.tikoncha_parent.presentation.policy.components

import uz.tikoncha_parent.ui.expressiveSegment
import uz.tikoncha_parent.ui.segmentShape
import uz.tikoncha_parent.ui.BlobPlace
import uz.tikoncha_parent.ui.homeSubtitleColor
import uz.tikoncha_parent.ui.homeTileColor
import uz.tikoncha_parent.ui.tileBlob
import uz.tikoncha_parent.ui.tileBlobColor
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.DrawableResource
import uz.tikoncha_parent.ui.theme.AppColors
import uz.tikoncha_parent.ui.theme.AppTypography

/** Blok balandligi — Bosh sahifa bloklariga yaqin ([uz.tikoncha_parent.ui.HomeItemHeight] 106): 3D rasmga joy kerak. */
private val TileMinHeight = 100.dp

/** Rasm ortidagi pastel doira — Bosh sahifadagidek har blokda boşqa joyda (rasm bu yerda çapda). */
private val LeadBlobs = listOf(BlobPlace.RowLeadTop, BlobPlace.RowLeadBottom, BlobPlace.RowLeadEdge)

/**
 * Ro'yxat bloki (2026-11, Bosh sahifa bloklari kabi): 3D rasmda mayda detal köp — unga joy kerak.
 * Çapda katta 3D rasm pastel doira ustida · örtada katta qalin sarlavha + izoh · öngda switch, qulf yoki chevron
 * (vertikal markazda). Fon `homeTileColor()`, guruhlangan shakl (M3 Expressive). Katta matnda matn
 * qatorlarga bölinadi va blok böyiga ösadi (kesilmaydi).
 */
@Composable
fun PolicyCardBody(
    /** 3D rasm (`ic3d_rule_*`, `ic3d_sp_*`) — tintsiz, fonsiz çiziladi ([Policy3dIcon]). */
    icon: DrawableResource,
    /** [IconTone.GRAY] — öçiq: rasm xiraroq. */
    tone: IconTone,
    title: String,
    subtitle: String?,
    /** `null` — blokning özi bosilmaydi (faqat switch), masalan Maxsus himoya. */
    onClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
    /** Qo'shimcha eslatma (masalan "Faqat Qalqon rejimida ishlaydi"). */
    note: String? = null,
    /** M3 Expressive guruhlangan ro'yxatdagi o'rni (tashqi burchaklar katta, ichkilari kichik). */
    segmentIndex: Int = 0,
    segmentCount: Int = 1,
    trailing: @Composable () -> Unit,
) {
    val color = homeTileColor()
    val surface = if (onClick != null) {
        Modifier.expressiveSegment(segmentIndex, segmentCount, color = color, onClick = onClick)
    } else {
        val shape = segmentShape(segmentIndex, segmentCount)
        Modifier.clip(shape).background(color, shape)
    }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = TileMinHeight)
            .then(surface)
            .padding(start = 14.dp, end = 16.dp, top = 14.dp, bottom = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Policy3dIcon(icon = icon, dimmed = tone == IconTone.GRAY, size = Policy3dIconSize)
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = title,
                style = AppTypography.headlineSmSemiBold.copy(fontWeight = FontWeight.Bold),
                color = AppColors.text.primary,
                // Odam yozgan uzun nom — 3 qatordan oşsa qisqaradi; qolgan matn to'liq bölinadi
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
            )
            if (!subtitle.isNullOrBlank()) {
                Text(text = subtitle, style = AppTypography.titleSmMedium, color = homeSubtitleColor())
            }
            if (note != null) {
                Text(text = note, style = AppTypography.titleSmMedium, color = AppColors.text.accentWarning)
            }
        }
        trailing()
    }
}

/** Faqat ko'rish — boshqa odamniki (farzand, ikkinchi ota-ona, maktab). */
@Composable
fun LockIcon(modifier: Modifier = Modifier) {
    Icon(
        imageVector = Icons.Rounded.Lock,
        contentDescription = null,
        modifier = modifier.size(22.dp),
        tint = AppColors.icon.secondary,
    )
}

@Composable
fun SectionTitle(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = PolicyText.listSection,
        color = AppColors.text.primary,
        modifier = modifier.padding(start = 4.dp, top = 8.dp),
    )
}

