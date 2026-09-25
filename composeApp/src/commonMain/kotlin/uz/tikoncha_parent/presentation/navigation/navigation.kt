package uz.tikoncha_parent.presentation.navigation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.SaveableStateHolder
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.core.stack.StackEvent
import cafe.adriel.voyager.navigator.Navigator
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

expect val isSwipeBackEnabled: Boolean

private const val PUSH_DURATION = 320
private const val POP_DURATION = 280
private const val REPLACE_DURATION = 220

@Composable
fun SwipeBackContent(navigator: Navigator) {
    // Ekranlarning rememberSaveable holati (scroll va h.k.) boshqa ekranga o'tib qaytganda saqlansin.
    // navigator.saveableState() ishlatilmaydi — u Android'da har ekranga alohida
    // Lifecycle/ViewModelStore berib, koinViewModel'lar umrini o'zgartiradi.
    val stateHolder = rememberSaveableStateHolder()

    // AnimatedContent hozir chizib turgan ekranlar kaliti — swipe'da bir kalit ikki joyda ishlatilmasin.
    val animatedKeys = remember { mutableSetOf<String>() }

    // Ekran kaliti klass nomi: stack'dan chiqqan ekran holati o'chirilmasa,
    // o'sha ekran keyinroq yangidan ochilganda eski holat tiklanib qoladi.
    val stackKeys = navigator.items.map { it.key }.toSet()
    val savedKeys = remember { mutableSetOf<String>() }
    SideEffect {
        if (savedKeys != stackKeys) {
            (savedKeys - stackKeys).forEach { stateHolder.removeState(it) }
            savedKeys.clear()
            savedKeys += stackKeys
        }
    }

    // Swipe ishlamasa ham push/pop animatsiya bo'lishi kerak
    if (!isSwipeBackEnabled || !navigator.canPop) {
        AnimatedScreenContent(
            navigator = navigator,
            skipAnimation = false,
            stateHolder = stateHolder,
            animatedKeys = animatedKeys,
        )
        return
    }

    val scope = rememberCoroutineScope()
    val offsetX = remember { Animatable(0f) }
    var screenWidth by remember { mutableStateOf(1f) }

    var isSwiping by remember { mutableStateOf(false) }
    var gestureStarted by remember { mutableStateOf(false) }
    var skipNextAnim by remember { mutableStateOf(false) }

    LaunchedEffect(navigator.lastItem.key) {
        if (skipNextAnim) {
            withFrameNanos { }   // bir frame
            skipNextAnim = false
        }
    }

    val previousScreen = remember(navigator.items) {
        if (navigator.items.size >= 2) navigator.items[navigator.items.size - 2] else null
    }

    val progress = if (screenWidth > 0f) (offsetX.value / screenWidth).coerceIn(0f, 1f) else 0f

    Box(
        modifier = Modifier
            .fillMaxSize()
            .onSizeChanged { screenWidth = it.width.toFloat() }
    ) {
        // ── Oldingi screen (parallax) — faqat swipe paytida ──
        if (isSwiping && previousScreen != null) {
            val prevOffset = (-screenWidth * 0.3f * (1f - progress)).roundToInt()

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .offset { IntOffset(prevOffset, 0) }
            ) {
                // Ochilish animatsiyasi tugamagan bo'lsa bu ekran AnimatedContent'da ham chizilib turadi —
                // bir kalit ikki joyda ishlatilsa Compose xato beradi, shuning uchun holatsiz chiziladi.
                val canRestore = previousScreen.key != navigator.lastItem.key &&
                        previousScreen.key !in animatedKeys
                if (canRestore) {
                    stateHolder.SaveableStateProvider(previousScreen.key) {
                        previousScreen.Content()
                    }
                } else {
                    previousScreen.Content()
                }
            }

            // Qorong'ulashish overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .drawWithContent {
                        drawContent()
                        drawRect(Color.Black.copy(alpha = 0.15f * (1f - progress)))
                    }
            )
        }

        // ── Hozirgi screen (push/pop animatsiyasi bilan) ──
        Box(
            modifier = Modifier
                .fillMaxSize()
                .offset { IntOffset(offsetX.value.roundToInt(), 0) }
                .then(
                    if (isSwiping) {
                        Modifier.shadow(
                            elevation = (8.dp * (1f - progress)),
                            ambientColor = Color.Black.copy(alpha = 0.2f)
                        )
                    } else Modifier
                )
                .pointerInput(navigator.lastItem.key) {
                    awaitEachGesture {
                        val down = awaitFirstDown(pass = PointerEventPass.Initial)
                        val startX = down.position.x

                        val edgeZone = maxOf(screenWidth * 0.15f, 50.dp.toPx())
                        if (startX > edgeZone) return@awaitEachGesture

                        gestureStarted = true
                        var totalDragX = 0f
                        var totalDragY = 0f
                        var directionDecided = false

                        try {
                            while (true) {
                                val event = awaitPointerEvent(PointerEventPass.Initial)
                                val change = event.changes.firstOrNull() ?: break

                                if (!change.pressed) {
                                    if (isSwiping) {
                                        val threshold = screenWidth * 0.3f
                                        scope.launch {
                                            if (offsetX.value > threshold) {
                                                offsetX.animateTo(
                                                    screenWidth,
                                                    tween(200, easing = FastOutSlowInEasing)
                                                )
                                                skipNextAnim = true
                                                navigator.pop()
                                                offsetX.snapTo(0f)
                                            } else {
                                                offsetX.animateTo(
                                                    0f,
                                                    tween(250, easing = FastOutSlowInEasing)
                                                )
                                            }
                                            isSwiping = false
                                        }
                                    }
                                    gestureStarted = false
                                    break
                                }

                                val delta = change.positionChange()
                                totalDragX += delta.x
                                totalDragY += delta.y

                                if (!directionDecided && (abs(totalDragX) > 10f || abs(totalDragY) > 10f)) {
                                    directionDecided = true
                                    if (abs(totalDragX) < abs(totalDragY) || totalDragX < 0) {
                                        gestureStarted = false
                                        break
                                    }
                                    isSwiping = true
                                }

                                if (isSwiping) {
                                    change.consume()
                                    scope.launch {
                                        val newVal = (offsetX.value + delta.x).coerceIn(0f, screenWidth)
                                        offsetX.snapTo(newVal)
                                    }
                                }
                            }
                        } catch (_: Exception) {
                            scope.launch {
                                if (isSwiping) {
                                    offsetX.animateTo(0f, tween(250))
                                    isSwiping = false
                                }
                            }
                            gestureStarted = false
                        }
                    }
                }
        ) {
            // ★ MUHIM: swipe paytida AnimatedContent animatsiya qilmasligi kerak
            // (chunki swipe o'zi qo'lda render qilyapti)
            AnimatedScreenContent(
                navigator = navigator,
                skipAnimation = isSwiping || skipNextAnim,
                stateHolder = stateHolder,
                animatedKeys = animatedKeys,
            )
        }
    }
}

/**
 * Push/Pop/Replace uchun iOS uslubidagi animatsiyalar.
 * skipAnimation=true bo'lganda hech qanday animatsiya bo'lmaydi (swipe paytida).
 */
@Composable
private fun AnimatedScreenContent(
    navigator: Navigator,
    skipAnimation: Boolean,
    stateHolder: SaveableStateHolder,
    animatedKeys: MutableSet<String>,
) {
    AnimatedContent(
        targetState = navigator.lastItem,
        contentKey = { it.key },
        transitionSpec = {
            if (skipAnimation) {
                EnterTransition.None togetherWith ExitTransition.None
            } else {
                when (navigator.lastEvent) {
                    StackEvent.Pop -> {
                        // Pop: yangi (avvalgi) ekran chapdan 1/3 dan keladi, eski to'liq o'ngga
                        slideInHorizontally(tween(POP_DURATION)) { -it / 3 } +
                                fadeIn(tween(POP_DURATION)) togetherWith
                                slideOutHorizontally(tween(POP_DURATION)) { it }
                    }
                    StackEvent.Replace -> {
                        // replaceAll (LoginScreen ga o'tish): fade
                        fadeIn(tween(REPLACE_DURATION)) togetherWith
                                fadeOut(tween(REPLACE_DURATION))
                    }
                    else -> {
                        // Push: yangi ekran o'ngdan keladi, eski chapga 1/3 siljiydi
                        slideInHorizontally(tween(PUSH_DURATION)) { it } togetherWith
                                slideOutHorizontally(tween(PUSH_DURATION)) { -it / 3 } +
                                fadeOut(tween(PUSH_DURATION))
                    }
                }
            }
        },
        label = "ScreenTransition"
    ) { screen: Screen ->
        DisposableEffect(screen.key) {
            animatedKeys += screen.key
            onDispose { animatedKeys -= screen.key }
        }
        stateHolder.SaveableStateProvider(screen.key) {
            screen.Content()
        }
    }
}