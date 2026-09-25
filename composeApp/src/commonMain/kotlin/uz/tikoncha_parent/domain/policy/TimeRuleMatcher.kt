package uz.tikoncha_parent.domain.policy

import uz.tikoncha_parent.domain.model.policy.TimeCondition

/**
 * Bitta vaqt oynasini "hozir" ga solishtiradi — backend `time_matches` va Student
 * `TimeRuleMatcher` porti.
 *
 *  · `include = true`  → ro'yxatdagi kunlarda oyna ICHIDA
 *  · `include = false` → ro'yxatdagi kunlarda oynadan TASHQARIDA
 *  · tungi oyna (`startMin >= endMin`) BOSHLANGAN kunga tegishli:
 *    Juma 22:00–06:00 → juma kechasi va shanba tongi
 *  · ro'yxatda bo'lmagan kun → hech qachon mos kelmaydi (teskari bo'lsa ham)
 *  · 1439 "kun oxiri" — 1440 deb olinadi, aks holda 23:59 da oyna uzilardi.
 */
object TimeRuleMatcher {

    /** @param weekDay 1 = Dushanba … 7 = Yakshanba; @param minuteOfDay 0..1439 */
    fun matches(weekDay: Int, minuteOfDay: Int, rule: TimeCondition): Boolean {
        val end = if (rule.endMin >= TimeCondition.END_OF_DAY_SENTINEL) TimeCondition.MINUTES_PER_DAY else rule.endMin
        val overnight = rule.startMin >= end
        var appliesToday = false
        var inside = false
        for (day in rule.days.map { it.num }) {
            if (day == weekDay) {
                appliesToday = true
                inside = inside || if (!overnight) minuteOfDay in rule.startMin until end else minuteOfDay >= rule.startMin
            } else if (overnight && day == previousDay(weekDay)) {
                appliesToday = true
                inside = inside || minuteOfDay < end
            }
        }
        if (!appliesToday) return false
        return if (rule.include) inside else !inside
    }

    private fun previousDay(weekDay: Int): Int = if (weekDay == 1) 7 else weekDay - 1
}
