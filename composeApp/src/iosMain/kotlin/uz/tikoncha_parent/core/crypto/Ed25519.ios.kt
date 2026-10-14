package uz.tikoncha_parent.core.crypto

/**
 * iOS Ed25519 — STUB (hozircha). `isSupported = false` bo'lgani uchun [LiveSigner]
 * imzolamaydi va tekshirmaydi (qo'shimcha rejim — viewer ishlayveradi). Haqiqiy
 * implementatsiya keyin: CryptoKit `Curve25519.Signing` cinterop orqali.
 */
actual object Ed25519 {
    actual val isSupported: Boolean = false

    actual fun generatePrivateKey(): ByteArray = ByteArray(32)
    actual fun publicKey(privateKey: ByteArray): ByteArray = ByteArray(32)
    actual fun sign(privateKey: ByteArray, message: ByteArray): ByteArray = ByteArray(0)
    actual fun verify(publicKey: ByteArray, signature: ByteArray, message: ByteArray): Boolean = false
}
