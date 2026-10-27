// composeApp/src/commonMain/kotlin/uz/tikoncha_parent/App.kt
package uz.tikoncha_parent

import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalInspectionMode
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.NavigatorDisposeBehavior
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import tikoncha_parents.composeapp.generated.resources.Res
import tikoncha_parents.composeapp.generated.resources.bekor_qilish
import tikoncha_parents.composeapp.generated.resources.dialog_failed
import tikoncha_parents.composeapp.generated.resources.login_qilish
import tikoncha_parents.composeapp.generated.resources.token_eskirgan
import tikoncha_parents.composeapp.generated.resources.tokenni_yangilash_uchun
import uz.tikoncha_parent.data.remote.AuthEvent
import uz.tikoncha_parent.data.remote.AuthEventBus
import uz.tikoncha_parent.domain.model.DeepLink
import uz.tikoncha_parent.platform.AppEnvironment
import uz.tikoncha_parent.platform.Logger
import uz.tikoncha_parent.presentation.base.CustomDialog
import uz.tikoncha_parent.presentation.chat.chat_list.ChatScreen
import uz.tikoncha_parent.presentation.chat.chat_room.ChatRoomScreen
import uz.tikoncha_parent.presentation.login.LoginScreen
import uz.tikoncha_parent.presentation.map.MapKitInitializer
import uz.tikoncha_parent.presentation.model.ChatType
import uz.tikoncha_parent.presentation.navigation.SwipeBackContent
import uz.tikoncha_parent.presentation.new_home.NewHomeScreen
import uz.tikoncha_parent.presentation.profile.language.LanguageController
import uz.tikoncha_parent.presentation.profile.language.LocalLanguageController
import uz.tikoncha_parent.presentation.protection.ProtectionScreen
import uz.tikoncha_parent.presentation.push.DeepLinkEffect
import uz.tikoncha_parent.presentation.push.PendingDeepLinks
import uz.tikoncha_parent.presentation.push.navigateByDeepLink
import uz.tikoncha_parent.presentation.splash.SplashScreen
import uz.tikoncha_parent.ui.theme.PlatformThemeBridge
import uz.tikoncha_parent.ui.theme.ThemeController
import uz.tikoncha_parent.ui.theme.TikonchaParentTheme

@Composable
@Preview
fun App() {

    val langController = remember { LanguageController() }
    val mode by ThemeController.mode.collectAsState()
    val restartKey by AppRestartBus.key.collectAsState()

    val inPreview = LocalInspectionMode.current
    if (!inPreview) {
        SideEffect { PlatformThemeBridge.onModeChanged(mode) }
    }

    AppEnvironment {
        CompositionLocalProvider(LocalLanguageController provides langController) {

            val disposeBehavior = remember {
                NavigatorDisposeBehavior(
                    disposeNestedNavigators = true,
                    disposeSteps = true,
                )
            }

            TikonchaParentTheme(mode = mode) {
                // Bo'sh joyga bosish klaviaturani yopadi (iPhone raqam klaviaturasida "Tayyor" tugmasi yo'q).
                // Tugma/maydon bosilsa ular hodisani o'zlari egallaydi — bu faqat bo'sh joyda ishlaydi.
                val focusManager = LocalFocusManager.current
                Surface(
                    modifier = Modifier.pointerInput(Unit) {
                        detectTapGestures(onTap = { focusManager.clearFocus() })
                    }
                ) {

                    // Cold start uchun yig'ilgan deep linklar
                    val pendingLinks = remember { PendingDeepLinks.drain() }
                    val initialStack: List<Screen> = remember(pendingLinks) {
                        initialStackFor(pendingLinks.firstOrNull()) ?: emptyList()
                    }

                    Logger.d("App", "pendingLinks=$pendingLinks")
                    Logger.d("App", "initialStack=$initialStack")

                    key(restartKey) {
                        if (initialStack.isNotEmpty()) {
                            val first = initialStack.first()
                            val rest = initialStack.drop(1)

                            Navigator(first, disposeBehavior) { nav ->
                                SwipeBackContent(nav)
                                DeepLinkEffect(nav)
                                AuthEventListener(nav)

                                // Stack'ni to'liq tiklash
                                LaunchedEffect(rest) {
                                    rest.forEach { nav.push(it) }
                                }
                                // Qolgan pendinglarni navbatdan o'tkazish
                                LaunchedEffect(pendingLinks) {
                                    pendingLinks.drop(1).forEach { navigateByDeepLink(nav, it) }
                                }
                            }
                        } else {
                            Navigator(SplashScreen(), disposeBehavior) { nav ->
                                SwipeBackContent(navigator = nav)
                                DeepLinkEffect(nav)
                                AuthEventListener(nav)
                            }
                        }
                    }
                }
            }
        }
    }
}

/** Cold start'da deep link bo'yicha boshlang'ich stack */
private fun initialStackFor(link: DeepLink?): List<Screen>? = when (link) {
    is DeepLink.Chat -> listOf(
        NewHomeScreen(),
        ChatScreen(),
        ChatRoomScreen(
            chatId = link.chatId,
            chatAvatar = "",
            chatTitle = link.chatTitle ?: "",
            chatType = ChatType.NONE,
        )
    )
    DeepLink.ParentalRequest -> listOf(NewHomeScreen(), ProtectionScreen())
    // ⬇️ StrictDisable ekraniga moslang (hozircha ProtectionScreen)
    DeepLink.StrictDisable -> listOf(NewHomeScreen(), ProtectionScreen())
    is DeepLink.General, null -> null
}

fun initMapKit() {
    try {
        val mapKey = "21612db3-4394-4fde-b579-d2e7a1f9afa3"
        MapKitInitializer.initialize(mapKey)
        Logger.d("App", "MAPKIT_OK initialized")
    } catch (e: Throwable) {
        Logger.d("App", "MAPKIT_ERROR: ${e::class.simpleName}: ${e.message}")
    }
}

@Composable
private fun AuthEventListener(navigator: Navigator) {

    var showDialog by remember { mutableStateOf(false) }

    CustomDialog(
        title = stringResource(Res.string.token_eskirgan),
        message = stringResource(Res.string.tokenni_yangilash_uchun),
        painter = painterResource(Res.drawable.dialog_failed),
        buttonText = stringResource(Res.string.login_qilish),
        buttonText2 = stringResource(Res.string.bekor_qilish),
        showCloseButton = false,
        show = showDialog,
        onButtonClick = {
            showDialog = false
            if (navigator.lastItem !is LoginScreen) {
                navigator.replaceAll(LoginScreen())
            }
        },
        onDismiss = { showDialog = false },
    )

    // iPhone: pul yechilgan, lekin server hali tasdiqlamagan App Store xaridlari (internet uzilgan, ilova yopilgan)
    val iap: uz.tikoncha_parent.domain.use_case.payment.PurchaseIApPremiumUseCase = org.koin.compose.koinInject()
    LaunchedEffect(Unit) {
        if (uz.tikoncha_parent.platform.isIos()) runCatching { iap.retryPending() }
    }

    LaunchedEffect(navigator) {
        AuthEventBus.events.collect { event ->
            when (event) {
                AuthEvent.SessionExpired -> showDialog = true
            }
        }
    }
}