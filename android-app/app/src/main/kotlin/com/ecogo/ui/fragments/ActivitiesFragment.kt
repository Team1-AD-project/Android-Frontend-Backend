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
import com.ecogo.data.MockData
import com.ecogo.databinding.FragmentActivitiesBinding
import com.ecogo.ui.adapters.ActivityAdapter
import com.ecogo.repository.EcoGoRepository
import kotlinx.coroutines.launch

class ActivitiesFragment : Fragment() {

    private var _binding: FragmentActivitiesBinding? = null
    private val binding get() = _binding!!
    private val repository = EcoGoRepository()
    private val args: ActivitiesFragmentArgs by navArgs()

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

        setupRecyclerView()
        setupAnimations()
        loadActivities()
    }

    private fun setupRecyclerView() {
        binding.recyclerActivities.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = ActivityAdapter(emptyList())
        }
    }

    private fun loadActivities() {
        viewLifecycleOwner.lifecycleScope.launch {
            val userId = com.ecogo.auth.TokenManager.getUserId() ?: "user123"
            val allActivities = repository.getAllActivities().getOrElse { MockData.ACTIVITIES }

            // 根据参数决定是否只显示已加入的活动
            val activities = if (args.showJoinedOnly) {
                allActivities.filter { it.participantIds.contains(userId) }
            } else {
                allActivities
            }

            if (activities.isEmpty() && args.showJoinedOnly) {
                // 显示空状态
                binding.textEmptyState?.visibility = View.VISIBLE
                binding.recyclerActivities.visibility = View.GONE
            } else {
                binding.textEmptyState?.visibility = View.GONE
                binding.recyclerActivities.visibility = View.VISIBLE
                binding.recyclerActivities.adapter = ActivityAdapter(activities) { activity ->
                    // 导航到活动详情页
                    val action = ActivitiesFragmentDirections
                        .actionActivitiesToActivityDetail(activityId = activity.id ?: "")
                    findNavController().navigate(action)
                }
            }
        }
    }

    private fun setupAnimations() {
        val slideUp = AnimationUtils.loadAnimation(requireContext(), com.ecogo.R.anim.slide_up)
        binding.recyclerActivities.startAnimation(slideUp)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
