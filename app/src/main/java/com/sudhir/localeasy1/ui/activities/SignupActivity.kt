package com.sudhir.localeasy1.ui.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.RadioButton
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.sudhir.localeasy1.MainActivity
import com.sudhir.localeasy1.data.UserRole
import com.sudhir.localeasy1.databinding.ActivitySignupBinding
import com.sudhir.localeasy1.ui.viewmodel.AuthViewModel

class SignupActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignupBinding
    private val authViewModel: AuthViewModel by viewModels()
    private val categories = listOf("Salon", "Clinic", "Gym", "Spa", "Restaurant", "Cleaning")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupObservers()
        setupClickListeners()
        setupRoleSelection()
        setupCategoryDropdown()
    }

    private fun setupCategoryDropdown() {
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, categories)
        (binding.businessCategoryDropdown as? AutoCompleteTextView)?.setAdapter(adapter)
    }

    private fun setupObservers() {
        authViewModel.isLoading.observe(this, Observer { isLoading ->
            binding.signupButton.isEnabled = !isLoading
            binding.signupButton.text = if (isLoading) "Registering..." else "Register"
        })

        authViewModel.error.observe(this, Observer { error ->
            binding.errorTextView.visibility = if (error != null) View.VISIBLE else View.GONE
            binding.errorTextView.text = error
        })

        authViewModel.isLoggedIn.observe(this, Observer { isLoggedIn ->
            if (isLoggedIn) {
                navigateToMain()
            }
        })
    }

    private fun setupClickListeners() {
        binding.signupButton.setOnClickListener {
            val name = binding.nameEditText.text.toString().trim()
            val email = binding.emailEditText.text.toString().trim()
            val password = binding.passwordEditText.text.toString().trim()
            val selectedRole = getSelectedRole()
            val businessName = binding.businessNameEditText.text.toString().trim()
            val businessCategory = binding.businessCategoryDropdown.text.toString().trim()
            val businessDesc = binding.businessDescEditText.text.toString().trim()

            if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (selectedRole == UserRole.ADMIN && (businessName.isEmpty() || businessCategory.isEmpty() || businessDesc.isEmpty())) {
                Toast.makeText(this, "Please fill business details", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            authViewModel.signUp(email, password, name, selectedRole, businessName, businessDesc, businessCategory,
                onSuccess = {
                    Toast.makeText(this, "Registration successful", Toast.LENGTH_SHORT).show()
                },
                onError = { error ->
                    Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
                }
            )
        }

        binding.loginButton.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            intent.putExtra("fromRegister", true)
            startActivity(intent)
            finish()
        }
    }

    private fun setupRoleSelection() {
        binding.roleRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            val isAdmin = checkedId == binding.adminRadioButton.id
            binding.businessLayout.visibility = if (isAdmin) View.VISIBLE else View.GONE
        }
    }

    private fun getSelectedRole(): UserRole {
        return when (binding.roleRadioGroup.checkedRadioButtonId) {
            binding.adminRadioButton.id -> UserRole.ADMIN
            else -> UserRole.USER
        }
    }

    private fun navigateToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
