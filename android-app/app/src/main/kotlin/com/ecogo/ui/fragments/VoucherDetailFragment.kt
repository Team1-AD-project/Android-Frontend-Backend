package com.ecogo.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.ecogo.R
import com.ecogo.data.MockData
import com.ecogo.databinding.FragmentVoucherDetailBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.util.UUID

/**
 * 兑换券详情页面
 * 显示券码、二维码、使用说明等
 */
class VoucherDetailFragment : Fragment() {

    private var _binding: FragmentVoucherDetailBinding? = null
    private val binding get() = _binding!!
    
    private val args: VoucherDetailFragmentArgs by navArgs()
    private var voucherId: String = ""
    private var isRedeemed = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentVoucherDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        voucherId = args.voucherId
        
        setupUI()
        loadVoucherDetail()
        setupAnimations()
    }
    
    private fun setupUI() {
        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }
        
        binding.btnRedeem.setOnClickListener {
            redeemVoucher()
        }
        
        binding.btnUse.setOnClickListener {
            useVoucher()
        }
    }
    
    private fun loadVoucherDetail() {
        val voucher = MockData.VOUCHERS.find { it.id == voucherId }
        
        if (voucher == null) {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("错误")
                .setMessage("找不到该兑换券")
                .setPositiveButton("确定") { _, _ ->
                    findNavController().navigateUp()
                }
                .show()
            return
        }
        
        // 显示券信息
        binding.textName.text = voucher.name
        binding.textDescription.text = voucher.description
        binding.textCost.text = "${voucher.cost} 积分"
        
        // 生成券码（模拟）
        val code = generateVoucherCode()
        binding.textVoucherCode.text = code
        
        // 设置到期时间（模拟）
        val expiryDate = "2026/03/31"
        binding.textExpiry.text = "有效期至：$expiryDate"
        
        // 使用说明
        binding.textInstructions.text = """
            使用说明：
            1. 出示此券码给商家
            2. 商家确认后即可享受优惠
            3. 每张券仅限使用一次
            4. 过期后自动失效
        """.trimIndent()
        
        // 根据是否已兑换显示不同UI
        updateUIForRedeemStatus(isRedeemed)
    }
    
    private fun updateUIForRedeemStatus(redeemed: Boolean) {
        if (redeemed) {
            binding.layoutVoucherInfo.visibility = View.VISIBLE
            binding.layoutRedeemAction.visibility = View.GONE
            binding.btnUse.isEnabled = true
        } else {
            binding.layoutVoucherInfo.visibility = View.GONE
            binding.layoutRedeemAction.visibility = View.VISIBLE
            binding.btnUse.isEnabled = false
        }
    }
    
    private fun generateVoucherCode(): String {
        // 生成随机券码
        val uuid = UUID.randomUUID().toString().take(12).uppercase()
        return uuid.chunked(4).joinToString("-")
    }
    
    private fun redeemVoucher() {
        // 显示兑换确认对话框
        val voucher = MockData.VOUCHERS.find { it.id == voucherId }
        voucher?.let {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("兑换确认")
                .setMessage("确定要使用 ${it.cost} 积分兑换此券吗？")
                .setPositiveButton("确定") { _, _ ->
                    performRedeem(it.cost)
                }
                .setNegativeButton("取消", null)
                .show()
        }
    }
    
    private fun performRedeem(cost: Int) {
        // TODO: 调用API兑换
        isRedeemed = true
        updateUIForRedeemStatus(true)
        
        // 显示成功对话框
        val dialog = android.app.Dialog(requireContext())
        dialog.requestWindowFeature(android.view.Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_success)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        
        val title: TextView? = dialog.findViewById(R.id.text_title)
        val message: TextView? = dialog.findViewById(R.id.text_message)
        val button: com.google.android.material.button.MaterialButton? = dialog.findViewById(R.id.button_ok)
        
        title?.text = "兑换成功"
        message?.text = "券已添加到我的券包\n剩余积分：-$cost"
        button?.setOnClickListener {
            dialog.dismiss()
        }
        
        dialog.show()
    }
    
    private fun useVoucher() {
        // 使用券
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("确认使用")
            .setMessage("确定要使用此券吗？使用后将无法恢复。")
            .setPositiveButton("确定使用") { _, _ ->
                // TODO: 调用API标记为已使用
                
                android.widget.Toast.makeText(
                    requireContext(),
                    "券已使用",
                    android.widget.Toast.LENGTH_SHORT
                ).show()
                
                findNavController().navigateUp()
            }
            .setNegativeButton("取消", null)
            .show()
    }
    
    private fun setupAnimations() {
        val popIn = AnimationUtils.loadAnimation(requireContext(), R.anim.pop_in)
        binding.cardVoucher.startAnimation(popIn)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
