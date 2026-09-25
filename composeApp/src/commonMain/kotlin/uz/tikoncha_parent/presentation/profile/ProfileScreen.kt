package uz.tikoncha_parent.presentation.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.Navigator
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import qrgenerator.qrkitpainter.rememberQrKitPainter
import tikoncha_parents.composeapp.generated.resources.Res
import tikoncha_parents.composeapp.generated.resources.add
import tikoncha_parents.composeapp.generated.resources.bekor_qilish
import tikoncha_parents.composeapp.generated.resources.biz_bilan_aloqa
import tikoncha_parents.composeapp.generated.resources.biz_haqimizda
import tikoncha_parents.composeapp.generated.resources.call_filled
import tikoncha_parents.composeapp.generated.resources.chiqish
import tikoncha_parents.composeapp.generated.resources.coin_3d
import tikoncha_parents.composeapp.generated.resources.coins_profile
import tikoncha_parents.composeapp.generated.resources.dialog_failed
import tikoncha_parents.composeapp.generated.resources.family
import tikoncha_parents.composeapp.generated.resources.faol_vazifalar
import tikoncha_parents.composeapp.generated.resources.farzand_qo_shish
import tikoncha_parents.composeapp.generated.resources.farzandlarim
import tikoncha_parents.composeapp.generated.resources.file_3d
import tikoncha_parents.composeapp.generated.resources.global
import tikoncha_parents.composeapp.generated.resources.id_card
import tikoncha_parents.composeapp.generated.resources.info_profile_us
import tikoncha_parents.composeapp.generated.resources.logout
import tikoncha_parents.composeapp.generated.resources.obuna
import tikoncha_parents.composeapp.generated.resources.ochirish
import tikoncha_parents.composeapp.generated.resources.ok
import tikoncha_parents.composeapp.generated.resources.person
import tikoncha_parents.composeapp.generated.resources.profil
import tikoncha_parents.composeapp.generated.resources.profil_rasmi_olib_tashlanadi
import tikoncha_parents.composeapp.generated.resources.rasmni_ochirish
import tikoncha_parents.composeapp.generated.resources.shaxsiy_malumotlar
import tikoncha_parents.composeapp.generated.resources.star_vector
import tikoncha_parents.composeapp.generated.resources.ta
import tikoncha_parents.composeapp.generated.resources.tangachalar
import tikoncha_parents.composeapp.generated.resources.tangachalaringiz
import tikoncha_parents.composeapp.generated.resources.tema
import tikoncha_parents.composeapp.generated.resources.theme_mode
import tikoncha_parents.composeapp.generated.resources.til
import tikoncha_parents.composeapp.generated.resources.tolovlar_tarixi
import tikoncha_parents.composeapp.generated.resources.versiya
import tikoncha_parents.composeapp.generated.resources.xatolik
import uz.tikoncha_parent.data.mapper.toUploadPart
import uz.tikoncha_parent.domain.repository.ChildRepository
import uz.tikoncha_parent.domain.repository.MyCoinsRepository
import uz.tikoncha_parent.domain.repository.PaymentRepository
import uz.tikoncha_parent.platform.decodeImageBitmapOrNull
import uz.tikoncha_parent.platform.getAppVersion
import uz.tikoncha_parent.platform.openUrl
import uz.tikoncha_parent.platform.rememberImagePicker
import uz.tikoncha_parent.presentation.add_child.AddChildScreen
import uz.tikoncha_parent.presentation.base.CustomBottomDialog
import uz.tikoncha_parent.presentation.base.CustomButtonDash
import uz.tikoncha_parent.presentation.base.CustomDialog
import uz.tikoncha_parent.presentation.base.CustomHeader
import uz.tikoncha_parent.presentation.base.LoadingDialog
import uz.tikoncha_parent.presentation.common.TransparentQrScreen
import uz.tikoncha_parent.presentation.profile.about_us.AboutUsScreen
import uz.tikoncha_parent.presentation.profile.children.ChildrenSelectScreen
import uz.tikoncha_parent.presentation.profile.coins.CoinsScreen
import uz.tikoncha_parent.presentation.profile.coins.CoinsViewModel
import uz.tikoncha_parent.presentation.profile.language.LanguageScreen
import uz.tikoncha_parent.presentation.profile.logout.LogoutScreen
import uz.tikoncha_parent.presentation.profile.payment_history.PaymentHistoryScreen
import uz.tikoncha_parent.presentation.profile.personal_information.PersonalInformationScreen
import uz.tikoncha_parent.presentation.profile.settings.theme.ThemeScreen
import uz.tikoncha_parent.presentation.profile.subscription.info.SubscriptionScreen
import uz.tikoncha_parent.presentation.task.TaskListEvent
import uz.tikoncha_parent.presentation.task.TaskListViewModel
import uz.tikoncha_parent.presentation.task.TaskScreen
import uz.tikoncha_parent.presentation.task.model.rememberSharedScreenModel
import uz.tikoncha_parent.presentation.ui_state.ResponseState
import uz.tikoncha_parent.presentation.ui_state.errorText
import uz.tikoncha_parent.ui.ContainerPadding
import uz.tikoncha_parent.ui.ProfileStatsContainerHeight
import uz.tikoncha_parent.ui.SpaceMedium
import uz.tikoncha_parent.ui.theme.AppColors
import uz.tikoncha_parent.ui.theme.ThemeMode
import uz.tikoncha_parent.ui.theme.TikonchaParentTheme
import uz.tikoncha_parent.ui.theme.extendedColor
import uz.tikoncha_parent.ui.theme.rememberScreenSystemBars

class ProfileScreen : Screen {
    @Composable
    override fun Content() {

        val navigator = LocalNavigator.current

        val viewModel = koinScreenModel<ProfileViewModel>()
        val state = viewModel.state.collectAsStateWithLifecycle()
        val event = viewModel::onEvent

        val myCoinsRepo: MyCoinsRepository = koinInject()
        val paymentRepo: PaymentRepository = koinInject()
        val childRepo: ChildRepository = koinInject()
        val coinsViewModel = remember {
            CoinsViewModel(
                myCoinsRepository = myCoinsRepo,
                paymentRepository = paymentRepo,
                childRepository = childRepo
            )
        }
        LaunchedEffect(Unit) {
            viewModel.onEvent(ProfileEvent.Refresh)
        }

        LaunchedEffect(Unit) {
            coinsViewModel.load()
        }

        val ui by coinsViewModel.state.collectAsStateWithLifecycle()
        val aiTokens = ui.myCoins

        val taskViewModel = rememberSharedScreenModel<TaskListViewModel>()
        val taskState by taskViewModel.state.collectAsStateWithLifecycle()

        LaunchedEffect(Unit) {
            taskViewModel.onEvent(TaskListEvent.LoadAllChildrenActiveTasks)
        }
        val activeTasksCount = taskState.allChildrenActiveTaskCount

        ProfileUi(
            navigator = navigator,
            event = event,
            state = state.value,
            aiTokens = aiTokens ?: 0,
            activeTasksCount = activeTasksCount,
        )
    }
}

@Composable
fun ProfileUi(
    navigator: Navigator?,
    state: ProfileState,
    aiTokens: Int,
    activeTasksCount: Int,
    event: (ProfileEvent) -> Unit
) {
    var showQrCode by remember { mutableStateOf(false) }
    var showFullscreenAvatar by remember { mutableStateOf(false) }
    var showDeleteAvatarDialog by remember { mutableStateOf(false) }
    var showDeleteAvatarErrorDialog by remember { mutableStateOf(false) }

    var imageBitmap by remember { mutableStateOf<ImageBitmap?>(null) }
    val launchPicker = rememberImagePicker { picked ->
        val bitmap = decodeImageBitmapOrNull(picked.bytes)
        imageBitmap = bitmap
        event(ProfileEvent.OnAvatarPreviewSelected(bitmap))
        event(ProfileEvent.OnAvatarPhotoSelected(picked.toUploadPart("avatar.jpg")))
    }

    val deleteAvatarLoading = state.deleteAvatarState is ResponseState.Loading
    val deleteAvatarError = state.deleteAvatarState.errorText()
    val deleteAvatarSuccess = state.deleteAvatarState is ResponseState.Success
    val hasAvatar = (state.profileImageUrl?.isNotEmpty() == true) || state.localAvatar != null

    LaunchedEffect(deleteAvatarSuccess) {
        if (deleteAvatarSuccess) {
            showDeleteAvatarDialog = false
            event(ProfileEvent.ClearDeleteAvatarState)
        }
    }

    LaunchedEffect(deleteAvatarError) {
        if (deleteAvatarError.isNotEmpty()) showDeleteAvatarErrorDialog = true
    }

    LoadingDialog(deleteAvatarLoading)

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

    val painter = rememberQrKitPainter(data = "There will be url or smth like this")

    if (showQrCode) {
        TransparentQrScreen(
            painter = painter,
            onDismissRequest = {
                showQrCode = false
            }
        )
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

    val systemBars = rememberScreenSystemBars(
        statusBarColor = AppColors.bg.page,
        navigationBarColor = AppColors.bg.page
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .then(systemBars.modifier)
                .background(AppColors.bg.page)
        ) {
            CustomHeader(
                showBackButton = true,
                title = stringResource(Res.string.profil),
                onBackClick = {
                    navigator?.pop()
                }
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = ContainerPadding)
                    .verticalScroll(rememberScrollState())
            ) {
                Spacer(Modifier.height(16.dp))

                ProfileHeader(
                    state = state,
                    firstName = state.userInfo?.name ?: "",
                    lastName = state.userInfo?.lastName ?: "",
                    fathersName = state.userInfo?.patronymic ?: "",
                    onSelectImageButtonClick = { launchPicker() },
                    onAvatarClick = {
                        if (state.profileImageUrl.isNotEmpty()) {
                            showFullscreenAvatar = true
                        }
                    }
                )
                Spacer(Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    UserStatsItem(
                        icon = painterResource(Res.drawable.coin_3d),
                        title = stringResource(Res.string.tangachalaringiz),
                        value = "$aiTokens ${stringResource(Res.string.ta)}",
                        modifier = Modifier
                            .height(ProfileStatsContainerHeight)
                            .weight(1f),
                        onClick = {
                            navigator?.push(CoinsScreen())
                        }
                    )
                    Spacer(Modifier.width(12.dp))

                    UserStatsItem(
                        icon = painterResource(Res.drawable.file_3d),
                        title = stringResource(Res.string.faol_vazifalar),
                        value = "$activeTasksCount ${stringResource(Res.string.ta)}",
                        modifier = Modifier
                            .height(ProfileStatsContainerHeight)
                            .weight(1f),
                        onClick = {
                            navigator?.push(TaskScreen())
                        }
                    )
                }
                Spacer(Modifier.height(12.dp))

                CustomButtonDash(
                    text = stringResource(Res.string.farzand_qo_shish),
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { navigator?.push(AddChildScreen()) },
                    leadingIcon = {
                        Icon(
                            painter = painterResource(Res.drawable.add),
                            contentDescription = "",
                            tint = AppColors.icon.accentPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                )
                Spacer(Modifier.height(12.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(AppColors.bg.surface, RoundedCornerShape(24.dp))
                        .padding(horizontal = 8.dp)
                ) {
                    ProfileSectionItem(
                        title = stringResource(Res.string.shaxsiy_malumotlar),
                        icon = painterResource(Res.drawable.person),
                        onItemClick = {
                            navigator?.push(PersonalInformationScreen())
                        }
                    )

                    ProfileSectionItem(
                        title = stringResource(Res.string.farzandlarim),
                        icon = painterResource(Res.drawable.family),
                        onItemClick = {
                            navigator?.push(ChildrenSelectScreen())
                        }
                    )

                    ProfileSectionItem(
                        title = stringResource(Res.string.tema),
                        icon = painterResource(Res.drawable.theme_mode),
                        onItemClick = {
                            navigator?.push(ThemeScreen())
                        }
                    )

                    ProfileSectionItem(
                        title = stringResource(Res.string.til),
                        icon = painterResource(Res.drawable.global),
                        onItemClick = {
                            navigator?.push(LanguageScreen())
                        }
                    )

                    ProfileSectionItem(
                        title = stringResource(Res.string.obuna),
                        icon = painterResource(Res.drawable.star_vector),
                        onItemClick = {
                            navigator?.push(SubscriptionScreen())
                        }
                    )

                    ProfileSectionItem(
                        title = stringResource(Res.string.tangachalar),
                        icon = painterResource(Res.drawable.coins_profile),
                        onItemClick = {
                            navigator?.push(CoinsScreen())
                        }
                    )
                    ProfileSectionItem(
                        title = stringResource(Res.string.tolovlar_tarixi),
                        icon = painterResource(Res.drawable.id_card),
                        onItemClick = {
                            navigator?.push(PaymentHistoryScreen())
                        }
                    )

                    ProfileSectionItem(
                        title = stringResource(Res.string.biz_haqimizda),
                        icon = painterResource(Res.drawable.info_profile_us),
                        onItemClick = {
                            navigator?.push(AboutUsScreen())
                        }
                    )

                    ProfileSectionItem(
                        title = stringResource(Res.string.biz_bilan_aloqa),
                        icon = painterResource(Res.drawable.call_filled),
                        onItemClick = {
                            openUrl("https://t.me/tikoncha_support")
                        }
                    )

                    ProfileSectionItem(
                        divider = false,
                        title = stringResource(Res.string.chiqish),
                        icon = painterResource(Res.drawable.logout),
                        iconColor = AppColors.icon.accentDanger,
                        textColor = AppColors.text.accentDanger,
                        onItemClick = {
                            navigator?.push(LogoutScreen())
                        }
                    )
                }

                SpaceMedium()
                val appVersion = if (LocalInspectionMode.current) {
                    "1.0.0"
                } else {
                    getAppVersion()
                }
                Text(
                    text = "${stringResource(Res.string.versiya)}: $appVersion",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.extendedColor.textColor,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
                SpaceMedium()
            }
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
private fun PreviewProfileScreen() {
    TikonchaParentTheme(
        ThemeMode.LIGHT
    ) {
        ProfileUi(
            navigator = null,
            state = ProfileState(),
            aiTokens = 50,
            activeTasksCount = 20,
            event = {}
        )
    }
}
