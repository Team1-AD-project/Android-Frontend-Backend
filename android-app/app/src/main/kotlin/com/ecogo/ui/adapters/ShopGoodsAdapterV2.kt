package com.ecogo.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ecogo.api.GoodsDto
import com.ecogo.databinding.ItemVoucherBinding

class ShopGoodsAdapterV2(
    private var items: List<GoodsDto> = emptyList(),
    private val onItemClick: (GoodsDto) -> Unit
) : RecyclerView.Adapter<ShopGoodsAdapterV2.VH>() {

    fun update(newItems: List<GoodsDto>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemVoucherBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return VH(binding, onItemClick)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(items[position])
    }

    class VH(
        private val binding: ItemVoucherBinding,
        private val onItemClick: (GoodsDto) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: GoodsDto) {
            // 文本
            binding.textName.text = item.name
            binding.textDescription.text = item.description ?: ""

            // Shop 列表隐藏 Redeem
            binding.buttonRedeem.visibility = View.GONE

            // Shop 用 price
            val price = item.price ?: 0.0
            binding.textCost.text = String.format("%.2f", price)

            // VIP 标签（有就显示）
            if (item.vipLevelRequired > 0) {
                binding.chipStatus.visibility = View.VISIBLE
                binding.chipStatus.text = "VIP L${item.vipLevelRequired}"
            } else {
                binding.chipStatus.visibility = View.GONE
            }

            // 图片：必须显示（有 url 就加载）
            val url = item.imageUrl?.trim().orEmpty()
            if (url.isNotEmpty()) {
                binding.imgIcon.visibility = View.VISIBLE
                binding.textIcon.visibility = View.GONE

                Glide.with(binding.imgIcon)
                    .load(url)
                    .into(binding.imgIcon)
            } else {
                // 没 url 才用 emoji（你说兜底无所谓，但这里不影响正常情况）
                binding.imgIcon.visibility = View.GONE
                binding.textIcon.visibility = View.VISIBLE
                binding.textIcon.text = "🛍️"
            }

            binding.root.setOnClickListener { onItemClick(item) }
        }
    }
}
