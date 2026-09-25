package uz.tikoncha_parent.presentation.profile

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import tikoncha_parents.composeapp.generated.resources.Res
import tikoncha_parents.composeapp.generated.resources.kutilmagan_xatolik_qayta_urining
import uz.tikoncha_parent.data.local.AppSettings
import uz.tikoncha_parent.data.mapper.toUserInfo
import uz.tikoncha_parent.domain.model.UploadPart
import uz.tikoncha_parent.domain.model.app_error.Outcome
import uz.tikoncha_parent.domain.repository.AvatarRepository
import uz.tikoncha_parent.domain.repository.ChildRepository
import uz.tikoncha_parent.domain.repository.DeviceRepository
import uz.tikoncha_parent.domain.repository.LoginRepository
import uz.tikoncha_parent.platform.Logger
import uz.tikoncha_parent.presentation.ui_state.ResponseState

class ProfileViewModel(
    private val loginRepository: LoginRepository,
    private val childRepository: ChildRepository,
    private val deviceRepository: DeviceRepository,
    private val avatarRepository: AvatarRepository,
) : ScreenModel {
    var userInfoJob: Job? = null
    var childrenJob: Job? = null
    var avatarJob: Job? = null
    var deleteAvatarJob: Job? = null
    var unlinkJob: Job? = null

    private val _state = MutableStateFlow(ProfileState())
    val state = _state.asStateFlow()

    init {
        Logger.d("ProfileViewModel", " AppSettings.userInfo=${AppSettings.userInfo}")
        _state.update {
            it.copy(
                userInfo = AppSettings.userInfo,
                children = AppSettings.children,
                profileImageUrl = AppSettings.profileImageUrl
            )
        }
    }

    fun onEvent(event: ProfileEvent) {
        when (event) {
            is ProfileEvent.OnAvatarPhotoSelected -> {
                uploadAvatar(event.part)
            }

            ProfileEvent.LoadAvatarFromServer -> {
                getAvatar()
            }

            is ProfileEvent.OnChangeProfilePhotoClicked -> {

            }

            is ProfileEvent.OnAvatarPreviewSelected -> {
                _state.update {
                    it.copy(
                        localAvatar = event.bitmap
                    )
                }
            }

            ProfileEvent.Refresh -> {
                userInfoJob()
                getChildren()
                getAvatar()
            }

            ProfileEvent.RequestLogout -> {
                logoutRequest()
            }

            ProfileEvent.Clear -> {
                _state.update {
                    it.copy(
                        logoutState = ResponseState.Idle
                    )
                }
            }

            ProfileEvent.ClearDeleteAvatarState -> {
                _state.update {
                    it.copy(
                        deleteAvatarState = ResponseState.Idle
                    )
                }
            }

            ProfileEvent.RequestDeleteAvatar -> {
                deleteAvatar()
            }

            is ProfileEvent.OnUnlinkClicked -> {
                _state.update {
                    it.copy(
                        unlinkTarget = event.child
                    )
                }
            }

            ProfileEvent.DismissUnlinkDialog -> {
                _state.update {
                    it.copy(
                        unlinkTarget = null
                    )
                }
            }

            ProfileEvent.ConfirmUnlink -> {
                unlinkChild()
            }

            ProfileEvent.ClearUnlinkState -> {
                _state.update {
                    it.copy(
                        unlinkState = ResponseState.Idle
                    )
                }
            }
        }
    }


    private fun unlinkChild() {
        val child = _state.value.unlinkTarget
        val parentId = AppSettings.userInfo?.userId ?: _state.value.userInfo?.userId ?: ""

        if (parentId.isEmpty()) {
            _state.update {
                it.copy(
                    unlinkState = ResponseState.Error(
                        res = Res.string.kutilmagan_xatolik_qayta_urining
                    )
                )
            }
            return
        }

        unlinkJob?.cancel()
        _state.update {
            it.copy(
                unlinkState = ResponseState.Loading
            )
        }

        unlinkJob = screenModelScope.launch {
            val res = childRepository.unlinkChild(
                childUserId = child?.userId ?: "",
                parentUserId = parentId
            )

            when (res) {
                is Outcome.Failure -> {
                    _state.update {
                        it.copy(
                            unlinkState = ResponseState.Error(failure = res)
                        )
                    }
                }

                is Outcome.Success -> {
                    val update = _state.value.children.filterNot { it.userId == child?.userId }

                    AppSettings.syncSelectedChildWith(update)

                    _state.update {
                        it.copy(
                            children = update,
                            unlinkTarget = null,
                            unlinkState = ResponseState.Success()
                        )
                    }
                    getChildren()
                }
            }
        }
    }

    private fun deleteAvatar() {
        deleteAvatarJob?.cancel()
        _state.update {
            it.copy(
                deleteAvatarState = ResponseState.Loading
            )
        }

        deleteAvatarJob = screenModelScope.launch {
            when (val res = avatarRepository.deleteAvatar()) {
                is Outcome.Failure -> {
                    _state.update {
                        it.copy(
                            deleteAvatarState = ResponseState.Error(failure = res)
                        )
                    }
                }

                is Outcome.Success -> {
                    AppSettings.profileImageUrl = ""
                    _state.update {
                        it.copy(
                            profileImageUrl = "",
                            localAvatar = null,
                            deleteAvatarState = ResponseState.Success()
                        )
                    }
                }
            }
        }
    }

    private fun uploadAvatar(part: UploadPart) {
        avatarJob?.cancel()

        avatarJob = screenModelScope.launch {
            when (val res = avatarRepository.uploadAvatar(part)) {
                is Outcome.Success -> {
                    val url = res.data.avatar_url ?: ""
                    AppSettings.profileImageUrl = url
                    _state.update { it.copy(profileImageUrl = url) }
                }

                is Outcome.Failure -> Unit
            }
        }
    }

    private fun getAvatar() {
        avatarJob?.cancel()

        avatarJob = screenModelScope.launch {
            when (val res = avatarRepository.getAvatarFromServer()) {
                is Outcome.Success -> {
                    val url = res.data.avatar_url ?: ""
                    AppSettings.profileImageUrl = url
                    _state.update { it.copy(profileImageUrl = url) }
                }

                is Outcome.Failure -> Unit
            }
        }
    }

    private fun logoutRequest() {
        _state.update {
            it.copy(
                logoutState = ResponseState.Loading
            )
        }
        screenModelScope.launch {
            when (val res = deviceRepository.logout(AppSettings.fcmToken)) {
                is Outcome.Failure -> {
                    _state.update {
                        it.copy(
                            logoutState = ResponseState.Error(failure = res)
                        )
                    }
                }

                is Outcome.Success -> {
                    AppSettings.clearSession()
                    _state.update {
                        it.copy(
                            logoutState = ResponseState.Success()
                        )
                    }
                }
            }
        }
    }

    private fun userInfoJob() {
        userInfoJob?.cancel()
        userInfoJob = screenModelScope.launch {
            when (val res = loginRepository.userInfo()) {
                is Outcome.Failure -> Unit
                is Outcome.Success -> {
                    val info = res.data.toUserInfo()
                    AppSettings.userInfo = info
                    _state.update {
                        it.copy(
                            children = AppSettings.children,
                            userInfo = info
                        )
                    }
                }
            }
        }
    }

    fun getChildren() {
        childrenJob?.cancel()
        childrenJob = screenModelScope.launch {
            when (val res = childRepository.children()) {
                is Outcome.Failure -> Unit
                is Outcome.Success -> {
                    AppSettings.syncSelectedChildWith(res.data)
                    _state.update { it.copy(children = AppSettings.children) }
                }
            }
        }
    }
}