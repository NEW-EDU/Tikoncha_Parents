@file:OptIn(ExperimentalTime::class)

package uz.tikoncha_parent.presentation.base

import uz.tikoncha_parent.ui.rememberHapticSource
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

fun Modifier.singleClick(
    delay: Long = 500L,
    onClick: () -> Unit
): Modifier = composed {
    var lastClickTime by remember { mutableLongStateOf(0L) }

    this.clickable(
        indication = null,
        interactionSource = rememberHapticSource()
    ) {
        val now = Clock.System.now().toEpochMilliseconds()
        if (now - lastClickTime >= delay) {
            lastClickTime = now
            onClick()
        }
    }
}