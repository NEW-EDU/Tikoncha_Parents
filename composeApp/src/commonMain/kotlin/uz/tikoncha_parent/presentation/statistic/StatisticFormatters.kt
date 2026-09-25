package uz.tikoncha_parent.presentation.statistic

import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import tikoncha_parents.composeapp.generated.resources.*
import uz.tikoncha_parent.domain.model.HourMinute
import uz.tikoncha_parent.domain.model.HourMinute.Companion.isEmpty

private val MonthRes: List<StringResource> = listOf(
    Res.string.month_january, Res.string.month_february, Res.string.month_march,
    Res.string.month_april,   Res.string.month_may,      Res.string.month_june,
    Res.string.month_july,    Res.string.month_august,   Res.string.month_september,
    Res.string.month_october, Res.string.month_november, Res.string.month_december
)

private val WeekdayShortRes: List<StringResource> = listOf(
    Res.string.dush, Res.string.sesh,
    Res.string.chor, Res.string.pay,
    Res.string.jum, Res.string.shan,
    Res.string.yak
)

@Composable
fun monthName(monthIndex: Int): String = stringResource(MonthRes[monthIndex.coerceIn(0, 11)])

@Composable
fun weekdayShort(index: Int): String = stringResource(WeekdayShortRes[index.coerceIn(0, 6)])

@Composable
fun durationString(hm: HourMinute): String {
    val hourText = pluralStringResource(Res.plurals.hours, hm.hour, hm.hour)
    val minText  = pluralStringResource(Res.plurals.minutes, hm.minute, hm.minute)
    val zeroText = pluralStringResource(Res.plurals.minutes, 0, 0)

    if (hm.isEmpty()) return zeroText

    return buildList {
        if (hm.hour > 0)   add(hourText)
        if (hm.minute > 0) add(minText)
    }.joinToString(" ")
}

@Composable
fun durationStringShort(hm: HourMinute): String {
    val min = stringResource(Res.string.d)
    val hour = stringResource(Res.string.s)
    if (hm.isEmpty())  return "0 $min"
    return buildList {
        if (hm.hour > 0)   add("${hm.hour} $hour")
        if (hm.minute > 0) add("${hm.minute} $min")
    }.joinToString("  ")
}

/**
 * Soat ham, daqiqa ham bo'lsa qisqa: "1 s 24 d".
 * Bittasi bo'lsa to'liq: "45 daqiqa", "2 soat".
 */
@Composable
fun durationStringCompact(hm: HourMinute): String {
    if (hm.hour > 0 && hm.minute > 0) {
        val hour = stringResource(Res.string.s)
        val min = stringResource(Res.string.d)
        return "${hm.hour} $hour  ${hm.minute} $min"
    }
    return durationString(hm)
}

@Composable
fun pageTitleString(title: PageTitle): String = when (title) {
    is PageTitle.Day -> {
        val base = "${title.day} - ${monthName(title.monthIndex)}"
        when (title.dayLabel) {
            DayLabel.TODAY     -> "${stringResource(Res.string.bugun)}, $base"
            DayLabel.YESTERDAY -> "${stringResource(Res.string.kecha)}, $base"
            DayLabel.NONE      -> base
        }
    }
    is PageTitle.Week -> {
        val sm = monthName(title.startMonthIndex).lowercase()
        val em = monthName(title.endMonthIndex).lowercase()
        "${title.startDay} $sm - ${title.endDay} $em"
    }
}

@Composable
fun chartSubtitleString(sub: ChartSubtitle): String = when (sub) {
    is ChartSubtitle.WeeklyAverage ->
        "${stringResource(Res.string.bir_kunda_o_rtacha)} ${durationString(sub.avgPerDay)}"
}

@Composable
fun usageDetailsTitleString(t: UsageDetailsTitle): String = when (t) {
    is UsageDetailsTitle.WeekdayDate ->
        "${weekdayShort(t.weekdayIndex)}, ${t.day} - ${monthName(t.monthIndex)}"
    is UsageDetailsTitle.HourRange ->
        "${t.day} - ${monthName(t.monthIndex)}, " +
                "${t.hourFrom.pad2()}:00 - ${t.hourToExclusive.pad2()}:00"
}

@Composable
fun barLabel(bar: ChartBarUi, mode: DateSelectionType): String = when (mode) {
    DateSelectionType.WEEK -> weekdayShort(bar.slotIndex)
    DateSelectionType.DAY  -> "${(bar.slotIndex * 2).pad2()}:00"
}
private fun Int.pad2(): String = if (this < 10) "0$this" else this.toString()