package uz.tikoncha_parent.core.crypto

/**
 * Ed25519 primitivi iOS'da Swift (CryptoKit `Curve25519.Signing`) tomonidan beriladi —
 * Kotlin/Native CryptoKit'ni to'g'ridan-to'g'ri chaqira olmaydi. iosApp ishga tushganda
 * [LiveEd25519BridgeProvider.bridge] ni o'rnatadi (TelegramAuthBridge bilan bir xil naqsh).
 * Xom shakllar: private 32 bayt, public 32 bayt, signature 64 bayt.
 */
interface LiveEd25519Bridge {
    fun generatePrivateKey(): ByteArray
    fun publicKey(privateKey: ByteArray): ByteArray
    fun sign(privateKey: ByteArray, message: ByteArray): ByteArray
    fun verify(publicKey: ByteArray, signature: ByteArray, message: ByteArray): Boolean
}

object LiveEd25519BridgeProvider {
    var bridge: LiveEd25519Bridge? = null
}
