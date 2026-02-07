package com.ecogo.app.ui.map

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ecogo.R
import com.ecogo.app.data.model.RouteStep

/**
 * 路线步骤 RecyclerView 适配器
 */
class RouteStepAdapter : RecyclerView.Adapter<RouteStepAdapter.StepViewHolder>() {

    private var steps: List<RouteStep> = emptyList()

    fun setSteps(newSteps: List<RouteStep>) {
        steps = newSteps
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StepViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_route_step, parent, false)
        return StepViewHolder(view)
    }

    override fun onBindViewHolder(holder: StepViewHolder, position: Int) {
        holder.bind(steps[position])
    }

    override fun getItemCount(): Int = steps.size

    class StepViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivStepIcon: ImageView = itemView.findViewById(R.id.ivStepIcon)
        private val tvStepInstruction: TextView = itemView.findViewById(R.id.tvStepInstruction)
        private val tvStepDistance: TextView = itemView.findViewById(R.id.tvStepDistance)

        // 公交详情视图
        private val layoutTransitDetails: View = itemView.findViewById(R.id.layoutTransitDetails)
        private val tvLineName: TextView = itemView.findViewById(R.id.tvLineName)
        private val tvDepartureStop: TextView = itemView.findViewById(R.id.tvDepartureStop)
        private val tvArrivalStop: TextView = itemView.findViewById(R.id.tvArrivalStop)
        private val tvHeadsign: TextView = itemView.findViewById(R.id.tvHeadsign)

        fun bind(step: RouteStep) {
            // 设置步骤图标
            val iconRes = when (step.travel_mode) {
                "WALKING" -> R.drawable.ic_walking
                "TRANSIT" -> R.drawable.ic_transit
                "DRIVING" -> R.drawable.ic_driving
                "BICYCLING" -> R.drawable.ic_bicycling
                else -> R.drawable.ic_walking
            }
            ivStepIcon.setImageResource(iconRes)

            // 设置步骤说明
            tvStepInstruction.text = step.instruction

            // 设置距离和时长
            val distanceText = if (step.distance >= 1000) {
                String.format("%.1f 公里", step.distance / 1000.0)
            } else {
                String.format("%.0f 米", step.distance)
            }
            val durationText = if (step.duration >= 60) {
                String.format("%d 分钟", step.duration / 60)
            } else {
                String.format("%d 秒", step.duration)
            }
            tvStepDistance.text = "$distanceText · $durationText"

            // 显示公交详情（仅 TRANSIT 模式）
            if (step.travel_mode == "TRANSIT" && step.transit_details != null) {
                layoutTransitDetails.visibility = View.VISIBLE

                val transit = step.transit_details

                // 线路名称
                val lineName = transit.line_short_name ?: transit.line_name
                tvLineName.text = lineName

                // 上车站
                tvDepartureStop.text = "上车: ${transit.departure_stop}"

                // 下车站和经过站数
                val arrivalText = if (transit.num_stops > 0) {
                    "下车: ${transit.arrival_stop} (经过 ${transit.num_stops} 站)"
                } else {
                    "下车: ${transit.arrival_stop}"
                }
                tvArrivalStop.text = arrivalText

                // 方向标识
                if (transit.headsign != null) {
                    tvHeadsign.visibility = View.VISIBLE
                    tvHeadsign.text = "往${transit.headsign}方向"
                } else {
                    tvHeadsign.visibility = View.GONE
                }
            } else {
                layoutTransitDetails.visibility = View.GONE
            }
        }
    }
}
