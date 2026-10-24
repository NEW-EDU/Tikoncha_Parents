package uz.tikoncha_parent.presentation.base

import androidx.compose.material3.Icon
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.runtime.remember
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import uz.tikoncha_parent.ui.*
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import tikoncha_parents.composeapp.generated.resources.Res
import tikoncha_parents.composeapp.generated.resources.*
import uz.tikoncha_parent.App
import uz.tikoncha_parent.ui.theme.AppColors
import uz.tikoncha_parent.ui.theme.AppTypography
import uz.tikoncha_parent.ui.theme.ThemeMode
import uz.tikoncha_parent.ui.theme.TikonchaParentTheme
import uz.tikoncha_parent.ui.theme.extendedColor

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun CustomDialog(
    modifier: Modifier = Modifier,
    title: String,
    message: String,
    show: Boolean = true,
    onDismiss: () -> Unit,
    buttonText: String = "Ok",
    onButtonClick: () -> Unit,
    showCloseButton: Boolean = false,
    painter: Painter = painterResource(Res.drawable.dialog_info),
    buttonText2: String = stringResource(Res.string.bekor_qilish),
    /** Asosiy tugma ikonasi (masalan, "Farzand qo'shish" uchun +). */
    buttonIcon: Painter? = null,
) {

    val dialogIcon = if (message == stringResource(Res.string.iltimos_internetga_ulang)) {
        painterResource(Res.drawable.dialog_internet)
    } else {
        painter
    }

    val dialogTitle = if (message == stringResource(Res.string.iltimos_internetga_ulang)) {
        stringResource(Res.string.aloqa_uzildi)
    } else {
        title
    }

    val dialogMessage = if (message == stringResource(Res.string.iltimos_internetga_ulang)) {
        stringResource(Res.string.internet_aloqa_uzildi)
    } else {
        message
    }

    if (show) {
        Dialog(
            onDismissRequest = onDismiss
        ) {
            Card(
                modifier = modifier
                    .fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = AppColors.bg.surface
                ),
                // M3 Expressive: dialoglar — extra-large (28 dp) burchak
                shape = MaterialTheme.shapes.extraLarge
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    SpaceLarge()
                    // Ikonka prujina bilan "otilib" chiqadi (MotionScheme.defaultSpatial)
                    val pop = remember { Animatable(0.6f) }
                    val spatial = MaterialTheme.motionScheme.defaultSpatialSpec<Float>()
                    LaunchedEffect(Unit) { pop.animateTo(1f, spatial) }
                    Image(
                        painter = dialogIcon,
                        contentDescription = "",
                        modifier = Modifier
                            .size(64.dp)
                            .graphicsLayer { scaleX = pop.value; scaleY = pop.value }
                            .align(Alignment.CenterHorizontally)
                    )
                    Space(16.dp)
                    Text(
                        text = dialogTitle,
                        color = AppColors.text.primary,
                        style = AppTypography.titleLgSemiBold.copy(fontWeight = FontWeight.ExtraBold),
                        modifier = Modifier
                            .fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )

                    Space(8.dp)

                    Text(
                        text = dialogMessage,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        color = AppColors.text.secondary,
                        style = AppTypography.emphasizedMdMedium
                    )

                    Space(16.dp)

                    CustomButtonNew(
                        text = buttonText,
                        onClick = {
                            onButtonClick()
                        },
                        modifier = Modifier
                            .fillMaxWidth(),
                        containerColor = AppColors.button.primary,
                        leadingIcon = buttonIcon?.let { icon ->
                            { Icon(icon, contentDescription = null, tint = AppColors.text.inverse, modifier = Modifier.size(18.dp)) }
                        }
                    )
                    if (showCloseButton){
                        Space(8.dp)
                        CustomButtonNew(
                            text = buttonText2,
                            onClick = onDismiss,
                            modifier = Modifier
                                .fillMaxWidth(),
                            containerColor = AppColors.section.section,
                            contentColor = AppColors.text.primary
                        )
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun Preview() {
    TikonchaParentTheme(
        ThemeMode.DARK
    ){
        CustomDialog(
            show = true,
            onDismiss = {},
            title = "Titel",
            buttonText = "Ok",
            onButtonClick = {},
            message = "Message",
            showCloseButton = false,
        )
    }
}