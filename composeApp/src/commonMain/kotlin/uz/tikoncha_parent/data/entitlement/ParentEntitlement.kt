package uz.tikoncha_parent.data.entitlement

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull
import uz.tikoncha_parent.data.local.AppSettings
import uz.tikoncha_parent.domain.model.SubscriptionLimit
import uz.tikoncha_parent.domain.model.SubscriptionType
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.time.Clock

/** ES256 imzosini platforma tekşiradi (Android: java.security, iPhone: CryptoKit — iosApp/EntitlementCryptoImpl.swift). */
expect fun verifyEntitlementSignature(signingInput: String, rawSignature: ByteArray): Boolean

/** Tikoncha_API secrets/entitlement_es256.pub.pem (SubjectPublicKeyInfo, base64). Kalit almaşsa — bolaning ilovalarida ham. */
const val ENTITLEMENT_PUBLIC_KEY_B64 =
    "MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEaLG2iFVkeIdUW2d8TzwW+4OiESAR" +
        "6rmL3dxQipTde8hwlGZVojI8cHKM11mcDxKWyABcHMAYjr0sBVGpfzrRDw=="

/**
 * Server imzolagan holat (offline-first, `GET /subscriptions/entitlement`): ota-ona uçun `children` =
 * {farzand id: PLUS tugaşi}. Server yetib bölmasa farzandning PLUS'i shu bilan tekşiriladi — muddati ötsa
 * offline ham öçadi. Imzo, egasi, `exp` (iat + 14 kun) va soatni orqaga buraş tekşiriladi.
 */
object ParentEntitlement {
    private const val TOKEN_KEY = "entitlement_token"
    private const val MAX_IAT_KEY = "entitlement_max_iat"
    private const val CLOCK_SKEW_S = 24 * 3600L
    private val json = Json { ignoreUnknownKeys = true }

    data class Claims(val sub: String, val iat: Long, val exp: Long, val children: Map<String, Long?>)

    @OptIn(ExperimentalEncodingApi::class)
    private fun b64url(s: String): ByteArray =
        Base64.UrlSafe.withPadding(Base64.PaddingOption.ABSENT_OPTIONAL).decode(s)

    fun verify(token: String, verifier: (String, ByteArray) -> Boolean = ::verifyEntitlementSignature): Claims? =
        runCatching {
            val parts = token.split(".")
            if (parts.size != 3) return null
            val header = json.parseToJsonElement(b64url(parts[0]).decodeToString()).jsonObject
            if (header["alg"]?.jsonPrimitive?.contentOrNull != "ES256") return null
            val sig = b64url(parts[2])
            if (sig.size != 64 || !verifier("${parts[0]}.${parts[1]}", sig)) return null
            val c = json.parseToJsonElement(b64url(parts[1]).decodeToString()).jsonObject
            if (c["iss"]?.jsonPrimitive?.contentOrNull != "tikoncha") return null
            if (c["aud"]?.jsonPrimitive?.contentOrNull != "tikoncha-app") return null
            val kids = c["children"]?.takeUnless { it is JsonNull }?.jsonObject
                ?.mapValues { (_, v) -> (v as? JsonNull)?.let { null } ?: v.jsonPrimitive.longOrNull }
                .orEmpty()
            Claims(
                sub = c["sub"]?.jsonPrimitive?.contentOrNull ?: return null,
                iat = c["iat"]?.jsonPrimitive?.longOrNull ?: return null,
                exp = c["exp"]?.jsonPrimitive?.longOrNull ?: return null,
                children = kids,
            )
        }.getOrNull()

    /** true/false — imzolangan javob; null — token yöq yoki ishonchsiz (keşdagi holat qoladi). */
    fun childPlusActive(claims: Claims?, userId: String, childId: String, nowS: Long, maxIatSeen: Long): Boolean? {
        if (claims == null || !claims.sub.equals(userId, ignoreCase = true)) return null
        if (nowS > claims.exp || nowS < maxIatSeen - CLOCK_SKEW_S) return null
        if (!claims.children.containsKey(childId)) return false
        val until = claims.children[childId]
        return until == null || nowS < until
    }

    fun store(token: String?) {
        val claims = token?.let { verify(it) } ?: return
        AppSettings.setOfflineValue(TOKEN_KEY, token)
        val max = AppSettings.offlineValue(MAX_IAT_KEY)?.toLongOrNull() ?: 0
        if (claims.iat > max) AppSettings.setOfflineValue(MAX_IAT_KEY, claims.iat.toString())
    }

    /**
     * Keşdagi farzand chegaralari: imzolangan holatga köra PLUS'i offline paytda tugagan farzand FREE bölib körinadi.
     * Online sinxronda röyxat ham, token ham yangilanadi.
     */
    fun applyTo(limits: List<SubscriptionLimit>): List<SubscriptionLimit> {
        val token = AppSettings.offlineValue(TOKEN_KEY) ?: return limits
        val claims = verify(token) ?: return limits
        val now = Clock.System.now().epochSeconds
        val maxIat = AppSettings.offlineValue(MAX_IAT_KEY)?.toLongOrNull() ?: 0
        return limits.map { l ->
            if (l.subscriptionType == SubscriptionType.FREE) l
            else when (childPlusActive(claims, AppSettings.userId, l.childId, now, maxIat)) {
                false -> l.copy(subscriptionType = SubscriptionType.FREE)
                else -> l
            }
        }
    }
}
