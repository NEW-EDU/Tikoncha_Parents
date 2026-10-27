package uz.tikoncha_parent.data

import io.ktor.http.URLBuilder
import io.ktor.http.Url
import kotlinx.coroutines.test.runTest
import uz.tikoncha_parent.data.remote.Failover
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class FailoverTest {

    @AfterTest fun reset() = Failover.resetForTest()

    @Test fun onlyPromotedSwitches() {
        assertTrue(Failover.isPromoted("""{"success":true,"data":{"role":"promoted"},"error":null}"""))
        assertFalse(Failover.isPromoted("""{"data":{"role":"standby"}}"""))
        assertFalse(Failover.isPromoted(null))
    }

    @Test fun followsTheStandbysRole() = runTest {
        Failover.fetchStandbyRole = { """{"data":{"role":"promoted"}}""" }
        Failover.check()
        assertTrue(Failover.active)
        Failover.fetchStandbyRole = { error("unreachable") }   // standby gone: back to the primary
        Failover.check()
        assertFalse(Failover.active)
    }

    @Test fun rewritesOnlyPrimaryWhileActive() = runTest {
        val url = URLBuilder(Url("https://api.tikoncha.uz/users/children?x=1"))
        Failover.rewrite(url)
        assertEquals("https://api.tikoncha.uz/users/children?x=1", url.buildString())
        Failover.fetchStandbyRole = { """{"data":{"role":"promoted"}}""" }
        Failover.check()
        Failover.rewrite(url)
        assertEquals("https://tikoncha.uz/api-standby/users/children?x=1", url.buildString())
    }
}
