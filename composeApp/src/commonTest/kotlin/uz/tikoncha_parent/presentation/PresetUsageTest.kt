package uz.tikoncha_parent.presentation

import kotlinx.datetime.LocalDate
import uz.tikoncha_parent.domain.model.app_usage.AppUsage
import uz.tikoncha_parent.domain.model.app_usage.UsageHistory
import uz.tikoncha_parent.domain.model.policy.PolicyTargets
import uz.tikoncha_parent.presentation.policy.preset.usedMinutes
import kotlin.test.Test
import kotlin.test.assertEquals

class PresetUsageTest {

    private val today = LocalDate(2026, 9, 25)
    private val yesterday = LocalDate(2026, 9, 24)
    private val min = 60_000L

    private fun app(pkg: String, vararg days: Pair<LocalDate, Map<Int, Long>>) =
        AppUsage(packageName = pkg, name = pkg, iconUrl = null, usage = days.toMap())

    private val history = UsageHistory(
        listOf(
            app("com.youtube", today to mapOf(9 to 20 * min, 14 to 15 * min), yesterday to mapOf(14 to 50 * min)),
            app("com.pubg", today to mapOf(14 to 30 * min)),
            app("com.whatsapp", today to mapOf(14 to 5 * min)),
        )
    )
    private val categoryOf = mapOf("com.youtube" to "VIDEO", "com.pubg" to "games", "com.whatsapp" to "SOCIAL")

    @Test
    fun allAppsCountsEverythingExceptExceptions() {
        val t = PolicyTargets(packages = listOf(PolicyTargets.ALL_APPS), excludePackages = listOf("com.whatsapp"))
        assertEquals(65, history.usedMinutes(t, categoryOf, today, hour = 14, hourly = false))
        assertEquals(45, history.usedMinutes(t, categoryOf, today, hour = 14, hourly = true))
    }

    @Test
    fun categoriesMatchCaseInsensitivelyAndRespectExclusions() {
        val t = PolicyTargets(categories = listOf("GAMES", "SOCIAL"), excludePackages = listOf("com.whatsapp"))
        assertEquals(30, history.usedMinutes(t, categoryOf, today, hour = 14, hourly = false))
    }

    @Test
    fun explicitPackageCountsEvenIfExcluded() {
        val t = PolicyTargets(packages = listOf("com.youtube"), excludePackages = listOf("com.youtube"))
        assertEquals(35, history.usedMinutes(t, categoryOf, today, hour = 0, hourly = false))
        assertEquals(0, history.usedMinutes(t, categoryOf, today, hour = 0, hourly = true))
    }
}
