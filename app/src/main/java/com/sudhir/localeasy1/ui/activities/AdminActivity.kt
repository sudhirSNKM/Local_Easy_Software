package com.sudhir.localeasy1.ui.activities

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.sudhir.localeasy1.R
import com.sudhir.localeasy1.databinding.ActivityAdminBinding
import com.sudhir.localeasy1.ui.adapters.BookingAdapter
import com.sudhir.localeasy1.ui.viewmodel.AdminViewModel

class AdminActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminBinding
    private val adminViewModel: AdminViewModel by viewModels()
    private lateinit var bookingAdapter: BookingAdapter
    private var adminBusinessId: String? = null
    private var adminBusinessApproved: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, true)
        binding = ActivityAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)
        findViewById<TextView>(R.id.titleText).text = "Admin Dashboard"

        setupRecyclerView()
        setupObservers()
        setupClickListeners()
        loadBusinessState()
    }

    private fun setupRecyclerView() {
        bookingAdapter = BookingAdapter(emptyList(), true) { bookingId, status ->
            adminBusinessId?.let { adminViewModel.updateBookingStatus(bookingId, status, it) }
        }
        binding.bookingsRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.bookingsRecyclerView.adapter = bookingAdapter
    }

    private fun setupClickListeners() {
        binding.addServiceButton.setOnClickListener {
            if (adminBusinessId.isNullOrBlank()) {
                createBusinessForAdmin()
                return@setOnClickListener
            }
            if (!adminBusinessApproved) {
                Toast.makeText(this, "Waiting for approval", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            startActivity(Intent(this, AddServiceActivity::class.java))
            overridePendingTransition(R.anim.slide_in_right, R.anim.fade_out)
        }

        binding.createOfferButton.setOnClickListener {
            if (adminBusinessId.isNullOrBlank()) {
                createBusinessForAdmin()
                return@setOnClickListener
            }
            if (!adminBusinessApproved) {
                Toast.makeText(this, "Waiting for approval", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            startActivity(Intent(this, CreateOfferActivity::class.java))
            overridePendingTransition(R.anim.slide_in_right, R.anim.fade_out)
        }

        binding.bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_dashboard -> true
                R.id.nav_services -> {
                    startActivity(Intent(this, ServiceListActivity::class.java))
                    overridePendingTransition(R.anim.slide_in_right, R.anim.fade_out)
                    true
                }
                R.id.nav_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    overridePendingTransition(R.anim.slide_in_right, R.anim.fade_out)
                    true
                }
                else -> false
            }
        }

        // Set Dashboard as selected
        binding.bottomNav.selectedItemId = R.id.nav_dashboard
    }

    private fun setupObservers() {
        adminViewModel.bookings.observe(this, Observer { bookings ->
            bookingAdapter.updateBookings(bookings)
        })

        adminViewModel.bookingsToday.observe(this, Observer { count ->
            binding.bookingsTodayTextView.text = count.toString()
        })

        adminViewModel.revenue.observe(this, Observer { revenue ->
            binding.revenueTextView.text = "₹${revenue}"
        })

        adminViewModel.isLoading.observe(this, Observer { isLoading ->
            // Show/hide loading
        })

        adminViewModel.error.observe(this, Observer { error ->
            error?.let {
                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun loadDashboardData() {
        val businessId = adminBusinessId ?: return
        adminViewModel.loadDashboardData(businessId)
    }

    private fun loadBusinessState() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
        db.collection("businesses")
            .whereEqualTo("ownerId", uid)
            .limit(1)
            .get()
            .addOnSuccessListener { result ->
                val doc = result.documents.firstOrNull()
                adminBusinessId = doc?.id
                adminBusinessApproved = doc?.getBoolean("approved") == true

                if (adminBusinessId.isNullOrBlank()) {
                    binding.addServiceButton.text = "Create Business"
                    binding.businessStatusTextView.text = "No business found. Create one to continue."
                    binding.businessStatusTextView.setTextColor(Color.RED)
                    Toast.makeText(this, "No business found. Create one to continue.", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }

                if (!adminBusinessApproved) {
                    Toast.makeText(this, "Business waiting for approval", Toast.LENGTH_SHORT).show()
                    binding.businessStatusTextView.text = "Pending approval. Please wait..."
                    binding.businessStatusTextView.setTextColor(Color.RED)
                } else {
                    binding.businessStatusTextView.text = "Business Active"
                    binding.businessStatusTextView.setTextColor(Color.parseColor("#16A34A"))
                    adminViewModel.loadDashboardData(adminBusinessId!!)
                }
            }
    }

    private fun createBusinessForAdmin() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
        val business = hashMapOf(
            "name" to "Business $uid",
            "ownerId" to uid,
            "approved" to false,
            "createdAt" to System.currentTimeMillis()
        )
        db.collection("businesses")
            .add(business)
            .addOnSuccessListener {
                Toast.makeText(this, "Business created. Waiting for approval.", Toast.LENGTH_SHORT).show()
                loadBusinessState()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
