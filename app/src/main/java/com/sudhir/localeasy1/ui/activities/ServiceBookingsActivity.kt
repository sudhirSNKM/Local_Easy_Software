package com.sudhir.localeasy1.ui.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import com.sudhir.localeasy1.data.Booking
import com.sudhir.localeasy1.databinding.ActivityServiceBookingsBinding
import com.sudhir.localeasy1.ui.adapters.ServiceBookingItemAdapter

class ServiceBookingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityServiceBookingsBinding
    private val db = FirebaseFirestore.getInstance()
    private lateinit var adapter: ServiceBookingItemAdapter
    private var allBookings = mutableListOf<Booking>()
    private var serviceId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityServiceBookingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        serviceId = intent.getStringExtra("serviceId") ?: ""
        val serviceName = intent.getStringExtra("serviceName") ?: "Service"

        binding.header.titleText.text = "Bookings - $serviceName"

        setupRecyclerView()
        setupSearch()
        loadBookings()
    }

    private fun setupRecyclerView() {
        adapter = ServiceBookingItemAdapter(
            bookings = emptyList(),
            onCallClick = { booking ->
                callUser(booking.phone)
            }
        )
        binding.recyclerBookings.layoutManager = LinearLayoutManager(this)
        binding.recyclerBookings.adapter = adapter
    }

    private fun setupSearch() {
        binding.searchBar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterBookings(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun loadBookings() {
        if (serviceId.isEmpty()) {
            Toast.makeText(this, "Invalid service ID", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        binding.loadingText.text = "Loading bookings..."

        db.collection("bookings")
            .whereEqualTo("serviceId", serviceId)
            .get()
            .addOnSuccessListener { result ->
                allBookings.clear()

                for (doc in result) {
                    try {
                        val booking = doc.toObject(Booking::class.java).copy(id = doc.id)
                        allBookings.add(booking)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                if (allBookings.isEmpty()) {
                    binding.loadingText.text = "No bookings yet"
                } else {
                    binding.loadingText.text = "Total: ${allBookings.size} bookings"
                    adapter.updateBookings(allBookings)
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error loading bookings: ${e.message}", Toast.LENGTH_SHORT).show()
                binding.loadingText.text = "Error loading bookings"
            }
    }

    private fun filterBookings(query: String) {
        val filtered = if (query.isEmpty()) {
            allBookings
        } else {
            allBookings.filter { booking ->
                booking.userName.lowercase().contains(query.lowercase()) ||
                booking.phone.contains(query)
            }
        }

        binding.loadingText.text = "Found: ${filtered.size} bookings"
        adapter.updateBookings(filtered)
    }

    private fun callUser(phone: String) {
        if (phone.isEmpty()) {
            Toast.makeText(this, "Phone number not available", Toast.LENGTH_SHORT).show()
            return
        }

        val intent = Intent(Intent.ACTION_DIAL).apply {
            data = Uri.parse("tel:$phone")
        }
        startActivity(intent)
    }
}

