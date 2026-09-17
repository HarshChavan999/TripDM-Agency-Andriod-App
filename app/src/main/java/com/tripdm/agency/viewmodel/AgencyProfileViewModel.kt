package com.tripdm.agency.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tripdm.agency.data.model.AgencyProfile
import com.tripdm.agency.data.model.CreditPlan
import com.tripdm.agency.data.model.CreditTransaction
import com.tripdm.agency.data.repository.AgencyCreditsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AgencyProfileViewModel(
    private val creditsRepository: AgencyCreditsRepository = AgencyCreditsRepository()
) : ViewModel() {

    private val _transactions = MutableStateFlow<List<CreditTransaction>>(emptyList())
    val transactions: StateFlow<List<CreditTransaction>> = _transactions.asStateFlow()

    private val _creditPlans = MutableStateFlow<List<CreditPlan>>(com.tripdm.agency.data.model.SampleCreditPlans)
    val creditPlans: StateFlow<List<CreditPlan>> = _creditPlans.asStateFlow()

    private val _isPurchasing = MutableStateFlow(false)
    val isPurchasing: StateFlow<Boolean> = _isPurchasing.asStateFlow()

    private val _purchaseSuccess = MutableStateFlow<String?>(null)
    val purchaseSuccess: StateFlow<String?> = _purchaseSuccess.asStateFlow()

    init {
        viewModelScope.launch {
            creditsRepository.observeCreditPlans().collect { plans ->
                _creditPlans.value = plans
            }
        }
    }

    fun loadTransactions(agencyId: String) {
        viewModelScope.launch {
            creditsRepository.observeTransactions(agencyId).collect { txs ->
                _transactions.value = txs
            }
        }
    }

    fun purchasePlan(agencyId: String, plan: CreditPlan) {
        viewModelScope.launch {
            _isPurchasing.value = true
            creditsRepository.purchasePlan(agencyId, plan).fold(
                onSuccess = {
                    _purchaseSuccess.value = "Successfully purchased ${plan.name}! +${plan.credits} credits added."
                },
                onFailure = { err ->
                    _purchaseSuccess.value = "Purchase error: ${err.message}"
                }
            )
            _isPurchasing.value = false
        }
    }

    fun clearPurchaseMessage() {
        _purchaseSuccess.value = null
    }
}
