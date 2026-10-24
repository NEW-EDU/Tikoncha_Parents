package uz.tikoncha_parent.presentation.chat.item

import uz.tikoncha_parent.ui.Haptics
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.platform.LocalViewConfiguration
import androidx.compose.ui.platform.LocalWindowInfo
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
import tikoncha_parents.composeapp.generated.resources.message_delete
import tikoncha_parents.composeapp.generated.resources.message_edit
import tikoncha_parents.composeapp.generated.resources.message_failed
import tikoncha_parents.composeapp.generated.resources.message_read
import tikoncha_parents.composeapp.generated.resources.message_reply
import tikoncha_parents.composeapp.generated.resources.message_retry
import tikoncha_parents.composeapp.generated.resources.message_sending
import tikoncha_parents.composeapp.generated.resources.message_sent
import tikoncha_parents.composeapp.generated.resources.nusxalash
import tikoncha_parents.composeapp.generated.resources.ochirish
import tikoncha_parents.composeapp.generated.resources.qayta_urinish
import tikoncha_parents.composeapp.generated.resources.tahrirlangan
import tikoncha_parents.composeapp.generated.resources.tahrirlash
import uz.tikoncha_parent.common.DateTimeUtil
import uz.tikoncha_parent.presentation.base.CustomText
import uz.tikoncha_parent.presentation.chat.model.DeliveryStatus
import uz.tikoncha_parent.presentation.chat.model.MessageMenuAction
import uz.tikoncha_parent.presentation.model.ChatMessageUi
import uz.tikoncha_parent.ui.ChatMessageCornerRadius
import uz.tikoncha_parent.ui.Failed
import uz.tikoncha_parent.ui.SmallIconSize
import uz.tikoncha_parent.ui.SpaceUltraSmall
import uz.tikoncha_parent.ui.UltraSmallTextSize
import uz.tikoncha_parent.ui.theme.AppColors
import uz.tikoncha_parent.ui.theme.AppTypography
import uz.tikoncha_parent.ui.theme.ThemeMode
import uz.tikoncha_parent.ui.theme.TikonchaParentTheme
import uz.tikoncha_parent.ui.theme.extendedColor
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * ✅ Sent (right) bubble: Telegram-like time+status behavior (cached)
 */
@Composable
fun MessageSentItem(
    modifier: Modifier = Modifier,
    chatMessageUi: ChatMessageUi,
    menuExpanded: Boolean,
    failedMenuExpanded: Boolean,
    onOpenMenu: (failedMenu: Boolean) -> Unit,
    onDismissMenu: () -> Unit,
    onMenuAction: (MessageMenuAction) -> Unit
) {
    val maxBubbleWidth = LocalWindowInfo.current.containerDpSize.width * 0.78f
    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current
    val offsetX = remember { Animatable(0f) }
    val maxDrag = 260F
    val threshold = 180f
    val dragged = remember { mutableStateOf(false) }
    val fired = remember { mutableStateOf(false) }
    val viewConfig = LocalViewConfiguration.current
    val touchSlop = viewConfig.touchSlop
    var dragJob: Job? by remember { mutableStateOf(null) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .pointerInput(chatMessageUi.id + "_tap" + chatMessageUi.status) {
                detectTapGestures(
                    onTap = {
                        if (!dragged.value) {
                            onOpenMenu(chatMessageUi.status == DeliveryStatus.FAILED)
                        }
                    }
                )
            }
            .pointerInput(chatMessageUi.id + "_drag" + chatMessageUi.status) {
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

                                val newOffset = (offsetX.value + dx).coerceIn(-maxDrag, 0f)
                                dragJob?.cancel()
                                dragJob = scope.launch {
                                    offsetX.snapTo(newOffset)
                                }

                                if (!fired.value && abs(newOffset) >= threshold) {
                                    fired.value = true
                                    Haptics.tap()
                                }
                            }
                        } while (drag.pressed)

                        val shouldReply = isHorizontal == true && abs(offsetX.value) >= threshold
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
            },
        contentAlignment = Alignment.TopEnd
    ) {
        Box {

            Box(
                modifier = Modifier
                    .matchParentSize()
                    .padding(end = 18.dp),
                contentAlignment = Alignment.Center
            ) {
                val progress = (abs(offsetX.value) / threshold).coerceIn(0f,1f)
                Icon(
                    painter = painterResource(Res.drawable.message_reply),
                    contentDescription = null,
                    tint = AppColors.icon.accentPrimary.copy(alpha = progress),
                    modifier = Modifier.size(22.dp)
                )
            }

            Column(
                modifier = modifier
                    .widthIn(max = maxBubbleWidth)
                    .offset{IntOffset(offsetX.value.roundToInt(),0)}
                    .clip(
                        RoundedCornerShape(
                            topStart = ChatMessageCornerRadius,
                            topEnd = ChatMessageCornerRadius,
                            bottomStart = ChatMessageCornerRadius
                        )
                    )
                    .background(AppColors.bg.secondaryContainer)
                    .padding(start = 15.dp, top = 15.dp, end = 8.dp, bottom = 4.dp)
            ) {
                if (
                    chatMessageUi.replyToId != null &&
                    chatMessageUi.repliedMessageOwner != null &&
                    chatMessageUi.repliedMessageText != null
                ) {
                    RepliedMessageBubble(
                        ownerName = chatMessageUi.repliedMessageOwner,
                        text = chatMessageUi.repliedMessageText,
                        isMine = false
                    )
                    Spacer(Modifier.height(5.dp))
                }

                Text(
                    text = chatMessageUi.message,
                    style = AppTypography.emphasizedMdMedium,
                    color = AppColors.text.primary,
                    modifier = Modifier
                )

                SentMeta(
                    modifier = Modifier.align(Alignment.End),
                    time = chatMessageUi.time,
                    status = chatMessageUi.status,
                    isEdited = chatMessageUi.isEdited
                )
            }

            DropdownMenu(
                modifier = Modifier.clip(RoundedCornerShape(10.dp)),
                expanded = menuExpanded || failedMenuExpanded,
                onDismissRequest = onDismissMenu,
                shape = RoundedCornerShape(8.dp),
                containerColor = MaterialTheme.extendedColor.cardColor,
                tonalElevation = 0.dp,
                shadowElevation = 0.dp,
                offset = DpOffset(x = (-10).dp, y = 0.dp)
            ){
                if (menuExpanded) {
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
                                tint = MaterialTheme.extendedColor.primaryColor,
                                modifier = Modifier.size(SmallIconSize)
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
                                tint = MaterialTheme.extendedColor.primaryColor,
                                modifier = Modifier.size(SmallIconSize)
                            )
                        },
                        onClick = {
                            onMenuAction(MessageMenuAction.Copy)
                        }
                    )

                    DropdownMenuItem(
                        text = {
                            CustomText(
                                text = stringResource(Res.string.tahrirlash),
                            )
                        },
                        leadingIcon = {
                            Icon(
                                painter = painterResource(Res.drawable.message_edit),
                                contentDescription = null,
                                tint = MaterialTheme.extendedColor.primaryColor,
                                modifier = Modifier.size(SmallIconSize)
                            )
                        },
                        onClick = {
                            onMenuAction(MessageMenuAction.Edit)
                        }
                    )

                    DropdownMenuItem(
                        text = {
                            CustomText(
                                text = stringResource(Res.string.ochirish),
                            )
                        },
                        leadingIcon = {
                            Icon(
                                painter = painterResource(Res.drawable.message_delete),
                                contentDescription = null,
                                tint = MaterialTheme.extendedColor.primaryColor,
                                modifier = Modifier.size(SmallIconSize)
                            )
                        },
                        onClick = {
                            onMenuAction(MessageMenuAction.Delete)
                        }
                    )
                }

                if (failedMenuExpanded) {
                    DropdownMenuItem(
                        text = {
                            CustomText(
                                text = stringResource(Res.string.qayta_urinish)
                            )
                        },
                        leadingIcon = {
                            Icon(
                                painter = painterResource(Res.drawable.message_retry),
                                contentDescription = null,
                                tint = MaterialTheme.extendedColor.primaryColor,
                                modifier = Modifier.size(SmallIconSize)
                            )
                        },
                        onClick = {
                            onMenuAction(MessageMenuAction.Retry)
                        }
                    )

                    DropdownMenuItem(
                        text = {
                            CustomText(
                                text = stringResource(Res.string.ochirish)
                            )
                        },
                        leadingIcon = {
                            Icon(
                                painter = painterResource(Res.drawable.message_delete),
                                contentDescription = null,
                                tint = MaterialTheme.extendedColor.primaryColor,
                                modifier = Modifier.size(SmallIconSize)
                            )
                        },
                        onClick = {
                            onMenuAction(MessageMenuAction.RetryDelete)
                        }
                    )
                }
            }
        }
    }
}


@Composable
private fun SentMeta(
    modifier: Modifier = Modifier,
    time: String,
    status: DeliveryStatus,
    isEdited: Boolean = false
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.End
    ) {

        val textColor = when (status) {
            DeliveryStatus.SENDING -> MaterialTheme.extendedColor.hintColor
            DeliveryStatus.SENT -> MaterialTheme.extendedColor.primaryColor
            DeliveryStatus.READ -> MaterialTheme.extendedColor.primaryColor
            DeliveryStatus.FAILED -> Failed
        }

        val iconColor = when (status) {
            DeliveryStatus.SENDING -> MaterialTheme.extendedColor.hintColor
            DeliveryStatus.SENT -> MaterialTheme.extendedColor.primaryColor
            DeliveryStatus.READ -> MaterialTheme.extendedColor.primaryColor
            DeliveryStatus.FAILED -> Failed
        }

        val icon = when (status) {
            DeliveryStatus.SENDING -> painterResource(Res.drawable.message_sending)
            DeliveryStatus.SENT -> painterResource(Res.drawable.message_sent)
            DeliveryStatus.READ -> painterResource(Res.drawable.message_read)
            DeliveryStatus.FAILED -> painterResource(Res.drawable.message_failed)
        }

        if (isEdited) {
            CustomText(
                text = stringResource(Res.string.tahrirlangan),
                color = textColor,
                fontSize = UltraSmallTextSize,
                lineHeight = UltraSmallTextSize,
            )
            SpaceUltraSmall()
        }

        CustomText(
            text = time,
            color = textColor,
            fontSize = UltraSmallTextSize,
            lineHeight = UltraSmallTextSize,
        )
        SpaceUltraSmall()

        Icon(
            painter = icon,
            contentDescription = null,
            modifier = Modifier.size(SmallIconSize),
            tint = iconColor
        )
    }
}


//@Preview(
//    name = "Sent message – short",
//    showBackground = true,
//    backgroundColor = 0xFFF2F2F2,
//    widthDp = 360
//)
//@Composable
//private fun PreviewSentMessageShortOld() {
//    TikonchaParentTheme(
//        ThemeMode.DARK
//    ) {
//        MessageSentItem(
//            chatMessageUi = ChatMessageUi(
//                id = "1",
//                isMine = true,
//                message = "Salom",
//                createdAt = DateTimeUtil.nowMillis(),
//                time = "10:25",
//                status = DeliveryStatus.READ,
//                senderName = "Me",
//                senderAvatar = ""
//            ),
//            menuExpanded = false,
//            failedMenuExpanded = false,
//            onOpenMenu = {},
//            onDismissMenu = {},
//            onMenuAction = {}
//        )
//    }
//}

@Preview(
    name = "Sent message – 2 lines",
    showBackground = true,
    backgroundColor = 0xFFF2F2F2,
    widthDp = 360
)
@Composable
private fun PreviewSentMessageTwoLinesOld() {
    TikonchaParentTheme(
        ThemeMode.DARK
    ) {
        MessageSentItem(
            chatMessageUi = ChatMessageUi(
                id = "3",
                isMine = true,
                message = "Ustoz bugun birinchi darsga bir necha soatdan so‘ng darslar",
                createdAt = DateTimeUtil.nowMillis(),
                time = "10:26",
                status = DeliveryStatus.SENDING,
                senderName = "Me",
                senderAvatar = "",
                replyToId = "4",
                repliedMessageOwner = "Men",
                repliedMessageText = "Salom qandaysan"
            ),
            menuExpanded = false,
            failedMenuExpanded = true,
            onOpenMenu = {},
            onDismissMenu = {},
            onMenuAction = {}
        )
    }
}

//@Preview(
//    name = "Sent message – long (meta below)",
//    showBackground = true,
//    backgroundColor = 0xFFF2F2F2,
//    widthDp = 360
//)
//@Composable
//private fun PreviewSentMessageLongOld() {
//    TikonchaParentTheme(
//        ThemeMode.DARK
//    ) {
//        MessageSentItem(
//            chatMessageUi = ChatMessageUi(
//                id = "3",
//                isMine = true,
//                message = "Bu juda uzun xabar bo‘lib, oxirgi qatorda soat va belgi sig‘maydi va shuning uchun Telegram’dagi kabi pastga alohida qatorda chiqishi kerak",
//                createdAt = DateTimeUtil.nowMillis(),
//                time = "10:27",
//                status = DeliveryStatus.SENDING,
//                senderName = "Me",
//                senderAvatar = ""
//            ),
//            menuExpanded = false,
//            failedMenuExpanded = false,
//            onOpenMenu = {},
//            onDismissMenu = {},
//            onMenuAction = {}
//        )
//    }
//}

