package uz.tikoncha_parent.presentation.profile.coins

import uz.tikoncha_parent.domain.model.UserInfo
import uz.tikoncha_parent.domain.model.app_error.Outcome
import uz.tikoncha_parent.presentation.ui_state.ResponseState

data class CoinsState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: Outcome.Failure? = null,
    val myCoins: Int = 0,
    val coinsToBuy: Int = 0,
    val coinPrice: Int = 100,
    val totalPrice: Int = 0,
    val selectedChild: UserInfo? = null,
    val childrenList: List<UserInfo> = emptyList(),
    val coinPackageList: List<CoinPackageUi> = emptyList(),
    val childrenResponseState: ResponseState<Nothing> = ResponseState.Idle,
    val selectedPackageIndex: Int? = null,
){
    val continueButtonEnabled get() = coinsToBuy > 0
}