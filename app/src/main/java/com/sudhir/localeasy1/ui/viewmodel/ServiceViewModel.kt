package com.sudhir.localeasy1.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.sudhir.localeasy1.data.Service
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ServiceViewModel : ViewModel() {

    private val db by lazy { FirebaseFirestore.getInstance() }
    
    private val _services = MutableStateFlow<List<Service>>(emptyList())
    val services: StateFlow<List<Service>> = _services

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    init {
        fetchServices()
    }

    fun fetchServices() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                db.collection("services")
                    .get()
                    .addOnSuccessListener { result ->
                        try {
                            val serviceList = result.toObjects(Service::class.java)
                            _services.value = serviceList
                            Log.d("ServiceViewModel", "Successfully fetched ${serviceList.size} services")
                        } catch (e: Exception) {
                            Log.e("ServiceViewModel", "Error parsing services", e)
                        }
                        _isLoading.value = false
                    }
                    .addOnFailureListener { e ->
                        Log.e("ServiceViewModel", "Error fetching services", e)
                        _isLoading.value = false
                    }
            } catch (e: Exception) {
                Log.e("ServiceViewModel", "Firestore connection failed", e)
                _isLoading.value = false
            }
        }
    }
}
