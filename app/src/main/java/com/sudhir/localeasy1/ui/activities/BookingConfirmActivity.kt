package com.sudhir.localeasy1.ui.activities

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.sudhir.localeasy1.databinding.ActivityBookingConfirmBinding

class BookingConfirmActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBookingConfirmBinding
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBookingConfirmBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val serviceName = intent.getStringExtra("serviceName") ?: "Unknown Service"
        val price = intent.getDoubleExtra("price", 0.0)
        val serviceId = intent.getStringExtra("serviceId") ?: ""
        val businessId = intent.getStringExtra("businessId") ?: ""

        binding.serviceNameTextView.text = serviceName
        binding.priceTextView.text = "₹$price"

        // Check if user has phone number
        checkUserProfile()

        binding.confirmButton.setOnClickListener {
            confirmBooking(serviceName, price, serviceId, businessId)
        }

        binding.cancelButton.setOnClickListener {
            finish()
        }
    }

    private fun checkUserProfile() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        db.collection("users").document(uid).get()
            .addOnSuccessListener { document ->
                val phone = document.getString("phone")
                if (phone.isNullOrEmpty()) {
                    Toast.makeText(this, "Please add your phone number before booking", Toast.LENGTH_LONG).show()
                    // Redirect to profile
                    startActivity(android.content.Intent(this, ProfileActivity::class.java))
                    finish()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error checking profile", Toast.LENGTH_SHORT).show()
            }
    }

    private fun confirmBooking(serviceName: String, price: Double, serviceId: String, businessId: String) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        binding.confirmButton.isEnabled = false
        binding.confirmButton.text = "Confirming..."

        // Get user data
        db.collection("users").document(uid).get()
            .addOnSuccessListener { userDoc ->
                val userName = userDoc.getString("name") ?: "Unknown"
                val phone = userDoc.getString("phone") ?: ""

                val booking = hashMapOf(
                    "userId" to uid,
                    "serviceName" to serviceName,
                    "serviceId" to serviceId,
                    "businessId" to businessId,
                    "price" to price,
                    "status" to "pending",
                    "time" to System.currentTimeMillis(), // Currently booking for 'now'
                    "createdAt" to System.currentTimeMillis(),
                    "userName" to userName,
                    "phone" to phone
                )

                android.util.Log.d("BOOKING_DEBUG", "Creating booking for businessId: $businessId, price: $price")


                db.collection("bookings")
                    .add(booking)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Booking confirmed!", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                        binding.confirmButton.isEnabled = true
                        binding.confirmButton.text = "Confirm Booking"
                    }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error fetching user data", Toast.LENGTH_SHORT).show()
                binding.confirmButton.isEnabled = true
                binding.confirmButton.text = "Confirm Booking"
            }
    }
}
