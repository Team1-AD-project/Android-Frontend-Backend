package com.ecogo.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ecogo.R
import com.ecogo.data.Activity

class HighlightAdapter(
    private val activities: List<Activity>
) : RecyclerView.Adapter<HighlightAdapter.HighlightViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HighlightViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_highlight, parent, false)
        return HighlightViewHolder(view)
    }

    override fun onBindViewHolder(holder: HighlightViewHolder, position: Int) {
        holder.bind(activities[position])
    }

    override fun getItemCount(): Int = activities.size

    class HighlightViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val icon: TextView = itemView.findViewById(R.id.text_highlight_icon)
        private val title: TextView = itemView.findViewById(R.id.text_highlight_title)
        private val desc: TextView = itemView.findViewById(R.id.text_highlight_desc)

        fun bind(activity: Activity) {
            title.text = activity.title
            desc.text = activity.description
            icon.text = activity.title.firstOrNull()?.uppercase() ?: "A"
        }
    }
}
