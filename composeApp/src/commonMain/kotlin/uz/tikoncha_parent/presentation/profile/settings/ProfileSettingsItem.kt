package uz.tikoncha_parent.presentation.profile.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import tikoncha_parents.composeapp.generated.resources.Res
import tikoncha_parents.composeapp.generated.resources.akkauntdan_chiqish
import tikoncha_parents.composeapp.generated.resources.akkauntni_o_chirish
import tikoncha_parents.composeapp.generated.resources.delete
import tikoncha_parents.composeapp.generated.resources.haptics_title
import tikoncha_parents.composeapp.generated.resources.logout
import tikoncha_parents.composeapp.generated.resources.star_setting
import tikoncha_parents.composeapp.generated.resources.tema
import tikoncha_parents.composeapp.generated.resources.vibration
import uz.tikoncha_parent.presentation.profile.ProfileSectionItem
import uz.tikoncha_parent.ui.SegmentGap
import uz.tikoncha_parent.ui.theme.AppColors
import uz.tikoncha_parent.ui.theme.ThemeMode
import uz.tikoncha_parent.ui.theme.TikonchaParentTheme

/** Sozlamalar: M3E guruhlangan ro'yxat (Profil'dagidek) — ko'rinish guruhi va alohida "chiqish". */
@Composable
fun ProfileSettingsItem(
    onSettingSelected: (SettingType) -> Unit
) {
    val groups = SettingType.entries.groupBy { it.isDestructive }
    Column(Modifier.fillMaxWidth()) {
        listOfNotNull(groups[false], groups[true]).forEachIndexed { g, group ->
            if (g > 0) Spacer(Modifier.height(16.dp))
            Column(verticalArrangement = Arrangement.spacedBy(SegmentGap)) {
                group.forEachIndexed { i, setting ->
                    ProfileSectionItem(
                        icon = painterResource(setting.iconId),
                        title = stringResource(setting.settingName),
                        onItemClick = { onSettingSelected(setting) },
                        textColor = if (setting.isDestructive) AppColors.text.accentDanger else AppColors.text.primary,
                        iconColor = if (setting.isDestructive) AppColors.icon.accentDanger else AppColors.icon.accentPrimary,
                        segmentIndex = i,
                        segmentCount = group.size,
                    )
                }
            }
        }
    }
}

enum class SettingType(
    val iconId: DrawableResource,
    val settingName: StringResource,
    val isDestructive: Boolean = false
) {
    THEME(
        iconId = Res.drawable.star_setting,
        settingName = Res.string.tema
    ),
    HAPTICS(
        iconId = Res.drawable.vibration,
        settingName = Res.string.haptics_title
    ),
    LOGOUT(
        iconId = Res.drawable.logout,
        settingName = Res.string.akkauntdan_chiqish,
        isDestructive = true
    ),
    DELETE_ACCOUNT(
        iconId = Res.drawable.delete,
        settingName = Res.string.akkauntni_o_chirish,
        isDestructive = true
    )
}

@Preview
@Composable
private fun Preview() {
    TikonchaParentTheme(ThemeMode.DARK) {
        ProfileSettingsItem(onSettingSelected = {})
    }
}
