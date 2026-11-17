package uz.tikoncha_parent.presentation.policy.components

import tikoncha_parents.composeapp.generated.resources.preset_by_coparent
import tikoncha_parents.composeapp.generated.resources.preset_by_child
import tikoncha_parents.composeapp.generated.resources.policy_content_protection_short
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.stringResource
import tikoncha_parents.composeapp.generated.resources.Res
import tikoncha_parents.composeapp.generated.resources.parent_special_card
import tikoncha_parents.composeapp.generated.resources.parent_special_title
import tikoncha_parents.composeapp.generated.resources.bed_sleeping
import tikoncha_parents.composeapp.generated.resources.policy_content_protection
import tikoncha_parents.composeapp.generated.resources.policy_content_protection_others
import tikoncha_parents.composeapp.generated.resources.policy_paused
import tikoncha_parents.composeapp.generated.resources.quick_no_apps
import tikoncha_parents.composeapp.generated.resources.quick_title
import tikoncha_parents.composeapp.generated.resources.shift_clock
import tikoncha_parents.composeapp.generated.resources.timer
import tikoncha_parents.composeapp.generated.resources.ic3d_rule_always_closed
import tikoncha_parents.composeapp.generated.resources.ic3d_rule_bad_sites
import tikoncha_parents.composeapp.generated.resources.ic3d_rule_custom
import tikoncha_parents.composeapp.generated.resources.ic3d_rule_daily_time
import tikoncha_parents.composeapp.generated.resources.ic3d_rule_school
import tikoncha_parents.composeapp.generated.resources.ic3d_rule_sleep
import tikoncha_parents.composeapp.generated.resources.ic3d_rule_special
import tikoncha_parents.composeapp.generated.resources.ic3d_rule_telegram
import uz.tikoncha_parent.domain.model.policy.PolicyName
import uz.tikoncha_parent.presentation.policy.model.ContentProtectionUi
import uz.tikoncha_parent.presentation.policy.model.Ownership
import uz.tikoncha_parent.presentation.policy.model.PolicyCardUi
import uz.tikoncha_parent.presentation.policy.model.PolicySummary
import uz.tikoncha_parent.presentation.policy.model.PresetKind
import uz.tikoncha_parent.presentation.policy.model.PresetPolicyUi
import uz.tikoncha_parent.presentation.policy.model.QuickBlockUi
import tikoncha_parents.composeapp.generated.resources.protection_shield_short
import tikoncha_parents.composeapp.generated.resources.tg_rules_card_sub
import tikoncha_parents.composeapp.generated.resources.tg_rules_title

/* Ro'yxat kartalari — Student `PolicyCards` bilan bir xil. */

fun PresetKind.iconRes(): DrawableResource = when (this) {
    PresetKind.SLEEP -> Res.drawable.bed_sleeping
    PresetKind.LIMIT -> Res.drawable.timer
    PresetKind.SCHOOL -> Res.drawable.shift_clock
}

/** Ro'yxat kartasidagi 3D rasm (tintsiz) — "rasm matndan oldin". */
fun PresetKind.icon3d(): DrawableResource = when (this) {
    PresetKind.SLEEP -> Res.drawable.ic3d_rule_sleep
    PresetKind.LIMIT -> Res.drawable.ic3d_rule_daily_time
    PresetKind.SCHOOL -> Res.drawable.ic3d_rule_school
}

/**
 * Oddiy karta rasmi: tezkor blok — "doim yopiq", tayyor jadval nomi bölsa — öşa jadval rasmi, maktab jadvali —
 * maktab, qolgani (özim yoki boşqa odam yozgan qoida) — "öz qoidam".
 */
private fun PolicyCardUi.icon3d(): DrawableResource = when {
    isQuickBlock || title.kind == PolicyName.Kind.QUICK_BLOCK -> Res.drawable.ic3d_rule_always_closed
    title.kind == PolicyName.Kind.SLEEP -> Res.drawable.ic3d_rule_sleep
    title.kind == PolicyName.Kind.APP_LIMIT -> Res.drawable.ic3d_rule_daily_time
    title.kind == PolicyName.Kind.SCHOOL || ownership == Ownership.SCHOOL -> Res.drawable.ic3d_rule_school
    title.kind == PolicyName.Kind.CONTENT_PROTECTION -> Res.drawable.ic3d_rule_bad_sites
    else -> Res.drawable.ic3d_rule_custom
}

/** Egasidan qat'i nazar: yoqiq — rasm töliq rangda, o'chiq — xiraroq. */
@Suppress("UnusedReceiverParameter")
fun Ownership.tone(enabled: Boolean): IconTone = if (enabled) IconTone.SOLID else IconTone.GRAY

@Composable
fun PresetPolicyCard(ui: PresetPolicyUi, busy: Boolean, onToggle: (Boolean) -> Unit, onClick: () -> Unit, modifier: Modifier = Modifier, segmentIndex: Int = 0, segmentCount: Int = 1, plus: Boolean = false) {
    PolicyCardBody(
        icon = ui.kind.icon3d(),
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
        icon = Res.drawable.ic3d_rule_bad_sites,
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
        icon = Res.drawable.ic3d_rule_special,
        // Kirish kartasi (switch yöq) — rasm doim töliq rangda
        tone = IconTone.SOLID,
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
        icon = Res.drawable.ic3d_rule_always_closed,
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
        icon = Res.drawable.ic3d_rule_telegram,
        // Sozlamalar kirishi (yoqib-o'chiriladigan shablon emas) — rasm doim töliq rangda
        tone = IconTone.SOLID,
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
        icon = ui.icon3d(),
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

/**
 * Switch ustida "Plus" belgisi: yoqiş pullik — bosişdan oldin aytiladi. Ustma-ust (yonma-yon emas) —
 * katta 3D rasmli blokda sarlavhaga joy qolsin.
 */
@Composable
fun SwitchWithPlus(plus: Boolean, switch: @Composable () -> Unit) {
    if (!plus) {
        switch()
        return
    }
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        PlusBadge()
        switch()
    }
}
