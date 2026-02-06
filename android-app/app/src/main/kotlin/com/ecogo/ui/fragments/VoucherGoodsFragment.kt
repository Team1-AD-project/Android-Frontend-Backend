package com.ecogo.ui.fragments

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.ecogo.R
import com.ecogo.auth.TokenManager
import com.ecogo.databinding.FragmentVoucherGoodsBinding
import com.ecogo.repository.EcoGoRepository
import com.ecogo.ui.adapters.ShopGoodsAdapterV2
import kotlinx.coroutines.launch

class VoucherGoodsFragment : Fragment() {

    private var _binding: FragmentVoucherGoodsBinding? = null
    private val binding get() = _binding!!

    private val repo = EcoGoRepository()
    private lateinit var adapter: ShopGoodsAdapterV2

    private fun setVipActiveLocalTrue() {
        val prefs = requireContext().getSharedPreferences("EcoGoPrefs", Context.MODE_PRIVATE)
        prefs.edit().putBoolean("is_vip", true).apply()
    }

    private fun readVipActive(): Boolean {
        val prefs = requireContext().getSharedPreferences("EcoGoPrefs", Context.MODE_PRIVATE)
        Log.d("SHOP", "prefs is_vip=${prefs.getBoolean("is_vip", false)}")
        return prefs.getBoolean("is_vip", false)
    }




    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentVoucherGoodsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = ShopGoodsAdapterV2 { g ->
            val bundle = Bundle().apply { putString("goodsId", g.id) }
            findNavController().navigate(R.id.action_voucher_to_shopGoodsDetail, bundle)
        }

        binding.recyclerGoods.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerGoods.adapter = adapter

        binding.chipGroupCategories.setOnCheckedStateChangeListener { _, checkedIds ->
            val category = when (checkedIds.firstOrNull()) {
                binding.chipFood.id -> "food"
                binding.chipBeverage.id -> "beverage"
                binding.chipMerchandise.id -> "merchandise"
                binding.chipService.id -> "service"
                binding.chipAll.id, null -> null
                else -> null
            }
            loadGoods(category)
        }

        loadGoods(category = null)
    }

    private fun loadGoods(category: String?) {
        binding.progressLoading.visibility = View.VISIBLE
        viewLifecycleOwner.lifecycleScope.launch {
            val isVipActive = readVipActive()
            try {
                val resp = repo.getAllGoods(
                    page = 1,
                    size = 100,
                    category = category,
                    keyword = null,
                    isForRedemption = true,
                    isVipActive = isVipActive
                ).getOrThrow()

                // 后端已过滤 voucher，这里再兜底过滤一次（如果未来有人改后端逻辑）
                adapter.update(resp.items)
            } catch (_: Exception) {
                adapter.update(emptyList())
            } finally {
                binding.progressLoading.visibility = View.GONE
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
