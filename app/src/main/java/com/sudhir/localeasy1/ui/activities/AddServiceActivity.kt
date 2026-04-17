package com.sudhir.localeasy1.ui.activities

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.sudhir.localeasy1.databinding.ActivityAddServiceBinding

class AddServiceActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddServiceBinding
    private val db = FirebaseFirestore.getInstance()
    private var isEdit = false
    private var serviceId: String? = null
    private var adminBusinessId: String? = null
    private var businessApproved: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddServiceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        isEdit = intent.getBooleanExtra("isEdit", false)
        serviceId = intent.getStringExtra("serviceId")

        if (isEdit && serviceId != null) {
            loadServiceData()
            binding.submitButton.text = "Update Service"
        }

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

    private fun loadServiceData() {
        serviceId?.let { id ->
            db.collection("services").document(id).get()
                .addOnSuccessListener { doc ->
                    if (doc != null) {
                        binding.serviceNameEditText.setText(doc.getString("name"))
                        binding.categoryEditText.setText(doc.getString("category"))
                        binding.priceEditText.setText(doc.get("price").toString())
                        binding.durationEditText.setText(doc.getString("duration"))
                    }
                }
        }
    }

    private fun setupClickListener() {
        binding.submitButton.setOnClickListener {
            val serviceName = binding.serviceNameEditText.text.toString().trim()
            val category = binding.categoryEditText.text.toString().trim()
            val priceStr = binding.priceEditText.text.toString().trim()
            val duration = binding.durationEditText.text.toString().trim()

            if (serviceName.isEmpty() || category.isEmpty() || priceStr.isEmpty() || duration.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val price = priceStr.toDoubleOrNull() ?: 0.0
            binding.submitButton.isEnabled = false
            binding.submitButton.text = if (isEdit) "Updating..." else "Adding..."

            val businessId = adminBusinessId
            if (businessId.isNullOrBlank()) {
                Toast.makeText(this, "Create your business first", Toast.LENGTH_SHORT).show()
                binding.submitButton.isEnabled = true
                binding.submitButton.text = if (isEdit) "Update Service" else "Add Service"
                return@setOnClickListener
            }

            if (!businessApproved) {
                Toast.makeText(this, "Waiting for approval", Toast.LENGTH_SHORT).show()
                binding.submitButton.isEnabled = true
                binding.submitButton.text = if (isEdit) "Update Service" else "Add Service"
                return@setOnClickListener
            }

            val service = mutableMapOf<String, Any>()
            service["name"] = serviceName
            service["category"] = category
            service["price"] = price
            service["duration"] = duration
            service["businessId"] = businessId

            val task = if (isEdit && serviceId != null) {
                db.collection("services").document(serviceId!!).update(service)
            } else {
                service["createdAt"] = System.currentTimeMillis()
                db.collection("services").add(service)
            }

            task.addOnSuccessListener {
                val msg = if (isEdit) "Service updated!" else "Service added!"
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                binding.submitButton.isEnabled = true
                binding.submitButton.text = if (isEdit) "Update Service" else "Add Service"
            }
        }
    }
}
