package uz.tikoncha_parent.presentation.policy.tikonchagram

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.core.annotation.InternalVoyagerApi
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.internal.BackHandler
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import tikoncha_parents.composeapp.generated.resources.Res
import tikoncha_parents.composeapp.generated.resources.dialog_failed
import tikoncha_parents.composeapp.generated.resources.preset_enabled
import tikoncha_parents.composeapp.generated.resources.tg_allow_documents
import tikoncha_parents.composeapp.generated.resources.tg_allow_forwards
import tikoncha_parents.composeapp.generated.resources.tg_allow_gifs
import tikoncha_parents.composeapp.generated.resources.tg_allow_links
import tikoncha_parents.composeapp.generated.resources.tg_allow_photos
import tikoncha_parents.composeapp.generated.resources.tg_allow_stickers
import tikoncha_parents.composeapp.generated.resources.tg_allow_video_notes
import tikoncha_parents.composeapp.generated.resources.tg_allow_videos
import tikoncha_parents.composeapp.generated.resources.tg_allow_voice
import tikoncha_parents.composeapp.generated.resources.tg_contacts_only
import tikoncha_parents.composeapp.generated.resources.tg_contacts_only_sub
import tikoncha_parents.composeapp.generated.resources.tg_daily_minutes
import tikoncha_parents.composeapp.generated.resources.tg_daily_note
import tikoncha_parents.composeapp.generated.resources.tg_max_chats_all
import tikoncha_parents.composeapp.generated.resources.tg_max_chats_note
import tikoncha_parents.composeapp.generated.resources.tg_placeholder
import tikoncha_parents.composeapp.generated.resources.tg_placeholder_sub
import tikoncha_parents.composeapp.generated.resources.tg_preset_balanced
import tikoncha_parents.composeapp.generated.resources.tg_preset_balanced_desc
import tikoncha_parents.composeapp.generated.resources.tg_preset_custom
import tikoncha_parents.composeapp.generated.resources.tg_preset_open
import tikoncha_parents.composeapp.generated.resources.tg_preset_open_desc
import tikoncha_parents.composeapp.generated.resources.tg_preset_strict
import tikoncha_parents.composeapp.generated.resources.tg_preset_strict_desc
import tikoncha_parents.composeapp.generated.resources.tg_rules_enabled_sub
import tikoncha_parents.composeapp.generated.resources.tg_rules_info_privacy
import tikoncha_parents.composeapp.generated.resources.tg_rules_info_sync
import tikoncha_parents.composeapp.generated.resources.tg_rules_load_failed
import tikoncha_parents.composeapp.generated.resources.tg_rules_mode
import tikoncha_parents.composeapp.generated.resources.tg_rules_retry
import tikoncha_parents.composeapp.generated.resources.tg_rules_title
import tikoncha_parents.composeapp.generated.resources.tg_section_chats
import tikoncha_parents.composeapp.generated.resources.tg_section_content
import tikoncha_parents.composeapp.generated.resources.tg_section_daily
import tikoncha_parents.composeapp.generated.resources.tg_section_max_chats
import tikoncha_parents.composeapp.generated.resources.tg_show_bots
import tikoncha_parents.composeapp.generated.resources.tg_show_channels
import tikoncha_parents.composeapp.generated.resources.tg_show_groups
import tikoncha_parents.composeapp.generated.resources.xatolik
import uz.tikoncha_parent.domain.model.tikonchagram.TgPreset
import uz.tikoncha_parent.domain.model.tikonchagram.TgRules
import uz.tikoncha_parent.presentation.base.CustomDialog
import uz.tikoncha_parent.presentation.base.CustomHeader
import uz.tikoncha_parent.presentation.base.asText
import uz.tikoncha_parent.presentation.base.haptics.ErrorHaptic
import uz.tikoncha_parent.presentation.policy.components.GroupDivider
import uz.tikoncha_parent.presentation.policy.components.PolicyInfoBox
import uz.tikoncha_parent.presentation.policy.components.PolicySwitch
import uz.tikoncha_parent.presentation.policy.components.PolicyText
import uz.tikoncha_parent.presentation.policy.components.QuickChips
import uz.tikoncha_parent.presentation.policy.components.SectionLabel
import uz.tikoncha_parent.presentation.policy.components.SettingGroup
import uz.tikoncha_parent.presentation.policy.components.SettingRow
import uz.tikoncha_parent.ui.ContainerPadding
import uz.tikoncha_parent.ui.theme.AppColors
import uz.tikoncha_parent.ui.theme.rememberScreenSystemBars

/** Bitta farzand uchun Tikonchagram qoidalari (bola ilovasidagi Telegram). */
@OptIn(InternalVoyagerApi::class)
class TikonchagramRulesScreen(private val childId: String) : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.current ?: return
        val viewModel = koinScreenModel<TikonchagramRulesViewModel>()
        val state by viewModel.state.collectAsStateWithLifecycle()

        LaunchedEffect(Unit) { viewModel.onEvent(TikonchagramRulesEvent.Init(childId)) }
        BackHandler(true) { navigator.pop() }

        TikonchagramRulesUi(state = state, event = viewModel::onEvent, onBack = { navigator.pop() })
    }
}

@Composable
fun TikonchagramRulesUi(
    state: TikonchagramRulesState,
    event: (TikonchagramRulesEvent) -> Unit,
    onBack: () -> Unit,
) {
    val systemBars = rememberScreenSystemBars(statusBarColor = AppColors.bg.page, navigationBarColor = AppColors.bg.page)
    val r = state.rules
    val set: (TgRules) -> Unit = { event(TikonchagramRulesEvent.Changed(it)) }

    Column(modifier = Modifier.fillMaxSize().background(AppColors.bg.page).then(systemBars.modifier)) {
        CustomHeader(title = stringResource(Res.string.tg_rules_title), showBackButton = true, onBackClick = onBack)

        when {
            !state.loaded -> {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = AppColors.action.primary)
                }
                return@Column
            }
            !state.ready -> {
                Column(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(stringResource(Res.string.tg_rules_load_failed), style = PolicyText.row, color = AppColors.text.secondary)
                    TextButton(onClick = { event(TikonchagramRulesEvent.Retry) }) {
                        Text(stringResource(Res.string.tg_rules_retry), style = PolicyText.row, color = AppColors.action.primary)
                    }
                }
                return@Column
            }
        }

        Column(
            modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = ContainerPadding),
            verticalArrangement = Arrangement.spacedBy(22.dp),
        ) {
            Spacer(modifier = Modifier.height(2.dp))

            SettingGroup {
                SettingRow(
                    title = stringResource(Res.string.preset_enabled),
                    subtitle = stringResource(Res.string.tg_rules_enabled_sub),
                    trailing = {
                        PolicySwitch(checked = r.enabled, onCheckedChange = { set(r.copy(enabled = it)) })
                    },
                )
            }

            PolicyInfoBox(
                lines = listOf(
                    stringResource(Res.string.tg_rules_info_privacy),
                    stringResource(Res.string.tg_rules_info_sync),
                )
            )

            Column(
                modifier = Modifier.alpha(if (r.enabled) 1f else 0.45f),
                verticalArrangement = Arrangement.spacedBy(22.dp),
            ) {
                // ── Tayyor rejim ──
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    val preset = r.preset()
                    SectionLabel(stringResource(Res.string.tg_rules_mode), hint = if (preset == null) stringResource(Res.string.tg_preset_custom) else null)
                    SettingGroup {
                        Spacer(modifier = Modifier.height(16.dp))
                        QuickChips(
                            labels = TgPreset.entries.map { stringResource(it.title()) },
                            selectedIndex = preset?.ordinal ?: -1,
                            onSelect = { if (r.enabled) event(TikonchagramRulesEvent.PresetSelected(TgPreset.entries[it])) },
                        )
                    }
                    preset?.let { Hint(stringResource(it.description())) }
                }

                // ── Chat turlari ──
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    SectionLabel(stringResource(Res.string.tg_section_chats))
                    SettingGroup {
                        Toggle(Res.string.tg_show_channels, !r.hideChannels, r.enabled) { set(r.copy(hideChannels = !it)) }
                        GroupDivider()
                        Toggle(Res.string.tg_show_groups, !r.hideGroups, r.enabled) { set(r.copy(hideGroups = !it)) }
                        GroupDivider()
                        Toggle(Res.string.tg_show_bots, !r.hideBots, r.enabled) { set(r.copy(hideBots = !it)) }
                        GroupDivider()
                        Toggle(Res.string.tg_contacts_only, r.contactsOnly, r.enabled, Res.string.tg_contacts_only_sub) { set(r.copy(contactsOnly = it)) }
                    }
                }

                // ── Kontent ──
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    SectionLabel(stringResource(Res.string.tg_section_content))
                    SettingGroup {
                        Toggle(Res.string.tg_allow_photos, r.allowPhotos, r.enabled) { set(r.copy(allowPhotos = it)) }
                        GroupDivider()
                        Toggle(Res.string.tg_allow_voice, r.allowVoice, r.enabled) { set(r.copy(allowVoice = it)) }
                        GroupDivider()
                        Toggle(Res.string.tg_allow_videos, r.allowVideos, r.enabled) { set(r.copy(allowVideos = it)) }
                        GroupDivider()
                        Toggle(Res.string.tg_allow_video_notes, r.allowVideoNotes, r.enabled) { set(r.copy(allowVideoNotes = it)) }
                        GroupDivider()
                        Toggle(Res.string.tg_allow_stickers, r.allowStickers, r.enabled) { set(r.copy(allowStickers = it)) }
                        GroupDivider()
                        Toggle(Res.string.tg_allow_gifs, r.allowGifs, r.enabled) { set(r.copy(allowGifs = it)) }
                        GroupDivider()
                        Toggle(Res.string.tg_allow_documents, r.allowDocuments, r.enabled) { set(r.copy(allowDocuments = it)) }
                        GroupDivider()
                        Toggle(Res.string.tg_allow_links, r.allowLinks, r.enabled) { set(r.copy(allowLinks = it)) }
                        GroupDivider()
                        Toggle(Res.string.tg_allow_forwards, r.allowForwards, r.enabled) { set(r.copy(allowForwards = it)) }
                    }
                    SettingGroup {
                        Toggle(Res.string.tg_placeholder, r.showBlockedPlaceholder, r.enabled, Res.string.tg_placeholder_sub) {
                            set(r.copy(showBlockedPlaceholder = it))
                        }
                    }
                }

                // ── Kunlik vaqt ──
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    SectionLabel(stringResource(Res.string.tg_section_daily))
                    val daily = TgRules.DAILY_CHOICES
                    // null — farzand 10 daqiqagacha tanlaydi (eng kattasi bilan bir xil)
                    val dailyIndex = daily.indexOf(r.dailyMaxMinutes ?: daily.last())
                    SettingGroup {
                        Spacer(modifier = Modifier.height(16.dp))
                        QuickChips(
                            labels = daily.map { stringResource(Res.string.tg_daily_minutes, it) },
                            selectedIndex = dailyIndex,
                            onSelect = { if (r.enabled) set(r.copy(dailyMaxMinutes = daily[it])) },
                        )
                    }
                    Hint(stringResource(Res.string.tg_daily_note))
                }

                // ── Chatlar soni ──
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    SectionLabel(stringResource(Res.string.tg_section_max_chats))
                    val counts = TgRules.MAX_CHAT_CHOICES
                    SettingGroup {
                        Spacer(modifier = Modifier.height(16.dp))
                        QuickChips(
                            labels = counts.map { if (it == 0) stringResource(Res.string.tg_max_chats_all) else it.toString() },
                            selectedIndex = counts.indexOf(r.maxChats),
                            onSelect = { if (r.enabled) set(r.copy(maxChats = counts[it])) },
                        )
                    }
                    Hint(stringResource(Res.string.tg_max_chats_note))
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }

    val error = state.error
    ErrorHaptic(error)
    CustomDialog(
        painter = painterResource(Res.drawable.dialog_failed),
        show = error != null,
        title = stringResource(Res.string.xatolik),
        message = error?.asText().orEmpty(),
        onDismiss = { event(TikonchagramRulesEvent.ErrorDismissed) },
        onButtonClick = { event(TikonchagramRulesEvent.ErrorDismissed) },
    )
}

@Composable
private fun Toggle(
    title: StringResource,
    checked: Boolean,
    enabled: Boolean,
    subtitle: StringResource? = null,
    onChange: (Boolean) -> Unit,
) {
    SettingRow(
        title = stringResource(title),
        subtitle = subtitle?.let { stringResource(it) },
        trailing = { PolicySwitch(checked = checked, onCheckedChange = onChange, enabled = enabled) },
    )
}

@Composable
private fun Hint(text: String) {
    Text(
        text = text,
        style = PolicyText.hint,
        color = AppColors.text.tertiary,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
    )
}

fun TgPreset.title(): StringResource = when (this) {
    TgPreset.STRICT -> Res.string.tg_preset_strict
    TgPreset.BALANCED -> Res.string.tg_preset_balanced
    TgPreset.OPEN -> Res.string.tg_preset_open
}

private fun TgPreset.description(): StringResource = when (this) {
    TgPreset.STRICT -> Res.string.tg_preset_strict_desc
    TgPreset.BALANCED -> Res.string.tg_preset_balanced_desc
    TgPreset.OPEN -> Res.string.tg_preset_open_desc
}
