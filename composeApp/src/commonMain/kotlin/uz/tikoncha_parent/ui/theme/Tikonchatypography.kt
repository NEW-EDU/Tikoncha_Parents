package uz.tikoncha_parent.ui.theme

import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.Font
import tikoncha_parents.composeapp.generated.resources.Res
import tikoncha_parents.composeapp.generated.resources.nunito_black
import tikoncha_parents.composeapp.generated.resources.nunito_bold
import tikoncha_parents.composeapp.generated.resources.nunito_extrabold
import tikoncha_parents.composeapp.generated.resources.nunito_medium
import tikoncha_parents.composeapp.generated.resources.nunito_regular
import tikoncha_parents.composeapp.generated.resources.nunito_semibold

/**
 * Ilovaning yagona shrifti — Nunito (Android va iOS'da bir xil; bola ilovasidagi SF Rounded'ga yaqin).
 * Barcha og'irliklar statik fayl, shuning uchun FontWeight to'g'ridan-to'g'ri ishlaydi.
 */
@Composable
fun NunitoFontFamily(): FontFamily = FontFamily(
    Font(Res.font.nunito_regular, FontWeight.Normal),
    Font(Res.font.nunito_medium, FontWeight.Medium),
    Font(Res.font.nunito_semibold, FontWeight.SemiBold),
    Font(Res.font.nunito_bold, FontWeight.Bold),
    Font(Res.font.nunito_extrabold, FontWeight.ExtraBold),
    Font(Res.font.nunito_black, FontWeight.Black),
)

/* ---------------------------
   TextStyle builder (Figma JSON)
   --------------------------- */

private fun textStyle(
    family: FontFamily,
    weight: FontWeight,
    size: Int,
    lineHeight: Int,
): TextStyle = TextStyle(
    fontFamily = family,
    fontWeight = weight,
    fontSize = size.sp,
    lineHeight = lineHeight.sp,
    letterSpacing = 0.sp,
)

/* ---------------------------
   Public API: default + weight variants
   --------------------------- */

@Immutable
data class TikonchaTypography(

    val material3: Typography,

    /* ========== DISPLAY (44/48, 36/44, 32/38, 28/34) ========== */
    val display2xl: TextStyle,
    val displayLg: TextStyle,
    val displayMd: TextStyle,
    val displaySm: TextStyle,

    val display2xlRegular: TextStyle,
    val display2xlMedium: TextStyle,
    val display2xlSemiBold: TextStyle,
    val displayLgRegular: TextStyle,
    val displayLgMedium: TextStyle,
    val displayLgSemiBold: TextStyle,
    val displayMdRegular: TextStyle,
    val displayMdMedium: TextStyle,
    val displayMdSemiBold: TextStyle,
    val displaySmRegular: TextStyle,
    val displaySmMedium: TextStyle,
    val displaySmSemiBold: TextStyle,

    /* ========== HEADLINE (28/34, 24/30, 20/24) ========== */
    val headlineLg: TextStyle,
    val headlineMd: TextStyle,
    val headlineSm: TextStyle,

    val headlineLgRegular: TextStyle,
    val headlineLgMedium: TextStyle,
    val headlineLgSemiBold: TextStyle,
    val headlineMdRegular: TextStyle,
    val headlineMdMedium: TextStyle,
    val headlineMdSemiBold: TextStyle,
    val headlineSmRegular: TextStyle,
    val headlineSmMedium: TextStyle,
    val headlineSmSemiBold: TextStyle,

    /* ========== TITLE (18/22, 16/20, 14/18) ========== */
    val titleLg: TextStyle,
    val titleMd: TextStyle,
    val titleSm: TextStyle,

    val titleLgRegular: TextStyle,
    val titleLgMedium: TextStyle,
    val titleLgSemiBold: TextStyle,
    val titleMdRegular: TextStyle,
    val titleMdMedium: TextStyle,
    val titleMdSemiBold: TextStyle,
    val titleSmRegular: TextStyle,
    val titleSmMedium: TextStyle,
    val titleSmSemiBold: TextStyle,

    /* ========== BODY (13/14, 12/14, 10/14) ========== */
    val bodyLg: TextStyle,
    val bodyMd: TextStyle,
    val bodySm: TextStyle,

    val bodyLgRegular: TextStyle,
    val bodyLgMedium: TextStyle,
    val bodyLgSemiBold: TextStyle,
    val bodyMdRegular: TextStyle,
    val bodyMdMedium: TextStyle,
    val bodyMdSemiBold: TextStyle,
    val bodySmRegular: TextStyle,
    val bodySmMedium: TextStyle,
    val bodySmSemiBold: TextStyle,

    /* ========== EMPHASIZED (20/30, 16/24, 14/22, 13/20, 12/20) ========== */
    val emphasizedXl: TextStyle,
    val emphasizedLg: TextStyle,
    val emphasizedMd: TextStyle,
    val emphasizedSm: TextStyle,
    val emphasizedXs: TextStyle,

    val emphasizedXlRegular: TextStyle,
    val emphasizedXlMedium: TextStyle,
    val emphasizedXlSemiBold: TextStyle,
    val emphasizedLgRegular: TextStyle,
    val emphasizedLgMedium: TextStyle,
    val emphasizedLgSemiBold: TextStyle,
    val emphasizedMdRegular: TextStyle,
    val emphasizedMdMedium: TextStyle,
    val emphasizedMdSemiBold: TextStyle,
    val emphasizedSmRegular: TextStyle,
    val emphasizedSmMedium: TextStyle,
    val emphasizedSmSemiBold: TextStyle,
    val emphasizedXsRegular: TextStyle,
    val emphasizedXsMedium: TextStyle,
    val emphasizedXsSemiBold: TextStyle,
)

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
fun createTypography(family: FontFamily): TikonchaTypography {
    fun ts(weight: FontWeight, size: Int, lineHeight: Int) = textStyle(family, weight, size, lineHeight)
    val r = FontWeight.Normal
    val m = FontWeight.Medium
    val s = FontWeight.SemiBold

    // DISPLAY  —  2xl: 44/48,  lg: 36/44,  md: 32/38,  sm: 28/34
    val d2xlR = ts(r, 44, 48); val d2xlM = ts(m, 44, 48); val d2xlS = ts(s, 44, 48)
    val dLgR  = ts(r, 36, 44); val dLgM  = ts(m, 36, 44); val dLgS  = ts(s, 36, 44)
    val dMdR  = ts(r, 32, 38); val dMdM  = ts(m, 32, 38); val dMdS  = ts(s, 32, 38)
    val dSmR  = ts(r, 28, 34); val dSmM  = ts(m, 28, 34); val dSmS  = ts(s, 28, 34)

    // HEADLINE  —  lg: 28/34,  md: 24/30,  sm: 20/24
    val hLgR = ts(r, 28, 34); val hLgM = ts(m, 28, 34); val hLgS = ts(s, 28, 34)
    val hMdR = ts(r, 24, 30); val hMdM = ts(m, 24, 30); val hMdS = ts(s, 24, 30)
    val hSmR = ts(r, 20, 24); val hSmM = ts(m, 20, 24); val hSmS = ts(s, 20, 24)

    // TITLE  —  lg: 18/22,  md: 16/20,  sm: 14/18
    val tLgR = ts(r, 18, 22); val tLgM = ts(m, 18, 22); val tLgS = ts(s, 18, 22)
    val tMdR = ts(r, 16, 20); val tMdM = ts(m, 16, 20); val tMdS = ts(s, 16, 20)
    val tSmR = ts(r, 14, 18); val tSmM = ts(m, 14, 18); val tSmS = ts(s, 14, 18)

    // BODY  —  lg: 13/14,  md: 12/14,  sm: 10/14
    val bLgR = ts(r, 13, 14); val bLgM = ts(m, 13, 14); val bLgS = ts(s, 13, 14)
    val bMdR = ts(r, 12, 14); val bMdM = ts(m, 12, 14); val bMdS = ts(s, 12, 14)
    val bSmR = ts(r, 10, 14); val bSmM = ts(m, 10, 14); val bSmS = ts(s, 10, 14)

    // EMPHASIZED  —  xl: 20/30,  lg: 16/24,  md: 14/22,  sm: 13/20,  xs: 12/20
    val eXlR = ts(r, 20, 30); val eXlM = ts(m, 20, 30); val eXlS = ts(s, 20, 30)
    val eLgR = ts(r, 16, 24); val eLgM = ts(m, 16, 24); val eLgS = ts(s, 16, 24)
    val eMdR = ts(r, 14, 22); val eMdM = ts(m, 14, 22); val eMdS = ts(s, 14, 22)
    val eSmR = ts(r, 13, 20); val eSmM = ts(m, 13, 20); val eSmS = ts(s, 13, 20)
    val eXsR = ts(r, 12, 20); val eXsM = ts(m, 12, 20); val eXsS = ts(s, 12, 20)

    val material = Typography(
        displayLarge  = d2xlR,
        displayMedium = dLgR,
        displaySmall  = dMdR,

        headlineLarge  = hLgR,
        headlineMedium = hMdR,
        headlineSmall  = hSmR,

        titleLarge  = tLgM,
        titleMedium = tMdM,
        titleSmall  = tSmM,

        bodyLarge  = bLgR,
        bodyMedium = bMdR,
        bodySmall  = bSmR,

        labelLarge  = eLgR,
        labelMedium = eMdR,
        labelSmall  = eSmR,

        // Material 3 Expressive — ta'kidlangan uslublar: o'sha o'lcham, og'irroq Nunito (sarlavhalar, faol holatlar)
        displayLargeEmphasized  = d2xlR.copy(fontWeight = FontWeight.ExtraBold),
        displayMediumEmphasized = dLgR.copy(fontWeight = FontWeight.ExtraBold),
        displaySmallEmphasized  = dMdR.copy(fontWeight = FontWeight.ExtraBold),
        headlineLargeEmphasized  = hLgR.copy(fontWeight = FontWeight.ExtraBold),
        headlineMediumEmphasized = hMdR.copy(fontWeight = FontWeight.ExtraBold),
        headlineSmallEmphasized  = hSmR.copy(fontWeight = FontWeight.ExtraBold),
        titleLargeEmphasized  = tLgM.copy(fontWeight = FontWeight.Bold),
        titleMediumEmphasized = tMdM.copy(fontWeight = FontWeight.Bold),
        titleSmallEmphasized  = tSmM.copy(fontWeight = FontWeight.Bold),
        bodyLargeEmphasized  = bLgR.copy(fontWeight = FontWeight.SemiBold),
        bodyMediumEmphasized = bMdR.copy(fontWeight = FontWeight.SemiBold),
        bodySmallEmphasized  = bSmR.copy(fontWeight = FontWeight.SemiBold),
        labelLargeEmphasized  = eLgR.copy(fontWeight = FontWeight.Bold),
        labelMediumEmphasized = eMdR.copy(fontWeight = FontWeight.Bold),
        labelSmallEmphasized  = eSmR.copy(fontWeight = FontWeight.Bold),
    )

    return TikonchaTypography(
        material3 = material,

        // DISPLAY — default: SemiBold
        display2xl = d2xlS, displayLg = dLgS, displayMd = dMdS, displaySm = dSmS,

        display2xlRegular = d2xlR, display2xlMedium = d2xlM, display2xlSemiBold = d2xlS,
        displayLgRegular  = dLgR,  displayLgMedium  = dLgM,  displayLgSemiBold  = dLgS,
        displayMdRegular  = dMdR,  displayMdMedium  = dMdM,  displayMdSemiBold  = dMdS,
        displaySmRegular  = dSmR,  displaySmMedium  = dSmM,  displaySmSemiBold  = dSmS,

        // HEADLINE — default: SemiBold
        headlineLg = hLgS, headlineMd = hMdS, headlineSm = hSmS,

        headlineLgRegular = hLgR, headlineLgMedium = hLgM, headlineLgSemiBold = hLgS,
        headlineMdRegular = hMdR, headlineMdMedium = hMdM, headlineMdSemiBold = hMdS,
        headlineSmRegular = hSmR, headlineSmMedium = hSmM, headlineSmSemiBold = hSmS,

        // TITLE — default: Medium
        titleLg = tLgM, titleMd = tMdM, titleSm = tSmM,

        titleLgRegular = tLgR, titleLgMedium = tLgM, titleLgSemiBold = tLgS,
        titleMdRegular = tMdR, titleMdMedium = tMdM, titleMdSemiBold = tMdS,
        titleSmRegular = tSmR, titleSmMedium = tSmM, titleSmSemiBold = tSmS,

        // BODY — default: Regular
        bodyLg = bLgR, bodyMd = bMdR, bodySm = bSmR,

        bodyLgRegular = bLgR, bodyLgMedium = bLgM, bodyLgSemiBold = bLgS,
        bodyMdRegular = bMdR, bodyMdMedium = bMdM, bodyMdSemiBold = bMdS,
        bodySmRegular = bSmR, bodySmMedium = bSmM, bodySmSemiBold = bSmS,

        // EMPHASIZED — default: Medium
        emphasizedXl = eXlM,
        emphasizedLg = eLgM,
        emphasizedMd = eMdM,
        emphasizedSm = eSmM,
        emphasizedXs = eXsM,

        emphasizedXlRegular = eXlR, emphasizedXlMedium = eXlM, emphasizedXlSemiBold = eXlS,
        emphasizedLgRegular = eLgR, emphasizedLgMedium = eLgM, emphasizedLgSemiBold = eLgS,
        emphasizedMdRegular = eMdR, emphasizedMdMedium = eMdM, emphasizedMdSemiBold = eMdS,
        emphasizedSmRegular = eSmR, emphasizedSmMedium = eSmM, emphasizedSmSemiBold = eSmS,
        emphasizedXsRegular = eXsR, emphasizedXsMedium = eXsM, emphasizedXsSemiBold = eXsS,
    )
}