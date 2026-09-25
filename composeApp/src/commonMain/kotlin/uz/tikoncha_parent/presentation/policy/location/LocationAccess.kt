package uz.tikoncha_parent.presentation.policy.location

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import dev.icerock.moko.geo.LocationTracker
import dev.icerock.moko.geo.compose.BindLocationTrackerEffect
import dev.icerock.moko.permissions.DeniedAlwaysException
import dev.icerock.moko.permissions.DeniedException
import dev.icerock.moko.permissions.Permission
import dev.icerock.moko.permissions.PermissionState
import dev.icerock.moko.permissions.PermissionsController
import dev.icerock.moko.permissions.compose.BindEffect
import dev.icerock.moko.permissions.location.LOCATION
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import tikoncha_parents.composeapp.generated.resources.Res
import tikoncha_parents.composeapp.generated.resources.bekor_qilish
import tikoncha_parents.composeapp.generated.resources.dialog_info
import tikoncha_parents.composeapp.generated.resources.tracking_gps_off_enable
import tikoncha_parents.composeapp.generated.resources.tracking_gps_off_message
import tikoncha_parents.composeapp.generated.resources.tracking_gps_off_title
import tikoncha_parents.composeapp.generated.resources.tracking_permission_settings_message
import tikoncha_parents.composeapp.generated.resources.tracking_permission_settings_open
import tikoncha_parents.composeapp.generated.resources.tracking_permission_settings_title
import uz.tikoncha_parent.platform.isLocationServiceEnabled
import uz.tikoncha_parent.platform.openAppSettings
import uz.tikoncha_parent.platform.openLocationSettings
import uz.tikoncha_parent.presentation.base.CustomDialog
import uz.tikoncha_parent.presentation.map.LatLng

/** "Mening joylashuvim": [request] — ruxsat, GPS, bitta nuqta; [isLoading] — kutilmoqda. */
class LocationAccess(val request: () -> Unit, val requestIfGranted: () -> Unit, val isLoading: Boolean)

private enum class AccessDialog { SETTINGS, GPS }

/**
 * Telefon joylashuvini bir marta olish (moko permissions + geo). Rad etilsa — sozlamalar,
 * GPS o'chiq bo'lsa — yoqish dialogi (kuzatuv ekranidagi matnlar bilan).
 */
@Composable
fun rememberLocationAccess(onLocated: (LatLng) -> Unit): LocationAccess {
    val permissions: PermissionsController = koinInject()
    val tracker: LocationTracker = koinInject()
    BindEffect(permissions)
    BindLocationTrackerEffect(tracker)

    val scope = rememberCoroutineScope()
    var loading by remember { mutableStateOf(false) }
    var dialog by remember { mutableStateOf<AccessDialog?>(null) }

    suspend fun locateOnce() {
        if (!isLocationServiceEnabled()) {
            dialog = AccessDialog.GPS
            return
        }
        loading = true
        try {
            runCatching { tracker.startTracking() }
            val loc = withTimeoutOrNull(LOCATE_TIMEOUT_MS) { tracker.getLocationsFlow().first() }
            runCatching { tracker.stopTracking() }
            loc?.let { onLocated(LatLng(it.latitude, it.longitude)) }
        } finally {
            loading = false
        }
    }

    val request: () -> Unit = {
        if (!loading) scope.launch {
            when (permissions.getPermissionState(Permission.LOCATION)) {
                PermissionState.Granted -> locateOnce()
                PermissionState.DeniedAlways -> dialog = AccessDialog.SETTINGS
                else -> try {
                    permissions.providePermission(Permission.LOCATION)
                    locateOnce()
                } catch (_: DeniedAlwaysException) {
                    dialog = AccessDialog.SETTINGS
                } catch (_: DeniedException) {
                    Unit        // oddiy rad — keyingi bosishda yana so'raladi
                }
            }
        }
    }
    // Jim: faqat ruxsat va GPS bor bo'lsa (dialogsiz)
    val requestIfGranted: () -> Unit = {
        scope.launch {
            if (permissions.getPermissionState(Permission.LOCATION) == PermissionState.Granted && isLocationServiceEnabled()) locateOnce()
        }
    }

    CustomDialog(
        show = dialog == AccessDialog.SETTINGS,
        title = stringResource(Res.string.tracking_permission_settings_title),
        message = stringResource(Res.string.tracking_permission_settings_message),
        painter = painterResource(Res.drawable.dialog_info),
        buttonText = stringResource(Res.string.tracking_permission_settings_open),
        buttonText2 = stringResource(Res.string.bekor_qilish),
        showCloseButton = true,
        onButtonClick = {
            dialog = null
            openAppSettings()
        },
        onDismiss = { dialog = null },
    )
    CustomDialog(
        show = dialog == AccessDialog.GPS,
        title = stringResource(Res.string.tracking_gps_off_title),
        message = stringResource(Res.string.tracking_gps_off_message),
        painter = painterResource(Res.drawable.dialog_info),
        buttonText = stringResource(Res.string.tracking_gps_off_enable),
        buttonText2 = stringResource(Res.string.bekor_qilish),
        showCloseButton = true,
        onButtonClick = {
            dialog = null
            openLocationSettings()
        },
        onDismiss = { dialog = null },
    )

    return LocationAccess(request = request, requestIfGranted = requestIfGranted, isLoading = loading)
}

private const val LOCATE_TIMEOUT_MS = 10_000L
