import Foundation
import CryptoKit
import ComposeApp

/// Ed25519 (CryptoKit) — jonli signalizatsiya ramkalarini imzolash/tekshirish uchun.
/// Maxfiy kalit qurilmani tark etmaydi; bola bu qurilmaning PUBLIC kalitini oladi va
/// imzolarni uchdan-uchiga tekshiradi. Xom shakllar: private 32B, public 32B, sig 64B.
final class LiveEd25519BridgeImpl: LiveEd25519Bridge {

    func generatePrivateKey() -> KotlinByteArray {
        return Curve25519.Signing.PrivateKey().rawRepresentation.toKotlinByteArray()
    }

    func publicKey(privateKey: KotlinByteArray) -> KotlinByteArray {
        guard let key = try? Curve25519.Signing.PrivateKey(rawRepresentation: privateKey.toData()) else {
            return KotlinByteArray(size: 0)
        }
        return key.publicKey.rawRepresentation.toKotlinByteArray()
    }

    func sign(privateKey: KotlinByteArray, message: KotlinByteArray) -> KotlinByteArray {
        guard let key = try? Curve25519.Signing.PrivateKey(rawRepresentation: privateKey.toData()),
              let signature = try? key.signature(for: message.toData()) else {
            return KotlinByteArray(size: 0)
        }
        return signature.toKotlinByteArray()
    }

    func verify(publicKey: KotlinByteArray, signature: KotlinByteArray, message: KotlinByteArray) -> Bool {
        guard let pk = try? Curve25519.Signing.PublicKey(rawRepresentation: publicKey.toData()) else {
            return false
        }
        return pk.isValidSignature(signature.toData(), for: message.toData())
    }
}

private extension Data {
    func toKotlinByteArray() -> KotlinByteArray {
        let arr = KotlinByteArray(size: Int32(count))
        for (i, b) in enumerated() {
            arr.set(index: Int32(i), value: Int8(bitPattern: b))
        }
        return arr
    }
}

private extension KotlinByteArray {
    func toData() -> Data {
        var data = Data(count: Int(size))
        for i in 0..<Int(size) {
            data[i] = UInt8(bitPattern: get(index: Int32(i)))
        }
        return data
    }
}
