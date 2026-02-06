package com.ecogo.ui.fragments

import com.ecogo.R
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
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

        setupViewPager()

        binding.cardHistory.setOnClickListener {
            findNavController().navigate(R.id.action_voucherFragment_to_orderHistoryFragment)
        }

        // ✅ 首次进入就拉一次
        loadUserPoints()
    }

    override fun onResume() {
        super.onResume()
        // ✅ 从 History / Redeem 返回时也刷新一次
        loadUserPoints()
    }

    private fun loadUserPoints() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val data = repository.getCurrentPoints().getOrThrow()
                val userPoints = data.currentPoints
                binding.textUserPoints.text = String.format("%,d", userPoints)
            } catch (_: Exception) {
                binding.textUserPoints.text = "0"
            }
        }
    }

    private fun setupViewPager() {
        val adapter = VoucherPagerAdapter(this)
        binding.viewPager.adapter = adapter

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
