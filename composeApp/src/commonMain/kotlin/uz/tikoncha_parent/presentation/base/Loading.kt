package uz.tikoncha_parent.presentation.base

import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LoadingIndicator
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import uz.tikoncha_parent.ui.theme.extendedColor


@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun Loading(
    show: Boolean,
) {
    // Tez javobda ko'rinmaydi (chaqnash yo'q) — qarang: rememberDelayedLoading.
    if (!rememberDelayedLoading(show)){
        return
    }



    Box(
        modifier = Modifier
            .size(60.dp)
            .padding(10.dp),
        contentAlignment = Alignment.Center,

        ){
        LoadingIndicator(
            color = MaterialTheme.extendedColor.primaryColor,
            modifier = Modifier
                .fillMaxSize()
        )
    }
}
