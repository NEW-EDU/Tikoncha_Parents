package uz.tikoncha_parent.presentation.support

import uz.tikoncha_parent.ui.expressiveCard
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import tikoncha_parents.composeapp.generated.resources.Res
import tikoncha_parents.composeapp.generated.resources.biz_bilan_aloqa
import tikoncha_parents.composeapp.generated.resources.ic_instagram
import tikoncha_parents.composeapp.generated.resources.ic_whatsapp
import tikoncha_parents.composeapp.generated.resources.support_email
import tikoncha_parents.composeapp.generated.resources.support_sub
import tikoncha_parents.composeapp.generated.resources.telegram
import uz.tikoncha_parent.platform.openUrl
import uz.tikoncha_parent.presentation.profile.language.LanguagePrefs
import uz.tikoncha_parent.ui.theme.AppColors
import uz.tikoncha_parent.ui.theme.AppTypography

/**
 * Qo'llab-quvvatlash kanallari (hoglt.com sayti pastidagi rasmiy kontaktlar bilan bir xil).
 * O'zbek/rus tilida Telegram birinchi, boshqa tillarda — WhatsApp.
 */
private object SupportContacts {
    const val WHATSAPP_URL = "https://wa.me/998972707007"
    const val WHATSAPP_LABEL = "+998 97 270 70 07"
    const val TELEGRAM_URL = "https://t.me/tikoncha_support"
    const val TELEGRAM_LABEL = "@tikoncha_support"
    const val EMAIL = "info@hoglt.com"
    const val INSTAGRAM_URL = "https://ig.me/m/hoglt.app"
    const val INSTAGRAM_LABEL = "@hoglt.app"
}

private enum class Channel { WHATSAPP, TELEGRAM, EMAIL, INSTAGRAM }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupportSheet(onDismiss: () -> Unit) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val local = LanguagePrefs.loadOrDefault().languageCode.let { it == "uz" || it == "ru" }
    val order = if (local) {
        listOf(Channel.TELEGRAM, Channel.WHATSAPP, Channel.EMAIL, Channel.INSTAGRAM)
    } else {
        listOf(Channel.WHATSAPP, Channel.EMAIL, Channel.TELEGRAM, Channel.INSTAGRAM)
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = AppColors.bg.surface,
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .navigationBarsPadding()
                .padding(bottom = 16.dp)
        ) {
            Text(stringResource(Res.string.biz_bilan_aloqa), style = AppTypography.headlineSmSemiBold, color = AppColors.text.primary)
            Spacer(Modifier.size(4.dp))
            Text(stringResource(Res.string.support_sub), style = AppTypography.emphasizedSmMedium, color = AppColors.text.tertiary)
            Spacer(Modifier.size(16.dp))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                order.forEach { channel ->
                    when (channel) {
                        Channel.WHATSAPP -> ChannelRow(painterResource(Res.drawable.ic_whatsapp), "WhatsApp", SupportContacts.WHATSAPP_LABEL) {
                            openUrl(SupportContacts.WHATSAPP_URL); onDismiss()
                        }
                        Channel.TELEGRAM -> ChannelRow(painterResource(Res.drawable.telegram), "Telegram", SupportContacts.TELEGRAM_LABEL) {
                            openUrl(SupportContacts.TELEGRAM_URL); onDismiss()
                        }
                        Channel.EMAIL -> ChannelRow(rememberVectorPainter(Icons.Filled.Email), stringResource(Res.string.support_email), SupportContacts.EMAIL) {
                            openUrl("mailto:${SupportContacts.EMAIL}"); onDismiss()
                        }
                        Channel.INSTAGRAM -> ChannelRow(painterResource(Res.drawable.ic_instagram), "Instagram", SupportContacts.INSTAGRAM_LABEL) {
                            openUrl(SupportContacts.INSTAGRAM_URL); onDismiss()
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ChannelRow(icon: Painter, title: String, value: String, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .expressiveCard(color = AppColors.bg.page, shape = RoundedCornerShape(20.dp), onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).background(AppColors.bg.primaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            // Yagona aksent — barcha belgilar karamel rangda
            Icon(icon, contentDescription = null, tint = AppColors.icon.accentPrimary, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(title, style = AppTypography.titleMdSemiBold, color = AppColors.text.primary)
            Text(value, style = AppTypography.emphasizedSmMedium, color = AppColors.text.tertiary)
        }
    }
}
