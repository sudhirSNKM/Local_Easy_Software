package com.sudhir.localeasy1.ui.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.sudhir.localeasy1.R
import com.sudhir.localeasy1.data.Booking
import com.sudhir.localeasy1.databinding.ItemBookingBinding
import java.text.SimpleDateFormat
import java.util.*

class BookingAdapter(
    private var bookings: List<Booking>,
    private val isAdmin: Boolean = false,
    private val onStatusUpdate: ((String, String) -> Unit)? = null,
    private val onViewClick: ((String) -> Unit)? = null
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

    fun updateBookings(newBookings: List<Booking>) {
        bookings = newBookings
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = bookings.size

    inner class BookingViewHolder(private val binding: ItemBookingBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(booking: Booking) {
            binding.serviceNameTextView.text = booking.serviceName
            binding.bookingTimeTextView.text = "${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault()).format(java.util.Date(booking.time))}"
            binding.statusTextView.text = booking.status.uppercase()
            
            val (bgColor, textColor) = when (booking.status.lowercase()) {
                "pending" -> R.drawable.status_pending_bg to Color.parseColor("#92400E")
                "confirmed" -> R.drawable.status_confirmed_bg to Color.parseColor("#166534")
                "cancelled" -> R.drawable.status_cancelled_bg to Color.parseColor("#991B1B")
                else -> R.drawable.category_chip_bg to Color.WHITE
            }
            
            binding.statusTextView.setBackgroundResource(bgColor)
            binding.statusTextView.setTextColor(textColor)

            if (isAdmin) {
                binding.viewButton.visibility = View.VISIBLE
                binding.viewButton.setOnClickListener {
                    onViewClick?.invoke(booking.id)
                }

                if (booking.status == "pending") {
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
            } else {
                binding.viewButton.visibility = View.GONE
                binding.confirmButton.visibility = View.GONE
                binding.cancelButton.visibility = View.GONE
            }

            binding.root.setOnClickListener {
                onViewClick?.invoke(booking.id)
            }
        }
    }
}
