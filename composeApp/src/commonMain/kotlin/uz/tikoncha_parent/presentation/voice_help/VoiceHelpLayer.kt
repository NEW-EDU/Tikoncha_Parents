package uz.tikoncha_parent.presentation.voice_help

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Smartphone
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.height
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import uz.tikoncha_parent.presentation.base.island.islandReservedFromBottom
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import tikoncha_parents.composeapp.generated.resources.Res
import tikoncha_parents.composeapp.generated.resources.voice_listen
import tikoncha_parents.composeapp.generated.resources.voice_playing
import tikoncha_parents.composeapp.generated.resources.voice_shake_hint
import tikoncha_parents.composeapp.generated.resources.voice_sheet_title
import tikoncha_parents.composeapp.generated.resources.voice_stop
import tikoncha_parents.composeapp.generated.resources.voice_unavailable
import tikoncha_parents.composeapp.generated.resources.voice_watch_video
import uz.tikoncha_parent.presentation.tutorial_clips.TutorialClip
import uz.tikoncha_parent.presentation.tutorial_clips.TutorialClipDialog
import uz.tikoncha_parent.ui.Haptics
import uz.tikoncha_parent.ui.expressiveButtonShapes
import uz.tikoncha_parent.ui.rememberHapticSource
import uz.tikoncha_parent.ui.theme.AppColors
import uz.tikoncha_parent.ui.theme.AppTypography

/** «Bu ekran haqida» varag'idagi «Video köriş» kliplari; böş — tugma körsatilmaydi. */
val VoiceScreen.helpClips: List<TutorialClip>
    get() = when (this) {
        // CHILD_DO_TASK ro'yxatda yöq (inList = false) — faqat şu yerdan oçiladi, şuning uçun qoldi
        VoiceScreen.TASKS -> listOf(TutorialClip.GIVE_TASK, TutorialClip.CHILD_DO_TASK, TutorialClip.CHECK_TASK)
        VoiceScreen.TASK_CREATE -> listOf(TutorialClip.GIVE_TASK)
        VoiceScreen.ADD_CHILD -> listOf(
            TutorialClip.CHILD_ADD_PARENT,
            TutorialClip.CHILD_ADD_PARENT_IOS,
            TutorialClip.ADD_CHILD,
            TutorialClip.ADD_CHILD_CODE,
        )
        VoiceScreen.PROTECTION -> listOf(TutorialClip.PROTECTION)
        VoiceScreen.RULES -> listOf(TutorialClip.LIMITS)
        VoiceScreen.STATS -> listOf(TutorialClip.SCREEN_TIME)
        VoiceScreen.CHAT -> listOf(TutorialClip.CHAT)
        VoiceScreen.HOME -> listOf(TutorialClip.TASKS)
        VoiceScreen.MAP -> emptyList()
    }

private const val AUTO_PLAY_DELAY_MS = 600L
/** Avto-ijrodan oldingi sanoq (pastda çapda, orol ustida) — bosilsa ijro bekor. */
private const val COUNTDOWN_MS = 3_000L
/** Bir xil holat xabari şu muddatda qayta aytilmaydi (ekranga kirib-çiqib tursa ham). */
private const val STATUS_REPEAT_MS = 10 * 60_000L

/** Oxirgi aytilgan holat xabari (audio manzili) va vaqti — ilova böyiça. */
private object StatusMemory {
    var lastUrl: String? = null
    var lastScreen: VoiceScreen? = null
    var lastAt: kotlin.time.TimeSource.Monotonic.ValueTimeMark? = null
    fun recentlySaid(url: String): Boolean {
        val at = lastAt ?: return false
        return url == lastUrl && at.elapsedNow().inWholeMilliseconds < STATUS_REPEAT_MS
    }
    /** Bu ekranda holat yaqinda aytildi — serverga sörov ham, sanoq ham kerak emas (owner, 2026-11-18). */
    fun recentlyOn(screen: VoiceScreen): Boolean {
        val at = lastAt ?: return false
        return screen == lastScreen && at.elapsedNow().inWholeMilliseconds < STATUS_REPEAT_MS
    }
    fun said(url: String, screen: VoiceScreen) {
        lastUrl = url
        lastScreen = screen
        lastAt = kotlin.time.TimeSource.Monotonic.markNow()
    }
}
private const val SHAKE_HINT_MS = 4_000L
private const val UNAVAILABLE_MS = 3_500L

private enum class Notice { LOADING, PLAYING, HINT, UNAVAILABLE }

/** Bitta ijro: avto (birinçi kiriş) yoki varaqdagi «Tinglaş». [started] — ovoz haqiqatan boşlandi. */
private class HelpRun(val screen: VoiceScreen, val auto: Boolean, val status: Boolean = false, val url: String? = null) {
    var started = false
    /** Ijro paytida "Bu ekran haqida" oçildi — foydalanuvçi yordamni biladi, maslahat kerak emas. */
    var skipHint = false
}

/**
 * Ekran yordami (navigatsiya qatlamida, har ekranga bittadan — [uz.tikoncha_parent.presentation.base.island.ScreenWithIsland]):
 *  • birinçi kirişda (har [VoiceScreen] uçun bir marta) tuşuntiriş özi boşlanadi, tepada "ijro etilmoqda" + «Töxtatiş»;
 *  • tugagaç (yoki töxtatilgaç) ~4 s "silkiting" maslahati — jami 3 ta ekranda;
 *  • telefon silkitilsa (yoki orol sarlavhasi uzoq bosilsa) — «Bu ekran haqida»: «Tinglaş» va «Video köriş».
 * Faqat Voyager stack tepasidagi ([isTop]) ekran ijro etadi va silkitişni tinglaydi. Profilda "Ovozli yordam"
 * öçiq bölsa — avto-ijro, maslahat va silkitiş yöq (uzoq bosiş — ataylab qilinadigan amal, işlayveradi).
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun BoxScope.VoiceHelpLayer(
    screen: VoiceScreen?,
    isTop: Boolean,
    /** Keyingi kirişlarda — holat xabari (audio manzili); null — faqat birinçi kirişdagi tuşuntiriş. */
    status: (suspend () -> String?)? = null,
    sheetOpen: Boolean,
    onSheetOpenChange: (Boolean) -> Unit,
) {
    val enabled by VoiceHelpPrefs.enabled.collectAsState()
    val token = remember { Any() }
    val owner by VoiceHelpPlayback.owner.collectAsState()
    val playback by VoiceHelpPlayback.state.collectAsState()
    val state = if (owner === token) playback else VoiceState.Idle
    var run by remember { mutableStateOf<HelpRun?>(null) }
    var hint by remember { mutableStateOf(false) }
    var unavailable by remember { mutableStateOf(false) }
    var clipsOpen by remember { mutableStateOf(false) }
    val topNow by rememberUpdatedState(isTop)
    val enabledNow by rememberUpdatedState(enabled)
    val sheetOpenNow by rememberUpdatedState(sheetOpen)

    val play: (VoiceScreen, Boolean) -> Unit = { target, auto ->
        hint = false
        unavailable = false
        run = HelpRun(target, auto)
        VoiceHelpPlayback.play(token, voiceUrl(target))
    }
    val statusNow by rememberUpdatedState(status)
    val scope = rememberCoroutineScope()
    // Avto-ijro oldidan 3 s sanoq: ota-ona bosib bekor qila oladi (ovoz kutilmaganda boşlanib qolmasin)
    var counting by remember { mutableStateOf(false) }
    val countdown = remember { Animatable(1f) }
    var cancelled by remember { mutableStateOf(false) }

    // Ekrandan çiqilsa (yoki ustiga boşqasi oçilsa) — ovoz töxtaydi
    DisposableEffect(token) { onDispose { VoiceHelpPlayback.stop(token) } }
    LaunchedEffect(isTop) {
        if (!isTop) {
            VoiceHelpPlayback.stop(token)
            hint = false
            onSheetOpenChange(false)
        }
    }
    LaunchedEffect(enabled) { if (!enabled) hint = false }

    // Birinçi kiriş — ekran tuşuntirişi; keyingilari — holat xabari (bor bölsa). Ekran körinib bölgaç 3 s sanoq
    // (pastda çapda), keyin özi ijro etiladi; sanoq paytida bosilsa — bekor. Holat audiosi sanoq paytida tayyorlanadi.
    LaunchedEffect(screen, isTop, enabled) {
        if (screen == null || !isTop || !enabled) return@LaunchedEffect
        val explainer = VoiceHelpPrefs.shouldAutoPlay(screen)
        val source = if (explainer) null else statusNow ?: return@LaunchedEffect
        // Holat 10 daqiqa içida aytilgan — serverga sörov yuborilmaydi, sanoq ham körsatilmaydi
        if (!explainer && StatusMemory.recentlyOn(screen)) return@LaunchedEffect
        val statusUrl = source?.let { src -> async { runCatching { src() }.getOrNull() } }
        delay(AUTO_PLAY_DELAY_MS)
        if (run != null || sheetOpenNow) return@LaunchedEffect
        cancelled = false
        counting = true
        countdown.snapTo(1f)
        try {
            countdown.animateTo(0f, tween(COUNTDOWN_MS.toInt(), easing = LinearEasing))
        } finally {
            counting = false
        }
        if (cancelled || run != null || sheetOpenNow || !topNow || !enabledNow) return@LaunchedEffect
        if (explainer) {
            play(screen, true)
        } else {
            val url = statusUrl?.await() ?: return@LaunchedEffect
            if (StatusMemory.recentlySaid(url)) return@LaunchedEffect
            hint = false
            unavailable = false
            run = HelpRun(screen, auto = true, status = true, url = url)
            VoiceHelpPlayback.play(token, url)
        }
    }

    LaunchedEffect(state) {
        val r = run ?: return@LaunchedEffect
        when (state) {
            VoiceState.Loading -> Unit
            VoiceState.Playing -> if (!r.started) {
                r.started = true
                if (r.auto && !r.status) VoiceHelpPrefs.markSeen(r.screen)
            }
            // Internet yöq, 404, 20 s javob yöq: avto — jim (keyingi işga tuşirişda yana), qöl bilan — xabar
            VoiceState.Error -> {
                run = null
                VoiceHelpPlayback.stop(token)
                if (r.status) Unit
                else if (r.auto) VoiceHelpPrefs.markAutoPlayFailed(r.screen) else unavailable = true
            }
            // Tugadi yoki töxtatildi
            VoiceState.Idle -> {
                run = null
                if (r.status && r.started) r.url?.let { StatusMemory.said(it, r.screen) }
                val showHint = r.auto && !r.status && r.started && !r.skipHint && topNow && enabledNow && !sheetOpenNow
                if (showHint && VoiceHelpPrefs.takeShakeHint()) hint = true
            }
        }
    }
    LaunchedEffect(hint) {
        if (hint) {
            delay(SHAKE_HINT_MS)
            hint = false
        }
    }
    LaunchedEffect(unavailable) {
        if (unavailable) {
            delay(UNAVAILABLE_MS)
            unavailable = false
        }
    }
    LaunchedEffect(sheetOpen) {
        if (sheetOpen) {
            hint = false
            run?.skipHint = true
        }
    }

    // Silkitiş — faqat körinib turgan ekranda, varaq/klip oçiq bölmaganda
    ShakeDetector(
        enabled = enabled && isTop && screen != null && !sheetOpen && !clipsOpen,
        onShake = {
            Haptics.tick()
            VoiceHelpPrefs.markShakeUsed()
            onSheetOpenChange(true)
        },
    )

    val current = run
    // Ijro va sanoq — pastda çapda, orol ustidagi kiçkina tugmada; tepada faqat maslahat va "hozir yöq"
    val playingNow = current != null && state == VoiceState.Playing
    val loadingNow = current != null && state == VoiceState.Loading && !current.auto
    VoiceChip(
        counting = counting,
        progress = { countdown.value },
        playing = playingNow,
        loading = loadingNow,
        onStop = {
            if (counting) {
                cancelled = true
                scope.launch { countdown.stop() }
            } else {
                VoiceHelpPlayback.stop(token)
            }
        },
    )
    val notice = when {
        unavailable -> Notice.UNAVAILABLE
        hint -> Notice.HINT
        else -> null
    }
    TopNotice(
        notice = notice,
        onStop = { VoiceHelpPlayback.stop(token) },
        onDismiss = {
            hint = false
            unavailable = false
        },
        // Bosh sahifada tepadagi profil va yordam tugmalari ochiq qolsin — pill ularning ostida
        modifier = Modifier.align(Alignment.TopCenter).padding(top = if (screen == VoiceScreen.HOME) 64.dp else 0.dp),
    )

    if (sheetOpen && screen != null) {
        val active = state == VoiceState.Loading || state == VoiceState.Playing
        ScreenHelpSheet(
            playing = active,
            hasClips = screen.helpClips.isNotEmpty(),
            onListen = { if (active) VoiceHelpPlayback.stop(token) else play(screen, false) },
            onVideo = {
                VoiceHelpPlayback.stop(token)
                clipsOpen = true
            },
            onDismiss = { onSheetOpenChange(false) },
        )
    }
    if (clipsOpen && screen != null) {
        TutorialClipDialog(screen.helpClips) { clipsOpen = false }
    }
}

/**
 * Pastda çapda, orol ustida: avto-ijrodan oldin 3 s sanoq (halqa + raqam), ijro paytida tovuş çiziqlari.
 * Matnsiz; butun tugma bosiladi — sanoqda bekor qiladi, ijroda töxtatadi.
 */
@Composable
private fun BoxScope.VoiceChip(
    counting: Boolean,
    progress: () -> Float,
    playing: Boolean,
    loading: Boolean,
    onStop: () -> Unit,
) {
    val stopLabel = stringResource(Res.string.voice_stop)
    AnimatedVisibility(
        visible = counting || playing || loading,
        modifier = Modifier
            .align(Alignment.BottomStart)
            .padding(start = 16.dp, bottom = islandReservedFromBottom() + 2.dp),
        enter = fadeIn() + scaleIn(initialScale = 0.8f),
        exit = fadeOut() + scaleOut(targetScale = 0.8f),
    ) {
        Surface(
            onClick = onStop,
            interactionSource = rememberHapticSource(),
            shape = CircleShape,
            color = AppColors.bg.surface,
            border = BorderStroke(1.dp, AppColors.border.secondarySubtle),
            modifier = Modifier.semantics { contentDescription = stopLabel },
        ) {
            Row(
                modifier = Modifier.height(48.dp).padding(start = 6.dp, end = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(Modifier.size(36.dp), contentAlignment = Alignment.Center) {
                    when {
                        counting -> {
                            val p = progress()
                            CircularProgressIndicator(
                                progress = { p },
                                modifier = Modifier.size(32.dp),
                                strokeWidth = 3.dp,
                                color = AppColors.icon.accentPrimary,
                                trackColor = AppColors.border.secondarySubtle,
                            )
                            Text(
                                text = "${kotlin.math.ceil(p * (COUNTDOWN_MS / 1000f)).toInt().coerceAtLeast(1)}",
                                style = AppTypography.titleSmSemiBold,
                                color = AppColors.text.primary,
                            )
                        }
                        loading -> CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            strokeWidth = 2.dp,
                            color = AppColors.icon.accentPrimary,
                        )
                        else -> SoundBars(color = AppColors.icon.accentPrimary)
                    }
                }
                Spacer(Modifier.width(6.dp))
                Icon(
                    imageVector = Icons.Rounded.Stop,
                    contentDescription = null,
                    tint = AppColors.icon.secondary,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
    }
}

/** Tepada, status bar ostida: "ijro etilmoqda", "silkiting" yoki "hozir yöq" — bittadan, yumşoq almaşadi. */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun TopNotice(
    notice: Notice?,
    onStop: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier,
) {
    val motion = MaterialTheme.motionScheme
    AnimatedContent(
        targetState = notice,
        // Yuklaniş → ijro: öşa pill, faqat ikonka almaşadi
        contentKey = { if (it == Notice.LOADING) Notice.PLAYING else it },
        transitionSpec = {
            (fadeIn(motion.defaultEffectsSpec()) + slideInVertically(motion.defaultSpatialSpec()) { -it / 2 }) togetherWith
                (fadeOut(motion.fastEffectsSpec()) + slideOutVertically(motion.fastSpatialSpec()) { -it / 2 }) using
                SizeTransform(clip = false)
        },
        modifier = modifier
            .statusBarsPadding()
            .padding(start = 16.dp, end = 16.dp, top = 8.dp),
        label = "voiceHelpNotice",
    ) { n ->
        when (n) {
            Notice.LOADING, Notice.PLAYING -> PlayingPill(loading = n == Notice.LOADING, onStop = onStop)
            Notice.HINT -> ShakeHintPill(onClick = onDismiss)
            Notice.UNAVAILABLE -> NoticePill(onClick = onDismiss, endPadding = 18.dp) {
                Icon(
                    imageVector = Icons.Rounded.Info,
                    contentDescription = null,
                    tint = AppColors.icon.accentPrimary,
                    modifier = Modifier.size(22.dp),
                )
                Spacer(Modifier.width(10.dp))
                NoticeText(stringResource(Res.string.voice_unavailable))
            }
            null -> Unit
        }
    }
}

/** Orol bilan bir xil: sirt rangi, ingiçka çegara, soyasiz. Katta matnda ikki qatorga öradi. */
@Composable
private fun NoticePill(
    onClick: (() -> Unit)? = null,
    endPadding: Dp = 6.dp,
    content: @Composable RowScope.() -> Unit,
) {
    val shape = RoundedCornerShape(28.dp)
    Row(
        modifier = Modifier
            .widthIn(max = 480.dp)
            .heightIn(min = 52.dp)
            .clip(shape)
            .background(AppColors.bg.surface)
            .border(1.dp, AppColors.border.secondarySubtle, shape)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(start = 16.dp, end = endPadding, top = 6.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        content = content,
    )
}

@Composable
private fun RowScope.NoticeText(text: String) {
    Text(
        text = text,
        style = AppTypography.bodyMdMedium,
        color = AppColors.text.primary,
        maxLines = 3,
        modifier = Modifier.weight(1f, fill = false),
    )
}

/** «🔊 Ovozli tuşuntiriş ijro etilmoqda   [■ Töxtatiş]» — telefon ovozi öçiq bölsa ham nima bölayotgani körinsin. */
@Composable
private fun PlayingPill(loading: Boolean, onStop: () -> Unit) {
    NoticePill {
        Box(Modifier.size(22.dp), contentAlignment = Alignment.Center) {
            if (loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.dp,
                    color = AppColors.icon.accentPrimary,
                )
            } else {
                SoundBars(color = AppColors.icon.accentPrimary)
            }
        }
        Spacer(Modifier.width(10.dp))
        NoticeText(stringResource(Res.string.voice_playing))
        Spacer(Modifier.width(8.dp))
        Surface(
            onClick = onStop,
            interactionSource = rememberHapticSource(),
            shape = CircleShape,
            color = AppColors.action.primary.copy(alpha = 0.14f),
            contentColor = AppColors.text.primary,
        ) {
            Row(
                modifier = Modifier.heightIn(min = 40.dp).padding(start = 12.dp, end = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Rounded.Stop,
                    contentDescription = null,
                    tint = AppColors.icon.accentPrimary,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = stringResource(Res.string.voice_stop),
                    style = AppTypography.titleSmSemiBold,
                    maxLines = 1,
                )
            }
        }
    }
}

/** Telefon ikonkasi çapga-öngga silkinadi (qisqa "silkiş", keyin tanaffus) + "silkiting" matni. */
@Composable
private fun ShakeHintPill(onClick: () -> Unit) {
    val transition = rememberInfiniteTransition(label = "shakeHint")
    val angle by transition.animateFloat(
        initialValue = 0f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = keyframes<Float> {
                durationMillis = 1_400
                0f at 0
                -16f at 90
                16f at 230
                -14f at 370
                12f at 510
                0f at 650
                0f at 1_400
            },
            repeatMode = RepeatMode.Restart,
        ),
        label = "shakeHintAngle",
    )
    NoticePill(onClick = onClick, endPadding = 18.dp) {
        Icon(
            imageVector = Icons.Rounded.Smartphone,
            contentDescription = null,
            tint = AppColors.icon.accentPrimary,
            modifier = Modifier
                .size(24.dp)
                .graphicsLayer {
                    rotationZ = angle
                    translationX = angle / 16f * 3.dp.toPx()
                },
        )
        Spacer(Modifier.width(10.dp))
        NoticeText(stringResource(Res.string.voice_shake_hint))
    }
}

/** «Bu ekran haqida»: ikki katta tugma — «Tinglaş» (ijro paytida «Töxtatiş») va «Video köriş» (klip bölsa). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ScreenHelpSheet(
    playing: Boolean,
    hasClips: Boolean,
    onListen: () -> Unit,
    onVideo: () -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    var acted by remember { mutableStateOf(false) }
    // Amal darhol (ovoz kutib turmasin), varaq orqasidan yopiladi; ikkinçi bosiş e'tiborsiz
    val actThenClose: (() -> Unit) -> Unit = { action ->
        if (!acted) {
            acted = true
            action()
            scope.launch { sheetState.hide() }.invokeOnCompletion { onDismiss() }
        }
    }
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = AppColors.bg.elevated,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(start = 24.dp, end = 24.dp, bottom = 20.dp),
        ) {
            Text(
                text = stringResource(Res.string.voice_sheet_title),
                style = AppTypography.headlineSmSemiBold,
                color = AppColors.text.primary,
            )
            Spacer(Modifier.height(20.dp))
            HelpSheetButton(
                text = stringResource(if (playing) Res.string.voice_stop else Res.string.voice_listen),
                icon = if (playing) Icons.Rounded.Stop else Icons.AutoMirrored.Rounded.VolumeUp,
                primary = true,
                onClick = { actThenClose(onListen) },
            )
            if (hasClips) {
                Spacer(Modifier.height(12.dp))
                HelpSheetButton(
                    text = stringResource(Res.string.voice_watch_video),
                    icon = Icons.Rounded.PlayArrow,
                    primary = false,
                    onClick = { actThenClose(onVideo) },
                )
            }
        }
    }
}

/** Katta tugma: kamida 64dp, katta matnda balandlaşadi (matn öraladi). */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun HelpSheetButton(text: String, icon: ImageVector, primary: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        interactionSource = rememberHapticSource(),
        shapes = expressiveButtonShapes(),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (primary) AppColors.button.primary else AppColors.action.primary.copy(alpha = 0.14f),
            contentColor = if (primary) AppColors.text.inverse else AppColors.text.primary,
        ),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 14.dp),
        modifier = Modifier.fillMaxWidth().heightIn(min = 64.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (primary) AppColors.text.inverse else AppColors.icon.accentPrimary,
            modifier = Modifier.size(26.dp),
        )
        Spacer(Modifier.width(12.dp))
        Text(
            text = text,
            style = AppTypography.titleMdSemiBold,
            textAlign = TextAlign.Center,
        )
    }
}
