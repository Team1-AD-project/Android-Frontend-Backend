package com.ecogo.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ecogo.R
import com.ecogo.data.Achievement

class AchievementAdapter(
    private val achievements: List<Achievement>
) : RecyclerView.Adapter<AchievementAdapter.AchievementViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AchievementViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_achievement, parent, false)
        return AchievementViewHolder(view)
    }

    override fun onBindViewHolder(holder: AchievementViewHolder, position: Int) {
        holder.bind(achievements[position])
    }

    override fun getItemCount(): Int = achievements.size

    class AchievementViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val icon: TextView = itemView.findViewById(R.id.text_badge_icon)
        private val name: TextView = itemView.findViewById(R.id.text_badge_name)
        private val desc: TextView = itemView.findViewById(R.id.text_badge_desc)

        fun bind(achievement: Achievement) {
            name.text = achievement.name
            desc.text = achievement.description
            icon.text = achievement.name.firstOrNull()?.uppercase() ?: "★"

            if (!achievement.unlocked) {
                itemView.alpha = 0.5f
            } else {
                itemView.alpha = 1f
            }
        }
    }
}
