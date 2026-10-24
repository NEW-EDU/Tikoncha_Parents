package uz.tikoncha_parent.presentation.notification

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.Navigator
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel
import uz.tikoncha_parent.presentation.base.CustomText
import uz.tikoncha_parent.data.mapper.toUi
import uz.tikoncha_parent.presentation.base.CustomHeader
import uz.tikoncha_parent.presentation.domain.model.LanguageType
import uz.tikoncha_parent.presentation.profile.language.LanguagePrefs
import uz.tikoncha_parent.ui.ContainerPadding
import uz.tikoncha_parent.ui.NormalLargeTextSize
import uz.tikoncha_parent.ui.SmallTextSize
import uz.tikoncha_parent.ui.theme.AppColors
import uz.tikoncha_parent.ui.theme.TikonchaParentTheme
import uz.tikoncha_parent.ui.theme.extendedColor
import uz.tikoncha_parent.ui.theme.rememberScreenSystemBars

class NotificationVerifyScreen(
    private val newsId: Long
) : Screen {

    @Composable
    override fun Content() {
        val viewModel = koinScreenModel<NotificationViewModel>()
        val uiState by viewModel.state.collectAsStateWithLifecycle()

        LaunchedEffect(newsId) {
            viewModel.load()
        }

        NotificationVerifyUi(
            newsId = newsId,
            uiState = uiState
        )
    }
}
@Composable
fun NotificationVerifyUi(
    newsId: Long,
    uiState: NotificationUiState
) {

    val navigator: Navigator? = LocalNavigator.current




    val language = remember {
        if (LanguagePrefs.loadOrDefault().isUzbekFamily) "uz" else "ru"  // yangiliklar faqat uz/ru
    }

    val ui = remember(uiState.items, newsId, language) {
        uiState.items.firstOrNull { it.id == newsId }?.toUi(language)
    }

    val systemBars = rememberScreenSystemBars(
        statusBarColor = AppColors.bg.secondary,
        navigationBarColor = AppColors.bg.secondary
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .then(systemBars.modifier)
            .background(AppColors.bg.secondary)
    ) {
        CustomHeader(
            title = ui?.title?:"",
            onBackClick = {
                navigator?.pop()
            },
            showBackButton = true
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(ContainerPadding)
        ) {
            CustomText(
                text = ui?.message?:"",
                fontSize = NormalLargeTextSize,
                fontWeight = FontWeight.W500
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                CustomText(
                    text = formatTimeOrDayNumber(ui?.createdAt ?: 0L),
                    fontSize = SmallTextSize
                )
            }
        }
    }
}

@Preview
@Composable
private fun Preview() {
    TikonchaParentTheme {
        NotificationVerifyScreen(
            newsId = 1
        )
    }
}