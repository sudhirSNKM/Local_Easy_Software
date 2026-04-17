package com.sudhir.localeasy1.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.sudhir.localeasy1.R
import com.sudhir.localeasy1.databinding.FragmentProfileBinding
import com.sudhir.localeasy1.ui.activities.LoginActivity

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadUserProfile()
        setupClickListeners()
    }

    private fun loadUserProfile() {
        val user = auth.currentUser ?: return

        binding.emailTextView.text = user.email ?: "No email"

        // Load additional profile data from Firestore
        db.collection("users").document(user.uid)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    binding.nameTextView.text = document.getString("name") ?: "No name"
                    binding.phoneTextView.text = document.getString("phone") ?: "No phone"
                } else {
                    binding.nameTextView.text = "No name"
                    binding.phoneTextView.text = "No phone"
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Error loading profile: ${e.message}", Toast.LENGTH_SHORT).show()
                binding.nameTextView.text = "Error loading name"
                binding.phoneTextView.text = "Error loading phone"
            }
    }

    private fun setupClickListeners() {
        binding.editProfileButton.setOnClickListener {
            // TODO: Implement profile editing
            Toast.makeText(requireContext(), "Profile editing coming soon", Toast.LENGTH_SHORT).show()
        }

        binding.logoutButton.setOnClickListener {
            auth.signOut()
            startActivity(Intent(requireContext(), LoginActivity::class.java))
            requireActivity().finish()
        }
    }

    // Utility function to check if profile is complete
    fun isProfileComplete(): Boolean {
        val user = auth.currentUser ?: return false
        val email = user.email
        val phone = binding.phoneTextView.text.toString()

        return !email.isNullOrEmpty() && phone != "No phone" && phone.isNotEmpty()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
