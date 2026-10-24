package uz.tikoncha_parent.presentation.chat.item

import uz.tikoncha_parent.ui.Haptics
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import tikoncha_parents.composeapp.generated.resources.Res
import tikoncha_parents.composeapp.generated.resources.javob_berish
import tikoncha_parents.composeapp.generated.resources.message_copy
import tikoncha_parents.composeapp.generated.resources.message_reply
import tikoncha_parents.composeapp.generated.resources.nusxalash
import tikoncha_parents.composeapp.generated.resources.tahrirlangan
import uz.tikoncha_parent.common.DateTimeUtil
import uz.tikoncha_parent.presentation.base.CustomText
import uz.tikoncha_parent.presentation.chat.model.DeliveryStatus
import uz.tikoncha_parent.presentation.chat.model.MessageMenuAction
import uz.tikoncha_parent.presentation.model.ChatMessageUi
import uz.tikoncha_parent.ui.ChatMessageCornerRadius
import uz.tikoncha_parent.ui.SmallTextSize
import uz.tikoncha_parent.ui.SpaceUltraSmall
import uz.tikoncha_parent.ui.UltraSmallTextSize
import uz.tikoncha_parent.ui.theme.AppColors
import uz.tikoncha_parent.ui.theme.AppTypography
import uz.tikoncha_parent.ui.theme.ThemeMode
import uz.tikoncha_parent.ui.theme.TikonchaParentTheme
import uz.tikoncha_parent.ui.theme.extendedColor
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
fun MessageReceivedItem(
    modifier: Modifier = Modifier,
    chatMessageUi: ChatMessageUi,
    showSender: Boolean = false,
    menuExpanded: Boolean,
    onOpenMenu: () -> Unit,
    onDismissMenu: () -> Unit,
    onMenuAction: (MessageMenuAction) -> Unit
) {
    val maxBubbleWidth = LocalWindowInfo.current.containerDpSize.width * 0.78f

    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current
    val offsetX = remember { Animatable(0f) }
    val maxDrag = 260f
    val threshold = 180f
    val dragged = remember { mutableStateOf(false) }
    val fired = remember { mutableStateOf(false) }
    var dragJob: Job? by remember { mutableStateOf(null) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .pointerInput(chatMessageUi.id + "_drag") {
                var isHorizontal: Boolean? = null

                awaitPointerEventScope {
                    while (true) {
                        awaitFirstDown(requireUnconsumed = false)
                        isHorizontal = null
                        dragged.value = false
                        fired.value = false

                        var totalX = 0f
                        var totalY = 0f

                        do {
                            val event = awaitPointerEvent()
                            val drag = event.changes.firstOrNull() ?: break
                            val dx = drag.position.x - drag.previousPosition.x
                            val dy = drag.position.y - drag.previousPosition.y
                            totalX += dx
                            totalY += dy

                            if (isHorizontal == null && (abs(totalX) > 10f || abs(totalY) > 10f)) {
                                isHorizontal = abs(totalX) > abs(totalY)
                            }

                            if (isHorizontal == true) {
                                drag.consume()
                                dragged.value = true

                                val newOffset = (offsetX.value + dx).coerceIn(0f, maxDrag)
                                dragJob?.cancel()
                                dragJob = scope.launch { offsetX.snapTo(newOffset) }

                                if (!fired.value && newOffset >= threshold) {
                                    fired.value = true
                                    Haptics.tap()
                                }
                            }
                        } while (drag.pressed)

                        val shouldReply = isHorizontal == true && offsetX.value >= threshold
                        dragged.value = false
                        fired.value = false
                        dragJob?.cancel()
                        scope.launch {
                            offsetX.animateTo(
                                0f,
                                spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness = Spring.StiffnessMedium
                                )
                            )
                        }
                        if (shouldReply) onMenuAction(MessageMenuAction.Reply)
                    }
                }
            }
            .pointerInput(chatMessageUi.id + "_tap") {
                detectTapGestures(
                    onTap = {
                        if (!dragged.value) {
                            onOpenMenu()
                        }
                    }
                )
            },
        contentAlignment = Alignment.TopStart
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .padding(start = 18.dp),
            contentAlignment = Alignment.CenterStart
        ){
            val progress = (offsetX.value / threshold).coerceIn(0f, 1f)
            Icon(
                painter = painterResource(Res.drawable.message_reply),
                contentDescription = null,
                tint = AppColors.icon.accentPrimary.copy(alpha = progress),
                modifier = Modifier.size(22.dp)
            )
        }
        Box {
            Column(
                modifier = modifier
                    .widthIn(max = maxBubbleWidth)
                    .offset{ IntOffset(offsetX.value.roundToInt(), 0) }
                    .clip(
                        RoundedCornerShape(
                            topStart = ChatMessageCornerRadius,
                            topEnd = ChatMessageCornerRadius,
                            bottomEnd = ChatMessageCornerRadius
                        )
                    )
                    .background(AppColors.bg.secondaryContainer)
                    .padding(horizontal = 15.dp, vertical = 8.dp)
            ) {
                if (showSender) {
                    CustomText(
                        text = chatMessageUi.senderName,
                        color = MaterialTheme.extendedColor.primaryColor,
                        fontSize = SmallTextSize,
                        fontWeight = FontWeight.W500
                    )
                    Spacer(Modifier.height(2.dp))
                }

                if (
                    chatMessageUi.replyToId != null &&
                    chatMessageUi.repliedMessageOwner != null &&
                    chatMessageUi.repliedMessageText != null
                ){
                    RepliedMessageBubble(
                        ownerName = chatMessageUi.repliedMessageOwner,
                        text = chatMessageUi.repliedMessageText,
                        isMine = false
                    )
                    Spacer(Modifier.height(5.dp))
                }

                Text(
                    modifier = Modifier,
                    text = chatMessageUi.message,
                    color = AppColors.text.primary,
                    style = AppTypography.emphasizedMdRegular
                )

                Row(
                    modifier = Modifier.align(Alignment.End),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (chatMessageUi.isEdited) {
                        CustomText(
                            text = stringResource(Res.string.tahrirlangan),
                            color = AppColors.text.tertiary,
                            fontSize = UltraSmallTextSize,
                            lineHeight = UltraSmallTextSize,
                        )
                        SpaceUltraSmall()
                    }

                    CustomText(
                        text = chatMessageUi.time,
                        color = AppColors.text.tertiary,
                        fontSize = UltraSmallTextSize,
                        lineHeight = UltraSmallTextSize,
                    )
                }
            }

            DropdownMenu(
                modifier = Modifier.clip(RoundedCornerShape(10.dp)),
                expanded = menuExpanded,
                onDismissRequest = onDismissMenu,
                shape = RoundedCornerShape(8.dp),
                containerColor = AppColors.bg.surface,
                tonalElevation = 0.dp,
                shadowElevation = 0.dp,
                offset = DpOffset(x = (-10).dp, y = 0.dp)
            ){
                DropdownMenuItem(
                    text = {
                        CustomText(
                            text = stringResource(Res.string.javob_berish),
                        )
                    },
                    leadingIcon = {
                        Icon(
                            painter = painterResource(Res.drawable.message_reply),
                            contentDescription = null,
                            tint = AppColors.icon.accentPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    onClick = {
                        onMenuAction(MessageMenuAction.Reply)
                    }
                )
                DropdownMenuItem(
                    text = {
                        CustomText(
                            text = stringResource(Res.string.nusxalash),
                        )
                    },
                    leadingIcon = {
                        Icon(
                            painter = painterResource(Res.drawable.message_copy),
                            contentDescription = null,
                            tint = AppColors.icon.accentPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    onClick = {
                        onMenuAction(MessageMenuAction.Copy)
                    }
                )
            }
        }
    }


}

@Composable
fun RepliedMessageBubble(
    modifier: Modifier = Modifier,
    ownerName: String,
    text: String,
    isMine: Boolean
) {
    val accentColor = AppColors.border.accentEmphasis
    val bgColor = if (isMine)
        AppColors.bg.page
    else
        AppColors.bg.page

    Row(
        modifier = modifier
            .width(IntrinsicSize.Max)
            .height(IntrinsicSize.Min)
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .width(2.dp)
                .fillMaxHeight()
                .background(accentColor, RoundedCornerShape(topStart = 8.dp, bottomStart = 8.dp))
        )
        Spacer(Modifier.width(8.dp))

        Column(
            modifier = Modifier
                .padding(vertical = 4.dp, horizontal = 4.dp)
                .weight(1f)
        ) {
            Text(
                text = ownerName,
                style = AppTypography.titleSmMedium,
                color = AppColors.text.accentEmphasis,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = text,
                style = AppTypography.emphasizedMdRegular,
                color = AppColors.text.primary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}


@Preview(
    name = "Sent message – short",
    showBackground = true,
    backgroundColor = 0xFFF2F2F2,
    widthDp = 360
)
@Composable
private fun PreviewSentMessageShortOld() {
    TikonchaParentTheme(
        ThemeMode.DARK
    ) {
        MessageReceivedItem(
            chatMessageUi = ChatMessageUi(
                id = "1",
                isMine = true,
                message = "Salom",
                createdAt = DateTimeUtil.nowMillis(),
                time = "10:25",
                status = DeliveryStatus.READ,
                senderName = "Me",
                senderAvatar = ""
            ),
            showSender = true,
            menuExpanded = true,
            onOpenMenu = {},
            onDismissMenu = {},
            onMenuAction = {}
        )
    }
}

@Preview(
    name = "Sent message – 2 lines",
    showBackground = true,
    backgroundColor = 0xFFF2F2F2,
    widthDp = 360
)
@Composable
private fun PreviewSentMessageTwoLinesOld() {
    TikonchaParentTheme(
        mode = ThemeMode.DARK
    ) {
        MessageReceivedItem(
            chatMessageUi = ChatMessageUi(
                id = "2",
                isMine = true,
                message = "Ustoz bugun birinchi darsga bir necha soatdan so‘ng darslar",
                createdAt = DateTimeUtil.nowMillis(),
                time = "10:26",
                status = DeliveryStatus.SENT,
                senderName = "Me",
                senderAvatar = ""
            ),
            showSender = true,
            menuExpanded = true,
            onOpenMenu = {},
            onDismissMenu = {},
            onMenuAction = {}
        )
    }
}

@Preview(
    name = "Sent message – long (meta below)",
    showBackground = true,
    backgroundColor = 0xFFF2F2F2,
    widthDp = 360
)
@Composable
private fun PreviewSentMessageLongOld() {
    TikonchaParentTheme(
        ThemeMode.DARK
    ) {
        MessageReceivedItem(
            showSender = true,
            chatMessageUi = ChatMessageUi(
                id = "3",
                isMine = true,
                message = "Bu juda uzun xabar bo‘lib, oxirgi qatorda soat va belgi sig‘maydi va shuning uchun Telegram’dagi kabi pastga alohida qatorda chiqishi kerak",
                createdAt = DateTimeUtil.nowMillis(),
                time = "10:27",
                status = DeliveryStatus.SENDING,
                senderName = "Me",
                senderAvatar = ""
            ),
            menuExpanded = true,
            onOpenMenu = {},
            onDismissMenu = {},
            onMenuAction = {}
        )
    }
}

@Preview(
    name = "Sent message – long (meta below)",
    showBackground = true,
    backgroundColor = 0xFFF2F2F2,
    widthDp = 360
)
@Composable
private fun PreviewReplay() {
    TikonchaParentTheme(
        ThemeMode.DARK
    ) {
        RepliedMessageBubble(
            ownerName = "Me",
            text = "Salom",
            isMine = false
        )
    }
}

