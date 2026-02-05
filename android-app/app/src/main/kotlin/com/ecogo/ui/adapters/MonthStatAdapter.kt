package com.ecogo.ui.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ecogo.R
import com.ecogo.ui.fragments.MonthStat

class MonthStatAdapter(
    private var stats: List<MonthStat>
) : RecyclerView.Adapter<MonthStatAdapter.MonthStatViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MonthStatViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_month_stat, parent, false)
        return MonthStatViewHolder(view)
    }

    override fun onBindViewHolder(holder: MonthStatViewHolder, position: Int) {
        holder.bind(stats[position])
    }

    override fun getItemCount(): Int = stats.size

    fun updateData(newStats: List<MonthStat>) {
        stats = newStats
        notifyDataSetChanged()
    }

    class MonthStatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val icon: TextView = itemView.findViewById(R.id.text_stat_icon)
        private val value: TextView = itemView.findViewById(R.id.text_stat_value)
        private val title: TextView = itemView.findViewById(R.id.text_stat_title)
        private val subtitle: TextView = itemView.findViewById(R.id.text_stat_subtitle)

        fun bind(stat: MonthStat) {
            icon.text = stat.icon
            value.text = stat.value
            title.text = stat.title
            subtitle.text = stat.subtitle
            
            // Apply color to icon background
            try {
                icon.setBackgroundColor(Color.parseColor(stat.color + "33")) // Add alpha for transparency
                subtitle.setTextColor(Color.parseColor(stat.color))
            } catch (e: Exception) {
                // Fallback to default colors if parsing fails
            }
        }
    }
}
