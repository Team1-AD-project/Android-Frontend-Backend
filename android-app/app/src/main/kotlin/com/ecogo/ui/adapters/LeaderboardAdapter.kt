package com.ecogo.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.ecogo.R
import com.ecogo.data.Ranking

class LeaderboardAdapter(
    private val rankings: List<Ranking>
) : RecyclerView.Adapter<LeaderboardAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_ranking, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(rankings[position])
    }

    override fun getItemCount() = rankings.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textRank: TextView = itemView.findViewById(R.id.text_rank)
        private val textNickname: TextView = itemView.findViewById(R.id.text_nickname)
        private val textUserId: TextView = itemView.findViewById(R.id.text_user_id)
        private val textSteps: TextView = itemView.findViewById(R.id.text_steps)
        private val textVipBadge: TextView = itemView.findViewById(R.id.text_vip_badge)

        fun bind(ranking: Ranking) {
            textRank.text = "#${ranking.rank}"
            textNickname.text = ranking.nickname.ifBlank { "N/A" }
            textUserId.text = ranking.userId
            textSteps.text = "%,d".format(ranking.steps)
            textVipBadge.visibility = if (ranking.isVip) View.VISIBLE else View.GONE

            val rankColor = when (ranking.rank) {
                1 -> ContextCompat.getColor(itemView.context, R.color.primary)
                2 -> ContextCompat.getColor(itemView.context, R.color.text_secondary)
                3 -> 0xFF8B4513.toInt()
                else -> ContextCompat.getColor(itemView.context, R.color.text_secondary)
            }
            textRank.setTextColor(rankColor)
        }
    }
}
