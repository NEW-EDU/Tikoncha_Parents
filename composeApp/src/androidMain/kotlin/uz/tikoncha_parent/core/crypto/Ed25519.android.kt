package uz.tikoncha_parent.core.crypto

import org.bouncycastle.crypto.params.Ed25519PrivateKeyParameters
import org.bouncycastle.crypto.params.Ed25519PublicKeyParameters
import org.bouncycastle.crypto.signers.Ed25519Signer
import java.security.SecureRandom

/** Android Ed25519 — BouncyCastle low-level API (JCA "Ed25519" is API 33+; min is 28). */
actual object Ed25519 {
    actual val isSupported: Boolean = true

    actual fun generatePrivateKey(): ByteArray =
        Ed25519PrivateKeyParameters(SecureRandom()).encoded

    actual fun publicKey(privateKey: ByteArray): ByteArray =
        Ed25519PrivateKeyParameters(privateKey, 0).generatePublicKey().encoded

    actual fun sign(privateKey: ByteArray, message: ByteArray): ByteArray {
        val signer = Ed25519Signer()
        signer.init(true, Ed25519PrivateKeyParameters(privateKey, 0))
        signer.update(message, 0, message.size)
        return signer.generateSignature()
    }

    actual fun verify(publicKey: ByteArray, signature: ByteArray, message: ByteArray): Boolean =
        runCatching {
            val signer = Ed25519Signer()
            signer.init(false, Ed25519PublicKeyParameters(publicKey, 0))
            signer.update(message, 0, message.size)
            signer.verifySignature(signature)
        }.getOrDefault(false)
}
