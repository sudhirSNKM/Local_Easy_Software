package com.sudhir.localeasy1.ui.adapters

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.recyclerview.widget.RecyclerView
import com.sudhir.localeasy1.R
import com.sudhir.localeasy1.data.Service
import com.sudhir.localeasy1.databinding.ItemServiceBinding
import com.sudhir.localeasy1.databinding.ItemServiceAdminBinding
import com.sudhir.localeasy1.ui.activities.BookingConfirmActivity

class ServiceAdapter(
    private var services: List<Service>,
    private val isAdmin: Boolean = false,
    private val onServiceClick: (Service) -> Unit = {},
    private val onEditClick: (Service) -> Unit = {},
    private val onDeleteClick: (Service) -> Unit = {},
    private val onViewBookingsClick: (Service) -> Unit = {}
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_USER = 1
        private const val VIEW_TYPE_ADMIN = 2
    }

    override fun getItemViewType(position: Int): Int {
        return if (isAdmin) VIEW_TYPE_ADMIN else VIEW_TYPE_USER
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == VIEW_TYPE_ADMIN) {
            val binding = ItemServiceAdminBinding.inflate(inflater, parent, false)
            AdminServiceViewHolder(binding)
        } else {
            val binding = ItemServiceBinding.inflate(inflater, parent, false)
            ServiceViewHolder(binding)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val service = services[position]
        if (holder is AdminServiceViewHolder) {
            holder.bind(service)
        } else if (holder is ServiceViewHolder) {
            holder.bind(service)
        }
    }

    override fun getItemCount(): Int = services.size

    fun updateList(newList: List<Service>) {
        services = newList
        notifyDataSetChanged()
    }

    inner class ServiceViewHolder(private val binding: ItemServiceBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(service: Service) {
            binding.serviceNameTextView.text = service.name
            binding.serviceCategoryTextView.text = service.category
            binding.serviceDurationTextView.text = "Duration: ${service.duration}"
            binding.servicePriceTextView.text = "₹${service.price}"

            // Apply fade-in animation
            val fadeInAnim = AnimationUtils.loadAnimation(binding.root.context, R.anim.fade_in)
            binding.root.startAnimation(fadeInAnim)

            binding.bookButton.setOnClickListener {
                // Scale animation on button click
                it.animate()
                    .scaleX(0.95f)
                    .scaleY(0.95f)
                    .setDuration(100)
                    .withEndAction {
                        it.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(100)
                            .start()
                    }
                    .start()

                val intent = Intent(binding.root.context, BookingConfirmActivity::class.java).apply {
                    putExtra("serviceId", service.id)
                    putExtra("businessId", service.businessId)
                    putExtra("serviceName", service.name)
                    putExtra("price", service.price.toDouble())
                }
                binding.root.context.startActivity(intent)
            }

            binding.root.setOnClickListener { onServiceClick(service) }
        }
    }

    inner class AdminServiceViewHolder(private val binding: ItemServiceAdminBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(service: Service) {
            binding.serviceNameTextView.text = service.name
            binding.serviceCategoryTextView.text = service.category
            binding.serviceDurationTextView.text = service.duration
            binding.servicePriceTextView.text = "₹${service.price}"

            binding.editBtn.setOnClickListener {
                onEditClick(service)
            }

            binding.deleteBtn.setOnClickListener {
                onDeleteClick(service)
            }

            // View Bookings button
            binding.viewBookingsBtn.setOnClickListener {
                onViewBookingsClick(service)
            }
        }
    }
}
