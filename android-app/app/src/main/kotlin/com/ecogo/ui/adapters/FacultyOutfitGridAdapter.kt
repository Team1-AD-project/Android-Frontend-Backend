package com.ecogo.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.ecogo.R
import com.ecogo.data.FacultyData
import com.ecogo.data.MascotSize
import com.ecogo.ui.views.MascotLionView
import com.google.android.material.card.MaterialCardView

/**
 * Grid adapter for faculty outfits in Closet dialog.
 * Shows each faculty with mascot preview, ownership status, and price.
 */
class FacultyOutfitGridAdapter(
    private val faculties: List<FacultyData>,
    private var equippedFacultyId: String? = null,
    private var ownedFacultyIds: Set<String> = emptySet(),
    private val userFacultyId: String? = null,
    private val costCalculator: (FacultyData) -> Int = { 0 },
    private val onFacultyClick: (FacultyData) -> Unit
) : RecyclerView.Adapter<FacultyOutfitGridAdapter.GridViewHolder>() {

    fun updateEquipped(facultyId: String?) {
        equippedFacultyId = facultyId
        notifyDataSetChanged()
    }

    fun updateOwned(owned: Set<String>) {
        ownedFacultyIds = owned
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GridViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_faculty_outfit_grid, parent, false)
        return GridViewHolder(view)
    }

    override fun onBindViewHolder(holder: GridViewHolder, position: Int) {
        holder.bind(faculties[position])
    }

    override fun getItemCount(): Int = faculties.size

    inner class GridViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val card: MaterialCardView = itemView.findViewById(R.id.card_faculty_outfit)
        private val mascot: MascotLionView = itemView.findViewById(R.id.mascot_faculty)
        private val name: TextView = itemView.findViewById(R.id.text_faculty_name)
        private val outfitItems: TextView = itemView.findViewById(R.id.text_outfit_items)
        private val equipped: ImageView = itemView.findViewById(R.id.image_equipped)

        fun bind(faculty: FacultyData) {
            name.text = faculty.name
            mascot.mascotSize = MascotSize.MEDIUM
            mascot.outfit = faculty.outfit

            val isOwned = ownedFacultyIds.contains(faculty.id)
            val isEquipped = equippedFacultyId == faculty.id
            val isUserFaculty = userFacultyId == faculty.id

            // 状态文字
            when {
                isEquipped -> {
                    outfitItems.text = "✅ Equipped"
                    outfitItems.setTextColor(ContextCompat.getColor(itemView.context, R.color.primary))
                }
                isOwned && isUserFaculty -> {
                    outfitItems.text = "🎁 Your Faculty"
                    outfitItems.setTextColor(ContextCompat.getColor(itemView.context, R.color.primary))
                }
                isOwned -> {
                    outfitItems.text = "Owned"
                    outfitItems.setTextColor(ContextCompat.getColor(itemView.context, R.color.text_secondary))
                }
                else -> {
                    val cost = costCalculator(faculty)
                    outfitItems.text = "🔒 $cost pts"
                    outfitItems.setTextColor(ContextCompat.getColor(itemView.context, R.color.text_secondary))
                }
            }

            // 边框高亮
            if (isEquipped) {
                equipped.visibility = View.VISIBLE
                card.strokeWidth = 4
                card.strokeColor = ContextCompat.getColor(itemView.context, R.color.primary)
            } else {
                equipped.visibility = View.GONE
                card.strokeWidth = 0
            }

            // 未拥有的学院服饰半透明
            itemView.alpha = if (isOwned) 1f else 0.7f

            itemView.setOnClickListener {
                onFacultyClick(faculty)
            }
        }
    }
}
