package com.ecogo.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ecogo.R

class MilestoneAdapter(
    private var milestones: List<Milestone>
) : RecyclerView.Adapter<MilestoneAdapter.MilestoneViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MilestoneViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_milestone, parent, false)
        return MilestoneViewHolder(view)
    }

    override fun onBindViewHolder(holder: MilestoneViewHolder, position: Int) {
        holder.bind(milestones[position], position == itemCount - 1)
    }

    override fun getItemCount(): Int = milestones.size

    fun updateData(newMilestones: List<Milestone>) {
        milestones = newMilestones
        notifyDataSetChanged()
    }

    class MilestoneViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val icon: TextView = itemView.findViewById(R.id.text_milestone_icon)
        private val title: TextView = itemView.findViewById(R.id.text_milestone_title)
        private val description: TextView = itemView.findViewById(R.id.text_milestone_description)
        private val date: TextView = itemView.findViewById(R.id.text_milestone_date)
        private val reward: TextView = itemView.findViewById(R.id.text_milestone_reward)
        private val timelineLine: View = itemView.findViewById(R.id.view_timeline_line)

        fun bind(milestone: Milestone, isLast: Boolean) {
            icon.text = milestone.icon
            title.text = milestone.title
            description.text = milestone.description
            date.text = milestone.date
            
            if (milestone.reward.isNotEmpty()) {
                reward.text = milestone.reward
                reward.visibility = View.VISIBLE
            } else {
                reward.visibility = View.GONE
            }
            
            // Hide timeline line for last item
            timelineLine.visibility = if (isLast) View.GONE else View.VISIBLE
        }
    }
}

data class Milestone(
    val icon: String,
    val title: String,
    val description: String,
    val date: String,
    val reward: String = ""
)
