package uz.tikoncha_parent.presentation.login

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import uz.tikoncha_parent.domain.model.app_error.ErrorCause
import uz.tikoncha_parent.domain.model.app_error.Outcome
import uz.tikoncha_parent.domain.model.auth.SocialAuthResult
import uz.tikoncha_parent.domain.model.auth.SocialProvider
import uz.tikoncha_parent.domain.model.auth.TelegramAuthResult
import uz.tikoncha_parent.domain.repository.AuthRepository
import uz.tikoncha_parent.domain.repository.SessionRepository
import uz.tikoncha_parent.domain.repository.SocialAuthRepository
import uz.tikoncha_parent.domain.repository.TelegramAuthRepository
import uz.tikoncha_parent.platform.BuildConfig
import uz.tikoncha_parent.presentation.profile.language.LanguagePrefs

class LoginViewModel(
    private val telegramAuth: TelegramAuthRepository,
    private val session: SessionRepository,
    private val auth: AuthRepository,
    private val socialAuth: SocialAuthRepository,
) : ScreenModel {

    private val _state = MutableStateFlow(LoginState())
    val state = _state.asStateFlow()

    private val _sideEffects = Channel<LoginSideEffect>(Channel.BUFFERED)
    val sideEffects = _sideEffects.receiveAsFlow()

    private var exchangeJob: Job? = null
    private var sendOtpJob: Job? = null

    init {
        val telegramInstalled = telegramAuth.isTelegramInstalled()
        val lang = LanguagePrefs.loadOrDefault().languageCode
        _state.update {
            it.copy(
                showPhone = !telegramInstalled || BuildConfig.isDebug,
                appleAvailable = socialAuth.isAppleAvailable(),
                socialFirst = lang != "uz" && lang != "ru",
            )
        }
        observeTelegramResult()
        observeSocialResult()
    }

    // ---------- Apple / Google ----------
    private fun observeSocialResult() {
        screenModelScope.launch {
            socialAuth.pendingResult.collect { result ->
                if (result == null) return@collect
                socialAuth.consumePending()
                when (result) {
                    is SocialAuthResult.Cancelled -> _state.update { it.copy(loadingProvider = null) }
                    is SocialAuthResult.Error -> _state.update {
                        it.copy(loadingProvider = null, error = Outcome.Failure(ErrorCause.Unknown, result.message))
                    }
                    is SocialAuthResult.Success -> exchangeSocial(result)
                }
            }
        }
    }

    private fun handleSocialClick(provider: SocialProvider) {
        val s = _state.value
        if (s.loadingProvider != null || s.isTelegramLoading) return
        if (socialAuth.start(provider)) {
            _state.update { it.copy(loadingProvider = provider, error = null) }
        } else {
            _state.update { it.copy(loadingProvider = null, error = Outcome.Failure(ErrorCause.Unknown)) }
        }
    }

    private fun exchangeSocial(result: SocialAuthResult.Success) {
        exchangeJob?.cancel()
        exchangeJob = screenModelScope.launch {
            _state.update { it.copy(loadingProvider = result.provider, error = null) }
            val res = when (result.provider) {
                SocialProvider.APPLE -> auth.appleLogin(result.token, result.fullName)
                SocialProvider.GOOGLE -> auth.googleLogin(result.token)
            }
            when (res) {
                is Outcome.Failure -> _state.update { it.copy(loadingProvider = null, error = res) }
                is Outcome.Success -> {
                    session.save(res.data)
                    _state.update { it.copy(loadingProvider = null) }
                    _sideEffects.send(
                        if (res.data.needsRegistration) LoginSideEffect.NavigateToRegister else LoginSideEffect.NavigateToHome
                    )
                }
            }
        }
    }

    private fun observeTelegramResult() {
        screenModelScope.launch {
            telegramAuth.pendingResult.collect { result ->
                if (result == null) return@collect
                handleTelegramResult(result)
                telegramAuth.consumePending()
            }
        }
    }

    fun onEvent(event: LoginEvent) {
        when (event) {
            is LoginEvent.OnNumberInsert -> {
                val clean = event.number.filter { it.isDigit() }.take(9)
                _state.update { it.copy(number = clean) }
            }

            LoginEvent.OnTelegramClicked -> handleTelegramClick()
            is LoginEvent.OnSocialClicked -> handleSocialClick(event.provider)
            LoginEvent.OnPhoneContinue -> handlePhoneContinue()
            LoginEvent.OnErrorDismissed ->
                _state.update { it.copy(error = null) }

            LoginEvent.OnDialogErrorDismissed ->
                _state.update { it.copy(dialogError = null) }

            LoginEvent.OnTelegramReturned -> {
                if (exchangeJob?.isActive != true) {
                    _state.update {
                        it.copy(
                            showPhone = true,
                            isTelegramLoading = false
                        )
                    }
                }
            }
        }
    }

    // ---------- Telegram ----------
    private fun handleTelegramClick() {
        if (_state.value.isTelegramLoading) return
        val started = telegramAuth.startLogin()
        if (started) {
            _state.update { it.copy(isTelegramLoading = true, error = null) }
        } else {
            _state.update {
                it.copy(
                    isTelegramLoading = false,
                    showPhone = true,
                    error = Outcome.Failure(ErrorCause.Unknown),
                )
            }
        }
    }

    private fun handleTelegramResult(result: TelegramAuthResult) {
        when (result) {
            TelegramAuthResult.Cancelled ->
                _state.update { it.copy(isTelegramLoading = false, showPhone = true) }

            is TelegramAuthResult.Error ->
                _state.update {
                    it.copy(
                        showPhone = true,
                        isTelegramLoading = false,
                        error = Outcome.Failure(
                            ErrorCause.Unknown, result.message
                        )
                    )
                }

            is TelegramAuthResult.Success -> exchange(result.idToken)
        }
    }

    private fun exchange(idToken: String) {
        exchangeJob?.cancel()
        exchangeJob = screenModelScope.launch {
            _state.update { it.copy(isTelegramLoading = true, error = null) }
            when (val res = auth.telegramLogin(idToken)) {
                is Outcome.Failure -> {
                    _state.update {
                        it.copy(
                            error = res,
                            showPhone = true,
                            isTelegramLoading = false
                        )
                    }
                }

                is Outcome.Success -> {
                    session.save(res.data)
                    _state.update { it.copy(isTelegramLoading = false) }
                    if (res.data.needsRegistration) {
                        _sideEffects.send(LoginSideEffect.NavigateToRegister)
                    } else {
                        _sideEffects.send(LoginSideEffect.NavigateToHome)
                    }
                }
            }
        }
    }

    // ---------- Phone OTP ----------
    private fun handlePhoneContinue() {
        val current = _state.value
        if (!current.isPhoneNumberValid) return
        if (current.isPhoneLoading) return

        sendOtpJob?.cancel()
        sendOtpJob = screenModelScope.launch {
            _state.update {
                it.copy(
                    isPhoneLoading = true,
                    dialogError = null,
                )
            }

            when (val res = auth.sendOtp(current.fullNumber)) {
                is Outcome.Failure -> _state.update {
                    it.copy(
                        isPhoneLoading = false,
                        dialogError = res,
                    )
                }

                is Outcome.Success -> {
                    _state.update { it.copy(isPhoneLoading = false) }
                    _sideEffects.send(LoginSideEffect.NavigateToOtp(current.fullNumber))
                }
            }
        }
    }
}