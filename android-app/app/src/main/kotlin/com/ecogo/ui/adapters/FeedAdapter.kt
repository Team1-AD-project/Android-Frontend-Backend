package com.ecogo.ui.adapters

import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ecogo.R
import com.ecogo.data.FeedItem

class FeedAdapter(
    private var feedItems: List<FeedItem>,
    private val onItemClick: (FeedItem) -> Unit
) : RecyclerView.Adapter<FeedAdapter.FeedViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FeedViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_feed, parent, false)
        return FeedViewHolder(view)
    }

    override fun onBindViewHolder(holder: FeedViewHolder, position: Int) {
        val item = feedItems[position]
        holder.bind(item)
        holder.itemView.setOnClickListener {
            onItemClick(item)
        }
    }

    override fun getItemCount() = feedItems.size

    fun updateFeed(newItems: List<FeedItem>) {
        feedItems = newItems
        notifyDataSetChanged()
    }

    class FeedViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val avatar: TextView = itemView.findViewById(R.id.text_avatar)
        private val username: TextView = itemView.findViewById(R.id.text_username)
        private val timestamp: TextView = itemView.findViewById(R.id.text_timestamp)
        private val icon: ImageView = itemView.findViewById(R.id.icon_type)
        private val content: TextView = itemView.findViewById(R.id.text_content)
        private val likes: TextView = itemView.findViewById(R.id.text_likes)

        fun bind(item: FeedItem) {
            // 头像（首字母）
            avatar.text = item.username.firstOrNull()?.toString() ?: "U"
            
            // 用户名
            username.text = item.username
            
            // 时间（相对时间）
            timestamp.text = DateUtils.getRelativeTimeSpanString(
                item.timestamp,
                System.currentTimeMillis(),
                DateUtils.MINUTE_IN_MILLIS
            )
            
            // 类型图标
            val iconRes = when (item.type) {
                "TRIP" -> R.drawable.ic_route
                "ACHIEVEMENT" -> R.drawable.ic_award
                "ACTIVITY" -> R.drawable.ic_calendar_check
                "CHALLENGE" -> R.drawable.ic_trophy
                else -> R.drawable.ic_sparkles
            }
            icon.setImageResource(iconRes)
            
            // 内容
            content.text = item.content
            
            // 点赞数
            likes.text = item.likes.toString()
        }
    }
}
