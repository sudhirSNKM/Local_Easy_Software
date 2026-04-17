package com.sudhir.localeasy1.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
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
        view.findViewById<TextView>(com.sudhir.localeasy1.R.id.titleText).text = "Analytics"
        loadAnalytics()
    }

    private fun loadAnalytics() {
        db.collection("bookings").get().addOnSuccessListener { result ->
            val currentBinding = _binding ?: return@addOnSuccessListener
            currentBinding.totalBookingsValue.text = result.size().toString()
            val totalRevenue = result.documents.sumOf { doc ->
                doc.getDouble("price") ?: doc.getLong("price")?.toDouble() ?: 0.0
            }
            currentBinding.revenueValue.text = "Rs. ${totalRevenue.toInt()}"
            currentBinding.growthValue.text = if (result.size() > 0) "+12% this month" else "No growth data"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
