package uz.tikoncha_parent.presentation.profile.user_edit

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.Navigator
import uz.tikoncha_parent.presentation.base.CustomDialog
import uz.tikoncha_parent.presentation.base.CustomTextField
import uz.tikoncha_parent.presentation.base.LoadingDialog
import uz.tikoncha_parent.ui.*
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.core.parameter.parametersOf
import tikoncha_parents.composeapp.generated.resources.*
import uz.tikoncha_parent.presentation.base.CustomButton
import uz.tikoncha_parent.domain.model.GenderType
import uz.tikoncha_parent.domain.model.UserInfo
import uz.tikoncha_parent.presentation.base.CustomHeader
import uz.tikoncha_parent.presentation.base.SegmentedToggle
import uz.tikoncha_parent.presentation.ui_state.ResponseState
import uz.tikoncha_parent.presentation.ui_state.errorText
import uz.tikoncha_parent.ui.theme.AppColors
import uz.tikoncha_parent.ui.theme.AppTypography
import uz.tikoncha_parent.ui.theme.ThemeMode
import uz.tikoncha_parent.ui.theme.TikonchaParentTheme
import uz.tikoncha_parent.ui.theme.extendedColor
import uz.tikoncha_parent.ui.theme.rememberScreenSystemBars


class UserEditScreen(
    private val userInfo: UserInfo
) : Screen {

    @Composable
    override fun Content() {
        
        val navigator = LocalNavigator.current

        val viewModel = koinScreenModel<UserInfoEditViewModel>{ parametersOf(userInfo) }
        val state = viewModel.state.collectAsStateWithLifecycle()
        val event = viewModel::onEvent

        LaunchedEffect(state.value.saveState) {
            if (state.value.saveState is ResponseState.Success) {
                navigator?.pop()
            }
        }

        UserEditUi(
            navigator = navigator,
            state = state.value,
            event = event
        )
    }
}

@Composable
fun UserEditUi(
    navigator: Navigator?,
    state: UserEditState,
    event: (UserEditEvent) -> Unit
) {
    val selectedGenderIndex = when(state.genderType){
        GenderType.MALE -> 0
        GenderType.FEMALE -> 1
    }

    val loading = state.saveState is ResponseState.Loading
    val error = state.saveState.errorText()
    val showDialog = !error.isNullOrBlank()

    LoadingDialog(loading)

    CustomDialog(
        message = error,
        show = showDialog,
        title = stringResource(Res.string.xatolik),
        onDismiss = { event(UserEditEvent.ClearError) },
        onButtonClick = { event(UserEditEvent.ClearError) }
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
            .imePadding()
    ){
        CustomHeader(
            showBackButton = true,
            onBackClick = { navigator?.pop() },
            title = stringResource(Res.string.tahrirlash),
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(ContainerPadding)
        ) {
            SpaceMedium()
            Text(
                text = stringResource(Res.string.malumotlarni_tahrirlash),
                style = AppTypography.titleMdSemiBold,
                color = AppColors.text.primary,
            )
            SpaceMedium()

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(AppColors.bg.surfaceTertiary, RoundedCornerShape(24.dp))
                    .padding(12.dp)
            ) {
                CustomTextField(
                    value = state.firstName,
                    style = AppTypography.titleSmMedium,
                    modifier = Modifier.height(TextFieldHeight),
                    label = stringResource(Res.string.ismingizni_kiriting),
                    onValueChange = { event(UserEditEvent.OnFirstName(it)) },
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words,
                        imeAction = ImeAction.Next
                    ),
                    leadingIcon = {
                        Image(
                            painter = painterResource(Res.drawable.parent),
                            contentDescription = "Parent",
                            colorFilter = ColorFilter.tint(MaterialTheme.extendedColor.primaryColor),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                )
                SpaceMedium()

                CustomTextField(
                    value = state.lastName,
                    style = AppTypography.titleSmMedium,
                    modifier = Modifier.height(TextFieldHeight),
                    onValueChange = { event(UserEditEvent.OnLastName(it)) },
                    label = stringResource(Res.string.familiyangizni_kiriting),
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                        imeAction = ImeAction.Next
                    ),
                    leadingIcon = {
                        Image(
                            painter = painterResource(Res.drawable.parent),
                            contentDescription = "Parent",
                            colorFilter = ColorFilter.tint(MaterialTheme.extendedColor.primaryColor),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                )
                SpaceMedium()

                CustomTextField(
                    value = state.patronymic,
                    style = AppTypography.titleSmMedium,
                    modifier = Modifier.height(TextFieldHeight),
                    onValueChange = { event(UserEditEvent.OnPatronymic(it)) },
                    label = stringResource(Res.string.otangizni_ismini_kiriting),
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                        imeAction = ImeAction.Next
                    ),
                    leadingIcon = {
                        Image(
                            painter = painterResource(Res.drawable.parent),
                            contentDescription = "Parent",
                            colorFilter = ColorFilter.tint(MaterialTheme.extendedColor.primaryColor),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                )
                SpaceMedium()

                SegmentedToggle(
                    selectedIndex = selectedGenderIndex,
                    containerColor = AppColors.bg.secondarySurface,
                    options = listOf(
                        stringResource(Res.string.ota) to painterResource(Res.drawable.father_icon),
                        stringResource(Res.string.ona) to painterResource(Res.drawable.mather_icon),
                    ),
                    onOptionSelected = { index ->
                        val gender = if (index == 0) GenderType.MALE else GenderType.FEMALE
                        event(UserEditEvent.OnGender(gender))
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(ButtonHeight)
                )
            }
        }

        CustomButton(
            onClick = {
                event(UserEditEvent.OnSave)
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = ContainerPadding)
                .height(ButtonHeight),
            enabled = state.isFormValid,
            text = stringResource(Res.string.saqlash),
            leadingIcon = { uz.tikoncha_parent.presentation.base.SaveIcon() },
        )
        Space(12.dp)
    }
}


@Preview
@Composable
private fun Preview() {
    TikonchaParentTheme(
        ThemeMode.DARK
    ){
        UserEditUi(
            navigator = null,
            state = UserEditState(),
            event = {}
        )
    }
}