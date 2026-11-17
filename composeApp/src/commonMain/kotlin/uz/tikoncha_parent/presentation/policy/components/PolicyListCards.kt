package uz.tikoncha_parent.presentation.policy.components

import tikoncha_parents.composeapp.generated.resources.preset_by_coparent
import tikoncha_parents.composeapp.generated.resources.preset_by_child
import tikoncha_parents.composeapp.generated.resources.policy_content_protection_short
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.stringResource
import tikoncha_parents.composeapp.generated.resources.Res
import tikoncha_parents.composeapp.generated.resources.camera
import tikoncha_parents.composeapp.generated.resources.parent_special_card
import tikoncha_parents.composeapp.generated.resources.parent_special_title
import tikoncha_parents.composeapp.generated.resources.ic_policy
import tikoncha_parents.composeapp.generated.resources.bed_sleeping
import tikoncha_parents.composeapp.generated.resources.blocklist
import tikoncha_parents.composeapp.generated.resources.policy_content_protection
import tikoncha_parents.composeapp.generated.resources.policy_content_protection_others
import tikoncha_parents.composeapp.generated.resources.policy_paused
import tikoncha_parents.composeapp.generated.resources.quick_no_apps
import tikoncha_parents.composeapp.generated.resources.quick_title
import tikoncha_parents.composeapp.generated.resources.shield
import tikoncha_parents.composeapp.generated.resources.shift_clock
import tikoncha_parents.composeapp.generated.resources.timer
import uz.tikoncha_parent.domain.model.policy.PolicyName
import uz.tikoncha_parent.presentation.policy.model.ContentProtectionUi
import uz.tikoncha_parent.presentation.policy.model.Ownership
import uz.tikoncha_parent.presentation.policy.model.PolicyCardUi
import uz.tikoncha_parent.presentation.policy.model.PolicySummary
import uz.tikoncha_parent.presentation.policy.model.PresetKind
import uz.tikoncha_parent.presentation.policy.model.PresetPolicyUi
import uz.tikoncha_parent.presentation.policy.model.QuickBlockUi
import tikoncha_parents.composeapp.generated.resources.protection_shield_short
import tikoncha_parents.composeapp.generated.resources.tg_rules_chat
import tikoncha_parents.composeapp.generated.resources.tg_rules_card_sub
import tikoncha_parents.composeapp.generated.resources.tg_rules_title

/* Ro'yxat kartalari — Student `PolicyCards` bilan bir xil. */

fun PresetKind.iconRes(): DrawableResource = when (this) {
    PresetKind.SLEEP -> Res.drawable.bed_sleeping
    PresetKind.LIMIT -> Res.drawable.timer
    PresetKind.SCHOOL -> Res.drawable.shift_clock
}

/**
 * Oddiy karta ikonkasi (yassi, kichik qator — 3D rasm faqat katta plitkalarda): tezkor blok — "doim yopiq",
 * tayyor jadval nomi bölsa — öşa jadval ikonkasi, maktab jadvali — maktab, qolgani (özim yoki boşqa odam
 * yozgan qoida) — umumiy qoida ikonkasi.
 */
private fun PolicyCardUi.iconRes(): DrawableResource = when {
    isQuickBlock || title.kind == PolicyName.Kind.QUICK_BLOCK -> Res.drawable.blocklist
    title.kind == PolicyName.Kind.SLEEP -> PresetKind.SLEEP.iconRes()
    title.kind == PolicyName.Kind.APP_LIMIT -> PresetKind.LIMIT.iconRes()
    title.kind == PolicyName.Kind.SCHOOL || ownership == Ownership.SCHOOL -> PresetKind.SCHOOL.iconRes()
    title.kind == PolicyName.Kind.CONTENT_PROTECTION -> Res.drawable.shield
    else -> Res.drawable.ic_policy
}

/** Egasidan qat'i nazar: yoqiq — brend rangi, o'chiq — kulrang. */
@Suppress("UnusedReceiverParameter")
fun Ownership.tone(enabled: Boolean): IconTone = if (enabled) IconTone.SOLID else IconTone.GRAY

@Composable
fun PresetPolicyCard(ui: PresetPolicyUi, busy: Boolean, onToggle: (Boolean) -> Unit, onClick: () -> Unit, modifier: Modifier = Modifier, segmentIndex: Int = 0, segmentCount: Int = 1, plus: Boolean = false) {
    PolicyCardBody(
        icon = ui.kind.iconRes(),
        // Ikonka — farzand uchun amaldagi holat (kimdir yoqqan), switch — o'zimniki
        tone = if (ui.isEnabled || ui.activeBy != null) IconTone.SOLID else IconTone.GRAY,
        title = ui.kind.title(),
        subtitle = when {
            ui.activeBy == Ownership.CHILD -> stringResource(Res.string.preset_by_child)
            ui.activeBy != null -> stringResource(Res.string.preset_by_coparent)
            ui.isPaused -> stringResource(Res.string.policy_paused)
            else -> ui.kind.summaryText(ui.summary)
        },
        onClick = onClick,
        modifier = modifier,
        segmentIndex = segmentIndex,
        segmentCount = segmentCount,
    ) {
        SwitchWithPlus(plus = plus) { PolicySwitch(checked = ui.isEnabled, onCheckedChange = onToggle, busy = busy) }
    }
}

/** Bitta switch hamma paket uchun; farzand yoki ikkinchi ota-ona yoqqan bo'lsa — izohda aytiladi. */
@Composable
fun ContentProtectionCard(ui: ContentProtectionUi, busy: Boolean, onToggle: (Boolean) -> Unit, onClick: () -> Unit, modifier: Modifier = Modifier, segmentIndex: Int = 0, segmentCount: Int = 1, plus: Boolean = false) {
    PolicyCardBody(
        icon = Res.drawable.shield,
        tone = if (ui.isEnabled || ui.byOthers) IconTone.SOLID else IconTone.GRAY,
        title = stringResource(Res.string.policy_content_protection),
        // Shablonda bitta qisqa qator; farzand yoki ikkinchi ota-ona yoqqan bo'lsa — shuni aytamiz
        subtitle = stringResource(if (!ui.isEnabled && ui.byOthers) Res.string.policy_content_protection_others else Res.string.policy_content_protection_short),
        onClick = onClick,
        modifier = modifier,
        segmentIndex = segmentIndex,
        segmentCount = segmentCount,
        note = stringResource(Res.string.protection_shield_short),
    ) {
        SwitchWithPlus(plus = plus) { PolicySwitch(checked = ui.isEnabled, onCheckedChange = onToggle, busy = busy, enabled = ui.isAvailable) }
    }
}

/** "Maxsus himoya" — nomaqbul rasm, xavfli yozişma, yaşirin rejim, xavfli joylar (alohida ekran). */
@Composable
fun SpecialProtectionCard(onClick: () -> Unit, modifier: Modifier = Modifier, segmentIndex: Int = 0, segmentCount: Int = 1) {
    PolicyCardBody(
        icon = Res.drawable.camera,
        tone = IconTone.GRAY,
        title = stringResource(Res.string.parent_special_title),
        subtitle = stringResource(Res.string.parent_special_card),
        onClick = onClick,
        modifier = modifier,
        segmentIndex = segmentIndex,
        segmentCount = segmentCount,
    ) {
        Chevron()
    }
}

/** "Tezkor blok" shabloni: shartsiz, faqat ilovalar ro'yxati. Bo'sh bo'lsa switch o'chiq. */
@Composable
fun QuickBlockCard(ui: QuickBlockUi, busy: Boolean, onToggle: (Boolean) -> Unit, onClick: () -> Unit, modifier: Modifier = Modifier, segmentIndex: Int = 0, segmentCount: Int = 1, plus: Boolean = false) {
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
        segmentIndex = segmentIndex,
        segmentCount = segmentCount,
    ) {
        SwitchWithPlus(plus = plus) { PolicySwitch(checked = ui.isOn, onCheckedChange = onToggle, busy = busy) }
    }
}

/** Tikonchagram (bola ilovasidagi Telegram) qoidalari — switch yo'q, ichida sozlanadi. */
@Composable
fun TikonchagramCard(onClick: () -> Unit, modifier: Modifier = Modifier) {
    PolicyCardBody(
        icon = Res.drawable.tg_rules_chat,
        // Sozlamalar kirishi (yoqib-o'chiriladigan shablon emas) — qo'shnilari bilan bir xil sokin
        tone = IconTone.GRAY,
        title = stringResource(Res.string.tg_rules_title),
        subtitle = stringResource(Res.string.tg_rules_card_sub),
        onClick = onClick,
        modifier = modifier,
    ) {
        Chevron()
    }
}

/** O'zim yaratgan / farzand / ikkinchi ota-ona / maktab jadvali va boshqalarning tezkor bloki. */
@Composable
fun PolicyCard(ui: PolicyCardUi, busy: Boolean, onToggle: (Boolean) -> Unit, onClick: () -> Unit, modifier: Modifier = Modifier, segmentIndex: Int = 0, segmentCount: Int = 1, plus: Boolean = false) {
    PolicyCardBody(
        icon = ui.iconRes(),
        tone = ui.ownership.tone(ui.isEnabled),
        title = ui.title.text(),
        subtitle = when {
            ui.isPaused -> stringResource(Res.string.policy_paused)
            // har egada tezkor blok doim bor (o'chiq, bo'sh) — bo'sh bo'lsa shuni aytamiz
            ui.isQuickBlock -> targetsText(ui.summary) ?: stringResource(Res.string.quick_no_apps)
            else -> ui.summary.cardText().ifBlank { null }
        },
        onClick = onClick,
        modifier = modifier,
        segmentIndex = segmentIndex,
        segmentCount = segmentCount,
    ) {
        if (ui.canControl) SwitchWithPlus(plus = plus) { PolicySwitch(checked = ui.isEnabled, onCheckedChange = onToggle, busy = busy) }
        else LockIcon()
    }
}

/** Switch oldida "Plus" belgisi: yoqiş pullik — bosişdan oldin aytiladi. */
@Composable
private fun SwitchWithPlus(plus: Boolean, switch: @Composable () -> Unit) {
    if (!plus) {
        switch()
        return
    }
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        PlusBadge()
        switch()
    }
}
