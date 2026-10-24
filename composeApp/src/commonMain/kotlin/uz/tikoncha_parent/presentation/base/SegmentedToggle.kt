package uz.tikoncha_parent.presentation.base

import uz.tikoncha_parent.ui.Haptics
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.unit.dp
import uz.tikoncha_parent.ui.*
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import tikoncha_parents.composeapp.generated.resources.*
import uz.tikoncha_parent.ui.theme.AppColors
import uz.tikoncha_parent.ui.theme.AppTypography
import uz.tikoncha_parent.ui.theme.ThemeMode
import uz.tikoncha_parent.ui.theme.TikonchaParentTheme
import uz.tikoncha_parent.ui.theme.extendedColor

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SegmentedToggle(
    options: List<Pair<String, Painter?>>,
    selectedIndex: Int,
    style: TextStyle = AppTypography.titleSmSemiBold,
    onOptionSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = AppColors.bg.surface,
    /** M3 Expressive: to'liq yumaloq konteyner va indikator. */
    shape: Shape = CircleShape
) {

    val itemCount = options.size
    val itemWidth = remember { mutableStateOf(0.dp) }

    val density = LocalDensity.current

    Box(
        modifier = modifier
            .height(TextFieldHeight)
            .background(containerColor, shape)
            .padding(4.dp)
            .onGloballyPositioned { layoutCoordinates ->
                val totalWidth = layoutCoordinates.size.width

                itemWidth.value = with(density) {
                    (totalWidth.toDp() / itemCount)
                }
            }
    ) {
        // Sliding background indicator
        // M3 Expressive: indikator mavzudagi prujina bilan siljiydi (default spatial: 0.8 / 380 — yengil "sakrash")
        val indicatorOffset by animateDpAsState(
            targetValue = itemWidth.value * selectedIndex,
            animationSpec = MaterialTheme.motionScheme.defaultSpatialSpec(),
            label = "IndicatorOffset"
        )

        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(itemWidth.value)
                .offset(x = indicatorOffset)
                .clip(shape = shape)
                .background(PrimaryColor)
        )

        Row(
            modifier = Modifier.fillMaxSize()
        ) {
            options.forEachIndexed { index, (label, icon) ->
                val isSelected = index == selectedIndex

                val contentColor by animateColorAsState(
                    targetValue = if (isSelected) OnPrimaryColor else MaterialTheme.extendedColor.hintColor,
                    label = "ContentColor"
                )

                val iconColor by animateColorAsState(
                    targetValue = if (isSelected) OnPrimaryColor else PrimaryColor,
                    label = "IconColor"
                )

                Row(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) {
                            Haptics.tick()
                            onOptionSelected(index)
                        },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    if (icon != null) {
                        Icon(
                            painter = icon,
                            contentDescription = label,
                            tint = iconColor,
                            modifier = Modifier.size(TextFieldIconSize)
                        )
                        Spacer(modifier = Modifier.width(TextFieldInnerPadding))
                    }

                    Text(
                        text = label,
                        color = contentColor,
                        style = style
                    )
                }
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
        SegmentedToggle(
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
