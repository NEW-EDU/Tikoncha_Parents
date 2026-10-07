package uz.tikoncha_parent.presentation.policy.components

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import tikoncha_parents.composeapp.generated.resources.Res
import tikoncha_parents.composeapp.generated.resources.bekor_qilish
import tikoncha_parents.composeapp.generated.resources.takeover_body_child
import tikoncha_parents.composeapp.generated.resources.takeover_body_coparent
import tikoncha_parents.composeapp.generated.resources.takeover_title
import tikoncha_parents.composeapp.generated.resources.yoqish
import uz.tikoncha_parent.presentation.policy.model.Ownership
import uz.tikoncha_parent.presentation.policy.model.PolicySummary
import uz.tikoncha_parent.presentation.policy.model.PresetKind
import uz.tikoncha_parent.ui.theme.AppColors

/**
 * Umumiy shablon: farzand yoki ikkinchi ota-ona yoqqan shablonni yoqishdan oldin.
 * Yoqsam — server uniki o'chiradi, mening sozlamam amal qiladi; keyin o'chirsam uniki o'chiq qoladi.
 */
@Composable
fun TakeOverDialog(
    kind: PresetKind,
    by: Ownership,
    otherSummary: PolicySummary,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    val setting = kind.summaryText(otherSummary)
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = AppColors.modal.primary,
        shape = RoundedCornerShape(24.dp),
        title = { Text(stringResource(Res.string.takeover_title, kind.title()), style = PolicyText.dialogTitle, color = AppColors.text.primary) },
        text = {
            Text(
                stringResource(if (by == Ownership.CHILD) Res.string.takeover_body_child else Res.string.takeover_body_coparent, setting),
                style = PolicyText.dialogText,
                color = AppColors.text.secondary,
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AppColors.action.primary, contentColor = AppColors.text.inverse),
            ) { Text(stringResource(Res.string.yoqish), style = PolicyText.action) }
        },
        dismissButton = {
            Button(
                onClick = onDismiss,
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AppColors.bg.tertiary, contentColor = AppColors.text.primary),
            ) { Text(stringResource(Res.string.bekor_qilish), style = PolicyText.action) }
        },
    )
}
