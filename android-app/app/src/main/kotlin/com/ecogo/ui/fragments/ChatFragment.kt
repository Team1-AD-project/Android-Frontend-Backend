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
import com.ecogo.data.ChatRequest
import com.ecogo.databinding.FragmentChatBinding
import com.ecogo.ui.adapters.ChatMessageAdapter
import com.ecogo.repository.EcoGoRepository
import kotlinx.coroutines.launch

class ChatFragment : Fragment() {
    
    private var _binding: FragmentChatBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: ChatMessageAdapter
    private val repository = EcoGoRepository()
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // #region agent log
        try { java.io.File("c:\\Users\\csls\\Desktop\\ad-ui\\.cursor\\debug.log").appendText("{\"location\":\"ChatFragment.kt:30\",\"message\":\"onCreateView started\",\"data\":{},\"timestamp\":${System.currentTimeMillis()},\"sessionId\":\"debug-session\",\"hypothesisId\":\"C\"}\n") } catch(e: Exception) {}
        // #endregion
        _binding = FragmentChatBinding.inflate(inflater, container, false)
        // #region agent log
        try { java.io.File("c:\\Users\\csls\\Desktop\\ad-ui\\.cursor\\debug.log").appendText("{\"location\":\"ChatFragment.kt:34\",\"message\":\"ViewBinding inflated successfully\",\"data\":{},\"timestamp\":${System.currentTimeMillis()},\"sessionId\":\"debug-session\",\"hypothesisId\":\"C\"}\n") } catch(e: Exception) {}
        // #endregion
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // #region agent log
        try { java.io.File("c:\\Users\\csls\\Desktop\\ad-ui\\.cursor\\debug.log").appendText("{\"location\":\"ChatFragment.kt:40\",\"message\":\"onViewCreated started\",\"data\":{},\"timestamp\":${System.currentTimeMillis()},\"sessionId\":\"debug-session\",\"hypothesisId\":\"ALL\"}\n") } catch(e: Exception) {}
        // #endregion
        
        setupRecyclerView()
        // #region agent log
        try { java.io.File("c:\\Users\\csls\\Desktop\\ad-ui\\.cursor\\debug.log").appendText("{\"location\":\"ChatFragment.kt:46\",\"message\":\"setupRecyclerView completed\",\"data\":{},\"timestamp\":${System.currentTimeMillis()},\"sessionId\":\"debug-session\",\"hypothesisId\":\"D\"}\n") } catch(e: Exception) {}
        // #endregion
        setupButtons()
        // #region agent log
        try { java.io.File("c:\\Users\\csls\\Desktop\\ad-ui\\.cursor\\debug.log").appendText("{\"location\":\"ChatFragment.kt:51\",\"message\":\"setupButtons completed\",\"data\":{},\"timestamp\":${System.currentTimeMillis()},\"sessionId\":\"debug-session\",\"hypothesisId\":\"ALL\"}\n") } catch(e: Exception) {}
        // #endregion
        setupAnimations()
        // #region agent log
        try { java.io.File("c:\\Users\\csls\\Desktop\\ad-ui\\.cursor\\debug.log").appendText("{\"location\":\"ChatFragment.kt:56\",\"message\":\"setupAnimations completed\",\"data\":{},\"timestamp\":${System.currentTimeMillis()},\"sessionId\":\"debug-session\",\"hypothesisId\":\"B\"}\n") } catch(e: Exception) {}
        // #endregion
    }
    
    private fun setupRecyclerView() {
        // #region agent log
        try { java.io.File("c:\\Users\\csls\\Desktop\\ad-ui\\.cursor\\debug.log").appendText("{\"location\":\"ChatFragment.kt:60\",\"message\":\"setupRecyclerView: before adapter creation\",\"data\":{},\"timestamp\":${System.currentTimeMillis()},\"sessionId\":\"debug-session\",\"hypothesisId\":\"D\"}\n") } catch(e: Exception) {}
        // #endregion
        adapter = ChatMessageAdapter(mutableListOf(
            ChatMessageAdapter.ChatMessage("Hi! I'm LiNUS, your campus assistant. How can I help you today?", false)
        ))
        // #region agent log
        try { java.io.File("c:\\Users\\csls\\Desktop\\ad-ui\\.cursor\\debug.log").appendText("{\"location\":\"ChatFragment.kt:66\",\"message\":\"setupRecyclerView: adapter created\",\"data\":{},\"timestamp\":${System.currentTimeMillis()},\"sessionId\":\"debug-session\",\"hypothesisId\":\"D\"}\n") } catch(e: Exception) {}
        // #endregion
        
        binding.recyclerChat.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = this@ChatFragment.adapter
        }
        // #region agent log
        try { java.io.File("c:\\Users\\csls\\Desktop\\ad-ui\\.cursor\\debug.log").appendText("{\"location\":\"ChatFragment.kt:73\",\"message\":\"setupRecyclerView: RecyclerView configured\",\"data\":{},\"timestamp\":${System.currentTimeMillis()},\"sessionId\":\"debug-session\",\"hypothesisId\":\"D\"}\n") } catch(e: Exception) {}
        // #endregion
    }
    
    private fun setupButtons() {
        binding.buttonSend.setOnClickListener {
            val message = binding.editMessage.text.toString()
            if (message.isNotEmpty()) {
                // Add user message
                adapter.addMessage(ChatMessageAdapter.ChatMessage(message, true))
                binding.editMessage.text?.clear()
                
                // Scroll to bottom
                binding.recyclerChat.scrollToPosition(adapter.itemCount - 1)
                viewLifecycleOwner.lifecycleScope.launch {
                    val response = repository.sendChat(ChatRequest(message))
                        .getOrElse { null }
                    val reply = response?.reply ?: generateSmartReply(message)
                    adapter.addMessage(ChatMessageAdapter.ChatMessage(reply, false))
                    binding.recyclerChat.scrollToPosition(adapter.itemCount - 1)
                }
            }
        }
    }
    
    private fun generateSmartReply(message: String): String {
        val lowerMessage = message.lowercase()
        
        // 检测关键词并提供智能回复和导航建议
        return when {
            lowerMessage.contains("activity") || lowerMessage.contains("活动") || lowerMessage.contains("event") -> {
                // 延迟跳转到活动页面
                android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                    findNavController()
                        .navigate(com.ecogo.R.id.action_chat_to_activities)
                }, 1500)
                "我找到了一些适合你的校园活动！让我带你去看看..."
            }
            lowerMessage.contains("route") || lowerMessage.contains("路线") || lowerMessage.contains("导航") || 
            lowerMessage.contains("how to get") || lowerMessage.contains("去哪里") -> {
                android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                    findNavController()
                        .navigate(com.ecogo.R.id.action_chat_to_routePlanner)
                }, 1500)
                "让我帮你规划一条绿色出行路线！"
            }
            lowerMessage.contains("map") || lowerMessage.contains("地图") || lowerMessage.contains("位置") ||
            lowerMessage.contains("green go") || lowerMessage.contains("spot") -> {
                // 地图功能临时禁用
                // android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                //     findNavController()
                //         .navigate(com.ecogo.R.id.action_chat_to_mapGreenGo)
                // }, 1500)
                "地图功能正在开发中，敬请期待！"
            }
            else -> "I'm currently a demo version. In a real app, I would respond to: '$message'"
        }
    }

    private fun setupAnimations() {
        // #region agent log
        try { java.io.File("c:\\Users\\csls\\Desktop\\ad-ui\\.cursor\\debug.log").appendText("{\"location\":\"ChatFragment.kt:130\",\"message\":\"setupAnimations: before loading animation\",\"data\":{},\"timestamp\":${System.currentTimeMillis()},\"sessionId\":\"debug-session\",\"hypothesisId\":\"B\"}\n") } catch(e: Exception) {}
        // #endregion
        val slideUp = AnimationUtils.loadAnimation(requireContext(), com.ecogo.R.anim.slide_up)
        // #region agent log
        try { java.io.File("c:\\Users\\csls\\Desktop\\ad-ui\\.cursor\\debug.log").appendText("{\"location\":\"ChatFragment.kt:135\",\"message\":\"setupAnimations: animation loaded\",\"data\":{},\"timestamp\":${System.currentTimeMillis()},\"sessionId\":\"debug-session\",\"hypothesisId\":\"B\"}\n") } catch(e: Exception) {}
        // #endregion
        binding.recyclerChat.startAnimation(slideUp)
        binding.inputContainer.startAnimation(slideUp)
        // #region agent log
        try { java.io.File("c:\\Users\\csls\\Desktop\\ad-ui\\.cursor\\debug.log").appendText("{\"location\":\"ChatFragment.kt:141\",\"message\":\"setupAnimations: animations started\",\"data\":{},\"timestamp\":${System.currentTimeMillis()},\"sessionId\":\"debug-session\",\"hypothesisId\":\"B\"}\n") } catch(e: Exception) {}
        // #endregion
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
