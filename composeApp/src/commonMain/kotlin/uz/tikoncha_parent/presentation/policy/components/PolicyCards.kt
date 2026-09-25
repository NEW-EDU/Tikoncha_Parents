package uz.tikoncha_parent.presentation.policy.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.DrawableResource
import uz.tikoncha_parent.presentation.base.singleClick
import uz.tikoncha_parent.ui.theme.AppColors

private val CardShape = RoundedCornerShape(24.dp)

/**
 * Ro'yxat kartasi (Student `CardBody`): katta ikonka · sarlavha + ikkinchi qator · o'ngda
 * switch yoki qulf. bg.section, 24dp burchak, 16dp ichki masofa.
 */
@Composable
fun PolicyCardBody(
    icon: DrawableResource,
    tone: IconTone,
    title: String,
    subtitle: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    /** Qo'shimcha eslatma (masalan "Faqat Qalqon rejimida ishlaydi"). */
    note: String? = null,
    trailing: @Composable () -> Unit,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(CardShape)
            .background(AppColors.bg.section)
            .singleClick(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        PolicyIcon(icon = icon, tone = tone)
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = title,
                style = PolicyText.cardTitle,
                color = AppColors.text.primary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (!subtitle.isNullOrBlank()) {
                Text(
                    text = subtitle,
                    style = PolicyText.subtitle,
                    color = AppColors.text.tertiary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            if (note != null) {
                Text(text = note, style = PolicyText.subtitle, color = AppColors.text.accentWarning, maxLines = 1, overflow = TextOverflow.Ellipsis)
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

@Composable
fun EmptyHint(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = PolicyText.empty,
        color = AppColors.text.tertiary,
        modifier = modifier.fillMaxWidth().padding(vertical = 40.dp),
    )
}
