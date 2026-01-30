package com.ecogo.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.ecogo.R
import com.ecogo.data.HistoryItem

class HistoryAdapter(
    private val history: List<HistoryItem>
) : RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_history, parent, false)
        return HistoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        holder.bind(history[position])
    }

    override fun getItemCount(): Int = history.size

    class HistoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val action: TextView = itemView.findViewById(R.id.text_history_action)
        private val time: TextView = itemView.findViewById(R.id.text_history_time)
        private val points: TextView = itemView.findViewById(R.id.text_history_points)

        fun bind(item: HistoryItem) {
            action.text = item.action
            time.text = item.time
            points.text = item.points

            val colorRes = if (item.type == "earn") R.color.primary else R.color.error
            points.setTextColor(ContextCompat.getColor(itemView.context, colorRes))
        }
    }
}
