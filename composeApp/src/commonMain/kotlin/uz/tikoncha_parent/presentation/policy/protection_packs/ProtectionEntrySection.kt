package uz.tikoncha_parent.presentation.policy.protection_packs

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import tikoncha_parents.composeapp.generated.resources.Res
import tikoncha_parents.composeapp.generated.resources.arrow_right
import tikoncha_parents.composeapp.generated.resources.guard
import tikoncha_parents.composeapp.generated.resources.himoya
import tikoncha_parents.composeapp.generated.resources.himoya_paketlari
import uz.tikoncha_parent.presentation.base.simpleShadow
import uz.tikoncha_parent.presentation.base.singleClick
import uz.tikoncha_parent.ui.NormalIconButtonPadding
import uz.tikoncha_parent.ui.theme.AppColors
import uz.tikoncha_parent.ui.theme.AppTypography

@Composable
fun ProtectionEntrySection(
    enabledNames: List<String>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = stringResource(Res.string.himoya),
            style = AppTypography.titleLgSemiBold,
            color = AppColors.text.primary,
        )
        Spacer(Modifier.height(12.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .simpleShadow(shape = RoundedCornerShape(20.dp))
                .clip(RoundedCornerShape(20.dp))
                .background(AppColors.bg.surface)
                .singleClick { onClick() }
                .padding(horizontal = 16.dp, vertical = 14.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(AppColors.bg.surfaceTertiary, RoundedCornerShape(NormalIconButtonPadding)),
                contentAlignment = Alignment.Center,
            ) {
                // guard.png rangli illyustratsiya — tint berilmaydi.
                Image(
                    painter = painterResource(Res.drawable.guard),
                    contentDescription = null,
                    modifier = Modifier.size(28.dp),
                )
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(Res.string.himoya_paketlari),
                    style = AppTypography.titleSmMedium,
                    color = AppColors.text.primary,
                )
                if (enabledNames.isNotEmpty()) {
                    Text(
                        text = enabledNames.joinToString(", "),
                        style = AppTypography.bodySmMedium,
                        color = AppColors.text.accentEmphasis,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            Icon(
                painter = painterResource(Res.drawable.arrow_right),
                contentDescription = null,
                tint = AppColors.icon.tertiary,
                modifier = Modifier.size(16.dp),
            )
        }
    }
}