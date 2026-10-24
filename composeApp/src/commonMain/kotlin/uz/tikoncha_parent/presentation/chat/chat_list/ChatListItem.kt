package uz.tikoncha_parent.presentation.chat.chat_list

import uz.tikoncha_parent.ui.rememberCachedImage
import uz.tikoncha_parent.ui.expressiveSegment
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox

import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale

import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow

import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import tikoncha_parents.composeapp.generated.resources.Res
import tikoncha_parents.composeapp.generated.resources.chat_ai_icon
import tikoncha_parents.composeapp.generated.resources.chat_bot
import tikoncha_parents.composeapp.generated.resources.chat_group
import tikoncha_parents.composeapp.generated.resources.chat_icon
import tikoncha_parents.composeapp.generated.resources.chat_person
import tikoncha_parents.composeapp.generated.resources.message_read
import tikoncha_parents.composeapp.generated.resources.message_sent
import uz.tikoncha_parent.presentation.base.CustomText
import uz.tikoncha_parent.presentation.base.CircularBadge
import uz.tikoncha_parent.presentation.chat.ChatUtil
import uz.tikoncha_parent.presentation.chat.ChatUtil.asText

import uz.tikoncha_parent.presentation.chat.model.ChatDateLabel
import uz.tikoncha_parent.presentation.model.ChatType
import uz.tikoncha_parent.presentation.model.ChatUi
import uz.tikoncha_parent.ui.ChatHeaderAvatarSize
import uz.tikoncha_parent.ui.ChatMessageColor
import uz.tikoncha_parent.ui.NormalTextSize
import uz.tikoncha_parent.ui.PrimaryColor
import uz.tikoncha_parent.ui.SmallIconSize
import uz.tikoncha_parent.ui.SmallTextSize
import uz.tikoncha_parent.ui.SpaceMedium
import uz.tikoncha_parent.ui.SpaceUltraSmall
import uz.tikoncha_parent.ui.theme.AppColors
import uz.tikoncha_parent.ui.theme.AppTypography
import uz.tikoncha_parent.ui.theme.ThemeMode
import uz.tikoncha_parent.ui.theme.TikonchaParentTheme
import uz.tikoncha_parent.ui.theme.extendedColor
import kotlin.toString


@Composable
fun ChatListItem(
    chatUi: ChatUi,
    modifier: Modifier = Modifier,
    /** M3 Expressive guruhlangan ro'yxatdagi o'rni. */
    segmentIndex: Int = 0,
    segmentCount: Int = 1,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .expressiveSegment(segmentIndex, segmentCount, color = AppColors.bg.surface, onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {

        val avatar = if (chatUi.type == ChatType.BOT){
            painterResource(Res.drawable.chat_ai_icon)
        }
        else{
            painterResource(Res.drawable.chat_icon)
        }

        if (chatUi.avatar.isNotEmpty()){
            AsyncImage(
                model = rememberCachedImage(chatUi.avatar),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(52.dp)
                    .border(1.dp, AppColors.bg.surface, CircleShape)
                    .clip(CircleShape),
                error = painterResource(Res.drawable.chat_icon),

            )
        }
        else{
            Box(
                modifier = Modifier.size(52.dp)
                    .background(AppColors.bg.primaryContainer, CircleShape)
                    .border(1.dp, AppColors.bg.surface, CircleShape),
                contentAlignment = Alignment.Center
            ){
                Text(
                    text = ChatUtil.getInitials(
                        fullName = chatUi.title
                    ),
                    style = AppTypography.titleLgMedium,
                    color = AppColors.text.accentEmphasis,
                )
            }
        }
        Spacer(Modifier.width(12.dp))

        Column(Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                val icon = when(chatUi.type){
                    ChatType.BOT -> {painterResource(Res.drawable.chat_bot)}
                    ChatType.PARENT_CHILD -> {painterResource(Res.drawable.chat_person)}
                    ChatType.CLASS -> {painterResource(Res.drawable.chat_group)}
                    else ->  {painterResource(Res.drawable.chat_person)}
                }

                Icon(
                    painter = icon,
                    contentDescription = "",
                    modifier = Modifier.size(SmallIconSize),
                    tint = AppColors.icon.primary
                )
                SpaceUltraSmall()

                Text(
                    maxLines = 1,
                    text = chatUi.title,
                    color = AppColors.text.primary,
                    overflow = TextOverflow.Ellipsis,
                    style = AppTypography.titleMdMedium,
                    modifier = Modifier
                        .weight(1f)
                        .padding(bottom = 4.dp)
                )
                SpaceMedium()

                Text(
                    text = chatUi.dateTime.asText(),
                    style = AppTypography.titleSmMedium,
                    color = AppColors.text.secondary,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    maxLines = 1,
                    text = chatUi.lastMessage,
                    style = AppTypography.titleSmMedium,
                    color = AppColors.text.secondary,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                if (chatUi.unreadCount > 0 && !chatUi.lastMessageIsMine) {
                    SpaceMedium()
                    BadgedBox(
                        badge = {},
                        modifier = Modifier.height(24.dp)
                    ) {
                        Badge(
                            modifier = Modifier,
                            containerColor = AppColors.bg.primary
                        ) {
                            Text(
                                text = chatUi.unreadCount.toString(),
                                style = AppTypography.bodyMdMedium,
                                color = AppColors.text.inverse,
                                modifier = Modifier.padding(4.dp)
                            )
                        }
                    }
                }
                else if (chatUi.lastMessageIsMine){
                    SpaceMedium()
                    val icon = if (chatUi.lastMessageIsRead){
                        painterResource(Res.drawable.message_read)
                    }
                    else{
                        painterResource(Res.drawable.message_sent)
                    }
                    Icon(
                        painter = icon,
                        contentDescription = "",
                        modifier = Modifier.size(20.dp),
                        tint = AppColors.icon.accentPrimary
                    )
                }
            }


        }



    }
}


@Preview
@Composable
private fun Preview() {
    TikonchaParentTheme(ThemeMode.LIGHT){
        ChatListItem(
            chatUi = ChatUi(
                title = "Tikoncha",
                lastMessage = "Barcha savollaringizga javob beraman",
                unreadCount = 8,
                chatId = "",
                dateTime = ChatDateLabel.Yesterday,
                type = ChatType.BOT,
                lastMessageIsMine = false,
                lastMessageIsRead = true,
                avatar = ""
            ),
            onClick = {

            },

            )
    }

}