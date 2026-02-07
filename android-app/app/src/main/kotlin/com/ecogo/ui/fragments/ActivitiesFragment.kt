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
import androidx.recyclerview.widget.LinearLayoutManager
import com.ecogo.R
import com.ecogo.auth.TokenManager
import com.ecogo.data.Activity
import com.ecogo.databinding.FragmentActivitiesBinding
import com.ecogo.repository.EcoGoRepository
import com.ecogo.ui.adapters.ActivityAdapter
import com.google.android.material.tabs.TabLayout
import kotlinx.coroutines.launch

class ActivitiesFragment : Fragment() {

    private var _binding: FragmentActivitiesBinding? = null
    private val binding get() = _binding!!
    private val repository = EcoGoRepository()
    private val args: ActivitiesFragmentArgs by navArgs()

    private lateinit var activityAdapter: ActivityAdapter
    private var allActivities: List<Activity> = emptyList()
    private var userId: String = "user123"
    private var currentFilter: String = "ALL"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentActivitiesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        userId = TokenManager.getUserId() ?: "user123"
        setupRecyclerView()
        setupTabs()
        setupAnimations()
        loadActivities()
    }

    private fun setupTabs() {
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                currentFilter = when (tab?.position) {
                    0 -> "ALL"
                    1 -> "JOINED"
                    else -> "ALL"
                }
                filterAndDisplayActivities()
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        // 如果从 MonthlyHighlights 的 View All 进来，默认选中 Joined tab
        if (args.showJoinedOnly) {
            binding.tabLayout.getTabAt(1)?.select()
        }
    }

    private fun setupRecyclerView() {
        activityAdapter = ActivityAdapter(emptyList()) { activity ->
            val action = ActivitiesFragmentDirections
                .actionActivitiesToActivityDetail(activityId = activity.id ?: "")
            findNavController().navigate(action)
        }

        binding.recyclerActivities.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = activityAdapter
        }
    }

    private fun loadActivities() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                allActivities = repository.getAllActivities().getOrElse { emptyList() }
                Log.d("ActivitiesFragment", "Loaded ${allActivities.size} activities from API")
                filterAndDisplayActivities()
            } catch (e: Exception) {
                Log.e("ActivitiesFragment", "Error loading activities", e)
                Toast.makeText(requireContext(), "Network error: ${e.message}", Toast.LENGTH_SHORT).show()
                showEmptyState("Failed to load activities")
            }
        }
    }

    private fun filterAndDisplayActivities() {
        val filtered = when (currentFilter) {
            "JOINED" -> allActivities.filter { it.participantIds.contains(userId) }
            else -> allActivities
        }

        activityAdapter.updateActivities(filtered)

        if (filtered.isEmpty()) {
            val message = if (currentFilter == "JOINED") "No activities joined yet" else "No activities available"
            showEmptyState(message)
        } else {
            binding.emptyState.visibility = View.GONE
            binding.recyclerActivities.visibility = View.VISIBLE
        }
    }

    private fun showEmptyState(message: String) {
        binding.emptyState.visibility = View.VISIBLE
        binding.recyclerActivities.visibility = View.GONE
        binding.textEmptyState.text = message
    }

    private fun setupAnimations() {
        val slideUp = AnimationUtils.loadAnimation(requireContext(), R.anim.slide_up)
        binding.recyclerActivities.startAnimation(slideUp)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
