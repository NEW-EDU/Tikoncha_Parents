package uz.tikoncha_parent.presentation.base.island

import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Dp
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

/**
 * Bir qo'l uchun: kontent ekrandan qisqa bo'lsa — pastga (orol va bosh barmoq yoniga) tushadi, tepada bo'sh joy
 * qoladi; uzun bo'lsa — oddiy scroll. [modifier] ga balandlik beriladi (odatda `weight(1f)`).
 */
@Composable
fun BottomAnchoredColumn(
    modifier: Modifier = Modifier,
    scrollState: ScrollState = rememberScrollState(),
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    spacing: Dp = 0.dp,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    content: @Composable ColumnScope.() -> Unit,
) {
    BoxWithConstraints(modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(scrollState)
                .heightIn(min = maxHeight)
                .padding(contentPadding),
            verticalArrangement = Arrangement.spacedBy(spacing, Alignment.Bottom),
            horizontalAlignment = horizontalAlignment,
            content = content,
        )
    }
}
