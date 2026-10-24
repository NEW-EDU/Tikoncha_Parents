package uz.tikoncha_parent.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import uz.tikoncha_parent.data.mapper.ExtendedColors
import uz.tikoncha_parent.platform.SystemBarTheme
import uz.tikoncha_parent.ui.BackgroundColor
import uz.tikoncha_parent.ui.BorderColor
import uz.tikoncha_parent.ui.CardColors
import uz.tikoncha_parent.ui.DarkBackgroundColor
import uz.tikoncha_parent.ui.DarkButtonBackgroundColors
import uz.tikoncha_parent.ui.DarkCardColors
import uz.tikoncha_parent.ui.DarkGrayColor
import uz.tikoncha_parent.ui.DarkTextColor
import uz.tikoncha_parent.ui.DisabledBgDark
import uz.tikoncha_parent.ui.DisabledBgLight
import uz.tikoncha_parent.ui.DisabledContentDark
import uz.tikoncha_parent.ui.DisabledContentLight
import uz.tikoncha_parent.ui.EditTextBorder
import uz.tikoncha_parent.ui.HintTextColor
import uz.tikoncha_parent.ui.NewBackgroundColor
import uz.tikoncha_parent.ui.PrimaryAlphaColor
import uz.tikoncha_parent.ui.PrimaryColor
import uz.tikoncha_parent.ui.ShadowColorDark
import uz.tikoncha_parent.ui.ShadowColorLight
import uz.tikoncha_parent.ui.TextColor
import uz.tikoncha_parent.ui.TitleDarkColor
import uz.tikoncha_parent.ui.TitleLightColor
import uz.tikoncha_parent.ui.TonalButtonContainerColor



/*
 * Material 3 Expressive rang sxemasi — brend karamel (#C3955B) urug'idan Google'ning material-color-utilities
 * (SchemeFidelity) bilan hosil qilingan tonal palitra. Avval faqat `primary` berilgan edi, shuning uchun
 * standart komponentlar (switch, sheet, menyu, sana tanlagich, tonal konteynerlar) Material'ning binafsha
 * ranglariga tushib qolardi. Yagona aksent qoidasi: tertiary ham iliq (secondary oilasi), boshqa rang yo'q.
 * `primary` — ilova tugmalaridagi brend karamel, shunda switch/checkbox/slider tugmalar bilan bir xil.
 */
private val LightColorScheme = lightColorScheme(
    primary = Color(0xFFC3955B),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFFFDDB7),
    onPrimaryContainer = Color(0xFF4C2E00),
    inversePrimary = Color(0xFFF0BE80),
    secondary = Color(0xFF6F5B43),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFF7DBBC),
    onSecondaryContainer = Color(0xFF574330),
    tertiary = Color(0xFF6F5B43),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFF7DBBC),
    onTertiaryContainer = Color(0xFF574330),
    background = Color(0xFFF3EADE),
    onBackground = Color(0xFF1F1B17),
    surface = Color(0xFFFFF8F4),
    onSurface = Color(0xFF1F1B17),
    surfaceVariant = Color(0xFFF0E0D0),
    onSurfaceVariant = Color(0xFF4F453A),
    surfaceTint = Color(0xFF7D5723),
    inverseSurface = Color(0xFF34302B),
    inverseOnSurface = Color(0xFFF9EFE7),
    error = Color(0xFFBA1A1A),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF93000A),
    outline = Color(0xFF817568),
    outlineVariant = Color(0xFFD3C4B5),
    scrim = Color(0xFF000000),
    surfaceBright = Color(0xFFFFF8F4),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFFCF2EA),
    surfaceContainer = Color(0xFFF6ECE5),
    surfaceContainerHigh = Color(0xFFF0E6DF),
    surfaceContainerHighest = Color(0xFFEAE1D9),
    surfaceDim = Color(0xFFE2D8D1),
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFC3955B),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFF62400D),
    onPrimaryContainer = Color(0xFFFFDDB7),
    inversePrimary = Color(0xFF7D5723),
    secondary = Color(0xFFDDC2A4),
    onSecondary = Color(0xFF3E2D18),
    secondaryContainer = Color(0xFF59462F),
    onSecondaryContainer = Color(0xFFF7DBBC),
    tertiary = Color(0xFFDDC2A4),
    onTertiary = Color(0xFF3E2D18),
    tertiaryContainer = Color(0xFF59462F),
    onTertiaryContainer = Color(0xFFF7DBBC),
    background = Color(0xFF010D01),
    onBackground = Color(0xFFEAE1D9),
    surface = Color(0xFF17130F),
    onSurface = Color(0xFFEAE1D9),
    surfaceVariant = Color(0xFF4F453A),
    onSurfaceVariant = Color(0xFFD3C4B5),
    surfaceTint = Color(0xFFF0BE80),
    inverseSurface = Color(0xFFEAE1D9),
    inverseOnSurface = Color(0xFF34302B),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    outline = Color(0xFF9C8E81),
    outlineVariant = Color(0xFF4F453A),
    scrim = Color(0xFF000000),
    surfaceBright = Color(0xFF3D3833),
    surfaceContainerLowest = Color(0xFF110E0A),
    surfaceContainerLow = Color(0xFF1F1B17),
    surfaceContainer = Color(0xFF231F1A),
    surfaceContainerHigh = Color(0xFF2E2924),
    surfaceContainerHighest = Color(0xFF39342F),
    surfaceDim = Color(0xFF17130F),
)


private val LightExtendedColorScheme = ExtendedColors(
    primaryColor = PrimaryColor,
    primaryAlphaColor = PrimaryColor,
    backgroundColor = NewBackgroundColor,
    cardColor = CardColors,
    onBackgroundColor = TextColor,
//    hintColor = HintTextColor,
    hintColor = TitleLightColor,
    textColor = TextColor,
    borderColor = BorderColor,
    buttonMenuColor = TextColor,
    shadowColor = ShadowColorLight,
    shadowLightColor = Color.White,
    buttonColor = CardColors,
    tonalButtonColor = TonalButtonContainerColor,
    disabledBgColor = DisabledBgLight,
    disabledContentColor = DisabledContentLight,
    titleColor = TitleLightColor
)
private val DarkExtendedColorScheme = ExtendedColors(
    primaryColor = PrimaryColor,
    primaryAlphaColor = PrimaryAlphaColor,
    backgroundColor = DarkBackgroundColor,
    cardColor = DarkCardColors,
    onBackgroundColor = DarkTextColor,
//    hintColor = HintTextColor,
    hintColor = TitleDarkColor,
    textColor = DarkTextColor,
    borderColor = EditTextBorder,
    buttonMenuColor = HintTextColor,
    shadowColor = ShadowColorDark,
    shadowLightColor = PrimaryColor.copy(alpha = 0.1f),
    buttonColor = DarkButtonBackgroundColors,
    tonalButtonColor = DarkCardColors,
    disabledBgColor = DisabledBgDark,
    disabledContentColor = DisabledContentDark,
    titleColor = TitleDarkColor
)

val MaterialTheme.extendedColor: ExtendedColors
    @Composable get() = LocalExtendedColors.current

private val TikonchaLocalExtendedColors = staticCompositionLocalOf<TikonchaExtendedColors> {
    TikonchaParentLightExtendedColors
}

private val LocalTikonchaTypography = staticCompositionLocalOf<TikonchaTypography> {
    error("TikonchaTypography not provided")
}

val AppColors @Composable get() = TikonchaLocalExtendedColors.current
val AppTypography @Composable get() = LocalTikonchaTypography.current

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TikonchaParentTheme(
    mode: ThemeMode = ThemeMode.DARK,
    content: @Composable () -> Unit
) {
    val isDark = when (mode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.DARK   -> true
        ThemeMode.LIGHT  -> false
    }

    val colorScheme = when {
        isDark -> DarkColorScheme
        else -> LightColorScheme
    }
    val fontFamily = NunitoFontFamily()
    val typographyPack = remember(fontFamily) { createTypography(fontFamily) }

    val extendedColor = if (isDark) DarkExtendedColorScheme else LightExtendedColorScheme

    val tikonchaColors = if (isDark) {
        TikonchaParentDarkExtendedColors
    } else {
        TikonchaParentLightExtendedColors
    }


    CompositionLocalProvider(
        LocalExtendedColors provides extendedColor,
        TikonchaLocalExtendedColors provides tikonchaColors,
        LocalTikonchaTypography provides typographyPack,
    ) {
        // Material 3 Expressive: prujinali harakat (MotionScheme.expressive — fast 0.6/800, default 0.8/380,
        // slow 0.8/200), kengaytirilgan burchak shkalasi (4…48 dp, full) va ta'kidlangan (emphasized) matn uslublari.
        MaterialExpressiveTheme(
            colorScheme = colorScheme,
            motionScheme = MotionScheme.expressive(),
            shapes = Shapes(),
            typography = typographyPack.material3,
        ) {
            val inPreview = androidx.compose.ui.platform.LocalInspectionMode.current

            if (!inPreview) {
                SystemBarTheme.applyIconStyle(isDark = isDark || SystemBarOverride.lightIconScreens > 0)
            }
            content()
        }
    }
}
