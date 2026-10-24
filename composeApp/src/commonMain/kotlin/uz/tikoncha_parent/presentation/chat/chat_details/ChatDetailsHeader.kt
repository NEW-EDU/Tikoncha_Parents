package uz.tikoncha_parent.presentation.chat.chat_details

import uz.tikoncha_parent.ui.rememberCachedImage
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import tikoncha_parents.composeapp.generated.resources.Res
import tikoncha_parents.composeapp.generated.resources.arrow_left
import tikoncha_parents.composeapp.generated.resources.azo
import tikoncha_parents.composeapp.generated.resources.azolar
import tikoncha_parents.composeapp.generated.resources.chat_icon
import uz.tikoncha_parent.presentation.base.CustomText
import uz.tikoncha_parent.ui.ContainerPadding
import uz.tikoncha_parent.ui.HeaderHeight
import uz.tikoncha_parent.ui.NormalIconButtonPadding
import uz.tikoncha_parent.ui.NormalIconButtonSize
import uz.tikoncha_parent.ui.ShapeCornerRadius
import uz.tikoncha_parent.ui.SmallTextSize
import uz.tikoncha_parent.ui.SpaceSmall
import uz.tikoncha_parent.ui.UltraSmallTextSize
import uz.tikoncha_parent.ui.theme.ThemeMode
import uz.tikoncha_parent.ui.theme.TikonchaParentTheme
import uz.tikoncha_parent.ui.theme.extendedColor

@Composable
fun ChatDetailsHeader(
    state: ChatDetailState,
    onBackPressed: () -> Unit
) {

    val bottomShape = RoundedCornerShape(topStart = 0.dp, topEnd = 0.dp, bottomStart = ShapeCornerRadius, bottomEnd = ShapeCornerRadius)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .height(HeaderHeight),
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 4.dp,
                    shape = bottomShape,
                    ambientColor = MaterialTheme.extendedColor.shadowColor,
                    spotColor = MaterialTheme.extendedColor.shadowColor
                )
                .background(
                    color = MaterialTheme.extendedColor.backgroundColor,
                    shape = bottomShape
                )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(HeaderHeight)
                    .padding(horizontal = ContainerPadding),
                verticalAlignment = Alignment.CenterVertically
            )
            {
                FilledTonalIconButton(
                    modifier = Modifier.size(NormalIconButtonSize),
                    onClick = onBackPressed,
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = MaterialTheme.extendedColor.cardColor,
                        contentColor = MaterialTheme.extendedColor.textColor
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.arrow_left),
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(NormalIconButtonPadding)
                    )
                }

                SpaceSmall()

                AsyncImage(
                    model = rememberCachedImage(state.chatAvatar),
                    error = painterResource(Res.drawable.chat_icon),
                    contentDescription = null,
                    modifier = Modifier.size(NormalIconButtonSize)
                        .border(1.dp, MaterialTheme.extendedColor.borderColor, CircleShape)
                        .clip(CircleShape)

                )


                SpaceSmall()


                Column(Modifier.fillMaxWidth()) {
                    CustomText(
                        text = state.chatTitle,
                        fontSize = SmallTextSize,
                        maxLines = 1,
                        lineHeight = SmallTextSize,
                        fontWeight = FontWeight.Normal
                    )


                    val memberBuilder = StringBuilder()
                    if (state.memberCount>1){
                        memberBuilder.append(stringResource(Res.string.azolar))
                    }else{
                        memberBuilder.append(stringResource(Res.string.azo))
                    }

                    memberBuilder.append(": ")
                    memberBuilder.append(state.memberCount)


                    CustomText(
                        text = memberBuilder.toString(),
                        fontSize = UltraSmallTextSize,
                        maxLines = 1,
                        color = MaterialTheme.extendedColor.hintColor,
                        lineHeight = UltraSmallTextSize,
                    )


                }
            }
        }
    }
}

@Preview
@Composable
private fun Preview() {
    TikonchaParentTheme(mode = ThemeMode.LIGHT){
       ChatDetailsHeader(
           state = ChatDetailState(),
           onBackPressed = {}
       )
   }

}
