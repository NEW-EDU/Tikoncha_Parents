package uz.tikoncha_parent.presentation.profile.children

import androidx.compose.foundation.layout.PaddingValues
import uz.tikoncha_parent.presentation.base.island.BottomAnchoredColumn
import uz.tikoncha_parent.ui.theme.AppTypography
import uz.tikoncha_parent.ui.rememberCachedImage
import tikoncha_parents.composeapp.generated.resources.ohirgi_faollik
import tikoncha_parents.composeapp.generated.resources.profile_hedgehog_img
import tikoncha_parents.composeapp.generated.resources.Res
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.painterResource
import coil3.compose.AsyncImage
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.draw.clip
import androidx.compose.ui.Alignment
import androidx.compose.material3.Text
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.Navigator
import uz.tikoncha_parent.domain.model.GenderType
import uz.tikoncha_parent.domain.model.UserInfo
import uz.tikoncha_parent.presentation.base.CustomHeader
import uz.tikoncha_parent.presentation.profile.ProfileState
import uz.tikoncha_parent.presentation.profile.ProfileViewModel
import uz.tikoncha_parent.presentation.profile.child_user_edit.ChildEditScreen
import uz.tikoncha_parent.presentation.profile.personal_information.PersonalInfoItem
import uz.tikoncha_parent.ui.ContainerPadding
import uz.tikoncha_parent.ui.theme.AppColors
import uz.tikoncha_parent.ui.theme.ThemeMode
import uz.tikoncha_parent.ui.theme.TikonchaParentTheme
import uz.tikoncha_parent.ui.theme.rememberScreenSystemBars

class ChildDetailScreen(
    private val child: UserInfo
) : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.current
        val viewModel = koinScreenModel<ProfileViewModel>()
        val state by viewModel.state.collectAsStateWithLifecycle()

        LaunchedEffect(Unit) { viewModel.getChildren() }

        val current = state.children.firstOrNull { it.userId == child.userId } ?: child

        ChildDetailUi(
            navigator = navigator,
            child = current,
            onEdit = { navigator?.push(ChildEditScreen(current)) }
        )
    }
}

@Composable
fun ChildDetailUi(
    child: UserInfo,
    navigator: Navigator?,
    onEdit: () -> Unit = {},
) {
    val systemBars = rememberScreenSystemBars(
        statusBarColor = AppColors.bg.secondary,
        navigationBarColor = AppColors.bg.secondary
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .then(systemBars.modifier)
            .background(AppColors.bg.secondary)
    ) {
        CustomHeader(
            title = child.name,
            showBackButton = true,
            onBackClick = { navigator?.pop() }
        )

        BottomAnchoredColumn(
            modifier = Modifier.fillMaxWidth().weight(1f),
            contentPadding = PaddingValues(ContainerPadding),
            spacing = 20.dp,
        ) {
            // Bola "yuzi": avatar + ism — ma'lumotlar guruhidan oldin kimligi darhol ko'rinsin.
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                AsyncImage(
                    model = rememberCachedImage(child.avatarUrl),
                    contentDescription = null,
                    error = painterResource(Res.drawable.profile_hedgehog_img),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(96.dp)
                        .clip(CircleShape)
                        .background(AppColors.bg.primaryContainer, CircleShape),
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    text = listOf(child.name, child.lastName).filter { it.isNotBlank() }.joinToString(" "),
                    style = AppTypography.headlineSmSemiBold.copy(fontWeight = FontWeight.ExtraBold),
                    color = AppColors.text.primary,
                    textAlign = TextAlign.Center,
                )
                if (!child.last_seen.isNullOrBlank()) {
                    Text(
                        text = "${stringResource(Res.string.ohirgi_faollik)} ${child.last_seen}",
                        style = AppTypography.emphasizedSmRegular,
                        color = AppColors.text.tertiary,
                    )
                }
            }
            PersonalInfoItem(
                userInfo = child,
                onEdit = onEdit
            )
        }
    }
}

@Preview
@Composable
private fun PreviewChildrenSelectScreen() {
    TikonchaParentTheme(ThemeMode.LIGHT) {
        ChildDetailUi(
            navigator = null,
            child = UserInfo(
                userId = "1",
                phoneNumber = "+998901234567",
                fullName = "John Doe",
                name = "John",
                lastName = "Doe",
                patronymic = "Smith",
                genderType = GenderType.MALE,
                age = 18
            )
        )
    }
}