package com.sudhir.localeasy1.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import com.sudhir.localeasy1.R
import com.sudhir.localeasy1.databinding.FragmentHomeBinding
import com.sudhir.localeasy1.ui.activities.BookingConfirmActivity
import com.sudhir.localeasy1.ui.adapters.ServiceAdapter
import com.sudhir.localeasy1.ui.viewmodel.HomeViewModel

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val homeViewModel: HomeViewModel by viewModels()
    private lateinit var serviceAdapter: ServiceAdapter
    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        try {
            Log.d("HomeFragment", "onViewCreated started")
            setupRecyclerView()
            setupCategoryButtons()
            setupObservers()
            loadApprovedOfferPromo()
            Log.d("HomeFragment", "Setup completed")
        } catch (e: Exception) {
            Log.e("HomeFragment", "Error in onViewCreated", e)
            Toast.makeText(requireContext(), "Error loading home screen: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun setupRecyclerView() {
        try {
            serviceAdapter = ServiceAdapter(emptyList()) { service ->
                val intent = Intent(requireContext(), BookingConfirmActivity::class.java).apply {
                    putExtra("serviceId", service.id)
                    putExtra("serviceName", service.name)
                    putExtra("price", service.price.toDouble())
                    putExtra("businessId", service.businessId)
                }
                startActivity(intent)
            }
            binding.servicesRecyclerView.layoutManager = LinearLayoutManager(requireContext())
            binding.servicesRecyclerView.adapter = serviceAdapter
            Log.d("HomeFragment", "RecyclerView setup completed")
        } catch (e: Exception) {
            Log.e("HomeFragment", "Error setting up RecyclerView", e)
        }
    }

    private fun setupCategoryButtons() {
        try {
            homeViewModel.categories.observe(viewLifecycleOwner, Observer { categories ->
                Log.d("HomeFragment", "Categories loaded: ${categories.size}")
                binding.categoriesContainer.removeAllViews()
                categories.forEach { category ->
                    try {
                        val button = Button(requireContext()).apply {
                            text = category
                            setBackgroundResource(R.drawable.category_chip_bg)
                            setTextColor(resources.getColor(R.color.text_primary))
                            textSize = 14f
                            setPadding(16, 8, 16, 8)
                            setOnClickListener {
                                Log.d("HomeFragment", "Category clicked: $category")
                                if (category == "All") {
                                    homeViewModel.loadAllServices()
                                } else {
                                    homeViewModel.loadServicesByCategory(category)
                                }
                            }
                        }
                        val params = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        ).apply {
                            marginEnd = 8
                        }
                        button.layoutParams = params
                        binding.categoriesContainer.addView(button)
                    } catch (e: Exception) {
                        Log.e("HomeFragment", "Error creating category button", e)
                    }
                }
            })
        } catch (e: Exception) {
            Log.e("HomeFragment", "Error setting up category buttons", e)
        }
    }

    private fun setupObservers() {
        try {
            homeViewModel.services.observe(viewLifecycleOwner, Observer { services ->
                Log.d("HomeFragment", "Services loaded: ${services.size}")
                try {
                    serviceAdapter = ServiceAdapter(services) { service ->
                        // Navigate to booking confirmation
                        val intent = Intent(requireContext(), BookingConfirmActivity::class.java).apply {
                            putExtra("serviceName", service.name)
                            putExtra("price", service.price.toDouble())
                            putExtra("serviceId", service.id)
                            putExtra("businessId", service.businessId)
                        }
                        startActivity(intent)
                    }
                    binding.servicesRecyclerView.adapter = serviceAdapter
                } catch (e: Exception) {
                    Log.e("HomeFragment", "Error updating adapter", e)
                }
            })

            homeViewModel.isLoading.observe(viewLifecycleOwner, Observer { isLoading ->
                Log.d("HomeFragment", "Loading state: $isLoading")
                // Show/hide loading indicator if needed
            })

            homeViewModel.error.observe(viewLifecycleOwner, Observer { error ->
                error?.let {
                    Log.e("HomeFragment", "Error: $it")
                    Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                }
            })
        } catch (e: Exception) {
            Log.e("HomeFragment", "Error setting up observers", e)
        }
    }

    private fun loadApprovedOfferPromo() {
        db.collection("offers")
            .get()
            .addOnSuccessListener { result ->
                result.documents.forEach { offerDoc ->
                    val businessId = offerDoc.getString("businessId") ?: return@forEach
                    db.collection("businesses")
                        .document(businessId)
                        .get()
                        .addOnSuccessListener { businessDoc ->
                            if (businessDoc.getBoolean("approved") == true) {
                                val title = offerDoc.getString("title") ?: "Special Offer"
                                val discount = offerDoc.getLong("discount")?.toInt()
                                    ?: (offerDoc.getDouble("discount")?.toInt() ?: 0)
                                binding.promoTitleTextView.text = title
                                binding.promoSubtitleTextView.text = "Get $discount% off on selected services"
                                return@addOnSuccessListener
                            }
                        }
                }
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
