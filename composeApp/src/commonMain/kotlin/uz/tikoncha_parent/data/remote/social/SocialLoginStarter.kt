package uz.tikoncha_parent.data.remote.social

/** Sign in with Apple — faqat iOS'da (Android'da false). */
expect fun isAppleLoginAvailable(): Boolean

/** Tizim oynasini ochadi; natija [SocialAuthBus] ga keladi. false — boshlab bo'lmadi. */
expect fun startAppleLogin(): Boolean

expect fun startGoogleLogin(): Boolean

/** Firebase loyihasi (tikoncha-d0995) OAuth mijozlari. Client ID'lar maxfiy emas. */
object GoogleOAuthConfig {
    /** Web client — Android Credential Manager `serverClientId`; backend `aud` sifatida tekshiradi. */
    const val WEB_CLIENT_ID = "935823573557-728j75sq21sq4kkpuogh2o07m3pm5uc1.apps.googleusercontent.com"
    /** Ota-ona iOS ilovasi (www.newedu.tikoncha-parent.uz). */
    const val IOS_CLIENT_ID = "935823573557-0f1ath3suah83snfhk51cq87ap8e23os.apps.googleusercontent.com"
}
