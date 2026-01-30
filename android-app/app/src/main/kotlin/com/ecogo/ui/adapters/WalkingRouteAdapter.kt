package com.ecogo.ui.adapters

import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.ecogo.R
import com.ecogo.data.WalkingRoute

class WalkingRouteAdapter(
    private val routes: List<WalkingRoute>
) : RecyclerView.Adapter<WalkingRouteAdapter.WalkingRouteViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WalkingRouteViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_walking_route, parent, false)
        return WalkingRouteViewHolder(view)
    }

    override fun onBindViewHolder(holder: WalkingRouteViewHolder, position: Int) {
        holder.bind(routes[position], position)
    }

    override fun getItemCount(): Int = routes.size

    class WalkingRouteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val background: LinearLayout = itemView.findViewById(R.id.layout_route_bg)
        private val time: TextView = itemView.findViewById(R.id.text_route_time)
        private val title: TextView = itemView.findViewById(R.id.text_route_title)
        private val distance: TextView = itemView.findViewById(R.id.text_route_distance)

        fun bind(route: WalkingRoute, position: Int) {
            time.text = route.time
            title.text = route.title
            distance.text = route.distance

            val colors = listOf(
                intArrayOf(0xFFA7F3D0.toInt(), 0xFF059669.toInt()),
                intArrayOf(0xFFBFDBFE.toInt(), 0xFF3B82F6.toInt()),
                intArrayOf(0xFFFDE68A.toInt(), 0xFFD97706.toInt())
            )
            val gradient = GradientDrawable(
                GradientDrawable.Orientation.BL_TR,
                colors[position % colors.size]
            )
            gradient.cornerRadius = itemView.resources.getDimension(R.dimen.route_card_radius)
            background.background = gradient
        }
    }
}
