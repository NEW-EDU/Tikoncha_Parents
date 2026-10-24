package uz.tikoncha_parent.platform

import platform.UIKit.popoverPresentationController
import platform.UIKit.UIViewController
import platform.UIKit.UIAlertControllerStyleActionSheet
import platform.UIKit.UIAlertController
import platform.UIKit.UIAlertActionStyleDefault
import platform.UIKit.UIAlertActionStyleCancel
import platform.UIKit.UIAlertAction
import platform.Foundation.URLQueryAllowedCharacterSet
import platform.Foundation.stringByAddingPercentEncodingWithAllowedCharacters
import platform.Foundation.NSString
import platform.Foundation.NSCharacterSet
import kotlinx.cinterop.ExperimentalForeignApi
import platform.CoreLocation.CLLocationManager
import platform.Foundation.NSURL
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication
import platform.UIKit.UIApplicationOpenSettingsURLString

actual fun isLocationServiceEnabled(): Boolean = CLLocationManager.locationServicesEnabled()

// iOS 18+: eski `openURL(_:)` hech narsa qilmaydi — faqat options/completion varianti ishlaydi.
private fun launch(url: NSURL?) {
    url ?: return
    UIApplication.sharedApplication.openURL(url, emptyMap<Any?, Any?>(), null)
}

actual fun openLocationSettings() = launch(NSURL.URLWithString(UIApplicationOpenSettingsURLString))

actual fun openAppSettings() = launch(NSURL.URLWithString(UIApplicationOpenSettingsURLString))

@OptIn(ExperimentalForeignApi::class)
actual fun openInExternalMaps(lat: Double, lng: Double, label: String, cancelText: String) {
    val app = UIApplication.sharedApplication
    @Suppress("CAST_NEVER_SUCCEEDS")
    val name = (label as NSString).stringByAddingPercentEncodingWithAllowedCharacters(
        NSCharacterSet.URLQueryAllowedCharacterSet
    ) ?: ""
    fun installed(scheme: String) = NSURL.URLWithString(scheme)?.let { app.canOpenURL(it) } == true
    // O'zbekistonda eng ko'p ishlatiladigani — Yandex; keyin Google; Apple Maps doim bor
    val choices = buildList {
        if (installed("yandexmaps://")) add("Yandex Maps" to "yandexmaps://maps.yandex.ru/?pt=$lng,$lat&z=16&l=map")
        if (installed("comgooglemaps://")) add("Google Maps" to "comgooglemaps://?q=$lat,$lng&center=$lat,$lng&zoom=16")
        add("Apple Maps" to "https://maps.apple.com/?ll=$lat,$lng&q=$name")
    }
    if (choices.size == 1) {
        launch(NSURL.URLWithString(choices.first().second))
        return
    }
    val sheet = UIAlertController.alertControllerWithTitle(
        title = label.ifBlank { null },
        message = null,
        preferredStyle = UIAlertControllerStyleActionSheet,
    )
    choices.forEach { (title, link) ->
        sheet.addAction(UIAlertAction.actionWithTitle(title, UIAlertActionStyleDefault) { _ -> launch(NSURL.URLWithString(link)) })
    }
    sheet.addAction(UIAlertAction.actionWithTitle(cancelText, UIAlertActionStyleCancel, null))
    val top = topController() ?: return
    // iPad (moslik rejimi ham): action sheet popover manbasiz yiqiladi
    sheet.popoverPresentationController?.let { pop ->
        pop.sourceView = top.view
        pop.sourceRect = top.view.bounds
        pop.permittedArrowDirections = 0uL
    }
    top.presentViewController(sheet, animated = true, completion = null)
}

private fun topController(): UIViewController? {
    var vc = UIApplication.sharedApplication.keyWindow?.rootViewController
    while (vc?.presentedViewController != null) vc = vc.presentedViewController
    return vc
}

@OptIn(ExperimentalForeignApi::class)
actual fun shareLocation(lat: Double, lng: Double, name: String) {
    val text = "$name\nhttps://maps.apple.com/?ll=$lat,$lng"
    val vc = UIActivityViewController(activityItems = listOf(text), applicationActivities = null)
    UIApplication.sharedApplication.keyWindow?.rootViewController
        ?.presentViewController(vc, animated = true, completion = null)
}