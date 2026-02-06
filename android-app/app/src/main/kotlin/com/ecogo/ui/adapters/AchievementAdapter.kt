package com.ecogo.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.ecogo.R
import com.ecogo.data.Achievement
import com.google.android.material.card.MaterialCardView

class AchievementAdapter(
    private val achievements: List<Achievement>,
    private val equippedBadgeId: String = "none",
    private val onBadgeClick: ((String) -> Unit)? = null
) : RecyclerView.Adapter<AchievementAdapter.AchievementViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AchievementViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_achievement, parent, false)
        return AchievementViewHolder(view)
    }

    override fun onBindViewHolder(holder: AchievementViewHolder, position: Int) {
        holder.bind(achievements[position], equippedBadgeId, onBadgeClick)
    }

    override fun getItemCount(): Int = achievements.size

    class AchievementViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val card: MaterialCardView = itemView as MaterialCardView
        private val icon: TextView = itemView.findViewById(R.id.text_badge_icon)
        private val name: TextView = itemView.findViewById(R.id.text_badge_name)
        private val desc: TextView = itemView.findViewById(R.id.text_badge_desc)

        fun bind(achievement: Achievement, equippedBadgeId: String, onBadgeClick: ((String) -> Unit)?) {
            name.text = achievement.name
            
            val isEquipped = equippedBadgeId == achievement.id

            // 成就图标使用 emoji 展示
            icon.text = when (achievement.id) {
                // 新人入门
                "a1" -> "🚌"   // First Ride
                "a2" -> "✅"   // First Check-in
                "a3" -> "🎪"   // First Activity
                "a4" -> "📝"   // Profile Complete
                // 连续打卡
                "a5" -> "⚡"   // Week Warrior
                "a6" -> "🔄"   // Habit Builder
                "a7" -> "📅"   // Month Master
                "a8" -> "💪"   // Iron Will
                // 积分里程碑
                "a9" -> "💯"   // Century Club
                "a10" -> "💰"  // Points Pro
                "a11" -> "💎"  // Points Legend
                // 出行方式
                "a12" -> "🚴"  // Cycling Champion
                "a13" -> "🚶"  // Walk the Talk
                "a14" -> "🚍"  // Bus Regular
                "a15" -> "♻️"  // Carbon Cutter
                // 社交社区
                "a16" -> "🦋"  // Social Butterfly
                "a17" -> "🤝"  // Team Player
                "a18" -> "👥"  // Community Leader
                // 特殊成就
                "a19" -> "🎫"  // Master Saver
                "a20" -> "🏆"  // Eco Champion
                else -> "🏅"
            }

            // 描述：显示佩戴状态或原始描述
            when {
                isEquipped -> {
                    desc.text = "✅ Wearing"
                    desc.setTextColor(ContextCompat.getColor(itemView.context, R.color.primary))
                }
                !achievement.unlocked -> {
                    desc.text = "🔒 Locked"
                    desc.setTextColor(ContextCompat.getColor(itemView.context, R.color.text_secondary))
                }
                else -> {
                    desc.text = achievement.description
                    desc.setTextColor(ContextCompat.getColor(itemView.context, R.color.text_secondary))
                }
            }

            // 视觉状态
            if (!achievement.unlocked) {
                itemView.alpha = 0.5f
                card.strokeWidth = 0
            } else if (isEquipped) {
                itemView.alpha = 1f
                card.strokeWidth = 4
                card.strokeColor = ContextCompat.getColor(itemView.context, R.color.primary)
            } else {
                itemView.alpha = 1f
                card.strokeWidth = 0
            }
            
            itemView.setOnClickListener {
                onBadgeClick?.invoke(achievement.id)
            }
        }
    }
}
