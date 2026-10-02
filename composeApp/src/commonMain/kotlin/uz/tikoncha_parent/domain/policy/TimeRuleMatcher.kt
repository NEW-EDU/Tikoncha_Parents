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
 *  · ro'yxatda bo'lmagan kun → `include = true` bo'lsa faqat oldingi kunning
 *    tungi davomi; `include = false` bo'lsa hech qachon (ilgari "Du 22–06 dan
 *    tashqari" seshanba 06:00 dan keyin ham amal qilardi)
 *  · 1439 "kun oxiri" — 1440 deb olinadi, aks holda 23:59 da oyna uzilardi.
 */
object TimeRuleMatcher {

    /** @param weekDay 1 = Dushanba … 7 = Yakshanba; @param minuteOfDay 0..1439 */
    fun matches(weekDay: Int, minuteOfDay: Int, rule: TimeCondition): Boolean {
        val (today, spill) = window(weekDay, minuteOfDay, rule)
        return if (rule.include) today || spill
        else rule.days.any { it.num == weekDay } && !today && !spill
    }

    /**
     * 1 — tungi oynaning yarim tundan keyingi qismi: limit kuni (va kunlik byudjeti)
     * oyna boshlangan kun, ya'ni kecha. Aks holda 0.
     */
    fun occurrenceDayOffset(weekDay: Int, minuteOfDay: Int, rule: TimeCondition): Int {
        if (!rule.include) return 0
        val (today, spill) = window(weekDay, minuteOfDay, rule)
        return if (spill && !today) 1 else 0
    }

    fun previousDay(weekDay: Int): Int = if (weekDay == 1) 7 else weekDay - 1

    /** (bugungi oyna ichida, kechagi tungi oynaning davomida) */
    private fun window(weekDay: Int, minuteOfDay: Int, rule: TimeCondition): Pair<Boolean, Boolean> {
        val end = if (rule.endMin >= TimeCondition.END_OF_DAY_SENTINEL) TimeCondition.MINUTES_PER_DAY else rule.endMin
        val overnight = rule.startMin >= end
        val days = rule.days.map { it.num }
        val today = weekDay in days &&
            if (!overnight) minuteOfDay in rule.startMin until end else minuteOfDay >= rule.startMin
        val spill = overnight && previousDay(weekDay) in days && minuteOfDay < end
        return today to spill
    }
}
