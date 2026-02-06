package com.ecogo.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.ecogo.R
import com.ecogo.databinding.FragmentMonthlyHighlightsBinding
import com.ecogo.repository.EcoGoRepository
import com.ecogo.ui.adapters.MonthlyActivityAdapter
import com.ecogo.ui.adapters.MonthStatAdapter
import com.ecogo.ui.adapters.PopularRouteAdapter
import com.ecogo.ui.adapters.MilestoneAdapter
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * 月度亮点页面
 * 展示本月精选活动、统计数据和成就
 */
class MonthlyHighlightsFragment : Fragment() {

    private var _binding: FragmentMonthlyHighlightsBinding? = null
    private val binding get() = _binding!!
    
    private val repository = EcoGoRepository()
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMonthlyHighlightsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupUI()
        setupRecyclerViews()
        loadData()
        setupAnimations()
    }
    
    private fun setupUI() {
        // 返回按钮
        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }
        
        // 设置当前月份
        val currentMonth = LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM yyyy"))
        binding.textCurrentMonth.text = currentMonth
        
        // 上个月/下个月按钮（可选功能）
        binding.btnPreviousMonth.setOnClickListener {
            // TODO: 加载上个月数据
            android.widget.Toast.makeText(requireContext(), "查看上个月", android.widget.Toast.LENGTH_SHORT).show()
        }
        
        binding.btnNextMonth.setOnClickListener {
            // TODO: 加载下个月数据（如果有预告）
            android.widget.Toast.makeText(requireContext(), "下个月即将推出", android.widget.Toast.LENGTH_SHORT).show()
        }
        
        // 查看全部已加入的活动
        binding.btnViewAllActivities.setOnClickListener {
            val action = MonthlyHighlightsFragmentDirections
                .actionMonthlyHighlightsToJoinedActivities(showJoinedOnly = true)
            findNavController().navigate(action)
        }
        
        // 查看完整排行榜
        binding.btnViewFullLeaderboard.setOnClickListener {
            findNavController().navigate(R.id.communityFragment)
        }
        
        // 挑战卡片点击事件
        binding.cardChallenge1.setOnClickListener {
            findNavController().navigate(R.id.challengesFragment)
        }
        
        binding.cardChallenge2.setOnClickListener {
            findNavController().navigate(R.id.challengesFragment)
        }
        
        binding.cardChallenge3.setOnClickListener {
            findNavController().navigate(R.id.challengesFragment)
        }
    }
    
    private fun setupRecyclerViews() {
        // 月度统计卡片（横向滚动）
        binding.recyclerMonthStats.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = MonthStatAdapter(emptyList())
        }
        
        // 精选活动列表（网格布局）
        binding.recyclerFeaturedActivities.apply {
            layoutManager = GridLayoutManager(context, 2)
            adapter = MonthlyActivityAdapter(emptyList()) { activity ->
                // 跳转到活动详情
                val action = MonthlyHighlightsFragmentDirections
                    .actionMonthlyHighlightsToActivityDetail(activity.id ?: "")
                findNavController().navigate(action)
            }
        }
        
        // 流行路线列表
        binding.recyclerPopularRoutes.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = PopularRouteAdapter(emptyList()) { route ->
                // 可以跳转到路线详情或路线规划
                android.widget.Toast.makeText(requireContext(), "Route: ${route.name}", android.widget.Toast.LENGTH_SHORT).show()
            }
        }
        
        // 里程碑时间线
        binding.recyclerMilestones.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = MilestoneAdapter(emptyList())
        }
    }
    
    private fun loadData() {
        viewLifecycleOwner.lifecycleScope.launch {
            // 加载月度统计
            loadMonthlyStats()
            
            // 加载精选活动
            loadFeaturedActivities()
            
            // 加载用户本月成就
            loadMonthlyAchievements()
            
            // 加载挑战进度
            loadChallenges()
            
            // 加载流行路线
            loadPopularRoutes()
            
            // 加载里程碑
            loadMilestones()
        }
    }
    
    private suspend fun loadMonthlyStats() {
        val userId = com.ecogo.auth.TokenManager.getUserId() ?: "user123"
        val stats = mutableListOf<MonthStat>()

        // 1. 获取真实积分数据
        val pointsResult = repository.getCurrentPoints().getOrNull()
        val userProfile = repository.getMobileUserProfile(userId).getOrNull()

        val currentPoints = pointsResult?.currentPoints
            ?: userProfile?.userInfo?.currentPoints?.toLong()
            ?: 0L

        stats.add(MonthStat(
            icon = "⭐",
            title = "Total Points",
            value = "$currentPoints",
            subtitle = "current balance",
            color = "#FCD34D"
        ))

        // 2. 获取用户已加入的活动数量
        val joinedActivitiesCount = repository.getJoinedActivitiesCount(userId).getOrNull() ?: 0

        stats.add(MonthStat(
            icon = "🎯",
            title = "Activities",
            value = "$joinedActivitiesCount",
            subtitle = "joined this month",
            color = "#A78BFA"
        ))

        // 3. 获取用户已加入的挑战数量 (Mock data for now)
        val joinedChallengesCount = 3 // TODO: Replace with real API call when challenges table is ready

        stats.add(MonthStat(
            icon = "🏆",
            title = "Challenges",
            value = "$joinedChallengesCount",
            subtitle = "joined this month",
            color = "#F97316"
        ))

        // 4. CO2减排 (尝试从API获取，失败则使用mock)
        val carbon = repository.getCarbonFootprint(userId, "monthly").getOrNull()
        if (carbon != null) {
            stats.add(MonthStat(
                icon = "🌱",
                title = "CO₂ Saved",
                value = "${String.format("%.1f", carbon.co2Saved)} kg",
                subtitle = "${carbon.equivalentTrees} trees equivalent",
                color = "#34D399"
            ))

            // 环保出行次数
            val totalTrips = carbon.tripsByBus + carbon.tripsByWalking + carbon.tripsByBicycle
            stats.add(MonthStat(
                icon = "🚌",
                title = "Eco Trips",
                value = "$totalTrips",
                subtitle = "trips this month",
                color = "#60A5FA"
            ))
        }

        // 5. 连续签到天数 (尝试从API获取)
        val checkInStatus = repository.getCheckInStatus(userId).getOrNull()
        if (checkInStatus != null) {
            stats.add(MonthStat(
                icon = "🔥",
                title = "Check-in Streak",
                value = "${checkInStatus.consecutiveDays}",
                subtitle = "days in a row",
                color = "#F87171"
            ))
        }

        (binding.recyclerMonthStats.adapter as? MonthStatAdapter)?.updateData(stats)
    }
    
    private suspend fun loadFeaturedActivities() {
        val userId = com.ecogo.auth.TokenManager.getUserId() ?: "user123"
        val activitiesResult = repository.getAllActivities().getOrElse { emptyList() }

        // 筛选用户已加入的活动
        val joinedActivities = activitiesResult
            .filter { it.participantIds.contains(userId) }
            .sortedByDescending { it.rewardCredits }

        // 外面最多显示2个
        val displayActivities = joinedActivities.take(2)

        if (joinedActivities.isEmpty()) {
            // 没有加入任何活动，隐藏RecyclerView，显示空状态
            binding.recyclerFeaturedActivities.visibility = View.GONE
            binding.textNoJoinedActivities.visibility = View.VISIBLE
            binding.textActivityCount.text = "0 Joined Activities"
        } else {
            binding.recyclerFeaturedActivities.visibility = View.VISIBLE
            binding.textNoJoinedActivities.visibility = View.GONE
            (binding.recyclerFeaturedActivities.adapter as? MonthlyActivityAdapter)?.updateData(displayActivities)
            binding.textActivityCount.text = "${joinedActivities.size} Joined Activities"
        }
    }
    
    private suspend fun loadMonthlyAchievements() {
        // TODO: 从后端加载本月解锁的成就
        // 这里使用模拟数据
        binding.textAchievementsUnlocked.text = "3 Achievements Unlocked This Month"
        
        // 显示成就徽章（如果有）
        binding.layoutAchievements.visibility = View.VISIBLE
    }
    
    private suspend fun loadChallenges() {
        // TODO: 从后端加载挑战数据
        // 这里使用模拟数据展示
        val carbon = repository.getCarbonFootprint("user123", "monthly").getOrNull()
        val checkInStatus = repository.getCheckInStatus("user123").getOrNull()
        
        // 更新挑战1：巴士出行
        if (carbon != null) {
            val busTrips = carbon.tripsByBus
            val progress = (busTrips.toFloat() / 20 * 100).toInt().coerceIn(0, 100)
            binding.progressChallenge1.progress = progress
            binding.textChallenge1Progress.text = "$busTrips/20 trips · 🎁 +200 pts"
        }
        
        // 更新挑战2：CO2减排
        if (carbon != null) {
            val co2Saved = carbon.co2Saved
            val progress = (co2Saved / 50 * 100).toInt().coerceIn(0, 100)
            binding.progressChallenge2.progress = progress
            binding.textChallenge2Progress.text = "${String.format("%.1f", co2Saved)}/50 kg · 🎁 +300 pts"
        }
        
        // 更新挑战3：连续签到
        if (checkInStatus != null) {
            val consecutiveDays = checkInStatus.consecutiveDays
            val progress = (consecutiveDays.toFloat() / 15 * 100).toInt().coerceIn(0, 100)
            binding.progressChallenge3.progress = progress
            binding.textChallenge3Progress.text = "$consecutiveDays/15 days · 🎁 +500 pts"
        }
    }
    
    private suspend fun loadPopularRoutes() {
        val routes = repository.getBusRoutes().getOrElse { emptyList() }
        
        // 取前5条热门路线
        val popularRoutes = routes.take(5)
        (binding.recyclerPopularRoutes.adapter as? PopularRouteAdapter)?.updateData(popularRoutes)
    }
    
    private fun loadMilestones() {
        // TODO: 从后端加载实际里程碑数据
        // 这里使用模拟数据
        val milestones = listOf(
            com.ecogo.ui.adapters.Milestone(
                icon = "🎉",
                title = "First Trip of the Month",
                description = "Started the month strong! Earned +10 bonus points.",
                date = "Feb 1",
                reward = "🎁 +10 pts"
            ),
            com.ecogo.ui.adapters.Milestone(
                icon = "🌟",
                title = "10 Eco Trips Milestone",
                description = "Completed 10 eco-friendly trips. You're making a difference!",
                date = "Feb 5",
                reward = "🎁 +50 pts"
            ),
            com.ecogo.ui.adapters.Milestone(
                icon = "🏆",
                title = "New Badge Unlocked",
                description = "Unlocked the 'Green Commuter' badge for consistent eco travel.",
                date = "Feb 8",
                reward = "🏅 Badge"
            ),
            com.ecogo.ui.adapters.Milestone(
                icon = "🔥",
                title = "7-Day Streak",
                description = "Checked in for 7 consecutive days. Keep it up!",
                date = "Feb 12",
                reward = "🎁 +100 pts"
            ),
            com.ecogo.ui.adapters.Milestone(
                icon = "🚌",
                title = "Tried New Route",
                description = "Explored a new bus route. Adventure awaits!",
                date = "Feb 15"
            ),
            com.ecogo.ui.adapters.Milestone(
                icon = "💚",
                title = "Joined Campus Clean-Up",
                description = "Participated in the monthly campus clean-up activity.",
                date = "Feb 18",
                reward = "🎁 +150 pts"
            )
        )
        
        (binding.recyclerMilestones.adapter as? com.ecogo.ui.adapters.MilestoneAdapter)?.updateData(milestones)
    }
    
    private fun setupAnimations() {
        val slideUp = AnimationUtils.loadAnimation(requireContext(), R.anim.slide_up)
        val popIn = AnimationUtils.loadAnimation(requireContext(), R.anim.pop_in)
        
        binding.cardHeader.startAnimation(slideUp)
        binding.cardStats.startAnimation(popIn)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

// 月度统计数据类
data class MonthStat(
    val icon: String,
    val title: String,
    val value: String,
    val subtitle: String,
    val color: String
)
