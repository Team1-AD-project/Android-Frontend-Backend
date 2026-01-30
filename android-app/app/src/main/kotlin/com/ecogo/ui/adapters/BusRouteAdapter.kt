package com.ecogo.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.ecogo.R
import com.ecogo.data.BusRoute

class BusRouteAdapter(private val routes: List<BusRoute>) :
    RecyclerView.Adapter<BusRouteAdapter.RouteViewHolder>() {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RouteViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_bus_route, parent, false)
        return RouteViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: RouteViewHolder, position: Int) {
        holder.bind(routes[position])
    }
    
    override fun getItemCount() = routes.size
    
    class RouteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val number: TextView = itemView.findViewById(R.id.text_number)
        private val name: TextView = itemView.findViewById(R.id.text_name)
        private val nextArrival: TextView = itemView.findViewById(R.id.text_next_arrival)
        private val crowding: TextView = itemView.findViewById(R.id.text_crowding)
        private val status: TextView = itemView.findViewById(R.id.text_status)
        
        fun bind(route: BusRoute) {
            number.text = route.name
            name.text = if (route.from.isNotEmpty() && route.to.isNotEmpty()) {
                "${route.from} -> ${route.to}"
            } else {
                route.name
            }
            nextArrival.text = route.time ?: "${route.nextArrival} min"
            crowding.text = route.crowd ?: route.crowding
            status.text = route.status ?: if (route.operational) "Active" else "Inactive"
            
            // Set crowding color
            val crowdLevel = (route.crowd ?: route.crowding).lowercase()
            val crowdingColor = when {
                crowdLevel.contains("low") -> ContextCompat.getColor(itemView.context, R.color.crowding_low)
                crowdLevel.contains("med") -> ContextCompat.getColor(itemView.context, R.color.crowding_medium)
                else -> ContextCompat.getColor(itemView.context, R.color.crowding_high)
            }
            crowding.setTextColor(crowdingColor)
            
            // Set status color
            val statusColor = if ((route.status ?: "").lowercase().contains("delay")) {
                ContextCompat.getColor(itemView.context, R.color.error)
            } else {
                ContextCompat.getColor(itemView.context, R.color.success)
            }
            status.setTextColor(statusColor)
        }
    }
}
