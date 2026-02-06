package com.ecogo.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ecogo.R
import com.ecogo.data.FriendActivity

class FriendActivityAdapter(
    private val activities: List<FriendActivity>
) : RecyclerView.Adapter<FriendActivityAdapter.FriendActivityViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendActivityViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_friend_activity, parent, false)
        return FriendActivityViewHolder(view)
    }

    override fun onBindViewHolder(holder: FriendActivityViewHolder, position: Int) {
        holder.bind(activities[position])
    }

    override fun getItemCount(): Int = activities.size

    class FriendActivityViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val icon: ImageView = itemView.findViewById(R.id.image_activity_icon)
        private val friendName: TextView = itemView.findViewById(R.id.text_activity_friend_name)
        private val details: TextView = itemView.findViewById(R.id.text_activity_details)
        private val time: TextView = itemView.findViewById(R.id.text_activity_time)

        fun bind(activity: FriendActivity) {
            friendName.text = activity.friendName
            details.text = activity.details
            time.text = activity.timestamp

            // 根据动作类型设置图标
            val iconRes = when (activity.action) {
                "joined_activity" -> R.drawable.ic_trophy
                "earned_badge" -> R.drawable.ic_award
                "completed_goal" -> R.drawable.ic_check
                else -> R.drawable.ic_user
            }
            icon.setImageResource(iconRes)
        }
    }
}
