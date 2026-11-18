package uz.tikoncha_parent.presentation.assistant

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Undo
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import tikoncha_parents.composeapp.generated.resources.Res
import tikoncha_parents.composeapp.generated.resources.ai_auto_speak_off
import tikoncha_parents.composeapp.generated.resources.ai_auto_speak_on
import tikoncha_parents.composeapp.generated.resources.ai_setting_undone
import tikoncha_parents.composeapp.generated.resources.ai_undo
import tikoncha_parents.composeapp.generated.resources.haptics_light
import tikoncha_parents.composeapp.generated.resources.haptics_off
import tikoncha_parents.composeapp.generated.resources.haptics_standard
import tikoncha_parents.composeapp.generated.resources.haptics_strong
import tikoncha_parents.composeapp.generated.resources.haptics_title
import tikoncha_parents.composeapp.generated.resources.home_layout_grid
import tikoncha_parents.composeapp.generated.resources.home_layout_list
import tikoncha_parents.composeapp.generated.resources.home_layout_title
import tikoncha_parents.composeapp.generated.resources.tema
import tikoncha_parents.composeapp.generated.resources.text_size_extra_large
import tikoncha_parents.composeapp.generated.resources.text_size_large
import tikoncha_parents.composeapp.generated.resources.text_size_small
import tikoncha_parents.composeapp.generated.resources.text_size_standard
import tikoncha_parents.composeapp.generated.resources.text_size_title
import tikoncha_parents.composeapp.generated.resources.til
import uz.tikoncha_parent.presentation.domain.model.LanguageType
import uz.tikoncha_parent.presentation.new_home.HomeLayout
import uz.tikoncha_parent.presentation.new_home.HomeLayoutController
import uz.tikoncha_parent.presentation.profile.language.LanguageController
import uz.tikoncha_parent.ui.Haptics
import uz.tikoncha_parent.ui.HapticsLevel
import uz.tikoncha_parent.ui.theme.AppColors
import uz.tikoncha_parent.ui.theme.AppTypography
import uz.tikoncha_parent.ui.theme.TextSize
import uz.tikoncha_parent.ui.theme.TextSizeController
import uz.tikoncha_parent.ui.theme.ThemeController
import uz.tikoncha_parent.ui.theme.ThemeMode

/**
 * Yordamçi özgartira oladigan ilova sozlamalari — ota-onaning öz telefonida, darhol, "Qaytariş" bilan.
 * Kalit va qiymatlar — server `setting` hodisasidagi kabi (kiçik harf): text_size=large, theme=dark, language=ru,
 * haptics=off, home_layout=list. "voice_replies" (on/off) — yordamçining özida, ViewModel qöllaydi.
 */
internal object AssistantSettings {
    const val VOICE_REPLIES = "voice_replies"

    /** Joriy qiymat (server yozuvida); noma'lum kalit — null. */
    fun current(key: String, lang: LanguageController): String? = when (key) {
        "text_size" -> TextSizeController.size.value.name.lowercase()
        "theme" -> ThemeController.mode.value.name.lowercase()
        "language" -> lang.current.value.languageCode
        "haptics" -> Haptics.level.name.lowercase()
        "home_layout" -> HomeLayoutController.layout.value.name.lowercase()
        else -> null
    }

    /** Qöllaydi; kalit yoki qiymat tanilmasa — false (hech narsa özgarmaydi). */
    fun apply(key: String, value: String, lang: LanguageController): Boolean = when (key) {
        "text_size" -> textSize(value)?.also { TextSizeController.set(it) } != null
        "theme" -> theme(value)?.also { ThemeController.setMode(it) } != null
        "language" -> language(value)?.also { lang.select(it) } != null
        "haptics" -> haptics(value)?.also { Haptics.level = it } != null
        "home_layout" -> layout(value)?.also { HomeLayoutController.set(it) } != null
        else -> false
    }

    private fun norm(v: String) = v.trim().replace('-', '_').replace(' ', '_')
    private fun textSize(v: String) = TextSize.entries.firstOrNull { it.name.equals(norm(v), ignoreCase = true) }
    private fun theme(v: String) = ThemeMode.entries.firstOrNull { it.name.equals(norm(v), ignoreCase = true) }
    private fun haptics(v: String) = HapticsLevel.entries.firstOrNull { it.name.equals(norm(v), ignoreCase = true) }
    private fun layout(v: String) = HomeLayout.entries.firstOrNull { it.name.equals(norm(v), ignoreCase = true) }
    private fun language(v: String) = LanguageType.entries.firstOrNull { it.languageCode.equals(v.trim(), ignoreCase = true) }

    /** Qatordagi matn: "Şrift: Katta", "Til: Русский"; tanilmasa null. */
    @Composable
    fun label(key: String, value: String): String? {
        if (key == VOICE_REPLIES) return stringResource(if (isOn(value)) Res.string.ai_auto_speak_on else Res.string.ai_auto_speak_off)
        val title: StringResource
        val valueText: String = when (key) {
            "text_size" -> {
                title = Res.string.text_size_title
                stringResource(
                    when (textSize(value) ?: return null) {
                        TextSize.SMALL -> Res.string.text_size_small
                        TextSize.STANDARD -> Res.string.text_size_standard
                        TextSize.LARGE -> Res.string.text_size_large
                        TextSize.EXTRA_LARGE -> Res.string.text_size_extra_large
                    }
                )
            }
            "theme" -> {
                title = Res.string.tema
                stringResource((theme(value) ?: return null).resId)
            }
            "language" -> {
                title = Res.string.til
                (language(value) ?: return null).languageName
            }
            "haptics" -> {
                title = Res.string.haptics_title
                stringResource(
                    when (haptics(value) ?: return null) {
                        HapticsLevel.OFF -> Res.string.haptics_off
                        HapticsLevel.LIGHT -> Res.string.haptics_light
                        HapticsLevel.STANDARD -> Res.string.haptics_standard
                        HapticsLevel.STRONG -> Res.string.haptics_strong
                    }
                )
            }
            "home_layout" -> {
                title = Res.string.home_layout_title
                stringResource(
                    when (layout(value) ?: return null) {
                        HomeLayout.GRID -> Res.string.home_layout_grid
                        HomeLayout.LIST -> Res.string.home_layout_list
                    }
                )
            }
            else -> return null
        }
        return stringResource(title) + ": " + valueText
    }

    fun isOn(value: String) = value.trim().lowercase() in setOf("on", "true", "yes", "1")
}

/** Sozlama özgartirildi: "✓ Şrift: Katta  [Qaytariş]" yoki qaytarilgan bölsa — kulrang "Qaytarildi". */
@Composable
internal fun SettingRow(item: AssistantItem.Setting, onUndo: () -> Unit) {
    val text = AssistantSettings.label(item.key, item.value) ?: return
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(AppColors.bg.surface)
            .padding(start = 14.dp, end = 4.dp, top = 4.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            Icons.Rounded.CheckCircle,
            null,
            tint = if (item.undone) AppColors.icon.secondary else AppColors.icon.accentPrimary,
            modifier = Modifier.size(20.dp),
        )
        Spacer(Modifier.width(10.dp))
        Text(
            text,
            style = AppTypography.titleSmMedium,
            color = if (item.undone) AppColors.text.tertiary else AppColors.text.primary,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f).padding(vertical = 8.dp),
        )
        Spacer(Modifier.width(8.dp))
        if (item.undone) {
            Text(
                stringResource(Res.string.ai_setting_undone),
                style = AppTypography.bodyMdMedium,
                color = AppColors.text.tertiary,
                modifier = Modifier.padding(end = 10.dp),
            )
        } else if (item.previous != null) {
            TextButton(onClick = onUndo) {
                Icon(Icons.AutoMirrored.Rounded.Undo, null, tint = AppColors.text.accentEmphasis, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text(stringResource(Res.string.ai_undo), style = AppTypography.bodyMdMedium, color = AppColors.text.accentEmphasis)
            }
        }
    }
}
