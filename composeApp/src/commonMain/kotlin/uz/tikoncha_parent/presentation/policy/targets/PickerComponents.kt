package uz.tikoncha_parent.presentation.policy.targets

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import tikoncha_parents.composeapp.generated.resources.Res
import tikoncha_parents.composeapp.generated.resources.arrow_down_reg
import tikoncha_parents.composeapp.generated.resources.bu_sayt_allaqachon_ro_yxatda
import tikoncha_parents.composeapp.generated.resources.close_remove
import tikoncha_parents.composeapp.generated.resources.global
import tikoncha_parents.composeapp.generated.resources.message_delete
import tikoncha_parents.composeapp.generated.resources.message_edit
import tikoncha_parents.composeapp.generated.resources.ochirish
import tikoncha_parents.composeapp.generated.resources.saqlash
import tikoncha_parents.composeapp.generated.resources.sayt_manzili_notogri
import tikoncha_parents.composeapp.generated.resources.sayt_manzilini_kiriting
import tikoncha_parents.composeapp.generated.resources.tahrirlash
import tikoncha_parents.composeapp.generated.resources.targets_covered_by_category
import tikoncha_parents.composeapp.generated.resources.targets_no_apps
import uz.tikoncha_parent.domain.model.apps.InstalledApp
import uz.tikoncha_parent.presentation.base.ChildAppIcon
import uz.tikoncha_parent.presentation.base.CustomButtonNew
import uz.tikoncha_parent.presentation.base.CustomTextField
import uz.tikoncha_parent.presentation.base.haptics.rememberAppHaptics
import uz.tikoncha_parent.presentation.base.singleClick
import uz.tikoncha_parent.presentation.policy.components.PolicyText
import uz.tikoncha_parent.ui.CardCornerRadius
import uz.tikoncha_parent.ui.ContainerPadding
import uz.tikoncha_parent.ui.HeaderHeight
import uz.tikoncha_parent.ui.NormalIconButtonSize
import uz.tikoncha_parent.ui.theme.AppColors
import tikoncha_parents.composeapp.generated.resources.protection_shield_short

/*
 * Nishon tanlash qatorlari — Student `PickerComponents` bilan bir xil:
 * 54dp qator, 32dp ikonka, 16sp matn, dumaloq-burchakli katak.
 */

private val RowHeight = 54.dp
private val IconSize = 32.dp
private val IconShape = RoundedCornerShape(12.dp)

// ═══════════════════════════════════════════
//  Ilova ichidagi funksiyalar (YouTube Shorts, Instagram Reels)
// ═══════════════════════════════════════════

/** Serverga `targets.features` ichida `key` ketadi. */
data class PolicyAppFeature(val key: String, val name: String, val parentPackage: String)

object PolicyAppFeatures {
    private const val YOUTUBE = "com.google.android.youtube"
    private const val INSTAGRAM = "com.instagram.android"

    private val REGISTRY: Map<String, List<PolicyAppFeature>> = mapOf(
        YOUTUBE to listOf(PolicyAppFeature("YOUTUBE_SHORTS", "YouTube Shorts", YOUTUBE)),
        INSTAGRAM to listOf(PolicyAppFeature("INSTAGRAM_REELS", "Instagram Reels", INSTAGRAM)),
    )

    fun featuresFor(packageName: String): List<PolicyAppFeature> = REGISTRY[packageName].orEmpty()

    /** Kalit bo'yicha funksiya (`YOUTUBE_SHORTS` → YouTube Shorts). */
    fun byKey(key: String): PolicyAppFeature? = REGISTRY.values.flatten().firstOrNull { it.key.equals(key, ignoreCase = true) }

    /** Qidiruvda: ilova nomi mos kelsa — hammasi, aks holda faqat mos funksiyalar. */
    fun visibleFeaturesFor(app: InstalledApp, query: String): List<PolicyAppFeature> {
        val all = featuresFor(app.packageName)
        if (all.isEmpty() || query.isBlank() || app.name.contains(query, ignoreCase = true)) return all
        return all.filter { it.name.contains(query, ignoreCase = true) }
    }

    fun matchesSearch(app: InstalledApp, query: String): Boolean =
        query.isBlank() ||
            app.name.contains(query, ignoreCase = true) ||
            featuresFor(app.packageName).any { it.name.contains(query, ignoreCase = true) }
}

// ═══════════════════════════════════════════
//  Katak
// ═══════════════════════════════════════════

@Composable
fun AppCheckbox(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    /** Kategoriya orqali tanlangan: och-yashil fon, yashil ✓ — alohida belgilab/olib bo'lmaydi. */
    covered: Boolean = false,
) {
    val markColor = if (covered) AppColors.icon.accentPrimary else AppColors.text.inverse
    val border by animateColorAsState(
        targetValue = when {
            covered -> AppColors.bg.primary.copy(alpha = 0.6f)
            checked -> AppColors.bg.primary
            else -> AppColors.border.secondary
        },
        animationSpec = tween(300),
        label = "checkboxBorder",
    )
    val idleFill = if (enabled) AppColors.bg.surface else AppColors.button.disabled
    val fill by animateColorAsState(
        targetValue = when {
            covered -> AppColors.bg.primary.copy(alpha = 0.18f)
            checked -> AppColors.bg.primary
            else -> idleFill
        },
        animationSpec = tween(300),
        label = "checkboxFill",
    )
    val markScale by animateFloatAsState(
        targetValue = if (checked || covered) 1f else 0f,
        animationSpec = spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMedium),
        label = "checkboxMark",
    )
    // Belgilandi / olib tashlandi — qator yoki katak bosilganidan qat'i nazar, bitta joyda
    val haptics = rememberAppHaptics()
    var shown by remember { mutableStateOf(checked) }
    LaunchedEffect(checked) {
        if (checked != shown) {
            shown = checked
            haptics.toggle(checked)
        }
    }
    Canvas(
        modifier = modifier
            .size(24.dp)
            .then(if (enabled) Modifier.singleClick { onCheckedChange(!checked) } else Modifier)
            .padding(1.dp),
    ) {
        val strokePx = 1.dp.toPx()
        val radius = 8.dp.toPx()
        drawRoundRect(color = fill, cornerRadius = CornerRadius(radius), size = size)
        drawRoundRect(color = border, cornerRadius = CornerRadius(radius), size = size, style = Stroke(width = strokePx))
        if (markScale > 0f) {
            val w = size.width
            val h = size.height
            val path = Path().apply {
                moveTo(w * 0.22f, h * 0.50f)
                lineTo(w * 0.42f, h * 0.70f)
                lineTo(w * 0.78f, h * 0.32f)
            }
            scale(markScale, pivot = center) {
                drawPath(path = path, color = markColor, style = Stroke(width = strokePx * 2, cap = StrokeCap.Round, join = StrokeJoin.Round))
            }
        }
    }
}

// ═══════════════════════════════════════════
//  Qidiruv sarlavhasi
// ═══════════════════════════════════════════

@Composable
fun PickerSearchHeader(query: String, placeholder: String, onQueryChange: (String) -> Unit, onClose: () -> Unit) {
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) { focusRequester.requestFocus() }
    Row(
        modifier = Modifier.fillMaxWidth().height(HeaderHeight).padding(horizontal = ContainerPadding),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(
            modifier = Modifier.size(NormalIconButtonSize),
            onClick = onClose,
            colors = IconButtonDefaults.iconButtonColors(containerColor = Color.Transparent, contentColor = AppColors.icon.primary),
        ) {
            Icon(imageVector = Icons.Default.Close, contentDescription = null, modifier = Modifier.size(24.dp))
        }
        Spacer(modifier = Modifier.width(16.dp))
        Box(modifier = Modifier.weight(1f).height(40.dp), contentAlignment = Alignment.CenterStart) {
            if (query.isEmpty()) {
                Text(text = placeholder, style = PolicyText.input, color = AppColors.text.tertiary)
            }
            BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
                singleLine = true,
                textStyle = PolicyText.input.copy(color = AppColors.text.primary),
                cursorBrush = SolidColor(AppColors.action.primary),
            )
        }
    }
}

// ═══════════════════════════════════════════
//  Yuklanish
// ═══════════════════════════════════════════

@Composable
fun ShimmerAppRow(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val offset by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(animation = tween(1000, easing = LinearEasing), repeatMode = RepeatMode.Restart),
        label = "shimmerOffset",
    )
    val base = AppColors.bg.tertiary
    val brush = Brush.linearGradient(
        colors = listOf(base, AppColors.border.secondary, base),
        start = Offset(offset - 300f, 0f),
        end = Offset(offset + 300f, 0f),
    )
    Row(modifier = modifier.fillMaxWidth().height(52.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(IconSize).clip(IconShape).background(brush))
        Spacer(modifier = Modifier.width(16.dp))
        Box(modifier = Modifier.weight(1f)) {
            Box(modifier = Modifier.fillMaxWidth(0.6f).height(14.dp).clip(RoundedCornerShape(4.dp)).background(brush))
        }
        Spacer(modifier = Modifier.width(10.dp))
        Box(modifier = Modifier.size(24.dp).clip(RoundedCornerShape(8.dp)).background(brush))
    }
}

// ═══════════════════════════════════════════
//  Ilova qatorlari
// ═══════════════════════════════════════════

/**
 * Ilova qatori. `covered` — ilova kategoriyasi orqali tanlangan: katak o'chiq,
 * bosilsa `onToggle` (ekran xabar ko'rsatadi).
 */
@Composable
fun PickerAppRow(
    name: String,
    iconUrl: String?,
    checked: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
    covered: Boolean = false,
    expandable: Boolean = false,
    expanded: Boolean = false,
    onExpandToggle: () -> Unit = {},
) {
    val rotation by animateFloatAsState(if (expanded) 180f else 0f, tween(300), label = "appArrow")
    val showArrow = expandable && !covered
    val haptics = rememberAppHaptics()
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(RowHeight)
            .singleClick {
                when {
                    covered -> {
                        haptics.error()      // kategoriya orqali tanlangan — alohida olib bo'lmaydi
                        onToggle()
                    }
                    expandable -> onExpandToggle()
                    else -> onToggle()
                }
            },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ChildAppIcon(iconUrl = iconUrl, size = IconSize)
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = name, maxLines = 1, overflow = TextOverflow.Ellipsis, color = AppColors.text.primary, style = PolicyText.row)
            if (covered) {
                Text(
                    text = stringResource(Res.string.targets_covered_by_category),
                    maxLines = 1,
                    color = AppColors.text.accentEmphasis,
                    style = PolicyText.subtitle,
                )
            }
        }
        Spacer(modifier = Modifier.width(8.dp))
        if (showArrow) {
            Icon(
                painter = painterResource(Res.drawable.arrow_down_reg),
                contentDescription = null,
                modifier = Modifier.size(20.dp).rotate(rotation),
                tint = AppColors.icon.secondary,
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
        AppCheckbox(checked = checked, enabled = !covered, covered = covered, onCheckedChange = { onToggle() })
    }
}

/** Ilova + ichki funksiyalari (Shorts / Reels). */
@Composable
fun PickerAppWithFeatures(
    app: InstalledApp,
    features: List<PolicyAppFeature>,
    checked: Boolean,
    selectedFeatures: Set<String>,
    covered: Boolean,
    expanded: Boolean,
    onToggleExpand: () -> Unit,
    onAppToggle: () -> Unit,
    onFeatureToggle: (PolicyAppFeature) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth().animateContentSize()) {
        PickerAppRow(
            name = app.name,
            iconUrl = app.iconUrl,
            checked = checked,
            onToggle = onAppToggle,
            modifier = Modifier.padding(horizontal = 16.dp),
            covered = covered,
            expandable = features.isNotEmpty(),
            expanded = expanded,
            onExpandToggle = onToggleExpand,
        )
        if (expanded && features.isNotEmpty()) {
            Column(modifier = Modifier.fillMaxWidth().background(AppColors.bg.surface).padding(start = 64.dp, end = 16.dp)) {
                features.forEach { f ->
                    Row(
                        modifier = Modifier.fillMaxWidth().height(RowHeight).singleClick { onFeatureToggle(f) },
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        ChildAppIcon(iconUrl = app.iconUrl, size = IconSize)
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = f.name, maxLines = 1, overflow = TextOverflow.Ellipsis, color = AppColors.text.primary, style = PolicyText.row)
                            // Shorts / Reels'ni Accessibility yopadi — faqat Qalqon rejimida
                            Text(
                                text = stringResource(Res.string.protection_shield_short),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                color = AppColors.text.accentWarning,
                                style = PolicyText.subtitle,
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        AppCheckbox(
                            checked = selectedFeatures.any { it.equals(f.key, ignoreCase = true) },
                            enabled = !covered,
                            covered = covered,
                            onCheckedChange = { onFeatureToggle(f) },
                        )
                    }
                }
            }
        }
    }
}

// ═══════════════════════════════════════════
//  Kategoriya
// ═══════════════════════════════════════════

/** Kategoriya kartasi: o'q · emoji · nom · son · katak; ochilsa ilovalar (faqat ma'lumot). */
@Composable
fun PickerCategoryCard(
    name: String,
    emoji: String,
    apps: List<InstalledApp>,
    selected: Boolean,
    expanded: Boolean,
    onToggleExpand: () -> Unit,
    onToggleSelect: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val rotation by animateFloatAsState(if (expanded) 180f else 0f, tween(300), label = "categoryArrow")
    Column(modifier = modifier.fillMaxWidth().animateContentSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(RowHeight)
                .padding(horizontal = 16.dp)
                .clickable(indication = null, interactionSource = null, onClick = onToggleExpand),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                painter = painterResource(Res.drawable.arrow_down_reg),
                contentDescription = null,
                modifier = Modifier.size(20.dp).rotate(rotation),
                tint = AppColors.icon.secondary,
            )
            Spacer(modifier = Modifier.width(16.dp))
            Box(
                modifier = Modifier.size(IconSize).clip(IconShape).background(AppColors.bg.surface),
                contentAlignment = Alignment.Center,
            ) {
                Text(text = emoji, modifier = Modifier.fillMaxWidth(), fontSize = 16.sp, lineHeight = 16.sp, textAlign = TextAlign.Center)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = name,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = AppColors.text.primary,
                style = PolicyText.row,
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = apps.size.toString(), color = AppColors.text.secondary, style = PolicyText.value)
            Spacer(modifier = Modifier.width(8.dp))
            AppCheckbox(checked = selected, onCheckedChange = { onToggleSelect() })
        }
        if (expanded) {
            Column(modifier = Modifier.fillMaxWidth().background(AppColors.bg.surface).padding(start = 48.dp, end = 16.dp)) {
                if (apps.isEmpty()) {
                    Text(
                        text = stringResource(Res.string.targets_no_apps),
                        style = PolicyText.hint,
                        color = AppColors.text.tertiary,
                        modifier = Modifier.padding(vertical = 16.dp),
                    )
                }
                apps.forEach { app ->
                    Row(modifier = Modifier.fillMaxWidth().height(RowHeight), verticalAlignment = Alignment.CenterVertically) {
                        ChildAppIcon(iconUrl = app.iconUrl, size = IconSize)
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = app.name,
                            modifier = Modifier.weight(1f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = AppColors.text.primary,
                            style = PolicyText.row,
                        )
                    }
                }
            }
        }
    }
}

// ═══════════════════════════════════════════
//  Sayt
// ═══════════════════════════════════════════

/** Sayt qatori: bosish — tanlash, uzoq bosish — tahrirlash/o'chirish (faqat o'zi qo'shganlari). */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PickerSiteRow(domain: String, checked: Boolean, onToggle: () -> Unit, onLongClick: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .combinedClickable(indication = null, interactionSource = null, onClick = onToggle, onLongClick = onLongClick),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(Res.drawable.global),
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = AppColors.icon.secondary,
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = domain,
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color = AppColors.text.primary,
            style = PolicyText.row,
        )
        Spacer(modifier = Modifier.width(10.dp))
        AppCheckbox(checked = checked, onCheckedChange = { onToggle() })
    }
}

/** "Sayt qo'shish" — saytlar ro'yxati tepasida (qator o'lchamida, yashil "+"). */
@Composable
fun PickerAddRow(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth().height(52.dp).singleClick { onClick() },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier.size(24.dp).clip(CircleShape).background(AppColors.bg.primary),
            contentAlignment = Alignment.Center,
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp), tint = AppColors.icon.inverse)
        }
        Spacer(modifier = Modifier.width(16.dp))
        Text(text = text, style = PolicyText.row, color = AppColors.text.accentEmphasis, maxLines = 1)
    }
}

/** Sayt manzili yoki kalit so'z. Qiymat dialog ichida; tekshiruv reducer'da. */
@Composable
fun SiteInputDialog(initial: String, error: SiteError?, onConfirm: (String) -> Unit, onDismiss: () -> Unit) {
    var value by remember(initial) { mutableStateOf(initial) }
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = AppColors.modal.primary),
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(ContainerPadding)) {
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = stringResource(Res.string.sayt_manzilini_kiriting),
                        color = AppColors.text.primary,
                        style = PolicyText.sheetTitle,
                        modifier = Modifier.weight(1f),
                    )
                    IconButton(
                        onClick = onDismiss,
                        colors = IconButtonDefaults.iconButtonColors(contentColor = AppColors.icon.secondary),
                        modifier = Modifier.size(30.dp),
                    ) {
                        Icon(painter = painterResource(Res.drawable.close_remove), contentDescription = null, modifier = Modifier.size(24.dp))
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
                CustomTextField(
                    label = "www.website.com",
                    value = value,
                    singleLine = true,
                    onValueChange = { value = it },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Uri,
                        capitalization = KeyboardCapitalization.None,
                        imeAction = ImeAction.Done,
                    ),
                )
                if (error != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = stringResource(
                            when (error) {
                                SiteError.INVALID -> Res.string.sayt_manzili_notogri
                                SiteError.DUPLICATE -> Res.string.bu_sayt_allaqachon_ro_yxatda
                            }
                        ),
                        color = AppColors.text.accentDanger,
                        style = PolicyText.hint,
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                CustomButtonNew(
                    text = stringResource(Res.string.saqlash),
                    onClick = { onConfirm(value) },
                    enabled = value.isNotBlank(),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

/** O'zi qo'shgan sayt: Tahrirlash / O'chirish. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SiteActionsSheet(onEdit: () -> Unit, onDelete: () -> Unit, onDismiss: () -> Unit) {
    ModalBottomSheet(
        sheetState = rememberModalBottomSheetState(),
        onDismissRequest = onDismiss,
        containerColor = Color.Transparent,
        dragHandle = {},
    ) {
        Column(modifier = Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = ContainerPadding)) {
            Card(
                shape = RoundedCornerShape(CardCornerRadius),
                colors = CardDefaults.cardColors(containerColor = AppColors.modal.primary),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(ContainerPadding), horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .padding(bottom = 15.dp)
                            .background(color = AppColors.border.secondary, shape = CircleShape)
                            .height(3.dp)
                            .width(36.dp),
                    )
                    SheetAction(isDelete = false, text = stringResource(Res.string.tahrirlash), onClick = onEdit)
                    SheetAction(isDelete = true, text = stringResource(Res.string.ochirish), onClick = onDelete)
                }
            }
        }
    }
}

@Composable
private fun SheetAction(isDelete: Boolean, text: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().height(48.dp).singleClick { onClick() },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(if (isDelete) Res.drawable.message_delete else Res.drawable.message_edit),
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = AppColors.icon.primary,
        )
        Spacer(modifier = Modifier.width(24.dp))
        Text(text = text, style = PolicyText.row, color = AppColors.text.primary)
    }
}

/** Bo'sh holat / "Topilmadi". */
@Composable
fun PickerEmpty(text: String, modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.TopCenter) {
        Text(text = text, style = PolicyText.empty, color = AppColors.text.tertiary, textAlign = TextAlign.Center)
    }
}
