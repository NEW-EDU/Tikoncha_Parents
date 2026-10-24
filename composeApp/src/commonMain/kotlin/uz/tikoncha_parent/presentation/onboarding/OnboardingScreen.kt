package uz.tikoncha_parent.presentation.onboarding

import uz.tikoncha_parent.ui.Haptics
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import tikoncha_parents.composeapp.generated.resources.Res
import tikoncha_parents.composeapp.generated.resources.close_remove
import tikoncha_parents.composeapp.generated.resources.davom_etish
import tikoncha_parents.composeapp.generated.resources.mascot_cheer
import tikoncha_parents.composeapp.generated.resources.mascot_heart
import tikoncha_parents.composeapp.generated.resources.mascot_shield
import tikoncha_parents.composeapp.generated.resources.mascot_think
import tikoncha_parents.composeapp.generated.resources.mascot_timer
import tikoncha_parents.composeapp.generated.resources.mascot_wave
import tikoncha_parents.composeapp.generated.resources.ob_chat_body
import tikoncha_parents.composeapp.generated.resources.ob_chat_title
import tikoncha_parents.composeapp.generated.resources.ob_day_body
import tikoncha_parents.composeapp.generated.resources.ob_day_title
import tikoncha_parents.composeapp.generated.resources.ob_got_it
import tikoncha_parents.composeapp.generated.resources.ob_hello_body
import tikoncha_parents.composeapp.generated.resources.ob_hello_title
import tikoncha_parents.composeapp.generated.resources.ob_map_body
import tikoncha_parents.composeapp.generated.resources.ob_map_title
import tikoncha_parents.composeapp.generated.resources.ob_rules_body
import tikoncha_parents.composeapp.generated.resources.ob_rules_title
import tikoncha_parents.composeapp.generated.resources.ob_skip
import tikoncha_parents.composeapp.generated.resources.ob_start
import tikoncha_parents.composeapp.generated.resources.ob_tasks_body
import tikoncha_parents.composeapp.generated.resources.ob_tasks_title
import uz.tikoncha_parent.ui.theme.SystemBarOverride
import uz.tikoncha_parent.platform.BuildConfig
import uz.tikoncha_parent.presentation.domain.model.LanguageType
import uz.tikoncha_parent.presentation.profile.language.LocalLanguageController
import kotlin.math.roundToInt
import kotlin.math.sin

/**
 * Ota-ona ilovasining tanishtiruvi. Birinchi ishga tushirishda: sahnalar → [NotificationPermissionScreen] → kirish.
 * [tourOnly] = true — "Hoglt nima?" (o'zbek tilidan boshqa tillarda video o'rniga): faqat sahnalar, oxirida yopiladi.
 */
class OnboardingScreen(private val tourOnly: Boolean = false) : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        OnboardingUi(
            tourOnly = tourOnly,
            onFinish = { if (tourOnly) navigator.pop() else navigator.push(NotificationPermissionScreen()) },
            onClose = { navigator.pop() },
        )
    }
}

/** Sahna: tipratikon pozasi, matnlar va pozaning sahnadagi joyi (x, y — sahna maydoniga nisbatan, o'lcham). */
private enum class Scene(
    val pose: DrawableResource,
    val title: StringResource,
    val body: StringResource,
    val ax: Float,
    val ay: Float,
    val mascot: Dp,
    /** Rasmning loyiha balandligi — kichik ekranlarda shunga qarab kichraytiriladi. */
    val designHeight: Dp,
) {
    HELLO(Res.drawable.mascot_wave, Res.string.ob_hello_title, Res.string.ob_hello_body, 0.5f, 0.5f, 0.dp, 300.dp),
    DAY(Res.drawable.mascot_think, Res.string.ob_day_title, Res.string.ob_day_body, 0.80f, 0f, 96.dp, 318.dp),
    RULES(Res.drawable.mascot_timer, Res.string.ob_rules_title, Res.string.ob_rules_body, 0.20f, 0f, 96.dp, 256.dp),
    MAP(Res.drawable.mascot_shield, Res.string.ob_map_title, Res.string.ob_map_body, 0.80f, 0f, 96.dp, 290.dp),
    CHAT(Res.drawable.mascot_heart, Res.string.ob_chat_title, Res.string.ob_chat_body, 0.80f, 0f, 96.dp, 184.dp),
    TASKS(Res.drawable.mascot_cheer, Res.string.ob_tasks_title, Res.string.ob_tasks_body, 0.20f, 0f, 96.dp, 132.dp),
}

private val TopBarHeight = 56.dp
private val BottomHeight = 132.dp
/** Tipratikon turadigan polosa: u kartaning ustki chetiga "o'tirib" turadi, hech narsani yopmaydi. */
private val ActorBand = 84.dp
/** Oyoqlari kartaga qancha kirib turadi. */
private val ActorSink = 14.dp

@Composable
internal fun OnboardingUi(tourOnly: Boolean, onFinish: () -> Unit, onClose: () -> Unit) {
    val scenes = Scene.entries
    // Faqat debug: `-hoglt.page N` — ekran rasmlari uchun N-sahnadan boshlash
    val startPage = remember {
        BuildConfig.debugLaunchArgs.let { a -> a.indexOf("-hoglt.page").takeIf { it >= 0 }?.let { a.getOrNull(it + 1)?.toIntOrNull() } } ?: 0
    }
    val pager = rememberPagerState(initialPage = startPage.coerceIn(0, scenes.lastIndex)) { scenes.size }
    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current
    val pos = pager.currentPage + pager.currentPageOffsetFraction
    val last = scenes.lastIndex

    ForceLightSystemIcons()

    // Har bir yangi sahnada yengil "tik"
    var firstSettle by remember { mutableStateOf(true) }
    LaunchedEffect(pager.settledPage) {
        if (firstSettle) firstSettle = false else Haptics.tick()
    }

    NightBackdrop(glowX = 0.5f + 0.2f * sin(pos * 1.4f)) {
        BoxWithConstraints(Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing)) {
            val width = maxWidth
            val pagerHeight = maxHeight - TopBarHeight - BottomHeight
            val stageHeight = pagerHeight * 0.66f
            val helloMascot = min(stageHeight * 0.66f, 250.dp)

            Column(Modifier.fillMaxSize()) {
                TopBar(
                    tourOnly = tourOnly,
                    langAlpha = (1f - pos).coerceIn(0f, 1f),
                    skipAlpha = (last - pos).coerceIn(0f, 1f),
                    onSkip = onFinish,
                    onClose = onClose,
                )
                HorizontalPager(state = pager, modifier = Modifier.fillMaxWidth().height(pagerHeight)) { page ->
                    val scene = scenes[page]
                    val play = rememberScenePlay(active = pager.settledPage == page, durationMs = if (scene == Scene.HELLO) 1400 else 2200)
                    Column(Modifier.fillMaxSize()) {
                        val band = if (scene == Scene.HELLO) 0.dp else ActorBand
                        val fit = ((stageHeight - band - 8.dp) / scene.designHeight).coerceAtMost(1f)
                        // Ixcham sahnalarda matn kartaga yaqin turadi (bo'sh joy pastga, tugma tomonga o'tadi)
                        val sceneHeight = if (scene == Scene.HELLO) stageHeight
                            else min(stageHeight, band + scene.designHeight * fit + 28.dp)
                        Box(Modifier.fillMaxWidth().height(sceneHeight)) {
                            Box(
                                Modifier
                                    .padding(top = band)
                                    .fillMaxSize()
                                    .wrapContentHeight(if (scene == Scene.HELLO) Alignment.CenterVertically else Alignment.Top, unbounded = true),
                                contentAlignment = Alignment.TopCenter,
                            ) {
                                Box(
                                    Modifier
                                        .requiredSize(width = width - 48.dp, height = scene.designHeight)
                                        .graphicsLayer {
                                            scaleX = fit; scaleY = fit
                                            transformOrigin = TransformOrigin(0.5f, if (scene == Scene.HELLO) 0.5f else 0f)
                                        },
                                    contentAlignment = if (scene == Scene.HELLO) Alignment.Center else Alignment.TopCenter,
                                ) {
                                    SceneArt(scene, play)
                                }
                            }
                        }
                        Column(
                            Modifier.fillMaxWidth().padding(horizontal = 28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Spacer(Modifier.height(8.dp))
                            KineticTitle(stringResource(scene.title), play)
                            Spacer(Modifier.height(10.dp))
                            Text(
                                stringResource(scene.body),
                                style = bodyStyle(),
                                modifier = Modifier.graphicsLayer {
                                    val t = easeOut(stage(play, 0.22f, 0.55f))
                                    alpha = t
                                    translationY = (1f - t) * 10.dp.toPx()
                                },
                            )
                        }
                    }
                }
                Column(
                    Modifier.fillMaxWidth().height(BottomHeight).padding(horizontal = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    PageDots(scenes.size, pos)
                    Spacer(Modifier.height(22.dp))
                    val isLast = pager.currentPage == last
                    CtaButton(
                        text = stringResource(
                            when {
                                !isLast -> Res.string.davom_etish
                                tourOnly -> Res.string.ob_got_it
                                else -> Res.string.ob_start
                            }
                        ),
                        onClick = {
                            Haptics.success()
                            if (isLast) onFinish()
                            else scope.launch { pager.animateScrollToPage(pager.currentPage + 1, animationSpec = tween(560, easing = FastOutSlowInEasing)) }
                        },
                    )
                }
            }

            // Tipratikon — sahifalar bilan birga sirpanmaydi, sahnadan sahnaga "yurib" o'tadi
            val from = scenes[pos.toInt().coerceIn(0, last)]
            val to = scenes[(pos.toInt() + 1).coerceIn(0, last)]
            val f = pos - pos.toInt()
            fun sizeOf(s: Scene) = if (s == Scene.HELLO) helloMascot else s.mascot
            val size = sizeOf(from) + (sizeOf(to) - sizeOf(from)) * f
            val ax = from.ax + (to.ax - from.ax) * f
            fun centerYOf(s: Scene) = if (s == Scene.HELLO) stageHeight * s.ay else ActorBand + ActorSink - s.mascot / 2
            val centerY = centerYOf(from) + (centerYOf(to) - centerYOf(from)) * f
            // O'tish paytida biroz sakraydi
            val hop = sin(f * kotlin.math.PI).toFloat() * 26f
            MascotActor(
                pose = scenes[pos.roundToInt().coerceIn(0, last)].pose,
                size = size,
                modifier = Modifier.offset(
                    x = width * ax - size / 2,
                    y = TopBarHeight + centerY - size / 2 - hop.dp,
                ),
            )
        }
    }
}

@Composable
private fun SceneArt(scene: Scene, play: Float) {
    val full = Modifier.fillMaxWidth()
    when (scene) {
        Scene.HELLO -> HelloRings(Modifier.fillMaxSize())
        Scene.DAY -> ScreenTimeIllustration(play, full)
        Scene.RULES -> RulesIllustration(play, full)
        Scene.MAP -> MapIllustration(play, Modifier.fillMaxSize())
        Scene.CHAT -> ChatIllustration(play, full)
        Scene.TASKS -> TasksIllustration(play, full)
    }
}

@Composable
private fun TopBar(tourOnly: Boolean, langAlpha: Float, skipAlpha: Float, onSkip: () -> Unit, onClose: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().height(TopBarHeight).padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (tourOnly) {
            Surface(onClick = onClose, shape = CircleShape, color = Night.card, modifier = Modifier.size(40.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(painterResource(Res.drawable.close_remove), contentDescription = null, tint = Night.ink, modifier = Modifier.size(18.dp))
                }
            }
        } else if (langAlpha > 0.01f) {
            LanguagePill(Modifier.graphicsLayer { alpha = langAlpha })
        }
        Spacer(Modifier.weight(1f))
        if (skipAlpha > 0.01f) {
            Surface(
                onClick = onSkip,
                enabled = skipAlpha > 0.5f,
                shape = CircleShape,
                color = Color.Transparent,
                modifier = Modifier.graphicsLayer { alpha = skipAlpha },
            ) {
                Text(
                    stringResource(Res.string.ob_skip),
                    style = labelStyle(15, FontWeight.Bold, Night.inkDim),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                )
            }
        }
    }
}

/** Birinchi sahnadagi til tanlash — ilova tili darhol almashadi. */
@Composable
private fun LanguagePill(modifier: Modifier = Modifier) {
    val controller = LocalLanguageController.current
    val current by controller.current.collectAsState()
    var open by remember { mutableStateOf(false) }
    Box(modifier) {
        Surface(onClick = { open = true }, shape = CircleShape, color = Night.card) {
            Row(Modifier.padding(horizontal = 14.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(current.languageName, style = labelStyle(14, FontWeight.Bold))
                Spacer(Modifier.size(6.dp))
                Text("▾", style = labelStyle(12, FontWeight.Bold, Night.inkDim))
            }
        }
        DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
            LanguageType.entries.forEach { lang ->
                DropdownMenuItem(
                    text = { Text(lang.languageName, style = labelStyle(15, if (lang == current) FontWeight.ExtraBold else FontWeight.Medium, Color.Unspecified)) },
                    onClick = {
                        open = false
                        controller.select(lang)
                    },
                )
            }
        }
    }
}

/** Qorong'i tanishtiruvda status bar belgilari doim oq; ekran yopilganda mavzuga qaytadi. */
@Composable
internal fun ForceLightSystemIcons() {
    DisposableEffect(Unit) {
        SystemBarOverride.lightIconScreens++
        onDispose { SystemBarOverride.lightIconScreens-- }
    }
}
