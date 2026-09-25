package uz.tikoncha_parent.di

import dev.icerock.moko.geo.LocationTracker
import dev.icerock.moko.permissions.ios.PermissionsController
import dev.icerock.moko.permissions.ios.PermissionsControllerProtocol
import org.koin.dsl.bind
import org.koin.dsl.module
import platform.CoreLocation.kCLLocationAccuracyBest
import uz.tikoncha_parent.data.in_app_update.InAppUpdateDataSource
import uz.tikoncha_parent.data.player.PlayerEngine
import uz.tikoncha_parent.domain.service.IOSPaymentService
import uz.tikoncha_parent.domain.service.PaymentService
import uz.tikoncha_parent.platform.AppIconLoader
import uz.tikoncha_parent.platform.IosAppIconLoader
import uz.tikoncha_parent.platform.IosInAppUpdateDataSource
import uz.tikoncha_parent.presentation.policy.location_rule.LocationRuleScreenModel
import uz.tikoncha_parent.presentation.tracking.TrackingScreenModel

actual val targetModule = module {
    single<AppIconLoader> { IosAppIconLoader() }
    single<PaymentService> { IOSPaymentService() }
    single<InAppUpdateDataSource> { IosInAppUpdateDataSource() }

    single { PermissionsController() } bind PermissionsControllerProtocol::class

    single {
        LocationTracker(
            permissionsController = get(),
            accuracy = kCLLocationAccuracyBest
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

    factory { LocationRuleScreenModel(get(), get()) }

    single { PlayerEngine() }
}