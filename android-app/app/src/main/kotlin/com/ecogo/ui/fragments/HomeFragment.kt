package com.ecogo.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.ecogo.data.MockData
import com.ecogo.data.RecommendationRequest
import com.ecogo.databinding.FragmentHomeBinding
import com.ecogo.ui.adapters.HighlightAdapter
import com.ecogo.ui.adapters.WalkingRouteAdapter
import com.ecogo.repository.EcoGoRepository
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {
    
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val repository = EcoGoRepository()
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupUI()
        setupRecyclerView()
        setupAnimations()
        setupActions()
        loadData()
    }
    
    private fun setupUI() {
        binding.textBusNumber.text = "--"
        binding.textBusTime.text = "-- min"
        binding.textBusRoute.text = "to --"
        binding.textMonthlyPoints.text = "850"
        binding.textPointsChange.text = "+150 this week"
        binding.textSocScore.text = "4,520"
        binding.textSocRank.text = "Rank #1"
    }
    
    private fun setupRecyclerView() {
        binding.recyclerHighlights.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = HighlightAdapter(emptyList())
        }
        binding.recyclerWalkingRoutes.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = WalkingRouteAdapter(emptyList())
        }
    }

    private fun loadData() {
        viewLifecycleOwner.lifecycleScope.launch {
            val routesResult = repository.getBusRoutes().getOrElse { MockData.ROUTES }
            val firstRoute = routesResult.firstOrNull()
            if (firstRoute != null) {
                binding.textBusNumber.text = firstRoute.name
                binding.textBusTime.text = firstRoute.time ?: "${firstRoute.nextArrival} min"
                binding.textBusRoute.text = if (firstRoute.to.isNotEmpty()) "to ${firstRoute.to}" else "to ${firstRoute.name}"
            }

            val activitiesResult = repository.getAllActivities().getOrElse { MockData.ACTIVITIES }
            binding.recyclerHighlights.adapter = HighlightAdapter(activitiesResult.take(3))

            val walkingRoutes = repository.getWalkingRoutes().getOrElse { MockData.WALKING_ROUTES }
            binding.recyclerWalkingRoutes.adapter = WalkingRouteAdapter(walkingRoutes)
        }
    }

    private fun setupAnimations() {
        val breathe = AnimationUtils.loadAnimation(requireContext(), com.ecogo.R.anim.breathe)
        val slideUp = AnimationUtils.loadAnimation(requireContext(), com.ecogo.R.anim.slide_up)
        val popIn = AnimationUtils.loadAnimation(requireContext(), com.ecogo.R.anim.pop_in)

        binding.cardRecommendation.startAnimation(popIn)
        binding.cardNextBus.startAnimation(breathe)
        binding.cardMap.startAnimation(slideUp)
        binding.imageAvatarSmall.startAnimation(breathe)
    }

    private fun setupActions() {
        binding.buttonOpenMap.setOnClickListener {
            findNavController().navigate(com.ecogo.R.id.mapFragment)
        }
        binding.textViewAll.setOnClickListener {
            findNavController().navigate(com.ecogo.R.id.activitiesFragment)
        }
        binding.imageAvatarSmall.setOnClickListener {
            findNavController().navigate(com.ecogo.R.id.profileFragment)
        }
        binding.buttonGo.setOnClickListener {
            val destination = binding.editPlan.text?.toString()?.trim().orEmpty()
            if (destination.isNotEmpty()) {
                requestRecommendation(destination)
                binding.editPlan.text?.clear()
            }
        }
    }

    private fun requestRecommendation(destination: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            val response = repository.getRecommendation(RecommendationRequest(destination)).getOrElse { null }
            if (response != null) {
                binding.layoutRecommendationResult.visibility = View.VISIBLE
                binding.textRecommendationTag.text = response.tag
                binding.textRecommendationResult.text = response.text
            }
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
