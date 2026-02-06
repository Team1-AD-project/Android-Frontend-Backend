package com.ecogo.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.ecogo.R
import com.ecogo.data.Community

class CommunityAdapter(private val communities: List<Community>) :
    RecyclerView.Adapter<CommunityAdapter.CommunityViewHolder>() {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommunityViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_community, parent, false)
        return CommunityViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: CommunityViewHolder, position: Int) {
        holder.bind(communities[position], position + 1)
    }
    
    override fun getItemCount() = communities.size
    
    class CommunityViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val rank: TextView = itemView.findViewById(R.id.text_rank)
        private val name: TextView = itemView.findViewById(R.id.text_name)
        private val points: TextView = itemView.findViewById(R.id.text_points)
        private val change: TextView = itemView.findViewById(R.id.text_change)
        private val progress: View = itemView.findViewById(R.id.view_progress)
        
        fun bind(community: Community, position: Int) {
            rank.text = "#$position"
            name.text = community.name
            points.text = community.points.toString()
            
            val changeText = if (community.change >= 0) "+${community.change}%" else "${community.change}%"
            change.text = changeText
            
            val changeColor = if (community.change >= 0) {
                android.graphics.Color.parseColor("#16A34A")
            } else {
                ContextCompat.getColor(itemView.context, R.color.error)
            }
            change.setTextColor(changeColor)
            
            val rankColor = if (position <= 3) {
                ContextCompat.getColor(itemView.context, R.color.primary)
            } else {
                ContextCompat.getColor(itemView.context, R.color.text_secondary)
            }
            rank.setTextColor(rankColor)
            
            val progressPercentage = (community.points.toFloat() / 5000f).coerceIn(0f, 1f)
            val params = progress.layoutParams
            params.width = (itemView.width * progressPercentage * 0.5).toInt()
            progress.layoutParams = params
        }
    }
}
