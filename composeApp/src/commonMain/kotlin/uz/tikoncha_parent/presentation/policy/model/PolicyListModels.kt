package uz.tikoncha_parent.presentation.policy.model

import uz.tikoncha_parent.domain.model.LimitWindow
import uz.tikoncha_parent.domain.model.policy.PolicyPreset
import kotlin.time.Instant

/**
 * Jadvallar ro'yxati tablari (Student: Shablonlar · Meniki · Ota-ona · Maktab).
 * Parent'da egalar: farzandning o'zi, ikkinchi ota-ona, maktab — faqat jadvali bo'lsa chiqadi.
 */
enum class PolicyTab { TEMPLATES, MINE, CHILD, COPARENT, SCHOOL }

/** Ro'yxatda doim turadigan uchta tayyor jadval. */
enum class PresetKind(val preset: PolicyPreset) {
    SLEEP(PolicyPreset.SLEEP),
    LIMIT(PolicyPreset.APP_LIMIT),
    SCHOOL(PolicyPreset.SCHOOL);

    companion object {
        fun of(preset: PolicyPreset?): PresetKind? = entries.firstOrNull { it.preset == preset }
    }
}

enum class Ownership { MINE, CHILD, COPARENT, SCHOOL }

/** Kartaning ikkinchi qatori uchun xom ma'lumot; matn UI'da lokalizatsiya qilinadi. */
data class PolicySummary(
    val startMin: Int? = null,
    val endMin: Int? = null,
    /** ISO kunlar 1..7; bo'sh — vaqt sharti yo'q. */
    val days: Set<Int> = emptySet(),
    val limitWindow: LimitWindow? = null,
    val limitMinutes: Int? = null,
    val appCount: Int = 0,
    val categoryCount: Int = 0,
    val siteCount: Int = 0,
    val hasLocation: Boolean = false,
    val allApps: Boolean = false,
)

/** Tayyor jadval kartasi. `policyId == null` — bazada hali yo'q: switch OFF, ikonka kulrang. */
data class PresetPolicyUi(
    val kind: PresetKind,
    val policyId: String? = null,
    val isEnabled: Boolean = false,
    val isActive: Boolean = false,
    val isPaused: Boolean = false,
    val summary: PolicySummary = PolicySummary(),
)

data class PolicyCardUi(
    val policyId: String,
    val title: String,
    val ownership: Ownership,
    val isEnabled: Boolean,
    val isActive: Boolean,
    val isPaused: Boolean,
    val isQuickBlock: Boolean,
    val summary: PolicySummary,
) {
    /** Faqat o'zim yaratganimni boshqara olaman. */
    val canControl: Boolean get() = ownership == Ownership.MINE
}

/** "Kontent himoya": bitta switch hamma paket uchun. [byOthers] — farzand yoki ikkinchi ota-ona yoqqan. */
data class ContentProtectionUi(
    val isEnabled: Boolean = false,
    val byOthers: Boolean = false,
    val isAvailable: Boolean = false,
)

/** "Tezkor blok" shabloni kartasi — mening tezkor blokim. */
data class QuickBlockUi(
    val policyId: String? = null,
    val isEnabled: Boolean = false,
    val isActive: Boolean = false,
    val isPaused: Boolean = false,
    val appCount: Int = 0,
    val pausedUntil: Instant? = null,
) {
    val isEmpty: Boolean get() = appCount == 0
    /** Ro'yxat bo'sh bo'lsa yoqishning ma'nosi yo'q. */
    val isOn: Boolean get() = isEnabled && !isEmpty
}

/** Ro'yxat tepasidagi izoh: hozir nima ishlayapti va jadvallar bir-biriga qanday ta'sir qiladi. */
data class PolicyListInfo(
    /** Hozir faol jadvallar nomi — kuchlisi birinchi (tezkor blok va himoyasiz). */
    val activeTitles: List<String> = emptyList(),
    val activeUntilMin: Int? = null,
    val quickActive: Boolean = false,
    val hasChildPolicies: Boolean = false,
    val hasSchoolPolicies: Boolean = false,
    val pausedCount: Int = 0,
)

/** Paywall sababi — matni boshqacha. */
enum class PayWallReason { QUICK_BLOCK, PROTECTION, POLICY_COUNT }
