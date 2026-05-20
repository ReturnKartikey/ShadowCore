package com.shadowcore.app.ui.screens.premium

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shadowcore.app.billing.BillingManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PremiumUiState(
    val isPremium: Boolean = false,
    val price: String? = null,
    val isPurchasing: Boolean = false,
)

@HiltViewModel
class PremiumViewModel @Inject constructor(
    private val billingManager: BillingManager,
) : ViewModel() {

    private val _uiState = MutableStateFlow(PremiumUiState())
    val uiState: StateFlow<PremiumUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            billingManager.isPremium.collect { premium ->
                _uiState.update { it.copy(isPremium = premium) }
            }
        }
        viewModelScope.launch {
            billingManager.premiumPrice.collect { price ->
                _uiState.update { it.copy(price = price) }
            }
        }
    }

    fun purchase() {
        _uiState.update { it.copy(isPurchasing = true) }
        billingManager.launchPurchaseFlow()
        _uiState.update { it.copy(isPurchasing = false) }
    }

    fun restorePurchases() {
        billingManager.restorePurchases()
    }
}
