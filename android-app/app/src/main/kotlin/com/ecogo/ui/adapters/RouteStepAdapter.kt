package com.ecogo.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ecogo.R
import com.ecogo.data.RouteStep
import com.ecogo.databinding.ItemRouteStepBinding
import com.ecogo.utils.MapUtils

/**
 * 路线步骤适配器
 */
class RouteStepAdapter(
    private var steps: List<RouteStep> = emptyList()
) : RecyclerView.Adapter<RouteStepAdapter.StepViewHolder>() {

    fun updateSteps(newSteps: List<RouteStep>) {
        steps = newSteps
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StepViewHolder {
        val binding = ItemRouteStepBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return StepViewHolder(binding)
    }

    override fun onBindViewHolder(holder: StepViewHolder, position: Int) {
        holder.bind(steps[position], position + 1)
    }

    override fun getItemCount(): Int = steps.size

    inner class StepViewHolder(
        private val binding: ItemRouteStepBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(step: RouteStep, stepNumber: Int) {
            binding.apply {
                // 交通方式图标
                val iconRes = when (step.mode) {
                    com.ecogo.data.TransportMode.WALK -> R.drawable.ic_walking
                    com.ecogo.data.TransportMode.CYCLE -> R.drawable.ic_bicycling
                    com.ecogo.data.TransportMode.BUS -> R.drawable.ic_transit
                    com.ecogo.data.TransportMode.MIXED -> R.drawable.ic_walking
                }
                ivStepIcon.setImageResource(iconRes)

                // 步骤说明
                tvStepInstruction.text = step.instruction

                // 距离和时长
                val distanceStr = MapUtils.formatDistance(step.distance)
                val durationStr = if (step.duration >= 60) {
                    "${step.duration / 60}分钟"
                } else {
                    "${step.duration}秒"
                }
                tvStepDistance.text = "$distanceStr • $durationStr"
            }
        }
    }
}
