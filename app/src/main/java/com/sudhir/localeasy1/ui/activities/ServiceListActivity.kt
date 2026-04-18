package com.sudhir.localeasy1.ui.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.sudhir.localeasy1.data.Service
import com.sudhir.localeasy1.databinding.ActivityServiceListBinding
import com.sudhir.localeasy1.ui.adapters.ServiceAdapter

class ServiceListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityServiceListBinding
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private lateinit var serviceAdapter: ServiceAdapter
    private var businessId: String? = null
    private var businessApproved: Boolean = false
    private var allServices = mutableListOf<Service>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityServiceListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        binding.header.titleText.text = "Manage Services"
        loadBusinessAndServices()
        setupSearch()
    }

    private fun setupRecyclerView() {
        binding.serviceRecycler.layoutManager = LinearLayoutManager(this)
        // We'll use a modified ServiceAdapter that supports edit/delete
    }

    private fun loadBusinessAndServices() {
        val uid = auth.currentUser?.uid ?: return
        db.collection("businesses")
            .whereEqualTo("ownerId", uid)
            .limit(1)
            .get()
            .addOnSuccessListener { result ->
                val doc = result.documents.firstOrNull()
                businessId = doc?.id
                businessApproved = doc?.getBoolean("approved") == true
                if (!businessApproved) {
                    Toast.makeText(this, "Waiting for approval", Toast.LENGTH_SHORT).show()
                    binding.serviceRecycler.adapter = ServiceAdapter(emptyList(), isAdmin = true)
                    return@addOnSuccessListener
                }
                loadServices()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadServices() {
        val resolvedBusinessId = businessId ?: return
        db.collection("services")
            .whereEqualTo("businessId", resolvedBusinessId)
            .get()
            .addOnSuccessListener { result ->
                val list = ArrayList<Service>()
                for (doc in result) {
                    val service = doc.toObject(Service::class.java).copy(id = doc.id)
                    list.add(service)
                }
                
                allServices = list // Save all services for search
                serviceAdapter = ServiceAdapter(
                    services = list,
                    isAdmin = true,
                    onEditClick = { service ->
                        val intent = Intent(this, AddServiceActivity::class.java)
                        intent.putExtra("serviceId", service.id)
                        intent.putExtra("isEdit", true)
                        startActivity(intent)
                    },
                    onDeleteClick = { service ->
                        deleteService(service)
                    },
                    onViewBookingsClick = { service ->
                        val intent = Intent(this, ServiceBookingsActivity::class.java)
                        intent.putExtra("serviceId", service.id)
                        intent.putExtra("serviceName", service.name)
                        startActivity(intent)
                    }
                )
                binding.serviceRecycler.adapter = serviceAdapter
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error loading services: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun setupSearch() {
        binding.searchBar.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s.toString().lowercase()
                val filtered = allServices.filter {
                    it.name.lowercase().contains(query)
                }
                serviceAdapter.updateList(filtered)
            }

            override fun afterTextChanged(s: android.text.Editable?) {}
        })
    }

    private fun deleteService(service: Service) {
        db.collection("services").document(service.id)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(this, "Service deleted", Toast.LENGTH_SHORT).show()
                loadServices() // Reload list
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error deleting: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
