package com.ecogo.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ecogo.R
import com.ecogo.data.Activity

class MonthlyActivityAdapter(
    private var activities: List<Activity>,
    private val onItemClick: (Activity) -> Unit
) : RecyclerView.Adapter<MonthlyActivityAdapter.MonthlyActivityViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MonthlyActivityViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_monthly_activity, parent, false)
        return MonthlyActivityViewHolder(view)
    }

    override fun onBindViewHolder(holder: MonthlyActivityViewHolder, position: Int) {
        holder.bind(activities[position], onItemClick)
    }

    override fun getItemCount(): Int = activities.size

    fun updateData(newActivities: List<Activity>) {
        activities = newActivities
        notifyDataSetChanged()
    }

    class MonthlyActivityViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val icon: TextView = itemView.findViewById(R.id.text_activity_icon)
        private val title: TextView = itemView.findViewById(R.id.text_activity_title)
        private val date: TextView = itemView.findViewById(R.id.text_activity_date)
        private val participants: TextView = itemView.findViewById(R.id.text_activity_participants)
        private val status: TextView = itemView.findViewById(R.id.text_activity_status)
        private val reward: TextView = itemView.findViewById(R.id.text_activity_reward)

        fun bind(activity: Activity, onItemClick: (Activity) -> Unit) {
            title.text = activity.title
            
            // Date formatting
            val dateStr = activity.startTime?.let { 
                try {
                    // Format: "2026-02-15T10:00:00" -> "Feb 15, 2026"
                    val parts = it.split("T")[0].split("-")
                    val months = arrayOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", 
                                       "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
                    val month = months[parts[1].toInt() - 1]
                    "$month ${parts[2]}, ${parts[0]}"
                } catch (e: Exception) {
                    it.substring(0, 10)
                }
            } ?: "TBD"
            date.text = dateStr
            
            // Participants
            participants.text = "${activity.currentParticipants} joined"
            
            // Status
            status.text = when (activity.status) {
                "PUBLISHED" -> "Upcoming"
                "ONGOING" -> "Ongoing"
                "ENDED" -> "Ended"
                "DRAFT" -> "Draft"
                else -> activity.status
            }
            
            // Reward
            reward.text = "+${activity.rewardCredits}"
            
            // Icon based on activity type or title
            icon.text = when {
                activity.title.contains("Clean", ignoreCase = true) -> "🧹"
                activity.title.contains("Workshop", ignoreCase = true) -> "🥗"
                activity.title.contains("Run", ignoreCase = true) -> "🏃"
                activity.title.contains("Recycl", ignoreCase = true) -> "♻️"
                activity.title.contains("Friday", ignoreCase = true) -> "🚶"
                activity.title.contains("Container", ignoreCase = true) -> "🍱"
                activity.title.contains("Plant", ignoreCase = true) -> "🌱"
                activity.title.contains("Bike", ignoreCase = true) -> "🚲"
                activity.title.contains("Walk", ignoreCase = true) -> "🚶"
                activity.type == "ONLINE" -> "💻"
                activity.type == "OFFLINE" -> "📍"
                else -> "🌱"
            }
            
            itemView.setOnClickListener {
                onItemClick(activity)
            }
        }
    }
}
