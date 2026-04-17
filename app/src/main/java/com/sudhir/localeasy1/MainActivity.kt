package com.sudhir.localeasy1

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.google.firebase.FirebaseApp
import com.sudhir.localeasy1.R
import com.sudhir.localeasy1.data.UserRole
import com.sudhir.localeasy1.ui.activities.*
import com.sudhir.localeasy1.ui.viewmodel.AuthViewModel

class MainActivity : AppCompatActivity() {

    private val authViewModel: AuthViewModel by viewModels()
    private var isNavigationHandled = false
    private lateinit var progressBar: ProgressBar
    private lateinit var statusText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_Localeasy1)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        progressBar = findViewById(R.id.progressBar)
        statusText = findViewById(R.id.statusText)

        // Initialize Firebase
        try {
            Log.d("MainActivity", "Initializing Firebase")
            FirebaseApp.initializeApp(this)
            statusText.text = "Initializing..."
        } catch (e: Exception) {
            Log.e("MainActivity", "Firebase initialization issue", e)
            statusText.text = "Firebase error: ${e.message}"
            navigateToLogin()
            return
        }

        // Set up observers
        setupAuthObservers()

        // Check initial auth state
        checkInitialAuthState()
    }

    private fun setupAuthObservers() {
        authViewModel.isLoggedIn.observe(this, Observer { isLoggedIn ->
            Log.d("MainActivity", "isLoggedIn changed: $isLoggedIn")
            if (isNavigationHandled) return@Observer

            if (isLoggedIn == true) {
                statusText.text = "Loading user profile..."
                // User is logged in, wait for role to be fetched
                authViewModel.userRole.observe(this, Observer { role ->
                    Log.d("MainActivity", "userRole changed: $role")
                    if (role != null && !isNavigationHandled) {
                        isNavigationHandled = true
                        navigateToRoleActivity(role)
                    }
                })
            } else {
                // User is not logged in
                if (!isNavigationHandled) {
                    isNavigationHandled = true
                    navigateToLogin()
                }
            }
        })
    }

    private fun checkInitialAuthState() {
        // Give a small delay to allow LiveData to initialize
        window.decorView.postDelayed({
            try {
                val currentUser = authViewModel.isLoggedIn.value
                Log.d("MainActivity", "Initial auth check: $currentUser")
                if (currentUser == null) {
                    // LiveData not ready yet, wait for observer
                    statusText.text = "Checking authentication..."
                    return@postDelayed
                }

                if (currentUser == false && !isNavigationHandled) {
                    isNavigationHandled = true
                    navigateToLogin()
                }
                // If true, the observer will handle navigation when role is available
            } catch (e: Exception) {
                Log.e("MainActivity", "Error in initial auth check", e)
                navigateToLogin()
            }
        }, 500)
    }

    private fun navigateToRoleActivity(role: UserRole) {
        Log.d("MainActivity", "Navigating to role activity: $role")
        val intent = when (role) {
            UserRole.USER -> Intent(this, UserMainActivity::class.java)
            UserRole.ADMIN -> Intent(this, AdminActivity::class.java)
            UserRole.SUPER_ADMIN -> Intent(this, SuperAdminActivity::class.java)
        }
        startActivity(intent)
        finish()
    }

    private fun navigateToLogin() {
        Log.d("MainActivity", "Navigating to login")
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }
}
