package com.ecogo.ui.fragments

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.animation.AnimationUtils
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.ecogo.R
import com.ecogo.data.MascotSize
import com.ecogo.data.MockData
import com.ecogo.data.Outfit
import com.ecogo.databinding.FragmentItemDetailBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder

/**
 * 商品详情页面
 * 支持预览试穿效果和购买
 */
class ItemDetailFragment : Fragment() {

    private var _binding: FragmentItemDetailBinding? = null
    private val binding get() = _binding!!
    
    private val args: ItemDetailFragmentArgs by navArgs()
    private var itemId: String = ""
    private var isOwned = false
    private var currentOutfit = Outfit(head = "none", face = "none", body = "shirt_nus", badge = "none")

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentItemDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        itemId = args.itemId
        
        setupUI()
        loadItemDetail()
        setupAnimations()
    }
    
    private fun setupUI() {
        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }
        
        binding.btnPurchase.setOnClickListener {
            purchaseItem()
        }
        
        binding.btnEquip.setOnClickListener {
            equipItem()
        }
        
        binding.btnTryOn.setOnClickListener {
            togglePreview()
        }
    }
    
    private fun loadItemDetail() {
        val item = MockData.SHOP_ITEMS.find { it.id == itemId }
        
        if (item == null) {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("错误")
                .setMessage("找不到该商品")
                .setPositiveButton("确定") { _, _ ->
                    findNavController().navigateUp()
                }
                .show()
            return
        }
        
        // 显示商品信息
        binding.textName.text = item.name
        binding.textType.text = when (item.type) {
            "head" -> "头饰"
            "face" -> "面部"
            "body" -> "身体"
            "badge" -> "徽章"
            else -> item.type
        }
        binding.textCost.text = "${item.cost} 积分"
        
        // 设置小狮子显示当前装备
        binding.mascotPreview.apply {
            mascotSize = MascotSize.XLARGE
            outfit = currentOutfit
        }
        
        // 检查是否已拥有
        isOwned = item.owned
        updateButtonStates()
    }
    
    private fun updateButtonStates() {
        if (isOwned) {
            binding.btnPurchase.visibility = View.GONE
            binding.btnEquip.visibility = View.VISIBLE
            binding.btnTryOn.text = "预览装备"
        } else {
            binding.btnPurchase.visibility = View.VISIBLE
            binding.btnEquip.visibility = View.GONE
            binding.btnTryOn.text = "试穿预览"
        }
    }
    
    private fun togglePreview() {
        val item = MockData.SHOP_ITEMS.find { it.id == itemId } ?: return
        
        // 切换预览（添加/移除该装备）
        val newOutfit = when (item.type) {
            "head" -> currentOutfit.copy(head = if (currentOutfit.head == item.id) "none" else item.id)
            "face" -> currentOutfit.copy(face = if (currentOutfit.face == item.id) "none" else item.id)
            "body" -> currentOutfit.copy(body = if (currentOutfit.body == item.id) "none" else item.id)
            "badge" -> currentOutfit.copy(badge = if (currentOutfit.badge == item.id) "none" else item.id)
            else -> currentOutfit
        }
        
        currentOutfit = newOutfit
        binding.mascotPreview.outfit = newOutfit
        
        // 跳跃动画
        binding.mascotPreview.celebrateAnimation()
        
        // 更新按钮文本
        val isPreviewing = when (item.type) {
            "head" -> currentOutfit.head == item.id
            "face" -> currentOutfit.face == item.id
            "body" -> currentOutfit.body == item.id
            "badge" -> currentOutfit.badge == item.id
            else -> false
        }
        
        binding.btnTryOn.text = if (isPreviewing) {
            if (isOwned) "取消预览" else "取消试穿"
        } else {
            if (isOwned) "预览装备" else "试穿预览"
        }
    }
    
    private fun purchaseItem() {
        val item = MockData.SHOP_ITEMS.find { it.id == itemId } ?: return
        
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("购买确认")
            .setMessage("确定要使用 ${item.cost} 积分购买 ${item.name} 吗？")
            .setPositiveButton("确定") { _, _ ->
                performPurchase(item.cost)
            }
            .setNegativeButton("取消", null)
            .show()
    }
    
    private fun performPurchase(cost: Int) {
        // TODO: 调用API购买
        isOwned = true
        updateButtonStates()
        
        // 显示购买成功对话框
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_purchase_success)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        
        val title = dialog.findViewById<TextView>(R.id.text_title)
        val message = dialog.findViewById<TextView>(R.id.text_message)
        val button = dialog.findViewById<com.google.android.material.button.MaterialButton>(R.id.button_ok)
        
        title?.text = "购买成功"
        message?.text = "已添加到你的衣橱\n剩余积分：-$cost"
        button?.setOnClickListener {
            dialog.dismiss()
        }
        
        dialog.show()
    }
    
    private fun equipItem() {
        // 装备物品（返回到Profile并应用装备）
        android.widget.Toast.makeText(
            requireContext(),
            "已装备",
            android.widget.Toast.LENGTH_SHORT
        ).show()
        
        findNavController().navigateUp()
    }
    
    private fun setupAnimations() {
        val popIn = AnimationUtils.loadAnimation(requireContext(), R.anim.pop_in)
        binding.cardItem.startAnimation(popIn)
        
        val slideUp = AnimationUtils.loadAnimation(requireContext(), R.anim.slide_up)
        binding.cardInstructions.startAnimation(slideUp)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
