package com.ecogo.ui.fragments.navigation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.ecogo.R
import com.ecogo.data.MascotSize
import com.ecogo.databinding.FragmentTripStartBinding
import com.ecogo.viewmodel.NavigationViewModel

/**
 * 行程开始确认页面
 * 展示选择的路线信息，预计时间、距离、积分等
 * 包含小狮子准备出发动画
 */
class TripStartFragment : Fragment() {

    private var _binding: FragmentTripStartBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var viewModel: NavigationViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTripStartBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        viewModel = ViewModelProvider(requireActivity())[NavigationViewModel::class.java]
        
        setupMascot()
        setupUI()
        observeViewModel()
        setupAnimations()
    }
    
    private fun setupMascot() {
        binding.mascotStart.apply {
            mascotSize = MascotSize.LARGE
            // 挥手准备出发动画
            waveAnimation()
        }
    }
    
    private fun setupUI() {
        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }
        
        binding.btnStartTrip.setOnClickListener {
            // 开始按钮点击动画
            val jump = AnimationUtils.loadAnimation(requireContext(), R.anim.jump)
            binding.btnStartTrip.startAnimation(jump)
            
            // 延迟一点启动行程，让动画完成
            binding.root.postDelayed({
                startTrip()
            }, 300)
        }
    }
    
    private fun observeViewModel() {
        viewModel.currentRoute.observe(viewLifecycleOwner) { route ->
            route?.let {
                // 显示路线信息
                binding.textRouteName.text = when (it.mode) {
                    com.ecogo.data.TransportMode.WALK -> "步行路线"
                    com.ecogo.data.TransportMode.CYCLE -> "骑行路线"
                    com.ecogo.data.TransportMode.BUS -> "公交路线"
                    com.ecogo.data.TransportMode.MIXED -> "混合路线"
                }
                
                binding.textOrigin.text = it.origin.name
                binding.textDestination.text = it.destination.name
                
                // 预计信息
                binding.textEstimatedTime.text = "${it.duration} 分钟"
                binding.textEstimatedDistance.text = String.format("%.1f 公里", it.distance)
                binding.textCarbonSaved.text = String.format("%.0f g CO₂", it.carbonSaved)
                binding.textPointsEarn.text = "+${it.points} 积分"
                
                // 如果有推荐标签
                if (it.badge.isNotEmpty()) {
                    binding.badgeRecommended.visibility = View.VISIBLE
                    binding.badgeRecommended.text = it.badge
                } else {
                    binding.badgeRecommended.visibility = View.GONE
                }
            }
        }
    }
    
    private fun setupAnimations() {
        val popIn = AnimationUtils.loadAnimation(requireContext(), R.anim.pop_in)
        binding.cardRouteInfo.startAnimation(popIn)
        
        val slideUp = AnimationUtils.loadAnimation(requireContext(), R.anim.slide_up)
        binding.cardEstimates.startAnimation(slideUp)
    }
    
    private fun startTrip() {
        // 开始行程，导航到进行中页面
        val action = TripStartFragmentDirections.actionTripStartToInProgress()
        findNavController().navigate(action)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
