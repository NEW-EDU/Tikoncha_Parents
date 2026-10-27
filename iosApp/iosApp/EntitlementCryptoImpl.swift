import Foundation
import CryptoKit
import ComposeApp

/// Kotlin'dagi `EntitlementCryptoBridge`: server imzolagan PLUS holatini (ES256) CryptoKit bilan tekşiradi.
final class EntitlementCryptoImpl: NSObject, EntitlementCryptoBridge {
    private static let key: P256.Signing.PublicKey? = {
        guard let der = Data(base64Encoded: ParentEntitlementKt.ENTITLEMENT_PUBLIC_KEY_B64) else { return nil }
        return try? P256.Signing.PublicKey(derRepresentation: der)
    }()

    func verify(signingInput: String, rawSignatureB64: String) -> Bool {
        guard let key = Self.key,
              let raw = Data(base64Encoded: rawSignatureB64),
              let signature = try? P256.Signing.ECDSASignature(rawRepresentation: raw) else { return false }
        return key.isValidSignature(signature, for: Data(signingInput.utf8))
    }
}
