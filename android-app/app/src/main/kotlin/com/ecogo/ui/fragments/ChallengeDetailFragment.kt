package com.ecogo.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.ecogo.R
import com.ecogo.data.MascotEmotion
import com.ecogo.data.MascotSize
import com.ecogo.data.MockData
import com.ecogo.databinding.FragmentChallengeDetailBinding
import com.ecogo.ui.adapters.LeaderboardAdapter
import com.ecogo.ui.dialogs.AchievementUnlockDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder

/**
 * 挑战详情页面
 * 显示挑战规则、进度、排行榜等
 */
class ChallengeDetailFragment : Fragment() {

    private var _binding: FragmentChallengeDetailBinding? = null
    private val binding get() = _binding!!
    
    private val args: ChallengeDetailFragmentArgs by navArgs()
    private lateinit var leaderboardAdapter: LeaderboardAdapter
    
    private var challengeId: String = ""
    private var isAccepted = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChallengeDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        challengeId = args.challengeId
        
        setupMascot()
        setupRecyclerView()
        setupUI()
        loadChallengeDetail()
        setupAnimations()
    }
    
    private fun setupMascot() {
        binding.mascotCheer.apply {
            mascotSize = MascotSize.MEDIUM
            setEmotion(MascotEmotion.HAPPY)
        }
    }
    
    private fun setupRecyclerView() {
        leaderboardAdapter = LeaderboardAdapter(emptyList())
        binding.recyclerLeaderboard.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = leaderboardAdapter
        }
    }
    
    private fun setupUI() {
        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }
        
        binding.btnAccept.setOnClickListener {
            acceptChallenge()
        }
        
        binding.btnShare.setOnClickListener {
            // TODO: 分享功能（将在阶段三实现）
            android.widget.Toast.makeText(
                requireContext(),
                "分享功能将在后续版本实现",
                android.widget.Toast.LENGTH_SHORT
            ).show()
        }
    }
    
    private fun loadChallengeDetail() {
        val challenge = MockData.CHALLENGES.find { it.id == challengeId }
        
        if (challenge == null) {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("错误")
                .setMessage("找不到该挑战")
                .setPositiveButton("确定") { _, _ ->
                    findNavController().navigateUp()
                }
                .show()
            return
        }
        
        // 显示挑战信息
        binding.textTitle.text = challenge.title
        binding.textIcon.text = challenge.icon
        binding.textDescription.text = challenge.description
        
        // 类型
        binding.textType.text = when (challenge.type) {
            "INDIVIDUAL" -> "个人挑战"
            "TEAM" -> "组队挑战"
            "FACULTY" -> "学院挑战"
            else -> challenge.type
        }
        
        // 进度
        val progressPercent = if (challenge.target > 0) {
            ((challenge.current.toFloat() / challenge.target) * 100).toInt()
        } else 0
        
        binding.progressChallenge.max = challenge.target
        binding.progressChallenge.progress = challenge.current
        binding.textProgress.text = "${challenge.current} / ${challenge.target}"
        binding.textProgressPercent.text = "$progressPercent%"
        
        // 奖励
        binding.textReward.text = "+${challenge.reward} 积分"
        if (challenge.badge != null) {
            binding.textBadgeReward.visibility = View.VISIBLE
            binding.textBadgeReward.text = "解锁成就徽章"
        } else {
            binding.textBadgeReward.visibility = View.GONE
        }
        
        // 时间
        binding.textEndTime.text = challenge.endTime.substring(0, 10).replace("-", "/")
        
        // 参与人数
        binding.textParticipants.text = "${challenge.participants} 人"
        
        // 排行榜（简化版，使用topUsers）
        if (challenge.topUsers.isNotEmpty()) {
            val rankings = challenge.topUsers.mapIndexed { index, user ->
                com.ecogo.data.Ranking(
                    id = user.id,
                    period = "current",
                    rank = index + 1,
                    userId = user.id,
                    nickname = user.username,
                    steps = user.points,
                    isVip = index < 3
                )
            }
            leaderboardAdapter = LeaderboardAdapter(rankings)
            binding.recyclerLeaderboard.adapter = leaderboardAdapter
            binding.recyclerLeaderboard.visibility = View.VISIBLE
            binding.emptyLeaderboard.visibility = View.GONE
        } else {
            binding.recyclerLeaderboard.visibility = View.GONE
            binding.emptyLeaderboard.visibility = View.VISIBLE
        }
        
        // 更新按钮状态
        updateButtonState(challenge.status)
    }
    
    private fun updateButtonState(status: String) {
        when (status) {
            "ACTIVE" -> {
                if (isAccepted) {
                    binding.btnAccept.text = "继续努力"
                    binding.btnAccept.setIconResource(R.drawable.ic_check)
                } else {
                    binding.btnAccept.text = "接受挑战"
                    binding.btnAccept.icon = null
                }
                binding.btnAccept.isEnabled = true
            }
            "COMPLETED" -> {
                binding.btnAccept.text = "挑战已完成"
                binding.btnAccept.isEnabled = false
            }
            "EXPIRED" -> {
                binding.btnAccept.text = "挑战已过期"
                binding.btnAccept.isEnabled = false
            }
        }
    }
    
    private fun acceptChallenge() {
        if (!isAccepted) {
            isAccepted = true
            binding.btnAccept.text = "继续努力"
            binding.btnAccept.setIconResource(R.drawable.ic_check)
            
            // 小狮子跳跃动画
            binding.mascotCheer.celebrateAnimation()
            
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("成功")
                .setMessage("你已接受挑战！加油完成吧！")
                .setPositiveButton("好的", null)
                .show()
        } else {
            // 如果已经完成了挑战
            val challenge = MockData.CHALLENGES.find { it.id == challengeId }
            if (challenge != null && challenge.current >= challenge.target) {
                // 显示成就解锁对话框
                if (challenge.badge != null) {
                    val achievement = MockData.ACHIEVEMENTS.find { it.id == challenge.badge }
                    achievement?.let {
                        val dialog = AchievementUnlockDialog(
                            requireContext(),
                            it,
                            onDismiss = {}
                        )
                        dialog.show()
                    }
                }
            }
        }
    }
    
    private fun setupAnimations() {
        val popIn = AnimationUtils.loadAnimation(requireContext(), R.anim.pop_in)
        binding.cardInfo.startAnimation(popIn)
        
        val slideUp = AnimationUtils.loadAnimation(requireContext(), R.anim.slide_up)
        binding.cardProgress.startAnimation(slideUp)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
