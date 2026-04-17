package com.sudhir.localeasy1.ui.activities

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.sudhir.localeasy1.databinding.ActivityBookingBinding
import com.sudhir.localeasy1.ui.adapters.TimeSlotAdapter
import com.sudhir.localeasy1.ui.viewmodel.BookingViewModel
import java.util.*

class BookingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBookingBinding
    private val bookingViewModel: BookingViewModel by viewModels()
    private var selectedTime: Long? = null
    private lateinit var timeSlotAdapter: TimeSlotAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBookingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val serviceId = intent.getStringExtra("service_id") ?: ""
        if (serviceId.isEmpty()) {
            finish()
            return
        }

        setupRecyclerView()
        setupTimeSlots()
        binding.header.titleText.text = "Book Service"
        setupObservers()
        bookingViewModel.loadService(serviceId)
    }

    private fun setupRecyclerView() {
        timeSlotAdapter = TimeSlotAdapter { time ->
            selectedTime = time
            bookingViewModel.selectTime(time)
        }
        binding.timeSlotsRecyclerView.apply {
            layoutManager = GridLayoutManager(this@BookingActivity, 3)
            adapter = timeSlotAdapter
        }
    }

    private fun setupTimeSlots() {
        val calendar = Calendar.getInstance()
        val currentTime = calendar.timeInMillis
        val slots = mutableListOf<Long>()

        // Generate time slots for next 7 days
        for (day in 0..6) {
            for (hour in 9..17) { // 9 AM to 5 PM
                calendar.timeInMillis = System.currentTimeMillis()
                calendar.add(Calendar.DAY_OF_MONTH, day)
                calendar.set(Calendar.HOUR_OF_DAY, hour)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)

                val timeSlot = calendar.timeInMillis
                if (timeSlot > currentTime) {
                    slots.add(timeSlot)
                }
            }
        }
        timeSlotAdapter.updateSlots(slots)
    }

    private fun setupObservers() {
        bookingViewModel.service.observe(this, Observer { service ->
            service?.let {
                binding.serviceNameTextView.text = it.name
                binding.serviceDetailsTextView.text = "${it.category} • ${it.duration} • ₹${it.price}"
            }
        })

        bookingViewModel.isLoading.observe(this, Observer { isLoading ->
            binding.bookButton.isEnabled = !isLoading
            binding.bookButton.text = if (isLoading) "Booking..." else "Complete Booking"
        })

        bookingViewModel.error.observe(this, Observer { error ->
            binding.errorTextView.visibility = if (error != null) View.VISIBLE else View.GONE
            binding.errorTextView.text = error
        })

        bookingViewModel.bookingSuccess.observe(this, Observer { success ->
            if (success) {
                Toast.makeText(this, "Booking successful!", Toast.LENGTH_SHORT).show()
                finish()
            }
        })
    }

    fun onBookButtonClick(view: View) {
        // Check if profile is complete before allowing booking
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show()
            return
        }

        // Check profile completion
        com.google.firebase.firestore.FirebaseFirestore.getInstance()
            .collection("users")
            .document(user.uid)
            .get()
            .addOnSuccessListener { document ->
                val email = user.email ?: ""
                val phone = document.getString("phone") ?: ""

                if (email.isEmpty() || phone.isEmpty()) {
                    Toast.makeText(this, "Complete your profile first (add phone number)", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }

                // Profile is complete, proceed with booking
                proceedWithBooking()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error checking profile: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun proceedWithBooking() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        val notes = binding.notesEditText.text.toString()

        if (selectedTime == null) {
            Toast.makeText(this, "Please select a time slot", Toast.LENGTH_SHORT).show()
            return
        }

        bookingViewModel.createBooking(userId, notes)
    }
}
