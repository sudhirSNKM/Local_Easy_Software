package com.sudhir.localeasy1.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.firebase.firestore.FirebaseFirestore
import com.sudhir.localeasy1.databinding.FragmentSuperAdminAnalyticsBinding

class SuperAdminAnalyticsFragment : Fragment() {

    private var _binding: FragmentSuperAdminAnalyticsBinding? = null
    private val binding get() = _binding!!
    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSuperAdminAnalyticsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadAnalytics()
    }

    private fun loadAnalytics() {
        db.collection("bookings").get().addOnSuccessListener { result ->
            binding.totalBookingsValue.text = result.size().toString()
            val totalRevenue = result.documents.sumOf { doc ->
                doc.getDouble("price") ?: doc.getLong("price")?.toDouble() ?: 0.0
            }
            binding.revenueValue.text = "Rs. ${totalRevenue.toInt()}"
            binding.growthValue.text = if (result.size() > 0) "+12% this month" else "No growth data"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
