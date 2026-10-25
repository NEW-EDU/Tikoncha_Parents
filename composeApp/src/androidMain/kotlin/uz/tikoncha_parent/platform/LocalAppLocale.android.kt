package uz.tikoncha_parent.platform

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidedValue
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import java.util.Locale

actual object LocalAppLocale {
    private var default: Locale? = null

    actual val current: String
        @Composable get() = Locale.getDefault().toString()

    @Composable
    actual infix fun provides(value: String?): ProvidedValue<*> {
        val config = LocalConfiguration.current
        val ctx = LocalContext.current
        if (default == null) default = Locale.getDefault()

        val newLocale = value?.let { Locale.forLanguageTag(it) } ?: default!!
        Locale.setDefault(newLocale)

        val newConfig = Configuration(config)
        newConfig.setLocale(newLocale)
        newConfig.setLayoutDirection(newLocale)
        ctx.resources.updateConfiguration(newConfig, ctx.resources.displayMetrics)

        return LocalConfiguration.provides(newConfig)
    }
}