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
        private val button: com.google.android.material.button.MaterialButton = itemView.findViewById(R.id.button_redeem)
        
        fun bind(voucher: Voucher) {
            name.text = voucher.name
            description.text = voucher.description
            button.text = "${voucher.cost} pts"
            button.isEnabled = voucher.available
            
            icon.text = when {
                voucher.name.contains("Starbucks", ignoreCase = true) -> "☕"
                voucher.name.contains("Subway", ignoreCase = true) -> "🥪"
                voucher.name.contains("Canteen", ignoreCase = true) -> "🍲"
                voucher.name.contains("Tea", ignoreCase = true) -> "🧋"
                else -> "🎁"
            }
            
            val iconColor = try {
                if (voucher.name.contains("Starbucks")) android.graphics.Color.parseColor("#00704A")
                else if (voucher.name.contains("Subway")) android.graphics.Color.parseColor("#FFC72C")
                else if (voucher.name.contains("Canteen")) android.graphics.Color.parseColor("#F97316")
                else if (voucher.name.contains("Tea")) android.graphics.Color.parseColor("#DC2626")
                else android.graphics.Color.parseColor("#15803D")
            } catch (e: Exception) {
                itemView.context.getColor(com.ecogo.R.color.primary)
            }
            icon.setBackgroundColor(iconColor)
            
            itemView.alpha = if (voucher.available) 1.0f else 0.6f
        }
    }
}
