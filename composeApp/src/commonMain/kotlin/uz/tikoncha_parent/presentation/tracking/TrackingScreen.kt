package uz.tikoncha_parent.presentation.tracking

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import coil3.compose.AsyncImage
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
import tikoncha_parents.composeapp.generated.resources.arrow_back
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
import uz.tikoncha_parent.presentation.live.ParentLiveScreen
import uz.tikoncha_parent.presentation.map.CameraPosition
import uz.tikoncha_parent.presentation.map.LatLng
import uz.tikoncha_parent.presentation.map.MapCircle
import uz.tikoncha_parent.presentation.map.MapController
import uz.tikoncha_parent.presentation.map.MapMarker
import uz.tikoncha_parent.presentation.map.MarkerStyle
import uz.tikoncha_parent.presentation.map.YandexMap
import uz.tikoncha_parent.presentation.map.rememberMapController
import uz.tikoncha_parent.presentation.profile.subscription.subscription_payment.SubscriptionPaymentScreen
import uz.tikoncha_parent.ui.Space
import uz.tikoncha_parent.ui.theme.AppColors
import uz.tikoncha_parent.ui.theme.AppTypography
import uz.tikoncha_parent.ui.theme.rememberIsDarkTheme
import uz.tikoncha_parent.ui.theme.rememberScreenSystemBars

private val ANDIJAN_CENTER = LatLng(40.7821, 72.3442)
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
            onBack = { navigator?.pop() }
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

        // ============== BOTTOM SHEET ==============

        PersonInfoSheet(
            show = state.showPersonSheet,
            onDismiss = { event(TrackingEvent.DismissPersonSheet) },
            person = state.sheetPerson,
            issues = state.sheetIssues,
            isCheckingStatus = state.isCheckingPermissionStatus,
            onWatchVideo = {
                // Jonli ko'rish — bolaga "chaqiruv" yuboradi; bola rozi bo'lgach jonli A/V + GPS.
                state.sheetPerson?.let { p -> navigator?.push(ParentLiveScreen(p.id)) }
            },
            onOpenInMaps = { person ->
                Logger.d("TrackingScreen", "person = $person")
                person.location?.let { loc ->
                    openInExternalMaps(loc.lat, loc.lon, person.name)
                }
            },
            onShare = { person ->
                Logger.d("TrackingScreen", "person = $person")
                person.location?.let { loc ->
                    shareLocation(loc.lat, loc.lon, person.name)
                }
            },
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

@Composable
fun TrackingContent(
    state: TrackingState,
    mapController: MapController,
    onEvent: (TrackingEvent) -> Unit,
    placeholderBitmap: ImageBitmap,
    selfText: String,                    // ⬅️ YANGI
    onBack: () -> Unit
) {
    val isDark = rememberIsDarkTheme()

    val initialCenter = remember(state.people, state.self) {
        state.self?.location
            ?: state.people.firstNotNullOfOrNull { it.location }
            ?: ANDIJAN_CENTER
    }

    val initialZoom = remember(state.people) {
        when {
            state.people.firstOrNull { it.isSelf }?.location != null -> 14f
            state.people.any { it.location != null } -> 13f
            else -> 11f
        }
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

    val systemBars = rememberScreenSystemBars(
        statusBarColor = AppColors.bg.page.copy(alpha = 0.3f),
        navigationBarColor = AppColors.bg.page.copy(alpha = 0.3f)
    )

    Box(Modifier.then(systemBars.modifier).fillMaxSize()) {

        YandexMap(
            controller = mapController,
            initialCamera = CameraPosition(initialCenter, zoom = initialZoom),
            markers = markers,
            circles = userAccuracyCircle,
            onMarkerClick = { id -> onEvent(TrackingEvent.MarkerClicked(id)) },
            modifier = Modifier.fillMaxSize(),
            isDark = isDark,
        )

        // ── Back FAB (TopStart) ──
        FloatingActionButton(
            onClick = onBack,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(top = 8.dp, start = 16.dp)
                .size(52.dp),
            containerColor = AppColors.modal.primary,
            contentColor = AppColors.icon.primary,
        ) {
            Icon(painter = painterResource(Res.drawable.arrow_back), contentDescription = null)
        }

        // ⬇️ Self FAB (TopEnd — LocationRule kabi)
        FloatingActionButton(
            onClick = { if (!state.isLocatingSelf) onEvent(TrackingEvent.SelfClicked) },
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.TopEnd)                   // ⬅️ BottomEnd → TopEnd
                .padding(top = 8.dp, end = 16.dp)          // ⬅️ LocationRule kabi
                .size(52.dp),
            containerColor = AppColors.modal.primary,
            contentColor = AppColors.icon.primary,
        ) {
            if (state.isLocatingSelf) {
                CircularProgressIndicator(
                    modifier = Modifier.size(22.dp),
                    strokeWidth = 2.dp,
                    color = AppColors.icon.primary,
                )
            } else {
                Icon(Icons.Default.MyLocation, contentDescription = null)
            }
        }

        // ── PeopleStrip (BottomCenter) — o'zgarmaydi ──
        PeopleStrip(
            people = state.people,
            selectedId = state.selectedPersonId,
            onClick = { onEvent(TrackingEvent.PersonClicked(it)) },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(bottom = 16.dp),
        )

        if (state.isLoading) {
            LinearProgressIndicator(
                Modifier.fillMaxWidth().align(Alignment.TopStart)
            )
        }
    }
}


@Composable
private fun PeopleStrip(
    people: List<Person>,
    selectedId: String?,
    onClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(people, key = { it.id }) { person ->
            PersonCard(
                person = person,
                selected = person.id == selectedId,
                onClick = { onClick(person.id) }
            )
        }
    }
}

@Composable
private fun PersonCard(
    person: Person,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = AppColors.bg.tertiary,
        tonalElevation = 2.dp,
        shadowElevation = 3.dp,
        border = if (selected) BorderStroke(2.dp, AppColors.border.accentEmphasis) else null
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
        ) {
            Avatar(
                person = person,
                modifier = Modifier.padding(8.dp)
            )
            Text(
                text = person.name,
                style = AppTypography.titleSmMedium,
                color = AppColors.text.primary
            )
            Space(16.dp)
        }
    }
}

@Composable
private fun Avatar(
    modifier: Modifier = Modifier,
    person: Person
) {
    Box(
        modifier = modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(AppColors.bg.primaryContainer),
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            model = person.avatarUrl,
            placeholder = painterResource(Res.drawable.profile_hedgehog_img),
            error = painterResource(Res.drawable.profile_hedgehog_img),
            fallback = painterResource(Res.drawable.profile_hedgehog_img),
            modifier = Modifier.fillMaxSize(),
            contentDescription = "",
            contentScale = ContentScale.Crop
        )
    }
}