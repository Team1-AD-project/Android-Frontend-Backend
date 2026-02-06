package com.ecogo.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ecogo.R
import com.ecogo.data.NavRoute
import com.ecogo.databinding.ItemRouteOptionBinding
import com.ecogo.utils.CarbonCalculator
import com.ecogo.utils.MapUtils

/**
 * 路线选项适配器
 */
class RouteOptionAdapter(
    private var routes: List<NavRoute> = emptyList(),
    private val onRouteClick: (NavRoute) -> Unit
) : RecyclerView.Adapter<RouteOptionAdapter.RouteViewHolder>() {

    fun updateRoutes(newRoutes: List<NavRoute>) {
        routes = newRoutes
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RouteViewHolder {
        val binding = ItemRouteOptionBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return RouteViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RouteViewHolder, position: Int) {
        holder.bind(routes[position])
    }

    override fun getItemCount(): Int = routes.size

    inner class RouteViewHolder(
        private val binding: ItemRouteOptionBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(route: NavRoute) {
            binding.apply {
                // 显示徽章（如果有）
                if (route.badge.isNotEmpty()) {
                    textBadge.text = route.badge
                    textBadge.visibility = View.VISIBLE
                } else {
                    textBadge.visibility = View.GONE
                }
                
                // 交通方式图标
                val modeIcon = when (route.mode) {
                    com.ecogo.data.TransportMode.WALK -> "🚶 步行"
                    com.ecogo.data.TransportMode.CYCLE -> "🚲 骑行"
                    com.ecogo.data.TransportMode.BUS -> "🚌 公交"
                    com.ecogo.data.TransportMode.MIXED -> "🚶🚌 混合"
                }
                textMode.text = modeIcon
                
                // 时间和距离
                textDuration.text = MapUtils.formatDuration(route.duration)
                textDistance.text = String.format("%.1fkm", route.distance)
                
                // 环保数据
                textCarbon.text = CarbonCalculator.formatCarbon(route.carbonEmission)
                textPoints.text = "+${route.points}积分"
                textSavings.text = "节省$${String.format("%.1f", CarbonCalculator.calculateMoneySaved(route.distance))}"
                
                // 推荐路线高亮
                if (route.isRecommended) {
                    root.setCardBackgroundColor(
                        root.context.getColor(R.color.background)
                    )
                } else {
                    root.setCardBackgroundColor(
                        root.context.getColor(android.R.color.white)
                    )
                }
                
                root.setOnClickListener {
                    onRouteClick(route)
                }
            }
        }
    }
}
