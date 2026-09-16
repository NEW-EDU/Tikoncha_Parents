package uz.tikoncha_parent.presentation.base

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshState
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import uz.tikoncha_parent.ui.theme.AppColors
import androidx.compose.material3.pulltorefresh.PullToRefreshBox as Material3PullToRefreshBox

/**
 * Brend rangidagi "pastga tortib yangilash".
 *
 * Material3 ning standart indikatori ranglarni `colorScheme.surfaceContainerHigh` va
 * `colorScheme.onSurfaceVariant` dan oladi. Ilova mavzusida bu rollar berilmagan, shuning uchun
 * standart binafsha-kulrang palitra chiqadi. Bu yerda rang aniq beriladi: doira — karta foni,
 * o'q va aylana — brend to'q sariq rangi.
 *
 * Imzo Material3 dagi bilan bir xil, shuning uchun ekranlarda faqat import o'zgaradi.
 */
@Composable
fun PullToRefreshBox(
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    state: PullToRefreshState = rememberPullToRefreshState(),
    contentAlignment: Alignment = Alignment.TopStart,
    content: @Composable BoxScope.() -> Unit,
) {
    Material3PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
        modifier = modifier,
        state = state,
        contentAlignment = contentAlignment,
        indicator = {
            PullToRefreshDefaults.Indicator(
                modifier = Modifier.align(Alignment.TopCenter),
                state = state,
                isRefreshing = isRefreshing,
                containerColor = AppColors.bg.surface,
                color = AppColors.icon.accentPrimary,
            )
        },
        content = content,
    )
}