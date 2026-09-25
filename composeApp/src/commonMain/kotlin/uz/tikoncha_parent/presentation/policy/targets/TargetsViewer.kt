package uz.tikoncha_parent.presentation.policy.targets

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.annotation.InternalVoyagerApi
import cafe.adriel.voyager.navigator.internal.BackHandler
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import tikoncha_parents.composeapp.generated.resources.Res
import tikoncha_parents.composeapp.generated.resources.global
import tikoncha_parents.composeapp.generated.resources.ilovalar
import tikoncha_parents.composeapp.generated.resources.kategoriyalar
import tikoncha_parents.composeapp.generated.resources.preset_all_apps
import tikoncha_parents.composeapp.generated.resources.preset_stays_open
import tikoncha_parents.composeapp.generated.resources.preset_what_closed
import tikoncha_parents.composeapp.generated.resources.saytlar
import tikoncha_parents.composeapp.generated.resources.targets_except_open
import tikoncha_parents.composeapp.generated.resources.targets_only_selected_open
import tikoncha_parents.composeapp.generated.resources.targets_only_selected_open_sub
import tikoncha_parents.composeapp.generated.resources.targets_selected_closed
import tikoncha_parents.composeapp.generated.resources.targets_selected_closed_sub
import uz.tikoncha_parent.domain.model.apps.AppCategory
import uz.tikoncha_parent.domain.model.apps.InstalledApp
import uz.tikoncha_parent.domain.model.policy.PolicyAction
import uz.tikoncha_parent.domain.model.policy.PolicyDraft
import uz.tikoncha_parent.domain.model.policy.PolicyTargets
import uz.tikoncha_parent.presentation.base.ChildAppIcon
import uz.tikoncha_parent.presentation.base.CustomHeader
import uz.tikoncha_parent.presentation.policy.components.GroupDivider
import uz.tikoncha_parent.presentation.policy.components.SectionLabel
import uz.tikoncha_parent.presentation.policy.components.SettingGroup
import uz.tikoncha_parent.presentation.policy.components.SettingRow
import uz.tikoncha_parent.presentation.profile.language.LanguagePrefs
import uz.tikoncha_parent.ui.ContainerPadding
import uz.tikoncha_parent.ui.theme.AppColors
import uz.tikoncha_parent.ui.theme.rememberScreenSystemBars
import tikoncha_parents.composeapp.generated.resources.info_sites_shield
import uz.tikoncha_parent.presentation.policy.components.PolicyText
import tikoncha_parents.composeapp.generated.resources.info_features_shield

/**
 * Farzand yoki ikkinchi ota-ona jadvali nimalarni yopishini ko'rish — faqat o'qish.
 * Ilovalar (Shorts / Reels bilan), kategoriyalar, saytlar va "Ochiq qoladi" istisnolari.
 * Bola ilovalari ro'yxatida yo'q paket nomi bilan ko'rsatiladi.
 */
@OptIn(InternalVoyagerApi::class)
@Composable
fun TargetsViewer(draft: PolicyDraft, apps: List<InstalledApp>, onClose: () -> Unit) {
    BackHandler(true) { onClose() }
    val systemBars = rememberScreenSystemBars(statusBarColor = AppColors.bg.page, navigationBarColor = AppColors.bg.page)
    val byPackage = remember(apps) { apps.associateBy { it.packageName.lowercase() } }
    val lang = remember { LanguagePrefs.loadOrDefault() }
    val t = draft.targets
    val allApps = t.isAllApps
    val packages = t.packages.filter { it != PolicyTargets.ALL_APPS }
    val allowList = draft.action == PolicyAction.ALLOW

    @Composable
    fun AppRows(rows: List<Pair<String, String>>) {
        rows.forEachIndexed { i, (pkg, name) ->
            SettingRow(title = name, leading = { ChildAppIcon(iconUrl = byPackage[pkg.lowercase()]?.iconUrl) })
            if (i < rows.lastIndex) GroupDivider()
        }
    }
    fun label(pkg: String) = byPackage[pkg.lowercase()]?.name ?: pkg

    Column(modifier = Modifier.fillMaxSize().background(AppColors.bg.page).then(systemBars.modifier)) {
        CustomHeader(
            title = stringResource(if (allowList) Res.string.targets_only_selected_open else Res.string.preset_what_closed),
            showBackButton = true,
            onBackClick = onClose,
        )
        Column(
            modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = ContainerPadding),
            verticalArrangement = Arrangement.spacedBy(22.dp),
        ) {
            Spacer(modifier = Modifier.height(2.dp))
            SettingGroup {
                SettingRow(
                    title = stringResource(
                        when {
                            allApps -> Res.string.preset_all_apps
                            allowList -> Res.string.targets_only_selected_open
                            else -> Res.string.targets_selected_closed
                        }
                    ),
                    subtitle = when {
                        allApps && t.excludePackages.isNotEmpty() -> stringResource(Res.string.targets_except_open)
                        allApps -> null
                        allowList -> stringResource(Res.string.targets_only_selected_open_sub)
                        else -> stringResource(Res.string.targets_selected_closed_sub)
                    },
                )
            }

            if (packages.isNotEmpty() || t.features.isNotEmpty()) {
                ViewerSection(
                    title = stringResource(Res.string.ilovalar),
                    count = packages.size + t.features.size,
                    footer = if (t.features.isNotEmpty()) stringResource(Res.string.info_features_shield) else null,
                ) {
                    AppRows(
                        packages.map { it to label(it) } + t.features.map { key ->
                            val f = PolicyAppFeatures.byKey(key)
                            (f?.parentPackage ?: "") to (f?.name ?: key)
                        }
                    )
                }
            }
            if (t.categories.isNotEmpty()) {
                ViewerSection(title = stringResource(Res.string.kategoriyalar), count = t.categories.size) {
                    t.categories.forEachIndexed { i, id ->
                        val localized = CategoryLocalizer.localize(AppCategory.from(id), lang)
                        SettingRow(title = localized.name, leading = { EmojiTile(localized.emoji) })
                        if (i < t.categories.lastIndex) GroupDivider()
                    }
                }
            }
            if (t.sites.isNotEmpty()) {
                ViewerSection(title = stringResource(Res.string.saytlar), count = t.sites.size, footer = stringResource(Res.string.info_sites_shield)) {
                    t.sites.forEachIndexed { i, site ->
                        SettingRow(
                            title = site,
                            leading = {
                                Icon(
                                    painter = painterResource(Res.drawable.global),
                                    contentDescription = null,
                                    modifier = Modifier.size(24.dp),
                                    tint = AppColors.icon.secondary,
                                )
                            },
                        )
                        if (i < t.sites.lastIndex) GroupDivider()
                    }
                }
            }
            if (!allowList && t.excludePackages.isNotEmpty()) {
                ViewerSection(title = stringResource(Res.string.preset_stays_open), count = t.excludePackages.size) {
                    AppRows(t.excludePackages.map { it to label(it) })
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun ViewerSection(title: String, count: Int, footer: String? = null, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        SectionLabel("$title · $count")
        SettingGroup { content() }
        if (footer != null) {
            Text(text = footer, style = PolicyText.hint, color = AppColors.text.tertiary, modifier = Modifier.padding(horizontal = 4.dp))
        }
    }
}

@Composable
private fun EmojiTile(emoji: String) {
    Box(
        modifier = Modifier.size(40.dp).background(AppColors.bg.surface, RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = emoji, modifier = Modifier.fillMaxWidth(), fontSize = 18.sp, lineHeight = 18.sp, textAlign = TextAlign.Center)
    }
}
