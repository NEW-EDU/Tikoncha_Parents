package uz.tikoncha_parent.data.mapper

import uz.tikoncha_parent.data.remote.model.SubscriptionLimitDto
import uz.tikoncha_parent.data.remote.model.SubscriptionPlansData
import uz.tikoncha_parent.domain.model.SubscriptionLimit
import uz.tikoncha_parent.domain.model.SubscriptionType
import uz.tikoncha_parent.presentation.profile.subscription.subscription_payment.PlanUi
import uz.tikoncha_parent.presentation.profile.subscription.subscription_payment.SubscriptionUi

fun SubscriptionPlansData.toSubscriptionPlanUi(): SubscriptionUi {
    return SubscriptionUi(
        planId = id,
        type = SubscriptionType.fromString(name),
        monthly = PlanUi(
            price = monthly.price,
            coin = monthly.coin,
            feature = monthly.feature,
            bonus = monthly.bonus
        ),
        annual = PlanUi(
            price = annual.price,
            coin = annual.coin,
            feature = annual.feature,
            bonus = annual.bonus
        ),
        cardMonthly = prices.firstOrNull { it.method == "CARD" && it.duration == "MONTHLY" }?.price,
        cardAnnual = prices.firstOrNull { it.method == "CARD" && it.duration == "ANNUAL" }?.price,
    )
}

fun SubscriptionLimitDto.toSubscriptionLimit(): SubscriptionLimit{
    return SubscriptionLimit(
        childId = child_id?:"",
        name = first_name?:"",
        lastname = last_name?:"",
        subscriptionType = SubscriptionType.fromString(subscription),
        appUsageDaily = app_usage.app_usage_daily ?: Int.MAX_VALUE,
        appUsageWeekly = app_usage.app_usage_weekly ?: Int.MAX_VALUE,
        policyCount = policy.policy_count ?: Int.MAX_VALUE,
        timeRule = policy.time_rule ?: Int.MAX_VALUE,
        limitRule = policy.limit_rule ?: Int.MAX_VALUE,
//        locationRule = policy.location_rule ?: Int.MAX_VALUE,
        locationRule = 1,
        wifiRule = policy.wifi_rule ?: Int.MAX_VALUE,
        appLaunchCountRule = policy.app_launch_count_rule ?: Int.MAX_VALUE,
        appCount = policy.app_count ?: Int.MAX_VALUE
    )
}