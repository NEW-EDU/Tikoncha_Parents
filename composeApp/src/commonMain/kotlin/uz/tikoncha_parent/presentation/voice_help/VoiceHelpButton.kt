package uz.tikoncha_parent.presentation.voice_help

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.StartOffset
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.stringResource
import tikoncha_parents.composeapp.generated.resources.Res
import tikoncha_parents.composeapp.generated.resources.voice_listen
import tikoncha_parents.composeapp.generated.resources.voice_stop
import tikoncha_parents.composeapp.generated.resources.voice_unavailable
import uz.tikoncha_parent.ui.expressiveButtonShapes
import uz.tikoncha_parent.ui.rememberHapticSource
import uz.tikoncha_parent.ui.theme.AppColors
import uz.tikoncha_parent.ui.theme.AppTypography

/**
 * "Tinglaş" — ekranni 20–40 soniyalik ovoz bilan tuşuntiradi (keksaroq ota-onalar uçun matndan osonroq).
 * Bosilsa — ijro, yana bosilsa — töxtaydi. Ovoz serverdan olinadi; hali yuklanmagan (404) yoki internet yöq bölsa —
 * tugma ostida qisqa xabar. Ilovada bir payt faqat bitta tuşuntiriş; ekrandan çiqilganda töxtaydi.
 * [compact] — faqat ikonka (48dp doira, pastki orol uçun, [VoiceHelpIconButton]); xabar tugma USTIDA çiqadi.
 * Profilda "Ovozli yordam" öçirilgan bölsa ([VoiceHelpPrefs]) — heç narsa çizilmaydi (joy ham egallamaydi).
 */
@Composable
fun VoiceHelpButton(screen: VoiceScreen, modifier: Modifier = Modifier, compact: Boolean = false) {
    val enabled by VoiceHelpPrefs.enabled.collectAsState()
    if (!enabled) return
    VoiceHelpButtonContent(screen, modifier, compact)
}

/** Ixçam «Tinglaş»: faqat karnay ikonkasi, orol tugmalari bilan bir xil (48dp tonal doira). */
@Composable
fun VoiceHelpIconButton(screen: VoiceScreen, modifier: Modifier = Modifier) {
    VoiceHelpButton(screen, modifier, compact = true)
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun VoiceHelpButtonContent(screen: VoiceScreen, modifier: Modifier, compact: Boolean) {
    val token = remember { Any() }
    val owner by VoiceHelpPlayback.owner.collectAsState()
    val playback by VoiceHelpPlayback.state.collectAsState()
    val state = if (owner === token) playback else VoiceState.Idle
    val active = state == VoiceState.Loading || state == VoiceState.Playing
    var showUnavailable by remember { mutableStateOf(false) }

    DisposableEffect(token) { onDispose { VoiceHelpPlayback.stop(token) } }

    LaunchedEffect(state) {
        if (state == VoiceState.Error) {
            VoiceHelpPlayback.stop(token)
            showUnavailable = true
        }
    }
    LaunchedEffect(showUnavailable) {
        if (showUnavailable) {
            delay(3500)
            showUnavailable = false
        }
    }

    val onClick: () -> Unit = {
        if (active) {
            VoiceHelpPlayback.stop(token)
        } else {
            showUnavailable = false
            VoiceHelpPlayback.play(token, voiceUrl(screen))
        }
    }
    val label = stringResource(if (active) Res.string.voice_stop else Res.string.voice_listen)

    Box(modifier) {
        if (compact) {
            FilledTonalIconButton(
                onClick = onClick,
                interactionSource = rememberHapticSource(),
                modifier = Modifier.size(48.dp).semantics { contentDescription = label },
                shapes = IconButtonDefaults.shapes(),
                colors = IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor = if (active) AppColors.action.primary else AppColors.action.primary.copy(alpha = 0.14f),
                    contentColor = if (active) AppColors.text.onPrimary else AppColors.icon.accentPrimary,
                ),
            ) {
                when (state) {
                    VoiceState.Loading -> CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = LocalContentColor.current,
                    )
                    // Ijro paytida — "Töxtatiş" holati: töla rangli doira, içida ■
                    VoiceState.Playing -> Icon(
                        imageVector = Icons.Rounded.Stop,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                    )
                    else -> Icon(
                        imageVector = Icons.AutoMirrored.Rounded.VolumeUp,
                        contentDescription = null,
                        modifier = Modifier.size(22.dp),
                    )
                }
            }
        } else {
            Button(
                onClick = onClick,
                interactionSource = rememberHapticSource(),
                shapes = expressiveButtonShapes(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (active) AppColors.action.primary else AppColors.action.primary.copy(alpha = 0.14f),
                    contentColor = if (active) AppColors.text.onPrimary else AppColors.text.primary,
                ),
                contentPadding = PaddingValues(start = 14.dp, end = 18.dp),
                // 44dp körinadi; Button özi bosiş maydonini 48dp gaça kengaytiradi
                modifier = Modifier.height(44.dp),
            ) {
                Box(Modifier.size(22.dp), contentAlignment = Alignment.Center) {
                    when (state) {
                        VoiceState.Loading -> CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = LocalContentColor.current,
                        )
                        VoiceState.Playing -> SoundBars(color = LocalContentColor.current)
                        else -> Icon(
                            imageVector = Icons.AutoMirrored.Rounded.VolumeUp,
                            contentDescription = null,
                            tint = AppColors.icon.accentPrimary,
                            modifier = Modifier.size(22.dp),
                        )
                    }
                }
                Spacer(Modifier.width(8.dp))
                Text(
                    text = label,
                    style = AppTypography.titleSmSemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }

        if (showUnavailable && compact) {
            // Pastki orolda: xabar tugma USTIDA (ostida ekran çeti — körinmay qolardi)
            val density = LocalDensity.current
            val provider = remember(density) {
                AboveAnchorPositionProvider(
                    gapPx = with(density) { 8.dp.roundToPx() },
                    marginPx = with(density) { 12.dp.roundToPx() },
                )
            }
            Popup(popupPositionProvider = provider, onDismissRequest = { showUnavailable = false }) {
                VoiceUnavailableToast(onClick = { showUnavailable = false })
            }
        } else if (showUnavailable) {
            // Toast (Info) körinişida, tugmaning özi ostida — ToastProvider har ekranda yöq, şuning uçun Popup
            val below = with(LocalDensity.current) { 52.dp.roundToPx() }
            Popup(
                alignment = Alignment.TopEnd,
                offset = IntOffset(0, below),
                onDismissRequest = { showUnavailable = false },
            ) {
                VoiceUnavailableToast(onClick = { showUnavailable = false })
            }
        }
    }
}

/** "Ovozli tuşuntiriş hozir yöq…" — Toast (Info) körinişida; bosilsa yopiladi. */
@Composable
private fun VoiceUnavailableToast(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .widthIn(max = 300.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(AppColors.bg.secondaryBrand)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Rounded.Info,
            contentDescription = null,
            tint = AppColors.text.onPrimary,
            modifier = Modifier.size(18.dp),
        )
        Spacer(Modifier.width(10.dp))
        Text(
            text = stringResource(Res.string.voice_unavailable),
            style = AppTypography.bodyMdMedium,
            color = AppColors.text.onPrimary,
        )
    }
}

/**
 * Xabarni tugma ustiga qöyadi: gorizontal — tugma markazida, ekran çetidan [marginPx] içkarida;
 * vertikal — tugma tepasidan [gapPx] yuqorida.
 */
private class AboveAnchorPositionProvider(
    private val gapPx: Int,
    private val marginPx: Int,
) : PopupPositionProvider {
    override fun calculatePosition(
        anchorBounds: IntRect,
        windowSize: IntSize,
        layoutDirection: LayoutDirection,
        popupContentSize: IntSize,
    ): IntOffset {
        val maxX = (windowSize.width - popupContentSize.width - marginPx).coerceAtLeast(marginPx)
        val x = (anchorBounds.center.x - popupContentSize.width / 2).coerceIn(marginPx, maxX)
        val y = (anchorBounds.top - popupContentSize.height - gapPx).coerceAtLeast(0)
        return IntOffset(x, y)
    }
}

/** Ijro paytida: uçta ustunça navbat bilan kötarilib-tuşadi ("ijro etilmoqda" pill'ida ham, [VoiceHelpLayer]). */
@Composable
internal fun SoundBars(color: Color, modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "voiceBars")
    val scales = listOf(0, 140, 280).map { offsetMs ->
        transition.animateFloat(
            initialValue = 0.3f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 420, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse,
                initialStartOffset = StartOffset(offsetMs),
            ),
            label = "voiceBar",
        )
    }
    Row(
        modifier = modifier.size(20.dp),
        horizontalArrangement = Arrangement.spacedBy(3.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        scales.forEach { scale ->
            Box(
                Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .graphicsLayer { scaleY = scale.value }
                    .clip(RoundedCornerShape(2.dp))
                    .background(color)
            )
        }
    }
}
