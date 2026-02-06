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
 * Challenge Detail Page
 * Display challenge rules, progress, leaderboard, etc.
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
            // TODO: Share functionality (to be implemented in phase 3)
            android.widget.Toast.makeText(
                requireContext(),
                "Share functionality coming soon",
                android.widget.Toast.LENGTH_SHORT
            ).show()
        }
    }
    
    private fun loadChallengeDetail() {
        val challenge = MockData.CHALLENGES.find { it.id == challengeId }
        
        if (challenge == null) {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Error")
                .setMessage("Challenge not found")
                .setPositiveButton("OK") { _, _ ->
                    findNavController().navigateUp()
                }
                .show()
            return
        }
        
        // Display challenge information
        binding.textTitle.text = challenge.title
        binding.textIcon.text = challenge.icon
        binding.textDescription.text = challenge.description
        
        // Type
        binding.textType.text = when (challenge.type) {
            "INDIVIDUAL" -> "Individual Challenge"
            "TEAM" -> "Team Challenge"
            "FACULTY" -> "Faculty Challenge"
            else -> challenge.type
        }
        
        // Progress
        val progressPercent = if (challenge.target > 0) {
            ((challenge.current.toFloat() / challenge.target) * 100).toInt()
        } else 0
        
        binding.progressChallenge.max = challenge.target
        binding.progressChallenge.progress = challenge.current
        binding.textProgress.text = "${challenge.current} / ${challenge.target}"
        binding.textProgressPercent.text = "$progressPercent%"
        
        // Reward
        binding.textReward.text = "+${challenge.reward} points"
        if (challenge.badge != null) {
            binding.textBadgeReward.visibility = View.VISIBLE
            binding.textBadgeReward.text = "Unlock Achievement Badge"
        } else {
            binding.textBadgeReward.visibility = View.GONE
        }
        
        // Time
        binding.textEndTime.text = challenge.endTime.substring(0, 10).replace("-", "/")
        
        // Participants
        binding.textParticipants.text = "${challenge.participants} people"
        
        // Leaderboard (simplified version, using topUsers)
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
        
        // Update button state
        updateButtonState(challenge.status)
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
    
    private fun acceptChallenge() {
        if (!isAccepted) {
            isAccepted = true
            binding.btnAccept.text = "Keep Going"
            binding.btnAccept.setIconResource(R.drawable.ic_check)
            
            // Mascot celebration animation
            binding.mascotCheer.celebrateAnimation()
            
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Success")
                .setMessage("Challenge accepted! Let's complete it!")
                .setPositiveButton("OK", null)
                .show()
        } else {
            // If challenge is already completed
            val challenge = MockData.CHALLENGES.find { it.id == challengeId }
            if (challenge != null && challenge.current >= challenge.target) {
                // Show achievement unlock dialog
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
