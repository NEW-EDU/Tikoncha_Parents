package uz.tikoncha_parent.presentation.policy.quick

import uz.tikoncha_parent.domain.model.app_error.Outcome
import uz.tikoncha_parent.domain.model.apps.InstalledApp
import uz.tikoncha_parent.domain.model.policy.QuickBlockEntry
import uz.tikoncha_parent.domain.model.policy.QuickBlockSnapshot
import kotlin.time.Clock
import kotlin.time.Instant

data class QuickBlockState(
    val childId: String = "",
    val loaded: Boolean = false,
    /** Bolaning barcha tezkor bloklari + tarif (domen qarorlari shu yerda). */
    val quick: QuickBlockSnapshot = QuickBlockSnapshot(),
    val now: Instant = Clock.System.now(),
    /** Bola qurilmasidagi bloklash mumkin bo'lgan ilovalar — nom/ikonka va qo'shish varag'i uchun. */
    val childApps: List<InstalledApp> = emptyList(),
    val removing: Set<String> = emptySet(),
    val adding: Boolean = false,
    /** Switch bosildi, server javobi kutilmoqda. */
    val pendingEnabled: Boolean? = null,
    val pausing: Boolean = false,
    val showAppsSheet: Boolean = false,
    val showPauseSheet: Boolean = false,
    val showPaywall: Boolean = false,
    val error: Outcome.Failure? = null,
) {
    private val appsByPackage: Map<String, InstalledApp> by lazy { childApps.associateBy { it.packageName.lowercase() } }

    val own: QuickBlockEntry? get() = quick.mine
    val ownPackages: List<String> get() = own?.targets?.packages.orEmpty()
    /** Server tasdiqlagan holat — switch shuni ko'rsatadi, so'rov paytida spinner ([pendingEnabled]). */
    val isOn: Boolean get() = own?.isActive == true
    val pausedUntil: Instant? get() = own?.pausedUntil?.takeIf { it > now }

    /** Switch faqat blok mavjud bo'lsa ishlaydi — bo'sh blokni yoqishdan ma'no yo'q. */
    val canToggle: Boolean get() = own != null && ownPackages.isNotEmpty() && pendingEnabled == null

    val childPackages: List<String>
        get() = quick.entries.filter { it.isChildOwner && it.isActive }.flatMap { it.targets.packages }.distinct()

    val coParentPackages: List<String>
        get() = quick.entries.filter { !it.isChildOwner && !it.isMine(quick.myUserId) && it.isActive }
            .flatMap { it.targets.packages }.distinct()

    /** Qo'shish varag'ida: allaqachon ro'yxatdagilar yo'q. */
    val addableApps: List<InstalledApp>
        get() = childApps.filter { app -> ownPackages.none { it.equals(app.packageName, ignoreCase = true) } }

    fun label(packageName: String): String = appsByPackage[packageName.lowercase()]?.name ?: packageName
    fun iconUrl(packageName: String): String? = appsByPackage[packageName.lowercase()]?.iconUrl
    fun isRemoving(packageName: String): Boolean = packageName in removing
}

sealed interface QuickBlockEvent {
    data class Load(val childId: String) : QuickBlockEvent
    data object Resumed : QuickBlockEvent
    data class EnabledToggled(val enabled: Boolean) : QuickBlockEvent
    data object PauseClicked : QuickBlockEvent
    /** Necha daqiqaga to'xtatish (15 / 30 / 60 / 180). */
    data class PauseSelected(val minutes: Int) : QuickBlockEvent
    data object ResumeClicked : QuickBlockEvent
    data object PauseDismissed : QuickBlockEvent
    data object AddClicked : QuickBlockEvent
    data class AppsAdded(val packages: Set<String>) : QuickBlockEvent
    data object SheetDismissed : QuickBlockEvent
    data class AppRemoved(val packageName: String) : QuickBlockEvent
    data object PaywallDismissed : QuickBlockEvent
    data object ErrorDismissed : QuickBlockEvent
}
