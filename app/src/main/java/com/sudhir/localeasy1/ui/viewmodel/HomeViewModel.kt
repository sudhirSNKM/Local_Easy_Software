package com.sudhir.localeasy1.ui.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sudhir.localeasy1.data.Service
import com.sudhir.localeasy1.repository.ServiceRepository
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {
    private val serviceRepository = ServiceRepository()

    private val _services = MutableLiveData<List<Service>>()
    val services: LiveData<List<Service>> = _services

    private val _categories = MutableLiveData<List<String>>()
    val categories: LiveData<List<String>> = _categories

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    init {
        loadServices()
        loadCategories()
    }

    private fun loadServices() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val serviceList = serviceRepository.getAllServices()
                _services.value = serviceList
            } catch (e: Exception) {
                _error.value = "Error loading services: ${e.message}"
                _services.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun loadCategories() {
        // Sample categories - in real app, fetch from backend
        _categories.value = listOf("All", "Salon", "Clinic", "Gym", "Spa", "Restaurant", "Cleaning")
    }

    fun loadServicesByCategory(category: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val serviceList = serviceRepository.getServicesByCategory(category)
                Log.d("HomeViewModel", "Category: $category, Found services: ${serviceList.size}")
                for (service in serviceList) {
                    Log.d("HomeViewModel", "Service: ${service.name}, Category: ${service.category}, BusinessId: ${service.businessId}")
                }
                // Show only real data from database - no sample data fallback
                _services.value = serviceList
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Error loading services for category $category", e)
                _error.value = "Error loading services: ${e.message}"
                _services.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadAllServices() {
        loadServices()
    }
}
