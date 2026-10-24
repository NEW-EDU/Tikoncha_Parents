package uz.saidburxon.newedu.presentation.feature.chat.chat_details

import uz.tikoncha_parent.ui.rememberCachedImage
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight

import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import tikoncha_parents.composeapp.generated.resources.Res
import tikoncha_parents.composeapp.generated.resources.chat_icon
import tikoncha_parents.composeapp.generated.resources.faol
import tikoncha_parents.composeapp.generated.resources.ohirgi_faollik
import uz.tikoncha_parent.presentation.base.CustomText
import uz.tikoncha_parent.presentation.model.ChatMemberUi
import uz.tikoncha_parent.ui.LargeIconButtonSize
import uz.tikoncha_parent.ui.NormalTextSize
import uz.tikoncha_parent.ui.SpaceMedium
import uz.tikoncha_parent.ui.UltraSmallTextSize
import uz.tikoncha_parent.ui.theme.extendedColor

@Composable
fun ChatMemberItem(chatMemberUi: ChatMemberUi) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    )
    {
        AsyncImage(
            model = rememberCachedImage(chatMemberUi.avatar),
            error = painterResource(Res.drawable.chat_icon),
            contentDescription = null,
            modifier = Modifier
                .size(LargeIconButtonSize)
                .border(1.dp, MaterialTheme.extendedColor.borderColor, CircleShape)
                .clip(CircleShape)

        )


        SpaceMedium()


        Column(Modifier.fillMaxWidth()) {
            CustomText(
                text = chatMemberUi.name,
                fontSize = NormalTextSize,
                maxLines = 1,
                lineHeight = NormalTextSize,
                fontWeight = FontWeight.W600
            )



            val memberBuilder = StringBuilder()
            if (chatMemberUi.isOnline){
                memberBuilder.append(stringResource(Res.string.faol))
            }else{
                memberBuilder.append(stringResource(Res.string.ohirgi_faollik))
                memberBuilder.append(": ")
                memberBuilder.append(chatMemberUi.lastSeen)
            }
            CustomText(
                text = memberBuilder.toString(),
                fontSize = UltraSmallTextSize,
                maxLines = 1,
                lineHeight = UltraSmallTextSize,
                color = when (chatMemberUi.isOnline) {
                    true -> MaterialTheme.extendedColor.primaryColor
                    false -> MaterialTheme.extendedColor.hintColor
                }
            )


        }
    }
}

@Preview
@Composable
private fun Pre() {
    ChatMemberItem(
        chatMemberUi = ChatMemberUi(
            id = "",
            avatar = "",
            name = "Ibroxim Odilov"
        )
    )
}