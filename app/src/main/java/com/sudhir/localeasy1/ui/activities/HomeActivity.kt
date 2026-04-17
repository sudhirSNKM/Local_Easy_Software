package com.sudhir.localeasy1.ui.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.sudhir.localeasy1.R
import com.sudhir.localeasy1.databinding.ActivityHomeBinding
import com.sudhir.localeasy1.ui.adapters.ServiceAdapter
import com.sudhir.localeasy1.ui.viewmodel.HomeViewModel

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private val homeViewModel: HomeViewModel by viewModels()
    private lateinit var serviceAdapter: ServiceAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            Log.d("HomeActivity", "onCreate started")
            binding = ActivityHomeBinding.inflate(layoutInflater)
            setContentView(binding.root)
            Log.d("HomeActivity", "Layout inflated successfully")

            setupRecyclerView()
            setupCategoryButtons()
            setupObservers()
            Log.d("HomeActivity", "Setup completed")
        } catch (e: Exception) {
            Log.e("HomeActivity", "Error in onCreate", e)
            Toast.makeText(this, "Error loading home screen: ${e.message}", Toast.LENGTH_LONG).show()
            finish()
        }
    }

    private fun setupRecyclerView() {
        try {
            serviceAdapter = ServiceAdapter(emptyList())
            binding.servicesRecyclerView.layoutManager = LinearLayoutManager(this)
            binding.servicesRecyclerView.adapter = serviceAdapter
            Log.d("HomeActivity", "RecyclerView setup completed")
        } catch (e: Exception) {
            Log.e("HomeActivity", "Error setting up RecyclerView", e)
        }
    }

    private fun setupCategoryButtons() {
        try {
            homeViewModel.categories.observe(this, Observer { categories ->
                Log.d("HomeActivity", "Categories loaded: ${categories.size}")
                binding.categoriesContainer.removeAllViews()
                categories.forEach { category ->
                    try {
                        val button = Button(this).apply {
                            text = category
                            setBackgroundResource(R.drawable.category_chip_bg)
                            setTextColor(resources.getColor(R.color.text_primary))
                            textSize = 14f
                            setPadding(16, 8, 16, 8)
                            setOnClickListener {
                                Log.d("HomeActivity", "Category clicked: $category")
                                homeViewModel.loadServicesByCategory(category)
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
                        Log.e("HomeActivity", "Error creating category button", e)
                    }
                }
            })
        } catch (e: Exception) {
            Log.e("HomeActivity", "Error setting up category buttons", e)
        }
    }

    private fun setupObservers() {
        try {
            homeViewModel.services.observe(this, Observer { services ->
                Log.d("HomeActivity", "Services loaded: ${services.size}")
                try {
                    serviceAdapter = ServiceAdapter(services)
                    binding.servicesRecyclerView.adapter = serviceAdapter
                } catch (e: Exception) {
                    Log.e("HomeActivity", "Error updating adapter", e)
                }
            })

            homeViewModel.isLoading.observe(this, Observer { isLoading ->
                Log.d("HomeActivity", "Loading state: $isLoading")
                // Show/hide loading indicator if needed
            })

            homeViewModel.error.observe(this, Observer { error ->
                error?.let {
                    Log.e("HomeActivity", "Error: $it")
                    Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
                }
            })
        } catch (e: Exception) {
            Log.e("HomeActivity", "Error setting up observers", e)
        }
    }
}
