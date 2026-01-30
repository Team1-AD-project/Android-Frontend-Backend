package com.ecogo.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.ecogo.data.MockData
import com.ecogo.databinding.FragmentActivitiesBinding
import com.ecogo.ui.adapters.ActivityAdapter
import com.ecogo.repository.EcoGoRepository
import kotlinx.coroutines.launch

class ActivitiesFragment : Fragment() {
    
    private var _binding: FragmentActivitiesBinding? = null
    private val binding get() = _binding!!
    private val repository = EcoGoRepository()
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentActivitiesBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupRecyclerView()
        setupAnimations()
        loadActivities()
    }
    
    private fun setupRecyclerView() {
        binding.recyclerActivities.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = ActivityAdapter(emptyList())
        }
    }

    private fun loadActivities() {
        viewLifecycleOwner.lifecycleScope.launch {
            val activities = repository.getAllActivities().getOrElse { MockData.ACTIVITIES }
            binding.recyclerActivities.adapter = ActivityAdapter(activities)
        }
    }

    private fun setupAnimations() {
        val slideUp = AnimationUtils.loadAnimation(requireContext(), com.ecogo.R.anim.slide_up)
        binding.recyclerActivities.startAnimation(slideUp)
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
