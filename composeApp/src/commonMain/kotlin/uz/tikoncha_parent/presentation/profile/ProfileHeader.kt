package uz.tikoncha_parent.presentation.profile

import tikoncha_parents.composeapp.generated.resources.parent_avatar_default
import uz.tikoncha_parent.ui.rememberCachedImage
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import uz.tikoncha_parent.ui.ProfileImageSize
import org.jetbrains.compose.resources.painterResource
import tikoncha_parents.composeapp.generated.resources.Res
import tikoncha_parents.composeapp.generated.resources.camera
import tikoncha_parents.composeapp.generated.resources.profile_hedgehog_img
import uz.tikoncha_parent.presentation.base.singleClick
import uz.tikoncha_parent.ui.NormalIconSize
import uz.tikoncha_parent.ui.SpaceSmall
import uz.tikoncha_parent.ui.theme.AppColors
import uz.tikoncha_parent.ui.theme.AppTypography
import uz.tikoncha_parent.ui.theme.ThemeMode
import uz.tikoncha_parent.ui.theme.TikonchaParentTheme

@Composable
fun ProfileHeader(
    lastName: String,
    firstName: String,
    fathersName: String,
    state: ProfileState,
    onAvatarClick: () -> Unit = {},
    onSelectImageButtonClick: () -> Unit
) {

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Box(
            modifier = Modifier.size(ProfileImageSize),
            contentAlignment = Alignment.BottomEnd
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(50))
                    .border(width = 2.dp, color = AppColors.border.tertiary, shape = CircleShape)
                    .singleClick { onAvatarClick() }
            ) {
                AsyncImage(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(50))
                        .background(AppColors.bg.tertiary),
                    model = rememberCachedImage(state.profileImageUrl),
                    error = painterResource(Res.drawable.parent_avatar_default),
                    contentScale = ContentScale.Crop,
                    contentDescription = null
                )
            }

            IconButton(
                modifier = Modifier
                    .clip(CircleShape)
                    .border(2.dp, AppColors.border.secondarySubtle, CircleShape)
                    .background(AppColors.bg.surface)
                    .size(32.dp),
                onClick = {
                    onSelectImageButtonClick()
                },
            ) {
                Icon(
                    painter = painterResource(Res.drawable.camera),
                    contentDescription = "",
                    tint = AppColors.icon.accentPrimary,
                    modifier = Modifier.size(NormalIconSize)
                )
            }
        }

        SpaceSmall()

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = listOf(firstName, lastName, fathersName).filter { it.isNotBlank() }.joinToString(" "),
                style = AppTypography.emphasizedXlSemiBold,
                color = AppColors.text.primary,
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
        ProfileHeader(
            lastName = "Ahmadjonov",
            firstName = "Husniddin",
            fathersName = "Nazirjon o'g'li",
            onSelectImageButtonClick = {},
            state = ProfileState(),
            onAvatarClick = {}
        )
    }
}