package com.ecogo.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.ecogo.data.MockData
import com.ecogo.data.Community
import com.ecogo.data.Friend
import com.ecogo.databinding.FragmentCommunityBinding
import com.ecogo.ui.adapters.CommunityAdapter
import com.ecogo.ui.adapters.FriendAdapter
import com.ecogo.repository.EcoGoRepository
import com.google.android.material.tabs.TabLayout
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
        
        setupTabs()
        setupRecyclerView()
        setupLeader()
        setupAnimations()
        setupActions()
        loadCommunities()
    }
    
    private fun setupTabs() {
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> showFacultyLeaderboard()
                    1 -> showFriendsLeaderboard()
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun showFacultyLeaderboard() {
        binding.layoutFacultyLeaderboard.visibility = View.VISIBLE
        binding.layoutFriendsLeaderboard.visibility = View.GONE
        loadCommunities()
    }

    private fun showFriendsLeaderboard() {
        binding.layoutFacultyLeaderboard.visibility = View.GONE
        binding.layoutFriendsLeaderboard.visibility = View.VISIBLE
        loadFriendsLeaderboard()
    }

    private fun setupRecyclerView() {
        binding.recyclerCommunity.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = CommunityAdapter(emptyList())
        }

        binding.recyclerFriendsLeaderboard.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = FriendAdapter(emptyList(),
                onMessageClick = { friend ->
                    findNavController().navigate(com.ecogo.R.id.chatFragment)
                },
                onFriendClick = { friend ->
                    findNavController().navigate(com.ecogo.R.id.profileFragment)
                }
            )
        }
    }

    private fun setupLeader() {
        val leader = MockData.COMMUNITIES.firstOrNull()
        if (leader != null) {
            binding.textLeaderName.text = leader.name
            binding.textLeaderPoints.text = "${leader.points} pts • +12% this week"
        }
    }

    private fun setupActions() {
        binding.textViewAllFriendsLeaderboard?.setOnClickListener {
            findNavController().navigate(com.ecogo.R.id.friendsFragment)
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

    private fun loadFriendsLeaderboard() {
        viewLifecycleOwner.lifecycleScope.launch {
            // 加载好友排行榜
            val friends = repository.getFriends("user123").getOrElse { MockData.FRIENDS }
            
            // 按积分排序
            val sortedFriends = friends.sortedByDescending { friend: Friend -> friend.points }
            
            // 更新我的排名
            binding.textMyRank?.text = "Rank #5"
            binding.textMyPoints?.text = "850 pts"
            
            // 更新好友排行榜
            binding.recyclerFriendsLeaderboard.adapter = FriendAdapter(sortedFriends,
                onMessageClick = { friend ->
                    findNavController().navigate(com.ecogo.R.id.chatFragment)
                },
                onFriendClick = { friend ->
                    findNavController().navigate(com.ecogo.R.id.profileFragment)
                }
            )
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
