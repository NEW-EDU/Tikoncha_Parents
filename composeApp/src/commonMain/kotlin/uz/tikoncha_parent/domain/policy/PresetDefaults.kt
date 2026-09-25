package uz.tikoncha_parent.domain.policy

import uz.tikoncha_parent.domain.model.LimitWindow
import uz.tikoncha_parent.domain.model.WeekDay
import uz.tikoncha_parent.domain.model.policy.PolicyAction
import uz.tikoncha_parent.domain.model.policy.PolicyConditions
import uz.tikoncha_parent.domain.model.policy.PolicyDraft
import uz.tikoncha_parent.domain.model.policy.PolicyLimits
import uz.tikoncha_parent.domain.model.policy.PolicyPreset
import uz.tikoncha_parent.domain.model.policy.PolicyTargets
import uz.tikoncha_parent.domain.model.policy.TimeCondition
import uz.tikoncha_parent.domain.model.policy.UsageLimit

/**
 * Tayyor jadvallar — switch birinchi marta yoqilganda shu qiymatlar bilan yaratiladi
 * (Student `PresetDefaults` bilan bir xil vaqt va nishonlar).
 *
 * Farqi: "ochiq qoladi" ilovalar ALLOW oq ro'yxat emas, `DENY "*"` + `exclude_packages`
 * sifatida saqlanadi — natija bir xil, lekin server ota-onaning ALLOW jadvalini faqat
 * Plus bilan qabul qiladi; Uyqu vaqti bepul tarifda ham yoqilsin.
 */
object PresetDefaults {

    val ALL_DAYS: Set<WeekDay> = WeekDay.entries.toSet()
    val WORK_DAYS: Set<WeekDay> = setOf(WeekDay.MON, WeekDay.TUE, WeekDay.WED, WeekDay.THU, WeekDay.FRI)

    /** Dars vaqtida yopiladigan meta-kategoriyalar (server UPPER kodlari). */
    val SCHOOL_CATEGORIES: List<String> = listOf("GAMES", "SOCIAL")

    /** Uyqu vaqti: har kuni 22:00–07:00, barcha ilovalar yopiq; [openPackages] ochiq qoladi. */
    fun sleep(title: String, openPackages: List<String> = emptyList()): PolicyDraft = PolicyDraft(
        name = title,
        action = PolicyAction.DENY,
        preset = PolicyPreset.SLEEP,
        targets = PolicyTargets(packages = listOf(PolicyTargets.ALL_APPS), excludePackages = openPackages.distinct()),
        conditions = PolicyConditions(time = TimeCondition(days = ALL_DAYS, startMin = 22 * 60, endMin = 7 * 60)),
    )

    /** Vaqt limiti: barcha ilovalar kuniga 1 soat; [notCounted] (qo'ng'iroq, SMS) hisoblanmaydi. */
    fun appLimit(title: String, notCounted: List<String> = emptyList()): PolicyDraft = PolicyDraft(
        name = title,
        action = PolicyAction.DENY,
        preset = PolicyPreset.APP_LIMIT,
        targets = PolicyTargets(packages = listOf(PolicyTargets.ALL_APPS), excludePackages = notCounted.distinct()),
        limits = PolicyLimits(usage = UsageLimit(days = ALL_DAYS, window = LimitWindow.DAY, minutes = 60)),
    )

    /** Dars vaqti: Du–Ju 08:00–14:00, o'yinlar va ijtimoiy tarmoqlar yopiq. */
    fun school(title: String): PolicyDraft = PolicyDraft(
        name = title,
        action = PolicyAction.DENY,
        preset = PolicyPreset.SCHOOL,
        targets = PolicyTargets(categories = SCHOOL_CATEGORIES),
        conditions = PolicyConditions(time = TimeCondition(days = WORK_DAYS, startMin = 8 * 60, endMin = 14 * 60)),
    )

    fun of(preset: PolicyPreset, title: String): PolicyDraft? = when (preset) {
        PolicyPreset.SLEEP -> sleep(title)
        PolicyPreset.APP_LIMIT -> appLimit(title)
        PolicyPreset.SCHOOL -> school(title)
        else -> null
    }
}
