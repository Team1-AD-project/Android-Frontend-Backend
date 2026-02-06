package com.ecogo.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.ecogo.data.MockData
import com.ecogo.data.Voucher
import com.ecogo.databinding.FragmentVoucherCouponsBinding
import com.ecogo.repository.EcoGoRepository
import com.ecogo.ui.adapters.VoucherAdapter
import kotlinx.coroutines.launch

class VoucherCouponsFragment : Fragment() {
    
    private var _binding: FragmentVoucherCouponsBinding? = null
    private val binding get() = _binding!!
    private val repository = EcoGoRepository()
    private lateinit var voucherAdapter: VoucherAdapter
    private var allVouchers: List<Voucher> = emptyList()
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentVoucherCouponsBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupRecyclerView()
        setupFilters()
        loadVouchers()
    }
    
    private fun setupRecyclerView() {
        voucherAdapter = VoucherAdapter(emptyList()) { voucher ->
            // 点击优惠券跳转到详情页
            try {
                val action = VoucherFragmentDirections
                    .actionVoucherToVoucherDetail(voucherId = voucher.id)
                findNavController().navigate(action)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        
        binding.recyclerCoupons.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = voucherAdapter
        }
    }
    
    private fun setupFilters() {
        binding.chipGroupFilters.setOnCheckedStateChangeListener { group, checkedIds ->
            if (checkedIds.isEmpty()) return@setOnCheckedStateChangeListener
            
            val selectedFilter = when (checkedIds.first()) {
                binding.chipMarketplace.id -> "marketplace"
                binding.chipMyVouchers.id -> "my_vouchers"
                binding.chipUsed.id -> "used"
                binding.chipExpired.id -> "expired"
                else -> "marketplace"
            }
            
            filterVouchers(selectedFilter)
        }
    }
    
    private fun loadVouchers() {
        binding.progressLoading.visibility = View.VISIBLE
        
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                // Load voucher data from Repository
                allVouchers = repository.getVouchers().getOrElse { MockData.VOUCHERS }
                
                if (_binding != null) {
                    voucherAdapter.updateVouchers(allVouchers)
                    binding.progressLoading.visibility = View.GONE
                }
            } catch (e: Exception) {
                if (_binding != null) {
                    binding.progressLoading.visibility = View.GONE
                }
                e.printStackTrace()
            }
        }
    }
    
    private fun filterVouchers(filter: String) {
        val filteredVouchers = when (filter) {
            "marketplace" -> {
                // Marketplace: Show all available vouchers
                allVouchers.filter { it.available }
            }
            "my_vouchers" -> {
                // My Coupons: Show redeemed but unused vouchers
                // In real app, should fetch from user's coupon collection
                allVouchers.filter { it.available }.take(3) // Example: show first 3
            }
            "used" -> {
                // Used: Show used vouchers
                // In real app, should fetch from user history
                emptyList()
            }
            "expired" -> {
                // Expired: Show expired vouchers
                // In real app, should fetch from user history
                emptyList()
            }
            else -> allVouchers
        }
        
        voucherAdapter.updateVouchers(filteredVouchers)
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
