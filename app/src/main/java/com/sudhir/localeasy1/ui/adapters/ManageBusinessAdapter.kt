package com.sudhir.localeasy1.ui.adapters

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.sudhir.localeasy1.data.Business
import com.sudhir.localeasy1.databinding.ItemManageBusinessBinding
import com.sudhir.localeasy1.ui.activities.BusinessDetailActivity

class ManageBusinessAdapter(
    private var businesses: List<Business>
) : RecyclerView.Adapter<ManageBusinessAdapter.ManageBusinessViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ManageBusinessViewHolder {
        val binding = ItemManageBusinessBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ManageBusinessViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ManageBusinessViewHolder, position: Int) {
        holder.bind(businesses[position])
    }

    override fun getItemCount(): Int = businesses.size

    fun updateList(newBusinesses: List<Business>) {
        businesses = newBusinesses
        notifyDataSetChanged()
    }

    inner class ManageBusinessViewHolder(private val binding: ItemManageBusinessBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(business: Business) {
            binding.businessNameTextView.text = business.name
            binding.ownerTextView.text = "Owner: ${business.ownerId}"
            binding.statusTextView.text = if (business.approved) "Approved" else "Pending approval"
            binding.root.setOnClickListener {
                val intent = Intent(binding.root.context, BusinessDetailActivity::class.java)
                intent.putExtra("businessId", business.id)
                binding.root.context.startActivity(intent)
            }
        }
    }
}
