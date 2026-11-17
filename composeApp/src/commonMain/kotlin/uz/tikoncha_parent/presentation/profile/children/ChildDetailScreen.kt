package uz.tikoncha_parent.presentation.profile.children

import androidx.compose.foundation.layout.PaddingValues
import uz.tikoncha_parent.presentation.base.island.BottomAnchoredColumn
import uz.tikoncha_parent.ui.theme.AppTypography
import uz.tikoncha_parent.ui.rememberCachedImage
import tikoncha_parents.composeapp.generated.resources.ohirgi_faollik
import tikoncha_parents.composeapp.generated.resources.profile_hedgehog_img
import tikoncha_parents.composeapp.generated.resources.Res
import tikoncha_parents.composeapp.generated.resources.bekor_qilish
import tikoncha_parents.composeapp.generated.resources.child_unlinked_success
import tikoncha_parents.composeapp.generated.resources.dialog_failed
import tikoncha_parents.composeapp.generated.resources.dialog_success
import tikoncha_parents.composeapp.generated.resources.muvaffaqiyatli
import tikoncha_parents.composeapp.generated.resources.ok
import tikoncha_parents.composeapp.generated.resources.unlink_child_action
import tikoncha_parents.composeapp.generated.resources.unlink_child_button
import tikoncha_parents.composeapp.generated.resources.unlink_child_message
import tikoncha_parents.composeapp.generated.resources.unlink_child_title
import tikoncha_parents.composeapp.generated.resources.xatolik
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import uz.tikoncha_parent.presentation.base.CustomButton
import uz.tikoncha_parent.presentation.base.CustomDialog
import uz.tikoncha_parent.presentation.base.LoadingDialog
import uz.tikoncha_parent.presentation.profile.ProfileEvent
import uz.tikoncha_parent.presentation.ui_state.ResponseState
import uz.tikoncha_parent.presentation.ui_state.errorText
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
        val event = viewModel::onEvent

        // ── Farzandni uziş: yozuvli qizil tugma → tasdiq ("Bekor qiliş" birinçi) → natija ──
        var askUnlink by remember { mutableStateOf(false) }
        val unlinkError = state.unlinkState.errorText()

        LoadingDialog(state.unlinkState is ResponseState.Loading)

        if (askUnlink) {
            UnlinkChildSheet(
                childName = current.name,
                onCancel = {
                    askUnlink = false
                    event(ProfileEvent.DismissUnlinkDialog)
                },
                onConfirm = {
                    askUnlink = false
                    event(ProfileEvent.ConfirmUnlink)
                },
            )
        }

        CustomDialog(
            painter = painterResource(Res.drawable.dialog_success),
            show = state.unlinkState is ResponseState.Success,
            title = stringResource(Res.string.muvaffaqiyatli),
            message = stringResource(Res.string.child_unlinked_success),
            buttonText = stringResource(Res.string.ok),
            showCloseButton = false,
            onDismiss = {
                event(ProfileEvent.ClearUnlinkState)
                navigator?.pop()
            },
            onButtonClick = {
                event(ProfileEvent.ClearUnlinkState)
                navigator?.pop()
            },
        )

        CustomDialog(
            painter = painterResource(Res.drawable.dialog_failed),
            show = unlinkError.isNotEmpty(),
            title = stringResource(Res.string.xatolik),
            message = unlinkError,
            buttonText = stringResource(Res.string.ok),
            showCloseButton = false,
            onDismiss = { event(ProfileEvent.ClearUnlinkState) },
            onButtonClick = { event(ProfileEvent.ClearUnlinkState) },
        )

        ChildDetailUi(
            navigator = navigator,
            child = current,
            onEdit = { navigator?.push(ChildEditScreen(current)) },
            onUnlink = {
                event(ProfileEvent.OnUnlinkClicked(current))
                askUnlink = true
            },
        )
    }
}

/** Uziş tasdig'i: oqibat va qanday qayta ulaş aytiladi; xavfsiz tanlov ("Bekor qiliş") — birinçi va asosiy. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UnlinkChildSheet(
    childName: String,
    onCancel: () -> Unit,
    onConfirm: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onCancel,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = AppColors.bg.elevated,
    ) {
        Column(Modifier.padding(start = 20.dp, end = 20.dp, bottom = 28.dp)) {
            Text(
                text = stringResource(Res.string.unlink_child_title),
                style = AppTypography.titleLgSemiBold,
                color = AppColors.text.primary,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = stringResource(Res.string.unlink_child_message, childName),
                style = AppTypography.titleSmMedium,
                color = AppColors.text.secondary,
            )
            Spacer(Modifier.height(20.dp))
            CustomButton(
                text = stringResource(Res.string.bekor_qilish),
                onClick = onCancel,
                modifier = Modifier.fillMaxWidth().height(52.dp),
            )
            Spacer(Modifier.height(10.dp))
            Text(
                text = stringResource(Res.string.unlink_child_action),
                style = AppTypography.titleSmSemiBold,
                color = AppColors.text.accentDanger,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable(onClick = onConfirm)
                    .padding(vertical = 14.dp),
            )
        }
    }
}

@Composable
fun ChildDetailUi(
    child: UserInfo,
    navigator: Navigator?,
    onEdit: () -> Unit = {},
    onUnlink: () -> Unit = {},
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
            // Yozuvli, qizil matnli tugma (belgisiz) — nima qilishi aniq; tasdiqsiz hech narsa uzilmaydi
            Text(
                text = stringResource(Res.string.unlink_child_button),
                style = AppTypography.titleSmSemiBold,
                color = AppColors.text.accentDanger,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable(onClick = onUnlink)
                    .padding(horizontal = 20.dp, vertical = 14.dp),
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