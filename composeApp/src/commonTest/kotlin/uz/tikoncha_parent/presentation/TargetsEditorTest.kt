package uz.tikoncha_parent.presentation

import uz.tikoncha_parent.domain.model.policy.PolicyAction
import uz.tikoncha_parent.domain.model.policy.PolicyDraft
import uz.tikoncha_parent.domain.model.policy.PolicyTargets
import uz.tikoncha_parent.presentation.policy.model.PresetKind
import uz.tikoncha_parent.presentation.policy.targets.SiteError
import uz.tikoncha_parent.presentation.policy.targets.TargetsEditorEvent
import uz.tikoncha_parent.presentation.policy.targets.TargetsFlavor
import uz.tikoncha_parent.presentation.policy.targets.TargetsMode
import uz.tikoncha_parent.presentation.policy.targets.applyTo
import uz.tikoncha_parent.presentation.policy.targets.isValidSite
import uz.tikoncha_parent.presentation.policy.targets.normalizeSite
import uz.tikoncha_parent.presentation.policy.targets.reduce
import uz.tikoncha_parent.presentation.policy.targets.targetsEditorFrom
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class TargetsEditorTest {

    private val sleep = PolicyDraft(
        name = "Uyqu",
        action = PolicyAction.DENY,
        targets = PolicyTargets(packages = listOf(PolicyTargets.ALL_APPS), excludePackages = listOf("com.clock"), iosSelectionIds = listOf("ios1")),
    )

    @Test
    fun presetAllAppsRoundTripKeepsExceptionsAndIosSelection() {
        val flavor = TargetsFlavor.Preset(PresetKind.SLEEP)
        val editor = targetsEditorFrom(sleep, flavor)
        assertEquals(TargetsMode.ALL, editor.mode)
        val out = editor.applyTo(sleep, flavor)
        assertEquals(listOf(PolicyTargets.ALL_APPS), out.targets.packages)
        assertEquals(listOf("com.clock"), out.targets.excludePackages)
        assertEquals(listOf("ios1"), out.targets.iosSelectionIds)
    }

    @Test
    fun presetSelectedWithoutCategoriesDropsExceptions() {
        val flavor = TargetsFlavor.Preset(PresetKind.SLEEP)
        val editor = targetsEditorFrom(sleep, flavor)
            .reduce(TargetsEditorEvent.ModeChanged(TargetsMode.SELECTED))
            .reduce(TargetsEditorEvent.TogglePackage("com.game"))
        val out = editor.applyTo(sleep, flavor)
        assertEquals(PolicyAction.DENY, out.action)
        assertEquals(listOf("com.game"), out.targets.packages)
        assertTrue(out.targets.excludePackages.isEmpty())
    }

    @Test
    fun presetSelectedWithCategoryKeepsExceptions() {
        val flavor = TargetsFlavor.Preset(PresetKind.SCHOOL)
        val out = targetsEditorFrom(sleep, flavor)
            .reduce(TargetsEditorEvent.ModeChanged(TargetsMode.SELECTED))
            .reduce(TargetsEditorEvent.ToggleCategory("GAMES"))
            .applyTo(sleep, flavor)
        assertEquals(listOf("GAMES"), out.targets.categories)
        assertEquals(listOf("com.clock"), out.targets.excludePackages)
    }

    @Test
    fun customAllowModeIsAllowListWithoutExceptions() {
        val draft = PolicyDraft(name = "X", action = PolicyAction.DENY, targets = PolicyTargets(categories = listOf("SOCIAL"), excludePackages = listOf("a")))
        val out = targetsEditorFrom(draft, TargetsFlavor.Custom)
            .reduce(TargetsEditorEvent.ModeChanged(TargetsMode.ALL))
            .applyTo(draft, TargetsFlavor.Custom)
        assertEquals(PolicyAction.ALLOW, out.action)
        assertEquals(listOf("SOCIAL"), out.targets.categories)
        assertTrue(out.targets.excludePackages.isEmpty())
        assertEquals(TargetsMode.ALL, targetsEditorFrom(out, TargetsFlavor.Custom).mode)
    }

    @Test
    fun sitesAreNormalizedAndValidated() {
        assertEquals("youtube.com", normalizeSite(" https://www.YouTube.com/ "))
        assertTrue(isValidSite("kino"))
        assertTrue(!isValidSite("ab"))
        val s = targetsEditorFrom(sleep, TargetsFlavor.Custom)
            .reduce(TargetsEditorEvent.AddSiteClicked)
            .reduce(TargetsEditorEvent.SiteSubmitted("http://Example.org/"))
        assertNull(s.siteDialog)
        assertTrue("example.org" in s.sites && "example.org" in s.customSites)
        val dup = s.reduce(TargetsEditorEvent.AddSiteClicked).reduce(TargetsEditorEvent.SiteSubmitted("example.org"))
        assertEquals(SiteError.DUPLICATE, dup.siteDialog?.error)
    }
}
