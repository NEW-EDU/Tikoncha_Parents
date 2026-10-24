@file:OptIn(androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class)

package uz.tikoncha_parent.presentation.add_child

import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.toShape
import androidx.compose.material3.MaterialShapes
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import tikoncha_parents.composeapp.generated.resources.Res
import tikoncha_parents.composeapp.generated.resources.mascot_guide
import tikoncha_parents.composeapp.generated.resources.ac_step_show_code
import tikoncha_parents.composeapp.generated.resources.ac_step_phone
import tikoncha_parents.composeapp.generated.resources.ac_step_install
import tikoncha_parents.composeapp.generated.resources.ac_step_enter_code
import tikoncha_parents.composeapp.generated.resources.ac_step_child_enters
import tikoncha_parents.composeapp.generated.resources.ac_hero_title
import tikoncha_parents.composeapp.generated.resources.ac_hero_sub
import tikoncha_parents.composeapp.generated.resources.dialog_success
import tikoncha_parents.composeapp.generated.resources.dialog_warning
import tikoncha_parents.composeapp.generated.resources.farzand_qo_shish
import tikoncha_parents.composeapp.generated.resources.farzand_ulan_di
import tikoncha_parents.composeapp.generated.resources.farzandingiz_tikoncha_ilovasidan_kirib_tasdiqlash
import tikoncha_parents.composeapp.generated.resources.kod_amal_qilish_muddati
import tikoncha_parents.composeapp.generated.resources.kod_muddati_tugadi
import tikoncha_parents.composeapp.generated.resources.kod_nusxalandi
import tikoncha_parents.composeapp.generated.resources.media_play
import tikoncha_parents.composeapp.generated.resources.muvaffaqiyatli
import tikoncha_parents.composeapp.generated.resources.ochish
import tikoncha_parents.composeapp.generated.resources.ok
import tikoncha_parents.composeapp.generated.resources.operator_kodi_topilmadi
import tikoncha_parents.composeapp.generated.resources.sorov_yuborish
import tikoncha_parents.composeapp.generated.resources.tasdiqlash_kodi
import tikoncha_parents.composeapp.generated.resources.ulash_tugma
import tikoncha_parents.composeapp.generated.resources.ulash_usuli_kod
import tikoncha_parents.composeapp.generated.resources.ulash_usuli_telefon
import tikoncha_parents.composeapp.generated.resources.ulashish
import tikoncha_parents.composeapp.generated.resources.xatolik
import tikoncha_parents.composeapp.generated.resources.yuborilmoqda
import uz.tikoncha_parent.platform.copyPlainText
import uz.tikoncha_parent.platform.openUrl
import uz.tikoncha_parent.platform.shareText
import uz.tikoncha_parent.presentation.base.CustomButtonNew
import uz.tikoncha_parent.presentation.base.CustomDialog
import uz.tikoncha_parent.presentation.base.CustomHeader
import uz.tikoncha_parent.presentation.base.LocalToastHost
import uz.tikoncha_parent.presentation.base.ToastData
import uz.tikoncha_parent.presentation.base.ToastProvider
import uz.tikoncha_parent.presentation.base.ToastType
import uz.tikoncha_parent.presentation.base.asText
import uz.tikoncha_parent.presentation.base.multi_phone_input.Country
import uz.tikoncha_parent.presentation.base.multi_phone_input.CountryPhoneInputField
import uz.tikoncha_parent.presentation.otp.OtpInput
import uz.tikoncha_parent.presentation.task.TaskSegmentedToggle
import uz.tikoncha_parent.presentation.video_tutorial.TutorialType
import uz.tikoncha_parent.presentation.video_tutorial.hasTutorialVideo
import uz.tikoncha_parent.presentation.video_tutorial.openTutorial
import uz.tikoncha_parent.ui.NormalIconSize
import uz.tikoncha_parent.ui.Space
import uz.tikoncha_parent.ui.SpaceUltraSmall
import uz.tikoncha_parent.ui.TextFieldCornerRadius
import uz.tikoncha_parent.ui.theme.AppColors
import uz.tikoncha_parent.ui.theme.AppTypography
import uz.tikoncha_parent.ui.theme.ThemeMode
import uz.tikoncha_parent.ui.theme.TikonchaParentTheme
import uz.tikoncha_parent.ui.theme.rememberScreenSystemBars

class AddChildScreen : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow

        val screenModel = koinScreenModel<AddChildViewModel>()
        val state by screenModel.state.collectAsStateWithLifecycle()
        val clipboardManager = LocalClipboard.current
        val coroutineScope = rememberCoroutineScope()

        ToastProvider {

            val toast = LocalToastHost.current


            // --- Effects (bir martalik harakatlar)
            LaunchedEffect(Unit) {
                screenModel.effects.collect { effect ->
                    when (effect) {
                        is AddChildEffect.OpenUrl -> openUrl(effect.url)
                        is AddChildEffect.ShareText -> {
                            val text = "Tikoncha \n${effect.text}"
                            shareText(text)
                        }

                        AddChildEffect.PlayTutorialVideo -> {

                        }

                        AddChildEffect.NavigateBack -> navigator.pop()
                    }
                }
            }

            // --- "Kod nusxalandi" snackbar
            val copiedMsg = stringResource(Res.string.kod_nusxalandi)
            LaunchedEffect(state.showCopiedSnackbar) {
                if (state.showCopiedSnackbar) {
                    toast.show(
                        toast = ToastData(
                            title = copiedMsg,
                            type = ToastType.Info
                        ),
                        durationMs = 1500
                    )
                    screenModel.onEvent(AddChildEvent.DismissSnackbar)
                }
            }

            // --- Error -> CustomDialog
            val errorText = state.error?.asText()
            val showErrorDialog = !errorText.isNullOrBlank()

            CustomDialog(
                painter = painterResource(Res.drawable.dialog_warning),
                show = showErrorDialog,
                title = stringResource(Res.string.xatolik),
                message = errorText.orEmpty(),
                buttonText = stringResource(Res.string.ok),
                showCloseButton = false,
                onDismiss = { screenModel.onEvent(AddChildEvent.DismissError) },
                onButtonClick = { screenModel.onEvent(AddChildEvent.DismissError) },
            )

            CustomDialog(
                painter = painterResource(Res.drawable.dialog_success),
                show = state.linkedChild != null,
                title = stringResource(Res.string.muvaffaqiyatli),
                message = stringResource(Res.string.farzand_ulan_di),
                buttonText = stringResource(Res.string.ok),
                showCloseButton = false,
                onDismiss = { screenModel.onEvent(AddChildEvent.SuccessAcknowledged) },
                onButtonClick = { screenModel.onEvent(AddChildEvent.SuccessAcknowledged) },
            )

            AddChildContent(
                state = state,
                event = { intent ->
                    // Copy intent'da clipboard'ga yozamiz (KMP-friendly)
                    if (intent is AddChildEvent.CodeCopied) {
                        state.code?.let { code ->

                            coroutineScope.launch {
                                copyPlainText(clipboardManager, code)
                            }
                        }
                    }
                    screenModel.onEvent(intent)
                },
                onTutorial = {
                    navigator.openTutorial(TutorialType.BIND_CHILD)
                },
                onBack = {
                    navigator.pop()
                }
            )
        }
    }
}

/* ===================== UI ===================== */

@Composable
private fun AddChildContent(
    state: AddChildState,
    event: (AddChildEvent) -> Unit,
    onTutorial: () -> Unit = {},
    onBack: () -> Unit = {},
) {

    val focusManager = LocalFocusManager.current
    val systemBars = rememberScreenSystemBars(
        statusBarColor = AppColors.bg.page,
        navigationBarColor = AppColors.bg.page
    )
    // Qo'llanma videosi faqat o'zbekcha — boshqa tillarda sahifaning o'zi qadam-baqadam qo'llanma
    val hasVideo = hasTutorialVideo()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .then(systemBars.modifier)
            .background(AppColors.bg.page)
    ) {
        CustomHeader(
            title = stringResource(Res.string.farzand_qo_shish),
            onBackClick = onBack,
            showBackButton = true,
            trailingIcon = {
                if (hasVideo && !state.showBindChildTutorial) {
                    IconButton(
                        onClick = onTutorial,
                        modifier = Modifier.size(44.dp),
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = AppColors.bg.surfaceTertiary,
                            contentColor = AppColors.icon.accentPrimary
                        )
                    ) {
                        Icon(
                            painter = painterResource(Res.drawable.media_play),
                            contentDescription = "",
                            modifier = Modifier.size(NormalIconSize)
                        )
                    }
                    SpaceUltraSmall()
                }
            }
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            AnimatedVisibility(
                visible = hasVideo && state.showBindChildTutorial,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column {
                    Space(8.dp)
                    ConnectChildTutorialCard(onClick = onTutorial)
                }
            }

            Space(8.dp)
            Hero()
            Space(18.dp)

            // Bog'lash usuli: telefonsiz ulanish kodi (standart) yoki telefon raqami
            TaskSegmentedToggle(
                options = listOf(
                    stringResource(Res.string.ulash_usuli_kod) to null,
                    stringResource(Res.string.ulash_usuli_telefon) to null,
                ),
                selectedIndex = if (state.mode == LinkMode.CODE) 0 else 1,
                onOptionSelected = { idx ->
                    focusManager.clearFocus()
                    event(AddChildEvent.ModeChanged(if (idx == 0) LinkMode.CODE else LinkMode.PHONE))
                },
                modifier = Modifier.fillMaxWidth()
            )
            Space(20.dp)

            // 1) Ilovani o'rnatish — havolani ulashish yoki do'konni ochish
            StepRow(number = 1, title = stringResource(Res.string.ac_step_install), active = false) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ShareActionButton(
                        text = stringResource(Res.string.ulashish),
                        icon = Icons.Filled.Share,
                        primary = false,
                        onClick = { event(AddChildEvent.ShareLink) },
                        modifier = Modifier.weight(1f)
                    )
                    ShareActionButton(
                        text = stringResource(Res.string.ochish),
                        icon = Icons.AutoMirrored.Filled.OpenInNew,
                        primary = false,
                        onClick = { event(AddChildEvent.OpenAppLink) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            AnimatedContent(targetState = state.mode, label = "link_mode") { mode ->
                Column {
                    if (mode == LinkMode.CODE) {
                        // 2) Farzand telefonida kodni ko'rsatish  3) Kodni shu yerga kiritish
                        StepRow(number = 2, title = stringResource(Res.string.ac_step_show_code), active = false)
                        StepRow(number = 3, title = stringResource(Res.string.ac_step_enter_code), active = true, isLast = true) {
                            LinkCodeCard(
                                code = state.linkCode,
                                isLoading = state.isLoading,
                                canRedeem = state.canRedeem,
                                onCodeChange = { event(AddChildEvent.LinkCodeChanged(it)) },
                                onRedeem = {
                                    focusManager.clearFocus()
                                    event(AddChildEvent.RedeemCode)
                                }
                            )
                        }
                    } else {
                        // 2) Farzand raqami → kod olish  3) Kodni farzand telefonida kiritish
                        StepRow(number = 2, title = stringResource(Res.string.ac_step_phone), active = !state.showCodeCard) {
                            PhoneInputCard(
                                phone = state.phoneNumber,
                                country = state.selectedCountry,
                                onPhoneChange = { event(AddChildEvent.PhoneChanged(it)) },
                                onCountryChange = { event(AddChildEvent.OnCountryChange(it)) },
                            )
                            if (state.showPrefixHint) {
                                Space(6.dp)
                                Text(
                                    text = stringResource(Res.string.operator_kodi_topilmadi),
                                    style = AppTypography.emphasizedSm,
                                    color = AppColors.text.accentWarning,
                                    modifier = Modifier.fillMaxWidth(),
                                    textAlign = TextAlign.End
                                )
                            }
                            if (!state.showCodeCard) {
                                Space(12.dp)
                                CustomButtonNew(
                                    text = if (state.isLoading) stringResource(Res.string.yuborilmoqda) else stringResource(Res.string.sorov_yuborish),
                                    enabled = state.canRequestCode,
                                    onClick = {
                                        focusManager.clearFocus()
                                        event(AddChildEvent.RequestCode)
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    leadingIcon = if (state.isLoading) {
                                        {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(16.dp),
                                                color = AppColors.text.inverse,
                                                strokeWidth = 2.dp
                                            )
                                        }
                                    } else null
                                )
                            }
                        }
                        StepRow(number = 3, title = stringResource(Res.string.ac_step_child_enters), active = state.showCodeCard, isLast = true) {
                            if (state.showCodeCard && state.code != null) {
                                CodeCard(
                                    code = state.code,
                                    isRefreshing = state.isLoading,
                                    isExpired = state.isCodeExpired,
                                    remainingText = state.remainingText,
                                    onRefresh = { event(AddChildEvent.RefreshCode) },
                                    onCopy = { event(AddChildEvent.CodeCopied) }
                                )
                            }
                        }
                    }
                }
            }
            Space(24.dp)
        }
    }
}

/** Sahifa boshi: tipratikon + nima qilishimiz haqida bir jumla. */
@Composable
private fun Hero() {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Image(
            painter = painterResource(Res.drawable.mascot_guide),
            contentDescription = null,
            modifier = Modifier.size(84.dp)
        )
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = stringResource(Res.string.ac_hero_title),
                style = AppTypography.headlineSmSemiBold.copy(fontWeight = FontWeight.ExtraBold),
                color = AppColors.text.primary
            )
            Space(4.dp)
            Text(
                text = stringResource(Res.string.ac_hero_sub),
                style = AppTypography.emphasizedSmMedium,
                color = AppColors.text.tertiary
            )
        }
    }
}

/**
 * Raqamlangan qadam: chapda doira (faol bo'lsa karamel) va keyingi qadamgacha chiziq,
 * o'ngda sarlavha va (bo'lsa) shu qadamning boshqaruvlari.
 */
@Composable
private fun StepRow(
    number: Int,
    title: String,
    active: Boolean,
    isLast: Boolean = false,
    content: (@Composable () -> Unit)? = null,
) {
    Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(32.dp)) {
            // M3 Expressive: faol qadam raqami 6 qirrali "Cookie" shaklida, kelganda prujina bilan kattalashadi
            val stepShape = if (active) MaterialShapes.Cookie6Sided.toShape() else CircleShape
            val pop by animateFloatAsState(
                targetValue = if (active) 1f else 0.86f,
                animationSpec = MaterialTheme.motionScheme.defaultSpatialSpec(),
                label = "stepPop",
            )
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .graphicsLayer { scaleX = pop; scaleY = pop }
                    .clip(stepShape)
                    .background(if (active) AppColors.button.primary else AppColors.bg.surface)
                    .then(if (active) Modifier else Modifier.border(1.dp, AppColors.border.secondary, CircleShape)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = number.toString(),
                    style = AppTypography.titleSmSemiBold,
                    color = if (active) AppColors.text.onPrimary else AppColors.text.secondary
                )
            }
            if (!isLast) {
                Box(
                    Modifier
                        .padding(vertical = 4.dp)
                        .width(2.dp)
                        .weight(1f)
                        .clip(CircleShape)
                        .background(AppColors.border.secondary)
                )
            }
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f).padding(bottom = if (isLast) 0.dp else 18.dp)) {
            Text(
                text = title,
                style = AppTypography.titleMdSemiBold,
                color = if (active) AppColors.text.primary else AppColors.text.secondary,
                modifier = Modifier.padding(top = 4.dp)
            )
            if (content != null) {
                Space(10.dp)
                content()
            }
        }
    }
}

@Composable
private fun PhoneInputCard(
    phone: String,
    country: Country,
    onPhoneChange: (String) -> Unit,
    onCountryChange: (Country) -> Unit,
) {
    CountryPhoneInputField(
        phoneNumber = phone,
        initialCountry = country,
        onPhoneNumberChange = { onPhoneChange(it) },
        onCountryChange = { onCountryChange(it) },
        modifier = Modifier
            .fillMaxWidth()
            .background(AppColors.bg.surface, RoundedCornerShape(TextFieldCornerRadius)),
    )
}

@Composable
private fun CodeCard(
    code: String,
    isRefreshing: Boolean,
    remainingText: String,
    isExpired: Boolean,
    onRefresh: () -> Unit,
    onCopy: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(AppColors.bg.surface)
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        // Sarlavha qatori — label + refresh tugmasi
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = stringResource(Res.string.tasdiqlash_kodi),
                style = AppTypography.titleSmSemiBold,
                color = AppColors.text.primary
            )
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(AppColors.bg.primaryContainer)
                    .clickable(enabled = !isRefreshing, onClick = onRefresh),
                contentAlignment = Alignment.Center
            ) {
                if (isRefreshing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(14.dp),
                        color = AppColors.icon.accentPrimary,
                        strokeWidth = 1.5.dp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Filled.Refresh,
                        contentDescription = "Yangi kod olish",
                        tint = AppColors.icon.accentPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        Spacer(Modifier.height(10.dp))

        // Kod + copy
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(AppColors.bg.page)
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = formatCode(code),
                style = TextStyle(
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 20.sp,
                    letterSpacing = 2.sp
                ),
                color = AppColors.text.primary
            )
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(AppColors.bg.tertiary)
                    .clickable(onClick = onCopy),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.ContentCopy,
                    contentDescription = "Nusxa olish",
                    tint = AppColors.icon.primary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        if (isExpired) {
            Text(
                text = stringResource(Res.string.kod_muddati_tugadi),
                style = AppTypography.emphasizedSmMedium,
                color = AppColors.text.accentWarning,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.End
            )
        } else if (remainingText != "0:00") {
            Text(
                text = stringResource(Res.string.kod_amal_qilish_muddati, remainingText),
                style = AppTypography.emphasizedSmMedium,
                color = AppColors.text.secondary
            )
        }

        Spacer(Modifier.height(10.dp))

        Text(
            text = stringResource(Res.string.farzandingiz_tikoncha_ilovasidan_kirib_tasdiqlash),
            style = AppTypography.bodyLgRegular,
            color = AppColors.text.secondary
        )
    }
}

@Composable
private fun LinkCodeCard(
    code: String,
    isLoading: Boolean,
    canRedeem: Boolean,
    onCodeChange: (String) -> Unit,
    onRedeem: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(AppColors.bg.surface)
            .padding(16.dp)
    ) {
        OtpInput(
            otpLength = 8,
            otpText = code,
            autoFocus = false,
            onOtpUpdate = onCodeChange
        )
        Spacer(Modifier.height(14.dp))
        CustomButtonNew(
            text = if (isLoading) stringResource(Res.string.yuborilmoqda) else stringResource(Res.string.ulash_tugma),
            enabled = canRedeem,
            onClick = onRedeem,
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = if (isLoading) {
                {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = AppColors.text.inverse,
                        strokeWidth = 2.dp
                    )
                }
            } else null
        )
    }
}

@Composable
private fun ShareActionButton(
    text: String,
    icon: ImageVector,
    primary: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bg = if (primary) AppColors.button.primary else AppColors.button.secondary
    val fg = if (primary) AppColors.text.onPrimary else AppColors.text.primary

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bg)
            .clickable(onClick = onClick)
            .padding(vertical = 11.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = fg,
            modifier = Modifier.size(14.dp)
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = text,
            style = AppTypography.bodyLgSemiBold,
            color = fg
        )
    }
}

private fun formatCode(code: String): String =
    if (code.length == 6 && code.all { it.isDigit() })
        "${code.substring(0, 3)}-${code.substring(3)}"
    else code


@Preview(name = "1. Boshlang'ich · Dark")
@Composable
private fun AddChildPreview_Initial() {
    TikonchaParentTheme(ThemeMode.DARK) {
        AddChildContent(
            state = AddChildState(),
            event = {}
        )
    }
}

@Preview(name = "2. Telefon kiritilgan · Dark")
@Composable
private fun AddChildPreview_PhoneTyped() {
    TikonchaParentTheme(ThemeMode.DARK) {
        AddChildContent(
            state = AddChildState(phoneNumber = "119952666"),
            event = {}
        )
    }
}

@Preview(name = "3. Yuklanmoqda · Dark")
@Composable
private fun AddChildPreview_Loading() {
    TikonchaParentTheme(ThemeMode.DARK) {
        AddChildContent(
            state = AddChildState(
                phoneNumber = "119952666",
                isLoading = true
            ),
            event = {}
        )
    }
}

@Preview(name = "4. Kod tayyor · Dark")
@Composable
private fun AddChildPreview_CodeReady() {
    TikonchaParentTheme(ThemeMode.DARK) {
        AddChildContent(
            state = AddChildState(
                phoneNumber = "119952666",
                requestedPhone = "119952666",
                code = "796961",
                showBindChildTutorial = true
            ),
            event = {}
        )
    }
}

@Preview(name = "5. Kod yangilanmoqda · Dark")
@Composable
private fun AddChildPreview_Refreshing() {
    TikonchaParentTheme(ThemeMode.DARK) {
        AddChildContent(
            state = AddChildState(
                phoneNumber = "119952666",
                requestedPhone = "119952666",
                code = "796961",
                showBindChildTutorial = true,
                isLoading = true     // ↻ icon spinner ko'rsatadi
            ),
            event = {}
        )
    }
}

@Preview(name = "6. Telefon o'zgartirildi (kod gone) · Dark")
@Composable
private fun AddChildPreview_PhoneChangedAfterCode() {
    TikonchaParentTheme(ThemeMode.DARK) {
        AddChildContent(
            state = AddChildState(
                phoneNumber = "119952600",        // o'zgartirilgan
                requestedPhone = "119952666",     // asl request
                code = "796961",                   // state'da bor lekin showCodeCard=false
                showBindChildTutorial = true           // tutorial header'da qoladi
            ),
            event = {}
        )
    }
}

@Preview(name = "7. Kod tayyor · Light")
@Composable
private fun AddChildPreview_CodeReady_Light() {
    TikonchaParentTheme(ThemeMode.LIGHT) {
        AddChildContent(
            state = AddChildState(
                phoneNumber = "119952666",
                requestedPhone = "119952666",
                code = "796961",
                showBindChildTutorial = true
            ),
            event = {}
        )
    }
}