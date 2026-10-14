package uz.tikoncha_parent.core.crypto

/**
 * Platform Ed25519 primitivi (RFC 8032). Faqat xom bayt matematikasi platformaga
 * bog'liq; kalit saqlash + kanonik xabar qurish [LiveSigner] da (umumiy).
 *
 * actual: Android = BouncyCastle; iOS = (hozircha stub, keyin CryptoKit/Security).
 * Xom shakllar: private 32 bayt, public 32 bayt, signature 64 bayt.
 */
expect object Ed25519 {
    /** Yangi 32-baytli maxfiy kalit (urug') yaratadi. Qo'llab-quvvatlanmasa bo'sh massiv. */
    fun generatePrivateKey(): ByteArray

    /** Maxfiy kalitdan 32-baytli public kalit. */
    fun publicKey(privateKey: ByteArray): ByteArray

    /** 64-baytli imzo. */
    fun sign(privateKey: ByteArray, message: ByteArray): ByteArray

    /** Imzoni public kalitga qarshi tekshiradi. */
    fun verify(publicKey: ByteArray, signature: ByteArray, message: ByteArray): Boolean

    /** Ushbu platformada haqiqiy imzolash mavjudmi (stub actual'da false). */
    val isSupported: Boolean
}
