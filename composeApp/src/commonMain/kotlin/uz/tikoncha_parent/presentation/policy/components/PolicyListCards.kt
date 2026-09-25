package uz.tikoncha_parent.presentation.policy.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.stringResource
import tikoncha_parents.composeapp.generated.resources.Res
import tikoncha_parents.composeapp.generated.resources.apps_icon
import tikoncha_parents.composeapp.generated.resources.bed_sleeping
import tikoncha_parents.composeapp.generated.resources.blocklist
import tikoncha_parents.composeapp.generated.resources.policy_content_protection
import tikoncha_parents.composeapp.generated.resources.policy_content_protection_others
import tikoncha_parents.composeapp.generated.resources.policy_content_protection_sub
import tikoncha_parents.composeapp.generated.resources.policy_paused
import tikoncha_parents.composeapp.generated.resources.quick_no_apps
import tikoncha_parents.composeapp.generated.resources.quick_title
import tikoncha_parents.composeapp.generated.resources.shield
import tikoncha_parents.composeapp.generated.resources.shift_clock
import tikoncha_parents.composeapp.generated.resources.timer
import uz.tikoncha_parent.presentation.policy.model.ContentProtectionUi
import uz.tikoncha_parent.presentation.policy.model.Ownership
import uz.tikoncha_parent.presentation.policy.model.PolicyCardUi
import uz.tikoncha_parent.presentation.policy.model.PolicySummary
import uz.tikoncha_parent.presentation.policy.model.PresetKind
import uz.tikoncha_parent.presentation.policy.model.PresetPolicyUi
import uz.tikoncha_parent.presentation.policy.model.QuickBlockUi

/* Ro'yxat kartalari — Student `PolicyCards` bilan bir xil. */

fun PresetKind.iconRes(): DrawableResource = when (this) {
    PresetKind.SLEEP -> Res.drawable.bed_sleeping
    PresetKind.LIMIT -> Res.drawable.timer
    PresetKind.SCHOOL -> Res.drawable.shift_clock
}

fun Ownership.tone(enabled: Boolean): IconTone = when {
    !enabled -> IconTone.GRAY
    this == Ownership.MINE -> IconTone.SOLID
    this == Ownership.SCHOOL -> IconTone.SCHOOL
    else -> IconTone.WARN
}

@Composable
fun PresetPolicyCard(ui: PresetPolicyUi, busy: Boolean, onToggle: (Boolean) -> Unit, onClick: () -> Unit, modifier: Modifier = Modifier) {
    PolicyCardBody(
        icon = ui.kind.iconRes(),
        tone = if (ui.isEnabled) IconTone.SOLID else IconTone.GRAY,
        title = ui.kind.title(),
        subtitle = if (ui.isPaused) stringResource(Res.string.policy_paused) else ui.kind.summaryText(ui.summary),
        onClick = onClick,
        modifier = modifier,
    ) {
        PolicySwitch(checked = ui.isEnabled, onCheckedChange = onToggle, busy = busy)
    }
}

/** Bitta switch hamma paket uchun; farzand yoki ikkinchi ota-ona yoqqan bo'lsa — izohda aytiladi. */
@Composable
fun ContentProtectionCard(ui: ContentProtectionUi, busy: Boolean, onToggle: (Boolean) -> Unit, onClick: () -> Unit, modifier: Modifier = Modifier) {
    PolicyCardBody(
        icon = Res.drawable.shield,
        tone = if (ui.isEnabled || ui.byOthers) IconTone.SOLID else IconTone.GRAY,
        title = stringResource(Res.string.policy_content_protection),
        subtitle = stringResource(if (!ui.isEnabled && ui.byOthers) Res.string.policy_content_protection_others else Res.string.policy_content_protection_sub),
        onClick = onClick,
        modifier = modifier,
    ) {
        PolicySwitch(checked = ui.isEnabled, onCheckedChange = onToggle, busy = busy, enabled = ui.isAvailable)
    }
}

/** "Tezkor blok" shabloni: shartsiz, faqat ilovalar ro'yxati. Bo'sh bo'lsa switch o'chiq. */
@Composable
fun QuickBlockCard(ui: QuickBlockUi, busy: Boolean, onToggle: (Boolean) -> Unit, onClick: () -> Unit, modifier: Modifier = Modifier) {
    PolicyCardBody(
        icon = Res.drawable.blocklist,
        tone = if (ui.isOn) IconTone.SOLID else IconTone.GRAY,
        title = stringResource(Res.string.quick_title),
        subtitle = when {
            ui.isPaused -> stringResource(Res.string.policy_paused)
            ui.isEmpty -> stringResource(Res.string.quick_no_apps)
            else -> targetsText(PolicySummary(appCount = ui.appCount))
        },
        onClick = onClick,
        modifier = modifier,
    ) {
        PolicySwitch(checked = ui.isOn, onCheckedChange = onToggle, busy = busy)
    }
}

/** O'zim yaratgan / farzand / ikkinchi ota-ona / maktab jadvali va boshqalarning tezkor bloki. */
@Composable
fun PolicyCard(ui: PolicyCardUi, busy: Boolean, onToggle: (Boolean) -> Unit, onClick: () -> Unit, modifier: Modifier = Modifier) {
    PolicyCardBody(
        icon = if (ui.isQuickBlock) Res.drawable.blocklist else Res.drawable.apps_icon,
        tone = ui.ownership.tone(ui.isEnabled),
        title = if (ui.isQuickBlock) stringResource(Res.string.quick_title) else ui.title,
        subtitle = if (ui.isPaused) stringResource(Res.string.policy_paused) else ui.summary.text().ifBlank { null },
        onClick = onClick,
        modifier = modifier,
    ) {
        if (ui.canControl) PolicySwitch(checked = ui.isEnabled, onCheckedChange = onToggle, busy = busy)
        else LockIcon()
    }
}
