package uz.tikoncha_parent.presentation.policy.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
import uz.tikoncha_parent.ui.theme.AppTypography

/**
 * Jadval (policy) ekranlarining yagona shrift shkalasi — Student ilovasidagi `PolicyText`
 * bilan AYNAN bir xil (ikkala ilova bir xil ko'rinsin). Loyihadagi `body*` rollari
 * policy ekranlarida ishlatilmaydi; har joy shu obyekt orqali oladi.
 */
object PolicyText {
    /** Bo'lim sarlavhasi: "Qachon", "Nimalar yopiladi", "Limit". */
    val section: TextStyle @Composable get() = AppTypography.titleLgSemiBold

    /** Ro'yxatdagi bo'lim sarlavhasi: "Tezkor bloklar". */
    val listSection: TextStyle @Composable get() = AppTypography.titleMdSemiBold

    /** Ro'yxat kartasining nomi: jadval, shablon, Kontent himoya. */
    val cardTitle: TextStyle @Composable get() = AppTypography.titleLgSemiBold

    /** Karta va qator izohi — 12sp / 16sp qator (ikki qatorda oraliq katta bo'lmasin). */
    val subtitle: TextStyle @Composable get() = AppTypography.emphasizedXsMedium.copy(lineHeight = 16.sp)

    /** Qator matni: sozlama, ilova, kategoriya, sayt, radio. */
    val row: TextStyle @Composable get() = AppTypography.titleMdMedium

    /** Qator matni — urg'uli (radius qiymati, xarita sarlavhasi). */
    val rowStrong: TextStyle @Composable get() = AppTypography.titleMdSemiBold

    /** Qator o'ngidagi qiymat yoki son: "Har kuni", "3". */
    val value: TextStyle @Composable get() = AppTypography.titleSmMedium

    /** Tab, hafta kuni chipi. */
    val tab: TextStyle @Composable get() = AppTypography.titleSmSemiBold

    /** Tez tanlov chipi: "22:00 – 07:00", "2 soat". */
    val chip: TextStyle @Composable get() = AppTypography.emphasizedSmSemiBold

    /** Katta qiymat: soat, limit (Student'da 32sp — Parent'ning displaySm 28, shuning uchun aniq beriladi). */
    val hero: TextStyle @Composable get() = AppTypography.displaySmSemiBold.copy(fontSize = 32.sp, lineHeight = 38.sp)

    /** Katta qiymat — uzun matn uchun ("2 soat 30 daqiqa"). */
    val heroCompact: TextStyle @Composable get() = AppTypography.headlineLgSemiBold

    /** Katta qiymat ustidagi yorliq: "Boshlanadi", "Kuniga". */
    val heroLabel: TextStyle @Composable get() = AppTypography.titleSmMedium

    /** Banner matni: "Hozir amalda: …". */
    val banner: TextStyle @Composable get() = AppTypography.titleSmMedium

    /** Urg'uli banner: "To'xtatilgan · 14:30 da qaytadi", "Davom ettirish". */
    val bannerStrong: TextStyle @Composable get() = AppTypography.titleSmSemiBold

    /** Izoh qutisi — asosiy gap. */
    val infoStrong: TextStyle @Composable get() = AppTypography.emphasizedMdMedium.copy(lineHeight = 20.sp)

    /** Izoh qutisi — qo'shimcha gaplar. */
    val info: TextStyle @Composable get() = AppTypography.emphasizedSmRegular.copy(lineHeight = 18.sp)

    /** Ekran ostidagi izoh, bo'lim yonidagi "ixtiyoriy". */
    val hint: TextStyle @Composable get() = AppTypography.emphasizedXsRegular

    /** "3 ta yoqiq" — ro'yxat tepasidagi hisob. */
    val count: TextStyle @Composable get() = AppTypography.emphasizedMdRegular

    /** Bo'sh holat matni. */
    val empty: TextStyle @Composable get() = AppTypography.titleMdMedium

    /** Pastdan chiquvchi varaq sarlavhasi. */
    val sheetTitle: TextStyle @Composable get() = AppTypography.headlineSmSemiBold

    /** Dialog va banner tugmalari (matnli). */
    val action: TextStyle @Composable get() = AppTypography.titleSmSemiBold

    /** Menyu bandi (⋮) — ixcham, 14sp. */
    val menu: TextStyle @Composable get() = AppTypography.titleSmMedium

    /** Matn kiritish maydoni va qidiruv. */
    val input: TextStyle @Composable get() = AppTypography.titleMdRegular
}
