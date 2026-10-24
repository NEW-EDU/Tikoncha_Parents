package uz.tikoncha_parent.presentation.profile.settings.notification

import androidx.compose.ui.unit.dp
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.Navigator
import uz.tikoncha_parent.presentation.base.CustomHeader
import uz.tikoncha_parent.ui.ButtonHeight
import uz.tikoncha_parent.ui.ContainerPadding
import uz.tikoncha_parent.ui.SpaceLarge
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import tikoncha_parents.composeapp.generated.resources.Res
import tikoncha_parents.composeapp.generated.resources.bildirishnomalar
import tikoncha_parents.composeapp.generated.resources.davom_etish
import uz.tikoncha_parent.presentation.base.CustomButton
import uz.tikoncha_parent.ui.theme.AppColors
import uz.tikoncha_parent.ui.theme.ThemeMode
import uz.tikoncha_parent.ui.theme.TikonchaParentTheme
import uz.tikoncha_parent.ui.theme.extendedColor
import uz.tikoncha_parent.ui.theme.rememberScreenSystemBars

class NotificationSettingsScreen: Screen {
    @Composable
    override fun Content() {

        val navigator = LocalNavigator.current

        NotificationSettingsUi(
            navigator = navigator
        )

    }
}

@Composable
fun NotificationSettingsUi(
    navigator: Navigator?
){

    val notificationStates = remember {
        mutableStateMapOf<NotificationType, Boolean>().apply {
            NotificationType.entries.forEach { this[it] = false }
        }
    }

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
            title = stringResource(Res.string.bildirishnomalar),
            showBackButton = true,
            onBackClick = {
                navigator!!.pop()
            }
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = ContainerPadding)
        ) {
            // Bo'sh joy tepada — sozlamalar tugma bilan birga pastda
            Spacer(
                modifier = Modifier
                    .weight(1f)
            )

            ProfileNotificationItem(
                notificationStates = notificationStates,
                onNotificationChanged = {notificationType ->
                    notificationStates[notificationType] = !notificationStates[notificationType]!!
                }
            )

            Spacer(Modifier.height(16.dp))

            CustomButton(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(ButtonHeight),
                text = stringResource(Res.string.davom_etish),
                onClick = {
                    navigator!!.pop()
                }
            )
            SpaceLarge()
        }
    }
}

@Preview
@Composable
private fun PreviewNotificationsettingsScreen(){
    TikonchaParentTheme(
        ThemeMode.DARK
    ) {
        NotificationSettingsUi(
            navigator = null
        )
    }
}