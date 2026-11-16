package uz.tikoncha_parent.presentation.base

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import tikoncha_parents.composeapp.generated.resources.Res
import tikoncha_parents.composeapp.generated.resources.bekor_qilish
import tikoncha_parents.composeapp.generated.resources.hedgehog_heart
import tikoncha_parents.composeapp.generated.resources.plus_imkoniyatlarini_ochish
import tikoncha_parents.composeapp.generated.resources.sub_per_child_for
import tikoncha_parents.composeapp.generated.resources.sub_per_child_title
import tikoncha_parents.composeapp.generated.resources.tikoncha_orqali_ilovalar_vaqt_va_foydalanishni_boshqaring
import tikoncha_parents.composeapp.generated.resources.to_liq_nazoratni_yoqing
import uz.tikoncha_parent.data.local.AppSettings
import uz.tikoncha_parent.ui.Space
import uz.tikoncha_parent.ui.SpaceLarge
import uz.tikoncha_parent.ui.theme.AppColors
import uz.tikoncha_parent.ui.theme.AppTypography
import uz.tikoncha_parent.ui.theme.TikonchaParentTheme

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
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {


    if (show){

        val sheetState = rememberModalBottomSheetState(
            skipPartiallyExpanded = true // ✅ yarim ochiq bo‘lmaydi
        )
        LaunchedEffect(show) {
            if (show) sheetState.expand()
        }
        // To'lov ekrani ham aynan tanlangan farzand uchun ochiladi
        val forChild = childName ?: remember { AppSettings.selectedChild?.name?.takeIf { it.isNotBlank() } }

        ModalBottomSheet(
            sheetState = sheetState,
            onDismissRequest = onDismiss,
            containerColor = Color.Transparent,
            dragHandle = {}
        ) {

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(
                        start = 16.dp,
                        end = 16.dp
                    )
            ){
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = AppColors.bg.elevated
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                {

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {

                        Box(
                            modifier = Modifier
                                .padding(bottom = 15.dp)
                                .background(color = AppColors.border.secondarySubtle, shape = CircleShape)
                                .height(3.dp)
                                .width(36.dp),
                        )

                        Image(
                            painter = painterResource(Res.drawable.hedgehog_heart),
                            contentDescription = "",
                            modifier = Modifier
                                .height(120.dp)
                                .width(80.dp)
                        )
                        Space(10.dp)



                        Text(
                            text = title,
                            color = AppColors.text.primary,
                            style = AppTypography.titleLgSemiBold,
                            textAlign = TextAlign.Center,

                        )
                        Space(12.dp)

                        Text(
                            text = message,
                            color = AppColors.text.secondary,
                            style = AppTypography.emphasizedMdMedium,
                            modifier = Modifier
                                .fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                        Space(12.dp)

                        // Plus har bir farzand uchun alohida — to'lovga o'tishdan oldin, ko'zga ko'rinadigan joyda
                        Text(
                            text = if (forChild != null) stringResource(Res.string.sub_per_child_for, forChild)
                            else stringResource(Res.string.sub_per_child_title),
                            color = AppColors.text.primary,
                            style = AppTypography.titleSmSemiBold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(AppColors.bg.surface, RoundedCornerShape(12.dp))
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                        )
                        Space(16.dp)

                        CustomButtonNew(
                            text = confirmButtonText,
                            onClick = {
                                onConfirm()
                            },
                            modifier = Modifier
                                .fillMaxWidth(),
                        )
                        if (showCancelButton){
                            Space(8.dp)
                            CustomButtonNew(
                                text = dismissButtonText,
                                onClick = onDismiss,
                                containerColor = AppColors.action.section,
                                contentColor = AppColors.text.primary,
                                modifier = Modifier
                                    .fillMaxWidth()
                            )
                        }

                    }

                }
                SpaceLarge()
            }
        }
    }


}

@Preview(
    showBackground = true,
)
@Composable
private fun ddInAppUpdateDialogPreview() {
    TikonchaParentTheme {
        SubscriptionBottomDialog(
            show = true,
            onDismiss = {},
            onConfirm = {},
        )
    }
}
