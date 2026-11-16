package uz.tikoncha_parent.presentation.profile.child_user_edit

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.Preview
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
import org.koin.core.parameter.parametersOf
import tikoncha_parents.composeapp.generated.resources.*
import uz.tikoncha_parent.presentation.base.CustomButton
import uz.tikoncha_parent.domain.model.GenderType
import uz.tikoncha_parent.domain.model.UserInfo
import uz.tikoncha_parent.presentation.base.CustomHeader
import uz.tikoncha_parent.presentation.base.CustomText
import uz.tikoncha_parent.presentation.base.SegmentedToggle
import uz.tikoncha_parent.presentation.ui_state.ResponseState
import uz.tikoncha_parent.presentation.ui_state.errorText
import uz.tikoncha_parent.ui.theme.AppColors
import uz.tikoncha_parent.ui.theme.ThemeMode
import uz.tikoncha_parent.ui.theme.TikonchaParentTheme
import uz.tikoncha_parent.ui.theme.extendedColor
import uz.tikoncha_parent.ui.theme.rememberScreenSystemBars


class ChildEditScreen(
    private val child: UserInfo
) : Screen {

    @Composable
    override fun Content() {

        val viewModel = koinScreenModel<ChildInfoEditViewModel>{ parametersOf(child) }
        val state = viewModel.state.collectAsStateWithLifecycle()
        val event = viewModel::onEvent

        val navigator = LocalNavigator.current

        LaunchedEffect(state.value.saveState){
            if (state.value.saveState is ResponseState.Success)
                navigator?.pop()
        }

        ChildEditUi(
            navigator = navigator,
            state = state.value,
            event = event
        )
    }
}

@Composable
fun ChildEditUi(
    navigator: Navigator?,
    state: ChildEditState,
    event: (ChildEditEvent) -> Unit
) {
    val selectedGenderIndex = when(state.genderType){
        GenderType.MALE -> 0
        GenderType.FEMALE -> 1
    }

    val loading = state.saveState is ResponseState.Loading
    val error = state.saveState.errorText()
    val showDialog by remember { mutableStateOf(false) }

    LoadingDialog(loading)

    CustomDialog(
        message = error,
        show = showDialog,
        title = stringResource(Res.string.xatolik),
        onDismiss = { event(ChildEditEvent.ClearError) },
        onButtonClick = { !showDialog }
    )

    val systemBars = rememberScreenSystemBars(
        statusBarColor = AppColors.bg.surface,
        navigationBarColor = AppColors.bg.surface
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .then(systemBars.modifier)
            .background(AppColors.bg.secondary)
    ){
        CustomHeader(
            title = stringResource(Res.string.tahrirlash),
            showBackButton = true,
            onBackClick = {
                navigator?.pop()
            }
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(ContainerPadding)
                .imePadding()
        ) {
            SpaceMedium()
            CustomText(
                text = stringResource(Res.string.farzand_malumotini_tahrirlash),
                fontSize = NormalTextSize,
                fontStyle = FontStyle.Normal,
                color = MaterialTheme.extendedColor.hintColor,
                fontWeight = FontWeight.W500,
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
                    label = stringResource(Res.string.ismi),
                    onValueChange = {
                        event(ChildEditEvent.OnFirstName(it))
                    },
                    modifier = Modifier.height(TextFieldHeight),
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words,
                        imeAction = ImeAction.Next
                    ),
                    leadingIcon = {
                        Image(
                            painter = painterResource(Res.drawable.parent),
                            contentDescription = "Parent",
                            colorFilter = ColorFilter.tint(MaterialTheme.extendedColor.primaryColor),
                            modifier = Modifier.size(NormalIconSize)
                        )
                    }
                )
                SpaceMedium()

                CustomTextField(
                    value = state.lastName,
                    label = stringResource(Res.string.familiyasi),
                    onValueChange = {
                        event(ChildEditEvent.OnLastName(it))
                    },
                    modifier = Modifier.height(TextFieldHeight),
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                        imeAction = ImeAction.Next
                    ),
                    leadingIcon = {
                        Image(
                            painter = painterResource(Res.drawable.parent),
                            contentDescription = "Parent",
                            colorFilter = ColorFilter.tint(MaterialTheme.extendedColor.primaryColor),
                            modifier = Modifier.size(NormalIconSize)
                        )
                    }
                )
                SpaceMedium()

                CustomTextField(
                    value = state.patronymic,
                    label = stringResource(Res.string.otasining_ismi),
                    onValueChange = {
                        event(ChildEditEvent.OnPatronymic(it))
                    },
                    modifier = Modifier.height(TextFieldHeight),
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                        imeAction = ImeAction.Next
                    ),
                    leadingIcon = {
                        Image(
                            painter = painterResource(Res.drawable.parent),
                            contentDescription = "Parent",
                            colorFilter = ColorFilter.tint(MaterialTheme.extendedColor.primaryColor),
                            modifier = Modifier.size(NormalIconSize)
                        )
                    }
                )
                SpaceMedium()

                CustomTextField(
                    value = state.age,
                    label = stringResource(Res.string.tugilgan_sanasi),
                    onValueChange = {
                        event(ChildEditEvent.OnAge(it))
                    },
                    modifier = Modifier.height(TextFieldHeight),
                    leadingIcon = {
                        Image(
                            painter = painterResource(Res.drawable.id_card),
                            contentDescription = "Parent",
                            colorFilter = ColorFilter.tint(MaterialTheme.extendedColor.primaryColor),
                            modifier = Modifier.size(NormalIconSize)
                        )
                    }
                )
                SpaceMedium()

                SegmentedToggle(
                    selectedIndex = selectedGenderIndex,
                    containerColor = AppColors.bg.secondarySurface,
                    options = listOf(
                        stringResource(Res.string.ogil_bola) to null,
                        stringResource(Res.string.qiz_bola) to null,
                    ),
                    onOptionSelected = { index ->
                        val gender = if (index == 0) GenderType.MALE else GenderType.FEMALE
                        event(ChildEditEvent.OnGender(gender))
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(ButtonHeight)
                )
            }

            Spacer(modifier = Modifier.weight(1f))
            CustomButton(
                onClick = {
                    event(ChildEditEvent.OnSave)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(ButtonHeight),
                enabled = state.isFormValid,
                text = stringResource(Res.string.saqlash),
                leadingIcon = { uz.tikoncha_parent.presentation.base.SaveIcon() },
            )
        }
    }
}


@Preview
@Composable
private fun Preview() {
    TikonchaParentTheme(
        ThemeMode.DARK
    ){
        ChildEditUi(
            navigator = null,
            state = ChildEditState(),
            event = {}
        )
    }
}