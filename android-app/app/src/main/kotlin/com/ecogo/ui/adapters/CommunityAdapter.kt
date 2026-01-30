package com.ecogo.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
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
        
        fun bind(community: Community, position: Int) {
            rank.text = "#$position"
            name.text = community.name
            points.text = "${community.points} pts"
            change.text = if (community.change >= 0) "+${community.change}" else "${community.change}"
            
            val changeColor = if (community.change >= 0) {
                itemView.context.getColor(R.color.success)
            } else {
                itemView.context.getColor(R.color.error)
            }
            change.setTextColor(changeColor)
            
            // Highlight first place
            if (position == 1) {
                itemView.setBackgroundColor(itemView.context.getColor(R.color.primary_light))
            }
        }
    }
}
