package com.sudhir.localeasy1.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.sudhir.localeasy1.R
import com.sudhir.localeasy1.databinding.FragmentBookingBinding
import com.sudhir.localeasy1.ui.adapters.BookingAdapter
import com.sudhir.localeasy1.data.Booking

class BookingFragment : Fragment() {

    private var _binding: FragmentBookingBinding? = null
    private val binding get() = _binding!!
    private lateinit var bookingAdapter: BookingAdapter
    private val bookings = mutableListOf<Booking>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBookingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        loadUserBookings()
    }

    private fun setupRecyclerView() {
        bookingAdapter = BookingAdapter(bookings)
        binding.bookingsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.bookingsRecyclerView.adapter = bookingAdapter
    }

    private fun loadUserBookings() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        binding.progressBar.visibility = View.VISIBLE
        binding.emptyTextView.visibility = View.GONE

        FirebaseFirestore.getInstance()
            .collection("bookings")
            .whereEqualTo("userId", uid)
            .get()
            .addOnSuccessListener { result ->
                binding.progressBar.visibility = View.GONE
                bookings.clear()

                if (result.isEmpty) {
                    binding.emptyTextView.visibility = View.VISIBLE
                    binding.emptyTextView.text = "No bookings found"
                } else {
                    binding.emptyTextView.visibility = View.GONE
                    val fetchedBookings = mutableListOf<Booking>()
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
                            fetchedBookings.add(booking)
                        } catch (e: Exception) {
                            Log.e("BookingFragment", "Error parsing booking", e)
                        }
                    }
                    // Sort manually to avoid Firestore composite index requirement
                    bookings.addAll(fetchedBookings.sortedByDescending { it.createdAt })
                    bookingAdapter.notifyDataSetChanged()
                }
            }
            .addOnFailureListener { e ->
                binding.progressBar.visibility = View.GONE
                binding.emptyTextView.visibility = View.VISIBLE
                binding.emptyTextView.text = "Error loading bookings"
                Log.e("BookingFragment", "Error loading bookings", e)
                Toast.makeText(requireContext(), "Error loading bookings: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
