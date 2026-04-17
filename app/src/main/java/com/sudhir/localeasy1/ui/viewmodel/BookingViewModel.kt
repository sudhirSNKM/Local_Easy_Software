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

class BookingViewModel : ViewModel() {
    private val bookingRepository = BookingRepository()
    private val serviceRepository = ServiceRepository()

    private val _service = MutableLiveData<Service?>()
    val service: LiveData<Service?> = _service

    private val _selectedTime = MutableLiveData<Long?>()
    val selectedTime: LiveData<Long?> = _selectedTime

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _bookingSuccess = MutableLiveData(false)
    val bookingSuccess: LiveData<Boolean> = _bookingSuccess

    fun loadService(serviceId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                // In a real app, you'd have a method to get service by ID
                val services = serviceRepository.getAllServices()
                val service = services.find { it.id == serviceId }
                _service.value = service
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to load service"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun selectTime(time: Long) {
        _selectedTime.value = time
    }

    fun createBooking(userId: String, notes: String = "") {
        val service = _service.value
        val time = _selectedTime.value

        if (service == null || time == null) {
            _error.value = "Please select a service and time"
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val booking = Booking(
                    userId = userId,
                    serviceId = service.id,
                    businessId = service.businessId,
                    time = time,
                    notes = notes
                )

                val result = bookingRepository.createBooking(booking)
                result.onSuccess {
                    _bookingSuccess.value = true
                }.onFailure { e ->
                    _error.value = e.message ?: "Failed to create booking"
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to create booking"
            } finally {
                _isLoading.value = false
            }
        }
    }
}
