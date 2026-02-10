package com.ecogo.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ecogo.data.TripSummaryUi
import com.ecogo.databinding.ItemTripHistoryBinding

class TripHistoryAdapter(
    private var items: List<TripSummaryUi> = emptyList()
) : RecyclerView.Adapter<TripHistoryAdapter.VH>() {

    fun update(newItems: List<TripSummaryUi>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemTripHistoryBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return VH(binding)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(items[position])

    class VH(private val binding: ItemTripHistoryBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(t: TripSummaryUi) {
            binding.textTripRoute.text = t.routeText
            binding.textTime.text = t.timeText
            binding.textPrimary.text = t.primaryText
            binding.textMeta.text = t.metaText
            binding.chipStatus.text = t.statusText
        }
    }
}
