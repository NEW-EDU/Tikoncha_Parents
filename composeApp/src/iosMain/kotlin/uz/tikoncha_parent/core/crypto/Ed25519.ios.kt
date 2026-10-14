package uz.tikoncha_parent.core.crypto

/**
 * iOS Ed25519 — Swift (CryptoKit) bridge orqali. `isSupported` bridge o'rnatilgandagina
 * true; o'rnatilmasa [LiveSigner] imzolamaydi/tekshirmaydi (qo'shimcha rejim).
 */
actual object Ed25519 {
    private val bridge: LiveEd25519Bridge? get() = LiveEd25519BridgeProvider.bridge

    actual val isSupported: Boolean get() = bridge != null

    actual fun generatePrivateKey(): ByteArray = bridge?.generatePrivateKey() ?: ByteArray(0)

    actual fun publicKey(privateKey: ByteArray): ByteArray = bridge?.publicKey(privateKey) ?: ByteArray(0)

    actual fun sign(privateKey: ByteArray, message: ByteArray): ByteArray = bridge?.sign(privateKey, message) ?: ByteArray(0)

    actual fun verify(publicKey: ByteArray, signature: ByteArray, message: ByteArray): Boolean =
        bridge?.verify(publicKey, signature, message) ?: false
}
