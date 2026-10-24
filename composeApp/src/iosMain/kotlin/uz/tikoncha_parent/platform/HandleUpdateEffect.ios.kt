package uz.tikoncha_parent.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import platform.Foundation.NSURL
import platform.UIKit.UIApplication
import uz.tikoncha_parent.domain.model.in_app_update.UpdateEffect
import uz.tikoncha_parent.presentation.in_app_update.UpdateViewModel

@Composable
actual fun HandleUpdateEffect(viewModel: UpdateViewModel) {
    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is UpdateEffect.StartUpdateFlow,
                UpdateEffect.OpenAppStore -> {
                    val url = NSURL.URLWithString("itms-apps://itunes.apple.com/app/id6756965935")
                    url?.let { UIApplication.sharedApplication.openURL(it, emptyMap<Any?, Any?>(), null) }
                }
            }
        }
    }
}