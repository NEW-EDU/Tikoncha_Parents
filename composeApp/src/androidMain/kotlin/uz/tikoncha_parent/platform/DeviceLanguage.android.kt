package uz.tikoncha_parent.platform

import android.content.res.Resources

actual fun deviceLanguageCode(): String =
    Resources.getSystem().configuration.locales[0]?.language.orEmpty()

actual fun deviceRegionCode(): String =
    Resources.getSystem().configuration.locales[0]?.country?.uppercase().orEmpty()
