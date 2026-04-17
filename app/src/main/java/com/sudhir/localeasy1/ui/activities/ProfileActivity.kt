package com.sudhir.localeasy1.ui.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.sudhir.localeasy1.MainActivity
import com.sudhir.localeasy1.data.Booking
import com.sudhir.localeasy1.databinding.ActivityProfileBinding
import com.sudhir.localeasy1.ui.adapters.BookingAdapter
import com.sudhir.localeasy1.ui.viewmodel.AuthViewModel

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    private val authViewModel: AuthViewModel by viewModels()
    private lateinit var bookingAdapter: BookingAdapter
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.header.titleText.text = "My Profile"

        setupRecyclerView()
        loadUserProfile()
        loadUserBookings()
        setupClickListeners()
        setupObservers()
    }

    private fun setupRecyclerView() {
        bookingAdapter = BookingAdapter(emptyList())
        binding.bookingsRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.bookingsRecyclerView.adapter = bookingAdapter
    }

    private fun loadUserProfile() {
        val uid = auth.currentUser?.uid ?: return
        
        // Load basic auth info
        binding.nameText.text = auth.currentUser?.displayName ?: "User"
        binding.emailText.text = auth.currentUser?.email ?: ""

        // Load additional info from Firestore
        db.collection("users").document(uid).get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    val name = document.getString("name")
                    val email = document.getString("email")
                    val phone = document.getString("phone")
                    
                    if (name != null) binding.nameText.text = name
                    if (email != null) binding.emailText.text = email
                    binding.phoneEdit.setText(phone ?: "")
                }
            }
    }

    private fun loadUserBookings() {
        val uid = auth.currentUser?.uid ?: return
        db.collection("bookings")
            .whereEqualTo("userId", uid)
            .get()
            .addOnSuccessListener { result ->
                val bookings = mutableListOf<Booking>()
                for (doc in result) {
                    try {
                        val booking = Booking(
                            id = doc.id,
                            userId = doc.getString("userId") ?: "",
                            serviceId = doc.getString("serviceId") ?: "",
                            businessId = doc.getString("businessId") ?: "",
                            serviceName = doc.getString("serviceName") ?: "Unknown Service",
                            time = doc.getLong("time") ?: System.currentTimeMillis(),
                            status = doc.getString("status") ?: "pending",
                            notes = doc.getString("notes") ?: "",
                            createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis()
                        )
                        bookings.add(booking)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                bookingAdapter.updateBookings(bookings)
            }
    }

    private fun setupClickListeners() {
        binding.saveBtn.setOnClickListener {
            val phone = binding.phoneEdit.text.toString().trim()
            val uid = auth.currentUser?.uid ?: return@setOnClickListener

            db.collection("users").document(uid)
                .update("phone", phone)
                .addOnSuccessListener {
                    Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error updating profile: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }

        binding.logoutBtn.setOnClickListener {
            auth.signOut()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }

    private fun setupObservers() {
        authViewModel.isLoggedIn.observe(this, Observer { isLoggedIn ->
            if (isLoggedIn == false) {
                val intent = Intent(this, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
        })
    }
}
