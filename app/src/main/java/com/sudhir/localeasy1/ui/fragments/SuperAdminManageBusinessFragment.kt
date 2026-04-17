package com.sudhir.localeasy1.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import com.sudhir.localeasy1.data.Business
import com.sudhir.localeasy1.databinding.FragmentSuperAdminManageBusinessBinding
import com.sudhir.localeasy1.ui.adapters.ManageBusinessAdapter

class SuperAdminManageBusinessFragment : Fragment() {

    private var _binding: FragmentSuperAdminManageBusinessBinding? = null
    private val binding get() = _binding!!
    private val db = FirebaseFirestore.getInstance()
    private lateinit var adapter: ManageBusinessAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSuperAdminManageBusinessBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<TextView>(com.sudhir.localeasy1.R.id.titleText).text = "Manage Businesses"
        adapter = ManageBusinessAdapter(emptyList())
        binding.businessRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.businessRecyclerView.adapter = adapter
        loadBusinesses()
    }

    override fun onResume() {
        super.onResume()
        loadBusinesses()
    }

    private fun loadBusinesses() {
        db.collection("businesses")
            .get()
            .addOnSuccessListener { result ->
                val currentBinding = _binding ?: return@addOnSuccessListener
                val businesses = result.documents.map { doc ->
                    Business(
                        id = doc.id,
                        ownerId = doc.getString("ownerId") ?: "",
                        name = doc.getString("name") ?: "",
                        description = doc.getString("description") ?: "",
                        category = doc.getString("category") ?: "",
                        address = doc.getString("address") ?: "",
                        approved = doc.getBoolean("approved") == true,
                        createdAt = doc.getLong("createdAt") ?: 0L
                    )
                }
                currentBinding.emptyStateTextView.visibility = if (businesses.isEmpty()) View.VISIBLE else View.GONE
                adapter.updateList(businesses)
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
