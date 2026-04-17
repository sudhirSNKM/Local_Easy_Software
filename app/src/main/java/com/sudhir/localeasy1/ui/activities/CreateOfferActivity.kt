package com.sudhir.localeasy1.ui.activities

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.sudhir.localeasy1.databinding.ActivityCreateOfferBinding

class CreateOfferActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCreateOfferBinding
    private val db = FirebaseFirestore.getInstance()
    private var adminBusinessId: String? = null
    private var businessApproved: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateOfferBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loadAdminBusinessState()
        setupClickListener()
    }

    private fun loadAdminBusinessState() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        db.collection("businesses")
            .whereEqualTo("ownerId", uid)
            .limit(1)
            .get()
            .addOnSuccessListener { result ->
                val doc = result.documents.firstOrNull()
                adminBusinessId = doc?.id
                businessApproved = doc?.getBoolean("approved") == true
            }
    }

    private fun setupClickListener() {
        binding.submitButton.setOnClickListener {
            val title = binding.titleEditText.text.toString().trim()
            val description = binding.descriptionEditText.text.toString().trim()
            val discount = binding.discountEditText.text.toString().trim()

            if (title.isEmpty() || description.isEmpty() || discount.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            binding.submitButton.isEnabled = false
            binding.submitButton.text = "Creating..."

            val businessId = adminBusinessId
            if (businessId.isNullOrBlank()) {
                Toast.makeText(this, "Create your business first", Toast.LENGTH_SHORT).show()
                binding.submitButton.isEnabled = true
                binding.submitButton.text = "Create Offer"
                return@setOnClickListener
            }
            if (!businessApproved) {
                Toast.makeText(this, "Waiting for approval", Toast.LENGTH_SHORT).show()
                binding.submitButton.isEnabled = true
                binding.submitButton.text = "Create Offer"
                return@setOnClickListener
            }

            val offer = mutableMapOf<String, Any>()
            offer["title"] = title
            offer["description"] = description
            offer["discount"] = discount.toIntOrNull() ?: 0
            offer["businessId"] = businessId
            offer["createdAt"] = System.currentTimeMillis()

            db.collection("offers")
                .add(offer)
                .addOnSuccessListener {
                    Toast.makeText(this, "Offer created successfully!", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    binding.submitButton.isEnabled = true
                    binding.submitButton.text = "Create Offer"
                }
        }
    }
}
