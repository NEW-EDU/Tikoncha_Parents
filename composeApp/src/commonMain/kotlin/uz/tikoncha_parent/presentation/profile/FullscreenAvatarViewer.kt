package uz.tikoncha_parent.presentation.profile

import tikoncha_parents.composeapp.generated.resources.parent_avatar_default
import uz.tikoncha_parent.ui.rememberCachedImage
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import coil3.compose.AsyncImage
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import tikoncha_parents.composeapp.generated.resources.Res
import tikoncha_parents.composeapp.generated.resources.close
import tikoncha_parents.composeapp.generated.resources.message_delete
import tikoncha_parents.composeapp.generated.resources.message_edit
import tikoncha_parents.composeapp.generated.resources.profil_rasmi
import tikoncha_parents.composeapp.generated.resources.profile_hedgehog_img
import tikoncha_parents.composeapp.generated.resources.rasmni_ochirish
import tikoncha_parents.composeapp.generated.resources.rasmni_ozgartirish
import tikoncha_parents.composeapp.generated.resources.vertical_menu
import uz.tikoncha_parent.presentation.base.singleClick
import uz.tikoncha_parent.ui.Space
import uz.tikoncha_parent.ui.theme.AppColors
import uz.tikoncha_parent.ui.theme.AppTypography
import uz.tikoncha_parent.ui.theme.ThemeMode
import uz.tikoncha_parent.ui.theme.TikonchaParentTheme
import uz.tikoncha_parent.ui.theme.rememberScreenSystemBars

@Composable
fun FullscreenAvatarViewer(
    show: Boolean = true,
    imageUrL: String?,
    userName: String,
    hasAvatar: Boolean,
    onDismiss: () -> Unit,
    onChangeClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    if (!show) return
    var showMenu by remember { mutableStateOf(false) }
    val systemBars = rememberScreenSystemBars(
        statusBarColor = Color.Black,
        navigationBarColor = Color.Black
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .then(systemBars.modifier)
            .background(Color.Black)
            .pointerInput(Unit) { detectTapGestures { } }
    ) {
        AsyncImage(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(
                    top = 72.dp,
                    bottom = 48.dp,
                    start = 16.dp,
                    end = 16.dp
                ),
            model = rememberCachedImage(imageUrL),
            error = painterResource(Res.drawable.parent_avatar_default),
            contentScale = ContentScale.Fit,
            contentDescription = null
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .size(40.dp)
                    .background(Color.White.copy(alpha = 0.12f), CircleShape)
            ) {
                Icon(
                    painter = painterResource(Res.drawable.close),
                    contentDescription = null,
                    tint = AppColors.icon.inverse,
                    modifier = Modifier.size(12.dp)
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = userName,
                    color = AppColors.text.inverse,
                    style = AppTypography.headlineSm
                )
                Text(
                    text = stringResource(Res.string.profil_rasmi),
                    color = AppColors.text.placeholder,
                    style = AppTypography.titleMdMedium
                )
            }

            Box {
                IconButton(
                    onClick = {
                        showMenu = !showMenu
                    },
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color.White.copy(alpha = 0.12f), CircleShape)
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.vertical_menu),
                        contentDescription = null,
                        tint = AppColors.icon.inverse,
                        modifier = Modifier.size(20.dp)
                    )
                }
                if (showMenu) {
                    Popup(
                        alignment = Alignment.TopEnd,
                        offset = IntOffset(0, 110),
                        onDismissRequest = { showMenu = false },
                        properties = PopupProperties(focusable = true)
                    ) {
                        DropdownMenu(
                            hasAvatar = hasAvatar,
                            onChange = {
                                showMenu = false
                                onChangeClick()
                            },
                            onDelete = {
                                showMenu = false
                                onDeleteClick()
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DropdownMenu(
    hasAvatar: Boolean,
    onChange: () -> Unit,
    onDelete: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(220.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF2A2A2A))
            .padding(6.dp)
    ) {
        DropdownMenuItem(
            icon = painterResource(Res.drawable.message_edit),
            text = stringResource(Res.string.rasmni_ozgartirish),
            onClick = onChange
        )
        if (hasAvatar) {
            Space(4.dp)
            HorizontalDivider(
                color = Color.White.copy(alpha = 0.1f),
                thickness = 0.5.dp,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
            Space(4.dp)
            DropdownMenuItem(
                icon = painterResource(Res.drawable.message_delete),
                text = stringResource(Res.string.rasmni_ochirish),
                iconTint = AppColors.icon.accentDanger,
                textColor = AppColors.text.accentDanger,
                onClick = onDelete
            )
        }
    }
}

@Composable
private fun DropdownMenuItem(
    icon: Painter,
    text: String,
    iconTint: Color = AppColors.icon.inverse,
    textColor: Color = AppColors.text.inverse,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .singleClick { onClick() }
            .padding(horizontal = 12.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            painter = icon,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(18.dp)
        )
        Text(
            text = text,
            color = textColor,
            style = AppTypography.titleSmMedium
        )
    }
}

@Preview
@Composable
private fun AvatarViewer() {
    TikonchaParentTheme(
        ThemeMode.LIGHT
    ) {
        FullscreenAvatarViewer(
            show = true,
            imageUrL = null,
            userName = "Ilhom Isomiddinov",
            hasAvatar = true,
            onDismiss = {},
            onChangeClick = {},
            onDeleteClick = {}
        )
    }
}