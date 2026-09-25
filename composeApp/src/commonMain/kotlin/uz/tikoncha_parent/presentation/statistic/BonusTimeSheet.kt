@file:OptIn(ExperimentalMaterial3Api::class)

package uz.tikoncha_parent.presentation.statistic

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import tikoncha_parents.composeapp.generated.resources.Res
import tikoncha_parents.composeapp.generated.resources.minutes
import tikoncha_parents.composeapp.generated.resources.vaqtincha_ruxsat
import uz.tikoncha_parent.ui.DividerHorizontal
import uz.tikoncha_parent.ui.theme.AppColors
import uz.tikoncha_parent.ui.theme.AppTypography

/** Bonus vaqt variantlari, daqiqada (hujjat §5.7). */
private val BONUS_MINUTES = listOf(15, 30, 60)

/**
 * "Vaqtincha ruxsat" — ilovaga bir necha daqiqa ochiq vaqt berish.
 * ⚠️ Faqat [uz.tikoncha_parent.core.FeatureFlags.BONUS_TIME] = true bo'lganda ochiladi.
 */
@Composable
fun BonusTimeSheet(
    appName: String,
    onSelect: (minutes: Int) -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = AppColors.bg.surface,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 24.dp),
        ) {
            Text(
                text = stringResource(Res.string.vaqtincha_ruxsat),
                style = AppTypography.titleLgSemiBold,
                color = AppColors.text.primary,
                modifier = Modifier.align(Alignment.CenterHorizontally),
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = appName,
                style = AppTypography.bodyMdRegular,
                color = AppColors.text.secondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.align(Alignment.CenterHorizontally),
            )
            Spacer(Modifier.height(16.dp))

            BONUS_MINUTES.forEachIndexed { index, minutes ->
                if (index > 0) {
                    DividerHorizontal(
                        color = AppColors.border.secondarySubtle,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                Text(
                    text = pluralStringResource(Res.plurals.minutes, minutes, minutes),
                    style = AppTypography.bodyLgMedium,
                    color = AppColors.text.primary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelect(minutes) }
                        .padding(vertical = 16.dp),
                )
            }
        }
    }
}