package com.sudhir.localeasy1.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sudhir.localeasy1.data.Booking
import com.sudhir.localeasy1.data.Service
import com.sudhir.localeasy1.repository.BookingRepository
import com.sudhir.localeasy1.repository.ServiceRepository
import kotlinx.coroutines.launch

class AdminViewModel : ViewModel() {
    private val bookingRepository = BookingRepository()
    private val serviceRepository = ServiceRepository()

    private val _bookings = MutableLiveData<List<Booking>>()
    val bookings: LiveData<List<Booking>> = _bookings

    private val _services = MutableLiveData<List<Service>>()
    val services: LiveData<List<Service>> = _services

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _bookingsToday = MutableLiveData(0)
    val bookingsToday: LiveData<Int> = _bookingsToday

    private val _revenue = MutableLiveData(0.0)
    val revenue: LiveData<Double> = _revenue

    fun loadDashboardData(businessId: String) {
        loadBookings(businessId)
        loadServices(businessId)
    }

    private fun loadBookings(businessId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val bookingList = bookingRepository.getBusinessBookings(businessId)
                    .sortedByDescending { it.time }
                _bookings.value = bookingList
                calculateStats(bookingList, _services.value ?: emptyList())
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to load bookings"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun loadServices(businessId: String) {
        viewModelScope.launch {
            try {
                // In real app, filter by businessId
                val serviceList = serviceRepository.getAllServices()
                    .filter { it.businessId == businessId }
                _services.value = serviceList
                calculateStats(_bookings.value ?: emptyList(), serviceList)
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to load services"
            }
        }
    }

    private fun calculateStats(bookings: List<Booking>, services: List<Service>) {
        val today = System.currentTimeMillis()
        val startOfDay = today - (today % (24 * 60 * 60 * 1000))

        val todayBookings = bookings.filter { it.time >= startOfDay }
        _bookingsToday.value = todayBookings.size

        // Calculate revenue (simplified)
        val totalRevenue = bookings.filter { it.status == "confirmed" || it.status == "completed" }
            .sumOf { booking ->
                val service = services.find { it.id == booking.serviceId }
                service?.price?.toDouble() ?: 0.0
            }
        _revenue.value = totalRevenue
    }

    fun updateBookingStatus(bookingId: String, status: String, businessId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val result = bookingRepository.updateBookingStatus(bookingId, status)
                result.onSuccess {
                    loadBookings(businessId)
                }.onFailure { e ->
                    _error.value = e.message ?: "Failed to update booking"
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to update booking"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun addService(service: Service) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val result = serviceRepository.addService(service)
                result.onSuccess {
                    // Reload services
                    val businessId = service.businessId
                    loadServices(businessId)
                }.onFailure { e ->
                    _error.value = e.message ?: "Failed to add service"
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to add service"
            } finally {
                _isLoading.value = false
            }
        }
    }
}
