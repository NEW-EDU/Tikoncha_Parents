package uz.tikoncha_parent.presentation.base.island

import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.tween
import uz.tikoncha_parent.ui.rememberHapticSource
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectTapGestures
import kotlinx.coroutines.delay
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.DisposableEffect
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import org.jetbrains.compose.resources.DrawableResource
import uz.tikoncha_parent.ui.SuccessColor
import uz.tikoncha_parent.presentation.profile.children.ChildrenSelectScreen
import tikoncha_parents.composeapp.generated.resources.plus_home_sheet_subscribe
import tikoncha_parents.composeapp.generated.resources.hammasi
import cafe.adriel.voyager.navigator.LocalNavigator
import androidx.compose.ui.unit.Dp
import androidx.compose.material3.TextButton
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.border
import androidx.compose.foundation.Image
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
import coil3.compose.AsyncImage
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import tikoncha_parents.composeapp.generated.resources.Res
import tikoncha_parents.composeapp.generated.resources.farzand_qo_shish
import tikoncha_parents.composeapp.generated.resources.farzandlaringiz
import tikoncha_parents.composeapp.generated.resources.profile_hedgehog_img
import tikoncha_parents.composeapp.generated.resources.support_icon
import uz.tikoncha_parent.domain.model.UserInfo
import uz.tikoncha_parent.ui.SegmentGap
import uz.tikoncha_parent.ui.expressivePress
import uz.tikoncha_parent.ui.expressiveSegment
import uz.tikoncha_parent.ui.rememberCachedImage
import uz.tikoncha_parent.ui.theme.AppColors
import uz.tikoncha_parent.ui.theme.AppTypography


/*
 * Farzand tanlagich — butun ilovada BITTA (Bosh sahifa, Statistika, Jadvallar, Himoya, Vazifalar…): orol ichidagi
 * pill [avatar  ism  ▴], bosilganda farzandlar ro'yxati orol ustida pastdan YUQORIGA ochiladi ("drop-up").
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun IslandChildPicker(
    spec: ChildPickerSpec,
    modifier: Modifier = Modifier,
    open: MutableTransitionState<Boolean> = remember { MutableTransitionState(false) },
    /** Orolda joy tor bo'lsa (ekran amallari bor) — faqat avatar va ▴. */
    compact: Boolean = false,
) {
    val hasChildren = spec.children.isNotEmpty()
    val arrowTurn by animateFloatAsState(
        targetValue = if (open.targetState) 180f else 0f,
        animationSpec = MaterialTheme.motionScheme.defaultSpatialSpec(),
        label = "pickerArrow",
    )
    val source = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
    Box(modifier) {
        Row(
            modifier = Modifier
                .then(if (compact) Modifier else Modifier.fillMaxWidth())
                .heightIn(min = 48.dp)
                .expressivePress(source, pressedScale = 0.96f)
                .clip(CircleShape)
                .background(AppColors.action.primary.copy(alpha = 0.14f))
                .clickable(interactionSource = source, indication = null) {
                    if (hasChildren) open.targetState = !open.targetState else spec.onAddChild()
                }
                .padding(start = 6.dp, end = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (hasChildren) {
                PlusAvatar(spec.selected, 36.dp)
                if (!compact) {
                Spacer(Modifier.width(8.dp))
                Text(
                    text = spec.selected?.name.orEmpty(),
                    style = AppTypography.titleMdSemiBold,
                    color = AppColors.text.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false),
                )
                }
                Spacer(Modifier.width(2.dp))
                // ▴ — ro'yxat yuqoriga ochiladi; ochiq bo'lsa ▾
                Icon(
                    Icons.Rounded.KeyboardArrowUp,
                    contentDescription = null,
                    tint = AppColors.icon.secondary,
                    modifier = Modifier.size(24.dp).graphicsLayer { rotationZ = arrowTurn },
                )
            } else {
                Spacer(Modifier.width(6.dp))
                Icon(Icons.Rounded.Add, contentDescription = null, tint = AppColors.icon.accentPrimary, modifier = Modifier.size(22.dp))
                Spacer(Modifier.width(6.dp))
                Text(
                    text = stringResource(Res.string.farzand_qo_shish),
                    style = AppTypography.titleMdSemiBold,
                    color = AppColors.text.accentEmphasis,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }

        // Ro'yxat ekran qatlamida chiziladi (alohida oyna emas) — boshqa sahifaga o'tilganda u sahifa bilan birga
        // siljib ketadi: "yopildi → keyin o'tdi" bo'shlig'i umuman yo'q.
        val island = LocalIsland.current
        val currentSpec by rememberUpdatedState(spec)
        val layer: @Composable () -> Unit = remember(open) {
            {
                ChildDropUp(
                    open = open,
                    children = currentSpec.children,
                    selected = currentSpec.selected,
                    onSelect = { open.targetState = false; currentSpec.onSelect(it) },
                    onAddChild = currentSpec.onAddChild,
                    onDismiss = { open.targetState = false },
                )
            }
        }
        DisposableEffect(island, layer) {
            island?.dropUp = layer
            onDispose { if (island?.dropUp === layer) island.dropUp = null }
        }
    }
}

/** Farzand avatari: PLUS bo'lsa yashil halqa va "PLUS" belgisi (eski tanlagichdagidek). */
@Composable
fun PlusAvatar(child: UserInfo?, size: Dp) {
    val plus = !child?.subscription.isNullOrBlank() && child?.subscription != "FREE"
    Box(Modifier.size(size + 6.dp), contentAlignment = Alignment.Center) {
        ChildAvatar(
            child?.avatarUrl,
            Modifier.size(size).then(if (plus) Modifier.border(2.dp, SuccessColor, CircleShape) else Modifier),
        )
        if (plus) {
            Image(
                painter = painterResource(Res.drawable.plus_home_sheet_subscribe),
                contentDescription = null,
                modifier = Modifier.align(Alignment.TopEnd).offset(x = 4.dp, y = (-2).dp).height(15.dp).width(22.dp),
            )
        }
    }
}

@Composable
fun ChildAvatar(
    url: String?,
    modifier: Modifier,
    /** Rasm yo'q bo'lsa: farzand — yashil tipratikan, ota-ona — ilova ikonkasidagi ko'zoynakli tipratikan. */
    fallback: DrawableResource = Res.drawable.profile_hedgehog_img,
) {
    AsyncImage(
        model = rememberCachedImage(url),
        contentDescription = null,
        error = painterResource(fallback),
        contentScale = ContentScale.Crop,
        modifier = modifier.clip(CircleShape).background(AppColors.bg.primaryContainer, CircleShape),
    )
}

/** Farzandlar ro'yxati — orolning ustida, pastdan yuqoriga ochiladi (M3E guruhlangan ro'yxat). */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ChildDropUp(
    open: MutableTransitionState<Boolean>,
    children: List<UserInfo>,
    selected: UserInfo?,
    onSelect: (UserInfo) -> Unit,
    onAddChild: () -> Unit,
    onDismiss: () -> Unit,
) {
    if (!open.currentState && !open.targetState) return
    // Boshqa sahifaga o'tish: ro'yxat yopilmaydi — eski sahifa bilan birga siljib ketadi; sahifa qatlamdan
    // chiqqach holat o'z-o'zidan tozalanadi (qaytilganda yopiq). Shu sahifa qolib ketsa — keyinroq yopiladi.
    val scope = rememberCoroutineScope()
    val navigateAway: (() -> Unit) -> Unit = { go ->
        go()
        scope.launch {
            delay(700)
            open.targetState = false
        }
    }
    @OptIn(cafe.adriel.voyager.core.annotation.InternalVoyagerApi::class)
    cafe.adriel.voyager.navigator.internal.BackHandler(open.targetState) { onDismiss() }
    Box(Modifier.fillMaxSize()) {
        // Tashqariga bosish — yopadi (avvalgi oynadagidek); fon o'zgarmaydi
        Box(
            Modifier
                .fillMaxSize()
                .pointerInput(Unit) { detectTapGestures { onDismiss() } }
        )
        AnimatedVisibility(
            visibleState = open,
            // Soya kartadan tashqarida çiziladi; fade paytida qatlam öz çegarasi bilan kesilgani uçun soya animatsiya
            // tugagandagina "sakrab" paydo bölardi (miltillaş). Endi soya uçun joy konteyner içida — u bilan birga söniq.
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = islandBottomOffset() + IslandHeight - 4.dp),
            enter = scaleIn(MaterialTheme.motionScheme.defaultSpatialSpec(), initialScale = 0.85f, transformOrigin = TransformOrigin(0.5f, 1f)) + fadeIn(),
            // Yopilish — prujinasiz (prujina "ortga sakrab" keyin yo'qolardi): tez, bir yo'nalishda pastga cho'kadi
            exit = fadeOut(tween(150)) +
                scaleOut(tween(170, easing = FastOutLinearInEasing), targetScale = 0.95f, transformOrigin = TransformOrigin(0.5f, 1f)) +
                slideOutVertically(tween(170, easing = FastOutLinearInEasing)) { it / 14 },
        ) {
          Box(Modifier.padding(start = 14.dp, end = 14.dp, top = 14.dp, bottom = 18.dp)) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    // Tizim soyasi (elevation) ochiliş animatsiyasida (fade) bir lahza töğri burçakli çizilardi —
                    // öz soyamiz: kartaning şaklida, u bilan birga paydo böladi
                    .softShadow(28.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .background(AppColors.bg.page)
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(SegmentGap),
            ) {
                // Sarlavha + "Hammasi" — Farzandlarim sahifasiga (Profil ichidagi)
                val navigator = LocalNavigator.current
                Row(Modifier.fillMaxWidth().padding(start = 12.dp, end = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = stringResource(Res.string.farzandlaringiz),
                        style = AppTypography.titleSmMedium,
                        color = AppColors.text.tertiary,
                        modifier = Modifier.weight(1f),
                    )
                    TextButton(interactionSource = rememberHapticSource(), onClick = { navigateAway { navigator?.push(ChildrenSelectScreen()) } }) {
                        Text(stringResource(Res.string.hammasi), style = AppTypography.titleSmMedium, color = AppColors.text.accentEmphasis)
                        Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, contentDescription = null, tint = AppColors.text.accentEmphasis, modifier = Modifier.size(18.dp))
                    }
                }
                val count = children.size + 1
                children.forEachIndexed { i, child ->
                    val isSel = child.userId == selected?.userId
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 60.dp)
                            .expressiveSegment(
                                i, count,
                                color = if (isSel) AppColors.action.primary.copy(alpha = 0.14f) else AppColors.bg.surface,
                                outer = 22.dp, selected = false,
                            ) { onSelect(child) }
                            .padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        PlusAvatar(child, 40.dp)
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f).padding(vertical = 8.dp)) {
                            Text(
                                text = child.name,
                                style = AppTypography.titleMdSemiBold,
                                color = AppColors.text.primary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            if (!child.last_seen.isNullOrBlank()) {
                                Text(
                                    text = child.last_seen.orEmpty(),
                                    style = AppTypography.emphasizedSmRegular,
                                    color = AppColors.text.tertiary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                        }
                        if (isSel) Icon(Icons.Rounded.Check, contentDescription = null, tint = AppColors.action.primary, modifier = Modifier.size(22.dp))
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 56.dp)
                        .expressiveSegment(children.size, count, color = AppColors.bg.surface, outer = 22.dp) { navigateAway(onAddChild) }
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Rounded.Add, contentDescription = null, tint = AppColors.icon.accentPrimary, modifier = Modifier.size(22.dp))
                    Spacer(Modifier.width(12.dp))
                    Text(stringResource(Res.string.farzand_qo_shish), style = AppTypography.titleMdSemiBold, color = AppColors.text.accentEmphasis)
                }
            }
          }
        }
    }
}

/** Yumşoq soya: [cornerRadius] li burçakli bir neçta qatlam, pastga biroz siljigan. Fade va scale bilan töğri çiziladi. */
private fun Modifier.softShadow(cornerRadius: androidx.compose.ui.unit.Dp): Modifier = drawBehind {
    val layers = 8
    val spread = 14.dp.toPx()
    val dy = 4.dp.toPx()
    val r = cornerRadius.toPx()
    for (i in layers downTo 1) {
        val grow = spread * i / layers
        drawRoundRect(
            color = androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.035f),
            topLeft = androidx.compose.ui.geometry.Offset(-grow, -grow + dy),
            size = androidx.compose.ui.geometry.Size(size.width + 2 * grow, size.height + 2 * grow),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(r + grow, r + grow),
        )
    }
}
