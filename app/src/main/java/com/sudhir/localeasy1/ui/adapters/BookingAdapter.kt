package com.sudhir.localeasy1.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.sudhir.localeasy1.data.Booking
import com.sudhir.localeasy1.databinding.ItemBookingBinding
import java.text.SimpleDateFormat
import java.util.*

class BookingAdapter(
    private val bookings: List<Booking>,
    private val isAdmin: Boolean = false,
    private val onStatusUpdate: ((String, String) -> Unit)? = null
) : RecyclerView.Adapter<BookingAdapter.BookingViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookingViewHolder {
        val binding = ItemBookingBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return BookingViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BookingViewHolder, position: Int) {
        holder.bind(bookings[position])
    }

    override fun getItemCount(): Int = bookings.size

    inner class BookingViewHolder(private val binding: ItemBookingBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(booking: Booking) {
            binding.serviceNameTextView.text = booking.serviceName
            binding.bookingTimeTextView.text = "${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault()).format(java.util.Date(booking.time))}"
            binding.statusTextView.text = booking.status.uppercase()

            if (isAdmin && booking.status == "pending") {
                binding.confirmButton.visibility = View.VISIBLE
                binding.cancelButton.visibility = View.VISIBLE

                binding.confirmButton.setOnClickListener {
                    onStatusUpdate?.invoke(booking.id, "confirmed")
                }

                binding.cancelButton.setOnClickListener {
                    onStatusUpdate?.invoke(booking.id, "cancelled")
                }
            } else {
                binding.confirmButton.visibility = View.GONE
                binding.cancelButton.visibility = View.GONE
            }
        }
    }
}
