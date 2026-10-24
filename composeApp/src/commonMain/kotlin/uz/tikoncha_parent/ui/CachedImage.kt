package uz.tikoncha_parent.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest

/*
 * Rasm "miltillashi"siz: har ekranga qaytganda avatar/ikonka avval placeholder (tipratikan) bo'lib, keyin
 * rasmga almashardi. Endi kesh kaliti URL'ning o'zi — xotirada bor rasm darhol (birinchi kadrdayoq)
 * placeholder sifatida chiziladi, keyin kerak bo'lsa kattaroq o'lchamda qayta dekodlanadi.
 *
 * Eslatma: `AsyncImage(placeholder = ...)` parametri bu keshni bosib ketadi — shuning uchun bu so'rov bilan
 * faqat `error` beriladi.
 */
@Composable
fun rememberCachedImage(url: String?): ImageRequest? {
    val context = LocalPlatformContext.current
    return remember(url) {
        if (url.isNullOrBlank()) null
        else ImageRequest.Builder(context)
            .data(url)
            .memoryCacheKey(url)
            .placeholderMemoryCacheKey(url)
            .build()
    }
}
