package com.ecogo.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
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
        _binding = FragmentChatBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupRecyclerView()
        setupButtons()
        setupAnimations()
    }
    
    private fun setupRecyclerView() {
        adapter = ChatMessageAdapter(mutableListOf(
            ChatMessageAdapter.ChatMessage("Hi! I'm LiNUS, your campus assistant. How can I help you today?", false)
        ))
        
        binding.recyclerChat.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = this@ChatFragment.adapter
        }
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
                    val reply = response?.reply
                        ?: "I'm currently a demo version. In a real app, I would respond to: '$message'"
                    adapter.addMessage(ChatMessageAdapter.ChatMessage(reply, false))
                    binding.recyclerChat.scrollToPosition(adapter.itemCount - 1)
                }
            }
        }
    }

    private fun setupAnimations() {
        val slideUp = AnimationUtils.loadAnimation(requireContext(), com.ecogo.R.anim.slide_up)
        binding.recyclerChat.startAnimation(slideUp)
        binding.inputContainer.startAnimation(slideUp)
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
