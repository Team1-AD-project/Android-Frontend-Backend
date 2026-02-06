package com.ecogo.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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

            // 目标值显示（用户进度需要从API单独获取）
            val targetInt = challenge.target.toInt()
            progress.max = targetInt
            progress.progress = 0 // 用户具体进度需要调用 getChallengeProgress API
            progressText.text = "Target: $targetInt ${getTargetUnit(challenge.type)}"

            // Reward
            reward.text = "+${challenge.reward} points"

            // Participants
            participants.text = "${challenge.participants} participants"

            // Type tag - 显示挑战类型
            typeTag.text = when (challenge.type) {
                "GREEN_TRIPS_COUNT" -> "Trip Count"
                "GREEN_TRIPS_DISTANCE" -> "Distance"
                "CARBON_SAVED" -> "Carbon Saved"
                else -> challenge.type
            }

            // Status badge
            when (challenge.status) {
                "ACTIVE" -> {
                    statusBadge.visibility = View.GONE
                }
                "COMPLETED" -> {
                    statusBadge.visibility = View.VISIBLE
                    statusBadge.text = "Completed"
                    statusBadge.setBackgroundResource(R.drawable.badge_background)
                }
                "EXPIRED" -> {
                    statusBadge.visibility = View.VISIBLE
                    statusBadge.text = "Expired"
                }
            }
        }

        /**
         * 根据挑战类型获取单位
         */
        private fun getTargetUnit(type: String): String {
            return when (type) {
                "GREEN_TRIPS_COUNT" -> "trips"
                "GREEN_TRIPS_DISTANCE" -> "km"
                "CARBON_SAVED" -> "g CO₂"
                else -> ""
            }
        }
    }
}
