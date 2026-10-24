// composeApp/src/androidMain/kotlin/uz/tikoncha_parent/presentation/push/PushPlatform.android.kt
package uz.tikoncha_parent.presentation.push

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationManagerCompat
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.withTimeoutOrNull
import uz.tikoncha_parent.data.service.AndroidNotificationHelper
import java.lang.ref.WeakReference

actual object PushPlatform {
    private const val REQUEST_CODE = 1001

    private var activityRef: WeakReference<Activity>? = null
    private var appContext: Context? = null
    private var pending: CompletableDeferred<Boolean>? = null

    @JvmStatic
    fun bind(activity: Activity) {
        activityRef = WeakReference(activity)
        appContext = activity.applicationContext
    }

    actual fun initialize() {
        val ctx = appContext ?: activityRef?.get() ?: return
        AndroidNotificationHelper.ensureChannel(ctx)
    }

    actual suspend fun requestNotificationPermission(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return true
        val act = activityRef?.get() ?: return false
        if (NotificationManagerCompat.from(act).areNotificationsEnabled()) return true
        val result = CompletableDeferred<Boolean>()
        pending = result
        ActivityCompat.requestPermissions(
            act,
            arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
            REQUEST_CODE
        )
        // Activity qayta yaratilsa natija kelmasligi mumkin — tugma osilib qolmasin
        return withTimeoutOrNull(120_000) { result.await() } ?: false
    }

    /** MainActivity.onRequestPermissionsResult'dan chaqiriladi. */
    @JvmStatic
    fun onPermissionResult(requestCode: Int, grantResults: IntArray) {
        if (requestCode != REQUEST_CODE) return
        pending?.complete(grantResults.firstOrNull() == PackageManager.PERMISSION_GRANTED)
        pending = null
    }
}
