package uz.tikoncha_parent.domain.model

sealed class PurchaseResult {
    data object Success : PurchaseResult()
    data object Cancelled : PurchaseResult()
    data object Pending : PurchaseResult()
    data class Error(val message: String) : PurchaseResult()
    /** App Store: Apple imzolagan tranzaksiya — serverga yuboriladi, keyin yopiladi. */
    data class Signed(val jws: String, val transactionId: String) : PurchaseResult()
}
