package com.sudhir.localeasy1.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.firebase.firestore.FirebaseFirestore
import com.sudhir.localeasy1.databinding.FragmentSuperAdminDashboardBinding

class SuperAdminDashboardFragment : Fragment() {

    private var _binding: FragmentSuperAdminDashboardBinding? = null
    private val binding get() = _binding!!
    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSuperAdminDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<TextView>(com.sudhir.localeasy1.R.id.titleText).text = "Dashboard"
        loadStats()
    }

    private fun loadStats() {
        db.collection("businesses")
            .whereEqualTo("approved", false)
            .get()
            .addOnSuccessListener { result ->
                _binding?.pendingApprovalsValue?.text = result.size().toString()
            }

        db.collection("businesses")
            .get()
            .addOnSuccessListener { result ->
                _binding?.totalBusinessesValue?.text = result.size().toString()
            }

        db.collection("users")
            .get()
            .addOnSuccessListener { result ->
                _binding?.totalUsersValue?.text = result.size().toString()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
