package com.ecogo.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.ecogo.R
import com.ecogo.data.BusRoute

class BusRouteAdapter(
    private val routes: List<BusRoute>,
    private val onRouteClick: ((BusRoute) -> Unit)? = null
) : RecyclerView.Adapter<BusRouteAdapter.RouteViewHolder>() {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RouteViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_bus_route, parent, false)
        return RouteViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: RouteViewHolder, position: Int) {
        holder.bind(routes[position], onRouteClick)
    }
    
    override fun getItemCount() = routes.size
    
    class RouteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val colorStripe: View = itemView.findViewById(R.id.view_color_stripe)
        private val number: TextView = itemView.findViewById(R.id.text_number)
        private val name: TextView = itemView.findViewById(R.id.text_name)
        private val destination: TextView = itemView.findViewById(R.id.text_destination)
        private val nextArrival: TextView = itemView.findViewById(R.id.text_next_arrival)
        private val crowding: TextView = itemView.findViewById(R.id.text_crowding)
        private val status: TextView = itemView.findViewById(R.id.text_status)
        private val routeDot: View = itemView.findViewById(R.id.view_route_dot)
        
        fun bind(route: BusRoute, onRouteClick: ((BusRoute) -> Unit)? = null) {
            number.text = route.name
            name.text = route.from.takeIf { it.isNotEmpty() } ?: "Start"
            destination.text = route.to.takeIf { it.isNotEmpty() } ?: "End"
            nextArrival.text = route.time ?: "${route.nextArrival} min"
            crowding.text = route.crowd ?: route.crowding
            status.text = route.status ?: if (route.operational) "On Time" else "Inactive"
            
            // Set click listener
            itemView.setOnClickListener {
                onRouteClick?.invoke(route)
            }
            
            // Set route color
            val routeColor = try {
                android.graphics.Color.parseColor(route.color ?: "#15803D")
            } catch (e: Exception) {
                ContextCompat.getColor(itemView.context, R.color.primary)
            }
            colorStripe.setBackgroundColor(routeColor)
            number.setBackgroundColor(routeColor)
            routeDot.setBackgroundColor(routeColor)
            
            // Set status badge background
            val statusBg = when ((route.status ?: "").lowercase()) {
                "arriving" -> "#DCFCE7"
                "delayed" -> "#FEE2E2"
                "crowded" -> "#FEF3C7"
                else -> "#E0F2FE"
            }
            val statusTextColor = when ((route.status ?: "").lowercase()) {
                "arriving" -> "#15803D"
                "delayed" -> "#B91C1C"
                "crowded" -> "#B45309"
                else -> "#0369A1"
            }
            status.setBackgroundColor(android.graphics.Color.parseColor(statusBg))
            status.setTextColor(android.graphics.Color.parseColor(statusTextColor))
            
            // Set crowding color
            val crowdLevel = (route.crowd ?: route.crowding).lowercase()
            val crowdingColor = when {
                crowdLevel.contains("low") -> ContextCompat.getColor(itemView.context, R.color.crowding_low)
                crowdLevel.contains("med") -> ContextCompat.getColor(itemView.context, R.color.crowding_medium)
                else -> ContextCompat.getColor(itemView.context, R.color.crowding_high)
            }
            crowding.setTextColor(crowdingColor)
        }
    }
}
