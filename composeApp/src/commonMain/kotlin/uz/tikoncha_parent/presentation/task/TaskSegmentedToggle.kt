package uz.tikoncha_parent.presentation.task

import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.Role
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import tikoncha_parents.composeapp.generated.resources.*
import uz.tikoncha_parent.ui.theme.AppColors
import uz.tikoncha_parent.ui.theme.AppTypography
import uz.tikoncha_parent.ui.theme.ThemeMode
import uz.tikoncha_parent.ui.theme.TikonchaParentTheme

/**
 * Material 3 Expressive "connected button group": har bir variant — ToggleButton; tanlangani karamel to'ldiriladi va
 * shakli to'liq yumaloqqa o'tadi (morf), qo'shnilari ichki burchaklari bilan bir-biriga yopishadi.
 * Imzo avvalgidek ([options] dagi rasm hozircha ko'rsatilmaydi — avvalgi komponent ham ko'rsatmagan).
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TaskSegmentedToggle(
    options: List<Pair<String, Painter?>>,
    selectedIndex: Int,
    style: TextStyle = AppTypography.bodyLgMedium,
    onOptionSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = Color.Transparent,
    shape: Shape = RoundedCornerShape(100.dp)
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween),
    ) {
        options.forEachIndexed { index, (label) ->
            val checked = index == selectedIndex
            ToggleButton(
                checked = checked,
                onCheckedChange = { if (!checked) onOptionSelected(index) },
                modifier = Modifier
                    .weight(1f)
                    .semantics { role = Role.RadioButton },
                shapes = when (index) {
                    0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                    options.lastIndex -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                    else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
                },
                colors = ToggleButtonDefaults.toggleButtonColors(
                    // Tanlanmagan tomon oq kartada ham, krem fonda ham ko'rinsin — neytral tonal kulrang
                    containerColor = AppColors.section.secondary,
                    contentColor = AppColors.text.secondary,
                    checkedContainerColor = AppColors.button.primary,
                    checkedContentColor = AppColors.text.onPrimary,
                ),
                contentPadding = PaddingValues(horizontal = 12.dp),
            ) {
                Text(
                    text = label,
                    style = if (checked) style.copy(fontWeight = FontWeight.Bold) else style,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Preview
@Composable
private fun PRe() {
    TikonchaParentTheme(
        ThemeMode.DARK
    ){
        TaskSegmentedToggle(
            options = listOf(
                "Ertalabdan" to painterResource(Res.drawable.father_icon),
                "Tushlikdan" to painterResource(Res.drawable.mather_icon)
            ),
            selectedIndex = 0,
            onOptionSelected = {  },
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        )
    }
}
