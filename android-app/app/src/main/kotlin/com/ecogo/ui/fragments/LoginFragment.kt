package com.ecogo.ui.fragments

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.ecogo.R
import androidx.lifecycle.lifecycleScope
import com.ecogo.api.MobileLoginRequest
import com.ecogo.api.RetrofitClient
import com.ecogo.auth.TokenManager
import kotlinx.coroutines.launch
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
        
        Log.d("DEBUG_LOGIN", "LoginFragment onViewCreated")
        
        binding.buttonSignIn.setOnClickListener {
            val inputNusnetId = binding.editNusnetId.text.toString()
            val inputPassword = binding.editPassword.text.toString()
            
            Log.d("DEBUG_LOGIN", "SignIn button clicked")
            
            if (inputNusnetId.isEmpty() || inputPassword.isEmpty()) {
                Toast.makeText(requireContext(), "Please enter NUSNET ID and password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            // 测试账号：用户名123，密码123
            if (inputNusnetId == "123" && inputPassword == "123") {
                Log.d("DEBUG_LOGIN", "Test account login successful")
                
                val prefs = requireContext().getSharedPreferences("EcoGoPrefs", Context.MODE_PRIVATE)
                // 标记用户已登录
                prefs.edit().putBoolean("is_logged_in", true).apply()
                
                Toast.makeText(requireContext(), "Test Account Login Successful! 🎉", Toast.LENGTH_SHORT).show()
                
                try {
                    Log.d("DEBUG_LOGIN", "Test account, going to home")
                    findNavController().navigate(R.id.action_login_to_home)
                } catch (e: Exception) {
                    Log.e("DEBUG_LOGIN", "Navigation FAILED: ${e.message}", e)
                    Toast.makeText(requireContext(), "❌ Navigation error: ${e.message}", Toast.LENGTH_LONG).show()
                }
                return@setOnClickListener
            }
            
            // 验证输入的凭证
            lifecycleScope.launch {
                try {
                    // Show loading state (optional, can add a ProgressBar later)
                    binding.buttonSignIn.isEnabled = false
                    binding.buttonSignIn.text = "Signing in..."

                    val request = MobileLoginRequest(
                        userid = inputNusnetId,
                        password = inputPassword // Assuming backend handles string/number conversion if needed
                    )

                    val response = RetrofitClient.apiService.login(request)
                    
                    // Restore button state
                    binding.buttonSignIn.isEnabled = true
                    binding.buttonSignIn.text = "Sign In"

                    if (response.success && response.data != null) {
                        val loginData = response.data
                        val userInfo = loginData.userInfo
                        Log.d("DEBUG_LOGIN", "Login successful: ${userInfo.nickname}")
                        
                        // Save token using TokenManager
                        TokenManager.init(requireContext()) 
                        TokenManager.saveToken(
                            token = loginData.token,
                            userId = userInfo.userid,
                            username = userInfo.nickname
                        )

                        // Save VIP status to EcoGoPrefs for SplashActivity
                        val prefs = requireContext().getSharedPreferences("EcoGoPrefs", Context.MODE_PRIVATE)
                        val isVip = userInfo.vip?.active == true
                        Log.d("DEBUG_LOGIN", "VIP Status: $isVip")

                        prefs.edit().apply {
                            putBoolean("is_logged_in", true) // Keep for compatibility
                            putBoolean("is_vip", isVip)      // Crucial for SplashActivity
                            putString("nusnet_id", userInfo.userid)
                            apply()
                        }

                        Toast.makeText(requireContext(), "Login Success! Navigating...", Toast.LENGTH_SHORT).show()
                        findNavController().navigate(com.ecogo.R.id.action_login_to_home)
                    } else {
                        val msg = "Code: ${response.code}, Success: ${response.success}, Data: ${response.data}"
                        Log.e("DEBUG_LOGIN", "Login failed checks: $msg")
                        Toast.makeText(requireContext(), "Debug: $msg", Toast.LENGTH_LONG).show()
                    }

                } catch (e: Exception) {
                    Log.e("DEBUG_LOGIN", "Login error: ${e.message}", e)
                    
                    // Restore button state
                    binding.buttonSignIn.isEnabled = true
                    binding.buttonSignIn.text = "Sign In"
                    
                    val errorMessage = when(e) {
                        is retrofit2.HttpException -> {
                            when(e.code()) {
                                401 -> "Invalid credentials"
                                404 -> "User not found"
                                500 -> "Server error"
                                else -> "Network error: ${e.code()}"
                            }
                        }
                        is java.net.ConnectException -> "Cannot connect to server. Check your internet connection."
                        else -> "Error: ${e.message}"
                    }
                    
                    Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show()
                }
            }
        }
        
        binding.buttonRegister.setOnClickListener {
            Log.d("DEBUG_LOGIN", "Register button clicked - attempting navigate")
            Toast.makeText(requireContext(), "🔄 正在跳转到注册页面...", Toast.LENGTH_SHORT).show()
            try {
                findNavController().navigate(R.id.action_login_to_signup)
                Log.d("DEBUG_LOGIN", "Navigate to signup completed successfully")
                Toast.makeText(requireContext(), "✅ 导航命令已执行", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Log.e("DEBUG_LOGIN", "Navigation to signup FAILED: ${e.message}", e)
                Toast.makeText(requireContext(), "❌ 导航错误: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
