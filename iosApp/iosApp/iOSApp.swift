import SwiftUI
import ComposeApp
import YandexMapsMobile
import TelegramLogin

@main
struct iOSApp: App {

    @UIApplicationDelegateAdaptor(AppDelegate.self) var appDelegate

    init() {
        // --- Yandex Maps ---
        YMKMapKit.setApiKey("21612db3-4394-4fde-b579-d2e7a1f9afa3")
        YMKMapKit.setLocale("uz_UZ")
        let _ = YMKMapKit.sharedInstance()

        // --- Telegram login ---
        // MUHIM: redirectUri BotFather generatsiya qilgan Universal Link bilan
        // AYNAN bir xil bo'lishi shart — PATH YO'Q.
        // Android'dagi "/tglogin" boshqa host uchun, iOS'ga tegishli emas.
        let redirect = "https://\(TelegramConfig.shared.redirectHost)"
        print("TG_CONFIG redirectUri=\(redirect)")
        TelegramLogin.configure(
            clientId: TelegramConfig.shared.CLIENT_ID,
            redirectUri: redirect,
            scopes: ["openid", "profile", "phone", "telegram:bot_access"]
        )
        TelegramAuthBridgeProvider.shared.bridge = TelegramAuthBridgeImpl()

        // --- Live (jonli A/V) Ed25519 — CryptoKit bridge ---
        LiveEd25519BridgeProvider.shared.bridge = LiveEd25519BridgeImpl()
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
                // Native Telegram qaytgandagi ASOSIY yo'l (Universal Link)
                .onContinueUserActivity(NSUserActivityTypeBrowsingWeb) { activity in
                    let url = activity.webpageURL
                    print("TG continue url=\(url?.absoluteString ?? "nil") host=\(url?.host ?? "nil")")
                    if let url { handleTelegram(url) }
                }
                // Custom scheme zaxira yo'li (hozir ishlatilmasa ham zarar qilmaydi)
                .onOpenURL { url in
                    print("TG openURL url=\(url.absoluteString) host=\(url.host ?? "nil")")
                    handleTelegram(url)
                }
        }
    }


    private func handleTelegram(_ url: URL) {
        if TelegramConfig.shared.isTelegramHost(host: url.host) {
            print("TG -> host mos, handle() chaqirilmoqda")
            TelegramLogin.handle(url)
        } else {
            print("TG -> host MOS EMAS: \(url.host ?? "nil") (tashlab yuborildi)")
        }
    }
}
