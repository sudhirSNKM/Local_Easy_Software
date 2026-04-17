package com.sudhir.localeasy1.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sudhir.localeasy1.data.Business
import com.sudhir.localeasy1.repository.BusinessRepository
import kotlinx.coroutines.launch

class SuperAdminViewModel : ViewModel() {
    private val businessRepository = BusinessRepository()

    private val _businesses = MutableLiveData<List<Business>>()
    val businesses: LiveData<List<Business>> = _businesses

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    init {
        loadBusinesses()
    }

    private fun loadBusinesses() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val pending = businessRepository.getPendingBusinesses()
                val approved = businessRepository.getApprovedBusinesses()
                _businesses.value = (pending + approved).distinctBy { it.id }
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to load businesses"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun approveBusiness(businessId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val result = businessRepository.approveBusiness(businessId)
                result.onSuccess {
                    loadBusinesses()
                }.onFailure { e ->
                    _error.value = e.message ?: "Failed to approve business"
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to approve business"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun rejectBusiness(businessId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val result = businessRepository.rejectBusiness(businessId)
                result.onSuccess {
                    loadBusinesses()
                }.onFailure { e ->
                    _error.value = e.message ?: "Failed to reject business"
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to reject business"
            } finally {
                _isLoading.value = false
            }
        }
    }
}
