package uz.tikoncha_parent.presentation.add_child

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import tikoncha_parents.composeapp.generated.resources.Res
import tikoncha_parents.composeapp.generated.resources.connect_child_line1
import tikoncha_parents.composeapp.generated.resources.connect_child_line2
import tikoncha_parents.composeapp.generated.resources.play
import uz.tikoncha_parent.presentation.base.simpleShadow
import uz.tikoncha_parent.ui.Space
import uz.tikoncha_parent.ui.theme.AppColors
import uz.tikoncha_parent.ui.theme.NunitoFontFamily
import uz.tikoncha_parent.ui.theme.TikonchaParentTheme

@Composable
fun ConnectChildTutorialCard(
    modifier: Modifier = Modifier,
    onClick: () -> Unit
){
    Row(
        modifier = modifier
            .fillMaxWidth()
            .simpleShadow(RoundedCornerShape(32.dp))
            .background(AppColors.modal.primary, RoundedCornerShape(32.dp))
            .clickable(
                indication = null,
                interactionSource = null,
                onClick = onClick
            )
            .padding(vertical = 30.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceAround
    ){
        Column {
            Text(
                text = stringResource(Res.string.connect_child_line1),
                color = AppColors.text.primary,
                fontFamily = NunitoFontFamily(),
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Space(4.dp)
            Text(
                text = stringResource(Res.string.connect_child_line2),
                color = AppColors.text.accentEmphasis,
                fontFamily = NunitoFontFamily(),
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier
                    .padding(start = 0.dp)
            )
        }
        Icon(
            painter = painterResource(Res.drawable.play),
            contentDescription = "",
            modifier = Modifier
                .size(52.dp),
            tint = AppColors.icon.accentPrimarySurface
        )
    }
}

@Preview
@Composable
private fun Pre(){
    TikonchaParentTheme {
        ConnectChildTutorialCard(
            modifier = Modifier
                .fillMaxWidth(),
            onClick = {}
        )
    }
}