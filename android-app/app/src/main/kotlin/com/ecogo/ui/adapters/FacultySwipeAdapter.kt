package com.ecogo.ui.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ecogo.data.FacultyData
import com.ecogo.databinding.ItemFacultySwipeCardBinding

class FacultySwipeAdapter(
    private val faculties: List<FacultyData>,
    private val onFacultySelected: (FacultyData) -> Unit
) : RecyclerView.Adapter<FacultySwipeAdapter.SwipeCardViewHolder>() {

    inner class SwipeCardViewHolder(private val binding: ItemFacultySwipeCardBinding) : 
        RecyclerView.ViewHolder(binding.root) {
        
        fun bind(faculty: FacultyData) {
            // Faculty info
            binding.textFacultyName.text = faculty.name
            binding.textFacultySlogan.text = faculty.slogan
            
            // Faculty color
            binding.viewFacultyColor.setBackgroundColor(Color.parseColor(faculty.color))
            
            // Set card background color with tint
            val colorWithAlpha = Color.parseColor(faculty.color)
            val red = Color.red(colorWithAlpha)
            val green = Color.green(colorWithAlpha)
            val blue = Color.blue(colorWithAlpha)
            val lightColor = Color.argb(30, red, green, blue) // 10% opacity
            binding.cardFaculty.setCardBackgroundColor(lightColor)
            
            // Mascot with outfit
            binding.mascotPreview.outfit = faculty.outfit
            
            // Outfit items list
            val outfitItems = mutableListOf<String>()
            if (faculty.outfit.head != "none") {
                outfitItems.add("• ${getItemName(faculty.outfit.head)}")
            }
            if (faculty.outfit.face != "none") {
                outfitItems.add("• ${getItemName(faculty.outfit.face)}")
            }
            if (faculty.outfit.body != "none") {
                outfitItems.add("• ${getItemName(faculty.outfit.body)}")
            }
            binding.textOutfitItems.text = outfitItems.joinToString("\n")
            
            // Click to select
            binding.cardFaculty.setOnClickListener {
                onFacultySelected(faculty)
            }
            
            // Add scale animation on touch
            binding.cardFaculty.setOnTouchListener { view, event ->
                when (event.action) {
                    android.view.MotionEvent.ACTION_DOWN -> {
                        view.animate()
                            .scaleX(0.95f)
                            .scaleY(0.95f)
                            .setDuration(100)
                            .start()
                    }
                    android.view.MotionEvent.ACTION_UP,
                    android.view.MotionEvent.ACTION_CANCEL -> {
                        view.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(100)
                            .start()
                    }
                }
                false // Let the click listener handle the event
            }
        }
        
        private fun getItemName(itemId: String): String {
            return when (itemId) {
                // Head items
                "hat_helmet" -> "Safety Helmet"
                "hat_beret" -> "Artist Beret"
                "hat_grad" -> "Grad Cap"
                "hat_cap" -> "Orange Cap"
                "hat_crown" -> "Golden Crown"
                "hat_party" -> "Party Hat"
                "hat_beanie" -> "Winter Beanie"
                "hat_cowboy" -> "Cowboy Hat"
                "hat_chef" -> "Chef Hat"
                "hat_wizard" -> "Wizard Hat"
                // Face items
                "face_goggles" -> "Safety Goggles"
                "glasses_sun" -> "Shades"
                "glasses_nerd" -> "Nerd Glasses"
                "glasses_3d" -> "3D Glasses"
                "face_mask" -> "Superhero Mask"
                "face_monocle" -> "Fancy Monocle"
                "face_scarf" -> "Winter Scarf"
                "face_vr" -> "VR Headset"
                // Body items
                "body_plaid" -> "Engin Plaid"
                "body_suit" -> "Biz Suit"
                "body_coat" -> "Lab Coat"
                "shirt_nus" -> "NUS Tee"
                "shirt_hoodie" -> "Blue Hoodie"
                "body_sports" -> "Sports Jersey"
                "body_kimono" -> "Traditional Kimono"
                "body_tux" -> "Fancy Tuxedo"
                "body_superhero" -> "Superhero Cape"
                "body_doctor" -> "Doctor's Coat"
                "body_pilot" -> "Pilot Uniform"
                "body_ninja" -> "Ninja Outfit"
                "body_scrubs" -> "Medical Scrubs"
                "body_polo" -> "Nurse Polo"
                else -> ""
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SwipeCardViewHolder {
        val binding = ItemFacultySwipeCardBinding.inflate(
            LayoutInflater.from(parent.context), 
            parent, 
            false
        )
        return SwipeCardViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SwipeCardViewHolder, position: Int) {
        holder.bind(faculties[position])
    }

    override fun getItemCount() = faculties.size
}
