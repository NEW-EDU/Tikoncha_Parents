package uz.tikoncha_parent.presentation.splash

import uz.tikoncha_parent.presentation.profile.subscription.autopay.method.ChildArgs
import uz.tikoncha_parent.presentation.profile.subscription.autopay.method.PaymentMethodScreen
import uz.tikoncha_parent.presentation.profile.subscription.subscription_payment.SubscriptionPaymentScreen
import uz.tikoncha_parent.presentation.profile.coins.CoinsScreen
import uz.tikoncha_parent.presentation.profile.subscription.info.SubscriptionScreen
import uz.tikoncha_parent.presentation.profile.children.ChildrenSelectScreen
import uz.tikoncha_parent.presentation.chat.chat_list.ChatScreen
import uz.tikoncha_parent.presentation.protection.ProtectionScreen
import uz.tikoncha_parent.presentation.task.TaskScreen
import uz.tikoncha_parent.presentation.policy.list.PolicyListScreen
import uz.tikoncha_parent.presentation.statistic.StatisticScreen
import uz.tikoncha_parent.presentation.profile.ProfileScreen
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import kotlinx.coroutines.delay
import uz.tikoncha_parent.data.local.AppSettings
import uz.tikoncha_parent.presentation.login.LoginScreen
import uz.tikoncha_parent.presentation.onboarding.OnboardingScreen
import uz.tikoncha_parent.presentation.onboarding.NotificationPermissionScreen
import uz.tikoncha_parent.presentation.add_child.AddChildScreen
import uz.tikoncha_parent.platform.BuildConfig
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import tikoncha_parents.composeapp.generated.resources.Res
import tikoncha_parents.composeapp.generated.resources.tikoncha_logo
import uz.tikoncha_parent.platform.Logger
import uz.tikoncha_parent.presentation.new_home.NewHomeScreen
import uz.tikoncha_parent.ui.theme.AppColors
import uz.tikoncha_parent.ui.theme.TikonchaParentTheme
import uz.tikoncha_parent.ui.theme.extendedColor

class SplashScreen : Screen {

    @Composable
    override fun Content() {


        val navigator = LocalNavigator.current

        LaunchedEffect(true) {

            println("Launcher screem")



            delay(1000) // 1 sekund
            // Faqat debug: tanishtiruv ekranlarini tizimga kirgan holda ham ko'rish (ekran rasmlari uchun)
            val debugArgs = BuildConfig.debugLaunchArgs
            when {
                "-hoglt.onboarding" in debugArgs -> { navigator?.replaceAll(OnboardingScreen()); return@LaunchedEffect }
                "-hoglt.notif" in debugArgs -> { navigator?.replaceAll(NotificationPermissionScreen()); return@LaunchedEffect }
                "-hoglt.addchild" in debugArgs -> { navigator?.replaceAll(AddChildScreen()); return@LaunchedEffect }
                "-hoglt.profile" in debugArgs -> { navigator?.replaceAll(listOf(NewHomeScreen(), ProfileScreen())); return@LaunchedEffect }
                "-hoglt.screen" in debugArgs -> {
                    val target: Screen? = when (debugArgs.getOrNull(debugArgs.indexOf("-hoglt.screen") + 1)) {
                        "statistic" -> StatisticScreen()
                        "policy" -> PolicyListScreen()
                        "task" -> TaskScreen()
                        "createtask" -> uz.tikoncha_parent.presentation.task.create_task.CreateTaskScreen()
                        "protection" -> ProtectionScreen()
                        "chat" -> ChatScreen()
                        "children" -> ChildrenSelectScreen()
                        "theme" -> uz.tikoncha_parent.presentation.profile.settings.theme.ThemeScreen()
                        "settings" -> uz.tikoncha_parent.presentation.profile.settings.SettingsScreen()
                        "haptics" -> uz.tikoncha_parent.presentation.profile.settings.haptics.HapticsScreen()
                        "map" -> uz.tikoncha_parent.presentation.tracking.TrackingScreen()
                        "language" -> uz.tikoncha_parent.presentation.profile.language.LanguageScreen()
                        "personal" -> uz.tikoncha_parent.presentation.profile.personal_information.PersonalInformationScreen()
                        "about" -> uz.tikoncha_parent.presentation.profile.about_us.AboutUsScreen()
                        "cards" -> uz.tikoncha_parent.presentation.profile.subscription.autopay.cards.AutopayCardsScreen()
                        "childdetail" -> AppSettings.selectedChild?.let { uz.tikoncha_parent.presentation.profile.children.ChildDetailScreen(it) } ?: ChildrenSelectScreen()
                        "subscription" -> SubscriptionScreen()
                        "coins" -> CoinsScreen()
                        "paywall" -> SubscriptionPaymentScreen()
                        "paymethod" -> PaymentMethodScreen(
                            child = ChildArgs(id = AppSettings.selectedChild?.userId, name = AppSettings.selectedChild?.name.orEmpty(), avatarUrl = null, plusUntil = null),
                            planId = "debug", period = "MONTHLY", cardAmount = 27000, clickAmount = 30000,
                        )
                        else -> null
                    }
                    if (target != null) { navigator?.replaceAll(listOf(NewHomeScreen(), target)); return@LaunchedEffect }
                }
            }
            if (AppSettings.hasUserLogin) {
                Logger.d("SplashScreen", "hasUserLogin")
                navigator?.replaceAll(NewHomeScreen())
            } else if (!AppSettings.onboardingDone) {
                // Birinchi ishga tushirish: tanishtiruv → bildirishnoma ruxsati → kirish
                navigator?.replaceAll(OnboardingScreen())
            } else {
                navigator?.replaceAll(LoginScreen())
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(AppColors.bg.page),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                painter = painterResource(Res.drawable.tikoncha_logo),
                contentDescription = null,
                tint = MaterialTheme.extendedColor.primaryColor,
                modifier = Modifier.size(200.dp)
            )
        }
    }
}

@Preview
@Composable
fun Preview() {
    TikonchaParentTheme() {
        SplashScreen().Content()
    }
}