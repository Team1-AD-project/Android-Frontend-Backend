package com.ecogo.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ecogo.R
import com.ecogo.data.Voucher

class VoucherAdapter(private val vouchers: List<Voucher>) :
    RecyclerView.Adapter<VoucherAdapter.VoucherViewHolder>() {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VoucherViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_voucher, parent, false)
        return VoucherViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: VoucherViewHolder, position: Int) {
        holder.bind(vouchers[position])
    }
    
    override fun getItemCount() = vouchers.size
    
    class VoucherViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val name: TextView = itemView.findViewById(R.id.text_name)
        private val cost: TextView = itemView.findViewById(R.id.text_cost)
        private val description: TextView = itemView.findViewById(R.id.text_description)
        private val button: Button = itemView.findViewById(R.id.button_redeem)
        
        fun bind(voucher: Voucher) {
            name.text = voucher.name
            cost.text = "${voucher.cost} pts"
            description.text = voucher.description
            button.isEnabled = voucher.available
            button.text = if (voucher.available) "Redeem" else "Redeemed"
        }
    }
}
