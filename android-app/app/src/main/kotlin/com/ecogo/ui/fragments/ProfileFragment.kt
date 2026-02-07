package com.ecogo.ui.fragments

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.animation.AnimationUtils
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.ecogo.R
import com.ecogo.api.MascotOutfitDto
import com.ecogo.api.UpdateProfileRequest
import com.ecogo.auth.TokenManager
import com.ecogo.data.Achievement
import com.ecogo.data.FacultyData
import com.ecogo.data.MockData
import com.ecogo.data.Outfit
import com.ecogo.data.ShopItem
import com.ecogo.databinding.FragmentProfileBinding
import com.ecogo.repository.EcoGoRepository
import com.ecogo.ui.adapters.AchievementAdapter
import com.ecogo.ui.adapters.FacultyOutfitAdapter
import com.ecogo.ui.adapters.HistoryAdapter
import com.ecogo.ui.adapters.ShopItemAdapter
import kotlinx.coroutines.launch

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private val repository = EcoGoRepository()

    // State management — loaded from API, persisted on changes
    private var currentPoints = 0
    private val inventory = mutableListOf<String>()
    private val currentOutfit = mutableMapOf(
        "head" to "none",
        "face" to "none",
        "body" to "none",
        "badge" to "none"
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUI()
        setupFacultyOutfitsRecyclerView()
        setupShopRecyclerView()
        setupBadgeRecyclerView()
        setupHistoryRecyclerView()
        setupTabs()
        setupAnimations()
        setupActions() // 移除重复的setupActions()调用
        loadHistory()
        loadUserProfile()

        Log.d("ProfileFragment", "Profile screen initialized with ${inventory.size} owned items")
    }


    private fun loadUserProfile() {
        // First restore from local cache immediately (fast UI)
        restoreFromLocalCache()

        // val userId = TokenManager.getUserId() ?: return // userId not needed for API call
        viewLifecycleOwner.lifecycleScope.launch {
            val result = repository.getMobileUserProfile()
            val profile = result.getOrNull()
            if (profile != null) {
                val userInfo = profile.userInfo

                // Update points
                currentPoints = userInfo.currentPoints
                binding.textPoints.text = currentPoints.toString()

                // Update basic info
                binding.textName.text = userInfo.nickname

                // Update faculty if available
                userInfo.faculty?.let { faculty ->
                    binding.textFaculty.text = "$faculty • Year 2"
                }

                // Restore mascot outfit from server
                userInfo.mascotOutfit?.let { outfit ->
                    currentOutfit["head"] = outfit.head
                    currentOutfit["face"] = outfit.face
                    currentOutfit["body"] = outfit.body
                    currentOutfit["badge"] = outfit.badge
                    updateMascotOutfit()
                }

                // Restore inventory from server
                userInfo.inventory?.let { items ->
                    inventory.clear()
                    inventory.addAll(items)
                    refreshShopAdapter()
                }

                // Sync local cache
                saveToLocalCache()

                Log.d("ProfileFragment", "Loaded user profile: ${userInfo.nickname}, points: $currentPoints, inventory: ${inventory.size} items")
            }
        }
    }

    /**
     * Persist current mascot outfit & inventory to backend.
     * Called after any outfit/inventory change (equip, buy, badge toggle).
     */
    private fun persistMascotState() {
        val userId = TokenManager.getUserId() ?: return

        // Save to local cache immediately for next quick restore
        saveToLocalCache()

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val request = UpdateProfileRequest(
                    mascotOutfit = MascotOutfitDto(
                        head = currentOutfit["head"] ?: "none",
                        face = currentOutfit["face"] ?: "none",
                        body = currentOutfit["body"] ?: "none",
                        badge = currentOutfit["badge"] ?: "none"
                    ),
                    inventory = inventory.toList()
                )
                val result = repository.updateUserProfile(userId, request)
                if (result.isSuccess) {
                    Log.d("ProfileFragment", "Mascot state persisted to server")
                } else {
                    Log.w("ProfileFragment", "Failed to persist mascot state: ${result.exceptionOrNull()?.message}")
                }
            } catch (e: Exception) {
                Log.e("ProfileFragment", "Error persisting mascot state: ${e.message}", e)
            }
        }
    }

    private fun saveToLocalCache() {
        try {
            val prefs = requireContext().getSharedPreferences("ecogo_mascot", android.content.Context.MODE_PRIVATE)
            prefs.edit()
                .putString("outfit_head", currentOutfit["head"])
                .putString("outfit_face", currentOutfit["face"])
                .putString("outfit_body", currentOutfit["body"])
                .putString("outfit_badge", currentOutfit["badge"])
                .putStringSet("inventory", inventory.toSet())
                .putInt("points", currentPoints)
                .apply()
        } catch (e: Exception) {
            Log.e("ProfileFragment", "Failed to save local cache: ${e.message}")
        }
    }

    private fun restoreFromLocalCache() {
        try {
            val prefs = requireContext().getSharedPreferences("ecogo_mascot", android.content.Context.MODE_PRIVATE)
            currentOutfit["head"] = prefs.getString("outfit_head", "none") ?: "none"
            currentOutfit["face"] = prefs.getString("outfit_face", "none") ?: "none"
            currentOutfit["body"] = prefs.getString("outfit_body", "none") ?: "none"
            currentOutfit["badge"] = prefs.getString("outfit_badge", "none") ?: "none"

            val cachedInventory = prefs.getStringSet("inventory", emptySet()) ?: emptySet()
            if (cachedInventory.isNotEmpty()) {
                inventory.clear()
                inventory.addAll(cachedInventory)
            }

            currentPoints = prefs.getInt("points", 0)

            updateMascotOutfit()
            binding.textPoints.text = currentPoints.toString()
        } catch (e: Exception) {
            Log.e("ProfileFragment", "Failed to restore local cache: ${e.message}")
        }
    }

    private fun setupUI() {
        binding.textPoints.text = currentPoints.toString()
        binding.textName.text = "Alex Tan"
        binding.textFaculty.text = "Computer Science • Year 2"

        // 初始化 MascotLionView
        updateMascotOutfit()
    }

    private fun setupFacultyOutfitsRecyclerView() {
        binding.recyclerFacultyOutfits.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = FacultyOutfitAdapter(MockData.FACULTY_DATA) { faculty ->
                equipFacultyOutfit(faculty)
            }
        }
    }

    private fun equipFacultyOutfit(faculty: FacultyData) {
        currentOutfit["head"] = faculty.outfit.head
        currentOutfit["face"] = faculty.outfit.face
        currentOutfit["body"] = faculty.outfit.body
        currentOutfit["badge"] = faculty.outfit.badge
        updateMascotOutfit()
        refreshShopAdapter()
        persistMascotState()
        Log.d("ProfileFragment", "Equipped faculty outfit: ${faculty.name}")
    }

    private fun setupShopRecyclerView() {
        Log.d("ProfileFragment", "Setting up shop RecyclerView with ${MockData.SHOP_ITEMS.size} items")

        val adapter = ShopItemAdapter(getShopItemsWithState()) { item ->
            handleItemClick(item)
        }

        binding.recyclerShop.apply {
            layoutManager = GridLayoutManager(context, 2)
            this.adapter = adapter
        }
    }

    /** 学院服装中用到的所有配件 ID（用于在全部服装中优先展示） */
    private fun getFacultyOutfitItemIds(): Set<String> {
        val ids = mutableSetOf<String>()
        MockData.FACULTY_DATA.forEach { faculty ->
            if (faculty.outfit.head != "none") ids.add(faculty.outfit.head)
            if (faculty.outfit.face != "none") ids.add(faculty.outfit.face)
            if (faculty.outfit.body != "none") ids.add(faculty.outfit.body)
            if (faculty.outfit.badge != "none") ids.add(faculty.outfit.badge)
        }
        return ids
    }

    /** 全部服装列表：学院服装配件优先展示，其余商品随后 */
    private fun getShopItemsWithState(): List<ShopItem> {
        val facultyIds = getFacultyOutfitItemIds()
        val allItems = MockData.SHOP_ITEMS.map { item ->
            item.copy(
                owned = inventory.contains(item.id),
                equipped = currentOutfit[item.type] == item.id
            )
        }
        // 学院服装配件排在前面（按 head → face → body 排序），其余保持原顺序
        val typeOrder = listOf("head", "face", "body")
        val facultyItems = allItems
            .filter { it.id in facultyIds }
            .sortedBy { typeOrder.indexOf(it.type) }
        val otherItems = allItems.filter { it.id !in facultyIds }
        return facultyItems + otherItems
    }

    private fun handleItemClick(item: ShopItem) {
        Log.d("ProfileFragment", "Item clicked: ${item.id}, owned=${item.owned}, equipped=${item.equipped}")

        val isOwned = inventory.contains(item.id)
        val isEquipped = currentOutfit[item.type] == item.id

        when {
            // 已装备 → 卸下
            isEquipped -> {
                currentOutfit[item.type] = "none"
                refreshShopAdapter()
                updateMascotOutfit()
                persistMascotState()
                Log.d("ProfileFragment", "Unequipped ${item.name}")
            }
            // 已拥有 → 装备
            isOwned -> {
                currentOutfit[item.type] = item.id
                refreshShopAdapter()
                updateMascotOutfit()
                persistMascotState()
                Log.d("ProfileFragment", "Equipped ${item.name}")
            }
            // 未拥有 → 购买并装备
            else -> {
                if (currentPoints >= item.cost) {
                    currentPoints -= item.cost
                    binding.textPoints.text = currentPoints.toString()
                    inventory.add(item.id)
                    currentOutfit[item.type] = item.id
                    refreshShopAdapter()
                    updateMascotOutfit()
                    persistMascotState()
                    showSuccessDialog("Bought & Equipped ${item.name}!", "-${item.cost} pts")

                    // 动画反馈
                    val popIn = AnimationUtils.loadAnimation(requireContext(), R.anim.pop_in)
                    binding.cardMascot.startAnimation(popIn)

                    Log.d("ProfileFragment", "Purchased ${item.name} for ${item.cost} pts")
                } else {
                    android.widget.Toast.makeText(
                        requireContext(),
                        "Not enough points!",
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                    Log.d("ProfileFragment", "Insufficient points for ${item.name}")
                }
            }
        }
    }

    private fun updateMascotOutfit() {
        // 更新小狮子外观
        binding.mascotLion.outfit = Outfit(
            head = currentOutfit["head"] ?: "none",
            face = currentOutfit["face"] ?: "none",
            body = currentOutfit["body"] ?: "none",
            badge = currentOutfit["badge"] ?: "none"
        )
    }

    private fun refreshShopAdapter() {
        (binding.recyclerShop.adapter as? ShopItemAdapter)?.updateItems(getShopItemsWithState())
    }

    private fun showSuccessDialog(message: String, points: String? = null) {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_success)
        dialog.window?.setBackgroundDrawableResource(android.R.drawable.screen_background_light_transparent)

        val textMessage = dialog.findViewById<TextView>(R.id.text_message)
        val textPoints = dialog.findViewById<TextView>(R.id.text_points)
        val buttonOk = dialog.findViewById<com.google.android.material.button.MaterialButton>(R.id.button_ok)

        textMessage.text = message
        if (points != null) {
            textPoints.text = points
            textPoints.visibility = View.VISIBLE
        }

        buttonOk.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()

        // 对话框弹入动画
        dialog.window?.attributes?.windowAnimations = R.style.DialogAnimation
    }

    private fun setupBadgeRecyclerView() {
        binding.recyclerAchievements.apply {
            layoutManager = GridLayoutManager(context, 3)
            adapter = AchievementAdapter(MockData.ACHIEVEMENTS) { achievementId: String ->
                handleBadgeClick(achievementId)
            }
        }
    }

    private fun handleBadgeClick(badgeId: String) {
        // 只有解锁的徽章可以装备
        val achievement = MockData.ACHIEVEMENTS.find { it.id == badgeId }
        if (achievement != null) {
            if (!achievement.unlocked) {
                // 模拟解锁成就（实际应用中应该检查是否满足条件）
                // 这里仅用于演示弹窗效果
                Log.d("ProfileFragment", "Achievement locked: $badgeId")
                return
            }

            val isEquipped = currentOutfit["badge"] == badgeId

            if (isEquipped) {
                // 卸下徽章
                currentOutfit["badge"] = "none"
            } else {
                // 装备徽章
                currentOutfit["badge"] = badgeId
            }

            updateMascotOutfit()
            persistMascotState()
            Log.d("ProfileFragment", "Badge toggled: $badgeId")
        }
    }

    /**
     * 显示成就解锁弹窗（示例方法）
     * 实际应用中应该在满足成就条件时调用
     */
    private fun showAchievementUnlock(achievement: Achievement, pointsEarned: Int = 50) {
        com.ecogo.ui.dialogs.AchievementUnlockDialog.show(
            requireContext(),
            achievement,
            pointsEarned
        ) {
            // 弹窗关闭后的回调
            Log.d("ProfileFragment", "Achievement dialog dismissed")
        }
    }

    private fun setupHistoryRecyclerView() {
        binding.recyclerHistory.apply {
            layoutManager = GridLayoutManager(context, 1)
            adapter = HistoryAdapter(MockData.HISTORY)
        }
    }

    private fun loadHistory() {
        viewLifecycleOwner.lifecycleScope.launch {
            val history = repository.getHistory().getOrElse { MockData.HISTORY }
            binding.recyclerHistory.adapter = HistoryAdapter(history)
        }
    }

    private fun setupTabs() {
        fun setActiveTab(tab: String) {
            val isCloset = tab == "closet"
            binding.textFacultyOutfits.visibility = if (isCloset) View.VISIBLE else View.GONE
            binding.recyclerFacultyOutfits.visibility = if (isCloset) View.VISIBLE else View.GONE
            binding.textAllOutfits.visibility = if (isCloset) View.VISIBLE else View.GONE
            binding.recyclerShop.visibility = if (isCloset) View.VISIBLE else View.GONE
            binding.recyclerAchievements.visibility = if (tab == "badges") View.VISIBLE else View.GONE
            binding.recyclerHistory.visibility = if (tab == "history") View.VISIBLE else View.GONE

            // 强制可见的 RecyclerView 重新布局，避免在 ScrollView 内测量为 0
            when (tab) {
                "closet" -> {
                    binding.recyclerFacultyOutfits.post { binding.recyclerFacultyOutfits.requestLayout() }
                    binding.recyclerShop.post { binding.recyclerShop.requestLayout() }
                }
                "badges" -> binding.recyclerAchievements.post { binding.recyclerAchievements.requestLayout() }
                "history" -> binding.recyclerHistory.post { binding.recyclerHistory.requestLayout() }
            }

            val primary = ContextCompat.getColor(requireContext(), R.color.primary)
            val secondary = ContextCompat.getColor(requireContext(), R.color.text_secondary)
            val transparent = ContextCompat.getColor(requireContext(), android.R.color.transparent)

            binding.tabCloset.setTextColor(if (tab == "closet") primary else secondary)
            binding.tabBadges.setTextColor(if (tab == "badges") primary else secondary)
            binding.tabHistory.setTextColor(if (tab == "history") primary else secondary)

            binding.tabCloset.setTypeface(null, if (tab == "closet") android.graphics.Typeface.BOLD else android.graphics.Typeface.NORMAL)
            binding.tabBadges.setTypeface(null, if (tab == "badges") android.graphics.Typeface.BOLD else android.graphics.Typeface.NORMAL)
            binding.tabHistory.setTypeface(null, if (tab == "history") android.graphics.Typeface.BOLD else android.graphics.Typeface.NORMAL)

            binding.tabClosetIndicator.setBackgroundColor(if (tab == "closet") primary else transparent)
            binding.tabBadgesIndicator.setBackgroundColor(if (tab == "badges") primary else transparent)
            binding.tabHistoryIndicator.setBackgroundColor(if (tab == "history") primary else transparent)

            Log.d("ProfileFragment", "Tab switched to: $tab")
        }

        binding.tabCloset.setOnClickListener { setActiveTab("closet") }
        binding.tabBadges.setOnClickListener { setActiveTab("badges") }
        binding.tabHistory.setOnClickListener { setActiveTab("history") }
        setActiveTab("closet")
    }

    private fun setupAnimations() {
        // MascotLionView 自带呼吸和眨眼动画
        // 点击触发跳跃动画在 View 内部处理

        // 卡片弹入动画
        val popIn = AnimationUtils.loadAnimation(requireContext(), R.anim.pop_in)
        binding.cardMascot.startAnimation(popIn)
        binding.cardPoints.startAnimation(popIn)
    }

    private fun setupActions() {
        binding.buttonSettings.setOnClickListener {
            findNavController().navigate(R.id.action_profile_to_settings)
        }
        binding.buttonRedeem.setOnClickListener {
            findNavController().navigate(R.id.action_profile_to_voucher)
        }

        // 添加商店入口：点击Closet tab可以进入完整商店
        binding.tabCloset.setOnLongClickListener {
            findNavController().navigate(R.id.action_profile_to_shop)
            true
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}