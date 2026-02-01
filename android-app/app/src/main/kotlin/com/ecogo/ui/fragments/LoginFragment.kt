package com.ecogo.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.ecogo.R
import com.ecogo.databinding.FragmentLoginBinding

class LoginFragment : Fragment() {
    
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        binding.buttonSignIn.setOnClickListener {
            val nusnetId = binding.editNusnetId.text.toString()
            val password = binding.editPassword.text.toString()
            
            if (nusnetId.isNotEmpty() && password.isNotEmpty()) {
                // Navigate to onboarding for new users, or home for existing users
                findNavController().navigate(R.id.action_login_to_onboarding)
            }
        }
        
        binding.buttonRegister.setOnClickListener {
            // Navigate to signup wizard
            findNavController().navigate(R.id.action_login_to_signup)
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
