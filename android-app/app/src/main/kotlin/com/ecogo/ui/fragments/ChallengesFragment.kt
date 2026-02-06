package com.ecogo.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.ecogo.R
import com.ecogo.data.MockData
import com.ecogo.databinding.FragmentChallengesBinding
import com.ecogo.ui.adapters.ChallengeAdapter
import com.google.android.material.tabs.TabLayout

/**
 * Challenges List Page
 * Display all available challenge tasks
 */
class ChallengesFragment : Fragment() {

    private var _binding: FragmentChallengesBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var challengeAdapter: ChallengeAdapter

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
        loadChallenges()
    }
    
    private fun setupTabs() {
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> loadChallenges("ALL")
                    1 -> loadChallenges("ACTIVE")
                    2 -> loadChallenges("COMPLETED")
                }
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
    
    private fun loadChallenges(filter: String = "ALL") {
        val challenges = MockData.CHALLENGES
        
        val filtered = when (filter) {
            "ACTIVE" -> challenges.filter { it.status == "ACTIVE" }
            "COMPLETED" -> challenges.filter { it.status == "COMPLETED" }
            else -> challenges
        }
        
        challengeAdapter.updateChallenges(filtered)
        
        // Show empty state
        if (filtered.isEmpty()) {
            binding.emptyState.visibility = View.VISIBLE
            binding.recyclerChallenges.visibility = View.GONE
        } else {
            binding.emptyState.visibility = View.GONE
            binding.recyclerChallenges.visibility = View.VISIBLE
        }
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
