package uz.tikoncha_parent.presentation.base

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LoadingIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import kotlin.time.Clock

/**
 * Yuklanish ko'rsatkichi (shimmer, loader) "miltillamasligi" uchun: tez javoblarda (< [delayMillis])
 * umuman ko'rinmaydi; ko'ringan bo'lsa — kamida [minShowMillis] turadi (aks holda bir lahzalik chaqnash).
 * Android'dagi ContentLoadingProgressBar mantig'i.
 */
@Composable
fun rememberDelayedLoading(loading: Boolean, delayMillis: Long = 300L, minShowMillis: Long = 450L): Boolean {
    var show by remember { mutableStateOf(false) }
    var shownAt by remember { mutableStateOf(0L) }
    LaunchedEffect(loading) {
        if (loading) {
            delay(delayMillis)
            shownAt = Clock.System.now().toEpochMilliseconds()
            show = true
        } else if (show) {
            val elapsed = Clock.System.now().toEpochMilliseconds() - shownAt
            if (elapsed < minShowMillis) delay(minShowMillis - elapsed)
            show = false
        }
    }
    return show
}

/**
 * Sahifa yuklanish indikatori — faqat yuklash [delayMillis] dan uzoq cho'zilsa ko'rinadi (tez javobda
 * bir lahzalik "chaqnash" bo'lmaydi), keyin silliq paydo bo'ladi.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun DelayedLoadingIndicator(
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    delayMillis: Long = 300L,
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(delayMillis)
        visible = true
    }
    AnimatedVisibility(visible = visible, modifier = modifier, enter = fadeIn(tween(180)), exit = fadeOut(tween(0))) {
        if (color == Color.Unspecified) LoadingIndicator() else LoadingIndicator(color = color)
    }
}
