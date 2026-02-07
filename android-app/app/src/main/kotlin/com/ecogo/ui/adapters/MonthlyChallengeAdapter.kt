package com.ecogo.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ecogo.R
import com.ecogo.data.Challenge
import com.ecogo.data.UserChallengeProgress

data class ChallengeWithProgress(
    val challenge: Challenge,
    val progress: UserChallengeProgress? = null
)

class MonthlyChallengeAdapter(
    private var items: List<ChallengeWithProgress>,
    private val onItemClick: (Challenge) -> Unit
) : RecyclerView.Adapter<MonthlyChallengeAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_monthly_challenge, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position], onItemClick)
    }

    override fun getItemCount(): Int = items.size

    fun updateData(newItems: List<ChallengeWithProgress>) {
        items = newItems
        notifyDataSetChanged()
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val icon: TextView = itemView.findViewById(R.id.text_challenge_icon)
        private val title: TextView = itemView.findViewById(R.id.text_challenge_title)
        private val description: TextView = itemView.findViewById(R.id.text_challenge_description)
        private val progressBar: ProgressBar = itemView.findViewById(R.id.progress_challenge)
        private val progressText: TextView = itemView.findViewById(R.id.text_challenge_progress)

        fun bind(item: ChallengeWithProgress, onItemClick: (Challenge) -> Unit) {
            val challenge = item.challenge
            val progress = item.progress

            icon.text = challenge.icon
            title.text = challenge.title
            description.text = challenge.description

            val targetVal = challenge.target
            val unit = getTargetUnit(challenge.type)

            if (progress != null) {
                val percent = progress.progressPercent.toInt().coerceIn(0, 100)
                progressBar.progress = percent

                val currentDisplay = if (progress.current == progress.current.toLong().toDouble()) {
                    "${progress.current.toLong()}"
                } else {
                    String.format("%.1f", progress.current)
                }
                val targetDisplay = if (targetVal == targetVal.toLong().toDouble()) {
                    "${targetVal.toLong()}"
                } else {
                    String.format("%.1f", targetVal)
                }
                progressText.text = "$currentDisplay/$targetDisplay $unit · +${challenge.reward} pts"
            } else {
                progressBar.progress = 0
                val targetDisplay = if (targetVal == targetVal.toLong().toDouble()) {
                    "${targetVal.toLong()}"
                } else {
                    String.format("%.1f", targetVal)
                }
                progressText.text = "0/$targetDisplay $unit · +${challenge.reward} pts"
            }

            itemView.setOnClickListener { onItemClick(challenge) }
        }

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
