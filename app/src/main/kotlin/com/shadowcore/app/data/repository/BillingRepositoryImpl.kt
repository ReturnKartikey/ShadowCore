package com.shadowcore.app.data.repository

import com.shadowcore.app.billing.BillingManager
import com.shadowcore.app.domain.repository.BillingRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BillingRepositoryImpl @Inject constructor(
    private val billingManager: BillingManager,
) : BillingRepository {

    override val isPremium: Flow<Boolean> = billingManager.isPremium

    override suspend fun launchPurchaseFlow(): Boolean {
        return billingManager.launchPurchaseFlow()
    }

    override suspend fun restorePurchases(): Boolean {
        billingManager.restorePurchases()
        return true
    }

    override suspend fun getPremiumPrice(): String? {
        return billingManager.premiumPrice.value
    }
}
