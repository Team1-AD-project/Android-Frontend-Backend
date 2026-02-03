package com.ecogo.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.ecogo.data.MockData
import com.ecogo.databinding.FragmentFriendsBinding
import com.ecogo.repository.EcoGoRepository
import com.ecogo.ui.adapters.FriendAdapter
import com.ecogo.ui.adapters.FriendActivityAdapter
import kotlinx.coroutines.launch

class FriendsFragment : Fragment() {

    private var _binding: FragmentFriendsBinding? = null
    private val binding get() = _binding!!
    private val repository = EcoGoRepository()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFriendsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerViews()
        setupActions()
        loadData()
    }

    private fun setupRecyclerViews() {
        binding.recyclerFriends.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = FriendAdapter(emptyList(), 
                onMessageClick = { friend ->
                    // 跳转到聊天页面
                    findNavController().navigate(com.ecogo.R.id.action_friends_to_chat)
                },
                onFriendClick = { friend ->
                    // 跳转到好友详情（个人资料页）
                    findNavController().navigate(com.ecogo.R.id.action_friends_to_profile)
                }
            )
        }

        binding.recyclerFriendActivities.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = FriendActivityAdapter(emptyList())
        }

        binding.recyclerFriendRequests.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = FriendAdapter(emptyList(),
                onMessageClick = { friend ->
                    // 接受好友请求
                    acceptFriendRequest(friend)
                },
                onFriendClick = { friend ->
                    // 查看请求详情
                    findNavController().navigate(com.ecogo.R.id.action_friends_to_profile)
                }
            )
        }
    }

    private fun setupActions() {
        binding.buttonAddFriend.setOnClickListener {
            // 打开添加好友对话框或页面
            // TODO: 实现添加好友功能
        }
    }

    private fun loadData() {
        viewLifecycleOwner.lifecycleScope.launch {
            // 加载好友列表
            val friends = repository.getFriends("user123").getOrElse { MockData.FRIENDS }
            binding.recyclerFriends.adapter = FriendAdapter(friends,
                onMessageClick = { friend ->
                    findNavController().navigate(com.ecogo.R.id.action_friends_to_chat)
                },
                onFriendClick = { friend ->
                    findNavController().navigate(com.ecogo.R.id.action_friends_to_profile)
                }
            )

            // 更新好友数量
            binding.textFriendCount.text = "${friends.size} friends"

            // 显示/隐藏空状态
            if (friends.isEmpty()) {
                binding.layoutEmptyState.visibility = View.VISIBLE
                binding.recyclerFriends.visibility = View.GONE
            } else {
                binding.layoutEmptyState.visibility = View.GONE
                binding.recyclerFriends.visibility = View.VISIBLE
            }

            // 加载好友动态
            val activities = repository.getFriendActivities("user123").getOrElse { MockData.FRIEND_ACTIVITIES }
            binding.recyclerFriendActivities.adapter = FriendActivityAdapter(activities)
        }
    }

    private fun acceptFriendRequest(friend: com.ecogo.data.Friend) {
        viewLifecycleOwner.lifecycleScope.launch {
            // TODO: 调用API接受好友请求
            loadData() // 重新加载数据
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
