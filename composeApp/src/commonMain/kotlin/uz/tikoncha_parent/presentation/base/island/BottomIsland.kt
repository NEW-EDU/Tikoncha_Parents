package uz.tikoncha_parent.presentation.base.island

import uz.tikoncha_parent.ui.expressivePress
import tikoncha_parents.composeapp.generated.resources.ai_voice_mic
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.Icons
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.clickable
import androidx.compose.ui.unit.sp
import androidx.compose.material3.LocalContentColor
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.LocalRippleConfiguration
import uz.tikoncha_parent.ui.rememberHapticSource
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.toRect
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.offset
import org.jetbrains.compose.resources.painterResource
import tikoncha_parents.composeapp.generated.resources.Res
import tikoncha_parents.composeapp.generated.resources.arrow_back
import uz.tikoncha_parent.domain.model.UserInfo
import uz.tikoncha_parent.platform.isIos
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.onLongClick
import androidx.compose.ui.semantics.semantics
import cafe.adriel.voyager.navigator.LocalNavigator
import org.jetbrains.compose.resources.stringResource
import tikoncha_parents.composeapp.generated.resources.voice_sheet_title
import uz.tikoncha_parent.presentation.voice_help.VoiceHelpLayer
import uz.tikoncha_parent.presentation.voice_help.VoiceScreen
import uz.tikoncha_parent.ui.Haptics
import uz.tikoncha_parent.ui.expressiveButtonShapes
import uz.tikoncha_parent.ui.theme.AppColors
import uz.tikoncha_parent.ui.theme.AppTypography

/*
 * Pastki "orol" (M3 Expressive floating toolbar) — bir qo'l bilan ishlatish uchun ekranning BUTUN boshqaruvi pastda:
 * [‹ orqaga] [ekran nomi] [amallar] [farzand tanlagich ▴]. Tepada sarlavha yo'q — joy kontentga.
 * Ekran yordami (ovozli tuşuntiriş, kliplar) — tugmasiz: birinçi kirişda özi, keyin silkitiş yoki
 * "‹ nom" pill'ini uzoq bosiş ([VoiceHelpLayer]).
 *
 * Har ekran o'z orolini oladi ([ScreenWithIsland], navigatsiya qatlamida) — swipe-back paytida orol ekran bilan
 * birga suriladi. Ekran uni [CustomHeader] (sarlavha, orqaga, amallar, farzand tanlagich) yoki [ProvideIsland]
 * orqali to'ldiradi. Kontent pastidan joy layout bosqichida ajratiladi ([islandInset]) — kadr sakrashi yo'q.
 */

val IslandHeight = 64.dp
/** Orol bilan kontent orasidagi bo'shliq. */
private val IslandTopGap = 10.dp

/** iOS: orol uy indikatori zonasining yarmiga kirishi mumkin, ustidan ozgina ko'tariladi. */
private val IosIslandLift: Dp = 5.dp

/**
 * Orolning ekran pastidan masofasi.
 * iOS: uy indikatori zonasining yarmi + [IosIslandLift] (indikator ingichka, orol unga yaqin turadi).
 * Android, imo-ishora chizig'i (past panel): zonaning yarmi + 12dp — chiziq orolga tegmaydi.
 * Android, 3 tugmali panel (48dp): tugmalar panel O'RTASIDA, ustki 4dp bo'sh — orol shu bo'sh chetga
 * tushishi mumkin, tugmalarga emas (yarmini olsak, orol tugmalar ustiga tushardi; to'liq + bo'shliq juda baland).
 * Kontent uchun joy ([islandReservedFromBottom]) va farzand ro'yxati shu qiymatdan hisoblanadi — hammasi birga siljiydi.
 */
@Composable
fun islandBottomOffset(): Dp {
    val nav = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    return if (isIos()) maxOf(nav * 0.5f, 8.dp) + IosIslandLift
    else maxOf(nav * 0.5f + 12.dp, nav - 4.dp)
}

/** Ekran pastidan orol egallagan butun balandlik (kontent shundan yuqorida tugaydi). */
@Composable
fun islandReservedFromBottom(): Dp = islandBottomOffset() + IslandHeight + IslandTopGap

/** Orol ichidagi farzand tanlagich: ro'yxat yuqoriga ochiladi (Bosh sahifadagi bilan bir xil). */
@Stable
class ChildPickerSpec(
    val children: List<UserInfo>,
    val selected: UserInfo?,
    val onSelect: (UserInfo) -> Unit,
    val onAddChild: () -> Unit,
)

/** Ekran oroli nimani ko'rsatadi. [content] berilsa — butunlay o'zinikini (masalan, Bosh sahifa). */
@Stable
class IslandSpec(
    val title: String? = null,
    val onBack: (() -> Unit)? = null,
    val picker: (() -> ChildPickerSpec?)? = null,
    val actions: (@Composable RowScope.() -> Unit)? = null,
    val content: (@Composable RowScope.() -> Unit)? = null,
    /**
     * Ekran yordami id'si (ovozli tuşuntiriş + kliplar). Orolda tugma yöq: birinçi kirişda tuşuntiriş özi
     * boşlanadi, silkitilsa yoki sarlavha pill'i uzoq bosilsa — «Bu ekran haqida» varag'i ([VoiceHelpLayer]).
     */
    val voice: VoiceScreen? = null,
    /**
     * Keyingi kirişlarda (tuşuntiriş allaqaçon eşitilgaç) — holat haqida ovozli xabar: tayyor audio manzilini
     * qaytaradi (server ovozga aylantirgan, keşda); null — aytadigan narsa yöq. 3 s sanoqdan keyin ijro etiladi.
     */
    val voiceStatus: (suspend () -> String?)? = null,
    /** AI yordamçi mikrofoni: bosilsa yordamçi oçiladi va darhol tinglaydi (har ekrandan bir bosişda). */
    val onAiMic: (() -> Unit)? = null,
    /** Kontent tepasi (status bar ostida) yumaloqlansinmi — ro'yxat [CustomHeader] dan keyin darhol boshlansa. */
    val roundTop: Boolean = true,
)

@Stable
class IslandState {
    var spec by mutableStateOf<IslandSpec?>(null)
    var imeVisible by mutableStateOf(false)
    /** Oroldan ochiladigan qatlam (farzand ro'yxati) — ekran ichida, orol ustida chiziladi va sahifa bilan siljiydi. */
    var dropUp by mutableStateOf<(@Composable () -> Unit)?>(null)
    /** «Bu ekran haqida» varag'i oçiqmi (silkitiş, sarlavha yoki Bosh sahifada profil pill'ini uzoq bosiş). */
    var helpSheet by mutableStateOf(false)

    /** Ekran yordami varag'ini oçadi — faqat ekranda yordam id'si ([IslandSpec.voice]) bölsa. */
    fun openHelp() {
        if (spec?.voice == null) return
        Haptics.tick()
        helpSheet = true
    }

    /** Layout bosqichida o'qiladi (kompozitsiyada emas) — shu kadrning o'zida to'g'ri. */
    val active: Boolean get() = spec != null && !imeVisible
}

val LocalIsland = staticCompositionLocalOf<IslandState?> { null }

/** Navigatsiya qatlami: har ekranga alohida orol holati va uning ustida chiziladigan orol. */
@Composable
fun ScreenWithIsland(key: Any, content: @Composable () -> Unit) {
    val state = remember(key) { IslandState() }
    // Ötiş yoki swipe paytida ikki ekran çiziladi — yordam (ovoz, silkitiş) faqat stack tepasidagisida
    val navigator = LocalNavigator.current
    val isTop = navigator == null || navigator.lastItem.key == key
    CompositionLocalProvider(LocalIsland provides state) {
        Box(Modifier.fillMaxSize()) {
            content()
            IslandOverlay(state)
            state.dropUp?.invoke()
            VoiceHelpLayer(
                screen = state.spec?.voice,
                status = state.spec?.voiceStatus,
                isTop = isTop,
                sheetOpen = state.helpSheet,
                onSheetOpenChange = { state.helpSheet = it },
            )
        }
    }
}

/** Ekran kompozitsiyasi davomida orolni to'ldiradi (orol birinchi kadrdanoq bor); ekran yopilganda tozalanadi. */
@Composable
fun ProvideIsland(spec: IslandSpec?) {
    val island = LocalIsland.current ?: return
    if (island.spec !== spec) island.spec = spec
    DisposableEffect(spec) {
        onDispose { if (island.spec === spec) island.spec = null }
    }
}

/**
 * Kontent pastidan orol uchun joy (orol ko'rinmasa — 0). `rememberScreenSystemBars` modifikatoriga qo'shilgan,
 * `navigationBarsPadding()` dan KEYIN keladi — shuning uchun [navBottom] ayiriladi.
 */
fun Modifier.islandInset(state: IslandState?, reserved: Dp, navBottom: Dp, topEdge: Dp? = null): Modifier =
    if (state == null) this
    else this
        .layout { measurable, constraints ->
            val inset = if (state.active) (reserved - navBottom).coerceAtLeast(0.dp).roundToPx() else 0
            val placeable = measurable.measure(constraints.offset(vertical = -inset))
            layout(placeable.width, placeable.height + inset) { placeable.place(0, 0) }
        }
        .clip(IslandEdgeShape(state, topEdge))

/*
 * Kontent chekkalari: aylantirilgan kartalar orol ostiga (pastda) va status bar ostiga (tepada) keskin to'g'ri
 * chiziqda emas, kartalar chizig'ida (yon tomondan [EdgeInset]) [EdgeRadius] bilan yumaloqlanib kiradi —
 * Statistika'dagidek. Orol ko'rinmasa (klaviatura ochiq, orolsiz ekran) — oddiy to'rtburchak.
 */
val EdgeInset = 16.dp
val EdgeRadius = 28.dp

/** Tepada: status bar + [CustomHeader] bo'shlig'i — ro'yxatlar shu yerdan boshlanadi. */
val ContentTopGap = 8.dp

private class IslandEdgeShape(private val state: IslandState, private val topEdge: Dp?) : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        if (!state.active) return Outline.Rectangle(size.toRect())
        val top = if (state.spec?.roundTop == true) topEdge?.let { with(density) { it.toPx() } } else null
        return Outline.Generic(edgePath(size, density, top, roundBottom = true))
    }
}

/**
 * Ekran o'zi joylashtirgan ro'yxat uchun (tepasida qotirilgan sarlavha/tugmalar bo'lsa — Bosh sahifa, Vazifalar):
 * ro'yxat chekkasini kartalar chizig'ida yumaloqlaydi.
 */
fun Modifier.roundedContentEdges(top: Boolean = true, bottom: Boolean = false): Modifier =
    clip(ContentEdgeShape(top, bottom))

private class ContentEdgeShape(private val top: Boolean, private val bottom: Boolean) : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline =
        Outline.Generic(edgePath(size, density, if (top) 0f else null, roundBottom = bottom))
}

/**
 * [topY] — yumaloq tepa chekka (null — tepasi to'g'ri); undan yuqorisi to'liq kenglikda qoladi (status bar foni).
 * O'rtasi to'liq kenglikda, yumaloq burchaklar faqat chekka oldidagi [EdgeRadius] balandlikda.
 */
private fun edgePath(size: Size, density: Density, topY: Float?, roundBottom: Boolean): Path {
    val w = size.width
    val h = size.height
    val r = with(density) { EdgeRadius.toPx() }.coerceAtMost(h / 4)
    val inset = with(density) { EdgeInset.toPx() }.coerceAtMost(w / 4)
    val top = (topY ?: 0f).coerceIn(0f, h)
    val bandTop = if (topY != null) top + r else 0f
    val bandBottom = if (roundBottom) h - r else h
    val topCorner = if (topY != null) CornerRadius(r, r) else CornerRadius.Zero
    val bottomCorner = if (roundBottom) CornerRadius(r, r) else CornerRadius.Zero
    return Path().apply {
        if (top > 0f) addRect(Rect(0f, 0f, w, top))
        if (bandBottom > bandTop) addRect(Rect(0f, bandTop, w, bandBottom))
        addRoundRect(
            RoundRect(
                left = inset, top = top, right = w - inset, bottom = h,
                topLeftCornerRadius = topCorner, topRightCornerRadius = topCorner,
                bottomRightCornerRadius = bottomCorner, bottomLeftCornerRadius = bottomCorner,
            )
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun BoxScope.IslandOverlay(state: IslandState) {
    val imeOpen = WindowInsets.ime.getBottom(LocalDensity.current) > 0
    if (state.imeVisible != imeOpen) state.imeVisible = imeOpen
    val spec = state.spec
    val bottom = islandBottomOffset()
    AnimatedVisibility(
        visible = spec != null && !imeOpen,
        modifier = Modifier.align(Alignment.BottomCenter),
        enter = slideInVertically(MaterialTheme.motionScheme.defaultSpatialSpec()) { it } + fadeIn(),
        exit = slideOutVertically(MaterialTheme.motionScheme.fastSpatialSpec()) { it } + fadeOut(),
    ) {
        val s = spec ?: return@AnimatedVisibility
        // Soya yo'q (foydalanuvchi talabi) — fonidan ingichka chegara bilan ajraladi.
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 12.dp, end = 12.dp, bottom = bottom)
                .height(IslandHeight)
                .clip(CircleShape)
                .background(AppColors.bg.surface)
                .border(1.dp, AppColors.border.secondarySubtle, CircleShape)
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            if (s.content != null) {
                s.content.invoke(this)
                return@Row
            }
            // Orqaga + sarlavha — bitta keng pill: sarlavhaning o'zi bosiladi (barmoqni chap burchakka cho'zish shart emas)
            when {
                // Uzoq bosiş — «Bu ekran haqida» (silkita olmaydiganlar uçun)
                s.onBack != null -> IslandTitlePill(
                    title = s.title.orEmpty(),
                    onClick = s.onBack,
                    onLongClick = if (s.voice != null) ({ state.openHelp() }) else null,
                    modifier = Modifier.weight(1f),
                )
                s.title != null -> Text(
                    text = s.title,
                    style = AppTypography.titleMdSemiBold.copy(fontWeight = FontWeight.ExtraBold),
                    color = AppColors.text.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f).padding(start = 12.dp),
                )
                else -> Spacer(Modifier.weight(1f))
            }
            s.actions?.invoke(this)
            s.onAiMic?.let { IslandAiMic(onClick = it) }
            s.picker?.invoke()?.let { p ->
                // Amal yoki mikrofon bor — tanlagich ixçam (avatar + ▴), sarlavhaga joy qolsin
                IslandChildPicker(p, modifier = Modifier.widthIn(max = 164.dp), compact = s.actions != null || s.onAiMic != null)
            }
        }
    }
}

/**
 * Orqaga tugmasi + ekran nomi — bitta tonal pill: "‹  Statistika". Butun pill bosiladi (sarlavhaga bosish = orqaga),
 * shuning uchun nishon katta va orolning o'rtasigacha cho'ziladi. [onLongClick] — uzoq bosiş (orqaga ketmaydi).
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class, androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun IslandTitlePill(
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onLongClick: (() -> Unit)? = null,
) {
    // Ripple yo'q: bosilganda sahifa darhol ketadi — yorqin to'lqin yarmida uzilib "lip" etardi.
    // Javob — taktil zarba va shakl morfi.
    CompositionLocalProvider(LocalRippleConfiguration provides null) {
        Button(
            onClick = onClick,
            interactionSource = rememberHapticSource(),
            shapes = expressiveButtonShapes(),
            colors = ButtonDefaults.buttonColors(
                containerColor = AppColors.action.primary.copy(alpha = 0.14f),
                contentColor = AppColors.text.primary,
            ),
            contentPadding = PaddingValues(start = 12.dp, end = 14.dp),
            modifier = modifier
                .height(48.dp)
                .then(if (onLongClick != null) Modifier.longPressInsteadOfClick(onLongClick) else Modifier),
        ) {
            Icon(painterResource(Res.drawable.arrow_back), contentDescription = null, modifier = Modifier.size(22.dp))
            Spacer(Modifier.width(8.dp))
            // Orolda tugmalar köp bölsa (Xarita: 3 ta amal) nom kesilmasin — şrift kiçrayadi
            val titleStyle = AppTypography.titleMdSemiBold.copy(fontWeight = FontWeight.ExtraBold, color = LocalContentColor.current)
            BasicText(
                text = title,
                style = titleStyle,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                autoSize = TextAutoSize.StepBased(minFontSize = 11.sp, maxFontSize = titleStyle.fontSize, stepSize = 0.5.sp),
                modifier = Modifier.weight(1f),
            )
        }
    }
}

/**
 * M3 [Button] da onLongClick yöq: barmoq uzoq tursa [action], qöyib yuborilganda tugmaning oddiy bosişi
 * (orqaga) işlamaydi — qolgan hodisalar Initial bosqiçida "yeyiladi", tugma buni bekor qiliş deb tuşunadi.
 * Qisqa bosiş — odatdagidek. TalkBack/VoiceOver uçun — "uzoq bosiş" amali.
 */
@Composable
private fun Modifier.longPressInsteadOfClick(action: () -> Unit): Modifier {
    val latest by rememberUpdatedState(action)
    val label = stringResource(Res.string.voice_sheet_title)
    return this
        .semantics { onLongClick(label = label) { latest(); true } }
        .pointerInput(Unit) {
            awaitEachGesture {
                awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
                var ended = false
                withTimeoutOrNull(viewConfiguration.longPressTimeoutMillis) {
                    waitForUpOrCancellation(PointerEventPass.Initial)
                    ended = true
                }
                if (ended) return@awaitEachGesture
                latest()
                do {
                    val event = awaitPointerEvent(PointerEventPass.Initial)
                    event.changes.forEach { it.consume() }
                } while (event.changes.any { it.pressed })
            }
        }
}

/** Oroldagi AI mikrofoni: urg'u rangli doira, 44dp (barmoq nishoni 48dp). */
@Composable
private fun IslandAiMic(onClick: () -> Unit) {
    val source = remember { MutableInteractionSource() }
    Box(
        modifier = Modifier
            .size(48.dp)
            .clickable(interactionSource = source, indication = null, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .expressivePress(source, pressedScale = 0.9f)
                .clip(CircleShape)
                .background(AppColors.action.primary),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Rounded.Mic,
                contentDescription = stringResource(Res.string.ai_voice_mic),
                tint = AppColors.text.inverse,
                modifier = Modifier.size(24.dp),
            )
        }
    }
}
