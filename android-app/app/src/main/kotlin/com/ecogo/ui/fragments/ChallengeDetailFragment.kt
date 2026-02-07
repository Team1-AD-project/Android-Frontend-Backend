package com.ecogo.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.ecogo.R
import com.ecogo.api.RetrofitClient
import com.ecogo.data.Challenge
import com.ecogo.data.MascotEmotion
import com.ecogo.data.MascotSize
import com.ecogo.data.UserChallengeProgress
import com.ecogo.databinding.FragmentChallengeDetailBinding
import com.ecogo.auth.TokenManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch

/**
 * Challenge Detail Page
 * Display challenge rules, progress, leaderboard, etc.
 * 从后端API获取挑战数据
 */
class ChallengeDetailFragment : Fragment() {

    private var _binding: FragmentChallengeDetailBinding? = null
    private val binding get() = _binding!!

    private val args: ChallengeDetailFragmentArgs by navArgs()
    private var challengeId: String = ""
    private var isAccepted = false
    private var currentChallenge: Challenge? = null
    private var userProgress: UserChallengeProgress? = null

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
        setupUI()
        fetchChallengeFromApi()
        setupAnimations()
    }

    private fun setupMascot() {
        binding.mascotCheer.apply {
            mascotSize = MascotSize.MEDIUM
            setEmotion(MascotEmotion.HAPPY)
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
            // TODO: Share functionality (to be implemented in phase 3)
            android.widget.Toast.makeText(
                requireContext(),
                "Share functionality coming soon",
                android.widget.Toast.LENGTH_SHORT
            ).show()
        }
    }

    /**
     * 从后端API获取挑战详情
     */
    private fun fetchChallengeFromApi() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                // 获取挑战详情
                val response = RetrofitClient.apiService.getChallengeById(challengeId)
                if (response.code == 200 && response.data != null) {
                    currentChallenge = response.data
                    displayChallengeDetail(response.data)

                    // 获取用户进度（如果用户已登录）
                    val userId = TokenManager.getUserId()
                    if (!userId.isNullOrEmpty()) {
                        fetchUserProgress(userId)
                    }

                    Log.d("ChallengeDetail", "Loaded challenge: ${response.data.title}")
                } else {
                    showError("Challenge not found: ${response.message}")
                }
            } catch (e: Exception) {
                Log.e("ChallengeDetail", "Error loading challenge", e)
                showError("Network error: ${e.message}")
            }
        }
    }

    /**
     * 获取用户在该挑战的进度
     */
    private fun fetchUserProgress(userId: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.getChallengeProgress(challengeId, userId)
                if (response.code == 200 && response.data != null) {
                    userProgress = response.data
                    isAccepted = true
                    updateProgressUI(response.data)
                    Log.d("ChallengeDetail", "User progress: ${response.data.current}/${response.data.target}")
                } else {
                    // 用户尚未参加此挑战
                    isAccepted = false
                    updateButtonState(currentChallenge?.status ?: "ACTIVE")
                }
            } catch (e: Exception) {
                Log.e("ChallengeDetail", "Error loading user progress", e)
                // 用户可能尚未参加此挑战，不显示错误
                isAccepted = false
                updateButtonState(currentChallenge?.status ?: "ACTIVE")
            }
        }
    }

    /**
     * 显示挑战详情
     */
    private fun displayChallengeDetail(challenge: Challenge) {
        // Display challenge information
        binding.textTitle.text = challenge.title
        binding.textIcon.text = challenge.icon
        binding.textDescription.text = challenge.description

        // Type - 显示挑战类型
        binding.textType.text = when (challenge.type) {
            "GREEN_TRIPS_COUNT" -> "Trip Count Challenge"
            "GREEN_TRIPS_DISTANCE" -> "Distance Challenge"
            "CARBON_SAVED" -> "Carbon Saving Challenge"
            else -> challenge.type
        }

        // Progress - 默认显示目标值（用户进度需要单独获取）
        val target = challenge.target.toInt()
        binding.progressChallenge.max = if (target > 0) target else 1
        binding.progressChallenge.progress = 0
        binding.textProgress.text = "0 / $target"
        binding.textProgressPercent.text = "0%"

        // Reward
        binding.textReward.text = "+${challenge.reward} points"
        if (challenge.badge != null) {
            binding.textBadgeReward.visibility = View.VISIBLE
            binding.textBadgeReward.text = "Unlock Achievement Badge"
        } else {
            binding.textBadgeReward.visibility = View.GONE
        }

        // Time - 显示结束时间
        if (!challenge.endTime.isNullOrEmpty()) {
            binding.textEndTime.text = challenge.endTime.substring(0, 10).replace("-", "/")
        } else {
            binding.textEndTime.text = "No deadline"
        }

        // Participants
        binding.textParticipants.text = "${challenge.participants} people"

        // Update button state
        updateButtonState(challenge.status)
    }

    /**
     * 更新用户进度UI
     */
    private fun updateProgressUI(progress: UserChallengeProgress) {
        val current = progress.current.toInt()
        val target = progress.target.toInt()
        val percent = progress.progressPercent.toInt()

        binding.progressChallenge.max = if (target > 0) target else 1
        binding.progressChallenge.progress = current.coerceAtMost(target)
        binding.textProgress.text = "$current / $target"
        binding.textProgressPercent.text = "$percent%"

        // 3-state button: Keep Going → Claim Reward → Challenge Completed
        if (progress.status == "COMPLETED") {
            if (progress.rewardClaimed) {
                // Reward already claimed → show completed
                binding.btnAccept.text = "Challenge Completed \u2713"
                binding.btnAccept.isEnabled = false
                binding.mascotCheer.setEmotion(MascotEmotion.CELEBRATING)
                binding.mascotCheer.celebrateAnimation()
            } else {
                // Completed but reward not claimed → show Claim Reward
                val reward = currentChallenge?.reward ?: 0
                binding.btnAccept.text = "Claim Reward (+$reward pts)"
                binding.btnAccept.icon = null
                binding.btnAccept.isEnabled = true
                binding.mascotCheer.setEmotion(MascotEmotion.CELEBRATING)
                binding.mascotCheer.celebrateAnimation()
            }
        } else {
            binding.btnAccept.text = "Keep Going"
            binding.btnAccept.setIconResource(R.drawable.ic_check)
            binding.btnAccept.isEnabled = true
        }
    }

    private fun updateButtonState(status: String) {
        when (status) {
            "ACTIVE" -> {
                if (isAccepted) {
                    binding.btnAccept.text = "Keep Going"
                    binding.btnAccept.setIconResource(R.drawable.ic_check)
                } else {
                    binding.btnAccept.text = "Accept Challenge"
                    binding.btnAccept.icon = null
                }
                binding.btnAccept.isEnabled = true
            }
            "COMPLETED" -> {
                binding.btnAccept.text = "Challenge Completed"
                binding.btnAccept.isEnabled = false
            }
            "EXPIRED" -> {
                binding.btnAccept.text = "Challenge Expired"
                binding.btnAccept.isEnabled = false
            }
        }
    }

    /**
     * 参加挑战 / 领取奖励
     */
    private fun acceptChallenge() {
        val userId = TokenManager.getUserId()
        if (userId.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "Please login first", Toast.LENGTH_SHORT).show()
            return
        }

        val progress = userProgress
        if (!isAccepted) {
            // Join challenge
            viewLifecycleOwner.lifecycleScope.launch {
                try {
                    val response = RetrofitClient.apiService.joinChallenge(challengeId, userId)
                    if (response.code == 200 && response.data != null) {
                        isAccepted = true
                        userProgress = response.data
                        updateProgressUI(response.data)
                        binding.mascotCheer.celebrateAnimation()

                        MaterialAlertDialogBuilder(requireContext())
                            .setTitle("Success")
                            .setMessage("Challenge accepted! Let's complete it!")
                            .setPositiveButton("OK", null)
                            .show()
                    } else {
                        Toast.makeText(requireContext(), "Failed to join: ${response.message}", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Log.e("ChallengeDetail", "Error joining challenge", e)
                    Toast.makeText(requireContext(), "Network error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        } else if (progress != null && progress.status == "COMPLETED" && !progress.rewardClaimed) {
            // Claim reward
            claimReward(userId)
        } else {
            Toast.makeText(requireContext(), "Keep going! You're making progress!", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * 领取挑战完成奖励
     */
    private fun claimReward(userId: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                binding.btnAccept.isEnabled = false
                val response = RetrofitClient.apiService.claimChallengeReward(challengeId, userId)
                if (response.code == 200 && response.data != null) {
                    userProgress = response.data
                    updateProgressUI(response.data)

                    val reward = currentChallenge?.reward ?: 0
                    MaterialAlertDialogBuilder(requireContext())
                        .setTitle("Reward Claimed!")
                        .setMessage("Congratulations! You earned +$reward points!")
                        .setPositiveButton("OK", null)
                        .show()

                    Log.d("ChallengeDetail", "Reward claimed successfully")
                } else {
                    binding.btnAccept.isEnabled = true
                    Toast.makeText(requireContext(), "Failed to claim: ${response.message}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("ChallengeDetail", "Error claiming reward", e)
                binding.btnAccept.isEnabled = true
                Toast.makeText(requireContext(), "Network error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showError(message: String) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Error")
            .setMessage(message)
            .setPositiveButton("OK") { _, _ ->
                findNavController().navigateUp()
            }
            .show()
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
