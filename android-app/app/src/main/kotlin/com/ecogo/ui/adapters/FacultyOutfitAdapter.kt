package com.ecogo.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ecogo.R
import com.ecogo.data.FacultyData
import com.ecogo.data.MascotSize
import com.ecogo.ui.views.MascotLionView

/**
 * Closet 中展示学院服装的 Adapter。
 * 每个 item 显示学院名称和小狮子（该学院装备），点击可一键装备该学院服装。
 */
class FacultyOutfitAdapter(
    private val faculties: List<FacultyData>,
    private val onFacultyOutfitClick: (FacultyData) -> Unit
) : RecyclerView.Adapter<FacultyOutfitAdapter.FacultyOutfitViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FacultyOutfitViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_faculty_outfit, parent, false)
        return FacultyOutfitViewHolder(view)
    }

    override fun onBindViewHolder(holder: FacultyOutfitViewHolder, position: Int) {
        holder.bind(faculties[position], onFacultyOutfitClick)
    }

    override fun getItemCount(): Int = faculties.size

    class FacultyOutfitViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val mascot: MascotLionView = itemView.findViewById(R.id.mascot_faculty)
        private val name: TextView = itemView.findViewById(R.id.text_faculty_name)

        fun bind(faculty: FacultyData, onFacultyOutfitClick: (FacultyData) -> Unit) {
            name.text = faculty.name
            mascot.mascotSize = MascotSize.MEDIUM
            mascot.outfit = faculty.outfit

            itemView.setOnClickListener {
                onFacultyOutfitClick(faculty)
            }
        }
    }
}
