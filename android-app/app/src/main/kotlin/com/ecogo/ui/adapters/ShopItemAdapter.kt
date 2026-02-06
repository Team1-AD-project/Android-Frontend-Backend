package com.ecogo.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.ecogo.R
import com.ecogo.data.ShopItem
import com.google.android.material.card.MaterialCardView

class ShopItemAdapter(
    private var items: List<ShopItem>,
    private val onItemClick: (ShopItem) -> Unit
) : RecyclerView.Adapter<ShopItemAdapter.ShopViewHolder>() {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShopViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_shop, parent, false)
        return ShopViewHolder(view, onItemClick)
    }
    
    override fun onBindViewHolder(holder: ShopViewHolder, position: Int) {
        holder.bind(items[position])
    }
    
    override fun getItemCount() = items.size
    
    fun updateItems(newItems: List<ShopItem>) {
        items = newItems
        notifyDataSetChanged()
    }
    
    class ShopViewHolder(
        itemView: View,
        private val onItemClick: (ShopItem) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        private val card = itemView as MaterialCardView
        private val icon: TextView = itemView.findViewById(R.id.text_icon)
        private val name: TextView = itemView.findViewById(R.id.text_name)
        private val cost: TextView = itemView.findViewById(R.id.text_cost)
        private val status: TextView = itemView.findViewById(R.id.text_status)
        private val check: View = itemView.findViewById(R.id.image_check)
        
        fun bind(item: ShopItem) {
            name.text = item.name
            cost.text = "${item.cost} pts"
            
            // 图标映射（覆盖全部 11 种商品）
            icon.text = when (item.id) {
                "hat_grad" -> "🎓"
                "hat_cap" -> "🧢"
                "hat_helmet" -> "⛑️"
                "hat_beret" -> "🎩"
                "glasses_sun" -> "🕶️"
                "face_goggles" -> "🥽"
                "shirt_nus" -> "👕"
                "shirt_hoodie" -> "🧥"
                "body_plaid" -> "👔"
                "body_suit" -> "🤵"
                "body_coat" -> "🥼"
                else -> "🎁"
            }
            
            // 状态显示
            when {
                item.equipped -> {
                    status.visibility = View.VISIBLE
                    status.text = itemView.context.getString(R.string.profile_equipped)
                    cost.visibility = View.GONE
                    check.visibility = View.VISIBLE
                    card.strokeWidth = 4
                    card.strokeColor = ContextCompat.getColor(itemView.context, R.color.primary)
                }
                item.owned -> {
                    status.visibility = View.VISIBLE
                    status.text = "Owned"
                    cost.visibility = View.GONE
                    check.visibility = View.GONE
                    card.strokeWidth = 0
                }
                else -> {
                    status.visibility = View.GONE
                    cost.visibility = View.VISIBLE
                    check.visibility = View.GONE
                    card.strokeWidth = 0
                }
            }
            
            itemView.setOnClickListener {
                onItemClick(item)
            }
        }
    }
}
