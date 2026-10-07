package uz.tikoncha_parent.presentation.policy.components

import tikoncha_parents.composeapp.generated.resources.policy_name_category_block
import tikoncha_parents.composeapp.generated.resources.policy_name_schedule
import tikoncha_parents.composeapp.generated.resources.quick_title
import tikoncha_parents.composeapp.generated.resources.policy_content_protection
import tikoncha_parents.composeapp.generated.resources.editor_new_policy
import uz.tikoncha_parent.domain.model.policy.PolicyName
import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.stringResource
import tikoncha_parents.composeapp.generated.resources.Res
import tikoncha_parents.composeapp.generated.resources.daqiqa
import tikoncha_parents.composeapp.generated.resources.list_info_also
import tikoncha_parents.composeapp.generated.resources.list_info_child_scope
import tikoncha_parents.composeapp.generated.resources.list_info_how
import tikoncha_parents.composeapp.generated.resources.list_info_many
import tikoncha_parents.composeapp.generated.resources.list_info_none
import tikoncha_parents.composeapp.generated.resources.list_info_paused
import tikoncha_parents.composeapp.generated.resources.list_info_quick
import tikoncha_parents.composeapp.generated.resources.list_info_school_scope
import tikoncha_parents.composeapp.generated.resources.policy_active_now
import tikoncha_parents.composeapp.generated.resources.policy_active_until
import tikoncha_parents.composeapp.generated.resources.policy_day_fri
import tikoncha_parents.composeapp.generated.resources.policy_day_mon
import tikoncha_parents.composeapp.generated.resources.policy_day_sat
import tikoncha_parents.composeapp.generated.resources.policy_day_sun
import tikoncha_parents.composeapp.generated.resources.policy_day_thu
import tikoncha_parents.composeapp.generated.resources.policy_day_tue
import tikoncha_parents.composeapp.generated.resources.policy_day_wed
import tikoncha_parents.composeapp.generated.resources.policy_every_day
import tikoncha_parents.composeapp.generated.resources.policy_location
import tikoncha_parents.composeapp.generated.resources.policy_n_apps
import tikoncha_parents.composeapp.generated.resources.policy_n_categories
import tikoncha_parents.composeapp.generated.resources.policy_n_sites
import tikoncha_parents.composeapp.generated.resources.policy_per_day
import tikoncha_parents.composeapp.generated.resources.policy_per_hour
import tikoncha_parents.composeapp.generated.resources.policy_preset_limit
import tikoncha_parents.composeapp.generated.resources.policy_preset_school
import tikoncha_parents.composeapp.generated.resources.policy_preset_sleep
import tikoncha_parents.composeapp.generated.resources.policy_tab_child
import tikoncha_parents.composeapp.generated.resources.policy_tab_coparent
import tikoncha_parents.composeapp.generated.resources.policy_tab_mine
import tikoncha_parents.composeapp.generated.resources.policy_tab_school
import tikoncha_parents.composeapp.generated.resources.policy_tab_templates
import tikoncha_parents.composeapp.generated.resources.policy_weekend
import tikoncha_parents.composeapp.generated.resources.policy_workdays
import tikoncha_parents.composeapp.generated.resources.preset_all_apps
import tikoncha_parents.composeapp.generated.resources.soat
import uz.tikoncha_parent.domain.model.LimitWindow
import uz.tikoncha_parent.presentation.policy.model.PolicyListInfo
import uz.tikoncha_parent.presentation.policy.model.PolicySummary
import uz.tikoncha_parent.presentation.policy.model.PolicyTab
import uz.tikoncha_parent.presentation.policy.model.PresetKind

/* Matnlar faqat Compose'da (ResourceProvider yo'q) — Student `PolicyTexts` bilan bir xil. */

@Composable
fun PresetKind.title(): String = stringResource(
    when (this) {
        PresetKind.SLEEP -> Res.string.policy_preset_sleep
        PresetKind.LIMIT -> Res.string.policy_preset_limit
        PresetKind.SCHOOL -> Res.string.policy_preset_school
    }
)

@Composable
fun durationText(minutes: Int): String {
    val h = minutes / 60
    val m = minutes % 60
    val hour = stringResource(Res.string.soat)
    val min = stringResource(Res.string.daqiqa)
    return when {
        h > 0 && m > 0 -> "$h $hour $m $min"
        h > 0 -> "$h $hour"
        else -> "$m $min"
    }
}

private val ALL_DAYS = (1..7).toSet()
private val WORK_DAYS = (1..5).toSet()
private val WEEKEND = setOf(6, 7)

private val DAY_SHORT = listOf(
    Res.string.policy_day_mon, Res.string.policy_day_tue, Res.string.policy_day_wed, Res.string.policy_day_thu,
    Res.string.policy_day_fri, Res.string.policy_day_sat, Res.string.policy_day_sun,
)

/** "Du" … "Ya" (ISO 1..7). */
@Composable
private fun shortDay(day: Int): String = stringResource(DAY_SHORT[day - 1])

@Composable
fun daysText(days: Set<Int>): String? {
    if (days.isEmpty()) return null
    val short = DAY_SHORT.map { stringResource(it) }
    return when (days) {
        ALL_DAYS -> stringResource(Res.string.policy_every_day)
        WORK_DAYS -> stringResource(Res.string.policy_workdays)
        WEEKEND -> stringResource(Res.string.policy_weekend)
        else -> days.filter { it in 1..7 }.sorted().joinToString(", ") { short[it - 1] }
    }
}

fun timeText(s: PolicySummary): String? {
    val a = s.startMin ?: return null
    val b = s.endMin ?: return null
    return "${a.asClock()} – ${b.asClock()}"
}

@Composable
fun limitText(s: PolicySummary): String? {
    val minutes = s.limitMinutes ?: return null
    val res = if (s.limitWindow == LimitWindow.HOUR) Res.string.policy_per_hour else Res.string.policy_per_day
    return stringResource(res, durationText(minutes))
}

@Composable
fun targetsText(s: PolicySummary): String? = listOfNotNull(
    stringResource(Res.string.preset_all_apps).takeIf { s.allApps },
    s.appCount.takeIf { it > 0 }?.let { stringResource(Res.string.policy_n_apps, it) },
    s.categoryCount.takeIf { it > 0 }?.let { stringResource(Res.string.policy_n_categories, it) },
    s.siteCount.takeIf { it > 0 }?.let { stringResource(Res.string.policy_n_sites, it) },
).takeIf { it.isNotEmpty() }?.joinToString(", ")

/**
 * Jadval kartasidagi BITTA qisqa qator (Student bilan bir xil): kunlar · vaqt; vaqt yo'q
 * bo'lsa kunlar · limit; ikkalasi ham yo'q bo'lsa nishon ("5 ta ilova"). Ilovalar soni,
 * joylashuv — jadval ichida. Dam olish kunlari kartada "Sh, Ya".
 */
@Composable
fun PolicySummary.cardText(): String {
    val days = if (days == WEEKEND) WEEKEND.sorted().map { shortDay(it) }.joinToString(", ") else daysText(days)
    val time = timeText(this)
    val limit = limitText(this)
    return when {
        time != null -> listOfNotNull(days, time).joinToString(" · ")
        limit != null -> listOfNotNull(days, limit).joinToString(" · ")
        else -> targetsText(this).orEmpty()
    }
}

/** Tayyor jadval — qisqa: Uyqu "22:00 – 07:00", Limit "Kuniga 1 soat", Dars "Ish kunlari · 08:00 – 14:00". */
@Composable
fun PresetKind.summaryText(s: PolicySummary): String = when (this) {
    PresetKind.SLEEP -> timeText(s).orEmpty()
    PresetKind.LIMIT -> limitText(s).orEmpty()
    PresetKind.SCHOOL -> listOfNotNull(daysText(s.days), timeText(s)).joinToString(" · ")
}

/**
 * Jadval nomi ilova tilida: tayyor jadval, tezkor blok va standart nomlar tarjima
 * qilinadi, odam yozgan nom o'zgarmaydi (`PolicyName`, Student bilan bir xil).
 */
@Composable
fun PolicyName.text(): String = when (kind) {
    PolicyName.Kind.CUSTOM -> raw.ifBlank { stringResource(Res.string.editor_new_policy) }
    PolicyName.Kind.SLEEP -> stringResource(Res.string.policy_preset_sleep)
    PolicyName.Kind.APP_LIMIT -> stringResource(Res.string.policy_preset_limit)
    PolicyName.Kind.SCHOOL -> stringResource(Res.string.policy_preset_school)
    PolicyName.Kind.CONTENT_PROTECTION -> stringResource(Res.string.policy_content_protection)
    PolicyName.Kind.QUICK_BLOCK -> stringResource(Res.string.quick_title)
    PolicyName.Kind.NEW_SCHEDULE -> stringResource(Res.string.editor_new_policy)
    PolicyName.Kind.SCHEDULE -> stringResource(Res.string.policy_name_schedule)
    PolicyName.Kind.CATEGORY_BLOCK -> stringResource(Res.string.policy_name_category_block)
}

@Composable
fun PolicyTab.title(): String = stringResource(
    when (this) {
        PolicyTab.TEMPLATES -> Res.string.policy_tab_templates
        PolicyTab.MINE -> Res.string.policy_tab_mine
        PolicyTab.CHILD -> Res.string.policy_tab_child
        PolicyTab.COPARENT -> Res.string.policy_tab_coparent
        PolicyTab.SCHOOL -> Res.string.policy_tab_school
    }
)

/** "Hozir amalda: Uyqu vaqti · 07:00 gacha" yoki "Hozir hech qaysi jadval ishlamayapti". */
@Composable
fun PolicyListInfo.title(): String {
    val main = activeTitles.firstOrNull()?.text() ?: return stringResource(Res.string.list_info_none)
    val until = activeUntilMin?.let { " · " + stringResource(Res.string.policy_active_until, it.asClock()) }.orEmpty()
    return stringResource(Res.string.policy_active_now, main) + until
}

/** Qolgan faollar, bir nechta jadval qoidasi, farzand/maktab ustunligi, tezkor blok, pauza. */
@Composable
fun PolicyListInfo.lines(): List<String> = buildList {
    val others = activeTitles.drop(1)
    if (others.isNotEmpty()) add(stringResource(Res.string.list_info_also, others.map { it.text() }.joinToString(", ")))
    val activeCount = activeTitles.size + if (quickActive) 1 else 0
    if (activeCount >= 2) add(stringResource(Res.string.list_info_many))
    if (hasChildPolicies) add(stringResource(Res.string.list_info_child_scope))
    if (hasSchoolPolicies) add(stringResource(Res.string.list_info_school_scope))
    if (quickActive) add(stringResource(Res.string.list_info_quick))
    if (pausedCount > 0) add(stringResource(Res.string.list_info_paused, pausedCount))
    if (activeTitles.isEmpty() && !quickActive) add(stringResource(Res.string.list_info_how))
}
