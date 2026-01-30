package com.ecogo.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ecogo.R
import com.ecogo.data.ShopItem

class ShopItemAdapter(
    private val items: List<ShopItem>,
    private val onPurchase: (ShopItem) -> Unit
) : RecyclerView.Adapter<ShopItemAdapter.ShopViewHolder>() {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShopViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_shop, parent, false)
        return ShopViewHolder(view, onPurchase)
    }
    
    override fun onBindViewHolder(holder: ShopViewHolder, position: Int) {
        holder.bind(items[position])
    }
    
    override fun getItemCount() = items.size
    
    class ShopViewHolder(
        itemView: View,
        private val onPurchase: (ShopItem) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        private val name: TextView = itemView.findViewById(R.id.text_name)
        private val cost: TextView = itemView.findViewById(R.id.text_cost)
        private val type: TextView = itemView.findViewById(R.id.text_type)
        private val button: Button = itemView.findViewById(R.id.button_action)
        
        fun bind(item: ShopItem) {
            name.text = item.name
            cost.text = "${item.cost} pts"
            type.text = item.type
            
            button.text = if (item.owned) "Equipped" else "Purchase"
            button.isEnabled = !item.owned
            
            button.setOnClickListener {
                onPurchase(item)
            }
        }
    }
}
