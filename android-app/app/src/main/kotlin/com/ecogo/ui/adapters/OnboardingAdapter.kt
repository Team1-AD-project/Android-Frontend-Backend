package com.ecogo.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ecogo.R

class OnboardingAdapter : RecyclerView.Adapter<OnboardingAdapter.OnboardingViewHolder>() {
    
    private val pages = listOf(
        OnboardingPage(R.drawable.app_icon, "Welcome to EcoGo! 🎉", "Transform your daily commute\ninto environmental impact"),
        OnboardingPage(R.drawable.app_icon, "Track Green Trips 🚌", "Earn points for every\neco-friendly journey"),
        OnboardingPage(R.drawable.app_icon, "Join Challenges 🏆", "Compete with friends and\nyour faculty for rewards"),
        OnboardingPage(R.drawable.app_icon, "Customize Mascot 🎨", "Unlock outfits and accessories\nas you progress"),
        OnboardingPage(R.drawable.app_icon, "Ready to Start? 🌱", "Begin your first\ngreen trip today!")
    )
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OnboardingViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_onboarding_page, parent, false)
        return OnboardingViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: OnboardingViewHolder, position: Int) {
        holder.bind(pages[position])
    }
    
    override fun getItemCount() = pages.size
    
    class OnboardingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val icon: ImageView = itemView.findViewById(R.id.image_icon)
        private val title: TextView = itemView.findViewById(R.id.text_title)
        private val description: TextView = itemView.findViewById(R.id.text_description)
        
        fun bind(page: OnboardingPage) {
            icon.setImageResource(page.iconRes)
            title.text = page.title
            description.text = page.description
        }
    }
    
    data class OnboardingPage(val iconRes: Int, val title: String, val description: String)
}
