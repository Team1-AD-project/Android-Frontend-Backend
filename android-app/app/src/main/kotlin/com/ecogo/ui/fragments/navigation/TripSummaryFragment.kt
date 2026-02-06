package com.ecogo.ui.fragments.navigation

import android.animation.ValueAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.ecogo.R
import com.ecogo.data.MascotEmotion
import com.ecogo.data.MascotSize
import com.ecogo.databinding.FragmentTripSummaryBinding
import com.ecogo.ui.dialogs.AchievementUnlockDialog
import com.ecogo.viewmodel.NavigationViewModel

/**
 * 行程结算页面 - 奖励仪式
 * 展示行程统计、获得的积分和成就
 */
class TripSummaryFragment : Fragment() {

    private var _binding: FragmentTripSummaryBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var viewModel: NavigationViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTripSummaryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        viewModel = ViewModelProvider(requireActivity())[NavigationViewModel::class.java]
        
        setupMascot()
        setupUI()
        displaySummary()
        setupAnimations()
    }
    
    private fun setupMascot() {
        binding.mascotCelebrate.apply {
            mascotSize = MascotSize.LARGE
            setEmotion(MascotEmotion.CELEBRATING)
            // 播放庆祝动画
            celebrateAnimation()
            
            val spin = AnimationUtils.loadAnimation(requireContext(), R.anim.spin)
            startAnimation(spin)
        }
    }
    
    private fun setupUI() {
        binding.btnClose.setOnClickListener {
            findNavController().navigateUp()
        }
        
        binding.btnViewLeaderboard.setOnClickListener {
            findNavController().navigate(R.id.communityFragment)
        }
        
        binding.btnRedeem.setOnClickListener {
            findNavController().navigate(R.id.voucherFragment)
        }
        
        binding.btnAgain.setOnClickListener {
            // 返回到路线规划
            findNavController().navigate(R.id.routePlannerFragment)
        }
        
        binding.btnShare.setOnClickListener {
            // TODO: 分享功能（将在阶段三实现）
            // 暂时显示提示
            android.widget.Toast.makeText(
                requireContext(),
                "分享功能将在后续版本实现",
                android.widget.Toast.LENGTH_SHORT
            ).show()
        }
    }
    
    private fun displaySummary() {
        viewModel.currentTrip.observe(viewLifecycleOwner) { trip ->
            trip?.let {
                // 只在行程完成时显示统计
                if (it.status != com.ecogo.data.TripStatus.COMPLETED) return@observe
                
                // 显示行程统计
                binding.textDistance.text = String.format("%.2f", it.actualDistance)
                binding.textDuration.text = formatDuration(it.endTime!! - it.startTime)
                binding.textCarbonSaved.text = String.format("%.0f", it.actualCarbonSaved)
                
                // 动画显示积分
                animatePointsIncrease(0, it.pointsEarned)
                
                // 如果有成就解锁
                if (it.achievementUnlocked != null) {
                    showAchievementDialog(it.achievementUnlocked!!)
                }
                
                // 计算环保等级
                val rating = calculateEcoRating(it.actualCarbonSaved)
                binding.textEcoRating.text = rating
                updateRatingColor(rating)
            }
        }
    }
    
    private fun setupAnimations() {
        // 逐个展示统计卡片
        val popIn = AnimationUtils.loadAnimation(requireContext(), R.anim.pop_in)
        
        binding.root.postDelayed({
            binding.cardDistance.visibility = View.VISIBLE
            binding.cardDistance.startAnimation(popIn)
        }, 200)
        
        binding.root.postDelayed({
            binding.cardDuration.visibility = View.VISIBLE
            binding.cardDuration.startAnimation(popIn)
        }, 400)
        
        binding.root.postDelayed({
            binding.cardCarbon.visibility = View.VISIBLE
            binding.cardCarbon.startAnimation(popIn)
        }, 600)
        
        binding.root.postDelayed({
            binding.cardPoints.visibility = View.VISIBLE
            binding.cardPoints.startAnimation(popIn)
        }, 800)
    }
    
    private fun animatePointsIncrease(from: Int, to: Int) {
        val animator = ValueAnimator.ofInt(from, to)
        animator.duration = 1500
        animator.addUpdateListener { animation ->
            val value = animation.animatedValue as Int
            binding.textPoints.text = "+$value"
        }
        animator.start()
    }
    
    private fun showAchievementDialog(achievementId: String) {
        // 延迟显示成就解锁对话框
        binding.root.postDelayed({
            val achievement = com.ecogo.data.MockData.ACHIEVEMENTS.find { it.id == achievementId }
            achievement?.let {
                val dialog = AchievementUnlockDialog(
                    requireContext(),
                    it,
                    onDismiss = {}
                )
                dialog.show()
            }
        }, 1500)
    }
    
    private fun formatDuration(durationMillis: Long): String {
        val minutes = (durationMillis / 1000 / 60).toInt()
        val seconds = ((durationMillis / 1000) % 60).toInt()
        return if (minutes > 0) {
            "$minutes"
        } else {
            "< 1"
        }
    }
    
    private fun calculateEcoRating(carbonSaved: Double): String {
        return when {
            carbonSaved >= 500 -> "A+"
            carbonSaved >= 300 -> "A"
            carbonSaved >= 200 -> "B+"
            carbonSaved >= 100 -> "B"
            else -> "C"
        }
    }
    
    private fun updateRatingColor(rating: String) {
        val color = when (rating) {
            "A+", "A" -> R.color.primary
            "B+", "B" -> R.color.secondary
            else -> R.color.warning
        }
        binding.textEcoRating.setTextColor(requireContext().getColor(color))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
