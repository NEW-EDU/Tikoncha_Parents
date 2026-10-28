import Foundation
import StoreKit
import ComposeApp

/// StoreKit 2 — Kotlin'dagi `StoreKitBridge`. Xarid natijasi Apple imzolagan JWS (server uni tekshiradi va PLUS'ni
/// farzandga yoqadi). Tranzaksiya faqat server tasdiqlagandan keyin yopiladi (`finish`); yopilmaganlari keyingi
/// ochilishda `unfinished` orqali qayta yuboriladi.
final class StoreKitBridgeImpl: NSObject, StoreKitBridge {

    func purchase(productId: String, appAccountToken: String?, callback: StoreKitPurchaseCallback) {
        _Concurrency.Task { @MainActor in
            do {
                guard let product = try await Self.product(productId) else {
                    callback.onError(message: "Product not found: \(productId)")
                    return
                }
                var options: Set<Product.PurchaseOption> = []
                if let token = appAccountToken, let uuid = UUID(uuidString: token) {
                    options.insert(.appAccountToken(uuid))
                }
                switch try await product.purchase(options: options) {
                case .success(let verification):
                    switch verification {
                    case .verified(let tx):
                        callback.onSigned(jws: verification.jwsRepresentation, transactionId: String(tx.id))
                    case .unverified(_, let error):
                        callback.onError(message: error.localizedDescription)
                    }
                case .userCancelled:
                    callback.onCancel()
                case .pending:
                    callback.onPending()
                @unknown default:
                    callback.onError(message: "Unknown purchase result")
                }
            } catch {
                callback.onError(message: error.localizedDescription)
            }
        }
    }

    /// App Store ba'zan mahsulotni birinçi so'rovda qaytarmaydi (tarmoq, yangi mahsulot) — 3 marta urinib köriladi.
    private static func product(_ id: String) async throws -> Product? {
        for attempt in 0..<3 {
            if let p = try await Product.products(for: [id]).first { return p }
            if attempt < 2 { try? await _Concurrency.Task.sleep(nanoseconds: 1_500_000_000) }
        }
        return nil
    }

    func finish(transactionId: String) {
        _Concurrency.Task {
            for await result in Transaction.unfinished {
                if case .verified(let tx) = result, String(tx.id) == transactionId {
                    await tx.finish()
                }
            }
        }
    }

    func unfinished(callback: StoreKitListCallback) {
        _Concurrency.Task { @MainActor in
            var items: [String] = []
            for await result in Transaction.unfinished {
                if case .verified(let tx) = result {
                    items.append("\(tx.id)\t\(result.jwsRepresentation)")
                }
            }
            callback.onResult(items: items)
        }
    }

    func displayPrice(productId: String, callback: StoreKitPriceCallback) {
        _Concurrency.Task { @MainActor in
            let product = try? await Self.product(productId)
            callback.onResult(price: product?.displayPrice)
        }
    }
}
