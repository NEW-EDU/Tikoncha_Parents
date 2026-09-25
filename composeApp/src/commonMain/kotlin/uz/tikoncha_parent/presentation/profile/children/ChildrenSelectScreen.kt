package uz.tikoncha_parent.presentation.profile.children

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NoAccounts
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleStartEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.Navigator
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import tikoncha_parents.composeapp.generated.resources.Res
import tikoncha_parents.composeapp.generated.resources.add
import tikoncha_parents.composeapp.generated.resources.child_unlinked_success
import tikoncha_parents.composeapp.generated.resources.dialog_failed
import tikoncha_parents.composeapp.generated.resources.dialog_success
import tikoncha_parents.composeapp.generated.resources.farzand_malumotlari_keyin_korinadi
import tikoncha_parents.composeapp.generated.resources.farzand_qo_shish
import tikoncha_parents.composeapp.generated.resources.farzand_qoshilmagan
import tikoncha_parents.composeapp.generated.resources.farzandlarim
import tikoncha_parents.composeapp.generated.resources.muvaffaqiyatli
import tikoncha_parents.composeapp.generated.resources.ok
import tikoncha_parents.composeapp.generated.resources.unlink_child_action
import tikoncha_parents.composeapp.generated.resources.unlink_child_message
import tikoncha_parents.composeapp.generated.resources.unlink_child_title
import tikoncha_parents.composeapp.generated.resources.xatolik
import uz.tikoncha_parent.domain.model.GenderType
import uz.tikoncha_parent.domain.model.UserInfo
import uz.tikoncha_parent.presentation.add_child.AddChildScreen
import uz.tikoncha_parent.presentation.base.CustomBottomDialog
import uz.tikoncha_parent.presentation.base.CustomButtonDash
import uz.tikoncha_parent.presentation.base.CustomDialog
import uz.tikoncha_parent.presentation.base.CustomHeader
import uz.tikoncha_parent.presentation.base.DashedBorderButton
import uz.tikoncha_parent.presentation.base.LoadingDialog
import uz.tikoncha_parent.presentation.profile.ProfileEvent
import uz.tikoncha_parent.presentation.profile.ProfileState
import uz.tikoncha_parent.presentation.profile.ProfileViewModel
import uz.tikoncha_parent.presentation.ui_state.ResponseState
import uz.tikoncha_parent.presentation.ui_state.errorText
import uz.tikoncha_parent.ui.Space
import uz.tikoncha_parent.ui.theme.AppColors
import uz.tikoncha_parent.ui.theme.AppTypography
import uz.tikoncha_parent.ui.theme.ThemeMode
import uz.tikoncha_parent.ui.theme.TikonchaParentTheme
import uz.tikoncha_parent.ui.theme.rememberScreenSystemBars

class ChildrenSelectScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.current
        val viewModel = koinScreenModel<ProfileViewModel>()
        val state by viewModel.state.collectAsStateWithLifecycle()
        val event = viewModel::onEvent

        LifecycleStartEffect(Unit) {
            event(ProfileEvent.Refresh)
            onStopOrDispose { }
        }

        ChildrenSelectUi(
            navigator = navigator,
            state = state,
            event = event
        )
    }
}

@Composable
fun ChildrenSelectUi(
    navigator: Navigator?,
    state: ProfileState,
    event: (ProfileEvent) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    var showErrorDialog by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    val errorText = state.unlinkState.errorText()
    val systemBars = rememberScreenSystemBars(
        statusBarColor = AppColors.bg.secondary,
        navigationBarColor = AppColors.bg.secondary
    )

    LaunchedEffect(state.unlinkState) {
        if (state.unlinkState is ResponseState.Success) {
            showSuccessDialog = true
        }
    }

    LaunchedEffect(errorText) {
        showErrorDialog = errorText.isNotEmpty()
    }

    LoadingDialog(state.unlinkState is ResponseState.Loading)

    CustomDialog(
        painter = painterResource(Res.drawable.dialog_success),
        show = showSuccessDialog,
        title = stringResource(Res.string.muvaffaqiyatli),
        message = stringResource(Res.string.child_unlinked_success),
        buttonText = stringResource(Res.string.ok),
        showCloseButton = false,
        onDismiss = {
            showSuccessDialog = false
            event(ProfileEvent.ClearUnlinkState)
        },
        onButtonClick = {
            showSuccessDialog = false
            event(ProfileEvent.ClearUnlinkState)
        }
    )


    CustomDialog(
        painter = painterResource(Res.drawable.dialog_failed),
        show = showErrorDialog,
        title = stringResource(Res.string.xatolik),
        message = errorText,
        buttonText = stringResource(Res.string.ok),
        showCloseButton = false,
        onDismiss = {
            showErrorDialog = false
            event(ProfileEvent.ClearUnlinkState)
        },
        onButtonClick = {
            showErrorDialog = false
            event(ProfileEvent.ClearUnlinkState)
        }
    )

    CustomBottomDialog(
        show = showDialog,
        title = stringResource(Res.string.unlink_child_title),
        message = stringResource(Res.string.unlink_child_message, state.unlinkTarget?.name ?: ""),
        confirmButtonText = stringResource(Res.string.unlink_child_action),
        confirmButtonColor = AppColors.button.accentDanger,
        onDismiss = {
            showDialog = false
            event(ProfileEvent.DismissUnlinkDialog)
        },
        onConfirm = {
            showDialog = false
            event(ProfileEvent.ConfirmUnlink)
        }
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .then(systemBars.modifier)
            .background(AppColors.bg.secondary)
    ) {
        CustomHeader(
            title = stringResource(Res.string.farzandlarim),
            showBackButton = true,
            onBackClick = { navigator?.pop() }
        )

        if (state.children.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {

                Icon(
                    imageVector = Icons.Default.NoAccounts,
                    contentDescription = "",
                    tint = AppColors.icon.accentWarningContainer,
                    modifier = Modifier
                        .size(80.dp)
                )
                Space(40.dp)
                val text =
                    "${stringResource(Res.string.farzand_qoshilmagan)} \n ${stringResource(Res.string.farzand_malumotlari_keyin_korinadi)}"
                Text(
                    text = text,
                    color = AppColors.text.secondary,
                    style = AppTypography.titleMdMedium,
                    textAlign = TextAlign.Center
                )
                Space(40.dp)
                DashedBorderButton(
                    text = stringResource(Res.string.farzand_qo_shish),
                    onClick = {
                        navigator?.push(AddChildScreen())
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(state.children, key = { it.userId }) { child ->
                    ChildrenItem(
                        name = child.name,
                        gadget = child.phoneNumber,
                        lastSeen = child.last_seen ?: "",
                        imageUrl = child.avatarUrl ?: "",
                        subscription = child.subscription,
                        onClick = { navigator?.push(ChildDetailScreen(child)) },
                        onMenuClick = {
                            event(ProfileEvent.OnUnlinkClicked(child))
                            showDialog = true
                        }
                    )
                }

                item {
                    Space(16.dp)
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
                }
            }
        }
    }
}

@Preview
@Composable
private fun PreviewChildrenSelectScreen() {
    TikonchaParentTheme(ThemeMode.LIGHT) {
        ChildrenSelectUi(
            navigator = null,
            state = ProfileState(),
            event = {}
        )
    }
}

private val previewChildren = listOf(
    UserInfo(
        userId = "preview-1",
        phoneNumber = "+998110467405",
        fullName = "Abduraxmonov Ahrorbek",
        name = "Ahrorbek",
        lastName = "Abduraxmonov",
        patronymic = "",
        genderType = GenderType.MALE,
        age = 10,
        last_seen = "Bugun 10:05",
        subscription = "PLUS",
    ),
    UserInfo(
        userId = "preview-2",
        phoneNumber = "+998901234567",
        fullName = "Abduraxmonova Madina",
        name = "Madina",
        lastName = "Abduraxmonova",
        patronymic = "",
        genderType = GenderType.FEMALE,
        age = 7,
        last_seen = null,
        subscription = "FREE",
    ),
)

@Preview(name = "Farzandlar bor · Light")
@Composable
private fun PreviewChildrenSelectScreen_WithChildren_Light() {
    TikonchaParentTheme(ThemeMode.LIGHT) {
        ChildrenSelectUi(
            navigator = null,
            state = ProfileState(children = previewChildren),
            event = {}
        )
    }
}

@Preview(name = "Farzandlar bor · Dark")
@Composable
private fun PreviewChildrenSelectScreen_WithChildren_Dark() {
    TikonchaParentTheme(ThemeMode.DARK) {
        ChildrenSelectUi(
            navigator = null,
            state = ProfileState(children = previewChildren),
            event = {}
        )
    }
}