package uz.tikoncha_parent.presentation.profile

import tikoncha_parents.composeapp.generated.resources.text_size_title
import tikoncha_parents.composeapp.generated.resources.pr_voice_help
import tikoncha_parents.composeapp.generated.resources.pr_voice_help_sub
import tikoncha_parents.composeapp.generated.resources.pr_what_is_hoglt
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import uz.tikoncha_parent.presentation.video_tutorial.TutorialType
import uz.tikoncha_parent.presentation.video_tutorial.openTutorial
import uz.tikoncha_parent.presentation.voice_help.VoiceHelpPrefs
import tikoncha_parents.composeapp.generated.resources.pr_section_help
import uz.tikoncha_parent.presentation.tutorial_clips.TutorialClipsScreen
import tikoncha_parents.composeapp.generated.resources.video_qollanma
import tikoncha_parents.composeapp.generated.resources.pr_section_payments
import tikoncha_parents.composeapp.generated.resources.pr_section_prefs
import tikoncha_parents.composeapp.generated.resources.pr_section_account
import uz.tikoncha_parent.ui.SegmentGap
import uz.tikoncha_parent.presentation.profile.subscription.autopay.cards.AutopayCardsScreen
import tikoncha_parents.composeapp.generated.resources.ap_my_cards
import tikoncha_parents.composeapp.generated.resources.call_filled
import tikoncha_parents.composeapp.generated.resources.coins_profile
import tikoncha_parents.composeapp.generated.resources.delete
import tikoncha_parents.composeapp.generated.resources.family
import tikoncha_parents.composeapp.generated.resources.file_text
import tikoncha_parents.composeapp.generated.resources.global
import tikoncha_parents.composeapp.generated.resources.id_card
import tikoncha_parents.composeapp.generated.resources.info_profile_us
import tikoncha_parents.composeapp.generated.resources.logout
import tikoncha_parents.composeapp.generated.resources.media_play
import tikoncha_parents.composeapp.generated.resources.person
import tikoncha_parents.composeapp.generated.resources.profile_variant_a
import tikoncha_parents.composeapp.generated.resources.profile_variant_b
import tikoncha_parents.composeapp.generated.resources.star_vector
import tikoncha_parents.composeapp.generated.resources.theme_mode
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CreditCard
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import kotlinx.coroutines.delay
import uz.tikoncha_parent.ui.theme.AppTypography
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
import tikoncha_parents.composeapp.generated.resources.chiqish
import tikoncha_parents.composeapp.generated.resources.coin_3d
import tikoncha_parents.composeapp.generated.resources.dialog_failed
import tikoncha_parents.composeapp.generated.resources.faol_vazifalar
import tikoncha_parents.composeapp.generated.resources.farzand_qo_shish
import tikoncha_parents.composeapp.generated.resources.farzandlarim
import tikoncha_parents.composeapp.generated.resources.file_3d
import tikoncha_parents.composeapp.generated.resources.ic3d_pr_about
import tikoncha_parents.composeapp.generated.resources.ic3d_pr_cards
import tikoncha_parents.composeapp.generated.resources.ic3d_pr_children
import tikoncha_parents.composeapp.generated.resources.ic3d_pr_coins
import tikoncha_parents.composeapp.generated.resources.ic3d_pr_contact
import tikoncha_parents.composeapp.generated.resources.ic3d_pr_delete_account
import tikoncha_parents.composeapp.generated.resources.ic3d_pr_language
import tikoncha_parents.composeapp.generated.resources.ic3d_pr_logout
import tikoncha_parents.composeapp.generated.resources.ic3d_pr_payments
import tikoncha_parents.composeapp.generated.resources.ic3d_pr_personal
import tikoncha_parents.composeapp.generated.resources.ic3d_pr_screen_colour
import tikoncha_parents.composeapp.generated.resources.ic3d_pr_subscription
import tikoncha_parents.composeapp.generated.resources.ic3d_pr_text_size
import tikoncha_parents.composeapp.generated.resources.ic3d_pr_video_help
import tikoncha_parents.composeapp.generated.resources.akkauntni_o_chirish
import tikoncha_parents.composeapp.generated.resources.obuna
import tikoncha_parents.composeapp.generated.resources.ochirish
import tikoncha_parents.composeapp.generated.resources.ok
import tikoncha_parents.composeapp.generated.resources.profil
import tikoncha_parents.composeapp.generated.resources.profil_rasmi_olib_tashlanadi
import tikoncha_parents.composeapp.generated.resources.rasmni_ochirish
import tikoncha_parents.composeapp.generated.resources.shaxsiy_malumotlar
import tikoncha_parents.composeapp.generated.resources.ta
import tikoncha_parents.composeapp.generated.resources.tangachalar
import tikoncha_parents.composeapp.generated.resources.tangachalaringiz
import tikoncha_parents.composeapp.generated.resources.tema
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
import uz.tikoncha_parent.data.local.AppSettings
import uz.tikoncha_parent.presentation.base.CustomDialog
import uz.tikoncha_parent.presentation.base.CustomHeader
import uz.tikoncha_parent.presentation.base.LoadingDialog
import uz.tikoncha_parent.presentation.common.TransparentQrScreen
import uz.tikoncha_parent.presentation.profile.about_us.AboutUsScreen
import uz.tikoncha_parent.presentation.profile.children.ChildrenSelectScreen
import uz.tikoncha_parent.presentation.profile.coins.CoinsScreen
import uz.tikoncha_parent.presentation.profile.coins.CoinsViewModel
import uz.tikoncha_parent.presentation.profile.language.LanguageScreen
import uz.tikoncha_parent.presentation.support.SupportSheet
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
        // Hali hech qachon yuklanmagan bo'lsa — "0" emas, "—" (keyin son silliq paydo bo'ladi).
        val aiTokens: Int? = ui.myCoins.takeIf { ui.coinsKnown }

        val taskViewModel = rememberSharedScreenModel<TaskListViewModel>()
        val taskState by taskViewModel.state.collectAsStateWithLifecycle()

        LaunchedEffect(Unit) {
            taskViewModel.onEvent(TaskListEvent.LoadAllChildrenActiveTasks)
        }
        val activeTasksCount: Int? = taskState.allChildrenActiveTaskCount
            .takeIf { taskState.allChildrenActiveTaskCountKnown }

        ProfileUi(
            navigator = navigator,
            event = event,
            state = state.value,
            aiTokens = aiTokens,
            activeTasksCount = activeTasksCount,
        )
    }
}

@Composable
fun ProfileUi(
    navigator: Navigator?,
    state: ProfileState,
    aiTokens: Int?,
    activeTasksCount: Int?,
    event: (ProfileEvent) -> Unit
) {
    var showQrCode by remember { mutableStateOf(false) }
    var showSupport by remember { mutableStateOf(false) }
    if (showSupport) SupportSheet(onDismiss = { showSupport = false })
    var showFullscreenAvatar by remember { mutableStateOf(false) }
    var showDeleteAvatarDialog by remember { mutableStateOf(false) }
    var showDeleteAvatarErrorDialog by remember { mutableStateOf(false) }

    // A/B: menyu körinişi. Versiya matnini uzoq bosiş almaştiradi (yaşirin, suhbatlarda sinaş uçun).
    val profileVariant by ProfileVariantController.variant.collectAsState()
    val haptic = LocalHapticFeedback.current
    var showVariantToast by remember { mutableStateOf(false) }
    var variantToastKey by remember { mutableStateOf(0) }
    LaunchedEffect(variantToastKey) {
        if (variantToastKey > 0) {
            delay(2000)
            showVariantToast = false
        }
    }

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
                        segmentIndex = 0,
                        segmentCount = 2,
                        icon = painterResource(Res.drawable.coin_3d),
                        title = stringResource(Res.string.tangachalaringiz),
                        value = aiTokens?.let { "$it ${stringResource(Res.string.ta)}" } ?: "—",
                        modifier = Modifier
                            .height(ProfileStatsContainerHeight)
                            .weight(1f),
                        onClick = {
                            navigator?.push(CoinsScreen())
                        }
                    )
                    Spacer(Modifier.width(SegmentGap))

                    UserStatsItem(
                        segmentIndex = 1,
                        segmentCount = 2,
                        icon = painterResource(Res.drawable.file_3d),
                        title = stringResource(Res.string.faol_vazifalar),
                        value = activeTasksCount?.let { "$it ${stringResource(Res.string.ta)}" } ?: "—",
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

                when (profileVariant) {
                    ProfileVariant.FLAT -> ProfileMenuFlat(
                        navigator = navigator,
                        onContactClick = { showSupport = true },
                    )
                    ProfileVariant.ICONS_3D -> ProfileMenu3d(
                        navigator = navigator,
                        onContactClick = { showSupport = true },
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
                    modifier = Modifier
                        .fillMaxWidth()
                        // Yaşirin almaştirgiç: uzoq bosiş → A (oddiy) ↔ B (3D). Saqlanadi, release'da ham işlaydi.
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onLongPress = {
                                    ProfileVariantController.toggle()
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    showVariantToast = true
                                    variantToastKey++
                                }
                            )
                        },
                    textAlign = TextAlign.Center
                )
                SpaceMedium()
            }
        }

        AnimatedVisibility(
            visible = showVariantToast,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 32.dp),
        ) {
            Text(
                text = stringResource(
                    if (profileVariant == ProfileVariant.FLAT) Res.string.profile_variant_a
                    else Res.string.profile_variant_b
                ),
                style = AppTypography.titleSmMedium,
                color = MaterialTheme.colorScheme.inverseOnSurface,
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(MaterialTheme.colorScheme.inverseSurface)
                    .padding(horizontal = 16.dp, vertical = 10.dp),
            )
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

/** Variant A (standart): ixçam ro'yxat — [ProfileSectionItem] qatorlari, kiçik tekis ikonkalar. */
@Composable
private fun ProfileMenuFlat(
    navigator: Navigator?,
    onContactClick: () -> Unit,
) {
    val voiceHelpOn by VoiceHelpPrefs.enabled.collectAsState()
    ProfileSectionLabel(stringResource(Res.string.pr_section_account))
    Column(verticalArrangement = Arrangement.spacedBy(SegmentGap)) {
        ProfileSectionItem(
            segmentIndex = 0,
            segmentCount = 2,
            title = stringResource(Res.string.shaxsiy_malumotlar),
            icon = painterResource(Res.drawable.person),
            onItemClick = {
                navigator?.push(PersonalInformationScreen())
            }
        )
        ProfileSectionItem(
            segmentIndex = 1,
            segmentCount = 2,
            title = stringResource(Res.string.farzandlarim),
            icon = painterResource(Res.drawable.family),
            onItemClick = {
                navigator?.push(ChildrenSelectScreen())
            }
        )
    }
    ProfileSectionLabel(stringResource(Res.string.pr_section_prefs))
    Column(verticalArrangement = Arrangement.spacedBy(SegmentGap)) {
        ProfileSectionItem(
            segmentIndex = 0,
            segmentCount = 4,
            title = stringResource(Res.string.tema),
            icon = painterResource(Res.drawable.theme_mode),
            onItemClick = {
                navigator?.push(ThemeScreen())
            }
        )
        // Matn o'lchami — Sozlamalar ekrani hech qayerdan ochilmaydi, shuning uchun shu yerda
        ProfileSectionItem(
            segmentIndex = 1,
            segmentCount = 4,
            title = stringResource(Res.string.text_size_title),
            icon = painterResource(Res.drawable.file_text),
            onItemClick = {
                navigator?.push(uz.tikoncha_parent.presentation.profile.settings.text_size.TextSizeScreen())
            }
        )
        ProfileSectionItem(
            segmentIndex = 2,
            segmentCount = 4,
            title = stringResource(Res.string.til),
            icon = painterResource(Res.drawable.global),
            onItemClick = {
                navigator?.push(LanguageScreen())
            }
        )
        // Ovozli yordam: har ekrandagi «Tinglaş» tugmasi (öçirilsa — hamma ekranda yaşirinadi)
        ProfileSectionItem(
            segmentIndex = 3,
            segmentCount = 4,
            title = stringResource(Res.string.pr_voice_help),
            subtitle = stringResource(Res.string.pr_voice_help_sub),
            icon = rememberVectorPainter(Icons.AutoMirrored.Rounded.VolumeUp),
            checked = voiceHelpOn,
            onItemClick = { VoiceHelpPrefs.set(!voiceHelpOn) }
        )
    }
    ProfileSectionLabel(stringResource(Res.string.pr_section_payments))
    Column(verticalArrangement = Arrangement.spacedBy(SegmentGap)) {
        ProfileSectionItem(
            segmentIndex = 0,
            segmentCount = 4,
            title = stringResource(Res.string.obuna),
            icon = painterResource(Res.drawable.star_vector),
            onItemClick = {
                navigator?.push(SubscriptionScreen())
            }
        )
        // Obunasiz ham: saqlangan kartani ko'rish, asosiy qilish, o'chirish
        ProfileSectionItem(
            segmentIndex = 1,
            segmentCount = 4,
            title = stringResource(Res.string.ap_my_cards),
            icon = rememberVectorPainter(Icons.Rounded.CreditCard),
            onItemClick = {
                navigator?.push(AutopayCardsScreen())
            }
        )
        ProfileSectionItem(
            segmentIndex = 2,
            segmentCount = 4,
            title = stringResource(Res.string.tangachalar),
            icon = painterResource(Res.drawable.coins_profile),
            onItemClick = {
                navigator?.push(CoinsScreen())
            }
        )
        ProfileSectionItem(
            segmentIndex = 3,
            segmentCount = 4,
            title = stringResource(Res.string.tolovlar_tarixi),
            icon = painterResource(Res.drawable.id_card),
            onItemClick = {
                navigator?.push(PaymentHistoryScreen())
            }
        )
    }
    ProfileSectionLabel(stringResource(Res.string.pr_section_help))
    Column(verticalArrangement = Arrangement.spacedBy(SegmentGap)) {
        // Ilovadagi qo'llanma kliplari (haqiqiy ekranlar, qayerga bosiş körsatilgan)
        ProfileSectionItem(
            segmentIndex = 0,
            segmentCount = 4,
            title = stringResource(Res.string.video_qollanma),
            icon = painterResource(Res.drawable.media_play),
            onItemClick = { navigator?.push(TutorialClipsScreen()) }
        )
        // «Hoglt nima?» — tanişuvni qayta oçadi (ilgari Bosh sahifaning tepasidagi ▶ tugma)
        ProfileSectionItem(
            segmentIndex = 1,
            segmentCount = 4,
            title = stringResource(Res.string.pr_what_is_hoglt),
            icon = painterResource(Res.drawable.media_play),
            onItemClick = { navigator?.openTutorial(TutorialType.TIKONCHA) }
        )
        ProfileSectionItem(
            segmentIndex = 2,
            segmentCount = 4,
            title = stringResource(Res.string.biz_haqimizda),
            icon = painterResource(Res.drawable.info_profile_us),
            onItemClick = {
                navigator?.push(AboutUsScreen())
            }
        )
        ProfileSectionItem(
            segmentIndex = 3,
            segmentCount = 4,
            title = stringResource(Res.string.biz_bilan_aloqa),
            icon = painterResource(Res.drawable.call_filled),
            onItemClick = onContactClick
        )
    }
    Spacer(Modifier.height(20.dp))
    Column(verticalArrangement = Arrangement.spacedBy(SegmentGap)) {
        ProfileSectionItem(
            segmentIndex = 0,
            segmentCount = 2,
            title = stringResource(Res.string.chiqish),
            icon = painterResource(Res.drawable.logout),
            iconColor = AppColors.icon.accentDanger,
            textColor = AppColors.text.accentDanger,
            onItemClick = {
                navigator?.push(LogoutScreen())
            }
        )
        // Akkauntni öçiriş — App Store talabi (5.1.1(v)): ilova içida topiladigan bölişi şart.
        // Ilgari faqat Sozlamalar ekranida edi, u esa hech qayerdan oçilmaydi.
        ProfileSectionItem(
            segmentIndex = 1,
            segmentCount = 2,
            title = stringResource(Res.string.akkauntni_o_chirish),
            icon = painterResource(Res.drawable.delete),
            iconColor = AppColors.icon.accentDanger,
            textColor = AppColors.text.accentDanger,
            onItemClick = {
                navigator?.push(uz.tikoncha_parent.presentation.profile.logout.DeleteAccountScreen())
            }
        )
    }
}

/** Variant B: keng qatorlar, 60dp 3D rasmlar ([ProfileTileGroup], `ic3d_pr_*`). */
@Composable
private fun ProfileMenu3d(
    navigator: Navigator?,
    onContactClick: () -> Unit,
) {
    val voiceHelpOn by VoiceHelpPrefs.enabled.collectAsState()
    ProfileSectionLabel(stringResource(Res.string.pr_section_account))
    ProfileTileGroup(
        listOf(
            ProfileTile(
                title = stringResource(Res.string.shaxsiy_malumotlar),
                icon = painterResource(Res.drawable.ic3d_pr_personal),
                onClick = { navigator?.push(PersonalInformationScreen()) },
            ),
            ProfileTile(
                title = stringResource(Res.string.farzandlarim),
                icon = painterResource(Res.drawable.ic3d_pr_children),
                onClick = { navigator?.push(ChildrenSelectScreen()) },
            ),
        )
    )
    ProfileSectionLabel(stringResource(Res.string.pr_section_prefs))
    ProfileTileGroup(
        listOf(
            ProfileTile(
                title = stringResource(Res.string.tema),
                icon = painterResource(Res.drawable.ic3d_pr_screen_colour),
                onClick = { navigator?.push(ThemeScreen()) },
            ),
            // Matn o'lchami — Sozlamalar ekrani hech qayerdan ochilmaydi, shuning uchun shu yerda
            ProfileTile(
                title = stringResource(Res.string.text_size_title),
                icon = painterResource(Res.drawable.ic3d_pr_text_size),
                onClick = {
                    navigator?.push(uz.tikoncha_parent.presentation.profile.settings.text_size.TextSizeScreen())
                },
            ),
            ProfileTile(
                title = stringResource(Res.string.til),
                icon = painterResource(Res.drawable.ic3d_pr_language),
                onClick = { navigator?.push(LanguageScreen()) },
            ),
            // Ovozli yordam: 3D rasm yöq — rasm örnida switch
            ProfileTile(
                title = stringResource(Res.string.pr_voice_help),
                subtitle = stringResource(Res.string.pr_voice_help_sub),
                icon = null,
                checked = voiceHelpOn,
                onClick = { VoiceHelpPrefs.set(!voiceHelpOn) },
            ),
        )
    )
    ProfileSectionLabel(stringResource(Res.string.pr_section_payments))
    ProfileTileGroup(
        listOf(
            ProfileTile(
                title = stringResource(Res.string.obuna),
                icon = painterResource(Res.drawable.ic3d_pr_subscription),
                onClick = { navigator?.push(SubscriptionScreen()) },
            ),
            // Obunasiz ham: saqlangan kartani ko'rish, asosiy qilish, o'chirish
            ProfileTile(
                title = stringResource(Res.string.ap_my_cards),
                icon = painterResource(Res.drawable.ic3d_pr_cards),
                onClick = { navigator?.push(AutopayCardsScreen()) },
            ),
            ProfileTile(
                title = stringResource(Res.string.tangachalar),
                icon = painterResource(Res.drawable.ic3d_pr_coins),
                onClick = { navigator?.push(CoinsScreen()) },
            ),
            ProfileTile(
                title = stringResource(Res.string.tolovlar_tarixi),
                icon = painterResource(Res.drawable.ic3d_pr_payments),
                onClick = { navigator?.push(PaymentHistoryScreen()) },
            ),
        )
    )
    ProfileSectionLabel(stringResource(Res.string.pr_section_help))
    ProfileTileGroup(
        listOf(
            // Ilovadagi qo'llanma kliplari (haqiqiy ekranlar, qayerga bosiş körsatilgan)
            ProfileTile(
                title = stringResource(Res.string.video_qollanma),
                icon = painterResource(Res.drawable.ic3d_pr_video_help),
                onClick = { navigator?.push(TutorialClipsScreen()) },
            ),
            // «Hoglt nima?» — tanişuvni qayta oçadi (ilgari Bosh sahifaning tepasidagi ▶ tugma)
            ProfileTile(
                title = stringResource(Res.string.pr_what_is_hoglt),
                icon = painterResource(Res.drawable.ic3d_pr_video_help),
                onClick = { navigator?.openTutorial(TutorialType.TIKONCHA) },
            ),
            ProfileTile(
                title = stringResource(Res.string.biz_haqimizda),
                icon = painterResource(Res.drawable.ic3d_pr_about),
                onClick = { navigator?.push(AboutUsScreen()) },
            ),
            ProfileTile(
                title = stringResource(Res.string.biz_bilan_aloqa),
                icon = painterResource(Res.drawable.ic3d_pr_contact),
                onClick = onContactClick,
            ),
        )
    )
    Spacer(Modifier.height(28.dp))
    // Çiqiş va Akkauntni öçiriş — alohida, qizil plitkalar
    ProfileTileGroup(
        listOf(
            ProfileTile(
                title = stringResource(Res.string.chiqish),
                icon = painterResource(Res.drawable.ic3d_pr_logout),
                onClick = { navigator?.push(LogoutScreen()) },
            ),
            // Akkauntni öçiriş — App Store talabi (5.1.1(v)): ilova içida topiladigan bölişi şart.
            // Ilgari faqat Sozlamalar ekranida edi, u esa hech qayerdan oçilmaydi.
            ProfileTile(
                title = stringResource(Res.string.akkauntni_o_chirish),
                icon = painterResource(Res.drawable.ic3d_pr_delete_account),
                onClick = {
                    navigator?.push(uz.tikoncha_parent.presentation.profile.logout.DeleteAccountScreen())
                },
            ),
        ),
        danger = true,
    )
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
