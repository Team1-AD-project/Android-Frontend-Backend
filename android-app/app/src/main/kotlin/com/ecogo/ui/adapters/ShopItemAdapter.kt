package com.ecogo.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.ecogo.R
import com.ecogo.data.ShopItem
import com.google.android.material.card.MaterialCardView

/**
 * RecyclerView 列表项：分组标题 或 商品卡片
 */
sealed class ShopListItem {
    data class Header(val title: String) : ShopListItem()
    data class Item(val shopItem: ShopItem) : ShopListItem()
}

class ShopItemAdapter(
    private var items: List<ShopListItem>,
    private val onItemClick: (ShopItem) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_HEADER = 0
        private const val VIEW_TYPE_ITEM = 1
    }

    override fun getItemViewType(position: Int): Int = when (items[position]) {
        is ShopListItem.Header -> VIEW_TYPE_HEADER
        is ShopListItem.Item -> VIEW_TYPE_ITEM
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_HEADER) {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_section_header, parent, false)
            HeaderViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_shop, parent, false)
            ShopViewHolder(view, onItemClick)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = items[position]) {
            is ShopListItem.Header -> (holder as HeaderViewHolder).bind(item.title)
            is ShopListItem.Item -> (holder as ShopViewHolder).bind(item.shopItem)
        }
    }

    override fun getItemCount() = items.size

    /** 判断指定位置是否为分组标题（用于 SpanSizeLookup） */
    fun isHeader(position: Int): Boolean = items.getOrNull(position) is ShopListItem.Header

    fun updateItems(newItems: List<ShopListItem>) {
        items = newItems
        notifyDataSetChanged()
    }

    // ── Header ViewHolder ──────────────────────

    class HeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val title: TextView = itemView.findViewById(R.id.text_section_title)
        fun bind(text: String) {
            title.text = text
        }
    }

    // ── Item ViewHolder ────────────────────────

    class ShopViewHolder(
        itemView: View,
        private val onItemClick: (ShopItem) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        private val card = itemView as MaterialCardView
        private val icon: TextView = itemView.findViewById(R.id.text_icon)
        private val name: TextView = itemView.findViewById(R.id.text_name)
        private val cost: TextView = itemView.findViewById(R.id.text_cost)
        private val status: TextView = itemView.findViewById(R.id.text_status)
        private val check: View = itemView.findViewById(R.id.image_check)

        fun bind(item: ShopItem) {
            name.text = item.name
            cost.text = "${item.cost} pts"

            // 图标映射（覆盖全部物品）
            icon.text = when (item.id) {
                // Head items (10)
                "hat_grad" -> "🎓"
                "hat_cap" -> "🧢"
                "hat_helmet" -> "⛑️"
                "hat_beret" -> "🎨"
                "hat_crown" -> "👑"
                "hat_party" -> "🎉"
                "hat_beanie" -> "❄️"
                "hat_cowboy" -> "🤠"
                "hat_chef" -> "👨‍🍳"
                "hat_wizard" -> "🧙"
                // Face items (8)
                "glasses_sun" -> "🕶️"
                "face_goggles" -> "🥽"
                "glasses_nerd" -> "🤓"
                "glasses_3d" -> "🎬"
                "face_mask" -> "🦸"
                "face_monocle" -> "🧐"
                "face_scarf" -> "🧣"
                "face_vr" -> "🥽"
                // Body items (14)
                "shirt_nus" -> "👕"
                "shirt_hoodie" -> "🧥"
                "body_plaid" -> "👔"
                "body_suit" -> "🤵"
                "body_coat" -> "🥼"
                "body_sports" -> "⚽"
                "body_kimono" -> "👘"
                "body_tux" -> "🎩"
                "body_superhero" -> "🦸"
                "body_doctor" -> "👨‍⚕️"
                "body_pilot" -> "✈️"
                "body_ninja" -> "🥷"
                "body_scrubs" -> "🏥"
                "body_polo" -> "👩‍⚕️"
                // Badge items (10)
                "badge_eco_warrior" -> "🌿"
                "badge_walker" -> "🚶"
                "badge_cyclist" -> "🚴"
                "badge_green" -> "🌱"
                "badge_pioneer" -> "🏆"
                "badge_streak" -> "🔥"
                "badge_social" -> "🦋"
                "badge_explorer" -> "🗺️"
                "badge_recycler" -> "♻️"
                "badge_legend" -> "⭐"
                else -> "🎁"
            }

            // 状态显示
            when {
                item.equipped -> {
                    status.visibility = View.VISIBLE
                    status.text = itemView.context.getString(R.string.profile_equipped)
                    cost.visibility = View.GONE
                    check.visibility = View.VISIBLE
                    card.strokeWidth = 4
                    card.strokeColor = ContextCompat.getColor(itemView.context, R.color.primary)
                }
                item.owned -> {
                    status.visibility = View.VISIBLE
                    status.text = "Owned"
                    cost.visibility = View.GONE
                    check.visibility = View.GONE
                    card.strokeWidth = 0
                }
                else -> {
                    status.visibility = View.GONE
                    cost.visibility = View.VISIBLE
                    check.visibility = View.GONE
                    card.strokeWidth = 0
                }
            }

            itemView.setOnClickListener {
                onItemClick(item)
            }
        }
    }
}
