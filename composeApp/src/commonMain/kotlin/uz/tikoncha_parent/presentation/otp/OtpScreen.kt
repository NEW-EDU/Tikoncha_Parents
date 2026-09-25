package uz.tikoncha_parent.presentation.otp

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.Navigator
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import tikoncha_parents.composeapp.generated.resources.Res
import tikoncha_parents.composeapp.generated.resources.davom_etish
import tikoncha_parents.composeapp.generated.resources.dialog_failed
import tikoncha_parents.composeapp.generated.resources.kodni_kiriting
import tikoncha_parents.composeapp.generated.resources.ok
import tikoncha_parents.composeapp.generated.resources.otp_enter_code_with_phone
import tikoncha_parents.composeapp.generated.resources.qayta_yuborish
import tikoncha_parents.composeapp.generated.resources.sahifaga_otish
import tikoncha_parents.composeapp.generated.resources.sekund
import tikoncha_parents.composeapp.generated.resources.xato_kod_kiritdingiz
import tikoncha_parents.composeapp.generated.resources.xatolik
import uz.tikoncha_parent.common.Util.maskPhone
import uz.tikoncha_parent.platform.openUrl
import uz.tikoncha_parent.presentation.base.CustomButton
import uz.tikoncha_parent.presentation.base.CustomDialog
import uz.tikoncha_parent.presentation.base.LoadingDialog
import uz.tikoncha_parent.presentation.base.TopBackButton
import uz.tikoncha_parent.presentation.new_home.NewHomeScreen
import uz.tikoncha_parent.presentation.register.RegisterScreen
import uz.tikoncha_parent.presentation.ui_state.ResponseState
import uz.tikoncha_parent.presentation.ui_state.errorText
import uz.tikoncha_parent.ui.ButtonHeight
import uz.tikoncha_parent.ui.SpaceUltraSmall
import uz.tikoncha_parent.ui.theme.AppColors
import uz.tikoncha_parent.ui.theme.AppTypography
import uz.tikoncha_parent.ui.theme.ThemeMode
import uz.tikoncha_parent.ui.theme.TikonchaParentTheme
import uz.tikoncha_parent.ui.theme.rememberScreenSystemBars

private const val OTP_LENGTH = 6
class OtpScreen(
    private val phoneNumber: String,
) : Screen {

    @Composable
    override fun Content() {
        val viewModel = koinScreenModel<OtpViewmodel>()
        val state by viewModel.state.collectAsStateWithLifecycle()
        val navigator = LocalNavigator.current

        LaunchedEffect(phoneNumber) {
            viewModel.onEvent(OtpEvent.SetPhone(phoneNumber))
        }

        OtpUi(navigator = navigator, state = state, event = viewModel::onEvent)
    }
}

@Composable
fun OtpUi(
    navigator: Navigator?,
    state: OtpState,
    event: (OtpEvent) -> Unit,
) {
    val isOtpCodeValid = state.otpCode.length == 6 && state.otpCode.all { it.isDigit() }
    val maskedPhone = remember(state.phoneNumber) { maskPhone(state.phoneNumber) }
    val formattedTime = state.timeLife.toString().padStart(2, '0')

    val showLoading = state.responseState is ResponseState.Loading || state.isSendingOtp
    val errorText = state.responseState.errorText()
    val isSuccess = state.responseState is ResponseState.Success

    var showErrorDialog by remember { mutableStateOf(false) }
    var hasNavigated by remember { mutableStateOf(false) }

    LaunchedEffect(errorText) {
        if (errorText.isNotEmpty()) showErrorDialog = true
    }

    LoadingDialog(show = showLoading)

    CustomDialog(
        painter = painterResource(Res.drawable.dialog_failed),
        show = showErrorDialog,
        title = stringResource(Res.string.xatolik),
        message = errorText,
        buttonText = if (!state.deleteAccountUrl.isNullOrBlank()) {
            stringResource(Res.string.sahifaga_otish)
        } else {
            stringResource(Res.string.ok)
        },
        showCloseButton = !state.deleteAccountUrl.isNullOrBlank(),
        onDismiss = {
            showErrorDialog = false
            event(OtpEvent.ResetError)
        },
        onButtonClick = {
            showErrorDialog = false
            val url = state.deleteAccountUrl
            if (!url.isNullOrBlank()) openUrl(url) else event(OtpEvent.ResetError)
        },
    )

    LaunchedEffect(isSuccess) {
        if (isSuccess && !hasNavigated) {
            val data = (state.responseState as? ResponseState.Success)?.data
            if (data != null) {
                hasNavigated = true
                if (data.needsRegistration) {
                    navigator?.push(RegisterScreen())
                } else {
                    navigator?.replaceAll(NewHomeScreen())
                }
                event(OtpEvent.Reset)
            }
        }
    }

    val systemBars = rememberScreenSystemBars(
        statusBarColor = AppColors.bg.page,
        navigationBarColor = AppColors.bg.page,
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .then(systemBars.modifier)
            .background(AppColors.bg.page),
        horizontalAlignment = Alignment.Start,
    ) {
        TopBackButton(
            modifier = Modifier.padding(top = 25.dp, start = 16.dp),
            onClick = { navigator?.pop() },
        )

        Spacer(Modifier.fillMaxHeight(0.1f))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 30.dp),
        ) {
            Text(
                text = stringResource(Res.string.kodni_kiriting),
                style = AppTypography.headlineMdSemiBold,
                color = AppColors.text.primary,
            )
            Spacer(Modifier.height(12.dp))

            Text(
                text = stringResource(Res.string.otp_enter_code_with_phone, maskedPhone),
                style = AppTypography.emphasizedMdMedium,
                color = AppColors.text.secondary,
            )
            Spacer(Modifier.height(24.dp))

            val otpTextCode = if (state.hasInputError) {
                AppColors.text.accentDanger
            } else {
                AppColors.text.primary
            }

            OtpInput(
                otpLength = OTP_LENGTH,
                textColor = otpTextCode,
                autoFocus = state.isRunning,
                otpText = state.otpCode,
                onOtpUpdate = {
                    event(OtpEvent.OnOtpUpdate(it))
                }
            )

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(Modifier.height(24.dp))

                if (state.isRunning) {
                    if (state.hasInputError) {
                        Text(
                            text = stringResource(Res.string.xato_kod_kiritdingiz),
                            style = AppTypography.titleSmMedium,
                            color = AppColors.text.accentDanger,
                        )
                        SpaceUltraSmall()
                    }
                    Text(
                        text = stringResource(Res.string.sekund, formattedTime),
                        style = AppTypography.titleSmMedium,
                        color = AppColors.text.accentEmphasis,
                    )
                } else {
                    if (state.hasInputError) {
                        Text(
                            text = stringResource(Res.string.xato_kod_kiritdingiz),
                            style = AppTypography.titleSmMedium,
                            color = AppColors.text.accentDanger,
                        )
                        Spacer(Modifier.height(8.dp))
                    }
                    Text(
                        text = stringResource(Res.string.qayta_yuborish),
                        style = AppTypography.titleSmMedium.copy(textDecoration = TextDecoration.Underline),
                        color = AppColors.text.accentEmphasis,
                        modifier = Modifier.clickable(
                            interactionSource = null,
                            indication = null,
                        ) { event(OtpEvent.SendOtp) },
                    )
                }
            }

            Spacer(Modifier.weight(1f))

            Column(
                modifier = Modifier
                    .imePadding()
                    .padding(bottom = 16.dp),
            ) {
                CustomButton(
                    enabled = isOtpCodeValid && !showLoading,
                    text = stringResource(Res.string.davom_etish),
                    onClick = {
                        if (!showLoading && !hasNavigated) event(OtpEvent.OnConfirmClicked)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(ButtonHeight),
                )
            }
        }
    }
}

@Preview
@Composable
private fun Preview() {
    TikonchaParentTheme(ThemeMode.DARK) {
        OtpUi(navigator = null, state = OtpState(), event = {})
    }
}