package com.shadowcore.app.domain.repository

import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for premium billing state.
 * Abstraction layer over Google Play Billing — domain has no billing SDK dependency.
 */
interface BillingRepository {
    val isPremium: Flow<Boolean>
    suspend fun launchPurchaseFlow(): Boolean
    suspend fun restorePurchases(): Boolean
    suspend fun getPremiumPrice(): String?
}
