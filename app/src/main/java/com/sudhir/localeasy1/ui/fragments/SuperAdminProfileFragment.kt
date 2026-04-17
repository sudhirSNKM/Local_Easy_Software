package com.sudhir.localeasy1.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.sudhir.localeasy1.databinding.FragmentSuperAdminProfileBinding
import com.sudhir.localeasy1.ui.activities.LoginActivity

class SuperAdminProfileFragment : Fragment() {

    private var _binding: FragmentSuperAdminProfileBinding? = null
    private val binding get() = _binding!!
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSuperAdminProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<TextView>(com.sudhir.localeasy1.R.id.titleText).text = "Profile"
        loadProfile()
        binding.logoutBtn.setOnClickListener {
            auth.signOut()
            val intent = Intent(requireContext(), LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
        }
    }

    private fun loadProfile() {
        val uid = auth.currentUser?.uid ?: return
        _binding?.emailValue?.text = auth.currentUser?.email ?: "-"
        db.collection("users").document(uid).get().addOnSuccessListener {
            _binding?.nameValue?.text = it.getString("name") ?: "Super Admin"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
