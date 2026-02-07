package com.ecogo.app.ui.map

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.ecogo.R
import com.ecogo.app.data.model.RouteAlternative
import com.ecogo.databinding.ItemRouteOptionBinding

/**
 * 路线选择适配器
 */
class RouteOptionAdapter(
    private val onRouteSelected: (RouteAlternative) -> Unit
) : RecyclerView.Adapter<RouteOptionAdapter.RouteOptionViewHolder>() {

    private var routes: List<RouteAlternative> = emptyList()
    private var selectedIndex: Int = 0

    fun setRoutes(newRoutes: List<RouteAlternative>) {
        routes = newRoutes
        selectedIndex = 0
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RouteOptionViewHolder {
        val binding = ItemRouteOptionBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return RouteOptionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RouteOptionViewHolder, position: Int) {
        holder.bind(routes[position], position == selectedIndex)
    }

    override fun getItemCount(): Int = routes.size

    inner class RouteOptionViewHolder(
        private val binding: ItemRouteOptionBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(route: RouteAlternative, isSelected: Boolean) {
            // 路线摘要
            binding.tvRouteSummary.text = route.summary

            // 时长
            binding.tvRouteTime.text = "${route.estimated_duration} 分钟"

            // 距离
            val distanceText = if (route.total_distance >= 1.0) {
                String.format("%.1f 公里", route.total_distance)
            } else {
                String.format("%.0f 米", route.total_distance * 1000)
            }
            binding.tvRouteDistance.text = distanceText

            // 碳排放
            binding.tvRouteCarbon.text = String.format("%.2f kg", route.total_carbon)

            // 选中状态
            if (isSelected) {
                binding.cardRouteOption.strokeWidth = 6
                binding.cardRouteOption.strokeColor = ContextCompat.getColor(
                    binding.root.context,
                    R.color.green_primary
                )
            } else {
                binding.cardRouteOption.strokeWidth = 0
            }

            // 点击事件
            binding.root.setOnClickListener {
                val oldSelectedIndex = selectedIndex
                selectedIndex = adapterPosition
                notifyItemChanged(oldSelectedIndex)
                notifyItemChanged(selectedIndex)
                onRouteSelected(route)
            }
        }
    }
}
