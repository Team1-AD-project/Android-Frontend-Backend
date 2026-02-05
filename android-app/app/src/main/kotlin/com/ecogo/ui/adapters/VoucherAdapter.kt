package com.ecogo.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ecogo.R
import com.ecogo.data.Voucher

class VoucherAdapter(
    private var vouchers: List<Voucher>,
    private val onVoucherClick: (Voucher) -> Unit = {}
) : RecyclerView.Adapter<VoucherAdapter.VoucherViewHolder>() {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VoucherViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_voucher, parent, false)
        return VoucherViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: VoucherViewHolder, position: Int) {
        val voucher = vouchers[position]
        holder.bind(voucher)
        holder.itemView.setOnClickListener {
            onVoucherClick(voucher)
        }
    }
    
    override fun getItemCount() = vouchers.size
    
    fun updateVouchers(newVouchers: List<Voucher>) {
        vouchers = newVouchers
        notifyDataSetChanged()
    }
    
    class VoucherViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val icon: TextView = itemView.findViewById(R.id.text_icon)
        private val name: TextView = itemView.findViewById(R.id.text_name)
        private val description: TextView = itemView.findViewById(R.id.text_description)
        private val cost: TextView = itemView.findViewById(R.id.text_cost)
        private val button: com.google.android.material.button.MaterialButton = itemView.findViewById(R.id.button_redeem)
        private val statusChip: com.google.android.material.chip.Chip = itemView.findViewById(R.id.chip_status)
        
        fun bind(voucher: Voucher) {
            name.text = voucher.name
            description.text = voucher.description
            cost.text = voucher.cost.toString()
            button.text = if (voucher.available) "Redeem" else "Sold Out"
            button.isEnabled = voucher.available
            
            // Set icon based on voucher type
            icon.text = when {
                // Food & Beverage
                voucher.name.contains("Starbucks", ignoreCase = true) -> "☕"
                voucher.name.contains("KFC", ignoreCase = true) -> "🍗"
                voucher.name.contains("McDonald", ignoreCase = true) -> "🍔"
                voucher.name.contains("Subway", ignoreCase = true) -> "🥪"
                voucher.name.contains("Pizza", ignoreCase = true) -> "🍕"
                voucher.name.contains("Bubble Tea", ignoreCase = true) || 
                voucher.name.contains("Tea", ignoreCase = true) -> "🧋"
                voucher.name.contains("Canteen", ignoreCase = true) -> "🍲"
                voucher.name.contains("Foodpanda", ignoreCase = true) -> "🍱"
                
                // Transportation
                voucher.name.contains("Grab", ignoreCase = true) -> "🚗"
                voucher.name.contains("Gojek", ignoreCase = true) -> "🏍️"
                voucher.name.contains("Transit", ignoreCase = true) -> "🚇"
                
                // Retail & Shopping
                voucher.name.contains("Bookstore", ignoreCase = true) || 
                voucher.name.contains("Book", ignoreCase = true) -> "📚"
                voucher.name.contains("FairPrice", ignoreCase = true) -> "🛒"
                voucher.name.contains("Uniqlo", ignoreCase = true) -> "👕"
                voucher.name.contains("Popular", ignoreCase = true) -> "📝"
                voucher.name.contains("Decathlon", ignoreCase = true) -> "⚽"
                
                // Entertainment
                voucher.name.contains("Cinema", ignoreCase = true) || 
                voucher.name.contains("Movie", ignoreCase = true) ||
                voucher.name.contains("GV", ignoreCase = true) ||
                voucher.name.contains("Shaw", ignoreCase = true) -> "🎬"
                voucher.name.contains("ActiveSG", ignoreCase = true) -> "🏃"
                voucher.name.contains("Kinokuniya", ignoreCase = true) -> "📖"
                
                // Health & Wellness
                voucher.name.contains("Guardian", ignoreCase = true) -> "💊"
                voucher.name.contains("Watson", ignoreCase = true) -> "💄"
                voucher.name.contains("Spa", ignoreCase = true) -> "🧖"
                
                // Special
                voucher.name.contains("Mystery", ignoreCase = true) -> "🎁"
                voucher.name.contains("Premium", ignoreCase = true) || 
                voucher.name.contains("Combo", ignoreCase = true) -> "⭐"
                voucher.name.contains("Flash", ignoreCase = true) -> "⚡"
                
                // Chinese
                voucher.name.contains("咖啡", ignoreCase = true) -> "☕"
                voucher.name.contains("食堂", ignoreCase = true) -> "🍲"
                
                else -> "🎫"
            }
            
            // Set icon background color based on brand
            val iconColor = try {
                when {
                    // Food & Beverage colors
                    voucher.name.contains("Starbucks", ignoreCase = true) -> 
                        android.graphics.Color.parseColor("#00704A")
                    voucher.name.contains("KFC", ignoreCase = true) -> 
                        android.graphics.Color.parseColor("#E4002B")
                    voucher.name.contains("McDonald", ignoreCase = true) -> 
                        android.graphics.Color.parseColor("#FFC72C")
                    voucher.name.contains("Subway", ignoreCase = true) -> 
                        android.graphics.Color.parseColor("#009E49")
                    voucher.name.contains("Pizza", ignoreCase = true) -> 
                        android.graphics.Color.parseColor("#E31837")
                    voucher.name.contains("Bubble Tea", ignoreCase = true) || 
                    voucher.name.contains("Tea", ignoreCase = true) -> 
                        android.graphics.Color.parseColor("#8B4513")
                    voucher.name.contains("Canteen", ignoreCase = true) || 
                    voucher.name.contains("食堂", ignoreCase = true) -> 
                        android.graphics.Color.parseColor("#F97316")
                    voucher.name.contains("Foodpanda", ignoreCase = true) -> 
                        android.graphics.Color.parseColor("#D70F64")
                    
                    // Transportation colors
                    voucher.name.contains("Grab", ignoreCase = true) -> 
                        android.graphics.Color.parseColor("#00B14F")
                    voucher.name.contains("Gojek", ignoreCase = true) -> 
                        android.graphics.Color.parseColor("#00AA13")
                    voucher.name.contains("Transit", ignoreCase = true) -> 
                        android.graphics.Color.parseColor("#0066CC")
                    
                    // Retail colors
                    voucher.name.contains("Uniqlo", ignoreCase = true) -> 
                        android.graphics.Color.parseColor("#E30011")
                    voucher.name.contains("Decathlon", ignoreCase = true) -> 
                        android.graphics.Color.parseColor("#0082C3")
                    
                    // Entertainment colors
                    voucher.name.contains("Cinema", ignoreCase = true) || 
                    voucher.name.contains("Movie", ignoreCase = true) ||
                    voucher.name.contains("GV", ignoreCase = true) ||
                    voucher.name.contains("Shaw", ignoreCase = true) -> 
                        android.graphics.Color.parseColor("#9333EA")
                    
                    // Health & Wellness colors
                    voucher.name.contains("Guardian", ignoreCase = true) -> 
                        android.graphics.Color.parseColor("#0066CC")
                    voucher.name.contains("Watson", ignoreCase = true) -> 
                        android.graphics.Color.parseColor("#00A19A")
                    voucher.name.contains("Spa", ignoreCase = true) -> 
                        android.graphics.Color.parseColor("#EC4899")
                    
                    // Special colors
                    voucher.name.contains("Mystery", ignoreCase = true) -> 
                        android.graphics.Color.parseColor("#8B5CF6")
                    voucher.name.contains("Premium", ignoreCase = true) || 
                    voucher.name.contains("Combo", ignoreCase = true) -> 
                        android.graphics.Color.parseColor("#F59E0B")
                    voucher.name.contains("Flash", ignoreCase = true) -> 
                        android.graphics.Color.parseColor("#EF4444")
                    
                    // Default green
                    else -> android.graphics.Color.parseColor("#15803D")
                }
            } catch (e: Exception) {
                itemView.context.getColor(com.ecogo.R.color.primary)
            }
            
            // Apply background color to icon card
            try {
                (itemView.findViewById<View>(R.id.card_icon) as? com.google.android.material.card.MaterialCardView)?.setCardBackgroundColor(iconColor)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            
            // Show popular badge for lower point vouchers
            if (voucher.cost < 600 && voucher.available) {
                statusChip.visibility = View.VISIBLE
                statusChip.text = "🔥 Popular"
            } else if (voucher.name.contains("Flash", ignoreCase = true) && voucher.available) {
                statusChip.visibility = View.VISIBLE
                statusChip.text = "⚡ Flash Sale"
            } else if (voucher.name.contains("Mystery", ignoreCase = true) && voucher.available) {
                statusChip.visibility = View.VISIBLE
                statusChip.text = "🎁 Special"
            } else {
                statusChip.visibility = View.GONE
            }
            
            // Adjust opacity for unavailable vouchers
            itemView.alpha = if (voucher.available) 1.0f else 0.6f
        }
    }
}
