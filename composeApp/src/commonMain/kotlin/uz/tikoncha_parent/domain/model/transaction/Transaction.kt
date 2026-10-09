package uz.tikoncha_parent.domain.model.transaction

import kotlinx.datetime.LocalDateTime

data class Transaction(
    val id: String,
    val userId: String,
    val childUserId: String?,
    val userFirstName: String,
    val userLastName: String,
    val userPhone: String,
    val isUserRegistered: Boolean,
    val merchantTransId: String,
    val amount: Long,
    val originalAmount: Long?,
    val coins: Long,
    val status: TransactionStatus,
    val purchaseType: PurchaseType,
    val planName: String?,
    val planDuration: PlanDuration?,
    val expiredAt: LocalDateTime?,
    val createdAt: LocalDateTime?,
    /** Karta orqali avto-to'lov (Paylov); aks holda Click. */
    val isAutopay: Boolean = false,
    /** Avto-to'lov kartasi: "HUMO •• 4821". */
    val cardMask: String? = null,
) {
    /** Birlashtirilgan ism. Agar ikkalasi ham bo'sh bo'lsa — null. */
    val userFullName: String?
        get() = listOf(userFirstName, userLastName)
            .filter { it.isNotBlank() }
            .joinToString(" ")
            .ifBlank { null }

    val hasUserName: Boolean get() = userFullName != null
    val hasUserPhone: Boolean get() = userPhone.isNotBlank()
}

enum class TransactionStatus {
    PENDING, COMPLETED, CANCELLED, FAILED, UNKNOWN;

    companion object {
        fun from(raw: String?): TransactionStatus = when (raw?.uppercase()) {
            "PENDING" -> PENDING
            "COMPLETED" -> COMPLETED
            "CANCELLED", "CANCELED" -> CANCELLED
            "FAILED" -> FAILED
            else -> UNKNOWN
        }
    }
}

enum class PurchaseType {
    SUBSCRIPTION, COINS, UNKNOWN;

    companion object {
        fun from(raw: String?): PurchaseType = when (raw?.lowercase()) {
            "subscription" -> SUBSCRIPTION
            "coin", "coins" -> COINS
            else -> UNKNOWN
        }
    }
}

enum class PlanDuration {
    MONTHLY, ANNUAL, UNKNOWN;

    companion object {
        fun from(raw: String?): PlanDuration = when (raw?.uppercase()) {
            "MONTHLY" -> MONTHLY
            "ANNUAL", "YEARLY" -> ANNUAL
            else -> UNKNOWN
        }
    }
}