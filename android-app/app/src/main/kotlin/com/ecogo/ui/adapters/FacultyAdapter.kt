package com.ecogo.ui.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.ecogo.R
import com.ecogo.data.FacultyData
import com.ecogo.databinding.ItemFacultyCardBinding

class FacultyAdapter(
    private val faculties: List<FacultyData>,
    private val onFacultySelected: (FacultyData) -> Unit
) : RecyclerView.Adapter<FacultyAdapter.FacultyViewHolder>() {

    private var selectedPosition = -1

    inner class FacultyViewHolder(private val binding: ItemFacultyCardBinding) : 
        RecyclerView.ViewHolder(binding.root) {
        
        fun bind(faculty: FacultyData, isSelected: Boolean) {
            binding.textFacultyName.text = faculty.name
            binding.textFacultySlogan.text = faculty.slogan
            
            // 学院颜色圆圈
            binding.viewFacultyColor.setBackgroundColor(Color.parseColor(faculty.color))
            
            // 选中状态
            if (isSelected) {
                binding.cardFaculty.strokeColor = ContextCompat.getColor(itemView.context, R.color.primary)
                binding.cardFaculty.strokeWidth = 4
                binding.cardFaculty.setCardBackgroundColor(
                    ContextCompat.getColor(itemView.context, R.color.primary_light)
                )
            } else {
                binding.cardFaculty.strokeColor = ContextCompat.getColor(itemView.context, R.color.border)
                binding.cardFaculty.strokeWidth = 2
                binding.cardFaculty.setCardBackgroundColor(Color.WHITE)
            }
            
            binding.root.setOnClickListener {
                val previousPosition = selectedPosition
                selectedPosition = adapterPosition
                
                notifyItemChanged(previousPosition)
                notifyItemChanged(selectedPosition)
                
                onFacultySelected(faculty)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FacultyViewHolder {
        val binding = ItemFacultyCardBinding.inflate(
            LayoutInflater.from(parent.context), 
            parent, 
            false
        )
        return FacultyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FacultyViewHolder, position: Int) {
        holder.bind(faculties[position], position == selectedPosition)
    }

    override fun getItemCount() = faculties.size
}
