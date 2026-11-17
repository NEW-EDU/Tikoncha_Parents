package uz.tikoncha_parent.presentation.profile

import androidx.compose.ui.draw.drawBehind
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.Icons
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.heightIn
import uz.tikoncha_parent.ui.expressiveSegment
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import tikoncha_parents.composeapp.generated.resources.Res
import tikoncha_parents.composeapp.generated.resources.ic3d_pr_language
import tikoncha_parents.composeapp.generated.resources.ic3d_pr_logout
import tikoncha_parents.composeapp.generated.resources.ic3d_pr_delete_account
import tikoncha_parents.composeapp.generated.resources.ic3d_pr_screen_colour
import tikoncha_parents.composeapp.generated.resources.ic3d_pr_text_size
import tikoncha_parents.composeapp.generated.resources.profile
import uz.tikoncha_parent.presentation.base.singleClick
import uz.tikoncha_parent.ui.BlobPlace
import uz.tikoncha_parent.ui.SegmentGap
import uz.tikoncha_parent.ui.SmallIconSize
import uz.tikoncha_parent.ui.expressiveGridSegment
import uz.tikoncha_parent.ui.homeTileColor
import uz.tikoncha_parent.ui.tileBlob
import uz.tikoncha_parent.ui.tileBlobColor
import uz.tikoncha_parent.ui.theme.AppColors
import uz.tikoncha_parent.ui.theme.AppTypography
import uz.tikoncha_parent.ui.theme.ThemeMode
import uz.tikoncha_parent.ui.theme.TikonchaParentTheme

/**
 * Profil menyusi qatori — M3 Expressive guruhlangan ro'yxat elementi: guruhning tashqi burchaklari katta, ichkilari
 * kichik, elementlar orasida ingichka bo'shliq; chapda tonal doiradagi ikonka, o'ngda strelka. [divider] eskirgan —
 * guruh uslubida chiziq kerak emas (imzo mos kelishi uchun qoldirilgan).
 * Faqat tekis (bir rangli) ikonkalar uchun (Tangalar, Qöllanma kliplari, Sozlamalar). 3D rasmlar (`ic3d_*`) kiçik
 * qatorga sig'maydi — ular uçun [ProfileTileGroup].
 */
@Composable
fun ProfileSectionItem(
    icon: Painter,
    title: String,
    onItemClick: () -> Unit,
    divider: Boolean = true,
    textColor: Color = AppColors.text.primary,
    iconColor: Color = AppColors.icon.accentPrimary,
    segmentIndex: Int = 0,
    segmentCount: Int = 1,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 64.dp)
            .expressiveSegment(segmentIndex, segmentCount, color = AppColors.bg.surface) { onItemClick() }
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(iconColor.copy(alpha = 0.14f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(Modifier.width(14.dp))
        Text(
            text = title,
            color = textColor,
            style = AppTypography.titleMdSemiBold,
            modifier = Modifier.weight(1f),
        )
        Icon(
            imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
            contentDescription = null,
            tint = AppColors.icon.secondary,
            modifier = Modifier.size(22.dp),
        )
    }
}

/** Profil menyusidagi guruh sarlavhasi (kichik, xira — e'tiborni elementlarga qoldiradi). */
@Composable
fun ProfileSectionLabel(text: String) {
    Text(
        text = text,
        style = AppTypography.titleSmMedium,
        color = AppColors.text.tertiary,
        modifier = Modifier.padding(start = 16.dp, top = 20.dp, bottom = 8.dp),
    )
}

/** Profil qatoridagi 3D rasm — bosh sahifa qatorlaridagi kabi joy beriladi (u yerda 76dp). */
val ProfileTileIconSize = 60.dp

/** Profil bölimidagi bitta qator: sarlavha, 3D rasm (`ic3d_pr_*`) va bosilganda nima bo'lishi. */
class ProfileTile(
    val title: String,
    val icon: Painter,
    val onClick: () -> Unit,
)

/**
 * Profil bölimi — bosh sahifadagi qatorlar uslubida: har biri butun kenglikdagi baland qator, sarlavha çapda,
 * 3D rasm öngda — markazida turgan sokin doira ustida. Bölim bitta yaxlit blok (faqat tashqi burçaklar katta).
 * Hamma qatorlar bir xil: bir xil balandlik, şrift va rasm ölçami (aralaş panjara yöq).
 * [danger] — Çiqiş / Akkauntni öçiriş: oddiy qator, faqat sarlavha qizil (katta qizil blok qörqitadi).
 */
@Composable
fun ProfileTileGroup(
    tiles: List<ProfileTile>,
    modifier: Modifier = Modifier,
    danger: Boolean = false,
) {
    if (tiles.isEmpty()) return
    val tileColor = homeTileColor()
    val blobColor = tileBlobColor()
    val titleColor = if (danger) AppColors.text.accentDanger else AppColors.text.primary
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(SegmentGap)) {
        tiles.forEachIndexed { i, tile ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 88.dp)
                    .expressiveSegment(i, tiles.size, color = tileColor, onClick = tile.onClick)
                    .padding(start = 20.dp, end = 14.dp, top = 10.dp, bottom = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = tile.title,
                    style = AppTypography.titleLgSemiBold,
                    color = titleColor,
                    modifier = Modifier.weight(1f),
                )
                Spacer(Modifier.width(12.dp))
                Image(
                    painter = tile.icon,
                    contentDescription = null,
                    modifier = Modifier.size(ProfileTileIconSize),
                )
            }
        }
    }
}

@Preview
@Composable
private fun Pre() {
    TikonchaParentTheme(
        ThemeMode.DARK
    ) {
        ProfileSectionItem(
            icon = painterResource(Res.drawable.profile),
            title = "Shaxsiy",
            onItemClick = {}
        )
    }
}

@Preview
@Composable
private fun PreTiles() {
    TikonchaParentTheme(
        ThemeMode.LIGHT
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
            ProfileTileGroup(
                tiles = listOf(
                    ProfileTile("Ekran rangi", painterResource(Res.drawable.ic3d_pr_screen_colour)) {},
                    ProfileTile("Matn ölçami", painterResource(Res.drawable.ic3d_pr_text_size)) {},
                    ProfileTile("Til", painterResource(Res.drawable.ic3d_pr_language)) {},
                ),
            )
            ProfileTileGroup(
                tiles = listOf(
                    ProfileTile("Çiqiş", painterResource(Res.drawable.ic3d_pr_logout)) {},
                    ProfileTile("Akkauntni öçiriş", painterResource(Res.drawable.ic3d_pr_delete_account)) {},
                ),
                danger = true,
            )
        }
    }
}
