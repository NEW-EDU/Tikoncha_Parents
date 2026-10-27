// commonMain/presentation/base/OfflineBanner.kt
package uz.tikoncha_parent.presentation.base

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.ktor.client.HttpClient
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.koinInject
import tikoncha_parents.composeapp.generated.resources.Res
import tikoncha_parents.composeapp.generated.resources.offline_pending_n
import tikoncha_parents.composeapp.generated.resources.offline_saved_data
import tikoncha_parents.composeapp.generated.resources.offline_sending_n
import uz.tikoncha_parent.data.outbox.ParentOutbox
import uz.tikoncha_parent.data.remote.ServerReachability
import uz.tikoncha_parent.domain.repository.SafetyRepository
import uz.tikoncha_parent.domain.repository.policy.PolicyRepository
import uz.tikoncha_parent.ui.theme.AppColors
import uz.tikoncha_parent.ui.theme.AppTypography
import uz.tikoncha_parent.ui.theme.ThemeMode
import uz.tikoncha_parent.ui.theme.TikonchaParentTheme

/**
 * Offline-first: server yetib bölmasa ham ilova keşdagi ma'lumot bilan ishlayveradi — xato oynasi emas, kiçik
 * xotirjam belgi. Internet qaytişi bilan navbatdagi özgarişlar ([ParentOutbox]) yuboriladi.
 */
@Composable
fun OfflineLayer() {
    val online by ServerReachability.online.collectAsState()
    val ops by ParentOutbox.ops.collectAsState()
    val pending = ParentOutbox.current().size
    val client: HttpClient = koinInject()
    val policies: PolicyRepository = koinInject()
    val safety: SafetyRepository = koinInject()

    // Offline — /health'ni vaqti-vaqti bilan tekşiradi; online bölişi bilan navbat yuboriladi
    LaunchedEffect(online) {
        if (!online) ServerReachability.probeUntilOnline(client)
    }
    LaunchedEffect(online, ops.size) {
        if (online && pending > 0) {
            runCatching { policies.flushOutbox() }
            runCatching { safety.flushOutbox() }
        }
    }

    Box(Modifier.fillMaxSize()) {
        OfflinePill(
            online = online,
            pending = pending,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(bottom = 84.dp),
        )
    }
}

@Composable
fun OfflinePill(online: Boolean, pending: Int, modifier: Modifier = Modifier) {
    val text = when {
        !online && pending > 0 -> stringResource(Res.string.offline_pending_n, pending)
        !online -> stringResource(Res.string.offline_saved_data)
        pending > 0 -> stringResource(Res.string.offline_sending_n, pending)
        else -> null
    }
    AnimatedVisibility(
        visible = text != null,
        enter = fadeIn() + slideInVertically { it / 2 },
        exit = fadeOut() + slideOutVertically { it / 2 },
        modifier = modifier,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .background(AppColors.bg.elevated, CircleShape)
                .padding(horizontal = 14.dp, vertical = 8.dp),
        ) {
            Box(
                Modifier
                    .size(7.dp)
                    .background(if (online) AppColors.text.accentSuccess else AppColors.text.tertiary, CircleShape)
            )
            Spacer(Modifier.width(8.dp))
            Text(text = text.orEmpty(), style = AppTypography.bodySmMedium, color = AppColors.text.secondary)
        }
    }
}

@Preview
@Composable
private fun Preview() {
    TikonchaParentTheme(ThemeMode.DARK) {
        OfflinePill(online = false, pending = 2)
    }
}
