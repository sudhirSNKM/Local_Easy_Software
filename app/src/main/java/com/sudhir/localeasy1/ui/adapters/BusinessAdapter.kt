package com.sudhir.localeasy1.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.sudhir.localeasy1.data.Business
import com.sudhir.localeasy1.databinding.ItemBusinessBinding

class BusinessAdapter(
    private val businesses: List<Business>,
    private val onApprove: (String) -> Unit,
    private val onReject: (String) -> Unit
) : RecyclerView.Adapter<BusinessAdapter.BusinessViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BusinessViewHolder {
        val binding = ItemBusinessBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return BusinessViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BusinessViewHolder, position: Int) {
        holder.bind(businesses[position])
    }

    override fun getItemCount(): Int = businesses.size

    inner class BusinessViewHolder(private val binding: ItemBusinessBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(business: Business) {
            binding.businessNameTextView.text = business.name
            binding.businessDescriptionTextView.text = business.description
            binding.businessCategoryTextView.text = "Category: ${business.category}"
            binding.businessAddressTextView.text = business.address
            binding.approveButton.visibility = if (business.approved) View.GONE else View.VISIBLE

            binding.approveButton.setOnClickListener {
                onApprove(business.id)
            }

            binding.rejectButton.setOnClickListener {
                onReject(business.id)
            }
        }
    }
}
