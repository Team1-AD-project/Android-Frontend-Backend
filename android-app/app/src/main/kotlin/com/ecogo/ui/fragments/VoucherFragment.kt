package com.ecogo.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.ecogo.data.MockData
import com.ecogo.databinding.FragmentVoucherBinding
import com.ecogo.ui.adapters.VoucherAdapter
import com.ecogo.repository.EcoGoRepository
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
        
        setupRecyclerView()
        loadVouchers()
    }
    
    private fun setupRecyclerView() {
        binding.recyclerVouchers.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = VoucherAdapter(emptyList())
        }
    }

    private fun loadVouchers() {
        viewLifecycleOwner.lifecycleScope.launch {
            val vouchers = repository.getVouchers().getOrElse { MockData.VOUCHERS }
            binding.recyclerVouchers.adapter = VoucherAdapter(vouchers)
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
