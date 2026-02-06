package com.ecogo.ui.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.ecogo.data.MockData
import com.ecogo.data.UserVoucher
import com.ecogo.data.Voucher
import com.ecogo.databinding.FragmentVoucherCouponsBinding
import com.ecogo.repository.EcoGoRepository
import com.ecogo.ui.adapters.VoucherAdapter
import kotlinx.coroutines.launch
import com.ecogo.auth.TokenManager
import com.ecogo.utils.DateFormatters

class VoucherCouponsFragment : Fragment() {

    private val currentUserId: String
        get() = requireNotNull(TokenManager.getUserId()) {
            "User not logged in or userId missing"
        }

    private fun readVipActive(): Boolean {
        val prefs = requireContext().getSharedPreferences("EcoGoPrefs", Context.MODE_PRIVATE)
        return prefs.getBoolean("is_vip", false)
    }



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

        observeRedeemResult()
        setupRecyclerView()
        setupFilters()
        loadVouchers()
    }
    
    private fun setupRecyclerView() {
        voucherAdapter = VoucherAdapter(emptyList()) { voucher ->
            try {
                val goodsId = voucher.goodsId ?: voucher.id
                val userVoucherId = voucher.userVoucherId // Marketplace 是 null；My/Used/Expired 才有值

                val action = VoucherFragmentDirections
                    .actionVoucherToVoucherDetail(
                        goodsId = goodsId,
                        userVoucherId = userVoucherId
                    )

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
            val isVipActive = readVipActive()
            try {
                // Load voucher data from Repository
                val result = repository.getVouchers(isVipActive = isVipActive)

                allVouchers = result.getOrThrow()


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

    private fun observeRedeemResult() {
        findNavController().currentBackStackEntry
            ?.savedStateHandle
            ?.getLiveData<Boolean>("voucher_redeemed")
            ?.observe(viewLifecycleOwner) { redeemed ->
                if (redeemed == true) {
                    // 清掉 flag，避免重复触发
                    findNavController().currentBackStackEntry?.savedStateHandle?.set("voucher_redeemed", false)

                    // 自动切到 My Coupons 并刷新
                    binding.chipMyVouchers.isChecked = true
                    filterVouchers("my_vouchers")
                }
            }
    }


    private fun filterVouchers(filter: String) {
        binding.progressLoading.visibility = View.VISIBLE

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val listToShow: List<Voucher> = when (filter) {
                    "marketplace" -> {
                        // 继续用 Marketplace 已加载的 allVouchers（或你也可以重新拉一次）
                        allVouchers
                    }

                    "my_vouchers" -> {
                        val uv = repository.getUserVouchers(currentUserId, "my").getOrElse { emptyList() }
                        uv.map { it.toVoucherUi() }
                    }

                    "used" -> {
                        val uv = repository.getUserVouchers(currentUserId, "used").getOrElse { emptyList() }
                        uv.map { it.toVoucherUi() }
                    }

                    "expired" -> {
                        val uv = repository.getUserVouchers(currentUserId, "expired").getOrElse { emptyList() }
                        uv.map { it.toVoucherUi() }
                    }

                    else -> allVouchers
                }

                voucherAdapter.updateVouchers(listToShow)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                if (_binding != null) binding.progressLoading.visibility = View.GONE
            }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun UserVoucher.toVoucherUi(): Voucher {
        val nice = DateFormatters.formatExpiry(this.expiresAt)
        return Voucher(
            id = this.goodsId,                  // 详情页默认用 goodsId 展示也行
            goodsId = this.goodsId,
            userVoucherId = this.id,            // ✅ 核销必须用它
            status = this.status,
            name = this.voucherName,
            description = if (nice.isNotBlank()) "Valid until: $nice" else "",
            cost = 0,
            available = this.status == "ACTIVE",
            imageUrl = this.imageUrl
        )
    }


}
