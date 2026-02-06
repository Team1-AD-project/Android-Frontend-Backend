package com.ecogo.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.ecogo.R
import com.ecogo.databinding.FragmentOnboardingBinding
import com.ecogo.ui.adapters.OnboardingAdapter

class OnboardingFragment : Fragment() {
    
    private var _binding: FragmentOnboardingBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: OnboardingAdapter
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOnboardingBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        Log.d("DEBUG_ONBOARDING", "OnboardingFragment onViewCreated")
        
        adapter = OnboardingAdapter()
        binding.viewPager.adapter = adapter
        
        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                updateProgressDots(position)
                if (position == 4) {  // 改为4，因为现在有5页（0-4）
                    binding.buttonNext.text = getString(R.string.onboarding_get_started)
                    binding.buttonNext.icon = null
                } else {
                    binding.buttonNext.text = getString(R.string.onboarding_next)
                    binding.buttonNext.setIconResource(R.drawable.ic_chevron_right)
                }
            }
        })
        
        updateProgressDots(0)
        
        binding.buttonNext.setOnClickListener {
            val currentItem = binding.viewPager.currentItem
            if (currentItem < 4) {  // 改为4
                binding.viewPager.setCurrentItem(currentItem + 1, true)
            } else {
                Log.d("DEBUG_ONBOARDING", "Next button clicked - attempting navigate to home")
                Toast.makeText(requireContext(), "🔄 正在跳转到主页...", Toast.LENGTH_SHORT).show()
                try {
                    findNavController().navigate(R.id.action_onboarding_to_home)
                    Log.d("DEBUG_ONBOARDING", "Navigate to home completed successfully")
                    Toast.makeText(requireContext(), "✅ 导航命令已执行", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Log.e("DEBUG_ONBOARDING", "Navigation to home FAILED: ${e.message}", e)
                    Toast.makeText(requireContext(), "❌ 导航错误: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
        
        binding.textSkip.setOnClickListener {
            Log.d("DEBUG_ONBOARDING", "Skip button clicked - attempting navigate to home")
            try {
                findNavController().navigate(R.id.action_onboarding_to_home)
                Log.d("DEBUG_ONBOARDING", "Navigate to home completed from skip")
            } catch (e: Exception) {
                Log.e("DEBUG_ONBOARDING", "Navigation to home from skip FAILED: ${e.message}", e)
                Toast.makeText(requireContext(), "导航错误: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
    
    private fun updateProgressDots(position: Int) {
        // 注意：布局中只有3个点，但我们有5页内容
        // 这里可以保持原样，或者动态更新。为了简单起见，先保持原样
        val dots = listOf(binding.dot1, binding.dot2, binding.dot3)
        
        // 将5页映射到3个点：页0-1→点0，页2→点1，页3-4→点2
        val dotPosition = when (position) {
            0, 1 -> 0
            2 -> 1
            3, 4 -> 2
            else -> 0
        }
        
        dots.forEachIndexed { index, dot ->
            dot.setBackgroundResource(
                if (index == dotPosition) R.drawable.progress_dot_active else R.drawable.progress_dot
            )
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
