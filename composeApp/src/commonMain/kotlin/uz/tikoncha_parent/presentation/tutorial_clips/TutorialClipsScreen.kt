package uz.tikoncha_parent.presentation.tutorial_clips

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import tikoncha_parents.composeapp.generated.resources.Res
import tikoncha_parents.composeapp.generated.resources.media_play
import tikoncha_parents.composeapp.generated.resources.video_qollanma
import uz.tikoncha_parent.presentation.base.CustomHeader
import uz.tikoncha_parent.presentation.profile.ProfileSectionItem
import uz.tikoncha_parent.ui.SegmentGap
import uz.tikoncha_parent.ui.theme.AppColors
import uz.tikoncha_parent.ui.theme.rememberScreenSystemBars

/** Profil → Yordam → "Video qöllanma": ilovadagi barça qo'llanma kliplari. */
class TutorialClipsScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        var open by remember { mutableStateOf<TutorialClip?>(null) }
        open?.let { TutorialClipDialog(listOf(it)) { open = null } }

        val systemBars = rememberScreenSystemBars(statusBarColor = AppColors.bg.page, navigationBarColor = AppColors.bg.page)
        Column(Modifier.fillMaxSize().then(systemBars.modifier).background(AppColors.bg.page)) {
            CustomHeader(
                title = stringResource(Res.string.video_qollanma),
                showBackButton = true,
                onBackClick = { navigator.pop() },
            )
            Column(
                verticalArrangement = Arrangement.spacedBy(SegmentGap),
                modifier = Modifier.verticalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 8.dp),
            ) {
                val all = TutorialClip.all
                all.forEachIndexed { i, clip ->
                    ProfileSectionItem(
                        segmentIndex = i,
                        segmentCount = all.size,
                        title = stringResource(clip.title),
                        icon = painterResource(Res.drawable.media_play),
                        onItemClick = { open = clip },
                    )
                }
            }
        }
    }
}
