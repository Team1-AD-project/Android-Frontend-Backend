package com.ecogo.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ecogo.R
import com.ecogo.data.Activity

class ActivityAdapter(private val activities: List<Activity>) : 
    RecyclerView.Adapter<ActivityAdapter.ActivityViewHolder>() {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ActivityViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_activity, parent, false)
        return ActivityViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: ActivityViewHolder, position: Int) {
        holder.bind(activities[position])
    }
    
    override fun getItemCount() = activities.size
    
    class ActivityViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val title: TextView = itemView.findViewById(R.id.text_title)
        private val date: TextView = itemView.findViewById(R.id.text_date)
        private val location: TextView = itemView.findViewById(R.id.text_location)
        private val points: TextView = itemView.findViewById(R.id.text_points)
        
        fun bind(activity: Activity) {
            title.text = activity.title
            
            // 格式化开始时间（从 ISO 8601 转换为友好格式）
            date.text = activity.startTime?.let { time ->
                // 简化的日期格式化（实际项目可能需要使用 DateTimeFormatter）
                time.substring(0, 10).replace("-", "/")
            } ?: "TBD"
            
            // 显示活动类型
            location.text = when (activity.type) {
                "ONLINE" -> "线上活动"
                "OFFLINE" -> "线下活动"
                else -> activity.type
            }
            
            // 使用新的 rewardCredits 字段
            points.text = "+${activity.rewardCredits} pts"
        }
    }
}
