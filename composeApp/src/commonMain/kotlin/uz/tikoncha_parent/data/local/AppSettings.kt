package uz.tikoncha_parent.data.local

import com.russhwolf.settings.Settings
import com.russhwolf.settings.get
import com.russhwolf.settings.set
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import uz.tikoncha_parent.domain.model.SubscriptionLimit
import uz.tikoncha_parent.domain.model.UserInfo
import uz.tikoncha_parent.presentation.map.LatLng

object AppSettings {

    private val settings = Settings()

    // JSON konfiguratsiya: backend yangi field qo‘shsa ham crash bo‘lmaydi
    private val json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
        isLenient = true
        coerceInputValues = true
    }

    // Keys
    private const val KEY_FCM_TOKEN = "fcmToken"
    private const val KEY_REFRESH_TOKEN = "refreshToken"
    private const val KEY_ACCESS_TOKEN = "accessToken"
    private const val KEY_PROFILE_IMAGE_URL = "profileImageUrl"
    private const val KEY_USER_ID = "userId"
    private const val KEY_SELECTED_CHILD_ID = "selectedChildId"
    private const val KEY_POLICY_ID = "policyId"
    private const val KEY_HAS_USER_LOGIN = "hasUserLogin"
    private const val KEY_IS_FIRST_LAUNCH = "isFirstLaunch"
    private const val KEY_ONBOARDING_DONE = "onboardingDone"
    private const val KEY_LAST_COINS = "lastCoins"
    private const val KEY_LAST_ACTIVE_TASKS = "lastActiveTaskCount"
    private const val KEY_HOME_SNAPSHOTS = "homeSnapshots"
    private const val KEY_LAST_CHILD_LOCATION = "lastChildLocation"
    private const val KEY_HOME_LAYOUT = "homeLayout"

    private const val KEY_USER_INFO = "userInfo"
    private const val KEY_CHILDREN = "children"
    private const val KEY_SELECTED_CHILD = "selectedChild"
    private const val TEST_ACCOUNT = "TEST_ACCOUNT"

    private const val KEY_SUBSCRIPTION_LIMIT_LIST = "SubscriptionLimit"
    private const val KEY_SELECT_SUBSCRIPTION_LIMIT = "selectSubscriptionLimit"

    private const val KEY_USER_SUBSCRIPTION_MAP = "userSubscriptionMap"
    private const val KEY_TIKONCHA_TUTORIAL = "KEY_WHAT_IS_TIKONCHA"
    private const val KEY_POLICY_TUTORIAL = "KEY_WHAT_IS_POLICY"
    private const val KEY_BIND_CHILD_TUTORIAL = "KEY_BIND_CHILD"

    // ------------ primitives ------------

    var fcmToken: String
        get() = settings.get(KEY_FCM_TOKEN) ?: ""
        set(value) = settings.set(KEY_FCM_TOKEN, value)

    var refreshToken: String
        get() = settings.get(KEY_REFRESH_TOKEN) ?: ""
        set(value) = settings.set(KEY_REFRESH_TOKEN, value)

    var accessToken: String
        get() = settings.get(KEY_ACCESS_TOKEN) ?: ""
        set(value) = settings.set(KEY_ACCESS_TOKEN, value)

    var profileImageUrl: String
        get() = settings.get(KEY_PROFILE_IMAGE_URL) ?: ""
        set(value) = settings.set(KEY_PROFILE_IMAGE_URL, value)

    var userId: String
        get() = settings.get(KEY_USER_ID) ?: ""
        set(value) = settings.set(KEY_USER_ID, value)

    var selectedChildId: String
        get() = settings.get(KEY_SELECTED_CHILD_ID) ?: ""
        set(value) {
            // bo‘sh bo‘lsa keyni olib tashlaymiz ("" saqlab yurmaymiz)
            if (value.isBlank()) settings.remove(KEY_SELECTED_CHILD_ID)
            else settings.set(KEY_SELECTED_CHILD_ID, value)
        }

    var hasUserLogin: Boolean
        get() = settings.get(KEY_HAS_USER_LOGIN) ?: false
        set(value) = settings.set(KEY_HAS_USER_LOGIN, value)

    var isFirstLaunch: Boolean
        get() = settings.get(KEY_IS_FIRST_LAUNCH) ?: true
        set(value) = settings.set(KEY_IS_FIRST_LAUNCH, value)

    /** Birinchi ishga tushirishdagi tanishtiruv + bildirishnoma ruxsati ko'rsatildi (logout'da o'chmaydi). */
    var onboardingDone: Boolean
        get() = settings.get(KEY_ONBOARDING_DONE) ?: false
        set(value) = settings.set(KEY_ONBOARDING_DONE, value)

    /*
     * Oxirgi ma'lum qiymatlar — ekran ochilganda "0" ko'rinib, keyin haqiqiy songa sakramasligi uchun.
     * null = hali hech qachon yuklanmagan.
     */
    var lastCoins: Int?
        get() = settings.getIntOrNull(KEY_LAST_COINS)
        set(value) = if (value == null) settings.remove(KEY_LAST_COINS) else settings.putInt(KEY_LAST_COINS, value)

    var lastActiveTaskCount: Int?
        get() = settings.getIntOrNull(KEY_LAST_ACTIVE_TASKS)
        set(value) = if (value == null) settings.remove(KEY_LAST_ACTIVE_TASKS) else settings.putInt(KEY_LAST_ACTIVE_TASKS, value)

    private val homeSnapshotsSerializer = MapSerializer(String.serializer(), HomeSnapshot.serializer())

    /** Bosh sahifa kartalarining oxirgi ko'rinishi (farzand bo'yicha) — sovuq ishga tushishda nollar chaqnamasin. */
    var homeSnapshots: Map<String, HomeSnapshot>
        get() = getJsonOrDefault(KEY_HOME_SNAPSHOTS, homeSnapshotsSerializer, emptyMap())
        set(value) = putJson(KEY_HOME_SNAPSHOTS, homeSnapshotsSerializer, value)

    /** Eng yangi joylashuvi bor farzandning oxirgi nuqtasi — xarita darhol o'sha yerda ochilsin. */
    var lastChildLocation: LatLng?
        get() = settings.getStringOrNull(KEY_LAST_CHILD_LOCATION)?.split(",")?.let { p ->
            val lat = p.getOrNull(0)?.toDoubleOrNull(); val lon = p.getOrNull(1)?.toDoubleOrNull()
            if (lat != null && lon != null) LatLng(lat, lon) else null
        }
        set(value) = if (value == null) settings.remove(KEY_LAST_CHILD_LOCATION)
            else settings.putString(KEY_LAST_CHILD_LOCATION, "${value.lat},${value.lon}")

    /** Bosh sahifa ko'rinishi ("LIST" | "GRID") — logout'da o'chmaydi (qurilma sozlamasi). */
    var homeLayoutName: String
        get() = settings.getStringOrNull(KEY_HOME_LAYOUT) ?: "LIST"
        set(value) = settings.putString(KEY_HOME_LAYOUT, value)

    var showTikonchaTutorial: Boolean
        get() = settings.get(KEY_TIKONCHA_TUTORIAL) ?: true
        set(value) = settings.set(KEY_TIKONCHA_TUTORIAL, value)

    var showPolicyTutorial: Boolean
        get() = settings.get(KEY_POLICY_TUTORIAL) ?: true
        set(value) = settings.set(KEY_POLICY_TUTORIAL, value)

    var showBindChildTutorial: Boolean
        get() = settings.get(KEY_BIND_CHILD_TUTORIAL) ?: true
        set(value) = settings.set(KEY_BIND_CHILD_TUTORIAL, value)


    // ------------ JSON helpers ------------

    private fun <T> getJsonOrNull(key: String, serializer: KSerializer<T>): T? {
        val raw = settings.getStringOrNull(key) ?: return null
        if (raw.isBlank() || raw == "null") return null

        return runCatching { json.decodeFromString(serializer, raw) }
            .getOrElse {
                // corrupt/empty json bo‘lsa keyni tozalab yuboramiz
                settings.remove(key)
                null
            }
    }

    private fun <T> getJsonOrDefault(key: String, serializer: KSerializer<T>, default: T): T {
        return getJsonOrNull(key, serializer) ?: default
    }

    private fun <T> putJson(key: String, serializer: KSerializer<T>, value: T?) {
        if (value == null) {
            settings.remove(key)
            return
        }
        val raw = json.encodeToString(serializer, value)
        settings.putString(key, raw)
    }

    private val userSubscriptionMapSerializer =
        kotlinx.serialization.builtins.MapSerializer(
            kotlinx.serialization.serializer<String>(),
            kotlinx.serialization.serializer<Boolean>()
        )


    // ------------ UserInfo ------------

    var userInfo: UserInfo?
        get() = getJsonOrNull(KEY_USER_INFO, UserInfo.serializer())
        set(value) = putJson(KEY_USER_INFO, UserInfo.serializer(), value)

    var isTestAccount: Boolean
        get() = settings.get(TEST_ACCOUNT) ?: false
        set(value) = settings.set(TEST_ACCOUNT, value)


    // ------------ Children list ------------

    private val userInfoListSerializer = ListSerializer(UserInfo.serializer())

    var children: List<UserInfo>
        get() = getJsonOrDefault(KEY_CHILDREN, userInfoListSerializer, emptyList())
        set(value) = putJson(KEY_CHILDREN, userInfoListSerializer, value)

    // ------------ Selected child ------------

    var selectedChild: UserInfo?
        get() = getJsonOrNull(KEY_SELECTED_CHILD, UserInfo.serializer())
        set(value) = putJson(KEY_SELECTED_CHILD, UserInfo.serializer(), value)

    // ------------ SubscriptionLimit ------------

    private val subscriptionLimitListSerializer = ListSerializer(SubscriptionLimit.serializer())

    var subscriptionLimitList: List<SubscriptionLimit>
        get() = getJsonOrDefault(KEY_SUBSCRIPTION_LIMIT_LIST, subscriptionLimitListSerializer, emptyList())
        set(value) = putJson(KEY_SUBSCRIPTION_LIMIT_LIST, subscriptionLimitListSerializer, value)

    var selectSubscriptionLimit: SubscriptionLimit
        get() = getJsonOrDefault(KEY_SELECT_SUBSCRIPTION_LIMIT, SubscriptionLimit.serializer(), SubscriptionLimit())
        set(value) = putJson(KEY_SELECT_SUBSCRIPTION_LIMIT, SubscriptionLimit.serializer(), value)

    var userSubscriptionMap: Map<String, Boolean>
        get() = getJsonOrDefault(
            KEY_USER_SUBSCRIPTION_MAP,
            userSubscriptionMapSerializer,
            emptyMap()
        )
        set(value) = putJson(
            KEY_USER_SUBSCRIPTION_MAP,
            userSubscriptionMapSerializer,
            value
        )
    fun setUserSubscription(phone: String, isSubscribed: Boolean) {
        if (phone.isBlank()) return

        val updated = userSubscriptionMap.toMutableMap()
        updated[phone] = isSubscribed
        userSubscriptionMap = updated
    }

    // ------------ Utilities ------------

    /**
     * Logout qilganda chaqir: "Said" qaytib kelishini butunlay yo‘q qiladi.
     */
    fun clearSession() {
        SessionCache.clear()
        // auth
        settings.remove(KEY_ACCESS_TOKEN)
        settings.remove(KEY_REFRESH_TOKEN)
        settings.remove(KEY_USER_ID)
        settings.remove(KEY_PROFILE_IMAGE_URL)
        settings.remove(KEY_POLICY_ID)
        settings.remove(KEY_HAS_USER_LOGIN)
        settings.remove(KEY_FCM_TOKEN)

        // user/children
        settings.remove(KEY_USER_INFO)
        settings.remove(KEY_CHILDREN)
        settings.remove(KEY_SELECTED_CHILD)
        settings.remove(KEY_SELECTED_CHILD_ID)
        settings.remove(KEY_LAST_COINS)
        settings.remove(KEY_LAST_ACTIVE_TASKS)
        settings.remove(KEY_HOME_SNAPSHOTS)
        settings.remove(KEY_LAST_CHILD_LOCATION)


        // optional
        settings.remove(KEY_SUBSCRIPTION_LIMIT_LIST)
        settings.remove(KEY_SELECT_SUBSCRIPTION_LIMIT)
        // fcmToken ni odatda saqlab qolsa ham bo‘ladi; xohlasang remove qil:
        // settings.remove(KEY_FCM_TOKEN)
    }

    /**
     * Serverdan children kelgandan keyin chaqir:
     * selectedChildId/selectedChild mismatch bo‘lsa to‘g‘rilaydi.
     */
    fun syncSelectedChildWith(childrenFromServer: List<UserInfo>) {
        children = childrenFromServer

        if (childrenFromServer.isEmpty()) {
            selectedChild = null
            selectedChildId = ""
            return
        }

        val byId = selectedChildId.takeIf { it.isNotBlank() }
            ?.let { id -> childrenFromServer.firstOrNull { it.userId == id } }

        val selected = byId ?: childrenFromServer.firstOrNull()

        selectedChild = selected
        selectedChildId = selected?.userId.orEmpty()
    }
}
