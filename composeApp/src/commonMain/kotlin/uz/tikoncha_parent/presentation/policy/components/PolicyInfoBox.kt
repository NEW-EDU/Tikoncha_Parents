package uz.tikoncha_parent.presentation.policy.components

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import uz.tikoncha_parent.ui.theme.AppColors
import uz.tikoncha_parent.presentation.base.haptics.rememberAppHaptics

/**
 * Izoh qutisi (Student `PolicyInfoBox`): tanlangan shart va nishonlardan keyin "endi nima
 * bo'ladi" — oddiy gaplar bilan. Boshida bitta qator (sarlavha yoki birinchi gap); bosilsa
 * to'liq ochiladi. Hammasi bir qatorga sig'sa — bosilmaydi, o'q yo'q.
 */
@Composable
fun PolicyInfoBox(
    lines: List<String>,
    modifier: Modifier = Modifier,
    title: String? = null,
) {
    if (lines.isEmpty() && title == null) return
    var expanded by rememberSaveable { mutableStateOf(false) }
    val haptics = rememberAppHaptics()
    var headOverflows by remember { mutableStateOf(false) }

    val head = title ?: lines.first()
    val rest = if (title != null) lines else lines.drop(1)
    val expandable = expanded || rest.isNotEmpty() || headOverflows
    val arrow by animateFloatAsState(if (expanded) 180f else 0f, label = "infoArrow")

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(AppColors.bg.primary.copy(alpha = 0.12f))
            // Ripple yo'q: blok ochilayotganda u eski o'lchamdan cho'zilib xunuk ko'rinardi — o'q va tik yetarli
            .then(
                if (expandable) Modifier.clickable(interactionSource = null, indication = null, role = Role.Button) {
                    haptics.tick()
                    expanded = !expanded
                } else Modifier
            )
            .animateContentSize()
            .padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Icon(
            imageVector = Icons.Outlined.Info,
            contentDescription = null,
            modifier = Modifier.padding(top = 1.dp).size(18.dp),
            tint = AppColors.icon.accentPrimary,
        )
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = head,
                style = if (title != null) PolicyText.bannerStrong else PolicyText.infoStrong,
                color = if (title != null) AppColors.text.accentEmphasis else AppColors.text.primary,
                maxLines = if (expanded) Int.MAX_VALUE else 1,
                overflow = TextOverflow.Ellipsis,
                onTextLayout = { if (!expanded) headOverflows = it.hasVisualOverflow },
            )
            if (expanded) {
                rest.forEach { line -> Text(text = line, style = PolicyText.info, color = AppColors.text.primary) }
            }
        }
        if (expandable) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                modifier = Modifier.size(20.dp).rotate(arrow),
                tint = AppColors.icon.secondary,
            )
        }
    }
}
