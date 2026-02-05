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
            
            // 验证用户凭证
            val prefs = requireContext().getSharedPreferences("EcoGoPrefs", Context.MODE_PRIVATE)
            val savedNusnetId = prefs.getString("nusnet_id", "")
            val savedPassword = prefs.getString("password", "")
            val isRegistered = prefs.getBoolean("is_registered", false)
            
            if (!isRegistered) {
                Toast.makeText(requireContext(), "No account found. Please register first.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            // 验证输入的凭证
            if (inputNusnetId == savedNusnetId && inputPassword == savedPassword) {
                Log.d("DEBUG_LOGIN", "Login successful")
                Log.d("DEBUG_LOGIN", "Saved credentials - nusnetId: $savedNusnetId")
                Log.d("DEBUG_LOGIN", "Input credentials - nusnetId: $inputNusnetId")

                // 标记用户已登录
                prefs.edit().putBoolean("is_logged_in", true).apply()

                // 检查是否首次登录
                val isFirstLogin = prefs.getBoolean("is_first_login", false)
                Log.d("DEBUG_LOGIN", "isFirstLogin flag: $isFirstLogin")

                Toast.makeText(requireContext(), "Welcome back! 🎉", Toast.LENGTH_SHORT).show()

                try {
                    if (isFirstLogin) {
                        // 首次登录，显示引导
                        Log.d("DEBUG_LOGIN", "First login, showing onboarding")
                        findNavController().navigate(R.id.action_login_to_onboarding)
                    } else {
                        // 不是首次登录，直接进入首页
                        Log.d("DEBUG_LOGIN", "Not first login, going to home")
                        Log.d("DEBUG_LOGIN", "Attempting navigation to homeFragment")
                        findNavController().navigate(R.id.action_login_to_home)
                        Log.d("DEBUG_LOGIN", "Navigation command executed")
                    }
                } catch (e: Exception) {
                    Log.e("DEBUG_LOGIN", "Navigation FAILED: ${e.message}", e)
                    Toast.makeText(requireContext(), "❌ 导航错误: ${e.message}", Toast.LENGTH_LONG).show()
                }
            } else {
                Log.d("DEBUG_LOGIN", "Login failed - incorrect credentials")
                Log.d("DEBUG_LOGIN", "Expected nusnetId: '$savedNusnetId', got: '$inputNusnetId'")
                Log.d("DEBUG_LOGIN", "Password match: ${inputPassword == savedPassword}")
                Toast.makeText(requireContext(), "Incorrect NUSNET ID or password", Toast.LENGTH_SHORT).show()
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
