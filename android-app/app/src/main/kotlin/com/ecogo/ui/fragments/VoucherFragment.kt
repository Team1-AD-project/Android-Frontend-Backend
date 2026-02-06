package com.ecogo.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.ecogo.databinding.FragmentVoucherBinding
import com.ecogo.repository.EcoGoRepository
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.launch

class VoucherFragment : Fragment() {
    
    private var _binding: FragmentVoucherBinding? = null
    private val binding get() = _binding!!
    private val repository = EcoGoRepository()
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentVoucherBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupUserPoints()
        setupViewPager()
    }
    
    private fun setupUserPoints() {
        // 从Repository加载用户积分
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                // TODO: Fetch user points from API
                val userPoints = 1250 // Sample data
                binding.textUserPoints.text = String.format("%,d", userPoints)
            } catch (e: Exception) {
                binding.textUserPoints.text = "0"
            }
        }
    }
    
    private fun setupViewPager() {
        // 设置ViewPager适配器
        val adapter = VoucherPagerAdapter(this)
        binding.viewPager.adapter = adapter
        
        // 连接TabLayout和ViewPager2
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "🛍️ Shop"
                1 -> "🎫 Coupons"
                else -> ""
            }
        }.attach()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    
    /**
     * ViewPager2 适配器
     */
    private inner class VoucherPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
        override fun getItemCount(): Int = 2
        
        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> VoucherGoodsFragment()
                1 -> VoucherCouponsFragment()
                else -> VoucherGoodsFragment()
            }
        }
    }
}
