package uz.tikoncha_parent.presentation.tracking

import uz.tikoncha_parent.data.local.AppSettings
import androidx.compose.foundation.background
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material.icons.rounded.MyLocation
import androidx.compose.material.icons.rounded.ZoomOutMap
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.graphics.vector.ImageVector
import cafe.adriel.voyager.navigator.internal.BackHandler
import tikoncha_parents.composeapp.generated.resources.xarita
import uz.tikoncha_parent.presentation.base.CustomHeader
import uz.tikoncha_parent.presentation.base.island.islandReservedFromBottom
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import dev.icerock.moko.geo.LocationTracker
import dev.icerock.moko.geo.compose.BindLocationTrackerEffect
import dev.icerock.moko.permissions.PermissionsController
import dev.icerock.moko.permissions.compose.BindEffect
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.imageResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import tikoncha_parents.composeapp.generated.resources.Res
import tikoncha_parents.composeapp.generated.resources.bekor_qilish
import tikoncha_parents.composeapp.generated.resources.dialog_info
import tikoncha_parents.composeapp.generated.resources.dialog_warning
import tikoncha_parents.composeapp.generated.resources.diqqat
import tikoncha_parents.composeapp.generated.resources.farzandingizni_qayerda_ekanini_kuzatish
import tikoncha_parents.composeapp.generated.resources.ok
import tikoncha_parents.composeapp.generated.resources.plus_obnuna_kerak
import tikoncha_parents.composeapp.generated.resources.profile_hedgehog_img
import tikoncha_parents.composeapp.generated.resources.siz
import tikoncha_parents.composeapp.generated.resources.tracking_gps_off_enable
import tikoncha_parents.composeapp.generated.resources.tracking_gps_off_message
import tikoncha_parents.composeapp.generated.resources.tracking_gps_off_title
import tikoncha_parents.composeapp.generated.resources.tracking_permission_grant
import tikoncha_parents.composeapp.generated.resources.tracking_permission_message
import tikoncha_parents.composeapp.generated.resources.tracking_permission_settings_message
import tikoncha_parents.composeapp.generated.resources.tracking_permission_settings_open
import tikoncha_parents.composeapp.generated.resources.tracking_permission_settings_title
import tikoncha_parents.composeapp.generated.resources.tracking_permission_title
import uz.tikoncha_parent.domain.model.SubscriptionType
import uz.tikoncha_parent.platform.Logger
import uz.tikoncha_parent.platform.openAppSettings
import uz.tikoncha_parent.platform.openInExternalMaps
import uz.tikoncha_parent.platform.openLocationSettings
import uz.tikoncha_parent.platform.openUrl
import uz.tikoncha_parent.platform.shareLocation
import uz.tikoncha_parent.presentation.base.CustomDialog
import uz.tikoncha_parent.presentation.base.OnScreenActive
import uz.tikoncha_parent.presentation.base.SubscriptionBottomDialog
import uz.tikoncha_parent.presentation.base.asText
import uz.tikoncha_parent.presentation.map.CameraPosition
import uz.tikoncha_parent.presentation.map.awaitReady
import uz.tikoncha_parent.presentation.map.regionFallbackCamera
import uz.tikoncha_parent.presentation.map.LatLng
import uz.tikoncha_parent.presentation.map.MapCircle
import uz.tikoncha_parent.presentation.map.MapController
import uz.tikoncha_parent.presentation.map.MapMarker
import uz.tikoncha_parent.presentation.map.MarkerStyle
import uz.tikoncha_parent.presentation.map.YandexMap
import uz.tikoncha_parent.presentation.map.rememberMapController
import uz.tikoncha_parent.presentation.profile.subscription.subscription_payment.SubscriptionPaymentScreen
import uz.tikoncha_parent.ui.theme.AppColors
import uz.tikoncha_parent.ui.theme.rememberIsDarkTheme

private fun Color.toArgbLong(): Long = this.toArgb().toLong() and 0xFFFFFFFFL
class TrackingScreen : Screen {

    @Composable
    override fun Content() {
        val screenModel = koinScreenModel<TrackingScreenModel>()
        val state by screenModel.state.collectAsState()
        val event = screenModel::onEvent
        val mapController = rememberMapController()
        val navigator = LocalNavigator.current

        val permissionsController: PermissionsController = koinInject()
        val locationTracker: LocationTracker = koinInject()

        BindEffect(permissionsController)
        BindLocationTrackerEffect(locationTracker)

        var cameFromSettings by remember { mutableStateOf(false) }

        OnScreenActive(
            launchedToSettings = cameFromSettings,
            onReturned = {
                cameFromSettings = false
                screenModel.onEvent(TrackingEvent.RecheckPermission)
            }
        )

        var showPermissionDialog by remember { mutableStateOf(false) }
        var showPermissionAlwaysDialog by remember { mutableStateOf(false) }
        var showGpsDialog by remember { mutableStateOf(false) }

        val placeholderBitmap = imageResource(Res.drawable.profile_hedgehog_img)
        val selfText = stringResource(Res.string.siz)

        // ⬇️ Self text'ni ScreenModel'ga uzatamiz
        LaunchedEffect(selfText) {
            screenModel.onEvent(TrackingEvent.SetSelfText(selfText))
        }

        // Effects
        LaunchedEffect(Unit) {
            screenModel.effect.collect { eff ->
                when (eff) {
                    is TrackingEffect.MoveCamera ->
                        mapController.moveTo(CameraPosition(eff.target, eff.zoom))
                    is TrackingEffect.FitBounds ->
                        mapController.fitBounds(eff.points)
                    TrackingEffect.OpenAppSettings -> openAppSettings()
                    TrackingEffect.OpenLocationSettings -> openLocationSettings()
                    TrackingEffect.RequestPermission -> Unit

                    TrackingEffect.MoveToUserLocation -> {
                        for (attempt in 0 until 10) {
                            delay(500)
                            val selfLoc = screenModel.state.value.self?.location  // ← state.self
                            if (selfLoc != null) {
                                mapController.moveTo(CameraPosition(selfLoc, 16f), animated = true)
                                break
                            }
                        }
                    }

                    is TrackingEffect.OpenUrl -> openUrl(eff.url)
                }
            }
        }

        LaunchedEffect(state.permissionDenied, state.permissionDeniedAlways) {
            showPermissionDialog = state.permissionDenied && !state.permissionDeniedAlways
            showPermissionAlwaysDialog = state.permissionDeniedAlways
        }

        LaunchedEffect(state.showGpsDialog) {
            showGpsDialog = state.showGpsDialog
        }

        TrackingContent(
            state = state,
            mapController = mapController,
            placeholderBitmap = placeholderBitmap,
            selfText = selfText,
            onEvent = event,
            onBack = { navigator?.pop() },
            onWatchVideo = { event(TrackingEvent.OpenYoutubeUrl(it)) },
            onOpenInMaps = { person ->
                Logger.d("TrackingScreen", "person = $person")
                person.location?.let { loc -> openInExternalMaps(loc.lat, loc.lon, person.name) }
            },
            onShare = { person ->
                Logger.d("TrackingScreen", "person = $person")
                person.location?.let { loc -> shareLocation(loc.lat, loc.lon, person.name) }
            },
        )

        // ============== DIALOGLAR ==============

        CustomDialog(
            show = showPermissionDialog,
            title = stringResource(Res.string.tracking_permission_title),
            message = stringResource(Res.string.tracking_permission_message),
            painter = painterResource(Res.drawable.dialog_info),
            buttonText = stringResource(Res.string.tracking_permission_grant),
            buttonText2 = stringResource(Res.string.bekor_qilish),
            showCloseButton = true,
            onButtonClick = {
                showPermissionDialog = false
                screenModel.onEvent(TrackingEvent.RequestLocationPermission)
            },
            onDismiss = {
                showPermissionDialog = false
                screenModel.onEvent(TrackingEvent.DismissPermissionDialog)
            }
        )

        CustomDialog(
            show = showPermissionAlwaysDialog,
            title = stringResource(Res.string.tracking_permission_settings_title),
            message = stringResource(Res.string.tracking_permission_settings_message),
            painter = painterResource(Res.drawable.dialog_info),
            buttonText = stringResource(Res.string.tracking_permission_settings_open),
            buttonText2 = stringResource(Res.string.bekor_qilish),
            showCloseButton = true,
            onButtonClick = {
                cameFromSettings = true
                showPermissionAlwaysDialog = false
                screenModel.onEvent(TrackingEvent.OpenAppSettings)
                screenModel.onEvent(TrackingEvent.DismissPermissionDialog)
            },
            onDismiss = {
                showPermissionAlwaysDialog = false
                screenModel.onEvent(TrackingEvent.DismissPermissionDialog)
            }
        )

        CustomDialog(
            show = showGpsDialog,
            title = stringResource(Res.string.tracking_gps_off_title),
            message = stringResource(Res.string.tracking_gps_off_message),
            painter = painterResource(Res.drawable.dialog_info),
            buttonText = stringResource(Res.string.tracking_gps_off_enable),
            buttonText2 = stringResource(Res.string.bekor_qilish),
            showCloseButton = true,
            onButtonClick = {
                cameFromSettings = true
                showGpsDialog = false
                openLocationSettings()
                screenModel.onEvent(TrackingEvent.GpsEnabledByUser)
            },
            onDismiss = {
                showGpsDialog = false
                screenModel.onEvent(TrackingEvent.DismissGpsDialog)
            }
        )

        CustomDialog(
            show = state.error != null,
            title = stringResource(Res.string.diqqat),
            message = state.error?.asText() ?: "",
            painter = painterResource(Res.drawable.dialog_warning),
            buttonText = stringResource(Res.string.ok),
            showCloseButton = false,
            onDismiss = { event(TrackingEvent.DismissError)},
            onButtonClick = { event(TrackingEvent.DismissError)}
        )

        SubscriptionBottomDialog(
            show = state.showSubscriptionDialog,
            title = stringResource(Res.string.plus_obnuna_kerak),
            message = stringResource(Res.string.farzandingizni_qayerda_ekanini_kuzatish),
            onConfirm = {
                screenModel.onEvent(TrackingEvent.DismissSubscriptionDialog)
                navigator?.push(SubscriptionPaymentScreen())
            },
            onDismiss = {
                screenModel.onEvent(TrackingEvent.DismissSubscriptionDialog)
            }
        )
    }
}

private const val CHILD_ZOOM = 15f

@OptIn(ExperimentalMaterial3ExpressiveApi::class, cafe.adriel.voyager.core.annotation.InternalVoyagerApi::class)
@Composable
fun TrackingContent(
    state: TrackingState,
    mapController: MapController,
    onEvent: (TrackingEvent) -> Unit,
    placeholderBitmap: ImageBitmap,
    selfText: String,                    // ⬅️ YANGI
    onBack: () -> Unit,
    onWatchVideo: (String) -> Unit = {},
    onOpenInMaps: (Person) -> Unit = {},
    onShare: (Person) -> Unit = {},
) {
    val isDark = rememberIsDarkTheme()

    // Xarita qayerda ochiladi: joylashuvi eng yangi farzand → (oldingi safar eslab qolingan) o'sha farzand nuqtasi →
    // men → telefon hududi. Avval "men" birinchi edi — ota-onaning o'z shahri (masalan, Andijon) ochilib qolardi.
    val freshestChild = state.people.freshestChild()?.location
    val rememberedChild = remember { AppSettings.lastChildLocation }
    val initialCamera = remember {
        val child = freshestChild ?: rememberedChild
        when {
            child != null -> CameraPosition(child, zoom = CHILD_ZOOM)
            state.self?.location != null -> CameraPosition(state.self.location, zoom = 14f)
            else -> regionFallbackCamera()
        }
    }
    // Farzand joylashuvi xarita ochilgandan keyin kelsa — bir marta o'sha yerga uchamiz.
    var focusedOnChild by remember { mutableStateOf(freshestChild != null) }
    LaunchedEffect(freshestChild) {
        if (focusedOnChild || freshestChild == null) return@LaunchedEffect
        focusedOnChild = true
        mapController.awaitReady()
        mapController.moveTo(CameraPosition(freshestChild, zoom = CHILD_ZOOM), animated = true)
    }
    // Hech bir farzandning joylashuvi yo'q bo'lsa — o'zimga (bir marta).
    var focusedOnSelf by remember { mutableStateOf(false) }
    val selfLocation = state.self?.location
    val noChildLocation = state.peopleLoaded && freshestChild == null && rememberedChild == null
    LaunchedEffect(selfLocation, noChildLocation) {
        if (focusedOnSelf || focusedOnChild || !noChildLocation || selfLocation == null) return@LaunchedEffect
        focusedOnSelf = true
        mapController.awaitReady()
        mapController.moveTo(CameraPosition(selfLocation, zoom = 14f), animated = true)
    }


    val dangerColor = AppColors.icon.accentDanger
    val accuracyFillColor = dangerColor.copy(alpha = 0.20f).toArgbLong()
    val accuracyStrokeColor = dangerColor.copy(alpha = 0.40f).toArgbLong()

    val userAccuracyCircle = remember(
        state.self?.location,
        accuracyFillColor,
        accuracyStrokeColor,
    ) {
        val loc = state.self?.location
        if (loc != null) {
            listOf(
                MapCircle(
                    id = "user_accuracy",
                    center = loc,
                    radiusMeters = 30.0,
                    reverse = false,
                    fillColor = accuracyFillColor,
                    strokeColor = accuracyStrokeColor,
                    strokeWidthDp = 1f,
                )
            )
        } else emptyList()
    }

    // ⬇️ YANGI — Self ham MapMarker bo'ldi
    val markers = remember(
        state.people, state.self, state.selectedPersonId,
        state.subscriptionLimits, placeholderBitmap, selfText,
    ) {
        buildList {
            state.people
                .filter { person ->
                    person.location ?: return@filter false
                    val limit = state.subscriptionLimits.firstOrNull { it.childId == person.id }
                    limit != null && limit.subscriptionType != SubscriptionType.FREE
                }
                .forEach { person ->
                    val isSelected = person.id == state.selectedPersonId
                    val style = if (isSelected) {
                        MarkerStyle.ChildSelected(
                            label = person.name,
                            avatarUrl = person.avatarUrl,
                            placeholderBitmap = placeholderBitmap,
                        )
                    } else {
                        MarkerStyle.Child(
                            label = person.name,
                            avatarUrl = person.avatarUrl,
                            placeholderBitmap = placeholderBitmap,
                        )
                    }
                    add(
                        MapMarker(
                            id = person.id,
                            position = person.location!!,
                            style = style,
                            zIndex = if (isSelected) 100f else 0f,
                        )
                    )
                }

            // Self alohida marker
            state.self?.location?.let { selfLoc ->
                add(
                    MapMarker(
                        id = "self",
                        position = selfLoc,
                        style = MarkerStyle.Self(),
                        zIndex = 50f,
                    )
                )
            }
        }
    }

    // Xarita butun ekran bo'ylab (status bar va orol ostida ham) — boshqaruv pastda, orolda.
    val statusTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val bottomReserved = islandReservedFromBottom()
    val locatedCount = state.people.count { it.location != null }

    // Orol: [‹ Xarita] [hammasini ko'rsatish] [men]
    CustomHeader(
        title = stringResource(Res.string.xarita),
        showBackButton = true,
        onBackClick = onBack,
        trailingIcon = {
            if (locatedCount > 1) {
                MapIslandButton(icon = Icons.Rounded.ZoomOutMap, onClick = { onEvent(TrackingEvent.FitAll) })
            }
            MapIslandButton(
                icon = Icons.Rounded.MyLocation,
                loading = state.isLocatingSelf,
                onClick = { if (!state.isLocatingSelf) onEvent(TrackingEvent.SelfClicked) },
            )
        },
    )

    val cardPerson = if (state.showPersonSheet) state.sheetPerson else null
    // Android "orqaga" — avval kartani yopadi
    BackHandler(cardPerson != null) { onEvent(TrackingEvent.DismissPersonSheet) }

    BoxWithConstraints(Modifier.fillMaxSize().background(AppColors.bg.page)) {
        val cardMax = maxHeight * 0.55f

        YandexMap(
            controller = mapController,
            initialCamera = initialCamera,
            markers = markers,
            circles = userAccuracyCircle,
            onMarkerClick = { id -> onEvent(TrackingEvent.MarkerClicked(id)) },
            onMapTap = { if (cardPerson != null) onEvent(TrackingEvent.DismissPersonSheet) },
            modifier = Modifier.fillMaxSize(),
            isDark = isDark,
            logoTopInset = statusTop + 8.dp,
        )

        // Status bar o'qilsin — tepadan yumshoq parda
        Box(
            Modifier
                .fillMaxWidth()
                .height(statusTop + 28.dp)
                .background(Brush.verticalGradient(listOf(AppColors.bg.page.copy(alpha = 0.85f), Color.Transparent)))
        )

        if (state.isLoading) {
            LinearProgressIndicator(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = statusTop + 4.dp, start = 24.dp, end = 24.dp)
                    .fillMaxWidth(),
                color = AppColors.action.primary,
                trackColor = AppColors.action.primary.copy(alpha = 0.16f),
            )
        }

        // Pastki qatlam — orol ustida: farzandlar qatori yoki tanlangan farzand kartasi
        val enterFade = MaterialTheme.motionScheme.defaultEffectsSpec<Float>()
        val enterSlide = MaterialTheme.motionScheme.defaultSpatialSpec<IntOffset>()
        val exitFade = MaterialTheme.motionScheme.fastEffectsSpec<Float>()
        AnimatedContent(
            targetState = cardPerson,
            contentKey = { it?.id },
            transitionSpec = {
                (fadeIn(enterFade) + slideInVertically(enterSlide) { it / 3 }) togetherWith
                    fadeOut(exitFade) using SizeTransform(clip = false)
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(bottom = bottomReserved),
            label = "mapBottom",
        ) { person ->
            if (person != null) {
                PersonLocationCard(
                    person = person,
                    issues = state.sheetIssues,
                    isCheckingStatus = state.isCheckingPermissionStatus,
                    maxHeight = cardMax,
                    onClose = { onEvent(TrackingEvent.DismissPersonSheet) },
                    onWatchVideo = onWatchVideo,
                    onOpenInMaps = onOpenInMaps,
                    onShare = onShare,
                )
            } else if (state.people.isNotEmpty()) {
                ChildrenRail(
                    people = state.people,
                    selectedId = state.selectedPersonId,
                    isLocked = { p ->
                        val limit = state.subscriptionLimits.firstOrNull { it.childId == p.id }
                        limit == null || limit.subscriptionType == SubscriptionType.FREE
                    },
                    onClick = { onEvent(TrackingEvent.PersonClicked(it)) },
                )
            } else {
                Spacer(Modifier.height(0.dp))
            }
        }
    }
}

/** Orol ichidagi xarita tugmasi (tonal doira, bosilganda shakl morfi). */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun MapIslandButton(icon: ImageVector, onClick: () -> Unit, loading: Boolean = false) {
    FilledTonalIconButton(
        onClick = onClick,
        modifier = Modifier.size(48.dp),
        shapes = IconButtonDefaults.shapes(),
        colors = IconButtonDefaults.filledTonalIconButtonColors(
            containerColor = AppColors.action.primary.copy(alpha = 0.14f),
            contentColor = AppColors.icon.accentPrimary,
        ),
    ) {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                strokeWidth = 2.dp,
                color = AppColors.icon.accentPrimary,
            )
        } else {
            Icon(icon, contentDescription = null, modifier = Modifier.size(22.dp))
        }
    }
}
