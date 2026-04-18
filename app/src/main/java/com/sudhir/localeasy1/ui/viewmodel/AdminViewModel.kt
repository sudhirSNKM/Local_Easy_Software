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

    private var ownerId: String = ""

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

    fun loadDashboardData(ownerId: String, businessId: String) {
        this.ownerId = ownerId
        loadBookings(ownerId)
        loadServices(businessId)
    }

    private fun loadBookings(ownerId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val bookingList = bookingRepository.getBookingsForOwner(ownerId)
                    .sortedByDescending { it.time }

                // DEBUG LOGGING
                android.util.Log.d("ADMIN_DEBUG", "Querying bookings for ownerId: $ownerId")
                android.util.Log.d("ADMIN_DEBUG", "Found ${bookingList.size} bookings for ownerId: $ownerId")
                bookingList.forEach { booking ->
                    android.util.Log.d("ADMIN_DEBUG", "Loaded booking: service=${booking.serviceName}, businessId=${booking.businessId}, user=${booking.userName}")
                }

                _bookings.value = bookingList
                calculateStats(bookingList, _services.value ?: emptyList())
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to load bookings"
                android.util.Log.e("ADMIN_DEBUG", "Error loading bookings", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun loadServices(businessId: String) {
        viewModelScope.launch {
            try {
                // Use the new method to get services specifically for this business
                val serviceList = serviceRepository.getServicesByBusinessId(businessId)
                _services.value = serviceList
                calculateStats(_bookings.value ?: emptyList(), serviceList)
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to load services"
            }
        }
    }

    private fun calculateStats(bookings: List<Booking>, services: List<Service>) {
        val today = System.currentTimeMillis()
        val calendar = java.util.Calendar.getInstance()
        calendar.timeInMillis = today
        calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
        calendar.set(java.util.Calendar.MINUTE, 0)
        calendar.set(java.util.Calendar.SECOND, 0)
        calendar.set(java.util.Calendar.MILLISECOND, 0)
        val startOfDay = calendar.timeInMillis
        val endOfDay = startOfDay + (24 * 60 * 60 * 1000)

        // Count all bookings created today OR scheduled for today
        val todayBookings = bookings.filter { 
            (it.createdAt in startOfDay until endOfDay) || (it.time in startOfDay until endOfDay)
        }
        _bookingsToday.value = todayBookings.size

        // Calculate revenue only for confirmed or completed bookings
        var totalRevenue = 0.0
        bookings.filter { it.status == "confirmed" || it.status == "completed" }
            .forEach { booking ->
                // Look for price in the booking first, then service
                val price = try {
                    // Try to find matching service to get price
                    val service = services.find { it.id == booking.serviceId }
                    service?.price?.toDouble() ?: 0.0
                } catch (e: Exception) {
                    0.0
                }
                totalRevenue += price
            }
        
        android.util.Log.d("ADMIN_STATS", "Stats Summary: Total Bookings=${bookings.size}, Today Count=${todayBookings.size}, Confirmed Revenue=$totalRevenue")
        _revenue.value = totalRevenue
    }

    fun updateBookingStatus(bookingId: String, status: String, businessId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val result = bookingRepository.updateBookingStatus(bookingId, status)
                result.onSuccess {
                    loadBookings(this@AdminViewModel.ownerId)
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
