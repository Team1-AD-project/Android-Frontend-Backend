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
import com.ecogo.databinding.FragmentRoutesBinding
import com.ecogo.ui.adapters.BusRouteAdapter
import com.ecogo.repository.EcoGoRepository
import kotlinx.coroutines.launch

class RoutesFragment : Fragment() {
    
    private var _binding: FragmentRoutesBinding? = null
    private val binding get() = _binding!!
    private val repository = EcoGoRepository()
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRoutesBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupRecyclerView()
        setupAnimations()
        loadRoutes()
    }
    
    private fun setupRecyclerView() {
        binding.recyclerRoutes.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = BusRouteAdapter(emptyList())
        }
    }

    private fun loadRoutes() {
        viewLifecycleOwner.lifecycleScope.launch {
            val routes = repository.getBusRoutes().getOrElse { MockData.ROUTES }
            binding.recyclerRoutes.adapter = BusRouteAdapter(routes)
        }
    }

    private fun setupAnimations() {
        val slideUp = AnimationUtils.loadAnimation(requireContext(), com.ecogo.R.anim.slide_up)
        binding.recyclerRoutes.startAnimation(slideUp)
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
