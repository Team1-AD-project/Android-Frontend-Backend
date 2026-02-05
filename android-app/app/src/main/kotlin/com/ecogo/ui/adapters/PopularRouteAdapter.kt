package com.ecogo.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ecogo.R
import com.ecogo.data.BusRoute

class PopularRouteAdapter(
    private var routes: List<BusRoute>,
    private val onItemClick: (BusRoute) -> Unit
) : RecyclerView.Adapter<PopularRouteAdapter.RouteViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RouteViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_popular_route, parent, false)
        return RouteViewHolder(view)
    }

    override fun onBindViewHolder(holder: RouteViewHolder, position: Int) {
        holder.bind(routes[position], onItemClick)
    }

    override fun getItemCount(): Int = routes.size

    fun updateData(newRoutes: List<BusRoute>) {
        routes = newRoutes
        notifyDataSetChanged()
    }

    class RouteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val routeNumber: TextView = itemView.findViewById(R.id.text_route_number)
        private val routeName: TextView = itemView.findViewById(R.id.text_route_name)
        private val routeUsers: TextView = itemView.findViewById(R.id.text_route_users)
        private val routeCo2: TextView = itemView.findViewById(R.id.text_route_co2)
        private val routeTrend: TextView = itemView.findViewById(R.id.text_route_trend)

        fun bind(route: BusRoute, onItemClick: (BusRoute) -> Unit) {
            routeNumber.text = route.name
            routeName.text = "${route.from} to ${route.to}"
            
            // Mock data for demonstration
            val trips = (200..500).random()
            val co2Saved = String.format("%.1f", trips * 0.13)
            val trend = (5..20).random()
            
            routeUsers.text = "$trips trips"
            routeCo2.text = "$co2Saved kg CO₂"
            routeTrend.text = "+$trend%"
            
            itemView.setOnClickListener {
                onItemClick(route)
            }
        }
    }
}
