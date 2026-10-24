package uz.tikoncha_parent.presentation.in_app_update

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import tikoncha_parents.composeapp.generated.resources.Res
import tikoncha_parents.composeapp.generated.resources.app_update_desc
import tikoncha_parents.composeapp.generated.resources.task_notification
import tikoncha_parents.composeapp.generated.resources.yangilanish_mavjud
import tikoncha_parents.composeapp.generated.resources.yangilash
import uz.tikoncha_parent.domain.model.in_app_update.UpdateType
import uz.tikoncha_parent.presentation.base.CustomButton
import uz.tikoncha_parent.presentation.base.verticalShadow
import uz.tikoncha_parent.ui.ContainerPadding
import uz.tikoncha_parent.ui.SpaceSmall
import uz.tikoncha_parent.ui.theme.AppColors
import uz.tikoncha_parent.ui.theme.AppTypography
import uz.tikoncha_parent.ui.theme.ThemeMode
import uz.tikoncha_parent.ui.theme.TikonchaParentTheme

@Composable
fun InAppUpdateCard(
    modifier: Modifier = Modifier,
    state: UpdateUiState,
    event: (UpdateEvent) -> Unit
) {
    val show = state.showCard && !state.isDownloading

    if (show){
        Card(
            modifier = modifier
                .fillMaxWidth()
                .verticalShadow(shape = RoundedCornerShape(20.dp), darkColor = AppColors.bg.primary),
            colors = CardDefaults.cardColors(
                containerColor = AppColors.bg.surface
            ),
            shape = RoundedCornerShape(20.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(ContainerPadding),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Image(
                    painter = painterResource(Res.drawable.task_notification),
                    contentDescription = null,
                    modifier = Modifier
                        .size(96.dp)
                )
                SpaceSmall()

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    Text(
                        text = stringResource(Res.string.yangilanish_mavjud),
                        color = AppColors.text.primary,
                        style = AppTypography.titleSmSemiBold
                    )
                    SpaceSmall()
                    Text(
                        text = stringResource(Res.string.app_update_desc),
                        color = AppColors.text.secondary,
                        style = AppTypography.emphasizedXsMedium
                    )

                    SpaceSmall()

                    CustomButton(
                        style = AppTypography.bodyMdMedium,
                        text = stringResource(Res.string.yangilash),
                        onClick = {
                            event(
                                UpdateEvent.StartUpdateClicked(
                                    state.recommendedType?: UpdateType.FLEXIBLE
                                )
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(36.dp),
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun InAppUpdateCardPreview() {
    TikonchaParentTheme(
        ThemeMode.LIGHT
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            InAppUpdateCard(
                state = UpdateUiState(
                    showCard = true
                ),
                event = {}
            )
        }
    }
}