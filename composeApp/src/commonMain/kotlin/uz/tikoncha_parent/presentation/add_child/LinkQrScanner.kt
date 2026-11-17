package uz.tikoncha_parent.presentation.add_child

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import qrscanner.CameraLens
import qrscanner.QrScanner
import tikoncha_parents.composeapp.generated.resources.Res
import tikoncha_parents.composeapp.generated.resources.ac_step_show_code
import tikoncha_parents.composeapp.generated.resources.add_child_scan_hint
import tikoncha_parents.composeapp.generated.resources.add_child_scan_no_code
import uz.tikoncha_parent.ui.Haptics
import uz.tikoncha_parent.ui.theme.AppTypography

/**
 * Farzand ilovasi körsatgan QR matni: `TIKONCHA-LINK:<8 raqam>`.
 * Matndagi birinçi 8 xonali raqamlar ketma-ketligini qaytaradi; bölmasa null (begona QR).
 */
internal fun extractLinkCode(qrText: String): String? = Regex("\\d{8}").find(qrText)?.value

/**
 * "Farzand qöşiş" şu kamera bilan oçiladi: ota-ona farzand telefonidagi QR kodga qaratadi, xolos.
 * Kod topilsa [onCode] çaqiriladi (ekran darhol ulaydi); kamera kod körib turganda qayta-qayta çaqirilişi mumkin,
 * takrorlarni çaqiruvçi tomon özi süzadi. Kamera ruxsatini kutubxona (qr-kit) özi söraydi.
 */
@Composable
internal fun LinkQrScanner(
    isLinking: Boolean,
    onCode: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var wrongQr by remember { mutableStateOf(false) }
    val currentOnCode by rememberUpdatedState(onCode)

    // Kamera ko'rinishi mavzudan qat'i nazar qora fonda — shuning uçun bu yerda oq/qora atayin qat'iy.
    Box(modifier.clip(RoundedCornerShape(28.dp)).background(Color.Black)) {
        QrScanner(
            modifier = Modifier.fillMaxSize(),
            flashlightOn = false,
            cameraLens = CameraLens.Back,
            openImagePicker = false,
            onCompletion = { text ->
                val code = extractLinkCode(text)
                wrongQr = code == null
                if (code != null) currentOnCode(code)
            },
            imagePickerHandler = {},
            onFailure = { wrongQr = true },
            // qr-kit'ning öz ötkir burçakli kvadrati yaşiriladi — örniga pastdagi yumaloq ramka
            overlayColor = Color.Transparent,
            overlayBorderColor = Color.Transparent,
        )

        // Yumaloq ramka: atrofi xiralaşgan, örtasi oçiq
        Canvas(Modifier.matchParentSize()) {
            val side = 240.dp.toPx()
            val corner = CornerRadius(32.dp.toPx())
            val topLeft = Offset((size.width - side) / 2f, (size.height - side) / 2f - 48.dp.toPx())
            val frame = Path().apply {
                fillType = PathFillType.EvenOdd
                addRect(Rect(Offset.Zero, size))
                addRoundRect(RoundRect(Rect(topLeft, Size(side, side)), corner))
            }
            drawPath(frame, Color.Black.copy(alpha = 0.45f))
            drawRoundRect(
                color = Color.White,
                topLeft = topLeft,
                size = Size(side, side),
                cornerRadius = corner,
                style = Stroke(width = 4.dp.toPx()),
            )
        }

        if (isLinking) {
            Box(
                Modifier.matchParentSize().background(Color.Black.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(color = Color.White)
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(12.dp)
                .background(Color.Black.copy(alpha = 0.55f), RoundedCornerShape(18.dp))
                .padding(horizontal = 16.dp, vertical = 12.dp),
        ) {
            Text(
                text = stringResource(if (wrongQr) Res.string.add_child_scan_no_code else Res.string.add_child_scan_hint),
                style = AppTypography.bodyLgSemiBold,
                color = Color.White,
                textAlign = TextAlign.Center,
            )
            if (!wrongQr) {
                // Farzand ilovasida QR qayerda: Profil → Ota-ona qöşiş
                Text(
                    text = stringResource(Res.string.ac_step_show_code),
                    style = AppTypography.emphasizedSmMedium,
                    color = Color.White.copy(alpha = 0.75f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
        }
    }

    // Kod topilib ulaş boşlanganda bir marta titraş
    LaunchedEffect(isLinking) { if (isLinking) Haptics.success() }
}
