package com.sudhir.localeasy1.ui.activities

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.sudhir.localeasy1.databinding.ActivityBusinessDetailBinding

class BusinessDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBusinessDetailBinding
    private val db = FirebaseFirestore.getInstance()
    private lateinit var businessId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBusinessDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        businessId = intent.getStringExtra("businessId").orEmpty()
        if (businessId.isBlank()) {
            Toast.makeText(this, "Business not found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        binding.approveBtn.setOnClickListener { approveBusiness() }
        binding.deleteBtn.setOnClickListener { deleteBusiness() }
        binding.backBtn.setOnClickListener { finish() }

        loadBusinessInfo()
        loadServices()
        loadOffers()
    }

    private fun loadBusinessInfo() {
        db.collection("businesses").document(businessId).get()
            .addOnSuccessListener {
                binding.businessNameText.text = it.getString("name") ?: "Business"
                binding.businessOwnerText.text = "Owner: ${it.getString("ownerId") ?: "-"}"
                val approved = it.getBoolean("approved") == true
                binding.businessStatusText.text = if (approved) "Approved" else "Pending approval"
            }
    }

    private fun loadServices() {
        db.collection("services")
            .whereEqualTo("businessId", businessId)
            .get()
            .addOnSuccessListener { result ->
                if (result.isEmpty) {
                    binding.servicesText.text = "No services found"
                } else {
                    binding.servicesText.text = result.documents.joinToString("\n") { doc ->
                        val name = doc.getString("name") ?: "Service"
                        val price = doc.getDouble("price") ?: doc.getLong("price")?.toDouble() ?: 0.0
                        "- $name (Rs. ${price.toInt()})"
                    }
                }
            }
    }

    private fun loadOffers() {
        db.collection("offers")
            .whereEqualTo("businessId", businessId)
            .get()
            .addOnSuccessListener { result ->
                if (result.isEmpty) {
                    binding.offersText.text = "No offers found"
                } else {
                    binding.offersText.text = result.documents.joinToString("\n") { doc ->
                        val title = doc.getString("title") ?: "Offer"
                        val discount = doc.getLong("discount")?.toInt()
                            ?: (doc.getDouble("discount")?.toInt() ?: 0)
                        "- $title ($discount% OFF)"
                    }
                }
            }
    }

    private fun approveBusiness() {
        db.collection("businesses")
            .document(businessId)
            .update("approved", true)
            .addOnSuccessListener {
                Toast.makeText(this, "Business approved", Toast.LENGTH_SHORT).show()
                loadBusinessInfo()
            }
    }

    private fun deleteBusiness() {
        db.collection("businesses")
            .document(businessId)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(this, "Business deleted", Toast.LENGTH_SHORT).show()
                finish()
            }
    }
}
