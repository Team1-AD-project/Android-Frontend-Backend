package com.ecogo.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.ecogo.data.MockData
import com.ecogo.data.Community
import com.ecogo.databinding.FragmentCommunityBinding
import com.ecogo.ui.adapters.CommunityAdapter
import com.ecogo.repository.EcoGoRepository
import kotlinx.coroutines.launch

class CommunityFragment : Fragment() {
    
    private var _binding: FragmentCommunityBinding? = null
    private val binding get() = _binding!!
    private val repository = EcoGoRepository()
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCommunityBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupRecyclerView()
        setupLeader()
        setupAnimations()
        loadCommunities()
    }
    
    private fun setupRecyclerView() {
        binding.recyclerCommunity.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = CommunityAdapter(emptyList())
        }
    }

    private fun setupLeader() {
        val leader = MockData.COMMUNITIES.firstOrNull()
        if (leader != null) {
            binding.textLeaderName.text = leader.name
            binding.textLeaderPoints.text = "${leader.points} pts • +12% this week"
        }
    }

    private fun loadCommunities() {
        viewLifecycleOwner.lifecycleScope.launch {
            val faculties = repository.getFaculties().getOrElse { emptyList() }
            val communities = if (faculties.isNotEmpty()) {
                faculties.map { Community(name = it.name, points = it.score, change = 0) }
            } else {
                MockData.COMMUNITIES
            }

            binding.recyclerCommunity.adapter = CommunityAdapter(communities)
            val leader = communities.firstOrNull()
            if (leader != null) {
                binding.textLeaderName.text = leader.name
                binding.textLeaderPoints.text = "${leader.points} pts • +12% this week"
            }
        }
    }

    private fun setupAnimations() {
        val popIn = AnimationUtils.loadAnimation(requireContext(), com.ecogo.R.anim.pop_in)
        binding.cardLeader.startAnimation(popIn)
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
