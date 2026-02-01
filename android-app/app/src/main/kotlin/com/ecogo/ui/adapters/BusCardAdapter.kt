package com.ecogo.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ecogo.data.BusInfo
import com.ecogo.databinding.ItemBusCardBinding

/**
 * 公交车信息适配器
 */
class BusCardAdapter(
    private var busList: List<BusInfo> = emptyList()
) : RecyclerView.Adapter<BusCardAdapter.BusViewHolder>() {

    fun updateBusList(newBusList: List<BusInfo>) {
        busList = newBusList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BusViewHolder {
        val binding = ItemBusCardBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return BusViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BusViewHolder, position: Int) {
        holder.bind(busList[position])
    }

    override fun getItemCount(): Int = busList.size

    inner class BusViewHolder(
        private val binding: ItemBusCardBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(busInfo: BusInfo) {
            binding.apply {
                textBusId.text = busInfo.busId
                textDestination.text = "前往 ${busInfo.destination}"
                textEta.text = "${busInfo.etaMinutes}分钟"
                textPlate.text = busInfo.plateNumber
                textCrowd.text = "${busInfo.crowdLevel}拥挤度"
                
                // 状态显示
                textStatus.text = when (busInfo.status) {
                    "arriving" -> "⚡ 即将到达"
                    "coming" -> "🚌 即将到站"
                    "delayed" -> "⚠️ 延误"
                    else -> ""
                }
                
                // 根据拥挤度设置颜色
                // TODO: 可以根据拥挤度设置不同的背景色
            }
        }
    }
}
