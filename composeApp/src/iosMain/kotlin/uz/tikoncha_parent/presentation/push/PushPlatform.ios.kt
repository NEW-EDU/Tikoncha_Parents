package uz.tikoncha_parent.presentation.push

import kotlinx.coroutines.suspendCancellableCoroutine
import platform.UIKit.UIApplication
import platform.UIKit.registerForRemoteNotifications
import platform.UserNotifications.UNAuthorizationOptionAlert
import platform.UserNotifications.UNAuthorizationOptionBadge
import platform.UserNotifications.UNAuthorizationOptionSound
import platform.UserNotifications.UNUserNotificationCenter
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue
import kotlin.coroutines.resume

actual object PushPlatform {

    actual fun initialize() {
    }

    actual suspend fun requestNotificationPermission(): Boolean = suspendCancellableCoroutine { cont ->
        val options = UNAuthorizationOptionAlert or UNAuthorizationOptionBadge or UNAuthorizationOptionSound
        UNUserNotificationCenter.currentNotificationCenter().requestAuthorizationWithOptions(options) { granted, _ ->
            dispatch_async(dispatch_get_main_queue()) {
                // APNs token AppDelegate'da ham so'raladi; ruxsatdan keyin yana bir bor — token aniq kelsin
                UIApplication.sharedApplication.registerForRemoteNotifications()
                if (cont.isActive) cont.resume(granted)
            }
        }
    }
}
