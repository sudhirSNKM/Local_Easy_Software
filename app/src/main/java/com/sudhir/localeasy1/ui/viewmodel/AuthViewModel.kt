package com.sudhir.localeasy1.ui.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.sudhir.localeasy1.data.Banner
import com.sudhir.localeasy1.data.Business
import com.sudhir.localeasy1.data.UserRole
import com.sudhir.localeasy1.repository.AuthRepository
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AuthViewModel : ViewModel() {
    private val authRepository = AuthRepository()
    private val db = FirebaseFirestore.getInstance()

    private val _userRole = MutableLiveData<UserRole?>()
    val userRole: LiveData<UserRole?> = _userRole

    private val _isLoggedIn = MutableLiveData(FirebaseAuth.getInstance().currentUser != null)
    val isLoggedIn: LiveData<Boolean> = _isLoggedIn

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    init {
        // Run seeding in background to avoid blocking
        viewModelScope.launch {
            seedInitialData()
        }

        if (FirebaseAuth.getInstance().currentUser != null) {
            fetchUserRole(FirebaseAuth.getInstance().currentUser!!.uid)
        }
    }

    private suspend fun seedInitialData() {
        try {
            // 1. Seed Sample Banners
            val banners = db.collection("banners").limit(1).get().await()
            if (banners.isEmpty) {
                val sampleBanner = Banner(
                    id = "banner1",
                    title = "Big Opening Sale!",
                    imageUrl = "https://example.com/banner.jpg",
                    isApproved = true,
                    createdAt = System.currentTimeMillis()
                )
                db.collection("banners").document("banner1").set(sampleBanner).await()
                Log.d("SEED", "Sample banner seeded")
            }

            // 2. Seed Sample Businesses
            val businesses = db.collection("businesses").limit(1).get().await()
            if (businesses.isEmpty) {
                val sampleBusiness = Business(
                    id = "biz1",
                    name = "City Spa & Wellness",
                    description = "Relax and rejuvenate at the heart of the city.",
                    ownerId = "system",
                    category = "Wellness",
                    address = "123 Main St, City",
                    approved = true,
                    createdAt = System.currentTimeMillis()
                )
                db.collection("businesses").document("biz1").set(sampleBusiness).await()
                Log.d("SEED", "Sample business seeded")
            }
        } catch (e: Exception) {
            Log.e("SEED", "Error seeding data", e)
        }
    }

    fun login(email: String, pass: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                Log.d("AUTH_DEBUG", "Attempting login for: $email")
                val result = authRepository.login(email, pass)
                result.onSuccess { userId ->
                    Log.d("AUTH_DEBUG", "Login successful, UID: $userId")

                    fetchUserRole(userId)
                    _isLoggedIn.value = true
                    onSuccess()
                }.onFailure { e ->
                    Log.e("AUTH_DEBUG", "Login failed: ${e.message}", e)
                    _error.value = e.message ?: "Login failed"
                    onError(e.message ?: "Login failed")
                }
            } catch (e: Exception) {
                Log.e("AUTH_DEBUG", "Login failed: ${e.message}", e)
                _error.value = e.message ?: "Login failed"
                onError(e.message ?: "Login failed")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun signUp(
        email: String,
        pass: String,
        name: String,
        role: UserRole,
        businessName: String = "",
        businessDesc: String = "",
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                // 🔥 DEBUG: Check initial auth state
                Log.d("AUTH_CHECK", "Current User before signup: ${FirebaseAuth.getInstance().currentUser}")

                // Prevent registration of Super Admin via Signup UI
                if (role == UserRole.SUPER_ADMIN) {
                    val errorMsg = "Cannot register as Super Admin"
                    _error.value = errorMsg
                    onError(errorMsg)
                    return@launch
                }
                if (email.equals("admin@localease.com", ignoreCase = true)) {
                    val errorMsg = "This email is reserved for Super Admin"
                    _error.value = errorMsg
                    onError(errorMsg)
                    return@launch
                }

                val result = authRepository.signup(email, pass, name, role)
                result.onSuccess { userId ->
                    Log.d("AUTH", "SUCCESS: User created with UID: $userId")

                    // If Admin, create a pending business entry
                    if (role == UserRole.ADMIN) {
                        val business = Business(
                            id = db.collection("businesses").document().id,
                            ownerId = userId,
                            name = businessName,
                            description = businessDesc,
                            approved = false
                        )
                        db.collection("businesses").document(business.id).set(business).await()
                        Log.d("AUTH", "Firestore: Business entry created for Admin")
                    }

                    _userRole.value = role
                    _isLoggedIn.value = true
                    onSuccess()
                }.onFailure { e ->
                    Log.e("AUTH_ERROR", "Signup operation failed: ${e.message}", e)
                    _error.value = e.message ?: "Signup failed"
                    onError(e.message ?: "Signup failed")
                }
            } catch (e: Exception) {
                // 🔥 DEBUG: Log the exact error message
                Log.e("AUTH_ERROR", "Signup operation failed: ${e.message}", e)
                _error.value = e.message ?: "Signup failed"
                onError(e.message ?: "Signup failed")
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun fetchUserRole(uid: String) {
        viewModelScope.launch {
            try {
                Log.d("AUTH_DEBUG", "Fetching role for UID: $uid")
                val role = authRepository.getUserRole(uid)
                Log.d("AUTH_DEBUG", "Role found in Firestore: $role")
                _userRole.value = role
            } catch (e: Exception) {
                Log.e("AUTH_DEBUG", "Error fetching role from Firestore: ${e.message}", e)
                _userRole.value = UserRole.USER
            }
        }
    }

    fun logout() {
        authRepository.logout()
        _isLoggedIn.value = false
        _userRole.value = null
        _error.value = null
    }
}
