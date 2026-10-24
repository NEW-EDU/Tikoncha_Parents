package uz.tikoncha_parent.presentation.base.island

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
import uz.tikoncha_parent.ui.expressiveButtonShapes
import uz.tikoncha_parent.ui.theme.AppColors
import uz.tikoncha_parent.ui.theme.AppTypography

/*
 * Pastki "orol" (M3 Expressive floating toolbar) — bir qo'l bilan ishlatish uchun ekranning BUTUN boshqaruvi pastda:
 * [‹ orqaga] [ekran nomi] [farzand tanlagich ▴ / amallar]. Tepada sarlavha yo'q — joy kontentga.
 *
 * Har ekran o'z orolini oladi ([ScreenWithIsland], navigatsiya qatlamida) — swipe-back paytida orol ekran bilan
 * birga suriladi. Ekran uni [CustomHeader] (sarlavha, orqaga, amallar, farzand tanlagich) yoki [ProvideIsland]
 * orqali to'ldiradi. Kontent pastidan joy layout bosqichida ajratiladi ([islandInset]) — kadr sakrashi yo'q.
 */

val IslandHeight = 64.dp
/** Orol bilan kontent orasidagi bo'shliq. */
private val IslandTopGap = 10.dp

/** Orolning ekran pastidan masofasi: tizim navigatsiya zonasining yarmi (iPhone'da indikatorga yaqin, lekin ustida). */
@Composable
fun islandBottomOffset(): Dp {
    val nav = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    return maxOf(nav * 0.5f, 8.dp)
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
)

@Stable
class IslandState {
    var spec by mutableStateOf<IslandSpec?>(null)
    var imeVisible by mutableStateOf(false)
    /** Layout bosqichida o'qiladi (kompozitsiyada emas) — shu kadrning o'zida to'g'ri. */
    val active: Boolean get() = spec != null && !imeVisible
}

val LocalIsland = staticCompositionLocalOf<IslandState?> { null }

/** Navigatsiya qatlami: har ekranga alohida orol holati va uning ustida chiziladigan orol. */
@Composable
fun ScreenWithIsland(key: Any, content: @Composable () -> Unit) {
    val state = remember(key) { IslandState() }
    CompositionLocalProvider(LocalIsland provides state) {
        Box(Modifier.fillMaxSize()) {
            content()
            IslandOverlay(state)
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
fun Modifier.islandInset(state: IslandState?, reserved: Dp, navBottom: Dp): Modifier =
    if (state == null) this
    else this
        .layout { measurable, constraints ->
            val inset = if (state.active) (reserved - navBottom).coerceAtLeast(0.dp).roundToPx() else 0
            val placeable = measurable.measure(constraints.offset(vertical = -inset))
            layout(placeable.width, placeable.height + inset) { placeable.place(0, 0) }
        }
        .clip(IslandEdgeShape(state))

/**
 * Kontentning orol tomondagi qirrasi: tepasi to'g'ri, pastki chekkasi esa kartalar chizig'ida (yon tomondan
 * [EdgeInset]) [EdgeRadius] bilan yumaloqlanadi — aylantirilgan kartalar orol ostiga keskin kesilib emas,
 * Statistika'dagidek yumaloq bo'lib kiradi. Orol ko'rinmasa (klaviatura ochiq, orolsiz ekran) — oddiy to'rtburchak.
 */
private val EdgeInset = 16.dp
private val EdgeRadius = 28.dp

private class IslandEdgeShape(private val state: IslandState) : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        if (!state.active) return Outline.Rectangle(size.toRect())
        val r = with(density) { EdgeRadius.toPx() }.coerceAtMost(size.height / 2)
        val inset = with(density) { EdgeInset.toPx() }.coerceAtMost(size.width / 4)
        val path = Path().apply {
            addRect(Rect(0f, 0f, size.width, size.height - r))
            addRoundRect(
                RoundRect(
                    left = inset, top = 0f, right = size.width - inset, bottom = size.height,
                    topLeftCornerRadius = CornerRadius.Zero, topRightCornerRadius = CornerRadius.Zero,
                    bottomRightCornerRadius = CornerRadius(r, r), bottomLeftCornerRadius = CornerRadius(r, r),
                )
            )
        }
        return Outline.Generic(path)
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
                s.onBack != null -> IslandTitlePill(title = s.title.orEmpty(), onClick = s.onBack, modifier = Modifier.weight(1f))
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
            s.picker?.invoke()?.let { p ->
                IslandChildPicker(p, modifier = Modifier.widthIn(max = 164.dp), compact = s.actions != null)
            }
        }
    }
}

/**
 * Orqaga tugmasi + ekran nomi — bitta tonal pill: "‹  Statistika". Butun pill bosiladi (sarlavhaga bosish = orqaga),
 * shuning uchun nishon katta va orolning o'rtasigacha cho'ziladi.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun IslandTitlePill(title: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(
        onClick = onClick,
        shapes = expressiveButtonShapes(),
        colors = ButtonDefaults.buttonColors(
            containerColor = AppColors.action.primary.copy(alpha = 0.14f),
            contentColor = AppColors.text.primary,
        ),
        contentPadding = PaddingValues(start = 14.dp, end = 18.dp),
        modifier = modifier.height(48.dp),
    ) {
        Icon(painterResource(Res.drawable.arrow_back), contentDescription = null, modifier = Modifier.size(22.dp))
        Spacer(Modifier.width(10.dp))
        Text(
            text = title,
            style = AppTypography.titleMdSemiBold.copy(fontWeight = FontWeight.ExtraBold),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
    }
}
