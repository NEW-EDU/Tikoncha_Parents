package uz.tikoncha_parent.presentation.protection

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import uz.tikoncha_parent.domain.model.protection.ChildMode
import uz.tikoncha_parent.domain.model.protection.ChildPermission

/**
 * Faqat debug: `-hoglt.screen protectionsample [default|gujanak|strict] [-hoglt.missing]`.
 * Himoya ekranini tizimga kirmasdan, namuna holat bilan körsatadi (ekran rasmlari va dizayn tekşiruvi uçun).
 */
internal class ProtectionSampleScreen(
    private val mode: ChildMode,
    private val withMissing: Boolean,
) : Screen {
    @Composable
    override fun Content() {
        ProtectionUi(
            navigator = LocalNavigator.current,
            state = sampleState(mode, withMissing),
            remainingSeconds = 0,
            event = {},
            childId = "",
            childName = "Malika",
        )
    }
}

private fun sampleState(mode: ChildMode, withMissing: Boolean): ProtectionState {
    // Egasining skrinşotidagi holat: asosiy sozlamalar yoqilgan, kuçliroq himoyaniki — yöq
    val off = buildSet {
        add(ChildPermission.ACCESSIBILITY)
        add(ChildPermission.OVERLAY)
        add(ChildPermission.BATTERY)
        add(ChildPermission.DEVICE_ADMIN)
        if (withMissing) add(ChildPermission.NOTIFICATION)
    }
    val all = setOf(
        ChildPermission.NOTIFICATION, ChildPermission.USAGE_STATS, ChildPermission.AUTO_START,
        ChildPermission.ACCESSIBILITY, ChildPermission.OVERLAY, ChildPermission.BATTERY, ChildPermission.DEVICE_ADMIN,
    )
    return ProtectionState(
        mode = mode,
        lastSyncAt = kotlin.time.Clock.System.now(),
        isOnline = true,
        policiesUpToDate = true,
        enabledPermissions = all - off,
        disabledPermissions = off,
    )
}
