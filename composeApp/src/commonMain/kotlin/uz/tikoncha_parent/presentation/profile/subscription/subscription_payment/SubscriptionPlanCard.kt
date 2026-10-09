package uz.tikoncha_parent.presentation.profile.subscription.subscription_payment

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import uz.tikoncha_parent.presentation.profile.subscription.autopay.components.CornerCheck
import uz.tikoncha_parent.presentation.profile.subscription.autopay.components.formatSum
import uz.tikoncha_parent.ui.theme.AppColors
import uz.tikoncha_parent.ui.theme.AppTypography

/** Tarif ekrani gradientining kartochkalar turgan joyidagi rangi — burchak belgisining halqasi shu rangda. */
internal val PaywallRing = Color(0xFF8E6A3F)
private val GoldText = Color(0xFF7A5414)

/**
 * Tarif kartasi (maket P1): chapda nom va izoh, o'ngda narx va davr. Tanlangan — oq 2 dp chegara va
 * burchakdagi belgi (radio o'rniga, Student bilan bir xil — egasining qarori, 2026-10-08).
 */
@Composable
fun SubscriptionPlanCard(
    title: String,
    subtitle: String?,
    price: Int,
    per: String,
    badgeText: String?,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) Color.White else Color.White.copy(alpha = 0.18f),
        animationSpec = tween(180),
        label = "border",
    )
    val shape = RoundedCornerShape(20.dp)
    val muted = AppColors.text.inverse.copy(alpha = 0.72f)

    Box(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(shape)
                .background(Color.White.copy(alpha = 0.12f))
                .border(if (isSelected) 2.dp else 1.dp, borderColor, shape)
                .selectable(selected = isSelected, onClick = onClick, role = Role.RadioButton)
                .padding(horizontal = 16.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(text = title, style = AppTypography.titleMdSemiBold, color = AppColors.text.inverse, maxLines = 1)
                if (subtitle != null) Text(text = subtitle, style = AppTypography.bodyMdMedium, color = muted)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(text = formatSum(price), style = AppTypography.titleLgSemiBold, color = AppColors.text.inverse, maxLines = 1)
                Text(text = per, style = AppTypography.bodyMdMedium, color = muted, maxLines = 1, textAlign = TextAlign.End)
            }
        }

        if (badgeText != null) {
            Text(
                text = badgeText,
                style = AppTypography.bodySmSemiBold,
                color = GoldText,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = 16.dp, y = (-11).dp)
                    .background(Color.White, RoundedCornerShape(10.dp))
                    .padding(horizontal = 10.dp, vertical = 4.dp),
            )
        }
        if (isSelected) {
            CornerCheck(
                Modifier.align(Alignment.TopEnd).offset(x = 8.dp, y = (-8).dp),
                ringColor = PaywallRing, fill = Color.White, tick = GoldText,
            )
        }
    }
}
