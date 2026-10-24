package uz.tikoncha_parent.presentation.profile.settings

import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.Navigator
import uz.tikoncha_parent.presentation.base.CustomHeader
import uz.tikoncha_parent.presentation.profile.settings.theme.ThemeScreen
import uz.tikoncha_parent.ui.ContainerPadding
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import tikoncha_parents.composeapp.generated.resources.Res
import tikoncha_parents.composeapp.generated.resources.sozlamalar
import uz.tikoncha_parent.presentation.profile.logout.LogoutScreen
import uz.tikoncha_parent.ui.theme.AppColors
import uz.tikoncha_parent.ui.theme.ThemeMode
import uz.tikoncha_parent.ui.theme.TikonchaParentTheme
import uz.tikoncha_parent.ui.theme.extendedColor
import uz.tikoncha_parent.ui.theme.rememberScreenSystemBars

class SettingsScreen: Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.current
        SettingsUi(navigator = navigator)
    }
}

@Composable
fun SettingsUi(
    navigator: Navigator?
){
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
            title = stringResource(Res.string.sozlamalar),
            showBackButton = true,
            onBackClick = {
                navigator?.pop()
            }
        )

        // Ikki qator — pastda, bosh barmoq yonida
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = ContainerPadding)
                .padding(bottom = 8.dp),
            verticalArrangement = Arrangement.Bottom,
        ) {

            ProfileSettingsItem(
                onSettingSelected = {settings ->
                    when(settings){
                        SettingType.THEME -> {
                            navigator?.push(ThemeScreen())
                        }
                        SettingType.HAPTICS -> {
                            navigator?.push(uz.tikoncha_parent.presentation.profile.settings.haptics.HapticsScreen())
                        }
                        SettingType.LOGOUT -> {
                            navigator?.push(LogoutScreen())
                        }
                    }
                }
            )
        }
    }
}

@Preview
@Composable
fun PreviewSettingsScreen(){
    TikonchaParentTheme(
        ThemeMode.DARK
    ){
        SettingsUi(
            navigator = null
        )
    }
}