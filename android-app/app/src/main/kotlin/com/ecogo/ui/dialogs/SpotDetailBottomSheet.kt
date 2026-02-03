package com.ecogo.ui.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.ecogo.R
import com.ecogo.data.GreenSpot
import com.ecogo.databinding.BottomSheetSpotDetailBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

/**
 * 绿色点位详情BottomSheet
 * 显示点位信息和操作
 */
class SpotDetailBottomSheet(
    private val spot: GreenSpot,
    private val onWalkThere: () -> Unit,
    private val onCollect: () -> Unit
) : BottomSheetDialogFragment() {

    private var _binding: BottomSheetSpotDetailBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetSpotDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupUI()
    }
    
    private fun setupUI() {
        // 显示点位信息
        binding.textName.text = spot.name
        binding.textDescription.text = spot.description
        binding.textReward.text = "+${spot.reward} 积分"
        
        // 类型图标
        binding.textIcon.text = when (spot.type) {
            "TREE" -> "🌳"
            "RECYCLE_BIN" -> "♻️"
            "PARK" -> "🌲"
            "LANDMARK" -> "🏛️"
            else -> "📍"
        }
        
        // 类型标签
        binding.textType.text = when (spot.type) {
            "TREE" -> "绿色植物"
            "RECYCLE_BIN" -> "回收站"
            "PARK" -> "公园"
            "LANDMARK" -> "地标"
            else -> spot.type
        }
        
        // 按钮状态
        if (spot.collected) {
            binding.btnCollect.text = "已领取"
            binding.btnCollect.isEnabled = false
            binding.btnWalkThere.visibility = View.GONE
        } else {
            binding.btnCollect.text = "立即领取"
            binding.btnCollect.isEnabled = true
            binding.btnWalkThere.visibility = View.VISIBLE
        }
        
        // 按钮点击
        binding.btnWalkThere.setOnClickListener {
            dismiss()
            onWalkThere()
        }
        
        binding.btnCollect.setOnClickListener {
            if (!spot.collected) {
                dismiss()
                onCollect()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
