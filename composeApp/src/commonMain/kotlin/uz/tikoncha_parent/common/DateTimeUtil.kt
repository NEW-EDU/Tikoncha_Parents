package uz.tikoncha_parent.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.Month
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.number
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.todayIn
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import tikoncha_parents.composeapp.generated.resources.Res
import tikoncha_parents.composeapp.generated.resources.month_april
import tikoncha_parents.composeapp.generated.resources.month_august
import tikoncha_parents.composeapp.generated.resources.month_december
import tikoncha_parents.composeapp.generated.resources.month_february
import tikoncha_parents.composeapp.generated.resources.month_january
import tikoncha_parents.composeapp.generated.resources.month_july
import tikoncha_parents.composeapp.generated.resources.month_june
import tikoncha_parents.composeapp.generated.resources.month_march
import tikoncha_parents.composeapp.generated.resources.month_may
import tikoncha_parents.composeapp.generated.resources.month_november
import tikoncha_parents.composeapp.generated.resources.month_october
import tikoncha_parents.composeapp.generated.resources.month_september
import tikoncha_parents.composeapp.generated.resources.weekday_friday
import tikoncha_parents.composeapp.generated.resources.weekday_monday
import tikoncha_parents.composeapp.generated.resources.weekday_saturday
import tikoncha_parents.composeapp.generated.resources.weekday_sunday
import tikoncha_parents.composeapp.generated.resources.weekday_thursday
import tikoncha_parents.composeapp.generated.resources.weekday_tuesday
import tikoncha_parents.composeapp.generated.resources.weekday_wednesday
import uz.tikoncha_parent.presentation.domain.model.LanguageType
import uz.tikoncha_parent.presentation.profile.language.LanguagePrefs
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
object DateTimeUtil {

    fun Int.two(): String = if (this < 10) "0$this" else toString()

    fun toMillis(s: String?): Long {
        if (s.isNullOrBlank()) return 0L
        val input = s.trim()

        // Capture date, time, and optional fractional seconds (any length)
        val re = Regex(
            pattern = """^(\d{4})-(\d{2})-(\d{2})T(\d{2}):(\d{2}):(\d{2})(?:\.(\d{1,}))?""",
            option = RegexOption.IGNORE_CASE
        )

        val m = re.find(input) ?: return 0L

        val year = m.groupValues[1].toInt()
        val month = m.groupValues[2].toInt()
        val day = m.groupValues[3].toInt()
        val hour = m.groupValues[4].toInt()
        val minute = m.groupValues[5].toInt()
        val second = m.groupValues[6].toInt()

        val frac = m.groupValues.getOrNull(7).orEmpty()
        // milliseconds = first 3 digits (truncate) or pad with zeros if shorter
        val ms = when {
            frac.isEmpty() -> 0
            frac.length >= 3 -> frac.substring(0, 3).toInt()
            else -> (frac + "000").substring(0, 3).toInt()
        }

        val ldt = LocalDateTime(
            year, month, day,
            hour, minute, second,
            ms * 1_000_000 // nanos
        )

        // Naive timestamp'ni MAHALLIY vaqt deb o'qiydi (server local yuborsa — shu).
        // Server UTC yuborsa toMillisUtc() ishlating.
        return ldt.toInstant(TimeZone.currentSystemDefault()).toEpochMilliseconds()
    }

    fun toMillisUtc(s: String?): Long {
        if (s.isNullOrBlank()) return 0L
        val input = s.trim()

        // Capture date, time, and optional fractional seconds (any length)
        val re = Regex(
            pattern = """^(\d{4})-(\d{2})-(\d{2})T(\d{2}):(\d{2}):(\d{2})(?:\.(\d{1,}))?""",
            option = RegexOption.IGNORE_CASE
        )

        val m = re.find(input) ?: return 0L

        val year = m.groupValues[1].toInt()
        val month = m.groupValues[2].toInt()
        val day = m.groupValues[3].toInt()
        val hour = m.groupValues[4].toInt()
        val minute = m.groupValues[5].toInt()
        val second = m.groupValues[6].toInt()

        val frac = m.groupValues.getOrNull(7).orEmpty()
        // milliseconds = first 3 digits (truncate) or pad with zeros if shorter
        val ms = when {
            frac.isEmpty() -> 0
            frac.length >= 3 -> frac.substring(0, 3).toInt()
            else -> (frac + "000").substring(0, 3).toInt()
        }

        val ldt = LocalDateTime(
            year, month, day,
            hour, minute, second,
            ms * 1_000_000 // nanos
        )

        // Interpret the naive timestamp as UTC (no offset in the string)
        return ldt.toInstant(TimeZone.UTC).toEpochMilliseconds()
    }

    fun toMillisForChild(s: String?): Long {
        if (s.isNullOrBlank()) return 0L
        val input = s.trim()

        // 1) ISO: 2025-12-19T15:54:05(.123)
        run {
            val iso =
                Regex("""^(\d{4})-(\d{2})-(\d{2})[T ](\d{2}):(\d{2}):(\d{2})(?:\.(\d{1,}))?""")
            val m = iso.find(input)
            if (m != null) {
                val year = m.groupValues[1].toInt()
                val month = m.groupValues[2].toInt()
                val day = m.groupValues[3].toInt()
                val hour = m.groupValues[4].toInt()
                val minute = m.groupValues[5].toInt()
                val second = m.groupValues[6].toInt()

                val frac = m.groupValues.getOrNull(7).orEmpty()
                val ms = when {
                    frac.isEmpty() -> 0
                    frac.length >= 3 -> frac.substring(0, 3).toInt()
                    else -> (frac + "000").substring(0, 3).toInt()
                }

                val ldt = LocalDateTime(year, month, day, hour, minute, second, ms * 1_000_000)

                // ISO odatda UTC bo‘ladi yoki offset bilan keladi (siznikida offset yo‘q)
                // shuning uchun bu joyni UTC qoldiramiz:
                return ldt.toInstant(TimeZone.UTC).toEpochMilliseconds()
            }
        }

        // 2) API format: 19.12.2025 17:38:29  (dd.MM.yyyy HH:mm:ss)
        run {
            val api = Regex("""^(\d{2})\.(\d{2})\.(\d{4})\s+(\d{2}):(\d{2}):(\d{2})$""")
            val m = api.find(input)
            if (m != null) {
                val day = m.groupValues[1].toInt()
                val month = m.groupValues[2].toInt()
                val year = m.groupValues[3].toInt()
                val hour = m.groupValues[4].toInt()
                val minute = m.groupValues[5].toInt()
                val second = m.groupValues[6].toInt()

                val ldt = LocalDateTime(year, month, day, hour, minute, second, 0)

                // ✅ API vaqti lokal deb hisoblaymiz (Toshkent)
                return ldt.toInstant(TimeZone.of("Asia/Tashkent")).toEpochMilliseconds()
                // yoki: TimeZone.currentSystemDefault()
            }
        }

        return 0L
    }


    private fun formatTimeHHmm(t: LocalTime): String {
        val h = t.hour
        val m = t.minute
        return "${h.two()}${":"}${m.two()}"
    }

    fun formatTime(millis: Long): String {
        val t = Instant.fromEpochMilliseconds(millis)
            .toLocalDateTime(TimeZone.currentSystemDefault()).time
        return "${t.hour.two()}:${t.minute.two()}"
    }

    fun formatLocalTime(millis: Long): LocalTime {
        val t = Instant.fromEpochMilliseconds(millis)
            .toLocalDateTime(TimeZone.currentSystemDefault()).time
        return LocalTime(hour = t.hour, minute = t.minute)
    }


    private fun formatDate_ddMMyyyy(d: LocalDate): String {
        val day = d.day
        val mon = d.month.ordinal
        val yr = d.year
        return "${day.two()}.${mon.two()}.$yr"
    }


    fun formatDayMonthLocalized(
        localDate: LocalDate?,
        lang: LanguageType,     // UZ yoki RU
        bugun: String,
        kecha: String,
        timeZone: TimeZone = TimeZone.currentSystemDefault()
    ): String {
        if (localDate == null) return ""

        val today = Clock.System.todayIn(timeZone)
        val yesterday = today.minus(1, DateTimeUnit.DAY)

        return when (localDate) {
            today -> bugun
            yesterday -> kecha
            else -> formatDayMonthLocal(localDate, lang)
        }
    }

    fun formatDayMonthLocal(date: LocalDate, lang: LanguageType): String {


        val months = when (lang) {
            LanguageType.RU -> ruMonths
            LanguageType.UZ -> uzMonths
            LanguageType.EN -> enMonths
        }

        val day = date.day
        val monthName = months[date.month.ordinal]

        return "$day $monthName"
    }

    fun formatDateTimeForChatUserStatus(
        millis: Long,
        bugun: String,
        kecha: String,
        zone: TimeZone = TimeZone.currentSystemDefault()
    ): String {
        if (millis == 0L) return ""

        val instant = Instant.fromEpochMilliseconds(millis)
        val localDT = instant.toLocalDateTime(zone)
        val date = localDT.date

        val nowDate = Clock.System.now().toLocalDateTime(zone).date
        val yesterday = nowDate.minus(DatePeriod(days = 1))

        return when (date) {
            nowDate -> "$bugun ${formatTimeHHmm(localDT.time)}"
            yesterday -> "$kecha ${formatTimeHHmm(localDT.time)}"
            else -> formatDate_ddMMyyyy(date)
        }
    }


    fun formatDateTimeMonthlyForMap(
        longDate: Long,
        lang: LanguageType,
        zone: TimeZone = TimeZone.currentSystemDefault()
    ): String {
        if (longDate == 0L) return ""

        val instant = Instant.fromEpochMilliseconds(longDate)

        val date = instant.toLocalDateTime(zone).date
        val today = Clock.System.now().toLocalDateTime(zone).date
        val time = formatTime(millis = longDate)

        val todayString = when (lang) {
            LanguageType.UZ -> {
                "Bugun"
            }

            LanguageType.RU -> {
                "Сегодня"
            }

            LanguageType.EN -> {
                "Today"
            }
        }



        return if (date == today) {
            "$todayString $time"
        } else {
            val formattedDate = formatDayMonth(longDate, lang, zone)
            "$formattedDate $time"
        }
    }

    fun formatDateTimeMonthlyForChat(
        longDate: Long,
        lang: LanguageType,
        zone: TimeZone = TimeZone.currentSystemDefault()
    ): String {
        if (longDate == 0L) return ""

        val instant = Instant.fromEpochMilliseconds(longDate)

        val date = instant.toLocalDateTime(zone).date
        val today = Clock.System.now().toLocalDateTime(zone).date

        return if (date == today)
            formatTime(longDate)
        else
            formatDayMonth(longDate, lang, zone)
    }

    fun Month.localized(lang: LanguageType): String {
        return when (lang) {
            LanguageType.UZ -> when (this) {
                Month.JANUARY -> "Yanvar"
                Month.FEBRUARY -> "Fevral"
                Month.MARCH -> "Mart"
                Month.APRIL -> "Aprel"
                Month.MAY -> "May"
                Month.JUNE -> "Iyun"
                Month.JULY -> "Iyul"
                Month.AUGUST -> "Avgust"
                Month.SEPTEMBER -> "Sentabr"
                Month.OCTOBER -> "Oktabr"
                Month.NOVEMBER -> "Noyabr"
                Month.DECEMBER -> "Dekabr"
            }

            LanguageType.RU -> when (this) {
                Month.JANUARY -> "Января"
                Month.FEBRUARY -> "Февраля"
                Month.MARCH -> "Марта"
                Month.APRIL -> "Апреля"
                Month.MAY -> "Мая"
                Month.JUNE -> "Июня"
                Month.JULY -> "Июля"
                Month.AUGUST -> "Августа"
                Month.SEPTEMBER -> "Сентября"
                Month.OCTOBER -> "Октября"
                Month.NOVEMBER -> "Ноября"
                Month.DECEMBER -> "Декабря"
            }

            else -> this.name.lowercase().replaceFirstChar { it.uppercase() }
        }
    }


    fun formatDayMonth(
        millis: Long,
        lang: LanguageType,
        zone: TimeZone = TimeZone.currentSystemDefault()
    ): String {


        if (millis == 0L) return ""

        val date = Instant.fromEpochMilliseconds(millis)
            .toLocalDateTime(zone).date

        val months = when (lang) {
            LanguageType.RU -> ruMonths
            LanguageType.UZ -> uzMonths
            LanguageType.EN -> enMonths
        }

        val day = date.day
        val monthName = months[date.month.ordinal]

        return "$day-$monthName"
    }

    @Composable
    fun reformattedDayMonthWithWeekdayForTask(
        date: LocalDate?
    ): String {
        if (date == null) return ""

        val months = mapOf(
            Month.JANUARY to Res.string.month_january,
            Month.FEBRUARY to Res.string.month_february,
            Month.MARCH to Res.string.month_march,
            Month.APRIL to Res.string.month_april,
            Month.MAY to Res.string.month_may,
            Month.JUNE to Res.string.month_june,
            Month.JULY to Res.string.month_july,
            Month.AUGUST to Res.string.month_august,
            Month.SEPTEMBER to Res.string.month_september,
            Month.OCTOBER to Res.string.month_october,
            Month.NOVEMBER to Res.string.month_november,
            Month.DECEMBER to Res.string.month_december,
        )

        val weekdays = mapOf(
            DayOfWeek.MONDAY to Res.string.weekday_monday,
            DayOfWeek.TUESDAY to Res.string.weekday_tuesday,
            DayOfWeek.WEDNESDAY to Res.string.weekday_wednesday,
            DayOfWeek.THURSDAY to Res.string.weekday_thursday,
            DayOfWeek.FRIDAY to Res.string.weekday_friday,
            DayOfWeek.SATURDAY to Res.string.weekday_saturday,
            DayOfWeek.SUNDAY to Res.string.weekday_sunday
        )

        val day = date.day.toString().padStart(2, '0')

        val monthRes = months[date.month] ?: return ""
        val weekdayRes = weekdays[date.dayOfWeek] ?: return ""

        val monthName = stringResource(monthRes)        // ← StringResource → String
        val weekdayName = stringResource(weekdayRes)    // ← StringResource → String

        return "$day-$monthName, $weekdayName"
    }

    @Composable
    fun reformattedToday(date: LocalDate?): String {
        if (date == null) return ""

        val months = mapOf(
            Month.JANUARY to Res.string.month_january,
            Month.FEBRUARY to Res.string.month_february,
            Month.MARCH to Res.string.month_march,
            Month.APRIL to Res.string.month_april,
            Month.MAY to Res.string.month_may,
            Month.JUNE to Res.string.month_june,
            Month.JULY to Res.string.month_july,
            Month.AUGUST to Res.string.month_august,
            Month.SEPTEMBER to Res.string.month_september,
            Month.OCTOBER to Res.string.month_october,
            Month.NOVEMBER to Res.string.month_november,
            Month.DECEMBER to Res.string.month_december,
        )

        val monthRes = months[date.month] ?: return ""
        val monthName = stringResource(monthRes)

        return "${date.day} - $monthName"
    }

    fun formatTime(localTime: LocalTime?): String {
        if (localTime == null) return ""

        val hour = localTime.hour.toString().padStart(2, '0')
        val minute = localTime.minute.toString().padStart(2, '0')

        return "$hour:$minute"
    }

    fun formatToIsoString(
        localDate: LocalDate?,
        localTime: LocalTime?,
        timeZone: TimeZone = TimeZone.currentSystemDefault()
    ): String {
        if (localTime == null || localDate == null) {
            return ""
        }
        // Combine LocalDate + LocalTime into LocalDateTime
        val localDateTime = LocalDateTime(localDate, localTime)
        val instant = localDateTime.toInstant(TimeZone.currentSystemDefault())
        val utcLdt = instant.toLocalDateTime(timeZone)


        val year = utcLdt.year.toString().padStart(4, '0')
        val month = (utcLdt.month.ordinal + 1).toString().padStart(2, '0')
        val day = utcLdt.day.toString().padStart(2, '0')
        val hour = utcLdt.hour.toString().padStart(2, '0')
        val minute = utcLdt.minute.toString().padStart(2, '0')
        val second = utcLdt.second.toString().padStart(2, '0')
        val millis = (utcLdt.nanosecond / 1_000_000).toString().padStart(3, '0')

        return "$year-$month-${day}T$hour:$minute:$second.${millis}"
    }

    fun formatToIsoString(
        millis: Long,
        timeZone: TimeZone = TimeZone.currentSystemDefault()
    ): String {
        println("formatToIsoString millis=$millis")
        val instant = Instant.fromEpochMilliseconds(millis)
        val localDateTime = instant.toLocalDateTime(timeZone)


        val year = localDateTime.year.toString().padStart(4, '0')
        val month = (localDateTime.month.ordinal + 1).toString().padStart(2, '0')
        val day = localDateTime.day.toString().padStart(2, '0')
        val hour = localDateTime.hour.toString().padStart(2, '0')
        val minute = localDateTime.minute.toString().padStart(2, '0')
        val second = localDateTime.second.toString().padStart(2, '0')
        val millis = (localDateTime.nanosecond / 1_000_000).toString().padStart(3, '0')

        return "$year-$month-${day}T$hour:$minute:$second.${millis}"
    }


    fun getCurrentIsoDateTime(timeZone: TimeZone = TimeZone.currentSystemDefault()): String {
        val nowInstant = Clock.System.now()
        val localDateTime = nowInstant.toLocalDateTime(timeZone)

        val year = localDateTime.year.toString().padStart(4, '0')
        val month = (localDateTime.month.ordinal + 1).toString().padStart(2, '0')
        val day = localDateTime.day.toString().padStart(2, '0')
        val hour = localDateTime.hour.toString().padStart(2, '0')
        val minute = localDateTime.minute.toString().padStart(2, '0')
        val second = localDateTime.second.toString().padStart(2, '0')
        val millis = (localDateTime.nanosecond / 1_000_000).toString().padStart(3, '0')

        return "$year-$month-${day}T$hour:$minute:$second.${millis}"
    }

    fun String.fromServerToLocalDateTime(): LocalDateTime =
        runCatching { Instant.parse(this).toLocalDateTime(TimeZone.currentSystemDefault()) }
            .getOrElse { Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()) }


    fun String?.serverDateTimeToMillis(defaultTimeZone: TimeZone = TimeZone.currentSystemDefault()): Long {
        if (this == null) {
            return 0
        }
        val raw = this.trim()
        if (raw.isEmpty()) return 0L

        return try {
            // Offset bormi? (Z yoki +HH:MM / +HHMM)
            val hasZone = raw.endsWith("Z") ||
                    Regex("[+-]\\d{2}:?\\d{2}$").containsMatchIn(raw)

            // Fraksiyani 9 digit (nanosecond) ga normallashtiramiz, offsetni HHMM -> HH:MM ga
            val normalized = raw
                // ".123" yoki ".123000" -> ".123000000"
                .replace(Regex("""\.(\d{1,9})""")) { m ->
                    val frac = m.groupValues[1]
                    "." + (frac + "000000000").take(9)
                }
                // "+0500" -> "+05:00"
                .replace(Regex("""([+-]\d{2})(\d{2})$"""), "$1:$2")

            if (hasZone) {
                // Z yoki offset bor: bevosita Instant
                Instant.parse(normalized).toEpochMilliseconds()
            } else {
                // Zona yo‘q: LocalDateTime sifatida parse qilib, defaultTimeZone bilan Instantga o‘tkazamiz
                val re = Regex(
                    """^(\d{4})-(\d{2})-(\d{2})T""" +         // yyyy-MM-ddT
                            """(\d{2}):(\d{2})""" +                   // HH:mm
                            """(?::(\d{2})(?:\.(\d{1,9}))?)?$"""      // [:ss[.fffffffff]]
                )
                val m = re.matchEntire(normalized) ?: return 0L

                val year = m.groupValues[1].toInt()
                val month = m.groupValues[2].toInt()
                val day = m.groupValues[3].toInt()
                val hour = m.groupValues[4].toInt()
                val minute = m.groupValues[5].toInt()
                val second = m.groupValues.getOrNull(6)?.takeIf { it.isNotEmpty() }?.toInt() ?: 0
                val nano = m.groupValues.getOrNull(7)?.takeIf { it.isNotEmpty() }?.let {
                    (it + "000000000").take(9).toInt()
                } ?: 0

                val ldt = LocalDateTime(year, month, day, hour, minute, second, nano)
                ldt.toInstant(defaultTimeZone).toEpochMilliseconds()
            }
        } catch (_: Throwable) {
            0L
        }
    }

    fun reformattedYearDay(reformatedDate: LocalDate?): String {
        if (reformatedDate == null) return ""

        val day = reformatedDate.day.toString().padStart(2, '0')
        val month = reformatedDate.month.number.toString().padStart(2, '0')
        val year = reformatedDate.year.toString()

        return "$day.$month.$year"
    }


    fun LocalDateTime.toUIData(): String {
        val d = date
        return "${d.day.toString().padStart(2, '0')}." +
                "${d.month.number.toString().padStart(2, '0')}." +
                d.year.toString().padStart(4, '0')
    }

    fun LocalDateTime.toUiTime(): String =
        "${hour.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')}"


    fun nowMillis(): Long {
        return Clock.System.now().toEpochMilliseconds()
    }


    // 5-apel, Chorshanba Formati uchun
    @Composable
    fun formatDayMonthWithWeekday(epochMs: Long?): String {
        if (epochMs == null || epochMs <= 0L) return ""

        val instant = Instant.fromEpochMilliseconds(epochMs)
        val dateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())

        val day = dateTime.day
        val monthRes = monthStringRes(dateTime.monthNumber)
        val monthName = if (monthRes != null) stringResource(monthRes) else ""

        val weekdayRes = weekdayStringRes(dateTime.dayOfWeek)
        val weekdayName = if (weekdayRes != null) stringResource(weekdayRes) else ""

        return "$day-$monthName, $weekdayName"
    }

    private fun monthStringRes(monthNumber: Int): StringResource? = when (monthNumber) {
        1 -> Res.string.month_january
        2 -> Res.string.month_february
        3 -> Res.string.month_march
        4 -> Res.string.month_april
        5 -> Res.string.month_may
        6 -> Res.string.month_june
        7 -> Res.string.month_july
        8 -> Res.string.month_august
        9 -> Res.string.month_september
        10 -> Res.string.month_october
        11 -> Res.string.month_november
        12 -> Res.string.month_december
        else -> null
    }

    private fun weekdayStringRes(dayOfWeek: DayOfWeek): StringResource? = when (dayOfWeek) {
        DayOfWeek.MONDAY -> Res.string.weekday_monday
        DayOfWeek.TUESDAY -> Res.string.weekday_tuesday
        DayOfWeek.WEDNESDAY -> Res.string.weekday_wednesday
        DayOfWeek.THURSDAY -> Res.string.weekday_thursday
        DayOfWeek.FRIDAY -> Res.string.weekday_friday
        DayOfWeek.SATURDAY -> Res.string.weekday_saturday
        DayOfWeek.SUNDAY -> Res.string.weekday_sunday
        else -> null
    }

    // 5-apel 2025, Chorshanba Formati uchun
    @Composable
    fun formatDayMonthYearWithWeekday(millis: Long): String {
        val ldt = remember(millis) {
            Instant.fromEpochMilliseconds(millis)
                .toLocalDateTime(TimeZone.currentSystemDefault())
        }

        val day = ldt.day.toString().padStart(2, '0')

        // ✅ Month enum'ni ishlatamiz — exhaustive, `else` kerak emas
        val month = stringResource(
            when (ldt.month) {
                Month.JANUARY   -> Res.string.month_january
                Month.FEBRUARY  -> Res.string.month_february
                Month.MARCH     -> Res.string.month_march
                Month.APRIL     -> Res.string.month_april
                Month.MAY       -> Res.string.month_may
                Month.JUNE      -> Res.string.month_june
                Month.JULY      -> Res.string.month_july
                Month.AUGUST    -> Res.string.month_august
                Month.SEPTEMBER -> Res.string.month_september
                Month.OCTOBER   -> Res.string.month_october
                Month.NOVEMBER  -> Res.string.month_november
                Month.DECEMBER  -> Res.string.month_december
            }
        )

        val weekday = stringResource(
            when (ldt.dayOfWeek) {
                DayOfWeek.MONDAY    -> Res.string.weekday_monday
                DayOfWeek.TUESDAY   -> Res.string.weekday_tuesday
                DayOfWeek.WEDNESDAY -> Res.string.weekday_wednesday
                DayOfWeek.THURSDAY  -> Res.string.weekday_thursday
                DayOfWeek.FRIDAY    -> Res.string.weekday_friday
                DayOfWeek.SATURDAY  -> Res.string.weekday_saturday
                DayOfWeek.SUNDAY    -> Res.string.weekday_sunday
            }
        )

        return "$day $month ${ldt.year}, $weekday"
    }


    val uzMonths = listOf(
        "Yanvar", "Fevral", "Mart", "Aprel", "May", "Iyun",
        "Iyul", "Avgust", "Sentyabr", "Oktyabr", "Noyabr", "Dekabr"
    )

    val ruMonths = listOf(
        "Января", "Февраля", "Марта", "Апреля", "Мая", "Июня",
        "Июля", "Августа", "Сентября", "Октября", "Ноября", "Декабря"
    )

    val enMonths = listOf(
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"
    )

    // ------------ Odamga qulay sana: "14-oktyabr 2027" / "14 октября 2027" / "14 October 2027" ------------

    /** Kun, oy nomi va yil — vaqtsiz. Til ilova tilidan. */
    fun formatLongDate(date: LocalDate, lang: LanguageType = LanguagePrefs.loadOrDefault()): String = when (lang) {
        LanguageType.UZ -> "${date.day}-${uzMonths[date.month.ordinal].lowercase()} ${date.year}"
        LanguageType.RU -> "${date.day} ${ruMonths[date.month.ordinal].lowercase()} ${date.year}"
        LanguageType.EN -> "${date.day} ${enMonths[date.month.ordinal]} ${date.year}"
    }

    /**
     * Serverdan kelgan sana satri ("14.10.2027 20:37:54", "2027-10-14", "2027-10-14T20:37:54…") → [formatLongDate].
     * Tanilmasa — satrning o'zi.
     */
    fun formatLongDate(raw: String?, lang: LanguageType = LanguagePrefs.loadOrDefault()): String {
        val s = raw?.trim().orEmpty()
        if (s.isEmpty()) return ""
        val dotted = Regex("""^(\d{1,2})\.(\d{1,2})\.(\d{4})""").find(s)
        val iso = Regex("""^(\d{4})-(\d{1,2})-(\d{1,2})""").find(s)
        val date = runCatching {
            when {
                dotted != null -> dotted.destructured.let { (d, m, y) -> LocalDate(y.toInt(), m.toInt(), d.toInt()) }
                iso != null -> iso.destructured.let { (y, m, d) -> LocalDate(y.toInt(), m.toInt(), d.toInt()) }
                else -> null
            }
        }.getOrNull() ?: return s
        return formatLongDate(date, lang)
    }

}