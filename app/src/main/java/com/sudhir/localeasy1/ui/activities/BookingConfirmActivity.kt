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

        binding.confirmButton.setOnClickListener {
            confirmBooking(serviceName, price, serviceId, businessId)
        }

        binding.cancelButton.setOnClickListener {
            finish()
        }
    }

    private fun confirmBooking(serviceName: String, price: Double, serviceId: String, businessId: String) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        binding.confirmButton.isEnabled = false
        binding.confirmButton.text = "Confirming..."

        val booking = hashMapOf(
            "userId" to uid,
            "serviceName" to serviceName,
            "serviceId" to serviceId,
            "businessId" to businessId,
            "price" to price,
            "status" to "pending",
            "time" to System.currentTimeMillis(),
            "createdAt" to System.currentTimeMillis()
        )

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
}
