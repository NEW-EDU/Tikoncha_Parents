package uz.tikoncha_parent.presentation.profile.coin_purchase

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import tikoncha_parents.composeapp.generated.resources.Res
import tikoncha_parents.composeapp.generated.resources.promokod
import tikoncha_parents.composeapp.generated.resources.promokodni_kiriting
import tikoncha_parents.composeapp.generated.resources.qollash
import uz.tikoncha_parent.presentation.base.CustomLoadingButton
import uz.tikoncha_parent.presentation.base.CustomTextField
import uz.tikoncha_parent.presentation.base.simpleShadow
import uz.tikoncha_parent.ui.theme.AppColors
import uz.tikoncha_parent.ui.theme.AppTypography
import uz.tikoncha_parent.ui.theme.ThemeMode
import uz.tikoncha_parent.ui.theme.TikonchaParentTheme

@Composable
fun PromoCodeContainer(
    modifier: Modifier = Modifier,
    showLoading: Boolean = false,
    enabled: Boolean = true,
    onClick: (String) -> Unit
) {


    var promoCode by rememberSaveable {
        mutableStateOf("")
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .simpleShadow(RoundedCornerShape(16.dp))
            .background(AppColors.bg.surface, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Text(
            text = stringResource(Res.string.promokod),
            color = AppColors.text.primary,
            style = AppTypography.titleMdMedium
        )
        Spacer(Modifier.height(10.dp))

        Row {
            CustomTextField(
                value = promoCode,
                onValueChange = {
                    promoCode = it.filterNot { c -> c.isWhitespace() }.uppercase()
                },
                modifier = Modifier
                    .height(48.dp)
                    .weight(1f),
                label = stringResource(Res.string.promokodni_kiriting),
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Characters,
                    imeAction = ImeAction.Done
                ),
                singleLine = true,
                shape = RoundedCornerShape(16.dp)
            )
            Spacer(Modifier.width(10.dp))

            CustomLoadingButton(
                loading = showLoading,
                shape = RoundedCornerShape(16.dp),
                text = stringResource(Res.string.qollash),
                enabled = promoCode.isNotBlank() && enabled,
                onClick = {
                    onClick(promoCode)
                },
                modifier = Modifier
                    .height(48.dp)
                    .widthIn(min = 105.dp),
            )
        }
    }
}

@Preview
@Composable
private fun Pre() {
    TikonchaParentTheme(
        ThemeMode.LIGHT
    ) {
        PromoCodeContainer(
            showLoading = true,
            onClick = {}
        )
    }
}