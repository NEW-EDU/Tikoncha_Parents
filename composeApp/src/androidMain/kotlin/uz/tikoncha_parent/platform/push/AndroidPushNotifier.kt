package uz.tikoncha_parent.platform.push

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.PowerManager
import android.provider.Settings
import uz.tikoncha_parent.IncomingSosActivity
import uz.tikoncha_parent.MainActivity
import uz.tikoncha_parent.data.service.AndroidNotificationHelper
import uz.tikoncha_parent.domain.model.DeepLink
import uz.tikoncha_parent.presentation.push.PushNotification
import uz.tikoncha_parent.presentation.push.PushNotifier

class AndroidPushNotifier(private val context: Context): PushNotifier{

    override fun show(notification: PushNotification) {
        val link = notification.deepLink
        // An incoming live session (child SOS) rings full-screen like a call.
        if (link is DeepLink.Live) {
            showIncomingSos(notification, link)
            return
        }
        val pi = link?.let { pendingIntentFor(it) }
        AndroidNotificationHelper.show(
            ctx = context,
            title = notification.title,
            body = notification.body,
            pendingIntent = pi
        )
    }

    private fun showIncomingSos(notification: PushNotification, link: DeepLink.Live) {
        val notifId = ("sos:" + link.sessionId).hashCode()
        val intent = Intent(context, IncomingSosActivity::class.java).apply {
            putExtra(IncomingSosActivity.EXTRA_SESSION_ID, link.sessionId)
            putExtra(IncomingSosActivity.EXTRA_CHILD_ID, link.childId)
            putExtra(IncomingSosActivity.EXTRA_KIND, link.kind)
            putExtra(IncomingSosActivity.EXTRA_TITLE, notification.title)
            putExtra(IncomingSosActivity.EXTRA_BODY, notification.body)
            putExtra(IncomingSosActivity.EXTRA_NOTIF_ID, notifId)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        val fsPi = PendingIntent.getActivity(context, notifId, intent, flags)

        // Always post the full-screen-intent notification — it takes over the screen
        // when the device is LOCKED, and is the heads-up fallback otherwise.
        AndroidNotificationHelper.showIncomingSos(
            ctx = context,
            title = notification.title,
            body = notification.body,
            fullScreenPi = fsPi,
            notificationId = notifId,
        )

        // When the screen is ON, an FSI is shown only as a heads-up. To ring full-screen
        // then (unlocked / app backgrounded) we must start the activity directly, which
        // a background app may do only with "appear on top" (SYSTEM_ALERT_WINDOW).
        if (isScreenInteractive() && Settings.canDrawOverlays(context)) {
            runCatching { context.startActivity(intent) }
        }
    }

    private fun isScreenInteractive(): Boolean = runCatching {
        (context.getSystemService(Context.POWER_SERVICE) as PowerManager).isInteractive
    }.getOrDefault(true)


    private fun pendingIntentFor(link: DeepLink): PendingIntent{
        val intent = Intent(Intent.ACTION_VIEW, DeepLinks.uri(link), context, MainActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        return PendingIntent.getActivity(context,link.hashCode(), intent, flags)
    }

}