package uz.tikoncha_parent.presentation.add_child

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import qrscanner.CameraLens
import qrscanner.QrScanner
import tikoncha_parents.composeapp.generated.resources.Res
import tikoncha_parents.composeapp.generated.resources.add_child_scan_hint
import tikoncha_parents.composeapp.generated.resources.add_child_scan_no_code
import tikoncha_parents.composeapp.generated.resources.close_remove
import tikoncha_parents.composeapp.generated.resources.yopish
import uz.tikoncha_parent.ui.Haptics
import uz.tikoncha_parent.ui.theme.AppTypography

/**
 * Farzand ilovasi körsatgan QR matni: `TIKONCHA-LINK:<8 raqam>`.
 * Matndagi birinçi 8 xonali raqamlar ketma-ketligini qaytaradi; bölmasa null (begona QR).
 */
internal fun extractLinkCode(qrText: String): String? = Regex("\\d{8}").find(qrText)?.value

/**
 * Töliq ekranli QR skaner. Kod topilsa [onCode] bir marta çaqiriladi (maydon töldiriladi, ulaş tugmasini ota-ona özi bosadi).
 * Kamera ruxsatini kutubxona (qr-kit) özi söraydi; ruxsat berilmasa uning standart "ruxsat kerak" körinişi çiqadi.
 */
@Composable
internal fun LinkQrScannerDialog(onCode: (String) -> Unit, onDismiss: () -> Unit) {
    var delivered by remember { mutableStateOf(false) }
    var wrongQr by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        // Kamera ko'rinishi mavzudan qat'i nazar qora fonda — shuning uçun bu yerda oq/qora atayin qat'iy.
        Box(Modifier.fillMaxSize().background(Color.Black)) {
            QrScanner(
                modifier = Modifier.fillMaxSize(),
                flashlightOn = false,
                cameraLens = CameraLens.Back,
                openImagePicker = false,
                onCompletion = { text ->
                    if (!delivered) {
                        val code = extractLinkCode(text)
                        if (code != null) {
                            delivered = true
                            Haptics.success()
                            onCode(code)
                        } else {
                            wrongQr = true
                        }
                    }
                },
                imagePickerHandler = {},
                onFailure = { wrongQr = true },
            )

            Box(Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing).padding(16.dp)) {
                Surface(
                    onClick = onDismiss,
                    shape = CircleShape,
                    color = Color.Black.copy(alpha = 0.55f),
                    modifier = Modifier.align(Alignment.TopEnd).size(48.dp),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            painter = painterResource(Res.drawable.close_remove),
                            contentDescription = stringResource(Res.string.yopish),
                            tint = Color.White,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }
                Text(
                    text = stringResource(if (wrongQr) Res.string.add_child_scan_no_code else Res.string.add_child_scan_hint),
                    style = AppTypography.bodyLgSemiBold,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .background(Color.Black.copy(alpha = 0.55f), RoundedCornerShape(16.dp))
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                )
            }
        }
    }
}
