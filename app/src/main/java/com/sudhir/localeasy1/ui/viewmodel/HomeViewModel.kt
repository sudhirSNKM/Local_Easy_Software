package com.sudhir.localeasy1.ui.viewmodel

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
                // If no services from Firebase, provide sample data
                if (serviceList.isEmpty()) {
                    _services.value = getSampleServices()
                } else {
                    _services.value = serviceList
                }
            } catch (e: Exception) {
                // If Firebase fails, show sample data
                _error.value = "Using offline data: ${e.message}"
                _services.value = getSampleServices()
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun loadCategories() {
        // Sample categories - in real app, fetch from backend
        _categories.value = listOf("Salon", "Clinic", "Gym", "Spa", "Restaurant", "Cleaning")
    }

    fun loadServicesByCategory(category: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val serviceList = serviceRepository.getServicesByCategory(category)
                // If no services from Firebase, filter sample data
                if (serviceList.isEmpty()) {
                    _services.value = getSampleServices().filter { it.category == category }
                } else {
                    _services.value = serviceList
                }
            } catch (e: Exception) {
                // If Firebase fails, filter sample data
                _error.value = "Using offline data: ${e.message}"
                _services.value = getSampleServices().filter { it.category == category }
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun getSampleServices(): List<Service> {
        return listOf(
            Service(
                id = "1",
                name = "Hair Cut & Styling",
                price = 500,
                duration = "45 mins",
                category = "Salon",
                businessId = "biz1"
            ),
            Service(
                id = "2",
                name = "General Checkup",
                price = 800,
                duration = "30 mins",
                category = "Clinic",
                businessId = "biz2"
            ),
            Service(
                id = "3",
                name = "Gym Membership",
                price = 2000,
                duration = "1 month",
                category = "Gym",
                businessId = "biz3"
            ),
            Service(
                id = "4",
                name = "Swedish Massage",
                price = 1500,
                duration = "60 mins",
                category = "Spa",
                businessId = "biz1"
            ),
            Service(
                id = "5",
                name = "Home Cleaning",
                price = 1200,
                duration = "2 hours",
                category = "Cleaning",
                businessId = "biz4"
            )
        )
    }
}
