package com.sudhir.localeasy1.ui.activities

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
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
    private val timings = mutableListOf<String>()
    private val categories = listOf("Salon", "Clinic", "Gym", "Spa", "Restaurant", "Cleaning")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddServiceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        isEdit = intent.getBooleanExtra("isEdit", false)
        serviceId = intent.getStringExtra("serviceId")

        if (isEdit && serviceId != null) {
            loadServiceData()
            binding.submitButton.text = "Update Service"
            binding.header.titleText.text = "Edit Service"
        } else {
            binding.header.titleText.text = "Add Service"
        }

        loadAdminBusinessState()
        setupClickListeners()
    }

    private fun setupCategoryDropdown() {
        // Categories list for reference (user can type or select)
        binding.categoryDropdown.hint = "Select category"
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
                        binding.serviceNameEditText.setText(doc.getString("name") ?: "")
                        binding.categoryDropdown.setText(doc.getString("category") ?: "")
                        binding.priceEditText.setText(doc.get("price").toString())
                        binding.durationEditText.setText(doc.getString("duration") ?: "")
                        binding.notesInput.setText(doc.getString("notes") ?: "")

                        val timingsList = doc.get("timings") as? List<String>
                        if (timingsList != null) {
                            timings.clear()
                            timings.addAll(timingsList)
                            updateTimeList()
                        }
                    }
                }
        }
    }

    private fun setupClickListeners() {
        // Add time button
        binding.addTimeBtn.setOnClickListener {
            addTime()
        }

        // Submit service button
        binding.submitButton.setOnClickListener {
            submitService()
        }
    }

    private fun addTime() {
        val timeInput = binding.timeInput.text.toString().trim()

        if (timeInput.isEmpty()) {
            Toast.makeText(this, "Please enter a time", Toast.LENGTH_SHORT).show()
            return
        }

        if (!timings.contains(timeInput)) {
            timings.add(timeInput)
            updateTimeList()
            binding.timeInput.setText("")
        } else {
            Toast.makeText(this, "Time already added", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateTimeList() {
        binding.timeList.text = if (timings.isEmpty()) {
            "No timings added"
        } else {
            timings.joinToString(", ")
        }
    }

    private fun submitService() {
        val serviceName = binding.serviceNameEditText.text.toString().trim()
        val category = binding.categoryDropdown.text.toString().trim()
        val price = binding.priceEditText.text.toString().trim()
        val duration = binding.durationEditText.text.toString().trim()
        val notes = binding.notesInput.text.toString().trim()

        if (serviceName.isEmpty() || category.isEmpty() || price.isEmpty() || duration.isEmpty()) {
            Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show()
            return
        }

        if (timings.isEmpty()) {
            Toast.makeText(this, "Please add at least one time slot", Toast.LENGTH_SHORT).show()
            return
        }

        binding.submitButton.isEnabled = false
        binding.submitButton.text = if (isEdit) "Updating..." else "Adding..."

        val businessId = adminBusinessId
        if (businessId.isNullOrBlank()) {
            Toast.makeText(this, "Create your business first", Toast.LENGTH_SHORT).show()
            binding.submitButton.isEnabled = true
            binding.submitButton.text = if (isEdit) "Update Service" else "Add Service"
            return
        }

        if (!businessApproved) {
            Toast.makeText(this, "Waiting for approval", Toast.LENGTH_SHORT).show()
            binding.submitButton.isEnabled = true
            binding.submitButton.text = if (isEdit) "Update Service" else "Add Service"
            return
        }

        val service = mutableMapOf<String, Any>()
        service["name"] = serviceName
        service["category"] = category
        service["price"] = price.toDoubleOrNull() ?: 0.0
        service["duration"] = duration
        service["businessId"] = businessId
        service["timings"] = timings
        service["notes"] = notes

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
