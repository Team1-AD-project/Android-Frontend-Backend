package com.ecogo.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ecogo.R

class ChatMessageAdapter(private val messages: MutableList<ChatMessage>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    
    companion object {
        const val VIEW_TYPE_USER = 1
        const val VIEW_TYPE_AI = 2
    }
    
    override fun getItemViewType(position: Int) =
        if (messages[position].isUser) VIEW_TYPE_USER else VIEW_TYPE_AI
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        // #region agent log
        try { java.io.File("c:\\Users\\csls\\Desktop\\ad-ui\\.cursor\\debug.log").appendText("{\"location\":\"ChatMessageAdapter.kt:22\",\"message\":\"onCreateViewHolder called\",\"data\":{\"viewType\":$viewType},\"timestamp\":${System.currentTimeMillis()},\"sessionId\":\"debug-session\",\"hypothesisId\":\"A\"}\n") } catch(e: Exception) {}
        // #endregion
        return if (viewType == VIEW_TYPE_USER) {
            // #region agent log
            try { java.io.File("c:\\Users\\csls\\Desktop\\ad-ui\\.cursor\\debug.log").appendText("{\"location\":\"ChatMessageAdapter.kt:26\",\"message\":\"inflating item_chat_user\",\"data\":{},\"timestamp\":${System.currentTimeMillis()},\"sessionId\":\"debug-session\",\"hypothesisId\":\"A\"}\n") } catch(e: Exception) {}
            // #endregion
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_chat_user, parent, false)
            // #region agent log
            try { java.io.File("c:\\Users\\csls\\Desktop\\ad-ui\\.cursor\\debug.log").appendText("{\"location\":\"ChatMessageAdapter.kt:31\",\"message\":\"item_chat_user inflated successfully\",\"data\":{},\"timestamp\":${System.currentTimeMillis()},\"sessionId\":\"debug-session\",\"hypothesisId\":\"A\"}\n") } catch(e: Exception) {}
            // #endregion
            UserMessageViewHolder(view)
        } else {
            // #region agent log
            try { java.io.File("c:\\Users\\csls\\Desktop\\ad-ui\\.cursor\\debug.log").appendText("{\"location\":\"ChatMessageAdapter.kt:36\",\"message\":\"inflating item_chat_ai\",\"data\":{},\"timestamp\":${System.currentTimeMillis()},\"sessionId\":\"debug-session\",\"hypothesisId\":\"A\"}\n") } catch(e: Exception) {}
            // #endregion
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_chat_ai, parent, false)
            // #region agent log
            try { java.io.File("c:\\Users\\csls\\Desktop\\ad-ui\\.cursor\\debug.log").appendText("{\"location\":\"ChatMessageAdapter.kt:41\",\"message\":\"item_chat_ai inflated successfully\",\"data\":{},\"timestamp\":${System.currentTimeMillis()},\"sessionId\":\"debug-session\",\"hypothesisId\":\"A\"}\n") } catch(e: Exception) {}
            // #endregion
            AiMessageViewHolder(view)
        }
    }
    
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = messages[position]
        when (holder) {
            is UserMessageViewHolder -> holder.bind(message)
            is AiMessageViewHolder -> holder.bind(message)
        }
    }
    
    override fun getItemCount() = messages.size
    
    fun addMessage(message: ChatMessage) {
        messages.add(message)
        notifyItemInserted(messages.size - 1)
    }
    
    class UserMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val text: TextView = itemView.findViewById(R.id.text_message)
        
        fun bind(message: ChatMessage) {
            text.text = message.text
        }
    }
    
    class AiMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val text: TextView = itemView.findViewById(R.id.text_message)
        
        fun bind(message: ChatMessage) {
            text.text = message.text
        }
    }
    
    data class ChatMessage(val text: String, val isUser: Boolean)
}
