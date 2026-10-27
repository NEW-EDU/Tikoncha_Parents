package uz.tikoncha_parent.data.entitlement

import java.security.KeyFactory
import java.security.Signature
import java.security.spec.X509EncodedKeySpec

private val publicKey by lazy {
    KeyFactory.getInstance("EC").generatePublic(
        X509EncodedKeySpec(java.util.Base64.getDecoder().decode(ENTITLEMENT_PUBLIC_KEY_B64))
    )
}

actual fun verifyEntitlementSignature(signingInput: String, rawSignature: ByteArray): Boolean = runCatching {
    Signature.getInstance("SHA256withECDSA").run {
        initVerify(publicKey)
        update(signingInput.encodeToByteArray())
        verify(rawToDer(rawSignature))
    }
}.getOrDefault(false)

/** JWS ES256 imzosi r‖s → DER. */
private fun rawToDer(raw: ByteArray): ByteArray {
    fun int(b: ByteArray): ByteArray {
        var i = 0
        while (i < b.size - 1 && b[i] == 0.toByte()) i++
        var v = b.copyOfRange(i, b.size)
        if (v[0] < 0) v = byteArrayOf(0) + v
        return byteArrayOf(0x02, v.size.toByte()) + v
    }
    val r = int(raw.copyOfRange(0, 32))
    val s = int(raw.copyOfRange(32, 64))
    return byteArrayOf(0x30, (r.size + s.size).toByte()) + r + s
}
