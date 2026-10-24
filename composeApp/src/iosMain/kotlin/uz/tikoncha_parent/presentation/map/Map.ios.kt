@file:OptIn(ExperimentalForeignApi::class)

package uz.tikoncha_parent.presentation.map

import androidx.compose.ui.unit.Dp
import cocoapods.YandexMapsMobile.YMKLogoPadding
import cocoapods.YandexMapsMobile.YMKLogoVerticalAlignment
import cocoapods.YandexMapsMobile.YMKLogoHorizontalAlignment
import cocoapods.YandexMapsMobile.YMKLogoAlignment
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.viewinterop.UIKitInteropInteractionMode
import androidx.compose.ui.viewinterop.UIKitInteropProperties
import androidx.compose.ui.viewinterop.UIKitView
import cocoapods.YandexMapsMobile.YMKAnimation
import cocoapods.YandexMapsMobile.YMKAnimationType
import cocoapods.YandexMapsMobile.YMKCameraPosition
import cocoapods.YandexMapsMobile.YMKCameraUpdateReason
import cocoapods.YandexMapsMobile.YMKCircle
import cocoapods.YandexMapsMobile.YMKCircleMapObject
import cocoapods.YandexMapsMobile.YMKIconStyle
import cocoapods.YandexMapsMobile.YMKLinearRing
import cocoapods.YandexMapsMobile.YMKMap
import cocoapods.YandexMapsMobile.YMKMapCameraListenerProtocol
import cocoapods.YandexMapsMobile.YMKMapInputListenerProtocol
import cocoapods.YandexMapsMobile.YMKMapKit
import cocoapods.YandexMapsMobile.YMKMapObject
import cocoapods.YandexMapsMobile.YMKMapObjectTapListenerProtocol
import cocoapods.YandexMapsMobile.YMKMapView
import cocoapods.YandexMapsMobile.YMKPlacemarkMapObject
import cocoapods.YandexMapsMobile.YMKPoint
import cocoapods.YandexMapsMobile.YMKPolygon
import cocoapods.YandexMapsMobile.YMKPolygonMapObject
import cocoapods.YandexMapsMobile.setApiKey
import cocoapods.YandexMapsMobile.sharedInstance
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.readValue
import kotlinx.coroutines.launch
import platform.CoreGraphics.CGPointMake
import platform.CoreGraphics.CGRectZero
import platform.Foundation.NSValue
import platform.UIKit.UIColor
import platform.UIKit.UIView
import platform.UIKit.valueWithCGPoint
import platform.darwin.NSObject
import kotlin.concurrent.Volatile

// ============================================================
// HELPERS
// ============================================================

@OptIn(ExperimentalForeignApi::class)
private fun Long.toUIColor(): UIColor {
    val a = ((this shr 24) and 0xFF) / 255.0
    val r = ((this shr 16) and 0xFF) / 255.0
    val g = ((this shr 8) and 0xFF) / 255.0
    val b = (this and 0xFF) / 255.0
    return UIColor.colorWithRed(
        red = r,
        green = g,
        blue = b,
        alpha = if (a == 0.0) 1.0 else a
    )
}

@OptIn(ExperimentalForeignApi::class)
private fun ymkPoint(lat: Double, lon: Double): YMKPoint =
    YMKPoint.pointWithLatitude(latitude = lat, longitude = lon)

@OptIn(ExperimentalForeignApi::class)
private fun LatLng.toYMK(): YMKPoint = ymkPoint(lat, lon)

@OptIn(ExperimentalForeignApi::class)
private fun ymkCameraPosition(
    target: YMKPoint,
    zoom: Float,
    azimuth: Float = 0f,
    tilt: Float = 0f
): YMKCameraPosition =
    YMKCameraPosition.cameraPositionWithTarget(
        target = target, zoom = zoom, azimuth = azimuth, tilt = tilt
    )

@OptIn(ExperimentalForeignApi::class)
private fun smoothAnimation(duration: Float = 0.4f): YMKAnimation =
    YMKAnimation.animationWithType(
        type = YMKAnimationType.YMKAnimationTypeSmooth,
        duration = duration
    )

private fun zoomForSpan(maxSpanDegrees: Double): Float = when {
    maxSpanDegrees > 180.0 -> 1f
    maxSpanDegrees > 90.0 -> 2f
    maxSpanDegrees > 45.0 -> 3f
    maxSpanDegrees > 20.0 -> 4f
    maxSpanDegrees > 10.0 -> 5f
    maxSpanDegrees > 5.0 -> 6f
    maxSpanDegrees > 2.0 -> 7f
    maxSpanDegrees > 1.0 -> 8f
    maxSpanDegrees > 0.5 -> 9f
    maxSpanDegrees > 0.25 -> 10f
    maxSpanDegrees > 0.1 -> 11f
    maxSpanDegrees > 0.05 -> 12f
    maxSpanDegrees > 0.025 -> 13f
    maxSpanDegrees > 0.01 -> 14f
    maxSpanDegrees > 0.005 -> 15f
    else -> 16f
}

private fun anchorIconStyle(style: MarkerStyle): YMKIconStyle = YMKIconStyle().apply {
    setAnchor(
        NSValue.valueWithCGPoint(
            CGPointMake(
                style.anchorX.toDouble(),
                style.anchorY.toDouble()
            )
        )
    )
}

// ============================================================
// MAP KIT INITIALIZER
// ============================================================

@OptIn(ExperimentalForeignApi::class)
actual object MapKitInitializer {
    @Volatile
    private var initialized = false

    actual fun initialize(apiKey: String) {
        if (initialized) return
        YMKMapKit.setApiKey(apiKey)
        YMKMapKit.sharedInstance()
        initialized = true
    }
}

// ============================================================
// MAP CONTROLLER
// ============================================================

@OptIn(ExperimentalForeignApi::class)
@Stable
actual class MapController actual constructor() {
    internal var mapView: YMKMapView? = null
    actual val isReady: Boolean get() = mapView != null

    actual fun moveTo(position: CameraPosition, animated: Boolean) {
        val map = mapView?.mapWindow?.map ?: return
        val target = ymkCameraPosition(
            target = position.target.toYMK(),
            zoom = position.zoom,
            azimuth = position.azimuth,
            tilt = position.tilt
        )
        if (animated) {
            map.moveWithCameraPosition(
                cameraPosition = target,
                animation = smoothAnimation(),
                cameraCallback = null
            )
        } else {
            map.moveWithCameraPosition(target)
        }
    }

    actual fun fitBounds(points: List<LatLng>, paddingDp: Float, animated: Boolean) {
        if (points.isEmpty()) return
        val map = mapView?.mapWindow?.map ?: return

        if (points.size == 1) {
            moveTo(CameraPosition(target = points.first(), zoom = 16f), animated)
            return
        }

        val minLat = points.minOf { it.lat }
        val maxLat = points.maxOf { it.lat }
        val minLon = points.minOf { it.lon }
        val maxLon = points.maxOf { it.lon }

        val centerLat = (minLat + maxLat) / 2.0
        val centerLon = (minLon + maxLon) / 2.0

        val maxSpan = maxOf(maxLat - minLat, maxLon - minLon)
        val zoom = zoomForSpan(maxSpan)
        val finalZoom = (zoom - 0.5f).coerceAtLeast(1f)

        val target = ymkCameraPosition(
            target = ymkPoint(centerLat, centerLon),
            zoom = finalZoom
        )

        if (animated) {
            map.moveWithCameraPosition(
                cameraPosition = target,
                animation = smoothAnimation(),
                cameraCallback = null
            )
        } else {
            map.moveWithCameraPosition(target)
        }
    }


    actual fun getCameraTarget(): LatLng? {
        val pos = mapView?.mapWindow?.map?.cameraPosition ?: return null
        val t = pos.target
        return LatLng(t.latitude, t.longitude)
    }

    actual fun getCameraPosition(): CameraPosition? {
        val pos = mapView?.mapWindow?.map?.cameraPosition ?: return null
        val t = pos.target
        return CameraPosition(
            target = LatLng(t.latitude, t.longitude),
            zoom = pos.zoom,
            azimuth = pos.azimuth,
            tilt = pos.tilt
        )
    }
}

@Composable
actual fun rememberMapController(): MapController = remember { MapController() }

// ============================================================
// LISTENERS
// ============================================================

@OptIn(ExperimentalForeignApi::class)
private class PlacemarkTapListener(
    private val markerId: String,
    private val onTap: (String) -> Unit
) : NSObject(), YMKMapObjectTapListenerProtocol {
    override fun onMapObjectTapWithMapObject(
        mapObject: YMKMapObject,
        point: YMKPoint
    ): Boolean {
        onTap(markerId)
        return true
    }
}

@OptIn(ExperimentalForeignApi::class)
private class MapInputListener(
    private val onTap: (LatLng) -> Unit
) : NSObject(), YMKMapInputListenerProtocol {
    override fun onMapTapWithMap(map: YMKMap, point: YMKPoint) {
        onTap(LatLng(point.latitude, point.longitude))
    }
    override fun onMapLongTapWithMap(map: YMKMap, point: YMKPoint) {}
}

@OptIn(ExperimentalForeignApi::class)
private class CameraIdleListener(
    private val onIdle: (LatLng) -> Unit
) : NSObject(), YMKMapCameraListenerProtocol {
    override fun onCameraPositionChangedWithMap(
        map: YMKMap,
        cameraPosition: YMKCameraPosition,
        cameraUpdateReason: YMKCameraUpdateReason,
        finished: Boolean
    ) {
        if (finished) {
            val t = cameraPosition.target
            onIdle(LatLng(t.latitude, t.longitude))
        }
    }
}



// ============================================================
// COMPOSABLE — YandexMap
// ============================================================

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun YandexMap(
    controller: MapController,
    initialCamera: CameraPosition,
    markers: List<MapMarker>,
    circles: List<MapCircle>,
    polygons: List<MapPolygon>,
    onMarkerClick: (String) -> Unit,
    onMapTap: (LatLng) -> Unit,
    onCameraIdle: (LatLng) -> Unit,
    isDark: Boolean,
    modifier: Modifier,
    logoTopInset: Dp?,
) {
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()
    val textMeasurer = rememberTextMeasurer()

    val onMarkerClickState = rememberUpdatedState(onMarkerClick)
    val onMapTapState = rememberUpdatedState(onMapTap)
    val onCameraIdleState = rememberUpdatedState(onCameraIdle)

    val placemarks = remember { mutableMapOf<String, YMKPlacemarkMapObject>() }
    val listeners = remember { mutableMapOf<String, PlacemarkTapListener>() }
    val circleObjects = remember { mutableMapOf<String, YMKCircleMapObject>() }
    val polygonObjects = remember { mutableMapOf<String, YMKPolygonMapObject>() }
    val iconCache = remember { mutableStateMapOf<String, NativeMarkerIcon>() }

    val mapInputListener = remember { MapInputListener { onMapTapState.value(it) } }
    val cameraListener = remember { CameraIdleListener { onCameraIdleState.value(it) } }


    LaunchedEffect(markers) {
        markers.forEach { marker ->
            val key = marker.style.cacheKey()
            if (iconCache.containsKey(key)) return@forEach
            scope.launch {
                try {
                    val icon = createMarkerIcon(
                        style = marker.style,
                        density = density,
                        textMeasurer = textMeasurer
                    )
                    iconCache[key] = icon
                } catch (e: Throwable) {
                    println("YMK_MARK: ❌ icon failed for ${marker.id}: ${e.message}")
                }
            }
        }
    }

    UIKitView(
        modifier = modifier,
        factory = {
            val view = YMKMapView(frame = CGRectZero.readValue())
            controller.mapView = view

            view.mapWindow?.map?.moveWithCameraPosition(
                ymkCameraPosition(
                    target = initialCamera.target.toYMK(),
                    zoom = initialCamera.zoom,
                    azimuth = initialCamera.azimuth,
                    tilt = initialCamera.tilt
                )
            )
            logoTopInset?.let { inset ->
                view.mapWindow?.map?.logo?.setAlignmentWithAlignment(
                    YMKLogoAlignment.alignmentWithHorizontalAlignment(
                        horizontalAlignment = YMKLogoHorizontalAlignment.YMKLogoHorizontalAlignmentRight,
                        verticalAlignment = YMKLogoVerticalAlignment.YMKLogoVerticalAlignmentTop,
                    )
                )
                view.mapWindow?.map?.logo?.setPaddingWithPadding(
                    // MapKit iOS'da logotip chekinishi piksellarda (nuqtada emas)
                    YMKLogoPadding.paddingWithHorizontalPadding(
                        (12 * density.density).toULong(),
                        verticalPadding = (inset.value * density.density).toULong(),
                    )
                )
            }
            view.mapWindow?.map?.addInputListenerWithInputListener(mapInputListener)
            view.mapWindow?.map?.addCameraListenerWithCameraListener(cameraListener)
            view as UIView
        },
        update = { uiView ->
            val view = uiView as YMKMapView
            val map = view.mapWindow?.map ?: return@UIKitView

            map.setNightModeEnabled(isDark)

            // -------- MARKERS --------
            val incoming = markers.associateBy { it.id }

            (placemarks.keys - incoming.keys).toList().forEach { id ->
                placemarks.remove(id)?.let { pm ->
                    listeners.remove(id)?.let { listener ->
                        pm.removeTapListenerWithTapListener(listener)
                    }
                    map.mapObjects.removeWithMapObject(pm)
                }
            }

            incoming.forEach { (id, marker) ->
                val point = marker.position.toYMK()

                val pm = placemarks[id] ?: map.mapObjects
                    .addPlacemarkWithPoint(point)
                    .also { placemarks[id] = it }

                pm.setGeometry(point)
                pm.setZIndex(marker.zIndex)

                if (listeners[id] == null) {
                    val listener = PlacemarkTapListener(id) { onMarkerClickState.value(it) }
                    pm.addTapListenerWithTapListener(listener)
                    listeners[id] = listener
                }

                val key = marker.style.cacheKey()
                iconCache[key]?.let { icon ->
                    pm.setIconWithImage(icon.image)
                    pm.setIconStyleWithStyle(anchorIconStyle(marker.style))
                }
            }

            // -------- CIRCLES --------
            val nonReverseCircles = circles.filter { !it.reverse }
            val inCircles = nonReverseCircles.associateBy { it.id }

            (circleObjects.keys - inCircles.keys).toList().forEach { id ->
                circleObjects.remove(id)?.let { map.mapObjects.removeWithMapObject(it) }
            }

            inCircles.forEach { (id, c) ->
                val geom = YMKCircle.circleWithCenter(
                    center = c.center.toYMK(),
                    radius = c.radiusMeters.toFloat()
                )

                val obj = circleObjects[id]
                    ?: map.mapObjects.addCircleWithCircle(geom).also {
                        circleObjects[id] = it
                    }

                obj.setGeometry(geom)
                obj.setFillColor(c.fillColor.toUIColor())
                obj.setStrokeColor(c.strokeColor.toUIColor())
                obj.setStrokeWidth(c.strokeWidthDp)
            }

            // -------- POLYGONS (asl + reverse=true circles) --------
            val combinedPolygons = buildList {
                addAll(polygons)
                circles.filter { it.reverse }.forEach { c ->
                    add(
                        MapPolygon(
                            id = "__circle_${c.id}",
                            points = circleToPolygonPoints(c.center, c.radiusMeters),
                            reverse = true,
                            fillColor = c.fillColor,
                            strokeColor = c.strokeColor,
                            strokeWidthDp = c.strokeWidthDp,
                        )
                    )
                }
            }

            val inPolys = combinedPolygons.associateBy { it.id }
            (polygonObjects.keys - inPolys.keys).toList().forEach { id ->
                polygonObjects.remove(id)?.let { map.mapObjects.removeWithMapObject(it) }
            }

            inPolys.forEach { (id, p) ->
                if (p.points.size < 3) return@forEach

                val outerPts = if (p.reverse) WORLD_OUTER_RING else p.points
                val innerRingsPts = if (p.reverse) listOf(p.points) else emptyList()

                val outer = YMKLinearRing.linearRingWithPoints(
                    outerPts.map { it.toYMK() }
                )
                val inners = innerRingsPts.map { ring ->
                    YMKLinearRing.linearRingWithPoints(ring.map { it.toYMK() })
                }
                val geom = YMKPolygon.polygonWithOuterRing(
                    outerRing = outer,
                    innerRings = inners
                )

                val obj = polygonObjects[id]
                    ?: map.mapObjects.addPolygonWithPolygon(geom).also {
                        polygonObjects[id] = it
                    }

                obj.setGeometry(geom)
                obj.setFillColor(p.fillColor.toUIColor())
                obj.setStrokeColor(p.strokeColor.toUIColor())
                obj.setStrokeWidth(p.strokeWidthDp)
            }

        },
        properties = UIKitInteropProperties(
            interactionMode = UIKitInteropInteractionMode.NonCooperative,
            isNativeAccessibilityEnabled = false
        )
    )

    DisposableEffect(Unit) {
        onDispose {
            placemarks.clear()
            listeners.clear()
            circleObjects.clear()
            polygonObjects.clear()
            iconCache.clear()
            controller.mapView = null
        }
    }
}