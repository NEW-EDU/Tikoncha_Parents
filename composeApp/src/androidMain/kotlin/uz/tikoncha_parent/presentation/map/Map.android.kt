package uz.tikoncha_parent.presentation.map

import androidx.compose.ui.unit.Dp
import android.graphics.PointF
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.yandex.mapkit.Animation
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.BoundingBox
import com.yandex.mapkit.geometry.Geometry
import com.yandex.mapkit.geometry.LinearRing
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.CameraListener
import com.yandex.mapkit.map.CameraPosition as YCameraPosition
import com.yandex.mapkit.map.CircleMapObject
import com.yandex.mapkit.map.IconStyle
import com.yandex.mapkit.map.InputListener
import com.yandex.mapkit.map.Map as YMap
import com.yandex.mapkit.map.MapObjectTapListener
import com.yandex.mapkit.map.PlacemarkMapObject
import com.yandex.mapkit.map.PolygonMapObject
import com.yandex.mapkit.geometry.Circle as YCircle
import com.yandex.mapkit.geometry.Polygon as YPolygon
import com.yandex.mapkit.mapview.MapView
import com.yandex.mapkit.user_location.UserLocationView
import kotlinx.coroutines.launch

// ============================================================
// MAP KIT INITIALIZER
// ============================================================

actual object MapKitInitializer {
    @Volatile
    private var initialized = false

    actual fun initialize(apiKey: String) {
        if (initialized) return
        MapKitFactory.setApiKey(apiKey)
        initialized = true
    }
}

// ============================================================
// MAP CONTROLLER
// ============================================================

@Stable
actual class MapController actual constructor() {
    internal var mapView: MapView? = null

    actual val isReady: Boolean get() = mapView != null

    actual fun moveTo(position: CameraPosition, animated: Boolean) {
        val map = mapView?.mapWindow?.map ?: return
        val target = YCameraPosition(
            Point(position.target.lat, position.target.lon),
            position.zoom, position.azimuth, position.tilt
        )
        if (animated) map.move(target, Animation(Animation.Type.SMOOTH, 0.4f), null)
        else map.move(target)
    }

    actual fun fitBounds(points: List<LatLng>, paddingDp: Float, animated: Boolean) {
        val map = mapView?.mapWindow?.map ?: return
        if (points.isEmpty()) return
        if (points.size == 1) {
            moveTo(CameraPosition(points.first(), 16f), animated)
            return
        }
        val pts = points.map { Point(it.lat, it.lon) }
        val box = BoundingBox(
            Point(pts.minOf { it.latitude }, pts.minOf { it.longitude }),
            Point(pts.maxOf { it.latitude }, pts.maxOf { it.longitude })
        )
        val cam = map.cameraPosition(Geometry.fromBoundingBox(box))
        val padded = YCameraPosition(cam.target, cam.zoom - 0.4f, cam.azimuth, cam.tilt)
        if (animated) map.move(padded, Animation(Animation.Type.SMOOTH, 0.4f), null)
        else map.move(padded)
    }



    actual fun getCameraTarget(): LatLng? {
        val pos = mapView?.mapWindow?.map?.cameraPosition ?: return null
        return LatLng(pos.target.latitude, pos.target.longitude)
    }

    actual fun getCameraPosition(): CameraPosition? {
        val pos = mapView?.mapWindow?.map?.cameraPosition ?: return null
        return CameraPosition(
            target = LatLng(pos.target.latitude, pos.target.longitude),
            zoom = pos.zoom,
            azimuth = pos.azimuth,
            tilt = pos.tilt
        )
    }
}

@Composable
actual fun rememberMapController(): MapController = remember { MapController() }

// ============================================================
// USER LOCATION HELPER
// ============================================================

private fun applyUserLocationIcon(view: UserLocationView, icon: NativeMarkerIcon?) {
    if (icon == null) return
    val style = IconStyle().apply {
        anchor = PointF(MarkerDimensions.ANCHOR_X, MarkerDimensions.ANCHOR_Y)
    }
    view.pin.setIcon(icon.provider, style)
    view.arrow.setIcon(icon.provider, style)
    view.accuracyCircle.fillColor = android.graphics.Color.TRANSPARENT
}

// ============================================================
// COMPOSABLE — YandexMap
// ============================================================

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
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val density = LocalDensity.current
    val textMeasurer = rememberTextMeasurer()
    val scope = rememberCoroutineScope()

    val onMarkerClickState = rememberUpdatedState(onMarkerClick)
    val onMapTapState = rememberUpdatedState(onMapTap)
    val onCameraIdleState = rememberUpdatedState(onCameraIdle)

    val placemarks = remember { mutableMapOf<String, PlacemarkMapObject>() }
    val placemarkListeners = remember { mutableMapOf<String, MapObjectTapListener>() }
    val circleObjects = remember { mutableMapOf<String, CircleMapObject>() }
    val polygonObjects = remember { mutableMapOf<String, PolygonMapObject>() }
    val iconCache = remember { mutableStateMapOf<String, NativeMarkerIcon>() }


    val cameraListener = remember {
        CameraListener { _, position, _, finished ->
            if (finished) {
                onCameraIdleState.value(
                    LatLng(position.target.latitude, position.target.longitude)
                )
            }
        }
    }

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
                    android.util.Log.e("YMK_MARK", "icon failed for ${marker.id}", e)
                }
            }
        }
    }

    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            MapView(ctx).also { view ->
                controller.mapView = view
                view.mapWindow.map.move(
                    YCameraPosition(
                        Point(initialCamera.target.lat, initialCamera.target.lon),
                        initialCamera.zoom,
                        initialCamera.azimuth,
                        initialCamera.tilt
                    )
                )
                logoTopInset?.let { inset ->
                    val px = (inset.value * ctx.resources.displayMetrics.density).toInt()
                    val h = (12 * ctx.resources.displayMetrics.density).toInt()
                    view.mapWindow.map.logo.setAlignment(
                        com.yandex.mapkit.logo.Alignment(
                            com.yandex.mapkit.logo.HorizontalAlignment.RIGHT,
                            com.yandex.mapkit.logo.VerticalAlignment.TOP,
                        )
                    )
                    view.mapWindow.map.logo.setPadding(com.yandex.mapkit.logo.Padding(h, px))
                }
                view.mapWindow.map.addInputListener(object : InputListener {
                    override fun onMapTap(map: YMap, point: Point) {
                        onMapTapState.value(LatLng(point.latitude, point.longitude))
                    }
                    override fun onMapLongTap(map: YMap, point: Point) {}
                })
                view.mapWindow.map.addCameraListener(cameraListener)
            }
        },
        update = { view ->
            val map = view.mapWindow.map
            map.isNightModeEnabled = isDark

            // -------- MARKERS --------
            val incoming = markers.associateBy { it.id }

            (placemarks.keys - incoming.keys).toList().forEach { id ->
                placemarks.remove(id)?.let { pm ->
                    placemarkListeners.remove(id)?.let { pm.removeTapListener(it) }
                    map.mapObjects.remove(pm)
                }
            }

            incoming.forEach { (id, marker) ->
                val pm = placemarks[id] ?: map.mapObjects.addPlacemark(
                    Point(marker.position.lat, marker.position.lon)
                ).also { placemarks[id] = it }

                pm.geometry = Point(marker.position.lat, marker.position.lon)
                pm.zIndex = marker.zIndex

                if (placemarkListeners[id] == null) {
                    val listener = MapObjectTapListener { _, _ ->
                        onMarkerClickState.value(id); true
                    }
                    pm.addTapListener(listener)
                    placemarkListeners[id] = listener
                }

                val key = marker.style.cacheKey()
                iconCache[key]?.let { icon ->
                    pm.setIcon(
                        icon.provider,
                        IconStyle().apply {
                            anchor = PointF(
                                marker.style.anchorX,
                                marker.style.anchorY
                            )
                        }
                    )
                }
            }

            // -------- CIRCLES --------
            val nonReverseCircles = circles.filter { !it.reverse }
            val inCircles = nonReverseCircles.associateBy { it.id }
            (circleObjects.keys - inCircles.keys).toList().forEach { id ->
                circleObjects.remove(id)?.let { map.mapObjects.remove(it) }
            }
            inCircles.forEach { (id, c) ->
                val obj = circleObjects[id] ?: map.mapObjects.addCircle(
                    YCircle(Point(c.center.lat, c.center.lon), c.radiusMeters.toFloat())
                ).also { circleObjects[id] = it }
                obj.geometry = YCircle(
                    Point(c.center.lat, c.center.lon),
                    c.radiusMeters.toFloat()
                )
                obj.fillColor = c.fillColor.toInt()
                obj.strokeColor = c.strokeColor.toInt()
                obj.strokeWidth = c.strokeWidthDp
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
                polygonObjects.remove(id)?.let { map.mapObjects.remove(it) }
            }
            inPolys.forEach { (id, p) ->
                if (p.points.size < 3) return@forEach
                val outerPts = if (p.reverse) WORLD_OUTER_RING else p.points
                val innerRingsPts = if (p.reverse) listOf(p.points) else emptyList()
                val outer = LinearRing(outerPts.map { Point(it.lat, it.lon) })
                val inners = innerRingsPts.map { ring ->
                    LinearRing(ring.map { Point(it.lat, it.lon) })
                }
                val geom = YPolygon(outer, inners)
                val obj = polygonObjects[id]
                    ?: map.mapObjects.addPolygon(geom).also { polygonObjects[id] = it }
                obj.geometry = geom
                obj.fillColor = p.fillColor.toInt()
                obj.strokeColor = p.strokeColor.toInt()
                obj.strokeWidth = p.strokeWidthDp
            }

        }
    )

    DisposableEffect(lifecycleOwner) {
        val obs = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> {
                    MapKitFactory.getInstance().onStart()
                    controller.mapView?.onStart()
                }
                Lifecycle.Event.ON_STOP -> {
                    controller.mapView?.onStop()
                    MapKitFactory.getInstance().onStop()
                }
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(obs)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(obs)
            controller.mapView?.mapWindow?.map?.removeCameraListener(cameraListener)
            placemarks.clear()
            placemarkListeners.clear()
            circleObjects.clear()
            polygonObjects.clear()
            iconCache.clear()
            controller.mapView = null
        }
    }
}