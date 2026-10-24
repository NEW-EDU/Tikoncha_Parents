import UIKit
import UserNotifications
import FirebaseCore
import FirebaseMessaging
import ComposeApp

class AppDelegate: UIResponder, UIApplicationDelegate, UNUserNotificationCenterDelegate, MessagingDelegate {

    func application(_ application: UIApplication, didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey : Any]? = nil) -> Bool {
        FirebaseApp.configure()

        // Bildirishnoma ruxsati ilova ochilganda so'ralmaydi — tanishtiruvdagi alohida ekranda
        // (NotificationPermissionScreen → PushPlatform.requestNotificationPermission).
        UNUserNotificationCenter.current().delegate = self
        Messaging.messaging().delegate = self
        application.registerForRemoteNotifications()
        return true
    }

    func application(_ application: UIApplication, didRegisterForRemoteNotificationsWithDeviceToken deviceToken: Data) {
        print("Device token:\(deviceToken)")
        Messaging.messaging().apnsToken = deviceToken
    }

    // DIQQAT: Telegram URL / Universal Link handling'i BU YERDA EMAS.
    // Ilova SwiftUI scene-based (@UIApplicationDelegateAdaptor) bo'lgani uchun
    // application(_:open:) va application(_:continue:) bu arxitekturada CHAQIRILMAYDI.
    // Callback iOSApp.swift dagi .onContinueUserActivity / .onOpenURL orqali keladi.

    func userNotificationCenter(_ center: UNUserNotificationCenter,
                                willPresent notification: UNNotification,
                                withCompletionHandler completionHandler: @escaping (UNNotificationPresentationOptions) -> Void) {
        let raw = notification.request.content.userInfo["payload"] as? String
        IosPushEntry.shared.onMessage(payloadRaw: raw)
        completionHandler([.banner, .badge, .sound])
    }

    func userNotificationCenter(_ center: UNUserNotificationCenter,
                                didReceive response: UNNotificationResponse,
                                withCompletionHandler completionHandler: @escaping () -> Void) {
        let raw = response.notification.request.content.userInfo["payload"] as? String
        IosPushEntry.shared.onTap(payloadRaw: raw)
        completionHandler()
    }

    func messaging(_ messaging: Messaging, didReceiveRegistrationToken fcmToken: String?) {
        if let token = fcmToken { IosPushEntry.shared.submitToken(token: token) }
    }
}
