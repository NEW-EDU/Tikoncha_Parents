package uz.tikoncha_parent.presentation.tutorial_clips

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import tikoncha_parents.composeapp.generated.resources.Res
import tikoncha_parents.composeapp.generated.resources.close_remove
import tikoncha_parents.composeapp.generated.resources.yopish
import uz.tikoncha_parent.presentation.task.TaskSegmentedToggle
import uz.tikoncha_parent.ui.Space
import uz.tikoncha_parent.ui.theme.AppColors
import uz.tikoncha_parent.ui.theme.AppTypography

/** Klip kadri: telefon ekrani nisbati (1080×2340). */
private const val CLIP_ASPECT = 1080f / 2340f

/**
 * Qo'llanma klipini toliq ekranda, ovozsiz va takrorlanib körsatadi. Bir nechta klip bölsa — pastda almaştirgiç
 * (masalan "Farzand qöşiş" / "Ulaniş kodi").
 */
@Composable
fun TutorialClipDialog(clips: List<TutorialClip>, onDismiss: () -> Unit) {
    if (clips.isEmpty()) return
    var index by remember { mutableIntStateOf(0) }
    val clip = clips[index.coerceIn(0, clips.lastIndex)]

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .background(AppColors.bg.page)
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(16.dp),
        ) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(clip.title),
                    style = AppTypography.titleMdSemiBold,
                    color = AppColors.text.primary,
                    modifier = Modifier.weight(1f),
                )
                Surface(onClick = onDismiss, shape = CircleShape, color = AppColors.bg.surfaceTertiary, modifier = Modifier.size(44.dp)) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            painter = painterResource(Res.drawable.close_remove),
                            contentDescription = stringResource(Res.string.yopish),
                            tint = AppColors.text.primary,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }
            }
            Space(12.dp)
            Box(
                Modifier
                    .weight(1f)
                    .aspectRatio(CLIP_ASPECT, matchHeightConstraintsFirst = true)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.Black)
            ) {
                key(clip) { LoopingClip(uri = clip.uri(), modifier = Modifier.fillMaxSize()) }
            }
            if (clips.size > 1) {
                Space(12.dp)
                TaskSegmentedToggle(
                    options = clips.map { stringResource(it.title) to null },
                    selectedIndex = index,
                    onOptionSelected = { index = it },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}
