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
import androidx.recyclerview.widget.LinearLayoutManager
import com.ecogo.R
import com.ecogo.api.RetrofitClient
import com.ecogo.auth.TokenManager
import com.ecogo.data.Challenge
import com.ecogo.databinding.FragmentChallengesBinding
import com.ecogo.ui.adapters.ChallengeAdapter
import com.google.android.material.tabs.TabLayout
import kotlinx.coroutines.launch

/**
 * Challenges List Page
 * Display all available challenge tasks
 * 从后端API获取挑战数据
 */
class ChallengesFragment : Fragment() {

    private var _binding: FragmentChallengesBinding? = null
    private val binding get() = _binding!!

    private lateinit var challengeAdapter: ChallengeAdapter
    private var allChallenges: List<Challenge> = emptyList()
    private var joinedChallengeIds: Set<String> = emptySet()
    private var completedChallengeIds: MutableSet<String> = mutableSetOf()
    private var currentFilter: String = "ALL"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChallengesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupTabs()
        setupRecyclerView()
        setupAnimations()
        fetchChallengesFromApi()
    }

    private fun setupTabs() {
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                currentFilter = when (tab?.position) {
                    0 -> "ALL"
                    1 -> "ACTIVE"
                    2 -> "COMPLETED"
                    else -> "ALL"
                }
                filterAndDisplayChallenges()
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun setupRecyclerView() {
        challengeAdapter = ChallengeAdapter(emptyList()) { challenge ->
            // 点击挑战导航到详情页
            val action = ChallengesFragmentDirections
                .actionChallengesToChallengeDetail(challengeId = challenge.id)
            findNavController().navigate(action)
        }

        binding.recyclerChallenges.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = challengeAdapter
        }
    }

    /**
     * 从后端API获取挑战数据
     */
    private fun fetchChallengesFromApi() {
        showLoading(true)

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.getAllChallenges()
                if (response.code == 200 && response.data != null) {
                    allChallenges = response.data
                    Log.d("ChallengesFragment", "Loaded ${allChallenges.size} challenges from API")
                } else {
                    showError("Failed to load challenges: ${response.message}")
                    return@launch
                }

                // 获取用户已加入的挑战ID列表
                val userId = TokenManager.getUserId() ?: "user123"
                try {
                    val userResponse = RetrofitClient.apiService.getUserChallenges(userId)
                    if (userResponse.success && userResponse.data != null) {
                        joinedChallengeIds = userResponse.data.map { it.id }.toSet()
                        Log.d("ChallengesFragment", "User joined ${joinedChallengeIds.size} challenges")

                        // 检查每个已参加挑战的进度，判断哪些已完成
                        completedChallengeIds.clear()
                        for (challengeId in joinedChallengeIds) {
                            try {
                                val progressResp = RetrofitClient.apiService.getChallengeProgress(challengeId, userId)
                                if (progressResp.code == 200 && progressResp.data != null) {
                                    if (progressResp.data.status == "COMPLETED") {
                                        completedChallengeIds.add(challengeId)
                                    }
                                }
                            } catch (e: Exception) {
                                Log.e("ChallengesFragment", "Error checking progress for $challengeId", e)
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.e("ChallengesFragment", "Error loading user challenges", e)
                }

                filterAndDisplayChallenges()
            } catch (e: Exception) {
                Log.e("ChallengesFragment", "Error loading challenges", e)
                showError("Network error: ${e.message}")
            } finally {
                showLoading(false)
            }
        }
    }

    /**
     * 根据当前筛选条件显示挑战
     */
    private fun filterAndDisplayChallenges() {
        val filtered = when (currentFilter) {
            "ACTIVE" -> allChallenges.filter { it.id in joinedChallengeIds && it.id !in completedChallengeIds }
            "COMPLETED" -> allChallenges.filter { it.id in completedChallengeIds }
            else -> allChallenges
        }

        challengeAdapter.updateChallenges(filtered, completedChallengeIds)

        // Show empty state
        if (filtered.isEmpty()) {
            binding.emptyState.visibility = View.VISIBLE
            binding.recyclerChallenges.visibility = View.GONE
        } else {
            binding.emptyState.visibility = View.GONE
            binding.recyclerChallenges.visibility = View.VISIBLE
        }
    }

    private fun showLoading(show: Boolean) {
        // 如果有loading指示器可以在这里显示/隐藏
        if (show) {
            binding.recyclerChallenges.visibility = View.GONE
        }
    }

    private fun showError(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        binding.emptyState.visibility = View.VISIBLE
        binding.recyclerChallenges.visibility = View.GONE
    }

    private fun setupAnimations() {
        val slideUp = AnimationUtils.loadAnimation(requireContext(), R.anim.slide_up)
        binding.recyclerChallenges.startAnimation(slideUp)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
