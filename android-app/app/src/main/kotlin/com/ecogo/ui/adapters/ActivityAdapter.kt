package com.ecogo.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ecogo.R
import com.ecogo.data.Activity

class ActivityAdapter(
    private var activities: List<Activity>,
    private val onActivityClick: (Activity) -> Unit = {}
) : RecyclerView.Adapter<ActivityAdapter.ActivityViewHolder>() {

    fun updateActivities(newActivities: List<Activity>) {
        activities = newActivities
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ActivityViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_activity, parent, false)
        return ActivityViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: ActivityViewHolder, position: Int) {
        val activity = activities[position]
        holder.bind(activity)
        holder.itemView.setOnClickListener {
            onActivityClick(activity)
        }
    }
    
    override fun getItemCount() = activities.size
    
    class ActivityViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageHeader: View = itemView.findViewById(R.id.layout_image_header)
        private val category: TextView = itemView.findViewById(R.id.text_category)
        private val title: TextView = itemView.findViewById(R.id.text_title)
        private val date: TextView = itemView.findViewById(R.id.text_date)
        private val location: TextView = itemView.findViewById(R.id.text_location)
        private val icon: TextView = itemView.findViewById(R.id.text_icon)
        
        fun bind(activity: Activity) {
            title.text = activity.title
            
            date.text = activity.startTime?.let { time ->
                time.substring(0, 10).replace("-", "/")
            } ?: "TBD"
            
            location.text = activity.description.takeIf { it.isNotEmpty() } 
                ?: when (activity.type) {
                    "ONLINE" -> "Online Event"
                    "OFFLINE" -> "On-Campus"
                    else -> activity.type
                }
            
            category.text = when (activity.type) {
                "ONLINE" -> "Campaign"
                "OFFLINE" -> "Campus"
                else -> "Event"
            }
            
            icon.text = when {
                activity.title.contains("Clean", ignoreCase = true) -> "🧹"
                activity.title.contains("Workshop", ignoreCase = true) -> "🥗"
                activity.title.contains("Run", ignoreCase = true) -> "🏃"
                activity.title.contains("Recycl", ignoreCase = true) -> "♻️"
                else -> "🌱"
            }
            
            val colorRes = when (bindingAdapterPosition % 4) {
                0 -> android.graphics.Color.parseColor("#3B82F6")
                1 -> android.graphics.Color.parseColor("#F97316")
                2 -> android.graphics.Color.parseColor("#EF4444")
                else -> android.graphics.Color.parseColor("#10B981")
            }
            imageHeader.setBackgroundColor(colorRes)
        }
    }
}
