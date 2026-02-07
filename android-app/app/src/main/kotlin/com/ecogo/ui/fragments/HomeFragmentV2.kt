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
import androidx.viewpager2.widget.ViewPager2
import com.ecogo.R
import com.ecogo.data.HomeBanner
import com.ecogo.data.MascotSize
import com.ecogo.data.MockData
import com.ecogo.data.Outfit
import com.ecogo.data.RecommendationRequest
import com.ecogo.databinding.FragmentHomeV2Binding
import com.ecogo.ui.adapters.HomeBannerAdapter
import com.ecogo.ui.adapters.HighlightAdapter
import com.ecogo.ui.adapters.WalkingRouteAdapter
import com.ecogo.repository.EcoGoRepository
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class HomeFragmentV2 : Fragment() {
    
    private var _binding: FragmentHomeV2Binding? = null
    private val binding get() = _binding!!
    private val repository = EcoGoRepository()
    
    // Banner components
    private lateinit var bannerAdapter: HomeBannerAdapter
    private var isAutoScrolling = true
    
    // 用户当前装备（实际应用中从用户数据获取）
    private val currentOutfit = Outfit(head = "none", face = "none", body = "shirt_nus", badge = "a1")
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        android.util.Log.d("DEBUG_HOME_V2", "HomeFragmentV2 onCreateView - inflating binding")
        _binding = FragmentHomeV2Binding.inflate(inflater, container, false)
        android.util.Log.d("DEBUG_HOME_V2", "HomeFragmentV2 binding inflated successfully")
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        android.util.Log.d("DEBUG_HOME_V2", "HomeFragmentV2 onViewCreated - starting setup")
        
        setupBanner()
        setupUI()
        setupRecyclerView()
        setupAnimations()
        setupActions()
        loadData()
        
        android.util.Log.d("DEBUG_HOME_V2", "HomeFragmentV2 onViewCreated completed")
    }
    
    private fun setupBanner() {
        bannerAdapter = HomeBannerAdapter { banner ->
            handleBannerClick(banner)
        }
        
        binding.bannerViewpager.apply {
            adapter = bannerAdapter
            offscreenPageLimit = 1
            
            // Stop auto-scroll when user interacts
            registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageScrollStateChanged(state: Int) {
                    if (state == ViewPager2.SCROLL_STATE_DRAGGING) {
                        isAutoScrolling = false
                    }
                }
            })
        }
        
        // Link indicator with ViewPager2
        TabLayoutMediator(
            binding.bannerIndicator,
            binding.bannerViewpager
        ) { _, _ -> }.attach()
        
        // Load banner data
        bannerAdapter.submitList(MockData.HOME_BANNERS)
        
        // Start auto-scroll
        startBannerAutoScroll()
    }
    
    private fun startBannerAutoScroll() {
        viewLifecycleOwner.lifecycleScope.launch {
            while (isAutoScrolling) {
                delay(4000) // 4 seconds per slide
                if (isAutoScrolling && _binding != null) {
                    val currentItem = binding.bannerViewpager.currentItem
                    val itemCount = bannerAdapter.itemCount
                    if (itemCount > 0) {
                        val nextItem = (currentItem + 1) % itemCount
                        binding.bannerViewpager.setCurrentItem(nextItem, true)
                    }
                }
            }
        }
    }
    
    private fun handleBannerClick(banner: HomeBanner) {
        when (banner.actionTarget) {
            "challenges" -> findNavController().navigate(R.id.challengesFragment)
            "vouchers" -> findNavController().navigate(R.id.voucherFragment)
            "community" -> findNavController().navigate(R.id.communityFragment)
            "routes" -> findNavController().navigate(R.id.routesFragment)
            "profile" -> findNavController().navigate(R.id.profileFragment)
            // Add more targets as needed
        }
    }
    
    private fun setupUI() {
        binding.textBusNumber.text = "D1"
        binding.textBusTime.text = "2 min"
        binding.textBusRoute.text = "to UTown"
        binding.textMonthlyPoints.text = "880"
        binding.textPointsChange.text = "+150 this week"
        binding.textSocScore.text = "5,530"
        binding.textSocRank.text = "Rank #1"
        binding.textLocation.text = getString(R.string.home_location)
        
        // 设置小狮子头像
        binding.mascotAvatar.apply {
            mascotSize = MascotSize.MEDIUM
            outfit = currentOutfit
            // 进入时播放挥手动画
            waveAnimation()
        }
    }
    
    private fun setupRecyclerView() {
        binding.recyclerHighlights.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = HighlightAdapter(emptyList()) { activity ->
                findNavController().navigate(R.id.activitiesFragment)
            }
        }
        binding.recyclerWalkingRoutes.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = WalkingRouteAdapter(emptyList()) { route ->
                findNavController().navigate(R.id.routesFragment)
            }
        }
    }

    /**
     * 优化：使用并发加载和懒加载策略
     * 1. 关键数据立即加载（巴士信息）
     * 2. 次要数据并发加载
     * 3. 非关键数据延迟加载
     */
    private fun loadData() {
        viewLifecycleOwner.lifecycleScope.launch {
            // 第一优先级：立即加载关键数据（巴士信息）
            loadBusInfo()
            
            // 第二优先级：并发加载次要数据
            launch { loadActivities() }
            launch { loadWalkingRoutes() }
            
            // 第三优先级：延迟加载非关键数据（200ms后）
            delay(200)
            launch { loadCheckInStatus() }
            launch { loadNotifications() }
            launch { loadDailyGoal() }
            launch { loadCarbonFootprint() }
            launch { loadWeather() }
        }
    }
    
    private suspend fun loadBusInfo() {
        val routesResult = repository.getBusRoutes().getOrElse { MockData.ROUTES }
        val firstRoute = routesResult.firstOrNull()
        if (firstRoute != null) {
            binding.textBusNumber.text = firstRoute.name
            binding.textBusTime.text = firstRoute.time ?: "${firstRoute.nextArrival} min"
            binding.textBusRoute.text = if (firstRoute.to.isNotEmpty()) "to ${firstRoute.to}" else "to ${firstRoute.name}"
        }
    }
    
    private suspend fun loadActivities() {
        val activitiesResult = repository.getAllActivities().getOrElse { MockData.ACTIVITIES }
        binding.recyclerHighlights.adapter = HighlightAdapter(activitiesResult.take(3)) { activity ->
            findNavController().navigate(R.id.activitiesFragment)
        }
    }
    
    private suspend fun loadWalkingRoutes() {
        val walkingRoutes = repository.getWalkingRoutes().getOrElse { MockData.WALKING_ROUTES }
        binding.recyclerWalkingRoutes.adapter = WalkingRouteAdapter(walkingRoutes) { route ->
            findNavController().navigate(R.id.routesFragment)
        }
    }

    private fun setupAnimations() {
        val breathe = AnimationUtils.loadAnimation(requireContext(), R.anim.breathe)
        val slideUp = AnimationUtils.loadAnimation(requireContext(), R.anim.slide_up)
        val popIn = AnimationUtils.loadAnimation(requireContext(), R.anim.pop_in)

        binding.cardRecommendation.startAnimation(popIn)
        binding.cardNextBus.startAnimation(breathe)
        binding.cardMap.startAnimation(slideUp)
    }

    private fun setupActions() {
        binding.buttonOpenMap.setOnClickListener {
            findNavController().navigate(R.id.action_homeV2_to_routePlanner)
        }

        // 点击地图卡片也可以进入路线规划
        binding.cardMap.setOnClickListener {
            findNavController().navigate(R.id.action_homeV2_to_routePlanner)
        }

        binding.cardVoucherShortcut.setOnClickListener {
            findNavController().navigate(R.id.voucherFragment)
        }
        
        binding.cardChallengesShortcut.setOnClickListener {
            findNavController().navigate(R.id.challengesFragment)
        }
        
        binding.mascotAvatar.setOnClickListener {
            findNavController().navigate(R.id.profileFragment)
        }
        
        binding.buttonCheckin.setOnClickListener {
            findNavController().navigate(R.id.checkInCalendarFragment)
        }
    }
    
    private suspend fun loadCheckInStatus() {
        // Load check-in status
    }
    
    private suspend fun loadNotifications() {
        // Load notifications
    }
    
    private suspend fun loadDailyGoal() {
        // Load daily goal progress
    }
    
    private suspend fun loadCarbonFootprint() {
        val footprint = MockData.CARBON_FOOTPRINT
        binding.textCo2Saved.text = "${footprint.co2Saved} kg"
        binding.textTreeEquivalent.text = "${footprint.equivalentTrees} trees"
    }
    
    private suspend fun loadWeather() {
        val weather = MockData.WEATHER
        binding.textTemperature.text = "${weather.temperature}°C"
        binding.textWeatherCondition.text = weather.condition
        binding.textHumidity.text = "Humidity ${weather.humidity}%"
        binding.textAqiValue.text = "AQI ${weather.aqi}"
        binding.textWeatherRecommendation.text = weather.recommendation
    }
    
    override fun onDestroyView() {
        isAutoScrolling = false
        _binding = null
        super.onDestroyView()
    }
}
