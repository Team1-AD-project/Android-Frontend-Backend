package com.ecogo.ui.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ecogo.R
import com.ecogo.data.HomeBanner
import com.google.android.material.button.MaterialButton

class HomeBannerAdapter(
    private val onBannerClick: (HomeBanner) -> Unit
) : ListAdapter<HomeBanner, HomeBannerAdapter.BannerViewHolder>(BannerDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BannerViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_home_banner, parent, false)
        return BannerViewHolder(view, onBannerClick)
    }

    override fun onBindViewHolder(holder: BannerViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class BannerViewHolder(
        itemView: View,
        private val onBannerClick: (HomeBanner) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        
        private val container: LinearLayout = itemView.findViewById(R.id.banner_container)
        private val titleText: TextView = itemView.findViewById(R.id.text_banner_title)
        private val subtitleText: TextView = itemView.findViewById(R.id.text_banner_subtitle)
        private val actionButton: MaterialButton = itemView.findViewById(R.id.button_banner_action)

        fun bind(banner: HomeBanner) {
            titleText.text = banner.title
            
            // Subtitle (optional)
            if (banner.subtitle != null) {
                subtitleText.text = banner.subtitle
                subtitleText.visibility = View.VISIBLE
            } else {
                subtitleText.visibility = View.GONE
            }
            
            // Background color
            try {
                container.setBackgroundColor(Color.parseColor(banner.backgroundColor))
            } catch (e: Exception) {
                container.setBackgroundColor(Color.parseColor("#15803D")) // Fallback
            }
            
            // Action button (optional)
            if (banner.actionText != null && banner.actionTarget != null) {
                actionButton.text = banner.actionText
                actionButton.visibility = View.VISIBLE
                actionButton.setOnClickListener { onBannerClick(banner) }
            } else {
                actionButton.visibility = View.GONE
            }
            
            // Card click (if no action button)
            if (banner.actionTarget != null && banner.actionText == null) {
                itemView.setOnClickListener { onBannerClick(banner) }
            }
        }
    }

    private class BannerDiffCallback : DiffUtil.ItemCallback<HomeBanner>() {
        override fun areItemsTheSame(oldItem: HomeBanner, newItem: HomeBanner): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: HomeBanner, newItem: HomeBanner): Boolean {
            return oldItem == newItem
        }
    }
}
