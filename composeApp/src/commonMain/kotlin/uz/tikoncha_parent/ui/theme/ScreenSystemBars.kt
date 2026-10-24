package uz.tikoncha_parent.ui.theme

import androidx.compose.ui.draw.drawBehind
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import uz.tikoncha_parent.presentation.base.island.LocalIsland
import uz.tikoncha_parent.presentation.base.island.islandInset
import uz.tikoncha_parent.presentation.base.island.islandReservedFromBottom
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.navigationBars

@Stable
class ScreenSystemBarsState(val modifier: Modifier)

@Composable
fun rememberScreenSystemBars(
    statusBarColor: Color = AppColors.bg.surface,
    navigationBarColor: Color = AppColors.bg.surface,
    hasStatusBarPadding: Boolean = true,
    hasNavigationBarPadding: Boolean = true
): ScreenSystemBarsState {

    // Pastki orol (BottomIsland) bo'lsa — kontent uning ustida tugaydi (orol ekran fonining ustida suzadi).
    val island = LocalIsland.current
    val islandReserved = islandReservedFromBottom()
    val navBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val modifier = when {
        hasStatusBarPadding && hasNavigationBarPadding ->
            Modifier
                // Orol bo'lsa pastki zona ekran fonining o'zi — kontentning yumaloq pastki burchaklari orasidan
                // boshqa rang (eski pastki panel rangi) ko'rinmasin
                .drawBehind { drawRect(if (island?.active == true) statusBarColor else navigationBarColor) }
                .navigationBarsPadding()
                .islandInset(island, islandReserved, navBottom)
                .background(statusBarColor)
                .statusBarsPadding()

        hasStatusBarPadding ->
            Modifier
                .background(statusBarColor)
                .statusBarsPadding()

        hasNavigationBarPadding ->
            Modifier
                .background(navigationBarColor)
                .navigationBarsPadding()
                .islandInset(island, islandReserved, navBottom)

        else -> Modifier
    }

    return ScreenSystemBarsState(modifier = modifier)

}