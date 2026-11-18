package uz.tikoncha_parent.presentation.base

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import tikoncha_parents.composeapp.generated.resources.Res
import tikoncha_parents.composeapp.generated.resources.bekor_qilish
import tikoncha_parents.composeapp.generated.resources.ic3d_pr_subscription
import tikoncha_parents.composeapp.generated.resources.plus_imkoniyatlarini_ochish
import tikoncha_parents.composeapp.generated.resources.sub_per_child_for
import tikoncha_parents.composeapp.generated.resources.sub_per_child_title
import tikoncha_parents.composeapp.generated.resources.tikoncha_orqali_ilovalar_vaqt_va_foydalanishni_boshqaring
import tikoncha_parents.composeapp.generated.resources.to_liq_nazoratni_yoqing
import uz.tikoncha_parent.data.local.AppSettings
import uz.tikoncha_parent.ui.Space
import uz.tikoncha_parent.ui.theme.AppColors
import uz.tikoncha_parent.ui.theme.AppTypography
import uz.tikoncha_parent.ui.theme.TikonchaParentTheme

/**
 * Plus taklifi — oddiy pastki varaq: katta 3D belgi (atrofida joy bilan; xaritada — xarita belgisi, boşqa joyda —
 * toj), sarlavha, bir qator izoh, asosiy tugma, uning ostida kiçkina "faqat {farzand} uçun" va jim "Bekor qiliş".
 * Ilgari: qizil yurak uşlagan tipratikan (kiçik ölçamda qöröqinçli körinardi), soxta tutqiç va qutiga olingan yozuv.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubscriptionBottomDialog(
    show: Boolean,
    title: String = stringResource(Res.string.to_liq_nazoratni_yoqing),
    message: String = stringResource(Res.string.tikoncha_orqali_ilovalar_vaqt_va_foydalanishni_boshqaring),
    confirmButtonText: String = stringResource(Res.string.plus_imkoniyatlarini_ochish),
    dismissButtonText: String = stringResource(Res.string.bekor_qilish),
    showCancelButton: Boolean = true,
    /** Obuna kim uchun olinadi (Plus — har bir farzand uchun alohida); null — hozir tanlangan farzand. */
    childName: String? = null,
    /** Qaysi imkoniyat uçun (masalan, xarita) — 3D belgi; sukut — Plus toji. */
    icon: DrawableResource = Res.drawable.ic3d_pr_subscription,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    if (!show) return

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    LaunchedEffect(show) { if (show) sheetState.expand() }
    // To'lov ekrani ham aynan tanlangan farzand uchun ochiladi
    val forChild = childName ?: remember { AppSettings.selectedChild?.name?.takeIf { it.isNotBlank() } }

    ModalBottomSheet(
        sheetState = sheetState,
        onDismissRequest = onDismiss,
        containerColor = AppColors.bg.elevated,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        dragHandle = { BottomSheetDefaults.DragHandle(color = AppColors.border.secondarySubtle) },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(start = 24.dp, end = 24.dp, bottom = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Image(
                painter = painterResource(icon),
                contentDescription = null,
                modifier = Modifier.padding(top = 4.dp).size(112.dp),
            )
            Space(16.dp)
            Text(
                text = title,
                color = AppColors.text.primary,
                style = AppTypography.titleLgSemiBold,
                textAlign = TextAlign.Center,
            )
            Space(8.dp)
            Text(
                text = message,
                color = AppColors.text.secondary,
                style = AppTypography.bodyLgRegular,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
            Space(24.dp)
            CustomButtonNew(
                text = confirmButtonText,
                onClick = onConfirm,
                modifier = Modifier.fillMaxWidth(),
            )
            // Plus har bir farzand uchun alohida — to'lovga o'tishdan oldin, tugma ostida kiçkina
            Space(10.dp)
            Text(
                text = if (forChild != null) stringResource(Res.string.sub_per_child_for, forChild)
                else stringResource(Res.string.sub_per_child_title),
                color = AppColors.text.tertiary,
                style = AppTypography.bodyMdRegular,
                textAlign = TextAlign.Center,
            )
            if (showCancelButton) {
                Space(4.dp)
                TextButton(onClick = onDismiss) {
                    Text(dismissButtonText, style = AppTypography.titleSmMedium, color = AppColors.text.secondary)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SubscriptionBottomDialogPreview() {
    TikonchaParentTheme {
        SubscriptionBottomDialog(show = true, onDismiss = {}, onConfirm = {})
    }
}
