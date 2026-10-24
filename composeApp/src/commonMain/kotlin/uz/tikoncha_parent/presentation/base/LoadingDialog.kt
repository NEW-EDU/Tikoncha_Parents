package uz.tikoncha_parent.presentation.base

import uz.tikoncha_parent.ui.theme.AppColors
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ContainedLoadingIndicator
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import uz.tikoncha_parent.ui.theme.extendedColor


@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun LoadingDialog(
    show: Boolean,
) {
    // Tez tugaydigan so'rovlarda dialog bir lahza "chaqnab" o'tmasin: 250 ms dan keyin chiqadi,
    // chiqqan bo'lsa kamida 400 ms turadi.
    val visible = rememberDelayedLoading(show, delayMillis = 250L, minShowMillis = 400L)
    if (!visible) {
        return
    }



    Dialog(
        onDismissRequest = {}
    ) {
        // Material 3 Expressive: shakli o'zgarib turadigan yuklanish ko'rsatkichi (aylanma spinner o'rniga)
        ContainedLoadingIndicator(
            modifier = Modifier.size(84.dp),
            containerColor = AppColors.bg.surface,
            indicatorColor = AppColors.icon.accentPrimary,
            containerShape = MaterialTheme.shapes.extraLarge,
        )
    }
}
