package uz.tikoncha_parent.domain.model.policy

import uz.tikoncha_parent.domain.policy.ProtectedPackages
import kotlin.time.Instant

/** Qulf tugmasi: ON — yopyapti, INACTIVE — mening ro'yxatimda, lekin ishlamayapti, OFF — yopilmagan. */
enum class QuickLock { OFF, ON, INACTIVE }

enum class QuickOwner { ME, CO_PARENT, CHILD }

/** ACTIVE — yopyapti; PAUSED — to'xtatilgan; UNPAID — bolaning obunasi yo'q, blok ishlamaydi. */
enum class QuickStatus { ACTIVE, PAUSED, UNPAID }

/** Qator ostidagi yozuv: kim bloklagan va hozir ishlayaptimi. */
data class QuickBadge(val owner: QuickOwner, val status: QuickStatus)

/**
 * Bitta bolaning barcha tezkor bloklari — statistika qatori va tezkor blok ekrani
 * uchun barcha qarorlar SHU YERDA, bitta emissiyadan (alohida oqimlar bir-ikki kadr
 * farq bilan kelib, qator sakrardi).
 *
 * Ota-ona faqat O'Z blokini o'zgartiradi. Bola yoki ikkinchi ota-ona bloklagan
 * ilovani o'z ro'yxatiga qo'shishi mumkin — ular o'znikini istalgan payt olib tashlashi mumkin.
 *
 * [paid] — bolaning tarifi: `null` = hali ma'lum emas (server o'zi hal qiladi).
 */
data class QuickBlockSnapshot(
    val entries: List<QuickBlockEntry> = emptyList(),
    val myUserId: String = "",
    val paid: Boolean? = null,
) {
    /** Mening tezkor blokim (server bitta ota-onaga bittadan ko'p yaratmaydi). */
    val mine: QuickBlockEntry? = entries.firstOrNull { it.isMine(myUserId) }

    fun isInMyList(packageName: String): Boolean = mine?.contains(packageName) == true

    /** Mening ro'yxatimda va blok yoqilgan (pauza/obuna bo'lsa ham — bosilsa olib tashlanadi). */
    fun isBlockedByMe(packageName: String): Boolean = mine?.isActive == true && isInMyList(packageName)

    fun lock(packageName: String, now: Instant): QuickLock? = when {
        !ProtectedPackages.canBlock(packageName) -> null
        isBlockedByMe(packageName) ->
            if (status(mine!!, now) == QuickStatus.ACTIVE) QuickLock.ON else QuickLock.INACTIVE
        else -> QuickLock.OFF
    }

    /**
     * Mening blokim ustun. Aks holda o'chirilmagan begona bloklardan eng kuchlisi:
     * ishlayotgani pauzadagidan, ikkinchi ota-onaniki bolanikidan oldin.
     */
    fun badge(packageName: String, now: Instant): QuickBadge? {
        if (isBlockedByMe(packageName)) return QuickBadge(QuickOwner.ME, status(mine!!, now))
        return entries
            .filter { !it.isMine(myUserId) && it.isActive && it.contains(packageName) }
            .map { QuickBadge(if (it.isChildOwner) QuickOwner.CHILD else QuickOwner.CO_PARENT, status(it, now)) }
            .minWithOrNull(compareBy<QuickBadge> { it.status.ordinal }.thenBy { it.owner.ordinal })
    }

    /** Qo'shish — pullik; olib tashlash doim bepul. Tarif noma'lum bo'lsa server hal qiladi. */
    fun needsPaywall(packageName: String): Boolean = paid == false && !isBlockedByMe(packageName)

    /**
     * Mening blokim o'chiq bo'lsa, unga ilova qo'shish BUTUN blokni qayta yoqadi —
     * ro'yxatdagi boshqa ilovalar ham yopiladi. 0 — ogohlantirish shart emas.
     */
    fun reenableCount(packageName: String): Int {
        val own = mine?.takeIf { !it.isActive } ?: return 0
        return own.targets.packages.count { !it.equals(packageName.trim(), ignoreCase = true) }
    }

    private fun status(entry: QuickBlockEntry, now: Instant): QuickStatus = when {
        paid == false -> QuickStatus.UNPAID
        entry.isPaused(now) -> QuickStatus.PAUSED
        else -> QuickStatus.ACTIVE
    }
}

/** Server paketlarni kichik harf bilan solishtiradi (`norm`) — bu yerda ham shunday. */
fun QuickBlockEntry.contains(packageName: String): Boolean {
    val key = packageName.trim()
    return targets.packages.any { it.equals(key, ignoreCase = true) }
}
