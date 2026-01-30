package com.ecogo.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.ecogo.data.MockData
import com.ecogo.databinding.FragmentMapBinding
import com.ecogo.repository.EcoGoRepository
import kotlinx.coroutines.launch

class MapFragment : Fragment() {
    
    private var _binding: FragmentMapBinding? = null
    private val binding get() = _binding!!
    private val repository = EcoGoRepository()
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMapBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        binding.textPlaceholder.text = getString(com.ecogo.R.string.map_placeholder)
        val slideUp = AnimationUtils.loadAnimation(requireContext(), com.ecogo.R.anim.slide_up)
        binding.cardFaculty.startAnimation(slideUp)

        loadFaculty()
    }

    private fun loadFaculty() {
        viewLifecycleOwner.lifecycleScope.launch {
            val faculties = repository.getFaculties().getOrElse { MockData.FACULTIES }
            val faculty = faculties.firstOrNull()
            if (faculty != null) {
                binding.textFacultyName.text = faculty.name
                binding.textFacultyRank.text = "Current Rank: #${faculty.rank}"
                binding.textFacultyScore.text = "${faculty.score} pts"
            }
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
