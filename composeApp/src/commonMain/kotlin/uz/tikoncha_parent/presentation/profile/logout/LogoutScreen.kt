package uz.tikoncha_parent.presentation.profile.logout

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.Navigator
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import tikoncha_parents.composeapp.generated.resources.Res
import tikoncha_parents.composeapp.generated.resources.akkauntdan_chiqish
import tikoncha_parents.composeapp.generated.resources.akkauntdan_chiqmoqchimisiz
import tikoncha_parents.composeapp.generated.resources.bekor_qilish
import tikoncha_parents.composeapp.generated.resources.chiqish
import tikoncha_parents.composeapp.generated.resources.chiqishni_xohlaysizmi
import tikoncha_parents.composeapp.generated.resources.dialog_warning
import tikoncha_parents.composeapp.generated.resources.hisobdan_chiqishni_tasdiqlaysizmi
import tikoncha_parents.composeapp.generated.resources.logout
import tikoncha_parents.composeapp.generated.resources.nimalar_saqlanib_qoladi
import tikoncha_parents.composeapp.generated.resources.ok
import tikoncha_parents.composeapp.generated.resources.profil_va_sozlamalar_saqlanadi
import tikoncha_parents.composeapp.generated.resources.qayta_kirish_telefon_tasdiq
import tikoncha_parents.composeapp.generated.resources.qayta_kirsangiz_tiklanadi
import tikoncha_parents.composeapp.generated.resources.warning_1
import tikoncha_parents.composeapp.generated.resources.xatolik
import uz.tikoncha_parent.AppRestartBus
import uz.tikoncha_parent.presentation.base.CustomBottomDialog
import uz.tikoncha_parent.presentation.base.CustomButtonNew
import uz.tikoncha_parent.presentation.base.CustomDialog
import uz.tikoncha_parent.presentation.base.CustomHeader
import uz.tikoncha_parent.presentation.base.CustomOutlinedButton
import uz.tikoncha_parent.presentation.base.LoadingDialog
import uz.tikoncha_parent.presentation.login.LoginScreen
import uz.tikoncha_parent.presentation.ui_state.ResponseState
import uz.tikoncha_parent.presentation.ui_state.errorText
import uz.tikoncha_parent.ui.CardCornerPadding
import uz.tikoncha_parent.ui.CardCornerRadius
import uz.tikoncha_parent.ui.ContainerPadding
import uz.tikoncha_parent.ui.NormalIconButtonSize
import uz.tikoncha_parent.ui.NormalIconSize
import uz.tikoncha_parent.ui.SpaceLarge
import uz.tikoncha_parent.ui.SpaceMedium
import uz.tikoncha_parent.ui.SpaceSmall
import uz.tikoncha_parent.ui.SpaceUltraSmall
import uz.tikoncha_parent.ui.TextFieldCornerRadius
import uz.tikoncha_parent.ui.theme.AppColors
import uz.tikoncha_parent.ui.theme.AppTypography
import uz.tikoncha_parent.ui.theme.ThemeMode
import uz.tikoncha_parent.ui.theme.TikonchaParentTheme
import uz.tikoncha_parent.ui.theme.rememberScreenSystemBars

class LogoutScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.current
        val viewModel = koinScreenModel<LogoutViewModel>()
        val state by viewModel.state.collectAsStateWithLifecycle()

        LogoutUi(
            navigator = navigator,
            state = state,
            onLogout = viewModel::logout,
            onErrorDismissed = viewModel::clearError,
        )
    }
}

@Composable
fun LogoutUi(
    navigator: Navigator?,
    state: ResponseState<Nothing> = ResponseState.Idle,
    onLogout: () -> Unit = {},
    onErrorDismissed: () -> Unit = {},
) {
    var showConfirmDialog by remember { mutableStateOf(false) }
    var showErrorDialog by remember { mutableStateOf(false) }

    val loading = state is ResponseState.Loading
    val success = state is ResponseState.Success
    val errorText = state.errorText()

    LaunchedEffect(success) {
        if (success) {
            navigator?.replaceAll(LoginScreen())
            AppRestartBus.restart()
        }
    }

    LaunchedEffect(errorText) {
        if (errorText.isNotEmpty()) showErrorDialog = true
    }

    LoadingDialog(loading)

    // Profil ekranidagi bilan bir xil tasdiq oynasi
    CustomBottomDialog(
        show = showConfirmDialog,
        title = stringResource(Res.string.chiqishni_xohlaysizmi),
        message = stringResource(Res.string.hisobdan_chiqishni_tasdiqlaysizmi),
        showCancelButton = true,
        confirmButtonText = stringResource(Res.string.chiqish),
        dismissButtonText = stringResource(Res.string.bekor_qilish),
        confirmButtonColor = AppColors.button.accentDanger,
        onConfirm = {
            showConfirmDialog = false
            onLogout()
        },
        onDismiss = { showConfirmDialog = false }
    )

    CustomDialog(
        painter = painterResource(Res.drawable.dialog_warning),
        show = showErrorDialog,
        title = stringResource(Res.string.xatolik),
        message = errorText,
        buttonText = stringResource(Res.string.ok),
        showCloseButton = false,
        onDismiss = {
            showErrorDialog = false
            onErrorDismissed()
        },
        onButtonClick = {
            showErrorDialog = false
            onErrorDismissed()
        },
    )

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
            title = stringResource(Res.string.akkauntdan_chiqish),
            showBackButton = true,
            onBackClick = { navigator?.pop() }
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = ContainerPadding)
        ) {
            SpaceMedium()

            // Ogohlantirish kartasi
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(CardCornerRadius))
                    .border(1.dp, AppColors.border.accentWarning, RoundedCornerShape(CardCornerRadius))
                    .background(AppColors.bg.surface)
                    .padding(ContainerPadding)
            ) {
                Icon(
                    painter = painterResource(Res.drawable.warning_1),
                    contentDescription = null,
                    tint = AppColors.icon.accentWarning,
                    modifier = Modifier
                        .size(NormalIconButtonSize)
                        .align(Alignment.CenterVertically),
                )
                SpaceSmall()

                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = stringResource(Res.string.akkauntdan_chiqmoqchimisiz),
                        style = AppTypography.titleSmSemiBold,
                        color = AppColors.text.primary
                    )
                    SpaceSmall()
                    Text(
                        text = stringResource(Res.string.qayta_kirish_telefon_tasdiq),
                        style = AppTypography.bodyMdRegular,
                        color = AppColors.text.secondary
                    )
                }
            }

            SpaceLarge()

            // "Nimalar saqlanib qoladi" kartasi
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(TextFieldCornerRadius))
                    .border(1.dp, AppColors.border.accentSuccess, RoundedCornerShape(TextFieldCornerRadius))
                    .background(AppColors.bg.surface)
                    .padding(CardCornerPadding)
            ) {
                Text(
                    text = stringResource(Res.string.nimalar_saqlanib_qoladi),
                    style = AppTypography.titleSmSemiBold,
                    color = AppColors.text.primary
                )
                SpaceUltraSmall()
                HorizontalDivider(
                    modifier = Modifier.fillMaxWidth(0.6f),
                    color = AppColors.border.secondary
                )
                SpaceSmall()

                BulletTex(text = stringResource(Res.string.profil_va_sozlamalar_saqlanadi))
                SpaceSmall()
                BulletTex(text = stringResource(Res.string.qayta_kirsangiz_tiklanadi))
            }

            Spacer(Modifier.weight(1f))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = ContainerPadding),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                CustomOutlinedButton(
                    text = stringResource(Res.string.bekor_qilish),
                    onClick = { navigator?.pop() },
                    modifier = Modifier.weight(1f)
                )
                CustomButtonNew(
                    text = stringResource(Res.string.chiqish),
                    onClick = { showConfirmDialog = true },
                    containerColor = AppColors.button.accentDanger,
                    modifier = Modifier.weight(1f),
                    leadingIcon = {
                        Icon(
                            painter = painterResource(Res.drawable.logout),
                            contentDescription = null,
                            modifier = Modifier.size(NormalIconSize)
                        )
                    }
                )
            }
        }
    }
}

@Preview
@Composable
private fun PreviewLogoutScreen() {
    TikonchaParentTheme(ThemeMode.DARK) {
        LogoutUi(navigator = null)
    }
}