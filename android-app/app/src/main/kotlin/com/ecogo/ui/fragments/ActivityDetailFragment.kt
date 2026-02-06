package com.ecogo.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.ecogo.R
import com.ecogo.databinding.FragmentActivityDetailBinding
import com.ecogo.repository.EcoGoRepository
import com.ecogo.ui.adapters.FriendAdapter
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch

/**
 * 活动详情页面
 * 显示活动完整信息，支持参与、签到等操作
 */
class ActivityDetailFragment : Fragment() {

    private var _binding: FragmentActivityDetailBinding? = null
    private val binding get() = _binding!!
    
    private val args: ActivityDetailFragmentArgs by navArgs()
    private val repository = EcoGoRepository()
    
    private var activityId: String = ""
    private var isJoined = false
    private var activityStatus = "PUBLISHED"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentActivityDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        activityId = args.activityId
        
        setupUI()
        loadActivityDetail()
        setupAnimations()
    }
    
    private fun setupUI() {
        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }
        
        binding.btnJoin.setOnClickListener {
            if (isJoined) {
                leaveActivity()
            } else {
                joinActivity()
            }
        }
        
        binding.btnStartRoute.setOnClickListener {
            // 导航到路线规划，设置活动地点为目的地
            // TODO: 需要传递地点信息
            findNavController().navigate(R.id.routePlannerFragment)
        }
        
        binding.btnCheckIn.setOnClickListener {
            checkInActivity()
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
    
    private fun loadActivityDetail() {
        viewLifecycleOwner.lifecycleScope.launch {
            val result = repository.getActivityById(activityId)
            result.onSuccess { activity ->
                // 显示活动信息
                binding.textTitle.text = activity.title
                binding.textDescription.text = activity.description
                
                // 活动类型和状态
                binding.textType.text = when (activity.type) {
                    "ONLINE" -> "线上活动"
                    "OFFLINE" -> "线下活动"
                    else -> activity.type
                }
                
                activityStatus = activity.status ?: "PUBLISHED"
                binding.textStatus.text = when (activityStatus) {
                    "DRAFT" -> "草稿"
                    "PUBLISHED" -> "已发布"
                    "ONGOING" -> "进行中"
                    "ENDED" -> "已结束"
                    else -> activity.status
                }
                
                // 时间
                binding.textStartTime.text = activity.startTime ?: "待定"
                binding.textEndTime.text = activity.endTime ?: "待定"
                
                // 奖励
                binding.textReward.text = "+${activity.rewardCredits} 积分"
                
                // 参与人数
                val currentParticipants = activity.currentParticipants
                val maxParticipants = activity.maxParticipants ?: Int.MAX_VALUE
                binding.textParticipants.text = "$currentParticipants / ${if (maxParticipants == Int.MAX_VALUE) "∞" else maxParticipants}"
                
                // 进度条
                if (maxParticipants != Int.MAX_VALUE) {
                    val progress = (currentParticipants.toFloat() / maxParticipants * 100).toInt()
                    binding.progressParticipants.progress = progress
                    binding.progressParticipants.visibility = View.VISIBLE
                } else {
                    binding.progressParticipants.visibility = View.GONE
                }
                
                // 检查用户是否已参与
                isJoined = activity.participantIds.contains("user123") // 应该从实际用户会话获取
                updateJoinButton()
                
                // 参与人员列表（简化版，显示前几个）
                // TODO: 实际应该加载参与用户的详细信息
                // binding.recyclerParticipants.apply {
                //     layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
                //     adapter = FriendAdapter(participants, {}, {})
                // }
                
                // 更新按钮可用性
                updateButtonStates()
            }.onFailure { error: Throwable ->
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("加载失败")
                    .setMessage("无法加载活动详情: ${error.message}")
                    .setPositiveButton("确定") { _, _ ->
                        findNavController().navigateUp()
                    }
                    .show()
            }
        }
    }
    
    private fun updateJoinButton() {
        if (isJoined) {
            binding.btnJoin.text = "退出活动"
            binding.btnJoin.setIconResource(R.drawable.ic_check)
        } else {
            binding.btnJoin.text = "参加活动"
            binding.btnJoin.icon = null
        }
    }
    
    private fun updateButtonStates() {
        // 根据活动状态更新按钮可用性
        when (activityStatus) {
            "ENDED" -> {
                binding.btnJoin.isEnabled = false
                binding.btnCheckIn.isEnabled = false
                binding.btnStartRoute.isEnabled = false
            }
            "ONGOING" -> {
                binding.btnJoin.isEnabled = true
                binding.btnCheckIn.isEnabled = isJoined
                binding.btnStartRoute.isEnabled = isJoined
            }
            else -> {
                binding.btnJoin.isEnabled = true
                binding.btnCheckIn.isEnabled = false
                binding.btnStartRoute.isEnabled = isJoined
            }
        }
    }
    
    private fun joinActivity() {
        viewLifecycleOwner.lifecycleScope.launch {
            val result = repository.joinActivity(activityId, "user123")
            result.onSuccess {
                isJoined = true
                updateJoinButton()
                updateButtonStates()
                
                // 显示成功消息
                val dialog = MaterialAlertDialogBuilder(requireContext())
                    .setTitle("成功")
                    .setMessage("你已成功参加活动！")
                    .setPositiveButton("确定", null)
                    .create()
                dialog.show()
                
                // 重新加载活动详情以更新参与人数
                loadActivityDetail()
            }.onFailure { error: Throwable ->
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("参加失败")
                    .setMessage(error.message)
                    .setPositiveButton("确定", null)
                    .show()
            }
        }
    }
    
    private fun leaveActivity() {
        viewLifecycleOwner.lifecycleScope.launch {
            val result = repository.leaveActivity(activityId, "user123")
            result.onSuccess {
                isJoined = false
                updateJoinButton()
                updateButtonStates()
                
                android.widget.Toast.makeText(
                    requireContext(),
                    "已退出活动",
                    android.widget.Toast.LENGTH_SHORT
                ).show()
                
                // 重新加载活动详情以更新参与人数
                loadActivityDetail()
            }.onFailure { error: Throwable ->
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("退出失败")
                    .setMessage(error.message)
                    .setPositiveButton("确定", null)
                    .show()
            }
        }
    }
    
    private fun checkInActivity() {
        // TODO: 实现GPS位置检测，确认用户在活动地点附近
        // 这里简化处理，直接签到
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("签到成功")
            .setMessage("恭喜！你已成功签到\n获得额外奖励 +50 积分")
            .setPositiveButton("太好了") { _, _ ->
                // TODO: 调用签到API
            }
            .show()
    }
    
    private fun setupAnimations() {
        val slideUp = AnimationUtils.loadAnimation(requireContext(), R.anim.slide_up)
        binding.cardInfo.startAnimation(slideUp)
        
        val popIn = AnimationUtils.loadAnimation(requireContext(), R.anim.pop_in)
        binding.cardDetails.startAnimation(popIn)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
