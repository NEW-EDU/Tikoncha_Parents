package uz.tikoncha_parent.presentation.base

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import org.jetbrains.compose.resources.painterResource
import tikoncha_parents.composeapp.generated.resources.Res
import tikoncha_parents.composeapp.generated.resources.arrow_down_reg
import tikoncha_parents.composeapp.generated.resources.plus_home_sheet_subscribe
import tikoncha_parents.composeapp.generated.resources.plus_symbol
import tikoncha_parents.composeapp.generated.resources.profile_hedgehog_img
import uz.tikoncha_parent.domain.model.UserInfo
import uz.tikoncha_parent.ui.ColorWhite
import uz.tikoncha_parent.ui.SuccessColor
import uz.tikoncha_parent.ui.theme.AppColors
import uz.tikoncha_parent.ui.theme.AppTypography
import uz.tikoncha_parent.ui.theme.ThemeMode
import uz.tikoncha_parent.ui.theme.TikonchaParentTheme


@Composable
fun ChildSelectionButton(
    modifier: Modifier = Modifier,
    text: String,
    label: String = "",
    imageUrl: String = "",
    onClick: () -> Unit,
    shape: Shape = CircleShape,
    trailingIcon: Boolean = true,
    userInfo: UserInfo? = null,
    background: Color = AppColors.section.tertiary,
    textStyle: TextStyle = AppTypography.titleSmMedium,
    hasSubscription: Boolean = !userInfo?.subscription.isNullOrBlank() && userInfo.subscription != "FREE",
) {
    val color = if (text.isEmpty()) AppColors.text.secondary else AppColors.text.primary
    val newText = text.ifEmpty { label }

    BoxWithConstraints(
        modifier = modifier.fillMaxWidth()
    ) {
        val isCompact = maxWidth < 360.dp

        Row(
            modifier = modifier
                .fillMaxWidth()
                .background(background, shape)
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                    onClick = { onClick() }
                )
                .padding(start = 2.dp, end = 12.dp)
                .padding(vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(44.dp),
                contentAlignment = Alignment.Center
            ) {
                // Avatar doirasi (border shu yerda)
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .border(1.dp, AppColors.bg.surface, CircleShape)
                        .then(
                            if (hasSubscription) {
                                Modifier.border(2.dp, SuccessColor, CircleShape)
                            } else {
                                Modifier
                            }
                        )
                        .background(AppColors.bg.primaryContainer, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    when {
                        text.isEmpty() -> {
                            Icon(
                                painter = painterResource(Res.drawable.plus_symbol),
                                contentDescription = "",
                                modifier = Modifier.size(16.dp),
                                tint = AppColors.icon.accentPrimary
                            )
                        }

                        imageUrl.isNotEmpty() -> {
                            AsyncImage(
                                model = imageUrl,
                                contentDescription = "",
                                error = painterResource(Res.drawable.profile_hedgehog_img),
                                placeholder = painterResource(Res.drawable.profile_hedgehog_img),
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape)
                            )
                        }

                        else -> {
                            Image(
                                painter = painterResource(Res.drawable.profile_hedgehog_img),
                                contentDescription = "",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape)
                            )
                        }
                    }
                }

                // PLUS badge — avatar Box'dan TASHQARIDA (sibling), shuning uchun border USTIDA chiziladi
                if (hasSubscription) {
                    Image(
                        painter = painterResource(Res.drawable.plus_home_sheet_subscribe),
                        contentDescription = null,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = (-2).dp, y = (-2).dp)
                            .height(18.dp)
                            .width(26.dp)
                    )
                }
            }

            Text(
                text = newText,
                style = textStyle,
                color = color,
                maxLines = 1,
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.width(4.dp))

            if (trailingIcon) {
                Icon(
                    painter = painterResource(Res.drawable.arrow_down_reg),
                    contentDescription = "",
                    modifier = Modifier.size(16.dp),
                    tint = color,
                )
                Spacer(Modifier.width(4.dp))
            }
        }
    }
}

@Preview
@Composable
private fun ChildSelectionButtonPreview() {
    TikonchaParentTheme(ThemeMode.LIGHT) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(ColorWhite)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // 1) Bola tanlanmagan — "+" holati
            ChildSelectionButton(
                text = "",
                label = "Bola tanlang",
                onClick = {},
            )

            // 2) Oddiy bola — obunasiz (badge yoq)
            ChildSelectionButton(
                text = "Ali",
                imageUrl = "",
                onClick = {},
            )

            // 3) PLUS obunali bola — yashil ramka + PLUS badge (rasimdagidek)
            ChildSelectionButton(
                text = "Vali",
                imageUrl = "",
                onClick = {},
                hasSubscription = true
            )
        }
    }
}