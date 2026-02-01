package com.ecogo.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ecogo.data.NavLocation
import com.ecogo.databinding.ItemLocationBinding
import com.ecogo.utils.MapUtils

/**
 * 地点搜索结果适配器
 */
class LocationAdapter(
    private var locations: List<NavLocation> = emptyList(),
    private val onLocationClick: (NavLocation) -> Unit
) : RecyclerView.Adapter<LocationAdapter.LocationViewHolder>() {

    fun updateLocations(newLocations: List<NavLocation>) {
        locations = newLocations
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LocationViewHolder {
        val binding = ItemLocationBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return LocationViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LocationViewHolder, position: Int) {
        holder.bind(locations[position])
    }

    override fun getItemCount(): Int = locations.size

    inner class LocationViewHolder(
        private val binding: ItemLocationBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(location: NavLocation) {
            binding.apply {
                textIcon.text = location.icon
                textName.text = location.name
                textAddress.text = location.address
                
                // 如果需要显示距离，可以在这里计算和显示
                textDistance.visibility = android.view.View.GONE
                
                root.setOnClickListener {
                    onLocationClick(location)
                }
            }
        }
    }
}
