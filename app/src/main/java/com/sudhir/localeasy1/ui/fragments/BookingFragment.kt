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
                    for (doc in result) {
                        try {
                            val bookingTime = doc.getLong("time") ?: 0L
                            val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                            val timeFormat = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
                            val date = dateFormat.format(java.util.Date(bookingTime))
                            val time = timeFormat.format(java.util.Date(bookingTime))

                            val booking = Booking(
                                id = doc.id,
                                userId = doc.getString("userId") ?: "",
                                serviceId = doc.getString("serviceId") ?: "",
                                serviceName = doc.getString("serviceName") ?: "Unknown Service",
                                time = bookingTime,
                                status = doc.getString("status") ?: "pending"
                            )
                            bookings.add(booking)
                        } catch (e: Exception) {
                            Log.e("BookingFragment", "Error parsing booking", e)
                        }
                    }
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
