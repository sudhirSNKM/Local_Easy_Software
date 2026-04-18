package com.sudhir.localeasy1.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.sudhir.localeasy1.data.Booking
import com.sudhir.localeasy1.databinding.ItemServiceBookingBinding
import java.text.SimpleDateFormat
import java.util.*

class ServiceBookingItemAdapter(
    private var bookings: List<Booking>,
    private val onCallClick: (Booking) -> Unit
) : RecyclerView.Adapter<ServiceBookingItemAdapter.ViewHolder>() {

    inner class ViewHolder(private val binding: ItemServiceBookingBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(booking: Booking) {
            // Set service name
            binding.serviceName.text = booking.serviceName

            // Set user name (clean, no prefixes)
            binding.userName.text = booking.userName

            // Set phone (clean, no prefixes)
            binding.userPhone.text = booking.phone

            // Format and set date and time separately
            val (dateStr, timeStr) = formatDateTime(booking.time)
            binding.bookingDate.text = dateStr
            binding.bookingTime.text = timeStr

            // Set booking status with color
            binding.bookingStatus.text = booking.status.uppercase()
            binding.bookingStatus.setBackgroundColor(
                when (booking.status) {
                    "confirmed" -> android.graphics.Color.parseColor("#22C55E")
                    "completed" -> android.graphics.Color.parseColor("#3B82F6")
                    "cancelled" -> android.graphics.Color.parseColor("#EF4444")
                    else -> android.graphics.Color.parseColor("#F59E0B") // pending
                }
            )

            // Call button
            binding.callBtn.setOnClickListener {
                onCallClick(booking)
            }
        }

        private fun formatDateTime(timeMillis: Long): Pair<String, String> {
            return try {
                val calendar = Calendar.getInstance().apply {
                    this.timeInMillis = timeMillis
                }

                val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())

                val dateStr = dateFormat.format(calendar.time)
                val timeStr = timeFormat.format(calendar.time)

                Pair(dateStr, timeStr)
            } catch (e: Exception) {
                Pair("Date not available", "Time not available")
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemServiceBookingBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(bookings[position])
    }

    override fun getItemCount(): Int = bookings.size

    fun updateBookings(newBookings: List<Booking>) {
        bookings = newBookings
        notifyDataSetChanged()
    }
}
