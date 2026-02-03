package com.ecogo.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ecogo.R
import com.ecogo.data.Challenge

class ChallengeAdapter(
    private var challenges: List<Challenge>,
    private val onChallengeClick: (Challenge) -> Unit
) : RecyclerView.Adapter<ChallengeAdapter.ChallengeViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChallengeViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_challenge, parent, false)
        return ChallengeViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChallengeViewHolder, position: Int) {
        val challenge = challenges[position]
        holder.bind(challenge)
        holder.itemView.setOnClickListener {
            onChallengeClick(challenge)
        }
    }

    override fun getItemCount() = challenges.size

    fun updateChallenges(newChallenges: List<Challenge>) {
        challenges = newChallenges
        notifyDataSetChanged()
    }

    class ChallengeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val icon: TextView = itemView.findViewById(R.id.text_icon)
        private val title: TextView = itemView.findViewById(R.id.text_title)
        private val description: TextView = itemView.findViewById(R.id.text_description)
        private val progress: ProgressBar = itemView.findViewById(R.id.progress_challenge)
        private val progressText: TextView = itemView.findViewById(R.id.text_progress)
        private val reward: TextView = itemView.findViewById(R.id.text_reward)
        private val participants: TextView = itemView.findViewById(R.id.text_participants)
        private val typeTag: TextView = itemView.findViewById(R.id.text_type_tag)
        private val statusBadge: TextView = itemView.findViewById(R.id.badge_status)

        fun bind(challenge: Challenge) {
            icon.text = challenge.icon
            title.text = challenge.title
            description.text = challenge.description
            
            // 进度
            val progressPercent = if (challenge.target > 0) {
                ((challenge.current.toFloat() / challenge.target) * 100).toInt()
            } else 0
            
            progress.max = challenge.target
            progress.progress = challenge.current
            progressText.text = "${challenge.current} / ${challenge.target} (${progressPercent}%)"
            
            // 奖励
            reward.text = "+${challenge.reward} 积分"
            
            // 参与人数
            participants.text = "${challenge.participants} 人参与"
            
            // 类型标签
            typeTag.text = when (challenge.type) {
                "INDIVIDUAL" -> "个人"
                "TEAM" -> "组队"
                "FACULTY" -> "学院"
                else -> challenge.type
            }
            
            // 状态徽章
            when (challenge.status) {
                "ACTIVE" -> {
                    statusBadge.visibility = View.GONE
                }
                "COMPLETED" -> {
                    statusBadge.visibility = View.VISIBLE
                    statusBadge.text = "已完成"
                    statusBadge.setBackgroundResource(R.drawable.badge_background)
                }
                "EXPIRED" -> {
                    statusBadge.visibility = View.VISIBLE
                    statusBadge.text = "已过期"
                }
            }
        }
    }
}
