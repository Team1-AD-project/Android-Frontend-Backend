package com.ecogo.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
        
        adapter = OnboardingAdapter()
        binding.viewPager.adapter = adapter
        
        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                updateProgressDots(position)
                if (position == 2) {
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
            if (currentItem < 2) {
                binding.viewPager.setCurrentItem(currentItem + 1, true)
            } else {
                findNavController().navigate(R.id.action_onboarding_to_home)
            }
        }
        
        binding.textSkip.setOnClickListener {
            findNavController().navigate(R.id.action_onboarding_to_home)
        }
    }
    
    private fun updateProgressDots(position: Int) {
        val dots = listOf(binding.dot1, binding.dot2, binding.dot3)
        val activeColor = ContextCompat.getColor(requireContext(), R.color.primary)
        val inactiveColor = ContextCompat.getColor(requireContext(), android.R.color.darker_gray)
        
        dots.forEachIndexed { index, dot ->
            dot.setBackgroundResource(
                if (index == position) R.drawable.progress_dot_active else R.drawable.progress_dot
            )
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
