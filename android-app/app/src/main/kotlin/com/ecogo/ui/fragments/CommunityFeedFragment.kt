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
import com.ecogo.R
import com.ecogo.data.FeedItem
import com.ecogo.databinding.FragmentCommunityFeedBinding
import com.ecogo.repository.EcoGoRepository
import com.ecogo.ui.adapters.FeedAdapter
import kotlinx.coroutines.launch

/**
 * 社区动态信息流页面
 * 显示好友的活动、成就、行程等动态
 */
class CommunityFeedFragment : Fragment() {

    private var _binding: FragmentCommunityFeedBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var feedAdapter: FeedAdapter
    private val repository = EcoGoRepository()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCommunityFeedBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupRecyclerView()
        setupUI()
        loadFeed()
        setupAnimations()
    }
    
    private fun setupRecyclerView() {
        feedAdapter = FeedAdapter(emptyList()) { feedItem ->
            handleFeedItemClick(feedItem)
        }
        
        binding.recyclerFeed.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = feedAdapter
        }
    }
    
    private fun setupUI() {
        binding.swipeRefresh.setOnRefreshListener {
            loadFeed()
            binding.swipeRefresh.isRefreshing = false
        }
    }
    
    private fun loadFeed() {
        viewLifecycleOwner.lifecycleScope.launch {
            // 显示加载中状态
            binding.emptyState.visibility = View.GONE
            binding.recyclerFeed.visibility = View.VISIBLE
            
            // 从 Repository 获取动态数据
            val result = repository.getFeed("user123")
            
            result.onSuccess { feedItems: List<com.ecogo.data.FeedItem> ->
                feedAdapter.updateFeed(feedItems)
                
                if (feedItems.isEmpty()) {
                    binding.emptyState.visibility = View.VISIBLE
                    binding.recyclerFeed.visibility = View.GONE
                } else {
                    binding.emptyState.visibility = View.GONE
                    binding.recyclerFeed.visibility = View.VISIBLE
                }
            }.onFailure { error: Throwable ->
                // 发生错误时显示空状态
                binding.emptyState.visibility = View.VISIBLE
                binding.recyclerFeed.visibility = View.GONE
                android.widget.Toast.makeText(
                    requireContext(),
                    "加载失败: ${error.message}",
                    android.widget.Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
    
    private fun handleFeedItemClick(feedItem: FeedItem) {
        // 根据类型跳转到不同页面
        when (feedItem.type) {
            "ACTIVITY" -> {
                // 跳转到活动详情
                val action = CommunityFeedFragmentDirections
                    .actionCommunityFeedToActivityDetail(feedItem.id)
                findNavController().navigate(action)
            }
            "ACHIEVEMENT" -> {
                // 跳转到个人资料页面查看成就
                findNavController()
                    .navigate(R.id.action_communityFeed_to_profile)
            }
            "CHALLENGE" -> {
                // 跳转到挑战详情
                val action = CommunityFeedFragmentDirections
                    .actionCommunityFeedToChallengeDetail(feedItem.id)
                findNavController().navigate(action)
            }
            "TRIP" -> {
                // 跳转到行程总结页面
                findNavController()
                    .navigate(R.id.action_communityFeed_to_tripSummary)
            }
            else -> {}
        }
    }
    
    private fun setupAnimations() {
        val slideUp = AnimationUtils.loadAnimation(requireContext(), R.anim.slide_up)
        binding.recyclerFeed.startAnimation(slideUp)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
