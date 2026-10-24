@file:OptIn(androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class)

package uz.tikoncha_parent.presentation.video_tutorial

import uz.tikoncha_parent.presentation.base.DelayedLoadingIndicator
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.core.annotation.InternalVoyagerApi
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.stringResource
import tikoncha_parents.composeapp.generated.resources.Res
import tikoncha_parents.composeapp.generated.resources.video_qollanma
import tikoncha_parents.composeapp.generated.resources.video_qollanma_error_message
import tikoncha_parents.composeapp.generated.resources.youtube_ochilmoqda
import uz.tikoncha_parent.platform.openUrl
import uz.tikoncha_parent.presentation.base.CustomHeader
import uz.tikoncha_parent.presentation.base.asText
import uz.tikoncha_parent.ui.theme.AppColors
import uz.tikoncha_parent.ui.theme.AppTypography
import uz.tikoncha_parent.ui.theme.TikonchaParentTheme
import uz.tikoncha_parent.ui.theme.rememberScreenSystemBars

data class VideoTutorialYoutubeScreen(val tutorialType: TutorialType) : Screen {

    @OptIn(InternalVoyagerApi::class)
    @Composable
    override fun Content() {
        val tutorialModel = koinScreenModel<VideoTutorialScreenModel>()

        val state by tutorialModel.state.collectAsStateWithLifecycle()
        val navigator = LocalNavigator.currentOrThrow


        val systemBars = rememberScreenSystemBars(
            statusBarColor = AppColors.bg.page,
            navigationBarColor = AppColors.bg.page
        )

        LaunchedEffect(tutorialType){
            tutorialModel.loadVideoTutorial(type = tutorialType)
        }


        when {
            state.isLoading -> {
                Box(Modifier.fillMaxSize().background(AppColors.bg.page), contentAlignment = Alignment.Center) {
                    DelayedLoadingIndicator()
                }
            }
            state.videoUrl.isNotBlank() -> {
                LaunchedEffect(state.videoUrl){
                    delay(1000)
                    navigator.pop()
                    openUrl(state.videoUrl)
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ){
                    val message = stringResource(Res.string.youtube_ochilmoqda)
                    Text(
                        text = message,
                        color = AppColors.text.secondary,
                        style = AppTypography.titleMdRegular,
                        textAlign = TextAlign.Center
                    )
                }

            }
            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .then(systemBars.modifier)
                        .background(AppColors.bg.page)
                ){
                    CustomHeader(
                        showBackButton = true,
                        onBackClick = { navigator.pop() },
                        title = stringResource(Res.string.video_qollanma),
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ){
                        val message = state.error?.asText() ?: stringResource(Res.string.video_qollanma_error_message)
                        Text(
                            text = message,
                            color = AppColors.text.secondary,
                            style = AppTypography.titleMdRegular,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun Pre(){
    TikonchaParentTheme {
        VideoTutorialYoutubeScreen(tutorialType = TutorialType.TIKONCHA)
    }
}