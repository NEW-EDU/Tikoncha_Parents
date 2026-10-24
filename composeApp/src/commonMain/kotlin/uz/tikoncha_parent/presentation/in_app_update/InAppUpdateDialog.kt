package uz.tikoncha_parent.presentation.in_app_update

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import tikoncha_parents.composeapp.generated.resources.Res
import tikoncha_parents.composeapp.generated.resources.app_update_desc
import tikoncha_parents.composeapp.generated.resources.hozir_emas
import tikoncha_parents.composeapp.generated.resources.task_notification
import tikoncha_parents.composeapp.generated.resources.yangilanish_mavjud
import tikoncha_parents.composeapp.generated.resources.yangilash
import uz.tikoncha_parent.domain.model.in_app_update.UpdateType
import uz.tikoncha_parent.presentation.base.CustomButton
import uz.tikoncha_parent.ui.CardCornerRadius
import uz.tikoncha_parent.ui.ContainerPadding
import uz.tikoncha_parent.ui.SpaceLarge
import uz.tikoncha_parent.ui.SpaceMedium
import uz.tikoncha_parent.ui.SpaceSmall
import uz.tikoncha_parent.ui.theme.AppColors
import uz.tikoncha_parent.ui.theme.AppTypography
import uz.tikoncha_parent.ui.theme.TikonchaParentTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InAppUpdateDialog(
    show: Boolean,
    state: UpdateUiState,
    onDismiss: () -> Unit,
    onConfirm: (UpdateType) -> Unit
) {
    if (!state.showUpdateDialog || state.recommendedType == null) return



    if (show){

        val sheetState = rememberModalBottomSheetState(
            skipPartiallyExpanded = true // ✅ yarim ochiq bo‘lmaydi
        )
        LaunchedEffect(show) {
            if (show) sheetState.expand()
        }



        val type = state.recommendedType
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
                        start = ContainerPadding,
                        end = ContainerPadding
                    )
            ){
                Card(
                    shape = RoundedCornerShape(CardCornerRadius),
                    colors = CardDefaults.cardColors(
                        containerColor = AppColors.bg.elevated
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                {

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(ContainerPadding),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Image(
                            painter = painterResource(Res.drawable.task_notification),
                            contentDescription = null,
                            modifier = Modifier
                                .size(120.dp)
                        )
                        SpaceMedium()
                        Text(
                            text = stringResource(Res.string.yangilanish_mavjud),
                            color = AppColors.text.primary,
                            style = AppTypography.titleLgSemiBold

                        )
                        SpaceSmall()

                        Text(
                            text = stringResource(Res.string.app_update_desc),
                            color = AppColors.text.secondary,
                            style = AppTypography.emphasizedSmMedium,
                            modifier = Modifier
                                .fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                        SpaceMedium()

                        CustomButton(
                            modifier = Modifier.fillMaxWidth(),
                            text = stringResource(Res.string.yangilash),
                            onClick = {
                                onConfirm(type)
                            },
                        )
                        SpaceSmall()


                        CustomButton(
                            onClick = onDismiss,
                            color = AppColors.action.section,
                            modifier = Modifier.fillMaxWidth(),
                            textColor = AppColors.text.primary,
                            text = stringResource(Res.string.hozir_emas),
                        )
                    }
                }
                SpaceLarge()
            }
        }
    }
}

@Preview
@Composable
fun InAppUpdateDialogPreview() {
    TikonchaParentTheme {
        InAppUpdateDialog(
            show = true,
            state = UpdateUiState(
                showUpdateDialog = true,
                recommendedType = UpdateType.FLEXIBLE
            ),
            onDismiss = {},
            onConfirm = {}
        )
    }
}
