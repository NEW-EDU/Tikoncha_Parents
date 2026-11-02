package uz.tikoncha_parent.core.crypto

import com.russhwolf.settings.Settings
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.random.Random
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull

/**
 * Ed25519 identlik imzolari — jonli signalizatsiya ramkalarini imzolash/tekshirish.
 * Ota-ona bola'ning offer/ICE imzolarini tekshiradi; o'z answer/ICE'sini imzolaydi.
 *
 * Kanonik imzolanadigan xabar backend `app/modules/live/signing.py` bilan AYNAN bir xil:
 *   offer/answer: type US session_id US sdp
 *   ice:          type US session_id US candidate US (sdpMid|"") US sdpMLineIndex   (US = 0x1F)
 * Sim: public_key = base64(xom 32B), signature = base64(xom 64B).
 */
@OptIn(ExperimentalEncodingApi::class)
object LiveSigner {

    private const val US = "\u001f"
    private val SIGNED = setOf("offer", "answer", "ice")

    private val settings = Settings()
    private const val K_PRIV = "live_priv_b64"
    private const val K_DEV = "live_device_uid"

    private var priv: ByteArray? = null
    private var pubB64: String? = null

    var deviceUid: String = ""
        private set

    val isSupported: Boolean get() = Ed25519.isSupported

    fun ensure() {
        if (priv != null) return
        val savedPriv = settings.getStringOrNull(K_PRIV)
        val savedDev = settings.getStringOrNull(K_DEV)
        val p: ByteArray
        if (savedPriv != null && savedDev != null) {
            p = Base64.decode(savedPriv)
            deviceUid = savedDev
        } else {
            p = Ed25519.generatePrivateKey()
            deviceUid = randomId()
            settings.putString(K_PRIV, Base64.encode(p))
            settings.putString(K_DEV, deviceUid)
        }
        priv = p
        pubB64 = Base64.encode(Ed25519.publicKey(p))
    }

    fun publicKeyB64(): String {
        ensure()
        return pubB64 ?: ""
    }

    /** Signature (base64) for a media frame, or null for an unsigned type / unsupported platform. */
    fun sign(type: String, sessionId: String, payload: JsonElement?): String? {
        if (type !in SIGNED || !Ed25519.isSupported) return null
        ensure()
        val p = priv ?: return null
        return runCatching { Base64.encode(Ed25519.sign(p, canonical(type, sessionId, payload))) }.getOrNull()
    }

    /** Verify a peer frame. Additive: if the peer has no key yet (or this platform can't
     *  verify yet, e.g. the iOS stub), accept so the viewer still works during rollout. */
    fun verify(type: String, sessionId: String, payload: JsonElement?, signatureB64: String?, peerKeysB64: List<String>): Boolean {
        if (peerKeysB64.isEmpty() || !Ed25519.isSupported) return true
        if (signatureB64.isNullOrBlank()) return false
        val sig = runCatching { Base64.decode(signatureB64) }.getOrNull() ?: return false
        val msg = canonical(type, sessionId, payload)
        return peerKeysB64.any { k ->
            runCatching { Ed25519.verify(Base64.decode(k), sig, msg) }.getOrDefault(false)
        }
    }

    private fun canonical(type: String, sessionId: String, payload: JsonElement?): ByteArray {
        val obj = payload as? JsonObject
        val core = when (type) {
            "offer", "answer" -> (obj?.get("sdp") as? JsonPrimitive)?.contentOrNull ?: ""
            "ice" -> {
                val candidate = (obj?.get("candidate") as? JsonPrimitive)?.contentOrNull ?: ""
                val mid = (obj?.get("sdpMid") as? JsonPrimitive)?.contentOrNull ?: ""
                val idx = (obj?.get("sdpMLineIndex") as? JsonPrimitive)?.intOrNull ?: 0
                listOf(candidate, mid, idx.toString()).joinToString(US)
            }
            else -> ""
        }
        return listOf(type, sessionId, core).joinToString(US).encodeToByteArray()
    }

    private fun randomId(): String {
        val b = Random.nextBytes(16)
        return b.joinToString("") { (it.toInt() and 0xFF).toString(16).padStart(2, '0') }
    }
}
