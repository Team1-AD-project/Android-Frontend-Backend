package com.ecogo.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ecogo.R
import com.ecogo.data.Voucher

class VoucherAdapter(
    private var vouchers: List<Voucher>,
    private val onVoucherClick: (Voucher) -> Unit = {},
    private val onRedeemClick: (Voucher) -> Unit = {}
) : RecyclerView.Adapter<VoucherAdapter.VoucherViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VoucherViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_voucher, parent, false)
        return VoucherViewHolder(view)
    }

    override fun onBindViewHolder(holder: VoucherViewHolder, position: Int) {
        val voucher = vouchers[position]
        holder.bind(voucher)

        // ✅ 整个卡片点击：统一进详情页（Marketplace / Owned 都可）
        holder.itemView.setOnClickListener {
            onVoucherClick(voucher)
        }

        // ✅ 按钮点击：只有在“允许按钮操作”的情况下才触发
        holder.setActionClickListener {
            // Marketplace: 走兑换（你现在是进详情页再 redeem 也行，但这里保留）
            // Owned: 我们也让它进详情（你的 onRedeemClick 可以不传，或让它也走 onVoucherClick）
            onRedeemClick(voucher)
        }
    }

    override fun getItemCount() = vouchers.size

    fun updateVouchers(newVouchers: List<Voucher>) {
        vouchers = newVouchers
        notifyDataSetChanged()
    }

    class VoucherViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val iconCard: com.google.android.material.card.MaterialCardView =
            itemView.findViewById(R.id.card_icon)

        private val iconImg: android.widget.ImageView =
            itemView.findViewById(R.id.img_icon)

        private val iconEmoji: android.widget.TextView =
            itemView.findViewById(R.id.text_icon)

        private val icon: TextView = itemView.findViewById(R.id.text_icon)
        private val name: TextView = itemView.findViewById(R.id.text_name)
        private val description: TextView = itemView.findViewById(R.id.text_description)
        private val cost: TextView = itemView.findViewById(R.id.text_cost)
        private val button: com.google.android.material.button.MaterialButton =
            itemView.findViewById(R.id.button_redeem)
        private val statusChip: com.google.android.material.chip.Chip =
            itemView.findViewById(R.id.chip_status)

        fun setActionClickListener(onClick: () -> Unit) {
            button.setOnClickListener {
                if (button.isEnabled) onClick()
            }
        }

        fun bind(voucher: Voucher) {
            val isOwned = !voucher.userVoucherId.isNullOrBlank()
            val status = voucher.status?.uppercase()

            name.text = voucher.name ?: ""
            description.text = voucher.description ?: ""

            // cost：只有 marketplace(未拥有) 才显示
            cost.text = if (!isOwned) (voucher.cost ?: 0).toString() else ""

            // ---------- 按钮 ----------
            val (btnText, enabled) = when {
                !isOwned -> {
                    if (voucher.available == true) "Redeem" to true else "Sold Out" to false
                }
                status == "ACTIVE" -> "View Code" to true
                status == "USED" -> "Used" to false
                status == "EXPIRED" -> "Expired" to false
                else -> "View Code" to true
            }
            button.text = btnText
            button.isEnabled = enabled

            // ---------- 灰态 ----------
            itemView.alpha = when {
                !isOwned -> if (voucher.available == true) 1f else 0.6f
                status == "ACTIVE" -> 1f
                else -> 0.6f
            }

            // ---------- chip_status（你现在默认 gone，可按需显示） ----------
            statusChip.visibility = if (!isOwned && voucher.available == true) View.VISIBLE else View.GONE
            if (statusChip.visibility == View.VISIBLE) {
                statusChip.text = "🔥 Popular"
            }

            // ---------- 图片优先：img_icon / text_icon 兜底 ----------
            val url = voucher.imageUrl
            if (!url.isNullOrBlank()) {
                iconImg.visibility = View.VISIBLE
                iconEmoji.visibility = View.GONE

                Glide.with(iconImg)
                    .load(url)
                    .placeholder(android.R.drawable.ic_menu_report_image)
                    .error(android.R.drawable.ic_menu_report_image)
                    .into(iconImg)

                // 有图时给个浅底
                iconCard.setCardBackgroundColor(android.graphics.Color.parseColor("#F5F5F5"))
            } else {
                iconImg.visibility = View.GONE
                iconEmoji.visibility = View.VISIBLE

                iconEmoji.text = "🎫"
                iconCard.setCardBackgroundColor(android.graphics.Color.parseColor("#F3E8FF"))
            }
        }


    }
}
