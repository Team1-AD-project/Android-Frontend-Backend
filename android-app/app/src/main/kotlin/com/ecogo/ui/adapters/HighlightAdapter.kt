package com.ecogo.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ecogo.R
import com.ecogo.data.Activity

class HighlightAdapter(
    private val activities: List<Activity>,
    private val onItemClick: (Activity) -> Unit
) : RecyclerView.Adapter<HighlightAdapter.HighlightViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HighlightViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_highlight, parent, false)
        return HighlightViewHolder(view)
    }

    override fun onBindViewHolder(holder: HighlightViewHolder, position: Int) {
        holder.bind(activities[position], onItemClick)
    }

    override fun getItemCount(): Int = activities.size

    class HighlightViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val icon: TextView = itemView.findViewById(R.id.text_highlight_icon)
        private val title: TextView = itemView.findViewById(R.id.text_highlight_title)
        private val desc: TextView = itemView.findViewById(R.id.text_highlight_desc)

        fun bind(activity: Activity, onItemClick: (Activity) -> Unit) {
            title.text = activity.title
            val dateStr = activity.startTime?.let { it.substring(0, 10) } ?: "TBD"
            desc.text = "$dateStr • ${activity.description.take(20)}"
            
            icon.text = when {
                activity.title.contains("Clean", ignoreCase = true) -> "🧹"
                activity.title.contains("Workshop", ignoreCase = true) -> "🥗"
                activity.title.contains("Run", ignoreCase = true) -> "🏃"
                activity.title.contains("Recycl", ignoreCase = true) -> "♻️"
                activity.title.contains("Friday", ignoreCase = true) -> "🚶"
                activity.title.contains("Container", ignoreCase = true) -> "🍱"
                else -> "🌱"
            }
            
            itemView.setOnClickListener {
                onItemClick(activity)
            }
        }
    }
}
