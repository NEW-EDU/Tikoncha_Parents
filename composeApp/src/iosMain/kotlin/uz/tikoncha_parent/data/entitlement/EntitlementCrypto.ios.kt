package uz.tikoncha_parent.data.entitlement

/** Swift'da (iosApp/EntitlementCryptoImpl.swift) — CryptoKit P256. iOSApp.swift ishga tushganda qayd etadi. */
interface EntitlementCryptoBridge {
    /** [rawSignatureB64] — r‖s, standart base64. */
    fun verify(signingInput: String, rawSignatureB64: String): Boolean
}

object EntitlementCryptoProvider {
    var bridge: EntitlementCryptoBridge? = null
}

@OptIn(kotlin.io.encoding.ExperimentalEncodingApi::class)
actual fun verifyEntitlementSignature(signingInput: String, rawSignature: ByteArray): Boolean =
    EntitlementCryptoProvider.bridge?.verify(signingInput, kotlin.io.encoding.Base64.encode(rawSignature)) ?: false
