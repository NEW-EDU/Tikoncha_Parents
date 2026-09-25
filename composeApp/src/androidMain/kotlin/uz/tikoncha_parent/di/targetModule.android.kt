package uz.tikoncha_parent.di

import com.google.android.gms.location.Priority
import dev.icerock.moko.geo.LocationTracker
import dev.icerock.moko.permissions.PermissionsController
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import uz.tikoncha_parent.data.in_app_update.InAppUpdateDataSource
import uz.tikoncha_parent.data.player.PlayerEngine
import uz.tikoncha_parent.domain.service.AndroidPaymentService
import uz.tikoncha_parent.domain.service.PaymentService
import uz.tikoncha_parent.platform.AndroidAppIconLoader
import uz.tikoncha_parent.platform.AppIconLoader
import uz.tikoncha_parent.platform.PlayCoreInAppUpdateDataSource
import uz.tikoncha_parent.presentation.tracking.TrackingScreenModel

actual val targetModule = module {
    single<AppIconLoader> { AndroidAppIconLoader(androidContext()) }
    single<PaymentService> { AndroidPaymentService(androidContext()) }
    single<InAppUpdateDataSource> { PlayCoreInAppUpdateDataSource(androidContext()) }
//    single<InAppUpdateDataSource> {
//        object : InAppUpdateDataSource {
//            override suspend fun checkUpdate() = UpdateStatus.UpdateAvailable(
//                allowedTypes = setOf(UpdateType.FLEXIBLE),
//                versionCodeAvailable = 99
//            )
//            override fun observeInstallEvents() = emptyFlow<InstallEvent>()
//            override suspend fun completeFlexibleUpdate() {}
//        }
//    }
    single {
        PermissionsController(
            applicationContext = androidContext()
        )
    }

    single {
        LocationTracker(
            permissionsController = get(),
            interval = 10000L,
            priority = Priority.PRIORITY_BALANCED_POWER_ACCURACY
        )
    }


    factory {
        TrackingScreenModel(
            childRepository = get(),
            locationTracker = get(),
            permissionsController = get(),
            paymentRepository = get(),
            permissionStatusRepository = get()
        )
    }


    single { PlayerEngine(androidContext()) }



}