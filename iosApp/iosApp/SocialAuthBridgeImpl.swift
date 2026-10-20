import Foundation
import AuthenticationServices
import CryptoKit
import UIKit
import ComposeApp

/// Sign in with Apple (AuthenticationServices) va Sign in with Google (ASWebAuthenticationSession + PKCE).
/// Natija Kotlin'dagi `SocialAuthBus`ga ketadi; backend `auth/apple-login` / `auth/google-login` tokenni tekshiradi.
/// Google uchun SDK kerak emas: iOS OAuth mijozi maxfiy kalitsiz ishlaydi, tizim brauzeri oynasi ochiladi.
final class SocialAuthBridgeImpl: NSObject, SocialAuthBridge {

    private var appleCallback: SocialAuthBridgeCallback?
    private var appleController: ASAuthorizationController?
    private var webSession: ASWebAuthenticationSession?

    // MARK: Apple

    func startApple(callback: SocialAuthBridgeCallback) {
        DispatchQueue.main.async {
            let request = ASAuthorizationAppleIDProvider().createRequest()
            request.requestedScopes = [.fullName, .email]
            let controller = ASAuthorizationController(authorizationRequests: [request])
            controller.delegate = self
            controller.presentationContextProvider = self
            self.appleCallback = callback
            self.appleController = controller
            controller.performRequests()
        }
    }

    // MARK: Google

    func startGoogle(clientId: String, callback: SocialAuthBridgeCallback) {
        // com.googleusercontent.apps.<raqam-xesh> — Google iOS mijozining qaytish sxemasi
        let scheme = "com.googleusercontent.apps." + clientId.replacingOccurrences(of: ".apps.googleusercontent.com", with: "")
        let redirectUri = "\(scheme):/oauth2redirect"
        let verifier = Self.randomURLSafe(32)
        let challenge = Self.base64URL(Data(SHA256.hash(data: Data(verifier.utf8))))
        let state = Self.randomURLSafe(16)

        var comps = URLComponents(string: "https://accounts.google.com/o/oauth2/v2/auth")!
        comps.queryItems = [
            .init(name: "client_id", value: clientId),
            .init(name: "redirect_uri", value: redirectUri),
            .init(name: "response_type", value: "code"),
            .init(name: "scope", value: "openid email profile"),
            .init(name: "code_challenge", value: challenge),
            .init(name: "code_challenge_method", value: "S256"),
            .init(name: "state", value: state),
            .init(name: "prompt", value: "select_account"),
        ]
        guard let authURL = comps.url else { callback.onError(message: "Bad Google auth URL"); return }

        DispatchQueue.main.async {
            let session = ASWebAuthenticationSession(url: authURL, callbackURLScheme: scheme) { [weak self] url, error in
                self?.webSession = nil
                if let error = error as? ASWebAuthenticationSessionError, error.code == .canceledLogin {
                    callback.onCancel(); return
                }
                if let error { callback.onError(message: error.localizedDescription); return }
                guard let url,
                      let items = URLComponents(url: url, resolvingAgainstBaseURL: false)?.queryItems,
                      items.first(where: { $0.name == "state" })?.value == state,
                      let code = items.first(where: { $0.name == "code" })?.value else {
                    callback.onError(message: "Google sign-in was not completed"); return
                }
                Self.exchange(code: code, clientId: clientId, redirectUri: redirectUri, verifier: verifier, callback: callback)
            }
            session.presentationContextProvider = self
            session.prefersEphemeralWebBrowserSession = false
            self.webSession = session
            if !session.start() { callback.onError(message: "Could not open Google sign-in") }
        }
    }

    /// Avtorizatsiya kodi → ID token (PKCE; iOS mijozida client secret yo'q).
    private static func exchange(code: String, clientId: String, redirectUri: String, verifier: String,
                                 callback: SocialAuthBridgeCallback) {
        var request = URLRequest(url: URL(string: "https://oauth2.googleapis.com/token")!)
        request.httpMethod = "POST"
        request.setValue("application/x-www-form-urlencoded", forHTTPHeaderField: "Content-Type")
        let form: [(String, String)] = [
            ("code", code), ("client_id", clientId), ("redirect_uri", redirectUri),
            ("code_verifier", verifier), ("grant_type", "authorization_code"),
        ]
        request.httpBody = form
            .map { "\($0.0)=\($0.1.addingPercentEncoding(withAllowedCharacters: .urlQueryValueAllowed) ?? "")" }
            .joined(separator: "&")
            .data(using: .utf8)
        URLSession.shared.dataTask(with: request) { data, _, error in
            DispatchQueue.main.async {
                if let error { callback.onError(message: error.localizedDescription); return }
                guard let data,
                      let json = try? JSONSerialization.jsonObject(with: data) as? [String: Any],
                      let idToken = json["id_token"] as? String else {
                    callback.onError(message: "Google did not return an ID token"); return
                }
                callback.onSuccess(token: idToken, fullName: nil)
            }
        }.resume()
    }

    private static func randomURLSafe(_ bytes: Int) -> String {
        var data = Data(count: bytes)
        _ = data.withUnsafeMutableBytes { SecRandomCopyBytes(kSecRandomDefault, bytes, $0.baseAddress!) }
        return base64URL(data)
    }

    private static func base64URL(_ data: Data) -> String {
        data.base64EncodedString()
            .replacingOccurrences(of: "+", with: "-")
            .replacingOccurrences(of: "/", with: "_")
            .replacingOccurrences(of: "=", with: "")
    }
}

extension SocialAuthBridgeImpl: ASAuthorizationControllerDelegate {
    func authorizationController(controller: ASAuthorizationController, didCompleteWithAuthorization authorization: ASAuthorization) {
        defer { appleCallback = nil; appleController = nil }
        guard let credential = authorization.credential as? ASAuthorizationAppleIDCredential,
              let tokenData = credential.identityToken,
              let token = String(data: tokenData, encoding: .utf8) else {
            appleCallback?.onError(message: "Apple did not return an identity token"); return
        }
        // Ism faqat birinchi kirishda keladi
        let name = [credential.fullName?.givenName, credential.fullName?.familyName]
            .compactMap { $0 }.filter { !$0.isEmpty }.joined(separator: " ")
        appleCallback?.onSuccess(token: token, fullName: name.isEmpty ? nil : name)
    }

    func authorizationController(controller: ASAuthorizationController, didCompleteWithError error: Error) {
        defer { appleCallback = nil; appleController = nil }
        if let e = error as? ASAuthorizationError, e.code == .canceled {
            appleCallback?.onCancel()
        } else {
            appleCallback?.onError(message: error.localizedDescription)
        }
    }
}

extension SocialAuthBridgeImpl: ASAuthorizationControllerPresentationContextProviding, ASWebAuthenticationPresentationContextProviding {
    private var keyWindow: ASPresentationAnchor {
        UIApplication.shared.connectedScenes
            .compactMap { $0 as? UIWindowScene }
            .flatMap { $0.windows }
            .first { $0.isKeyWindow } ?? ASPresentationAnchor()
    }

    func presentationAnchor(for controller: ASAuthorizationController) -> ASPresentationAnchor { keyWindow }

    func presentationAnchor(for session: ASWebAuthenticationSession) -> ASPresentationAnchor { keyWindow }
}

private extension CharacterSet {
    static let urlQueryValueAllowed: CharacterSet = {
        var set = CharacterSet.urlQueryAllowed
        set.remove(charactersIn: "&+=?/:")
        return set
    }()
}
