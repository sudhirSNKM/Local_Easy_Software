package com.sudhir.localeasy1.ui.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.google.firebase.auth.FirebaseAuth
import com.sudhir.localeasy1.data.UserRole
import com.sudhir.localeasy1.databinding.ActivityLoginBinding
import com.sudhir.localeasy1.ui.viewmodel.AuthViewModel

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupObservers()
        setupClickListeners()
    }

    override fun onStart() {
        super.onStart()

        val user = FirebaseAuth.getInstance().currentUser
        val fromRegister = intent.getBooleanExtra("fromRegister", false)

        if (user != null && !fromRegister) {
            authViewModel.userRole.value?.let { navigateByRole(it) }
        }
    }

    private fun setupObservers() {
        authViewModel.isLoading.observe(this, Observer { isLoading ->
            binding.loginButton.isEnabled = !isLoading
            binding.loginButton.text = if (isLoading) "Signing In..." else "Sign In"
        })

        authViewModel.error.observe(this, Observer { error ->
            binding.errorTextView.visibility = if (error != null) View.VISIBLE else View.GONE
            binding.errorTextView.text = error
        })

        authViewModel.isLoggedIn.observe(this, Observer { isLoggedIn ->
            if (isLoggedIn) {
                authViewModel.userRole.value?.let { navigateByRole(it) }
            }
        })

        authViewModel.userRole.observe(this, Observer { role ->
            if (authViewModel.isLoggedIn.value == true && role != null) {
                navigateByRole(role)
            }
        })
    }

    private fun setupClickListeners() {
        binding.loginButton.setOnClickListener {
            val email = binding.emailEditText.text.toString().trim()
            val password = binding.passwordEditText.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            authViewModel.login(email, password,
                onSuccess = {
                    Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show()
                },
                onError = { error ->
                    Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
                }
            )
        }

        binding.signupButton.setOnClickListener {
            startActivity(Intent(this, SignupActivity::class.java))
        }
    }

    private fun navigateByRole(role: UserRole) {
        val intent = when (role) {
            UserRole.USER -> Intent(this, UserMainActivity::class.java)
            UserRole.ADMIN -> Intent(this, AdminActivity::class.java)
            UserRole.SUPER_ADMIN -> Intent(this, SuperAdminActivity::class.java)
        }
        startActivity(intent)
        finish()
    }
}
