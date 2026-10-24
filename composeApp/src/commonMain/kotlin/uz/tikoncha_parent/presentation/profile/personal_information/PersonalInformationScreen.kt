package uz.tikoncha_parent.presentation.profile.personal_information

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.lifecycle.compose.LifecycleStartEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import tikoncha_parents.composeapp.generated.resources.Res
import tikoncha_parents.composeapp.generated.resources.bekor_qilish
import tikoncha_parents.composeapp.generated.resources.dialog_failed
import tikoncha_parents.composeapp.generated.resources.ochirish
import tikoncha_parents.composeapp.generated.resources.ok
import tikoncha_parents.composeapp.generated.resources.profil_rasmi_olib_tashlanadi
import tikoncha_parents.composeapp.generated.resources.rasmni_ochirish
import tikoncha_parents.composeapp.generated.resources.shaxsiy_malumotlar
import tikoncha_parents.composeapp.generated.resources.xatolik
import uz.tikoncha_parent.data.mapper.toUploadPart
import uz.tikoncha_parent.platform.decodeImageBitmapOrNull
import uz.tikoncha_parent.platform.rememberImagePicker
import uz.tikoncha_parent.presentation.base.CustomBottomDialog
import uz.tikoncha_parent.presentation.base.CustomDialog
import uz.tikoncha_parent.presentation.base.CustomHeader
import uz.tikoncha_parent.presentation.profile.FullscreenAvatarViewer
import uz.tikoncha_parent.presentation.profile.ProfileEvent
import uz.tikoncha_parent.presentation.profile.ProfileHeader
import uz.tikoncha_parent.presentation.profile.ProfileState
import uz.tikoncha_parent.presentation.profile.ProfileViewModel
import uz.tikoncha_parent.presentation.profile.user_edit.UserEditScreen
import uz.tikoncha_parent.presentation.ui_state.ResponseState
import uz.tikoncha_parent.presentation.ui_state.errorText
import uz.tikoncha_parent.ui.ContainerPadding
import uz.tikoncha_parent.ui.SpaceLarge
import uz.tikoncha_parent.ui.SpaceMedium
import uz.tikoncha_parent.ui.theme.AppColors
import uz.tikoncha_parent.ui.theme.ThemeMode
import uz.tikoncha_parent.ui.theme.TikonchaParentTheme
import uz.tikoncha_parent.ui.theme.rememberScreenSystemBars

class   PersonalInformationScreen : Screen {
    @Composable
    override fun Content() {

        val viewModel = koinScreenModel<ProfileViewModel>()
        val state = viewModel.state.collectAsStateWithLifecycle()
        val event = viewModel::onEvent

        LifecycleStartEffect(Unit){
            event(ProfileEvent.Refresh)
            onStopOrDispose {  }
        }

        PersonalInformationUi(
            state = state.value,
            event = event
        )
    }
}

@Composable
fun PersonalInformationUi(
    state: ProfileState,
    event: (ProfileEvent) -> Unit
){
    val navigator = LocalNavigator.current
    var showFullscreenAvatar by remember { mutableStateOf(false) }
    var showDeleteAvatarDialog by remember { mutableStateOf(false) }
    var showDeleteAvatarErrorDialog by remember { mutableStateOf(false) }
    val hasAvatar = (state.profileImageUrl?.isNotEmpty() == true) || state.localAvatar != null
    val deleteAvatarSuccess = state.deleteAvatarState is ResponseState.Success
    val deleteAvatarError = state.deleteAvatarState.errorText()
    var imageBitmap by remember { mutableStateOf<ImageBitmap?>(null) }
    val launchPicker = rememberImagePicker { picked ->
        event(ProfileEvent.OnAvatarPhotoSelected(picked.toUploadPart("avatar.jpg")))
        imageBitmap = decodeImageBitmapOrNull(picked.bytes)
    }

    val systemBars = rememberScreenSystemBars(
        statusBarColor = AppColors.bg.secondary,
        navigationBarColor = AppColors.bg.secondary
    )

    LaunchedEffect(deleteAvatarSuccess) {
        if (deleteAvatarSuccess) {
            showDeleteAvatarDialog = false
            event(ProfileEvent.ClearDeleteAvatarState)
        }
    }

    LaunchedEffect(deleteAvatarError) {
        if (deleteAvatarError.isNotEmpty()) showDeleteAvatarErrorDialog = true
    }

    CustomBottomDialog(
        showCancelButton = true,
        show = showDeleteAvatarDialog,
        title = stringResource(Res.string.rasmni_ochirish),
        message = stringResource(Res.string.profil_rasmi_olib_tashlanadi),
        confirmButtonText = stringResource(Res.string.ochirish),
        dismissButtonText = stringResource(Res.string.bekor_qilish),
        confirmButtonColor = AppColors.button.accentDanger,
        onConfirm = {
            showDeleteAvatarDialog = true
            event(ProfileEvent.RequestDeleteAvatar)
        },
        onDismiss = { showDeleteAvatarDialog = false }
    )

    CustomDialog(
        show = showDeleteAvatarErrorDialog,
        title = stringResource(Res.string.xatolik),
        message = deleteAvatarError,
        buttonText = stringResource(Res.string.ok),
        painter = painterResource(Res.drawable.dialog_failed),
        onDismiss = {
            event(ProfileEvent.ClearDeleteAvatarState)
            showDeleteAvatarErrorDialog = false
        },
        onButtonClick = {
            event(ProfileEvent.ClearDeleteAvatarState)
            showDeleteAvatarErrorDialog = false
        }
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .then(systemBars.modifier)
                .background(AppColors.bg.secondary)
        ) {
            CustomHeader(
                title = stringResource(Res.string.shaxsiy_malumotlar),
                showBackButton = true,
                onBackClick = {
                    navigator?.pop()
                }
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(ContainerPadding),
                verticalArrangement = Arrangement.Bottom,
            ) {
                item {
                    ProfileHeader(
                        state = state,
                        firstName = state.userInfo?.name ?: "",
                        lastName = state.userInfo?.lastName ?: "",
                        fathersName = state.userInfo?.patronymic ?: "",
                        onSelectImageButtonClick = {
                            launchPicker()
                        },
                        onAvatarClick = {
                            if (state.profileImageUrl.isNotEmpty()) {
                                showFullscreenAvatar = true
                            }
                        }
                    )
                    SpaceLarge()
                }

                item {
                    PersonalInfoItem(
                        userInfo = state.userInfo,
                        onEdit = {
                            state.userInfo?.let { user ->
                                navigator?.push(UserEditScreen(user))
                            }
                        }
                    )
                }
            }
            SpaceMedium()
        }

        FullscreenAvatarViewer(
            show = showFullscreenAvatar,
            imageUrL = state.profileImageUrl,
            userName = "${state.userInfo?.name ?: ""} ${state.userInfo?.lastName ?: ""}".trim(),
            hasAvatar = hasAvatar,
            onDismiss = { showFullscreenAvatar = false },
            onChangeClick = {
                showFullscreenAvatar = false
                launchPicker()
            },
            onDeleteClick = {
                showDeleteAvatarDialog = true
            }
        )
    }
}

@Preview
@Composable
private fun PreviewPersonalInformationScreen() {
    TikonchaParentTheme(
        ThemeMode.LIGHT
    ){
        PersonalInformationUi(
            state = ProfileState(),
            event = {}
        )
    }
}