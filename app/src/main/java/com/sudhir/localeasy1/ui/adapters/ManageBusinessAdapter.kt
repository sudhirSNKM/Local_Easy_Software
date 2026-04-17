package com.sudhir.localeasy1.ui.adapters

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.sudhir.localeasy1.data.Business
import com.sudhir.localeasy1.databinding.ItemManageBusinessBinding
import com.sudhir.localeasy1.ui.activities.BusinessDetailActivity

class ManageBusinessAdapter(
    private var businesses: List<Business>
) : RecyclerView.Adapter<ManageBusinessAdapter.ManageBusinessViewHolder>() {
    private val db = FirebaseFirestore.getInstance()
    private val ownerNameCache = mutableMapOf<String, String>()

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
            binding.statusTextView.text = if (business.approved) "Status: Active" else "Status: Pending Approval"

            val cachedName = ownerNameCache[business.ownerId]
            if (!cachedName.isNullOrBlank()) {
                binding.ownerTextView.text = "Owner: $cachedName"
            } else if (business.ownerId.isNotBlank()) {
                db.collection("users").document(business.ownerId).get()
                    .addOnSuccessListener { doc ->
                        val ownerName = doc.getString("name")
                        if (!ownerName.isNullOrBlank()) {
                            ownerNameCache[business.ownerId] = ownerName
                            binding.ownerTextView.text = "Owner: $ownerName"
                        }
                    }
            }
            binding.root.setOnClickListener {
                val intent = Intent(binding.root.context, BusinessDetailActivity::class.java)
                intent.putExtra("businessId", business.id)
                binding.root.context.startActivity(intent)
            }
        }
    }
}
