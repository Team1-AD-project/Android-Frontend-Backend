package com.ecogo.ui.fragments

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.ecogo.R
import com.ecogo.databinding.FragmentMapGreenGoBinding
import com.ecogo.data.NavigationState
import com.ecogo.viewmodel.NavigationViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.snackbar.Snackbar

/**
 * Map Green Go 主Fragment
 * 负责管理Google地图和导航状态
 */
class MapGreenGoFragment : Fragment(), OnMapReadyCallback {

    private var _binding: FragmentMapGreenGoBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var viewModel: NavigationViewModel
    private var googleMap: GoogleMap? = null
    private var currentState: NavigationState = NavigationState.IDLE
    
    // NUS 中心位置
    private val NUS_CENTER = LatLng(1.2966, 103.7764)
    
    // 权限请求
    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        when {
            permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true -> {
                // 精确位置权限已授予
                enableMyLocation()
            }
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true -> {
                // 粗略位置权限已授予
                enableMyLocation()
            }
            else -> {
                // 没有位置权限
                Snackbar.make(
                    binding.root,
                    "需要位置权限才能使用导航功能",
                    Snackbar.LENGTH_LONG
                ).show()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMapGreenGoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // 初始化ViewModel
        viewModel = ViewModelProvider(requireActivity())[NavigationViewModel::class.java]
        
        // 初始化地图
        val mapFragment = childFragmentManager
            .findFragmentById(R.id.mapView) as? SupportMapFragment
        mapFragment?.getMapAsync(this)
        
        setupUI()
        observeViewModel()
    }
    
    private fun setupUI() {
        // 搜索卡片点击
        binding.searchCard.setOnClickListener {
            // TODO: 导航到搜索界面
            Snackbar.make(binding.root, "打开搜索界面", Snackbar.LENGTH_SHORT).show()
        }
        
        // 我的位置按钮
        binding.btnMyLocation.setOnClickListener {
            googleMap?.let { map ->
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(NUS_CENTER, 15f))
            }
        }
    }
    
    private fun observeViewModel() {
        // 观察导航状态
        viewModel.navigationState.observe(viewLifecycleOwner) { state ->
            currentState = state
            updateUIForState(state)
        }
        
        // 观察当前路线
        viewModel.currentRoute.observe(viewLifecycleOwner) { route ->
            route?.let {
                displayRoute(it)
            }
        }
    }
    
    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        
        // 设置地图样式
        map.uiSettings.apply {
            isZoomControlsEnabled = false
            isMyLocationButtonEnabled = false
            isCompassEnabled = true
            isMapToolbarEnabled = false
        }
        
        // 移动相机到NUS中心
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(NUS_CENTER, 15f))
        
        // 请求位置权限
        checkAndRequestLocationPermission()
        
        // 地图点击监听
        map.setOnMapClickListener { latLng ->
            handleMapClick(latLng)
        }
    }
    
    private fun checkAndRequestLocationPermission() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                enableMyLocation()
            }
            else -> {
                locationPermissionRequest.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            }
        }
    }
    
    private fun enableMyLocation() {
        try {
            googleMap?.isMyLocationEnabled = true
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }
    
    private fun handleMapClick(latLng: LatLng) {
        when (currentState) {
            NavigationState.SEARCHING -> {
                // 选择地点作为目的地
                googleMap?.addMarker(
                    MarkerOptions()
                        .position(latLng)
                        .title("选中的位置")
                )
            }
            else -> {
                // 其他状态暂不处理
            }
        }
    }
    
    private fun updateUIForState(state: NavigationState) {
        when (state) {
            NavigationState.IDLE -> {
                // 显示搜索卡片
                binding.searchCard.visibility = View.VISIBLE
                binding.btnMyLocation.show()
            }
            NavigationState.SEARCHING -> {
                // 搜索模式
            }
            NavigationState.PLANNING -> {
                // 规划模式
            }
            NavigationState.NAVIGATING -> {
                // 导航模式 - 隐藏搜索卡片
                binding.searchCard.visibility = View.GONE
            }
            NavigationState.COMPLETED -> {
                // 完成状态
            }
        }
    }
    
    private fun displayRoute(route: com.ecogo.data.NavRoute) {
        googleMap?.let { map ->
            // 清除之前的标记
            map.clear()
            
            // 添加起点标记
            map.addMarker(
                MarkerOptions()
                    .position(LatLng(route.origin.latitude, route.origin.longitude))
                    .title(route.origin.name)
            )
            
            // 添加终点标记
            map.addMarker(
                MarkerOptions()
                    .position(LatLng(route.destination.latitude, route.destination.longitude))
                    .title(route.destination.name)
            )
            
            // TODO: 绘制路线
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
