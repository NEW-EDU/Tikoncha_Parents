package uz.tikoncha_parent.presentation.base

import uz.tikoncha_parent.presentation.base.island.ChildPickerSpec
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.Spacer
import uz.tikoncha_parent.presentation.base.island.ProvideIsland
import uz.tikoncha_parent.presentation.base.island.IslandSpec
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import uz.tikoncha_parent.ui.*
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import tikoncha_parents.composeapp.generated.resources.Res
import tikoncha_parents.composeapp.generated.resources.*
import uz.tikoncha_parent.ui.theme.AppColors
import uz.tikoncha_parent.ui.theme.AppTypography
import uz.tikoncha_parent.ui.theme.ThemeMode
import uz.tikoncha_parent.ui.theme.TikonchaParentTheme
import uz.tikoncha_parent.ui.theme.extendedColor

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun CustomHeader(
    modifier: Modifier = Modifier,
    title: String = "",
    fonWeight: FontWeight = FontWeight.W500,
    showBackButton: Boolean = false,
    onBackClick: () -> Unit = {},
    trailingIcon: (@Composable () -> Unit)? = null,
    /** Farzand tanlagich — orolda, Bosh sahifadagi bilan bir xil (ro'yxat yuqoriga ochiladi). */
    childPicker: ChildPickerSpec? = null,
) {
    // Bir qo'l bilan ishlatish: sarlavha, orqaga, amallar va farzand tanlagich — hammasi pastki orolda (BottomIsland).
    // Tepada faqat kichik bo'shliq qoladi — joy kontentga.
    val currentBack by rememberUpdatedState(onBackClick)
    val currentTrailing by rememberUpdatedState(trailingIcon)
    val currentPicker by rememberUpdatedState(childPicker)
    val hasTrailing = trailingIcon != null
    val hasPicker = childPicker != null
    val spec = remember(title, showBackButton, hasTrailing, hasPicker) {
        IslandSpec(
            title = title.takeIf { it.isNotBlank() },
            onBack = if (showBackButton) ({ currentBack() }) else null,
            picker = if (hasPicker) ({ currentPicker }) else null,
            actions = if (hasTrailing) ({ currentTrailing?.invoke() }) else null,
        )
    }
    ProvideIsland(spec)
    Spacer(modifier.height(8.dp))
}

@Preview
@Composable
private fun Preview() {
    TikonchaParentTheme(
        ThemeMode.LIGHT
    ){
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
        ) {
            CustomHeader(
                title = "Title",
                showBackButton = true
            )
        }
    }
}