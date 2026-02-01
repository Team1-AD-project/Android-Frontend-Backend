package com.ecogo.ui.fragments.navigation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.ecogo.R
import com.ecogo.data.MockData
import com.ecogo.data.NavLocation
import com.ecogo.data.TransportMode
import com.ecogo.databinding.FragmentRoutePlannerBinding
import com.ecogo.ui.adapters.RouteOptionAdapter
import com.ecogo.viewmodel.NavigationViewModel
import com.google.android.material.snackbar.Snackbar

/**
 * 路线规划Fragment
 */
class RoutePlannerFragment : Fragment() {

    private var _binding: FragmentRoutePlannerBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var viewModel: NavigationViewModel
    private lateinit var routeAdapter: RouteOptionAdapter
    
    private var selectedMode = TransportMode.WALK

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRoutePlannerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        viewModel = ViewModelProvider(requireActivity())[NavigationViewModel::class.java]
        
        setupRecyclerView()
        setupUI()
        observeViewModel()
        
        // 设置默认地点（用于演示）
        setDefaultLocations()
    }
    
    private fun setupRecyclerView() {
        routeAdapter = RouteOptionAdapter { route ->
            viewModel.selectRoute(route)
            binding.btnStartNavigation.isEnabled = true
        }
        
        binding.recyclerRoutes.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = routeAdapter
        }
    }
    
    private fun setupUI() {
        // 起点容器点击
        binding.originContainer.setOnClickListener {
            // TODO: 打开地点搜索界面（选择起点）
            Snackbar.make(binding.root, "选择起点", Snackbar.LENGTH_SHORT).show()
        }
        
        // 终点容器点击
        binding.destinationContainer.setOnClickListener {
            // TODO: 打开地点搜索界面（选择终点）
            Snackbar.make(binding.root, "选择终点", Snackbar.LENGTH_SHORT).show()
        }
        
        // 交通方式选择
        binding.modeWalk.setOnClickListener {
            selectMode(TransportMode.WALK)
        }
        
        binding.modeCycle.setOnClickListener {
            selectMode(TransportMode.CYCLE)
        }
        
        binding.modeBus.setOnClickListener {
            selectMode(TransportMode.BUS)
        }
        
        // 开始导航按钮
        binding.btnStartNavigation.setOnClickListener {
            viewModel.startNavigation()
            // TODO: 导航到实时导航界面
            Snackbar.make(binding.root, "开始导航", Snackbar.LENGTH_SHORT).show()
        }
    }
    
    private fun selectMode(mode: TransportMode) {
        selectedMode = mode
        viewModel.setTransportMode(mode)
        
        // 更新UI选中状态
        val primaryColor = requireContext().getColor(R.color.primary)
        val whiteColor = requireContext().getColor(android.R.color.white)
        
        binding.modeWalk.setCardBackgroundColor(
            if (mode == TransportMode.WALK) primaryColor else whiteColor
        )
        binding.modeCycle.setCardBackgroundColor(
            if (mode == TransportMode.CYCLE) primaryColor else whiteColor
        )
        binding.modeBus.setCardBackgroundColor(
            if (mode == TransportMode.BUS) primaryColor else whiteColor
        )
    }
    
    private fun observeViewModel() {
        // 观察起点
        viewModel.selectedOrigin.observe(viewLifecycleOwner) { origin ->
            origin?.let {
                binding.textOrigin.text = it.name
            }
        }
        
        // 观察终点
        viewModel.selectedDestination.observe(viewLifecycleOwner) { destination ->
            destination?.let {
                binding.textDestination.text = it.name
            }
        }
        
        // 观察路线选项
        viewModel.routeOptions.observe(viewLifecycleOwner) { routes ->
            routeAdapter.updateRoutes(routes)
        }
    }
    
    private fun setDefaultLocations() {
        // 设置默认起点和终点用于演示
        val origin = MockData.CAMPUS_LOCATIONS.find { it.id == "4" } // PGP
        val destination = MockData.CAMPUS_LOCATIONS.find { it.id == "1" } // COM1
        
        origin?.let { viewModel.setOrigin(it) }
        destination?.let { viewModel.setDestination(it) }
        
        // 默认选择步行
        selectMode(TransportMode.WALK)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
