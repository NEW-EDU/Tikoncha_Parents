@file:OptIn(ExperimentalMaterial3Api::class)

package uz.tikoncha_parent.presentation.policy.policy_list

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import tikoncha_parents.composeapp.generated.resources.Res
import tikoncha_parents.composeapp.generated.resources.bir_soatga
import tikoncha_parents.composeapp.generated.resources.davom_ettirish
import tikoncha_parents.composeapp.generated.resources.dushanbagacha
import tikoncha_parents.composeapp.generated.resources.ertagacha
import tikoncha_parents.composeapp.generated.resources.uch_soatga
import tikoncha_parents.composeapp.generated.resources.vaqtincha_toxtatish
import uz.tikoncha_parent.ui.DividerHorizontal
import uz.tikoncha_parent.ui.theme.AppColors
import uz.tikoncha_parent.ui.theme.AppTypography

@Composable
fun PausePolicySheet(
    /** Jadval hozir to'xtatilgan bo'lsa tepada "Davom ettirish" chiqadi. */
    isPaused: Boolean,
    onSelect: (PauseOption) -> Unit,
    onResume: () -> Unit,
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
                text = stringResource(Res.string.vaqtincha_toxtatish),
                style = AppTypography.titleLgSemiBold,
                color = AppColors.text.primary,
                modifier = Modifier.align(Alignment.CenterHorizontally),
            )
            Spacer(Modifier.height(16.dp))

            if (isPaused) {
                SheetRow(
                    text = stringResource(Res.string.davom_ettirish),
                    color = AppColors.text.accentEmphasis,
                    onClick = onResume,
                )
                DividerHorizontal(
                    color = AppColors.border.secondarySubtle,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            PauseOption.entries.forEachIndexed { index, option ->
                SheetRow(text = option.label(), onClick = { onSelect(option) })
                if (index < PauseOption.entries.lastIndex) {
                    DividerHorizontal(
                        color = AppColors.border.secondarySubtle,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }
}

@Composable
private fun PauseOption.label(): String = when (this) {
    PauseOption.ONE_HOUR -> stringResource(Res.string.bir_soatga)
    PauseOption.THREE_HOURS -> stringResource(Res.string.uch_soatga)
    PauseOption.UNTIL_TOMORROW -> stringResource(Res.string.ertagacha)
    PauseOption.UNTIL_MONDAY -> stringResource(Res.string.dushanbagacha)
}

@Composable
private fun SheetRow(
    text: String,
    color: Color = AppColors.text.primary,
    onClick: () -> Unit,
) {
    Text(
        text = text,
        style = AppTypography.bodyLgMedium,
        color = color,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 16.dp),
    )
}