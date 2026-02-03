package com.ecogo.ui.adapters

import android.animation.AnimatorInflater
import android.animation.AnimatorSet
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ecogo.R
import com.ecogo.data.FacultyData
import com.ecogo.databinding.ItemFacultyFlipCardBinding

class FacultyFlipAdapter(
    private val faculties: List<FacultyData>,
    private val onFacultySelected: (FacultyData) -> Unit
) : RecyclerView.Adapter<FacultyFlipAdapter.FlipCardViewHolder>() {

    private var selectedPosition = -1
    private val flippedStates = BooleanArray(faculties.size) { false }

    inner class FlipCardViewHolder(private val binding: ItemFacultyFlipCardBinding) : 
        RecyclerView.ViewHolder(binding.root) {
        
        private var isFlipped = false
        
        fun bind(faculty: FacultyData, position: Int, isSelected: Boolean) {
            // Front side setup
            binding.textFrontName.text = faculty.name
            binding.viewFrontColor.setBackgroundColor(Color.parseColor(faculty.color))
            
            // Back side setup
            binding.textBackName.text = faculty.name
            binding.textBackSlogan.text = faculty.slogan
            binding.mascotBack.outfit = faculty.outfit
            
            // Outfit items
            val outfitItems = mutableListOf<String>()
            if (faculty.outfit.head != "none") {
                outfitItems.add(getItemName(faculty.outfit.head))
            }
            if (faculty.outfit.face != "none") {
                outfitItems.add(getItemName(faculty.outfit.face))
            }
            if (faculty.outfit.body != "none") {
                outfitItems.add(getItemName(faculty.outfit.body))
            }
            binding.textBackOutfit.text = outfitItems.joinToString("\n")
            
            // Selection indicator
            binding.viewSelectedIndicator.visibility = if (isSelected) View.VISIBLE else View.GONE
            
            // Restore flipped state
            isFlipped = flippedStates[position]
            if (isFlipped) {
                binding.cardFront.visibility = View.GONE
                binding.cardBack.visibility = View.VISIBLE
                binding.cardFront.rotationY = 180f
                binding.cardBack.rotationY = 0f
            } else {
                binding.cardFront.visibility = View.VISIBLE
                binding.cardBack.visibility = View.GONE
                binding.cardFront.rotationY = 0f
                binding.cardBack.rotationY = 180f
            }
            
            // Click to flip
            binding.root.setOnClickListener {
                flipCard(position, faculty)
            }
        }
        
        private fun flipCard(position: Int, faculty: FacultyData) {
            isFlipped = !isFlipped
            flippedStates[position] = isFlipped
            
            val distance = 8000f
            val scale = binding.root.resources.displayMetrics.density * distance
            
            binding.cardFront.cameraDistance = scale
            binding.cardBack.cameraDistance = scale
            
            val flipOut: AnimatorSet
            val flipIn: AnimatorSet
            
            if (isFlipped) {
                // Flip to back
                flipOut = AnimatorInflater.loadAnimator(
                    binding.root.context,
                    R.animator.card_flip_out
                ) as AnimatorSet
                flipIn = AnimatorInflater.loadAnimator(
                    binding.root.context,
                    R.animator.card_flip_in
                ) as AnimatorSet
                
                flipOut.setTarget(binding.cardFront)
                flipIn.setTarget(binding.cardBack)
                
                flipOut.start()
                flipIn.start()
                
                binding.cardFront.postDelayed({
                    binding.cardFront.visibility = View.GONE
                    binding.cardBack.visibility = View.VISIBLE
                }, 200)
                
                // Auto-select when flipped
                val previousPosition = selectedPosition
                selectedPosition = position
                notifyItemChanged(previousPosition)
                notifyItemChanged(selectedPosition)
                onFacultySelected(faculty)
                
            } else {
                // Flip to front
                flipOut = AnimatorInflater.loadAnimator(
                    binding.root.context,
                    R.animator.card_flip_out
                ) as AnimatorSet
                flipIn = AnimatorInflater.loadAnimator(
                    binding.root.context,
                    R.animator.card_flip_in
                ) as AnimatorSet
                
                flipOut.setTarget(binding.cardBack)
                flipIn.setTarget(binding.cardFront)
                
                flipOut.start()
                flipIn.start()
                
                binding.cardBack.postDelayed({
                    binding.cardBack.visibility = View.GONE
                    binding.cardFront.visibility = View.VISIBLE
                }, 200)
            }
        }
        
        private fun getItemName(itemId: String): String {
            return when (itemId) {
                "hat_helmet" -> "Safety Helmet"
                "hat_beret" -> "Artist Beret"
                "hat_grad" -> "Grad Cap"
                "hat_cap" -> "Orange Cap"
                "face_goggles" -> "Safety Goggles"
                "glasses_sun" -> "Shades"
                "body_plaid" -> "Engin Plaid"
                "body_suit" -> "Biz Suit"
                "body_coat" -> "Lab Coat"
                "shirt_nus" -> "NUS Tee"
                "shirt_hoodie" -> "Blue Hoodie"
                else -> ""
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FlipCardViewHolder {
        val binding = ItemFacultyFlipCardBinding.inflate(
            LayoutInflater.from(parent.context), 
            parent, 
            false
        )
        return FlipCardViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FlipCardViewHolder, position: Int) {
        holder.bind(faculties[position], position, position == selectedPosition)
    }

    override fun getItemCount() = faculties.size
}
