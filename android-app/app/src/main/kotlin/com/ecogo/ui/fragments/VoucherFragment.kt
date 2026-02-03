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
import com.ecogo.databinding.FragmentVoucherBinding
import com.ecogo.ui.adapters.VoucherAdapter
import com.ecogo.repository.EcoGoRepository
import com.google.android.material.tabs.TabLayout
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
        
        setupTabs()
        setupRecyclerView()
        loadVouchers()
    }
    
    private fun setupTabs() {
        // Tab layout不存在于当前布局中，暂时移除
        // TODO: 如果需要标签页功能，请在布局中添加TabLayout
    }
    
    private fun setupRecyclerView() {
        binding.recyclerVouchers.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = VoucherAdapter(emptyList()) { voucher ->
                // 点击券跳转到详情页
                val action = VoucherFragmentDirections
                    .actionVoucherToVoucherDetail(voucherId = voucher.id)
                findNavController().navigate(action)
            }
        }
    }

    private fun loadVouchers(tab: String = "marketplace") {
        viewLifecycleOwner.lifecycleScope.launch {
            val allVouchers = repository.getVouchers().getOrElse { MockData.VOUCHERS }
            
            // 根据Tab过滤（简化版，实际应该从API获取不同数据）
            val vouchers = when (tab) {
                "my_vouchers" -> {
                    // 模拟"我的券包"，只显示部分
                    allVouchers.filter { voucher -> voucher.available }
                }
                else -> allVouchers
            }
            
            (binding.recyclerVouchers.adapter as? VoucherAdapter)?.updateVouchers(vouchers)
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
