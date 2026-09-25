package uz.tikoncha_parent.presentation.new_home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import tikoncha_parents.composeapp.generated.resources.Res
import tikoncha_parents.composeapp.generated.resources.arrow_right_rounded
import tikoncha_parents.composeapp.generated.resources.profil
import tikoncha_parents.composeapp.generated.resources.profile_hedgehog_img
import uz.tikoncha_parent.presentation.base.singleClick
import uz.tikoncha_parent.ui.Space
import uz.tikoncha_parent.ui.theme.AppColors
import uz.tikoncha_parent.ui.theme.AppTypography
import uz.tikoncha_parent.ui.theme.TikonchaParentTheme

@Composable
fun ProfileCard(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    imageUrl: String,
    name: String
){
    Row(
        modifier = modifier
            .background(AppColors.modal.primary, CircleShape)
            .singleClick{
                onClick()
            }
            .padding(start = 4.dp, top = 4.dp, end = 8.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically

    ){
        Box(
            modifier = Modifier
                .size(36.dp)
                .border(1.dp, AppColors.bg.surface, CircleShape)
                .background(AppColors.bg.primaryContainer, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            when {
                imageUrl.isNotEmpty() -> {
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = "",
                        error = painterResource(Res.drawable.profile_hedgehog_img),
                        placeholder = painterResource(Res.drawable.profile_hedgehog_img),
                        contentScale = ContentScale.Crop,
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

        Space(8.dp)
        Column(
            modifier = Modifier
                .weight(1f)
        ) {
            Text(
                text = name,
                color = AppColors.text.primary,
                style = AppTypography.titleSmSemiBold,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1
            )
            Text(
                text = stringResource(Res.string.profil),
                color = AppColors.text.secondary,
                style = AppTypography.bodySmRegular
            )

        }
        Space(8.dp)

        Icon(
            painterResource(Res.drawable.arrow_right_rounded),
            contentDescription = "",
            modifier = Modifier
                .size(16.dp),
            tint = AppColors.icon.primary
        )
    }
}

@Composable
@Preview
private fun Prev(){
    TikonchaParentTheme {
        ProfileCard(
            onClick = {},
            imageUrl = "",
            name = "Ibroxim"
        )
    }
}