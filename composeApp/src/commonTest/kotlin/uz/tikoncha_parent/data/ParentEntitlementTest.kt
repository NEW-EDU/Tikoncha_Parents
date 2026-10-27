package uz.tikoncha_parent.data

import uz.tikoncha_parent.data.entitlement.ParentEntitlement
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

/** Ota-ona: imzolangan holatdagi har farzandning PLUS tugaşi offline tekşiriladi (imzo bu yerda soxta tekşiruvchi). */
@OptIn(ExperimentalEncodingApi::class)
class ParentEntitlementTest {
    private val now = 1_800_000_000L
    private fun b64(s: String) = Base64.UrlSafe.withPadding(Base64.PaddingOption.ABSENT).encode(s.encodeToByteArray())
    private fun token(claims: String) =
        b64("""{"alg":"ES256"}""") + "." + b64(claims) + "." +
            Base64.UrlSafe.withPadding(Base64.PaddingOption.ABSENT).encode(ByteArray(64))
    private val ok: (String, ByteArray) -> Boolean = { _, _ -> true }
    private val bad: (String, ByteArray) -> Boolean = { _, _ -> false }
    private val claims = """{"sub":"P1","iat":${now - 60},"exp":${now + 86400},"iss":"tikoncha","aud":"tikoncha-app",""" +
        """"children":{"kidA":${now + 3600},"kidB":null,"kidC":${now - 10}}}"""

    @Test
    fun perChildPlusLapsesOnTime() {
        val c = ParentEntitlement.verify(token(claims), ok)
        assertNotNull(c)
        assertEquals(true, ParentEntitlement.childPlusActive(c, "p1", "kidA", now, 0))
        assertEquals(true, ParentEntitlement.childPlusActive(c, "p1", "kidB", now, 0))
        assertEquals(false, ParentEntitlement.childPlusActive(c, "p1", "kidC", now, 0))
        assertEquals(false, ParentEntitlement.childPlusActive(c, "p1", "kidD", now, 0))
        assertEquals(false, ParentEntitlement.childPlusActive(c, "p1", "kidA", now + 7200, 0))
    }

    @Test
    fun badSignatureForeignUserExpiredOrRewoundAreIgnored() {
        assertNull(ParentEntitlement.verify(token(claims), bad))
        val c = ParentEntitlement.verify(token(claims), ok)
        assertNull(ParentEntitlement.childPlusActive(c, "other", "kidA", now, 0))
        assertNull(ParentEntitlement.childPlusActive(c, "p1", "kidA", now + 2 * 86400, 0))
        assertNull(ParentEntitlement.childPlusActive(c, "p1", "kidA", now - 3 * 86400, maxIatSeen = now))
    }
}
