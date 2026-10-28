import Foundation
import TelegramLogin
import ComposeApp

final class TelegramAuthBridgeImpl: TelegramAuthBridge {

    func startLogin(callback: TelegramAuthBridgeCallback) {
        print("TG_BRIDGE login boshlanmoqda, redirect=\(TelegramConfig.shared.redirectHost)")
        MainActor.assumeIsolated {
            TelegramLogin.login { result in
                print("TG_BRIDGE completion keldi: \(result)")   // ← bu chiqyaptimi?
                switch result {
                case .success(let data): callback.onSuccess(idToken: data.idToken)
                case .failure(let error):
                    if let tgError = error as? TelegramLoginError, case .cancelled = tgError {
                        callback.onCancel()
                    } else if (error as NSError).domain == NSURLErrorDomain {
                        // Foydalanuvchiga xom NSError emas — "internet" xabari (Kotlin tomoni tarjima qiladi).
                        print("TG_BRIDGE network error: \(error)")
                        callback.onError(message: "network")
                    } else {
                        print("TG_BRIDGE error: \(error)")
                        callback.onError(message: "telegram")
                    }
                }
            }
        }
    }
}
