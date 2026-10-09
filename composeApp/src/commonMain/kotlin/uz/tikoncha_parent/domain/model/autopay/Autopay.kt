package uz.tikoncha_parent.domain.model.autopay

import kotlin.time.Instant

/**
 * Karta orqali avto-to'lov (Paylov). Ota-ona farzand uchun to'laydi; kartalar ota-onaniki.
 * Summa va muddat serverda (tarifdan) — ilova faqat ko'rsatadi.
 */

enum class CardVendor { UZCARD, HUMO, OTHER;
    companion object {
        fun of(raw: String?): CardVendor = when (raw?.uppercase()) {
            "UZCARD" -> UZCARD
            "HUMO" -> HUMO
            else -> OTHER
        }

        /** Raqam yozilayotganda — faqat ko'rsatish uchun taxmin (haqiqiy turni Paylov aytadi). */
        fun guess(digits: String): CardVendor = when {
            digits.startsWith("9860") -> HUMO
            // 5614 — Uzcard–MIR, 6262 — Uzcard–UnionPay (qo'shaloq kartalar, Paylov yechadi)
            digits.startsWith("8600") || digits.startsWith("5614") || digits.startsWith("6262") -> UZCARD
            digits.length >= 2 && (digits.startsWith("4") || digits.startsWith("5") || digits.startsWith("2")) -> OTHER
            else -> OTHER
        }
    }
}

/** Saqlangan karta. Raqam hech qachon to'liq emas: "9860 •• 4821". */
data class PayCard(
    val id: String,
    val maskedPan: String,
    val vendor: CardVendor,
    val expire: String,
    val inUse: Boolean,
    /** Asosiy karta — avto-to'lov shu kartadan yechiladi, yangi to'lovda oldindan tanlanadi. */
    val isPrimary: Boolean = false,
) {
    val last4: String get() = maskedPan.takeLast(4)
}

/** Karta qo'shish boshlandi — SMS ketdi. */
data class CardStart(
    val id: String,
    val maskedPan: String,
    val vendor: CardVendor,
    val phoneMask: String,
)

enum class PlanPeriod { MONTHLY, ANNUAL;
    val wire: String get() = name
    companion object {
        fun of(raw: String?): PlanPeriod? = entries.firstOrNull { it.name == raw }
    }
}

enum class MandateStatus { ACTIVE, PAST_DUE, CANCELED;
    companion object {
        fun of(raw: String?): MandateStatus? = entries.firstOrNull { it.name == raw }
    }
}

data class AutopayInfo(
    /** Bu hisobga avto-to'lov umuman ochiqmi (server sozlangan, sandbox emas …). */
    val available: Boolean,
    val enabled: Boolean,
    /** Shu ota-ona to'laydi. `enabled && !mine` — farzandning boshqa ota-onasi to'laydi (boshqaruv yo'q). */
    val mine: Boolean = true,
    /** `!mine` va to'lovchi — farzandning o'zi (Student ilovasida yoqqan). */
    val payerIsChild: Boolean = false,
    val status: MandateStatus?,
    val planId: String?,
    val period: PlanPeriod?,
    val amount: Int?,
    val card: PayCard?,
    val nextChargeAt: Instant?,
    /** Oxirgi xato — server tilida tayyor matn. */
    val lastErrorText: String?,
    val failCount: Int,
    /** Hozirgi PLUS qachongacha (bo'lmasa null). */
    val paidUntil: Instant?,
) {
    val pastDue: Boolean get() = enabled && mine && status == MandateStatus.PAST_DUE
    val otherPayer: Boolean get() = enabled && !mine
}

enum class ChargeStatus { PAID, SCHEDULED, FAILED, PENDING, SKIPPED;
    companion object {
        fun of(raw: String?): ChargeStatus = entries.firstOrNull { it.name.equals(raw, ignoreCase = true) } ?: FAILED
    }
}

data class ChargeResult(
    val status: ChargeStatus,
    /** Xato bo'lsa — foydalanuvchi tilida tayyor matn. */
    val errorText: String?,
    val transactionId: String?,
    val autopay: AutopayInfo,
)

enum class ChargeState { PENDING, COMPLETED, CANCELLED;
    companion object {
        fun of(raw: String?): ChargeState = entries.firstOrNull { it.name == raw } ?: PENDING
    }
}
