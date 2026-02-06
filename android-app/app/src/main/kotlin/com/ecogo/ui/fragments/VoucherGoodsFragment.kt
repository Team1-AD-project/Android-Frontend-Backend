package com.ecogo.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.ecogo.R
import com.ecogo.data.MockData
import com.ecogo.data.ShopItem
import com.ecogo.databinding.FragmentVoucherGoodsBinding
import com.ecogo.repository.EcoGoRepository
import com.ecogo.ui.adapters.GoodsAdapter
import kotlinx.coroutines.launch

class VoucherGoodsFragment : Fragment() {
    
    private var _binding: FragmentVoucherGoodsBinding? = null
    private val binding get() = _binding!!
    private val repository = EcoGoRepository()
    private lateinit var goodsAdapter: GoodsAdapter
    private var allGoods: List<ShopItem> = emptyList()
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentVoucherGoodsBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupRecyclerView()
        setupCategoryFilters()
        loadGoods()
    }
    
    private fun setupRecyclerView() {
        goodsAdapter = GoodsAdapter { shopItem ->
            // Navigate to item detail page on click
            try {
                val bundle = Bundle().apply {
                    putString("itemId", shopItem.id)
                }
                findNavController().navigate(com.ecogo.R.id.action_voucher_to_itemDetail, bundle)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        
        binding.recyclerGoods.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = goodsAdapter
        }
    }
    
    private fun setupCategoryFilters() {
        binding.chipGroupCategories.setOnCheckedStateChangeListener { group, checkedIds ->
            if (checkedIds.isEmpty()) return@setOnCheckedStateChangeListener
            
            val selectedCategory = when (checkedIds.first()) {
                binding.chipFood.id -> "food"
                binding.chipBeverage.id -> "beverage"
                binding.chipMerchandise.id -> "merchandise"
                binding.chipService.id -> "service"
                else -> "all"
            }
            
            filterGoods(selectedCategory)
        }
    }
    
    private fun loadGoods() {
        binding.progressLoading.visibility = View.VISIBLE
        
        viewLifecycleOwner.lifecycleScope.launch {
            try {
            // Load goods data from Repository
            // Using MockData as example, should fetch from API in production
                allGoods = MockData.SHOP_ITEMS.filter { !it.owned }
                
                goodsAdapter.updateGoods(allGoods)
                binding.progressLoading.visibility = View.GONE
            } catch (e: Exception) {
                binding.progressLoading.visibility = View.GONE
                e.printStackTrace()
            }
        }
    }
    
    private fun filterGoods(category: String) {
        val filteredGoods = if (category == "all") {
            allGoods
        } else {
            // Filter goods by category
            allGoods.filter { 
                when (category) {
                    "food" -> it.name.contains("food", ignoreCase = true) || 
                             it.name.contains("canteen", ignoreCase = true)
                    "beverage" -> it.name.contains("coffee", ignoreCase = true) || 
                                 it.name.contains("tea", ignoreCase = true) ||
                                 it.name.contains("drink", ignoreCase = true)
                    "merchandise" -> it.type == "badge"
                    "service" -> false // No service items yet
                    else -> true
                }
            }
        }
        
        goodsAdapter.updateGoods(filteredGoods)
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
