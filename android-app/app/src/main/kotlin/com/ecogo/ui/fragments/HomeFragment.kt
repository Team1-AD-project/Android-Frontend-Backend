package com.ecogo.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.ecogo.R
import com.ecogo.data.MascotSize
import com.ecogo.data.MockData
import com.ecogo.data.Outfit
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
    
    // 用户当前装备（实际应用中从用户数据获取）
    private val currentOutfit = Outfit(head = "none", face = "none", body = "shirt_nus", badge = "a1")
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return try {
            Log.d("DEBUG_HOME", "HomeFragment onCreateView - inflating binding")
            Toast.makeText(context, "🏠 HomeFragment 正在加载...", Toast.LENGTH_SHORT).show()
            _binding = FragmentHomeBinding.inflate(inflater, container, false)
            Log.d("DEBUG_HOME", "HomeFragment binding inflated successfully")
            Toast.makeText(context, "✅ HomeFragment 加载成功！", Toast.LENGTH_SHORT).show()
            binding.root
        } catch (e: Exception) {
            Log.e("DEBUG_HOME", "HomeFragment onCreateView FAILED: ${e.message}", e)
            Toast.makeText(context, "❌ HomeFragment 创建失败: ${e.message}", Toast.LENGTH_LONG).show()
            throw e
        }
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        try {
            Log.d("DEBUG_HOME", "HomeFragment onViewCreated - starting setup")
            
            try {
                setupUI()
                Log.d("DEBUG_HOME", "setupUI completed")
            } catch (e: Exception) {
                Log.e("DEBUG_HOME", "setupUI FAILED: ${e.message}", e)
                Toast.makeText(requireContext(), "setupUI 失败: ${e.message}", Toast.LENGTH_SHORT).show()
            }
            
            try {
                setupRecyclerView()
                Log.d("DEBUG_HOME", "setupRecyclerView completed")
            } catch (e: Exception) {
                Log.e("DEBUG_HOME", "setupRecyclerView FAILED: ${e.message}", e)
            }
            
            try {
                setupAnimations()
                Log.d("DEBUG_HOME", "setupAnimations completed")
            } catch (e: Exception) {
                Log.e("DEBUG_HOME", "setupAnimations FAILED: ${e.message}", e)
            }
            
            try {
                setupActions()
                Log.d("DEBUG_HOME", "setupActions completed")
            } catch (e: Exception) {
                Log.e("DEBUG_HOME", "setupActions FAILED: ${e.message}", e)
            }
            
            try {
                loadData()
            } catch (e: Exception) {
                Log.e("DEBUG_HOME", "loadData FAILED: ${e.message}", e)
            }
            
            Log.d("DEBUG_HOME", "HomeFragment onViewCreated completed")
        } catch (e: Exception) {
            Log.e("DEBUG_HOME", "HomeFragment onViewCreated FAILED: ${e.message}", e)
            Toast.makeText(requireContext(), "HomeFragment 初始化失败: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
    
    private fun setupUI() {
        try {
            binding.textBusNumber.text = "D1"
            binding.textBusTime.text = "2 min"
            binding.textBusRoute.text = "to UTown"
            binding.textMonthlyPoints.text = "880"
            binding.textPointsChange.text = "+150 this week"
            binding.textSocScore.text = "5,530"
            binding.textSocRank.text = "Rank #1"
            binding.textLocation.text = getString(com.ecogo.R.string.home_location)
            
            Log.d("DEBUG_HOME", "Basic UI setup completed, setting up mascot")
            
            // 设置小狮子头像
            try {
                binding.mascotAvatar.apply {
                    mascotSize = MascotSize.MEDIUM
                    outfit = currentOutfit
                    Log.d("DEBUG_HOME", "Mascot configured, starting wave animation")
                    // 进入时播放挥手动画
                    waveAnimation()
                }
                Log.d("DEBUG_HOME", "Mascot setup completed")
            } catch (e: Exception) {
                Log.e("DEBUG_HOME", "Mascot setup FAILED: ${e.message}", e)
                Toast.makeText(requireContext(), "小狮子初始化失败: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Log.e("DEBUG_HOME", "setupUI FAILED: ${e.message}", e)
            Toast.makeText(requireContext(), "UI初始化失败: ${e.message}", Toast.LENGTH_SHORT).show()
            throw e
        }
    }
    
    private fun setupRecyclerView() {
        binding.recyclerHighlights.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = HighlightAdapter(emptyList()) { activity ->
                // 跳转到活动详情页，传递活动ID
                val action = HomeFragmentDirections.actionHomeToActivityDetail(activity.id ?: "")
                findNavController().navigate(action)
            }
        }
        binding.recyclerWalkingRoutes.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = WalkingRouteAdapter(emptyList()) { route ->
                findNavController().navigate(com.ecogo.R.id.routesFragment)
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
            kotlinx.coroutines.delay(200)
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
            // 跳转到活动详情页，传递活动ID
            val action = HomeFragmentDirections.actionHomeToActivityDetail(activity.id ?: "")
            findNavController().navigate(action)
        }
    }
    
    private suspend fun loadWalkingRoutes() {
        val walkingRoutes = repository.getWalkingRoutes().getOrElse { MockData.WALKING_ROUTES }
        binding.recyclerWalkingRoutes.adapter = WalkingRouteAdapter(walkingRoutes) { route ->
            findNavController().navigate(com.ecogo.R.id.routesFragment)
        }
    }

    private fun setupAnimations() {
        val breathe = AnimationUtils.loadAnimation(requireContext(), com.ecogo.R.anim.breathe)
        val slideUp = AnimationUtils.loadAnimation(requireContext(), com.ecogo.R.anim.slide_up)
        val popIn = AnimationUtils.loadAnimation(requireContext(), com.ecogo.R.anim.pop_in)

        binding.cardRecommendation.startAnimation(popIn)
        binding.cardNextBus.startAnimation(breathe)
        binding.cardMap.startAnimation(slideUp)
        // 小狮子自带动画，不需要额外启动
    }

    private fun setupActions() {
        binding.buttonOpenMap.setOnClickListener {
            // 地图功能临时禁用
            // findNavController().navigate(com.ecogo.R.id.mapFragment)
            android.widget.Toast.makeText(requireContext(), "地图功能正在开发中", android.widget.Toast.LENGTH_SHORT).show()
        }
        binding.textViewAll.setOnClickListener {
            // 跳转到月度亮点页面
            findNavController().navigate(R.id.action_home_to_monthlyHighlights)
        }
        
        // 点击小狮子跳转到 Profile
        binding.mascotAvatar.setOnClickListener {
            findNavController().navigate(com.ecogo.R.id.profileFragment)
        }
        binding.buttonGo.setOnClickListener {
            val destination = binding.editPlan.text?.toString()?.trim().orEmpty()
            if (destination.isNotEmpty()) {
                requestRecommendation(destination)
                binding.editPlan.text?.clear()
            }
        }
        binding.buttonPlanMic?.setOnClickListener {
            // 语音输入占位：可后续接入语音识别
        }
        
        // 月度积分卡片 -> 个人资料页
        binding.cardMonthlyPoints.setOnClickListener {
            findNavController().navigate(com.ecogo.R.id.profileFragment)
        }
        
        // 社区分数卡片 -> 社区页
        binding.cardCommunityScore.setOnClickListener {
            findNavController().navigate(com.ecogo.R.id.communityFragment)
        }
        
        // 下一班巴士卡片 -> 路线页
        binding.cardNextBus.setOnClickListener {
            findNavController().navigate(com.ecogo.R.id.routesFragment)
        }
        
        // 地图预览卡片 -> 地图页（整个卡片可点击）
        binding.cardMap.setOnClickListener {
            // 地图功能临时禁用
            // findNavController().navigate(com.ecogo.R.id.mapFragment)
            android.widget.Toast.makeText(requireContext(), "地图功能正在开发中", android.widget.Toast.LENGTH_SHORT).show()
        }
        
        // === 新功能点击事件 ===
        
        // 每日签到按钮 - 跳转到完整日历界面
        binding.buttonCheckin.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_checkInCalendar)
        }
        
        // 通知横幅关闭按钮
        binding.buttonCloseNotification.setOnClickListener {
            binding.cardNotification.visibility = View.GONE
        }
        
        // 碳足迹卡片点击
        binding.cardCarbonFootprint.setOnClickListener {
            findNavController().navigate(com.ecogo.R.id.profileFragment)
        }
        
        // 天气卡片点击
        binding.cardWeather.setOnClickListener {
            // 可以跳转到天气详情或地图页
        }
        
        // 今日目标卡片点击
        binding.cardDailyGoal.setOnClickListener {
            findNavController().navigate(com.ecogo.R.id.profileFragment)
        }
        
        // === 快捷入口点击事件 ===
        
        // Voucher快捷入口
        binding.cardVoucherShortcut.setOnClickListener {
            findNavController().navigate(com.ecogo.R.id.action_home_to_voucher)
        }
        
        // Challenges快捷入口
        binding.cardChallengesShortcut.setOnClickListener {
            findNavController().navigate(com.ecogo.R.id.action_home_to_challenges)
        }
    }

    private fun requestRecommendation(destination: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            val response = repository.getRecommendation(RecommendationRequest(destination)).getOrElse { null }
            if (response != null) {
                val slideUp = AnimationUtils.loadAnimation(requireContext(), com.ecogo.R.anim.slide_up)
                binding.layoutRecommendationResult.visibility = View.VISIBLE
                binding.layoutRecommendationResult.startAnimation(slideUp)
                binding.textRecommendationTag.text = response.tag
                binding.textRecommendationResult.text = response.text
                
                binding.textAskAnother.setOnClickListener {
                    binding.layoutRecommendationResult.visibility = View.GONE
                }
            }
        }
    }
    
    // === 新功能辅助方法 ===
    
    private fun loadCheckInStatus() {
        viewLifecycleOwner.lifecycleScope.launch {
            val status = repository.getCheckInStatus("user123").getOrNull()
            if (status != null && status.lastCheckInDate == java.time.LocalDate.now().toString()) {
                // 已签到，显示状态
                binding.layoutCheckinStatus.visibility = View.VISIBLE
                binding.textCheckinStatus.text = "已签到 ${status.consecutiveDays} 天 · 今日获得 ${status.pointsEarned} 积分"
            }
        }
    }
    
    private fun performCheckIn() {
        viewLifecycleOwner.lifecycleScope.launch {
            val response = repository.checkIn("user123").getOrNull()
            if (response != null && response.success) {
                // 签到成功动画
                val popIn = AnimationUtils.loadAnimation(requireContext(), com.ecogo.R.anim.pop_in)
                binding.layoutCheckinStatus.visibility = View.VISIBLE
                binding.layoutCheckinStatus.startAnimation(popIn)
                binding.textCheckinStatus.text = "已签到 ${response.consecutiveDays} 天 · 今日获得 ${response.pointsEarned} 积分"
            }
        }
    }
    
    private fun loadNotifications() {
        viewLifecycleOwner.lifecycleScope.launch {
            val notifications = repository.getNotifications("user123").getOrNull()
            val unreadNotif = notifications?.firstOrNull()
            if (unreadNotif != null) {
                binding.cardNotification.visibility = View.VISIBLE
                binding.textNotificationTitle.text = unreadNotif.title
                binding.textNotificationMessage.text = unreadNotif.message
            }
        }
    }
    
    private fun loadDailyGoal() {
        viewLifecycleOwner.lifecycleScope.launch {
            val goal = repository.getDailyGoal("user123").getOrNull()
            if (goal != null) {
                val stepProgress = (goal.currentSteps.toFloat() / goal.stepGoal * 100).toInt().coerceIn(0, 100)
                val tripProgress = (goal.currentTrips.toFloat() / goal.tripGoal * 100).toInt().coerceIn(0, 100)
                val co2Progress = (goal.currentCo2Saved / goal.co2SavedGoal * 100).toInt().coerceIn(0, 100)

                binding.progressSteps.progress = stepProgress
                binding.progressTrips.progress = tripProgress
                binding.progressCo2.progress = co2Progress
                binding.textStepsProgress.text = "✔ ${goal.currentTrips}/${goal.tripGoal} eco trips"
                binding.textTripsProgress.text = "✔ ${"%.1f".format(goal.currentTrips.toFloat())}/${goal.tripGoal} eco trips"
                binding.textCo2Progress.text = "✔ ${"%.2f".format(goal.currentCo2Saved)} kg saved"
            }
        }
    }
    
    private fun loadCarbonFootprint() {
        viewLifecycleOwner.lifecycleScope.launch {
            val carbon = repository.getCarbonFootprint("user123", "monthly").getOrNull()
            if (carbon != null) {
                binding.textCo2Saved.text = "${"%.1f".format(carbon.co2Saved)} kg"
                binding.textTreeEquivalent.text = "${carbon.equivalentTrees} trees"
                val totalTrips = carbon.tripsByBus + carbon.tripsByWalking + carbon.tripsByBicycle
                binding.textCarbonPeriod.text = "This month · $totalTrips eco trips"
            }
        }
    }
    
    private fun loadWeather() {
        viewLifecycleOwner.lifecycleScope.launch {
            val weather = repository.getWeather("NUS").getOrNull()
            if (weather != null) {
                binding.textTemperature.text = "${weather.temperature}°C"
                binding.textWeatherCondition.text = weather.condition
                binding.textAqiValue.text = "AQI ${weather.aqi}"
                binding.textHumidity.text = "Humidity ${weather.humidity}%"
                binding.textWeatherRecommendation.text = weather.recommendation
                
                // 根据天气条件更新图标 (可选)
                // binding.imageWeatherIcon.setImageResource(...)
            }
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
