package uz.tikoncha_parent.presentation.policy.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import tikoncha_parents.composeapp.generated.resources.Res
import tikoncha_parents.composeapp.generated.resources.bekor_qilish
import uz.tikoncha_parent.ui.theme.AppColors

/** Bitta matn maydonli dialog (jadval nomi) — Student bilan bir xil. */
@Composable
fun TextInputDialog(
    title: String,
    confirmText: String,
    placeholder: String = "",
    initial: String = "",
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    var value by remember { mutableStateOf(initial) }
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = AppColors.modal.primary,
        shape = RoundedCornerShape(24.dp),
        title = { Text(text = title, style = PolicyText.dialogTitle, color = AppColors.text.primary) },
        text = {
            OutlinedTextField(
                value = value,
                onValueChange = { value = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(placeholder, style = PolicyText.input, color = AppColors.text.placeholder) },
                textStyle = PolicyText.input,
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AppColors.border.accentEmphasis,
                    unfocusedBorderColor = AppColors.border.primary,
                    focusedTextColor = AppColors.text.primary,
                    unfocusedTextColor = AppColors.text.primary,
                    cursorColor = AppColors.action.primary,
                ),
            )
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(value) }, enabled = value.isNotBlank()) {
                Text(text = confirmText, color = AppColors.text.accentEmphasis, style = PolicyText.action)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(Res.string.bekor_qilish), color = AppColors.text.secondary, style = PolicyText.action)
            }
        },
    )
}
