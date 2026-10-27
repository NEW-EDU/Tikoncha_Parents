package uz.tikoncha_parent.presentation.base

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import tikoncha_parents.composeapp.generated.resources.Res
import tikoncha_parents.composeapp.generated.resources.close_circle
import uz.tikoncha_parent.ui.CardCornerRadius
import uz.tikoncha_parent.ui.ContainerPadding
import uz.tikoncha_parent.ui.LargeTextSize
import uz.tikoncha_parent.ui.NormalIconButtonPadding
import uz.tikoncha_parent.ui.NormalIconButtonSize
import uz.tikoncha_parent.ui.SpaceLarge
import uz.tikoncha_parent.ui.SpaceMedium
import uz.tikoncha_parent.ui.TextFieldHeight
import uz.tikoncha_parent.ui.theme.extendedColor

@Composable
fun CustomDialogTextField(
    modifier: Modifier = Modifier,
    show: Boolean = false,
    title: String = "",
    /** Sarlavha ostidagi izoh (ixtiyoriy) — nega söralayotganini xotirjam tushuntirish uçun. */
    message: String? = null,
    /** false — tugma faqat [onButtonClick]ni chaqiradi ([onDismiss] = faqat yopiş, masalan "keyinroq"). */
    dismissOnButton: Boolean = true,
    buttonText: String = "Ok",
    showCloseButton: Boolean = true,
    onDismiss: () -> Unit,
    onButtonClick: () -> Unit,
    enabled: Boolean = true,

    value: String,
    onValueChange: (String) -> Unit,
    label: String = "",
    isReadOnly: Boolean = false,
    isSingleLine: Boolean = true,

    ) {

    if (!show) {
        return
    }

    Dialog(
        onDismissRequest = onDismiss
    ) {
        Card(
            modifier = Modifier
                .coverShadow(
                    radius = CardCornerRadius
                ),
            shape = RoundedCornerShape(CardCornerRadius)
        ) {
            Column(
                modifier = modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.extendedColor.backgroundColor)
                    .padding(ContainerPadding),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (showCloseButton) {
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(NormalIconButtonSize)
                            .padding(NormalIconButtonPadding)
                            .align(Alignment.End)
                    ) {
                        Icon(
                            painter = painterResource(Res.drawable.close_circle),
                            contentDescription = "",
                            tint = MaterialTheme.extendedColor.onBackgroundColor
                        )
                    }
                }
                CustomText(
                    text = title,
                    fontSize = LargeTextSize,
                    fontWeight = FontWeight.Normal,
                    letterSpacing = 1.sp
                )
                if (!message.isNullOrBlank()) {
                    SpaceMedium()
                    CustomText(
                        text = message,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Normal,
                        color = MaterialTheme.extendedColor.onBackgroundColor.copy(alpha = 0.7f),
                    )
                }
                SpaceLarge()
                CustomTextField(
                    containerColor = MaterialTheme.extendedColor.cardColor,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(TextFieldHeight),
                    value = value,
                    onValueChange = onValueChange,
                    label = label,
                    readOnly = isReadOnly,
                    singleLine = isSingleLine,
                    hasBorder = false
                )


                SpaceLarge()
                SpaceLarge()
                CustomButton(
                    enabled = enabled,
                    text = buttonText,
                    modifier = Modifier
                        .height(TextFieldHeight)
                        .fillMaxWidth(),
                    onClick = {
                        if (dismissOnButton) onDismiss.invoke()
                        onButtonClick.invoke()
                    }
                )
                SpaceMedium()
            }
        }
    }
}

@Preview
@Composable
private fun CustomConfirmDialogPre() {
    CustomDialogTextField(
        onDismiss = {},
        value = "",
        onValueChange = {},
        label = "misol: O'quv markaz",
        isReadOnly = false,
        isSingleLine = true,
        showCloseButton = true,
        show = true,
        title = "Jadval nomini kiriting",
        buttonText = "Saqlash",
        onButtonClick = {}
    )
}