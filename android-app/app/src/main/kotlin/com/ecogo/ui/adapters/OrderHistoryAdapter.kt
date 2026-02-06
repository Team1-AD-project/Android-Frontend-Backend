package com.ecogo.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ecogo.data.OrderSummaryUi
import com.ecogo.databinding.ItemOrderHistoryBinding
import com.ecogo.utils.TimeFormat


class OrderHistoryAdapter(
    private var items: List<OrderSummaryUi> = emptyList()
) : RecyclerView.Adapter<OrderHistoryAdapter.VH>() {

    fun update(newItems: List<OrderSummaryUi>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemOrderHistoryBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return VH(binding)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(items[position])

    class VH(private val binding: ItemOrderHistoryBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(o: OrderSummaryUi) {
            binding.textOrderNumber.text = o.orderNumber ?: o.id

            binding.textDate.text = TimeFormat.toSgTime(o.createdAt)

            val amt = o.finalAmount ?: 0.0
            binding.textAmount.text = String.format("$%.2f", amt)

            val count = o.itemCount ?: 0
            val redemption = if (o.isRedemption == true) "Redemption" else "Purchase"
            binding.textMeta.text = "$count items · $redemption"

            val status = (o.status ?: "UNKNOWN").uppercase()
            binding.chipStatus.text = status
        }
    }
}
