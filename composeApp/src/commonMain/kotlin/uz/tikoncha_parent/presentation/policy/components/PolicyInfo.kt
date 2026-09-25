package uz.tikoncha_parent.presentation.policy.components

import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.stringResource
import tikoncha_parents.composeapp.generated.resources.Res
import tikoncha_parents.composeapp.generated.resources.info_always
import tikoncha_parents.composeapp.generated.resources.info_disabled
import tikoncha_parents.composeapp.generated.resources.info_limit_all
import tikoncha_parents.composeapp.generated.resources.info_limit_daily
import tikoncha_parents.composeapp.generated.resources.info_limit_gate
import tikoncha_parents.composeapp.generated.resources.info_limit_hourly
import tikoncha_parents.composeapp.generated.resources.info_limit_selected
import tikoncha_parents.composeapp.generated.resources.info_not_counted
import tikoncha_parents.composeapp.generated.resources.info_nothing
import tikoncha_parents.composeapp.generated.resources.info_protected
import tikoncha_parents.composeapp.generated.resources.info_sentence
import tikoncha_parents.composeapp.generated.resources.info_stays_open
import tikoncha_parents.composeapp.generated.resources.info_what_all_apps
import tikoncha_parents.composeapp.generated.resources.info_what_all_apps_plus_sites
import tikoncha_parents.composeapp.generated.resources.info_what_all_apps_sites
import tikoncha_parents.composeapp.generated.resources.info_what_all_sites
import tikoncha_parents.composeapp.generated.resources.info_what_selected
import tikoncha_parents.composeapp.generated.resources.info_when_time
import tikoncha_parents.composeapp.generated.resources.info_when_time_outside
import tikoncha_parents.composeapp.generated.resources.info_where_inside
import tikoncha_parents.composeapp.generated.resources.info_where_outside
import tikoncha_parents.composeapp.generated.resources.policy_every_day
import tikoncha_parents.composeapp.generated.resources.policy_n_apps
import tikoncha_parents.composeapp.generated.resources.policy_n_categories
import tikoncha_parents.composeapp.generated.resources.policy_n_sites
import uz.tikoncha_parent.domain.model.LimitWindow
import uz.tikoncha_parent.presentation.policy.mapper.InfoWhat
import uz.tikoncha_parent.presentation.policy.mapper.PolicyInfo
import uz.tikoncha_parent.presentation.policy.mapper.TargetCounts

/** Jadval sozlamalaridan gaplar: asosiy gap · ochiq qoladiganlar · zarur ilovalar · o'chiq. */
@Composable
fun PolicyInfo.sentences(): List<String> {
    if (what == InfoWhat.NOTHING) return listOf(stringResource(Res.string.info_nothing))

    val out = ArrayList<String>(4)
    val gate = gateText()
    val rule = limit

    // Oq ro'yxatda asosiy gap — "qolgani yopiladi"; limit ochiq qoladiganlarga tegishli
    if (rule == null || allowList) {
        val prefix = gate ?: stringResource(Res.string.info_always)
        out += stringResource(Res.string.info_sentence, prefix, whatText()).capitalized()
    } else {
        out += limitSentence(selected = what == InfoWhat.SELECTED)
        if (gate != null) out += stringResource(Res.string.info_limit_gate, gate.trimEnd(',').trim())
    }
    if (!open.isEmpty) {
        out += stringResource(if (openNotCounted) Res.string.info_not_counted else Res.string.info_stays_open, open.text())
    }
    if (rule != null && allowList) out += limitSentence(selected = true)
    if (mentionProtected) out += stringResource(Res.string.info_protected)
    if (disabled) out += stringResource(Res.string.info_disabled)
    return out
}

@Composable
private fun PolicyInfo.limitSentence(selected: Boolean): String {
    val rule = limit ?: return ""
    val days = daysText(rule.days.map { it.num }.toSet()) ?: stringResource(Res.string.policy_every_day)
    val target = stringResource(if (selected) Res.string.info_limit_selected else Res.string.info_limit_all)
    val res = if (rule.window == LimitWindow.HOUR) Res.string.info_limit_hourly else Res.string.info_limit_daily
    return stringResource(res, days, target, durationText(rule.minutes))
}

/** "Har kuni 22:00 dan 07:00 gacha, farzandingiz belgilangan hudud ichida bo'lsa," — yoki null. */
@Composable
private fun PolicyInfo.gateText(): String? {
    val t = time?.let { r ->
        val days = daysText(r.days.map { it.num }.toSet()) ?: stringResource(Res.string.policy_every_day)
        stringResource(
            if (r.include) Res.string.info_when_time else Res.string.info_when_time_outside,
            days, r.startMin.asClock(), r.endMin.asClock(),
        )
    }
    val l = location?.let { stringResource(if (it.reverse) Res.string.info_where_outside else Res.string.info_where_inside) }
    return listOfNotNull(t, l).joinToString(", ").ifBlank { null }
}

@Composable
private fun PolicyInfo.whatText(): String = when (what) {
    InfoWhat.ALL_APPS ->
        if (closed.sites > 0) stringResource(Res.string.info_what_all_apps_plus_sites, closed.text())
        else stringResource(Res.string.info_what_all_apps)
    InfoWhat.ALL_SITES -> stringResource(Res.string.info_what_all_sites)
    InfoWhat.ALL_APPS_AND_SITES -> stringResource(Res.string.info_what_all_apps_sites)
    InfoWhat.SELECTED -> stringResource(Res.string.info_what_selected, closed.text())
    InfoWhat.NOTHING -> ""
}

@Composable
private fun TargetCounts.text(): String = listOfNotNull(
    apps.takeIf { it > 0 }?.let { stringResource(Res.string.policy_n_apps, it) },
    categories.takeIf { it > 0 }?.let { stringResource(Res.string.policy_n_categories, it) },
    sites.takeIf { it > 0 }?.let { stringResource(Res.string.policy_n_sites, it) },
).joinToString(", ")

private fun String.capitalized(): String = replaceFirstChar { it.titlecase() }
