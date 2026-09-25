package uz.tikoncha_parent.presentation.chat

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import uz.tikoncha_parent.data.local.AppSettings
import uz.tikoncha_parent.platform.Logger
import uz.tikoncha_parent.platform.UniversalJsonWebView
import uz.tikoncha_parent.ui.theme.AppColors
import uz.tikoncha_parent.ui.theme.rememberIsDarkTheme
import uz.tikoncha_parent.ui.theme.rememberScreenSystemBars

class ChatMessageAiScreen(
    private val chatId: String,
    private val chatTitle: String,
): Screen {


    @Composable
    override fun Content() {


        val isDark = rememberIsDarkTheme()

        val payload = remember(isDark, chatId, chatTitle) {
            Json.encodeToString(
                ChatAiMessagePayload(
                    type = "parent",
                    token = AppSettings.accessToken,
                    chatId = chatId,
                    chatTitle = chatTitle,
                    theme = if (isDark) "dark" else "light",
                    fontSize = "14",
                    parent = true
                )
            )
        }

        val navigator = LocalNavigator.current

        val systemBars = rememberScreenSystemBars(
            statusBarColor = AppColors.bg.secondary,
            navigationBarColor = AppColors.bg.secondary
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .then(systemBars.modifier)
        ) {

            UniversalJsonWebView(
                url = "https://tikoncha.uz/chat/ai/",
                json = payload,
                onIncomingJson = {},
                modifier = Modifier
                    .fillMaxSize(),
                onBackPressed = {
                    Logger.d("CHATAI", "backpressed")
                    navigator?.pop()
                }
            )

//            Box(
//                modifier = Modifier
//                    .padding(start = ContainerPadding, top = 21.dp, bottom = ContainerPadding)
//            ) {
//                FilledTonalIconButton(
//                    modifier = Modifier
//                        .size(NormalIconButtonSize),
//                    onClick = {
//                        navigator?.pop()
//                    },
//                    colors = IconButtonDefaults.filledTonalIconButtonColors(
//                        containerColor = MaterialTheme.extendedColor.cardColor,
//                        contentColor = MaterialTheme.extendedColor.textColor
//                    ),
//                    shape = RoundedCornerShape(10.dp)
//                ) {
//                    Icon(
//                        painter = painterResource(Res.drawable.arrow_left),
//                        contentDescription = "",
//                        modifier = Modifier
//                            .fillMaxSize()
//                            .padding(NormalIconButtonPadding)
//                    )
//                }
//            }

        }



    }


}

@Serializable
data class ChatAiMessagePayload(
    val type: String,
    val token: String,
    val chatId: String,
    val chatTitle: String,
    val theme: String,
    val fontSize: String,
    val parent: Boolean

)