package com.ecogo.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ecogo.R
import com.ecogo.data.WalkingRoute

class WalkingRouteAdapter(
    private val routes: List<WalkingRoute>,
    private val onItemClick: (WalkingRoute) -> Unit
) : RecyclerView.Adapter<WalkingRouteAdapter.WalkingRouteViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WalkingRouteViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_walking_route, parent, false)
        return WalkingRouteViewHolder(view)
    }

    override fun onBindViewHolder(holder: WalkingRouteViewHolder, position: Int) {
        holder.bind(routes[position], position, onItemClick)
    }

    override fun getItemCount(): Int = routes.size

    class WalkingRouteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val background: LinearLayout = itemView.findViewById(R.id.layout_route_bg)
        private val time: TextView = itemView.findViewById(R.id.text_route_time)
        private val title: TextView = itemView.findViewById(R.id.text_route_title)
        private val distance: TextView = itemView.findViewById(R.id.text_route_distance)

        fun bind(route: WalkingRoute, position: Int, onItemClick: (WalkingRoute) -> Unit) {
            time.text = route.time
            title.text = route.title
            distance.text = route.distance

            val gradients = listOf(
                R.drawable.bg_route_gradient_green,
                R.drawable.bg_route_gradient_blue,
                R.drawable.bg_route_gradient_orange
            )
            background.setBackgroundResource(gradients[position % gradients.size])
            
            itemView.setOnClickListener {
                onItemClick(route)
            }
        }
    }
}
