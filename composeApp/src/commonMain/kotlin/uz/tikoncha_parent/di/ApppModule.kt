package uz.tikoncha_parent.di

import PermissionStatusRepositoryImpl
import org.koin.dsl.module
import uz.tikoncha_parent.core.HttpClientEngineFactory
import uz.tikoncha_parent.data.remote.AvatarApiService
import uz.tikoncha_parent.data.remote.ChatApiService
import uz.tikoncha_parent.data.remote.ChatSocketService
import uz.tikoncha_parent.data.remote.ChildApiService
import uz.tikoncha_parent.data.remote.DeviceApiService
import uz.tikoncha_parent.data.remote.LoginApiService
import uz.tikoncha_parent.data.remote.MyCoinsApiService
import uz.tikoncha_parent.data.remote.NewApiService
import uz.tikoncha_parent.data.remote.PaymentApiService
import uz.tikoncha_parent.data.remote.PermissionStatusApiService
import uz.tikoncha_parent.data.remote.ProtectionApiService
import uz.tikoncha_parent.data.remote.TikonchaClient
import uz.tikoncha_parent.data.remote.TodoApiService
import uz.tikoncha_parent.data.remote.TutorialApiService
import uz.tikoncha_parent.data.remote.policy.PolicyApiService
import uz.tikoncha_parent.data.repository.AuthRepositoryImpl
import uz.tikoncha_parent.data.repository.AvatarRepositoryImpl
import uz.tikoncha_parent.data.repository.ChatRepositoryImpl
import uz.tikoncha_parent.data.repository.ChildRepositoryImpl
import uz.tikoncha_parent.data.repository.DeviceRepositoryImpl
import uz.tikoncha_parent.data.repository.LoginRepositoryImpl
import uz.tikoncha_parent.data.repository.MyCoinsRepositoryImpl
import uz.tikoncha_parent.data.repository.NewsRepositoryImpl
import uz.tikoncha_parent.data.repository.PaymentRepositoryImpl
import uz.tikoncha_parent.data.repository.PlayerRepositoryImpl
import uz.tikoncha_parent.data.repository.ProtectionRepositoryImpl
import uz.tikoncha_parent.data.repository.SessionRepositoryImpl
import uz.tikoncha_parent.data.repository.TelegramAuthRepositoryImpl
import uz.tikoncha_parent.data.repository.TodoRepositoryImpl
import uz.tikoncha_parent.data.repository.TutorialRepositoryImpl
import uz.tikoncha_parent.data.repository.UpdateRepositoryImpl
import uz.tikoncha_parent.data.repository.policy.PolicyAuditRepositoryImpl
import uz.tikoncha_parent.data.repository.policy.PolicyRepositoryImpl
import uz.tikoncha_parent.data.repository.policy.ProtectionPackRepositoryImpl
import uz.tikoncha_parent.data.repository.policy.QuickBlockRepositoryImpl
import uz.tikoncha_parent.domain.model.UserInfo
import uz.tikoncha_parent.domain.repository.AuthRepository
import uz.tikoncha_parent.domain.repository.AvatarRepository
import uz.tikoncha_parent.domain.repository.ChatRepository
import uz.tikoncha_parent.domain.repository.ChildRepository
import uz.tikoncha_parent.domain.repository.DeviceRepository
import uz.tikoncha_parent.domain.repository.LoginRepository
import uz.tikoncha_parent.domain.repository.MyCoinsRepository
import uz.tikoncha_parent.domain.repository.NewsRepository
import uz.tikoncha_parent.domain.repository.PaymentRepository
import uz.tikoncha_parent.domain.repository.PermissionStatusRepository
import uz.tikoncha_parent.domain.repository.PlayerRepository
import uz.tikoncha_parent.domain.repository.ProtectionRepository
import uz.tikoncha_parent.domain.repository.SessionRepository
import uz.tikoncha_parent.domain.repository.TelegramAuthRepository
import uz.tikoncha_parent.domain.repository.TodoRepository
import uz.tikoncha_parent.domain.repository.TutorialRepository
import uz.tikoncha_parent.domain.repository.UpdateRepository
import uz.tikoncha_parent.domain.repository.policy.PolicyAuditRepository
import uz.tikoncha_parent.domain.repository.policy.PolicyRepository
import uz.tikoncha_parent.domain.repository.policy.ProtectionPackRepository
import uz.tikoncha_parent.domain.repository.policy.QuickBlockRepository
import uz.tikoncha_parent.domain.use_case.app_usage.TodayUsageUseCase
import uz.tikoncha_parent.domain.use_case.app_usage.GetUsageHistoryUseCase
import uz.tikoncha_parent.domain.use_case.chat.GetChatMessagesFromServerUseCase
import uz.tikoncha_parent.domain.use_case.chat.ObserveChatStatusUseCase
import uz.tikoncha_parent.domain.use_case.chat.SendMessageApiUseCase
import uz.tikoncha_parent.domain.use_case.chat.SendMessageUseCase
import uz.tikoncha_parent.domain.use_case.in_app_update.CheckUpdateUseCase
import uz.tikoncha_parent.domain.use_case.in_app_update.CompleteFlexibleUpdateUseCase
import uz.tikoncha_parent.domain.use_case.in_app_update.ObserveInstallEventsUseCase
import uz.tikoncha_parent.domain.use_case.payment.GetPaymentTransactionsUseCase
import uz.tikoncha_parent.domain.use_case.payment.PurchaseIApPremiumUseCase
import uz.tikoncha_parent.domain.use_case.policy.AddQuickBlockUseCase
import uz.tikoncha_parent.domain.use_case.policy.CreatePolicyUseCase
import uz.tikoncha_parent.domain.use_case.policy.DeletePolicyUseCase
import uz.tikoncha_parent.domain.use_case.policy.GetPolicyEventsUseCase
import uz.tikoncha_parent.domain.use_case.policy.GetProtectionPackStatusesUseCase
import uz.tikoncha_parent.domain.use_case.policy.ObservePoliciesUseCase
import uz.tikoncha_parent.domain.use_case.policy.ObserveQuickBlocksUseCase
import uz.tikoncha_parent.domain.use_case.policy.PausePolicyUseCase
import uz.tikoncha_parent.domain.use_case.policy.RefreshPoliciesUseCase
import uz.tikoncha_parent.domain.use_case.policy.RefreshQuickBlocksUseCase
import uz.tikoncha_parent.domain.use_case.policy.RemoveQuickBlockUseCase
import uz.tikoncha_parent.domain.use_case.policy.ObserveQuickBlockSnapshotUseCase
import uz.tikoncha_parent.domain.use_case.policy.ToggleQuickBlockUseCase
import uz.tikoncha_parent.domain.use_case.policy.ChildPaidStatusUseCase
import uz.tikoncha_parent.domain.use_case.policy.UpdateOwnQuickBlockUseCase
import uz.tikoncha_parent.domain.use_case.policy.AddQuickBlockAppsUseCase
import uz.tikoncha_parent.domain.use_case.policy.GetBlockableChildAppsUseCase
import uz.tikoncha_parent.domain.use_case.policy.TogglePresetUseCase
import uz.tikoncha_parent.domain.use_case.policy.ToggleContentProtectionUseCase
import uz.tikoncha_parent.domain.use_case.policy.SavePolicyDraftUseCase
import uz.tikoncha_parent.domain.use_case.policy.TogglePolicyUseCase
import uz.tikoncha_parent.domain.use_case.policy.ToggleProtectionPackUseCase
import uz.tikoncha_parent.domain.use_case.policy.UpdatePolicyUseCase
import uz.tikoncha_parent.domain.use_case.todo.CompleteTodoUseCase
import uz.tikoncha_parent.domain.use_case.todo.CreateTodoUseCase
import uz.tikoncha_parent.domain.use_case.todo.DeleteTodoUseCase
import uz.tikoncha_parent.domain.use_case.todo.GetTodosUseCase
import uz.tikoncha_parent.domain.use_case.todo.UpdateTodoUseCase
import uz.tikoncha_parent.platform.PlatformPurchaseService
import uz.tikoncha_parent.presentation.add_child.AddChildViewModel
import uz.tikoncha_parent.presentation.chat.ChatConnectionManager
import uz.tikoncha_parent.presentation.chat.chat_details.ChatDetailsViewModel
import uz.tikoncha_parent.presentation.chat.chat_list.ChatViewModel
import uz.tikoncha_parent.presentation.chat.chat_room.ChatRoomViewModel
import uz.tikoncha_parent.presentation.child_confirm_cod.ChildConfirmViewModel
import uz.tikoncha_parent.presentation.in_app_update.UpdateViewModel
import uz.tikoncha_parent.presentation.login.LoginViewModel
import uz.tikoncha_parent.presentation.new_home.HomeViewModel
import uz.tikoncha_parent.presentation.notification.NotificationViewModel
import uz.tikoncha_parent.presentation.otp.OtpViewmodel
import uz.tikoncha_parent.presentation.player.PlayerScreenModel
import uz.tikoncha_parent.presentation.policy.app_site_selection.AppWebViewModel
import uz.tikoncha_parent.presentation.policy.history.PolicyHistoryViewModel
import uz.tikoncha_parent.presentation.policy.limit_rule.setup.LimitRuleSetupViewModel
import uz.tikoncha_parent.presentation.policy.policy_list.PolicyViewModel
import uz.tikoncha_parent.presentation.policy.policy_setup.PolicySetupViewModel
import uz.tikoncha_parent.presentation.policy.protection_packs.ProtectionPacksViewModel
import uz.tikoncha_parent.presentation.policy.shared.PolicySharedModel
import uz.tikoncha_parent.presentation.policy.time_rule.setup.TimeRuleSetupViewModel
import uz.tikoncha_parent.presentation.profile.ProfileViewModel
import uz.tikoncha_parent.presentation.profile.child_user_edit.ChildInfoEditViewModel
import uz.tikoncha_parent.presentation.profile.coin_purchase.CoinPurchaseViewModel
import uz.tikoncha_parent.presentation.profile.coins.CoinsViewModel
import uz.tikoncha_parent.presentation.profile.logout.LogoutViewModel
import uz.tikoncha_parent.presentation.profile.payment_history.PaymentHistoryScreenModel
import uz.tikoncha_parent.presentation.profile.subscription.payment.PaymentViewModel
import uz.tikoncha_parent.presentation.profile.subscription.subscription_info.SubscriptionViewModel
import uz.tikoncha_parent.presentation.profile.subscription.subscription_payment.SubscriptionPaymentViewModel
import uz.tikoncha_parent.presentation.profile.user_edit.UserInfoEditViewModel
import uz.tikoncha_parent.presentation.protection.ProtectionViewModel
import uz.tikoncha_parent.presentation.register.RegisterViewmodel
import uz.tikoncha_parent.presentation.statistic.StatisticViewModel
import uz.tikoncha_parent.presentation.policy.quick.QuickBlockViewModel
import uz.tikoncha_parent.presentation.task.TaskListViewModel
import uz.tikoncha_parent.presentation.task.completed_task.CompletedTaskViewModel
import uz.tikoncha_parent.presentation.task.create_task.CreateTaskViewModel
import uz.tikoncha_parent.presentation.video_tutorial.VideoTutorialScreenModel

val sharedModule = module {
    single {
        HttpClientEngineFactory().getHttpEngine()
    }

    single {
        TikonchaClient(get()).client
    }


    single {
        PlatformPurchaseService()
    }

    // Api Service
    single { LoginApiService(get()) }
    single { TodoApiService(get()) }
    single { ChildApiService(get()) }
    single { AvatarApiService(get()) }
    single { ChatApiService(get()) }
    single { ChatSocketService(get()) }
    single { DeviceApiService(get()) }
    single { NewApiService(get()) }
    single { MyCoinsApiService(get()) }
    single { PolicyApiService(get()) }
    single { PaymentApiService(get()) }
    single { PermissionStatusApiService(get()) }
    single { TutorialApiService(get()) }

    single { ProtectionApiService(get()) }

    // Repository
    single<LoginRepository> { LoginRepositoryImpl(get()) }
    single<TodoRepository> { TodoRepositoryImpl(get()) }
    single<ChildRepository> { ChildRepositoryImpl(get()) }
    single<AvatarRepository> { AvatarRepositoryImpl(get()) }
    single<ChatRepository> { ChatRepositoryImpl(get(), get()) }
    single<DeviceRepository> { DeviceRepositoryImpl(get()) }
    single<NewsRepository> { NewsRepositoryImpl(get()) }
    single<MyCoinsRepository> { MyCoinsRepositoryImpl(get()) }
    single<PolicyRepository> { PolicyRepositoryImpl(get()) }
    single<QuickBlockRepository> { QuickBlockRepositoryImpl(get()) }
    single<ProtectionPackRepository> { ProtectionPackRepositoryImpl(get()) }
    single<PolicyAuditRepository> { PolicyAuditRepositoryImpl(get()) }
    single<PaymentRepository> { PaymentRepositoryImpl(get()) }
    single<UpdateRepository> { UpdateRepositoryImpl(get()) }
    single<PermissionStatusRepository> { PermissionStatusRepositoryImpl(get()) }

    single<PlayerRepository> { PlayerRepositoryImpl(get()) }
    single<TutorialRepository> { TutorialRepositoryImpl(get()) }

    single<TelegramAuthRepository> { TelegramAuthRepositoryImpl() }
    single<ProtectionRepository> { ProtectionRepositoryImpl(get()) }
    single<SessionRepository> { SessionRepositoryImpl() }
    single<AuthRepository> { AuthRepositoryImpl(get()) }


    // Use Case Module
    single { ObserveChatStatusUseCase(get()) }
    single { GetChatMessagesFromServerUseCase(get()) }
    single { SendMessageUseCase(get()) }
    single { SendMessageApiUseCase(get()) }

    single { ChatConnectionManager(get()) }
    single { PurchaseIApPremiumUseCase(get()) }
    single { CheckUpdateUseCase(get()) }
    single { ObserveInstallEventsUseCase(get()) }
    single { CompleteFlexibleUpdateUseCase(get()) }
    factory { GetPaymentTransactionsUseCase(get()) }

    single { GetTodosUseCase(get()) }
    single { CreateTodoUseCase(get()) }
    single { UpdateTodoUseCase(get()) }
    single { DeleteTodoUseCase(get()) }
    single { CompleteTodoUseCase(get()) }
    single { TodayUsageUseCase(get()) }
    single { GetUsageHistoryUseCase(get()) }
    // Jadvallar
    single { ObservePoliciesUseCase(get()) }
    single { RefreshPoliciesUseCase(get(), get()) }
    single { CreatePolicyUseCase(get()) }
    single { UpdatePolicyUseCase(get()) }
    single { DeletePolicyUseCase(get()) }
    single { TogglePolicyUseCase(get()) }
    single { PausePolicyUseCase(get()) }
    single { GetPolicyEventsUseCase(get()) }
    // Tezkor blok
    single { ObserveQuickBlocksUseCase(get()) }
    single { RefreshQuickBlocksUseCase(get()) }
    single { AddQuickBlockUseCase(get()) }
    single { RemoveQuickBlockUseCase(get()) }
    single { ObserveQuickBlockSnapshotUseCase(get()) }
    single { ToggleQuickBlockUseCase(get()) }
    single { ChildPaidStatusUseCase(get()) }
    single { UpdateOwnQuickBlockUseCase(get()) }
    single { AddQuickBlockAppsUseCase(get()) }
    single { GetBlockableChildAppsUseCase(get()) }
    single { TogglePresetUseCase(get()) }
    single { ToggleContentProtectionUseCase(get()) }
    single { SavePolicyDraftUseCase(get(), get()) }
    // Himoya paketlari
    single { GetProtectionPackStatusesUseCase(get(), get()) }
    single { ToggleProtectionPackUseCase(get()) }

    // ViewModel
    factory { LoginViewModel(get(), get(), get()) }
    factory { OtpViewmodel(get(), get()) }
    factory { RegisterViewmodel(get(), get()) }
    factory { ProfileViewModel(get(), get(), get(), get()) }
    factory { LogoutViewModel(get()) }
    factory { AddChildViewModel(get()) }
    factory { ChildConfirmViewModel() }
    factory { TaskListViewModel(get(), get(), get()) }
    factory { CreateTaskViewModel(get(), get(), get()) }
    factory { CompletedTaskViewModel(get()) }
    factory { ChatViewModel(get(), get()) }
    factory {
        StatisticViewModel(
            childRepository = get(),
            permissionStatusRepository = get(),
            getUsageHistory = get(),
            observeQuickBlockSnapshot = get(),
            refreshQuickBlocks = get(),
            toggleQuickBlock = get(),
            childPaidStatus = get(),
        )
    }
    factory {
        HomeViewModel(
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get()
        )
    }
    factory {
        ChatRoomViewModel(
            get(),
            get(),
            get(),
            get(),
            get(),
        )
    }
    factory {
        PolicyViewModel(
            observePolicies = get(),
            refreshPolicies = get(),
            togglePolicy = get(),
            pausePolicy = get(),
            permissionStatusRepository = get(),
            childRepository = get(),
            observeQuickBlocks = get(),
            refreshQuickBlocks = get(),
        )
    }
    factory { ProtectionPacksViewModel(get(), get(), get()) }
    factory {
        uz.tikoncha_parent.presentation.policy.preset.PresetPolicyViewModel(
            observePolicies = get(),
            getApps = get(),
            getUsageHistory = get(),
            saveDraft = get(),
            togglePolicy = get(),
            pausePolicy = get(),
        )
    }
    factory {
        uz.tikoncha_parent.presentation.policy.list.PolicyListViewModel(
            childRepository = get(),
            observePolicies = get(),
            refreshPolicies = get(),
            observeQuickSnapshot = get(),
            refreshQuickBlocks = get(),
            getPackStatuses = get(),
            togglePreset = get(),
            togglePolicy = get(),
            toggleProtection = get(),
            updateOwnQuick = get(),
            childPaidStatus = get(),
        )
    }
    factory {
        QuickBlockViewModel(
            observeSnapshot = get(),
            refreshQuickBlocks = get(),
            updateOwn = get(),
            addApps = get(),
            removeQuickBlock = get(),
            getApps = get(),
            childPaidStatus = get(),
        )
    }
    factory { PolicyHistoryViewModel(get(), get()) }
    factory { NotificationViewModel(get()) }
    factory { TimeRuleSetupViewModel() }
    factory { LimitRuleSetupViewModel() }
    factory { PolicySetupViewModel(get(), get(), get()) }
    single { PolicySharedModel() }
    factory { AppWebViewModel(get()) }
    factory { PaymentViewModel(get(), get(), get()) }
    factory { SubscriptionPaymentViewModel(get()) }
    factory { CoinsViewModel(get(), get(), get()) }
    factory { (child: UserInfo) ->
        ChildInfoEditViewModel(
            loginRepository = get(),
            child = child
        )
    }
    factory { (userInfo: UserInfo) ->
        UserInfoEditViewModel(
            loginRepository = get(),
            userInfo = userInfo
        )
    }
    factory { CoinPurchaseViewModel(get()) }
    single { UpdateViewModel(get(), get(), get()) }
    factory { PlayerScreenModel(get(), get()) }
    factory { VideoTutorialScreenModel(get()) }
    factory { PaymentHistoryScreenModel(get()) }
    factory { SubscriptionViewModel(get()) }
    factory { ProtectionViewModel(get(), get()) }
    factory { ChatDetailsViewModel(get()) }
}