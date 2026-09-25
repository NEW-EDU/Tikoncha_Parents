package uz.tikoncha_parent.presentation.policy.protection_packs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import tikoncha_parents.composeapp.generated.resources.Res
import tikoncha_parents.composeapp.generated.resources.boshqa_ota_ona_yoqqan
import tikoncha_parents.composeapp.generated.resources.farzand_ozi_yoqqan
import tikoncha_parents.composeapp.generated.resources.ilova_soni
import tikoncha_parents.composeapp.generated.resources.premium
import tikoncha_parents.composeapp.generated.resources.sayt_soni
import uz.tikoncha_parent.presentation.base.CustomSwitch
import uz.tikoncha_parent.presentation.base.simpleShadow
import uz.tikoncha_parent.ui.ContainerPadding
import uz.tikoncha_parent.ui.theme.AppColors
import uz.tikoncha_parent.ui.theme.AppTypography

@Composable
fun ProtectionPackItem(
    pack: ProtectionPackUi,
    showPremiumBadge: Boolean,
    onToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    // Plural va stringlar shartsiz olinadi, keyin tanlanadi — shartli @Composable chaqiruvdan qochish uchun.
    val appsText = pluralStringResource(Res.plurals.ilova_soni, pack.packageCount, pack.packageCount)
    val sitesText = pluralStringResource(Res.plurals.sayt_soni, pack.siteCount, pack.siteCount)
    val byCoParentText = stringResource(Res.string.boshqa_ota_ona_yoqqan)
    val byChildText = stringResource(Res.string.farzand_ozi_yoqqan)

    val summary = listOfNotNull(
        appsText.takeIf { pack.packageCount > 0 },
        sitesText.takeIf { pack.siteCount > 0 },
    ).joinToString(", ")

    val enabledByOthers = listOfNotNull(
        byCoParentText.takeIf { pack.enabledByCoParent },
        byChildText.takeIf { pack.enabledByChild },
    ).joinToString(" · ")

    Column(
        modifier = modifier
            .fillMaxWidth()
            .simpleShadow(shape = RoundedCornerShape(20.dp))
            .clip(RoundedCornerShape(20.dp))
            .background(AppColors.bg.surface)
            .padding(horizontal = ContainerPadding, vertical = 14.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = pack.title,
                style = AppTypography.titleLgSemiBold,
                color = AppColors.text.primary,
                modifier = Modifier.weight(1f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )

            if (showPremiumBadge) {
                Text(
                    text = stringResource(Res.string.premium),
                    style = AppTypography.bodySmMedium,
                    color = AppColors.text.accentWarning,
                    modifier = Modifier
                        .padding(end = 8.dp)
                        .background(AppColors.bg.accentWarningContainer, RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp),
                )
            }

            CustomSwitch(
                checked = pack.enabledByMe,
                enabled = !pack.inProgress,
                onCheckedChange = onToggle,
            )
        }

        pack.description?.takeIf { it.isNotBlank() }?.let { description ->
            Spacer(Modifier.height(6.dp))
            Text(
                text = description,
                style = AppTypography.bodyMdRegular,
                color = AppColors.text.secondary,
            )
        }

        if (summary.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = summary,
                style = AppTypography.bodySmMedium,
                color = AppColors.text.tertiary,
            )
        }

        if (enabledByOthers.isNotEmpty()) {
            Spacer(Modifier.height(6.dp))
            Text(
                text = enabledByOthers,
                style = AppTypography.bodySmMedium,
                color = AppColors.text.accentEmphasis,
            )
        }
    }
}