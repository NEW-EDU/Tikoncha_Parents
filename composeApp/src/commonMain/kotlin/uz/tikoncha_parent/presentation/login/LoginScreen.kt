@file:OptIn(androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class)

package uz.tikoncha_parent.presentation.login

import androidx.compose.ui.text.font.FontWeight
import uz.tikoncha_parent.ui.expressivePress
import androidx.compose.material3.ButtonDefaults
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.BorderStroke
import uz.tikoncha_parent.domain.model.auth.SocialProvider
import tikoncha_parents.composeapp.generated.resources.apple_orqali_kirish
import tikoncha_parents.composeapp.generated.resources.google_orqali_kirish
import tikoncha_parents.composeapp.generated.resources.ic_apple_logo
import tikoncha_parents.composeapp.generated.resources.ic_google_g
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.Navigator
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import tikoncha_parents.composeapp.generated.resources.Res
import tikoncha_parents.composeapp.generated.resources.davom_etish
import tikoncha_parents.composeapp.generated.resources.dialog_failed
import tikoncha_parents.composeapp.generated.resources.hedgehog_heart
import tikoncha_parents.composeapp.generated.resources.kirish_subtitle
import tikoncha_parents.composeapp.generated.resources.login_privacy
import tikoncha_parents.composeapp.generated.resources.ok
import tikoncha_parents.composeapp.generated.resources.operator_kodi_topilmadi
import tikoncha_parents.composeapp.generated.resources.telefon_raqami
import tikoncha_parents.composeapp.generated.resources.telegram
import tikoncha_parents.composeapp.generated.resources.telegram_ochilmoqda
import tikoncha_parents.composeapp.generated.resources.telegram_orqali_kirish
import tikoncha_parents.composeapp.generated.resources.telegram_phone_note
import tikoncha_parents.composeapp.generated.resources.xatolik
import tikoncha_parents.composeapp.generated.resources.xush_kelibsiz
import tikoncha_parents.composeapp.generated.resources.yoki
import uz.tikoncha_parent.presentation.base.CustomButtonNew
import uz.tikoncha_parent.presentation.base.CustomDialog
import uz.tikoncha_parent.presentation.base.LoadingDialog
import uz.tikoncha_parent.presentation.base.OnScreenActive
import uz.tikoncha_parent.presentation.base.PhoneNumberInputField
import uz.tikoncha_parent.presentation.base.asText
import uz.tikoncha_parent.presentation.new_home.NewHomeScreen
import uz.tikoncha_parent.presentation.otp.OtpScreen
import uz.tikoncha_parent.presentation.register.RegisterScreen
import uz.tikoncha_parent.ui.ButtonHeight
import uz.tikoncha_parent.ui.Space
import uz.tikoncha_parent.ui.TextFieldCornerRadius
import uz.tikoncha_parent.ui.theme.AppColors
import uz.tikoncha_parent.ui.theme.AppTypography
import uz.tikoncha_parent.ui.theme.ThemeMode
import uz.tikoncha_parent.ui.theme.TikonchaParentTheme
import uz.tikoncha_parent.ui.theme.rememberScreenSystemBars

// Telegram brand rangi — design token emas
private val TelegramBlue = Color(0xFF229ED9)

class LoginScreen : Screen {

    @Composable
    override fun Content() {
        val viewModel = koinScreenModel<LoginViewModel>()
        val state by viewModel.state.collectAsStateWithLifecycle()
        val navigator = LocalNavigator.current

        LaunchedEffect(Unit) {
            viewModel.sideEffects.collect { effect ->
                when (effect) {
                    LoginSideEffect.NavigateToHome -> navigator?.replaceAll(NewHomeScreen())
                    LoginSideEffect.NavigateToRegister -> navigator?.push(RegisterScreen())
                    is LoginSideEffect.NavigateToOtp -> navigator?.push(OtpScreen(effect.phoneNumber))
                }
            }
        }

        LoginUi(
            navigator = navigator,
            state = state,
            event = viewModel::onEvent,
        )
    }
}

@Composable
fun LoginUi(
    navigator: Navigator?,
    state: LoginState,
    event: (LoginEvent) -> Unit,
) {
    val systemBars = rememberScreenSystemBars(
        statusBarColor = AppColors.bg.page,
        navigationBarColor = AppColors.bg.page,
    )
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()

    val errorText: String? = state.error?.asText()

    val dialogError: String? = state.dialogError?.asText()
    LoadingDialog(show = state.isPhoneLoading)
    CustomDialog(
        painter = painterResource(Res.drawable.dialog_failed),
        show = dialogError != null,
        title = stringResource(Res.string.xatolik),
        message = dialogError ?: "",
        buttonText = stringResource(Res.string.ok),
        showCloseButton = false,
        onDismiss = { event(LoginEvent.OnDialogErrorDismissed) },
        onButtonClick = { event(LoginEvent.OnDialogErrorDismissed) },
    )

    OnScreenActive(
        launchedToSettings = state.isTelegramLoading,
        onReturned = { event(LoginEvent.OnTelegramReturned)}
    )

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .then(systemBars.modifier)
            .background(AppColors.bg.page),
    ) {
        // Terms uchun pastda joy qoldirib, qolganini ikki teng yarmga bo'lamiz
        val termsReserve = 64.dp
        val halfHeight = (maxHeight - termsReserve) / 2f

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .imePadding()
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp),
        ) {
            // YUQORI YARM — hero markazda
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = halfHeight),
                contentAlignment = Alignment.Center,
            ) {
                HeroSection(modifier = Modifier.fillMaxWidth(), onMascotTap = { event(LoginEvent.OnMascotTapped) })
            }

            // PASTKI YARM — telegram + telefon/button markazda
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = halfHeight),
                contentAlignment = Alignment.Center,
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Global (uz/ru bo'lmagan) foydalanuvchiga Apple/Google birinchi
                    if (state.socialFirst) {
                        SocialButtons(state, event)
                        Space(12.dp)
                    }
                    TelegramLoginButton(
                        isLoading = state.isTelegramLoading,
                        onClick = { event(LoginEvent.OnTelegramClicked) },
                    )
                    if (!state.socialFirst) {
                        Space(12.dp)
                        SocialButtons(state, event)
                    }
                    if (errorText != null) {
                        Space(4.dp)
                        Text(
                            text = errorText,
                            style = AppTypography.emphasizedSmMedium,
                            color = AppColors.text.accentDanger,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                    Space(12.dp)
                    PhonePermissionNote()

                    if (state.showPhone) {
                        Space(30.dp)
                        OrDivider()
                        Space(30.dp)

                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = stringResource(Res.string.telefon_raqami),
                                style = AppTypography.emphasizedSmMedium,
                                color = AppColors.text.tertiary,
                            )
                            Spacer(Modifier.height(8.dp))
                            PhoneNumberInputField(
                                phoneNumber = state.number,
                                onPhoneNumberChange = { event(LoginEvent.OnNumberInsert(it)) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .onFocusChanged { focus ->
                                        if (focus.isFocused) {
                                            scope.launch {
                                                delay(300)
                                                scrollState.animateScrollTo(scrollState.maxValue)
                                            }
                                        }
                                    }
                                    .background(
                                        AppColors.field.secondary,
                                        RoundedCornerShape(TextFieldCornerRadius),
                                    ),
                            )

                            if (state.showPrefixHint) {
                                Spacer(Modifier.height(6.dp))
                                Text(
                                    text = stringResource(Res.string.operator_kodi_topilmadi),
                                    style = AppTypography.emphasizedSm,
                                    color = AppColors.text.accentWarning,
                                    modifier = Modifier.fillMaxWidth(),
                                    textAlign = TextAlign.End
                                )
                            }
                        }
                        Space(16.dp)
                        CustomButtonNew(
                            onClick = { event(LoginEvent.OnPhoneContinue) },
                            enabled = state.isPhoneNumberValid && !state.isPhoneLoading,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(ButtonHeight),
                            text = stringResource(Res.string.davom_etish),
                        )
                    }
                }
            }

            // TERMS — pastda
            TermsText(modifier = Modifier.padding(horizontal = 16.dp))
            Space(16.dp)
        }
    }
}

@Composable
private fun rememberIsKeyboardOpen(): Boolean {
    val ime = WindowInsets.ime
    val density = LocalDensity.current
    val isOpen by remember { derivedStateOf { ime.getBottom(density) > 0 } }
    return isOpen
}
@Composable
private fun HeroSection(modifier: Modifier = Modifier, onMascotTap: () -> Unit = {}) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Image(
            painter = painterResource(Res.drawable.hedgehog_heart),
            contentDescription = null,
            modifier = Modifier
                .size(100.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onMascotTap,
                ),
        )
        Spacer(Modifier.height(20.dp))
        Text(
            text = stringResource(Res.string.xush_kelibsiz),
            style = AppTypography.headlineMdSemiBold.copy(fontWeight = FontWeight.ExtraBold),
            color = AppColors.text.primary,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = stringResource(Res.string.kirish_subtitle),
            style = AppTypography.emphasizedMdMedium,
            color = AppColors.text.secondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.widthIn(max = 280.dp),
        )
    }
}

@Composable
private fun TelegramLoginButton(
    isLoading: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val source = remember { MutableInteractionSource() }
    Surface(
        onClick = onClick,
        enabled = !isLoading,
        shape = CircleShape,
        color = TelegramBlue,
        interactionSource = source,
        modifier = modifier
            .expressivePress(source)
            .fillMaxWidth()
            .heightIn(min = ButtonDefaults.MediumContainerHeight),
    ) {
        Box(contentAlignment = Alignment.Center) {
            if (isLoading) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        color = AppColors.icon.inverse,
                        strokeWidth = 2.5.dp,
                    )
                    Text(
                        text = stringResource(Res.string.telegram_ochilmoqda),
                        style = AppTypography.titleSmSemiBold,
                        color = AppColors.text.onPrimary,
                    )
                }
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.telegram),
                        contentDescription = null,
                        tint = AppColors.icon.inverse,
                        modifier = Modifier.size(20.dp),
                    )
                    Text(
                        text = stringResource(Res.string.telegram_orqali_kirish),
                        style = AppTypography.titleMdSemiBold,
                        color = AppColors.text.onPrimary,
                    )
                }
            }
        }
    }
}

/** Apple (faqat iOS) va Google. Apple tugmasi Telegram'niki bilan bir xil o'lchamda (Apple HIG). */
@Composable
private fun SocialButtons(state: LoginState, event: (LoginEvent) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        if (state.appleAvailable) {
            ProviderButton(
                text = stringResource(Res.string.apple_orqali_kirish),
                loading = state.loadingProvider == SocialProvider.APPLE,
                // Yorug' mavzuda qora, qorong'ida oq — Apple uslubi
                container = AppColors.text.primary,
                content = AppColors.bg.page,
                border = null,
                onClick = { event(LoginEvent.OnSocialClicked(SocialProvider.APPLE)) },
            ) { tint ->
                Icon(painterResource(Res.drawable.ic_apple_logo), contentDescription = null, tint = tint, modifier = Modifier.size(20.dp))
            }
        }
        ProviderButton(
            text = stringResource(Res.string.google_orqali_kirish),
            loading = state.loadingProvider == SocialProvider.GOOGLE,
            container = AppColors.bg.elevated,
            content = AppColors.text.primary,
            border = AppColors.border.secondary,
            onClick = { event(LoginEvent.OnSocialClicked(SocialProvider.GOOGLE)) },
        ) {
            // Google "G" — rangli, bo'yalmaydi
            Image(painterResource(Res.drawable.ic_google_g), contentDescription = null, modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
private fun ProviderButton(
    text: String,
    loading: Boolean,
    container: Color,
    content: Color,
    border: Color?,
    onClick: () -> Unit,
    icon: @Composable (Color) -> Unit,
) {
    val source = remember { MutableInteractionSource() }
    Surface(
        onClick = onClick,
        enabled = !loading,
        shape = CircleShape,
        color = container,
        border = border?.let { BorderStroke(1.dp, it) },
        interactionSource = source,
        modifier = Modifier.expressivePress(source).fillMaxWidth().heightIn(min = ButtonDefaults.MediumContainerHeight),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (loading) {
                CircularProgressIndicator(modifier = Modifier.size(22.dp), color = content, strokeWidth = 2.5.dp)
            } else {
                icon(content)
                Text(text = text, style = AppTypography.titleMdSemiBold, color = content)
            }
        }
    }
}

@Composable
private fun PhonePermissionNote(modifier: Modifier = Modifier) {
    Text(
        text = stringResource(Res.string.telegram_phone_note),
        style = AppTypography.bodyMdMedium,
        color = AppColors.text.tertiary,
        textAlign = TextAlign.Center,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
    )
}

@Composable
private fun OrDivider(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(Modifier.weight(1f).height(1.dp).background(AppColors.border.secondary))
        Text(
            text = stringResource(Res.string.yoki),
            style = AppTypography.bodyMdSemiBold,
            color = AppColors.text.tertiary,
        )
        Box(Modifier.weight(1f).height(1.dp).background(AppColors.border.secondary))
    }
}

@Composable
private fun TermsText(modifier: Modifier = Modifier) {
    Text(
        text = stringResource(Res.string.login_privacy),
        style = AppTypography.emphasizedXsRegular,
        color = AppColors.text.tertiary,
        textAlign = TextAlign.Center,
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 4.dp, start = 12.dp, end = 12.dp),
    )
}

@Preview
@Composable
private fun PreviewWithPhone() {
    TikonchaParentTheme(ThemeMode.LIGHT) {
        LoginUi(navigator = null, state = LoginState(showPhone = true), event = {})
    }
}

@Preview
@Composable
private fun PreviewTelegramOnly() {
    TikonchaParentTheme(ThemeMode.LIGHT) {
        LoginUi(navigator = null, state = LoginState(showPhone = false), event = {})
    }
}