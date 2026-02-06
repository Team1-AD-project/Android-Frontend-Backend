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
import com.ecogo.R
import com.ecogo.data.Achievement
import com.ecogo.data.FacultyData
import com.ecogo.data.MockData
import com.ecogo.data.Outfit
import com.ecogo.data.ShopItem
import com.ecogo.databinding.FragmentProfileBinding
import com.ecogo.repository.EcoGoRepository
import com.ecogo.ui.adapters.AchievementAdapter
import com.ecogo.ui.adapters.FacultyOutfitGridAdapter
import com.ecogo.ui.adapters.ShopItemAdapter
import com.ecogo.ui.adapters.ShopListItem
import kotlinx.coroutines.launch

class ProfileFragment : Fragment() {
    
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private val repository = EcoGoRepository()
    
    // 状态管理
    private var currentPoints = 1250
    private val inventory = mutableListOf("hat_grad", "shirt_nus")  // 已拥有的物品
    private val currentOutfit = mutableMapOf(
        "head" to "none",
        "face" to "none",
        "body" to "shirt_nus",  // 初始装备
        "badge" to "none"  // 新增徽章槽位
    )
    
    // 用户所属学院 ID（注册时确定，对应 FACULTY_DATA 中的 id）
    private val userFacultyId = "soc"  // 模拟：School of Computing
    // 已拥有（解锁）的学院服饰 ID 集合，注册时自己学院免费赠送
    private val ownedFaculties = mutableSetOf("soc")
    
    // Closet Dialog 状态
    private var closetDialog: Dialog? = null
    private var closetAdapter: ShopItemAdapter? = null
    private var closetFacultyAdapter: FacultyOutfitGridAdapter? = null
    private var closetMascot: com.ecogo.ui.views.MascotLionView? = null
    private var closetOutfitDetail: TextView? = null
    private var closetCurrentTab = "all"  // "all" 或 "faculty"
    
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
        grantUserFacultyOutfitIfNeeded()
        setupClosetEntry()
        setupBadgeEntry()
        setupBadgeRecyclerView()
        setupTabs()
        setupAnimations()
        setupActions()
        loadUserProfile()
        
        Log.d("ProfileFragment", "Profile screen initialized with ${inventory.size} owned items")
    }
    
    private fun loadUserProfile() {
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
                
                Log.d("ProfileFragment", "Loaded user profile: ${userInfo.nickname}, points: $currentPoints")
            }
        }
    }
    
    private fun setupUI() {
        binding.textPoints.text = currentPoints.toString()
        binding.textName.text = "Alex Tan"
        binding.textFaculty.text = "Computer Science • Year 2"

        // 初始化 MascotLionView
        updateMascotOutfit()
        
        // 更新徽章入口卡片
        updateBadgeEntry()
    }

    /** 注册赠送：默认解锁并拥有自己学院的套装配件（head/face/body） */
    private fun grantUserFacultyOutfitIfNeeded() {
        val faculty = MockData.FACULTY_DATA.find { it.id == userFacultyId } ?: return
        ownedFaculties.add(faculty.id)
        if (faculty.outfit.head != "none") inventory.add(faculty.outfit.head)
        if (faculty.outfit.face != "none") inventory.add(faculty.outfit.face)
        if (faculty.outfit.body != "none") inventory.add(faculty.outfit.body)
    }
    
    // 当前装备的学院 ID（用于高亮）
    private var equippedFacultyId: String? = null

    /** 设置 Closet 入口卡片 */
    private fun setupClosetEntry() {
        // 入口卡片显示当前装扮预览
        updateClosetPreview()
        binding.cardCloset.setOnClickListener {
            showClosetDialog()
        }
    }

    /** 更新 Closet 入口卡片上的预览 */
    private fun updateClosetPreview() {
        binding.mascotClosetPreview.outfit = Outfit(
            head = currentOutfit["head"] ?: "none",
            face = currentOutfit["face"] ?: "none",
            body = currentOutfit["body"] ?: "none",
            badge = currentOutfit["badge"] ?: "none"
        )
        val total = MockData.SHOP_ITEMS.size
        binding.textClosetDesc.text = "Browse & equip $total outfits"
    }

    // ═══════════════════════════════════════════
    //  Closet 全屏 Dialog（小狮子 + Tab 切换）
    // ═══════════════════════════════════════════

    private fun showClosetDialog() {
        val dialog = Dialog(requireContext(), android.R.style.Theme_Black_NoTitleBar_Fullscreen)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_closet)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )

        // 获取视图引用
        val mascot = dialog.findViewById<com.ecogo.ui.views.MascotLionView>(R.id.mascot_closet)
        val outfitDetail = dialog.findViewById<TextView>(R.id.text_outfit_detail)
        val btnClose = dialog.findViewById<android.widget.ImageView>(R.id.btn_close)
        val tabAll = dialog.findViewById<com.google.android.material.button.MaterialButton>(R.id.tab_all_clothes)
        val tabFaculty = dialog.findViewById<com.google.android.material.button.MaterialButton>(R.id.tab_faculty_clothes)
        val recycler = dialog.findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.recycler_closet)

        // 保持引用以便实时更新
        closetDialog = dialog
        closetMascot = mascot
        closetOutfitDetail = outfitDetail

        // 初始化小狮子
        updateClosetMascot()

        // 初始化 RecyclerView + GridLayoutManager
        val gridLayoutManager = GridLayoutManager(requireContext(), 2)
        recycler.layoutManager = gridLayoutManager

        // 创建 All Clothes adapter
        val shopAdapter = ShopItemAdapter(getShopItemsGrouped()) { item ->
            handleItemClick(item)
            // 刷新 Dialog 内列表状态 + 小狮子
            closetAdapter?.updateItems(getShopItemsGrouped())
            updateClosetMascot()
        }
        closetAdapter = shopAdapter

        // 分组标题跨两列
        gridLayoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                val adapter = recycler.adapter
                return when {
                    adapter is ShopItemAdapter && adapter.isHeader(position) -> 2
                    else -> 1
                }
            }
        }

        // 创建 Faculty Clothes adapter（传入价格计算、拥有状态、用户学院）
        val facultyAdapter = FacultyOutfitGridAdapter(
            faculties = MockData.FACULTY_DATA,
            equippedFacultyId = equippedFacultyId,
            ownedFacultyIds = ownedFaculties,
            userFacultyId = userFacultyId,
            costCalculator = { getFacultyOutfitCost(it) }
        ) { faculty ->
            handleFacultyClick(faculty)
            closetFacultyAdapter?.updateEquipped(equippedFacultyId)
            closetFacultyAdapter?.updateOwned(ownedFaculties)
            closetAdapter?.updateItems(getShopItemsGrouped())
            updateClosetMascot()
        }
        closetFacultyAdapter = facultyAdapter

        // 默认显示 All Clothes
        closetCurrentTab = "all"
        recycler.adapter = shopAdapter

        // Tab 切换逻辑
        tabAll.setOnClickListener {
            if (closetCurrentTab != "all") {
                closetCurrentTab = "all"
                updateClosetTabStyle(tabAll, tabFaculty)
                recycler.adapter = closetAdapter
                val slideIn = AnimationUtils.loadAnimation(requireContext(), R.anim.slide_in_left)
                recycler.startAnimation(slideIn)
            }
        }

        tabFaculty.setOnClickListener {
            if (closetCurrentTab != "faculty") {
                closetCurrentTab = "faculty"
                updateClosetTabStyle(tabFaculty, tabAll)
                recycler.adapter = closetFacultyAdapter
                val slideIn = AnimationUtils.loadAnimation(requireContext(), R.anim.slide_in_right)
                recycler.startAnimation(slideIn)
            }
        }

        // 初始 Tab 高亮
        updateClosetTabStyle(tabAll, tabFaculty)

        btnClose.setOnClickListener { dialog.dismiss() }

        dialog.setOnDismissListener {
            closetDialog = null
            closetAdapter = null
            closetFacultyAdapter = null
            closetMascot = null
            closetOutfitDetail = null
            // 同步回主页面
            updateMascotOutfit()
            updateClosetPreview()
        }

        dialog.show()
        Log.d("ProfileFragment", "Opened Closet dialog")
    }

    /** 更新 Dialog 内小狮子外观 */
    private fun updateClosetMascot() {
        closetMascot?.outfit = Outfit(
            head = currentOutfit["head"] ?: "none",
            face = currentOutfit["face"] ?: "none",
            body = currentOutfit["body"] ?: "none",
            badge = currentOutfit["badge"] ?: "none"
        )
        // 更新描述文字
        val parts = mutableListOf<String>()
        val head = currentOutfit["head"] ?: "none"
        val face = currentOutfit["face"] ?: "none"
        val body = currentOutfit["body"] ?: "none"
        if (head != "none") parts.add(getItemShortName(head))
        if (face != "none") parts.add(getItemShortName(face))
        if (body != "none") parts.add(getItemShortName(body))
        closetOutfitDetail?.text = if (parts.isEmpty()) "No outfit equipped" else parts.joinToString(" + ")
    }

    /** 更新 Tab 按钮样式：选中 vs 未选中 */
    private fun updateClosetTabStyle(
        active: com.google.android.material.button.MaterialButton,
        inactive: com.google.android.material.button.MaterialButton
    ) {
        val primaryColor = ContextCompat.getColor(requireContext(), R.color.primary)
        val surfaceColor = ContextCompat.getColor(requireContext(), R.color.surface)
        val borderColor = ContextCompat.getColor(requireContext(), R.color.border)

        active.backgroundTintList = android.content.res.ColorStateList.valueOf(primaryColor)
        active.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_white))
        active.strokeWidth = 0

        inactive.backgroundTintList = android.content.res.ColorStateList.valueOf(surfaceColor)
        inactive.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary))
        inactive.strokeWidth = 2
        inactive.strokeColor = android.content.res.ColorStateList.valueOf(borderColor)
    }

    /** 计算一套学院服饰的价格 = 各配件在 SHOP_ITEMS 中的价格之和 */
    private fun getFacultyOutfitCost(faculty: FacultyData): Int {
        val shopMap = MockData.SHOP_ITEMS.associateBy { it.id }
        var cost = 0
        if (faculty.outfit.head != "none") cost += shopMap[faculty.outfit.head]?.cost ?: 0
        if (faculty.outfit.face != "none") cost += shopMap[faculty.outfit.face]?.cost ?: 0
        if (faculty.outfit.body != "none") cost += shopMap[faculty.outfit.body]?.cost ?: 0
        return cost
    }

    /** 处理学院服饰点击：已拥有→装备/卸下，未拥有→购买 */
    private fun handleFacultyClick(faculty: FacultyData) {
        if (ownedFaculties.contains(faculty.id)) {
            equipFacultyOutfit(faculty)
            return
        }

        // 未解锁：点击先查看价格 → 确认购买
        val componentIds = listOf(faculty.outfit.head, faculty.outfit.face, faculty.outfit.body)
            .filter { it != "none" }
        val ownedComponents = componentIds.filter { inventory.contains(it) }
        val missingComponents = componentIds.filterNot { inventory.contains(it) }

        // 如果已经把配件都买齐了，就视为解锁
        if (missingComponents.isEmpty()) {
            ownedFaculties.add(faculty.id)
            closetFacultyAdapter?.updateOwned(ownedFaculties)
            equipFacultyOutfit(faculty)
            return
        }

        val missingCost = missingComponents.sumOf { id ->
            MockData.SHOP_ITEMS.find { it.id == id }?.cost ?: 0
        }
        val totalCost = getFacultyOutfitCost(faculty)

        val ownedText = if (ownedComponents.isEmpty()) {
            "You don't own any items from this outfit set yet."
        } else {
            val ownedNames = ownedComponents.joinToString(", ") { getItemShortName(it) }
            "You already own ${ownedComponents.size} item(s): $ownedNames."
        }
        val missingNames = missingComponents.joinToString(", ") { getItemShortName(it) }

        val message = buildString {
            append("${faculty.name} Outfit Set\n\n")
            append("$ownedText\n")
            append("Missing ${missingComponents.size} item(s): $missingNames\n\n")
            append("Full set price: $totalCost pts\n")
            append("Cost for missing items: $missingCost pts\n\n")
            append("Purchase and complete the set?")
        }

        showConfirmPurchaseDialog(
            icon = "🎓",
            title = "Purchase Faculty Outfit",
            message = message,
            onConfirm = {
                if (currentPoints < missingCost) {
                    android.widget.Toast.makeText(
                        requireContext(),
                        "Not enough points! Need $missingCost pts",
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                    return@showConfirmPurchaseDialog
                }

                currentPoints -= missingCost
                binding.textPoints.text = currentPoints.toString()

                // 只买缺失的配件（已有的配件不重复扣费）
                missingComponents.forEach { id ->
                    if (!inventory.contains(id)) inventory.add(id)
                }

                ownedFaculties.add(faculty.id)
                closetFacultyAdapter?.updateOwned(ownedFaculties)

                equipFacultyOutfit(faculty)

                // 立即刷新 Dialog 内 UI
                closetAdapter?.updateItems(getShopItemsGrouped())
                updateClosetMascot()

                showSuccessDialog("Unlocked ${faculty.name} outfit!", "-$missingCost pts")
                Log.d("ProfileFragment", "Purchased faculty outfit missing items for ${faculty.name}: $missingCost pts")
            }
        )
    }

    private fun equipFacultyOutfit(faculty: FacultyData) {
        currentOutfit["head"] = faculty.outfit.head
        currentOutfit["face"] = faculty.outfit.face
        currentOutfit["body"] = faculty.outfit.body
        currentOutfit["badge"] = faculty.outfit.badge
        equippedFacultyId = faculty.id
        updateMascotOutfit()
        refreshShopAdapter()
        Log.d("ProfileFragment", "Equipped faculty outfit: ${faculty.name}")
    }

    private fun getItemShortName(id: String): String = when (id) {
        "face_glasses_square" -> "Square Glasses"
        "hat_grad" -> "Grad Cap"
        "hat_cap" -> "Cap"
        "hat_helmet" -> "Helmet"
        "hat_beret" -> "Beret"
        "hat_crown" -> "Crown"
        "hat_party" -> "Party Hat"
        "hat_beanie" -> "Beanie"
        "hat_cowboy" -> "Cowboy"
        "hat_chef" -> "Chef Hat"
        "hat_wizard" -> "Wizard Hat"
        "glasses_sun" -> "Sunglasses"
        "face_goggles" -> "Goggles"
        "glasses_nerd" -> "Nerd Glasses"
        "glasses_3d" -> "3D Glasses"
        "face_mask" -> "Hero Mask"
        "face_monocle" -> "Monocle"
        "face_scarf" -> "Scarf"
        "face_vr" -> "VR Headset"
        "body_white_shirt" -> "White Shirt"
        "shirt_nus" -> "NUS Tee"
        "shirt_hoodie" -> "Hoodie"
        "body_plaid" -> "Plaid"
        "body_suit" -> "Suit"
        "body_coat" -> "Lab Coat"
        "body_sports" -> "Jersey"
        "body_kimono" -> "Kimono"
        "body_tux" -> "Tuxedo"
        "body_superhero" -> "Cape"
        "body_doctor" -> "Doctor"
        "body_pilot" -> "Pilot"
        "body_ninja" -> "Ninja"
        "body_scrubs" -> "Scrubs"
        "body_polo" -> "Polo"
        else -> id
    }
    
    /** 全部服饰列表：按 Head / Face / Body 分组，带分组标题 */
    private fun getShopItemsGrouped(): List<ShopListItem> {
        val allItems = MockData.SHOP_ITEMS.map { item ->
            item.copy(
                owned = inventory.contains(item.id),
                equipped = currentOutfit[item.type] == item.id
            )
        }

        val result = mutableListOf<ShopListItem>()

        val headItems = allItems.filter { it.type == "head" }
        val faceItems = allItems.filter { it.type == "face" }
        val bodyItems = allItems.filter { it.type == "body" }

        if (headItems.isNotEmpty()) {
            result.add(ShopListItem.Header("Head  (${headItems.size})"))
            result.addAll(headItems.map { ShopListItem.Item(it) })
        }
        if (faceItems.isNotEmpty()) {
            result.add(ShopListItem.Header("Face  (${faceItems.size})"))
            result.addAll(faceItems.map { ShopListItem.Item(it) })
        }
        if (bodyItems.isNotEmpty()) {
            result.add(ShopListItem.Header("Body  (${bodyItems.size})"))
            result.addAll(bodyItems.map { ShopListItem.Item(it) })
        }

        return result
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
                Log.d("ProfileFragment", "Unequipped ${item.name}")
            }
            // 已拥有 → 装备
            isOwned -> {
                currentOutfit[item.type] = item.id
                refreshShopAdapter()
                updateMascotOutfit()
                Log.d("ProfileFragment", "Equipped ${item.name}")
            }
            // 未拥有 → 购买并装备
            else -> {
                // 点击先查看价格 → 确认是否购买
                val message = "Price: ${item.cost} pts\n\nPurchase and equip \"${item.name}\" immediately?"
                showConfirmPurchaseDialog(
                    icon = getItemEmoji(item.id),
                    title = "Purchase Item",
                    message = message,
                    onConfirm = { purchaseAndEquipItem(item) }
                )
            }
        }
    }

    private fun purchaseAndEquipItem(item: ShopItem) {
        if (currentPoints < item.cost) {
            android.widget.Toast.makeText(
                requireContext(),
                "Not enough points! Need ${item.cost} pts",
                android.widget.Toast.LENGTH_SHORT
            ).show()
            Log.d("ProfileFragment", "Insufficient points for ${item.name}")
            return
        }

        currentPoints -= item.cost
        binding.textPoints.text = currentPoints.toString()
        if (!inventory.contains(item.id)) inventory.add(item.id)
        currentOutfit[item.type] = item.id

        // 刷新 UI（主页面 + Dialog）
        refreshShopAdapter()
        updateMascotOutfit()
        updateClosetPreview()
        closetAdapter?.updateItems(getShopItemsGrouped())
        updateClosetMascot()

        showSuccessDialog("Bought & Equipped ${item.name}!", "-${item.cost} pts")

        // 动画反馈
        val popIn = AnimationUtils.loadAnimation(requireContext(), R.anim.pop_in)
        binding.cardMascot.startAnimation(popIn)

        Log.d("ProfileFragment", "Purchased ${item.name} for ${item.cost} pts")
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
        closetAdapter?.updateItems(getShopItemsGrouped())
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

    /** 更新徽章入口卡片（预览 + 统计） */
    private fun updateBadgeEntry() {
        val unlockedCount = MockData.ACHIEVEMENTS.count { it.unlocked }
        val totalCount = MockData.ACHIEVEMENTS.size
        binding.textBadgeCount.text = "$unlockedCount / $totalCount unlocked"
        
        // 如果已装备徽章，显示该徽章 emoji，否则显示默认 🏆
        val equippedBadgeId = currentOutfit["badge"] ?: "none"
        val previewEmoji = if (equippedBadgeId != "none") {
            getBadgeEmoji(equippedBadgeId)
        } else {
            "🏆"
        }
        binding.textBadgePreview.text = previewEmoji
    }

    /** 设置徽章入口卡片点击 */
    private fun setupBadgeEntry() {
        binding.cardBadges.setOnClickListener {
            showBadgesDialog()
        }
    }
    
    /** 显示 Badges 全屏对话框 */
    private fun showBadgesDialog() {
        val dialog = Dialog(requireContext(), android.R.style.Theme_Black_NoTitleBar_Fullscreen)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_badges)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )

        val btnClose = dialog.findViewById<android.widget.ImageView>(R.id.btn_close)
        val mascot = dialog.findViewById<com.ecogo.ui.views.MascotLionView>(R.id.mascot_badges)
        val badgeLabel = dialog.findViewById<TextView>(R.id.text_badge_label)
        val badgeDetail = dialog.findViewById<TextView>(R.id.text_badge_detail)
        val recycler = dialog.findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.recycler_badges)

        btnClose.setOnClickListener { dialog.dismiss() }

        // 更新小狮子预览（显示当前装备）
        mascot.outfit = Outfit(
            head = currentOutfit["head"] ?: "none",
            face = currentOutfit["face"] ?: "none",
            body = currentOutfit["body"] ?: "none",
            badge = currentOutfit["badge"] ?: "none"
        )
        
        val equippedBadge = currentOutfit["badge"] ?: "none"
        if (equippedBadge != "none") {
            val badge = MockData.ACHIEVEMENTS.find { it.id == equippedBadge }
            badgeLabel.text = badge?.name ?: "Current Badge"
        } else {
            badgeLabel.text = "No Badge Equipped"
        }

        // 徽章列表：已解锁排前面
        val sortedAchievements = MockData.ACHIEVEMENTS.sortedByDescending { it.unlocked }

        recycler.apply {
            layoutManager = GridLayoutManager(context, 3)
            adapter = AchievementAdapter(
                sortedAchievements,
                equippedBadgeId = equippedBadge
            ) { achievementId: String ->
                handleBadgeClick(achievementId, dialog, mascot, badgeLabel)
            }
        }

        dialog.show()
    }
    
    private fun setupBadgeRecyclerView() {
        // 保留此方法为空或移除，现在改用入口卡片 + 全屏对话框
    }
    
    private fun handleBadgeClick(
        badgeId: String,
        parentDialog: Dialog? = null,
        mascot: com.ecogo.ui.views.MascotLionView? = null,
        badgeLabel: TextView? = null
    ) {
        val achievement = MockData.ACHIEVEMENTS.find { it.id == badgeId } ?: return
        showBadgeDetailDialog(achievement, parentDialog, mascot, badgeLabel)
    }

    /** 显示徽章详情弹窗：图标、描述、解锁方式、佩戴按钮 */
    private fun showBadgeDetailDialog(
        achievement: Achievement,
        parentDialog: Dialog? = null,
        mascot: com.ecogo.ui.views.MascotLionView? = null,
        badgeLabel: TextView? = null
    ) {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_badge_detail)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        val btnClose = dialog.findViewById<android.widget.ImageView>(R.id.btn_close)
        val iconView = dialog.findViewById<TextView>(R.id.text_badge_icon)
        val nameView = dialog.findViewById<TextView>(R.id.text_badge_name)
        val statusView = dialog.findViewById<TextView>(R.id.text_badge_status)
        val descView = dialog.findViewById<TextView>(R.id.text_badge_desc)
        val howToView = dialog.findViewById<TextView>(R.id.text_how_to_unlock)
        val btnEquip = dialog.findViewById<com.google.android.material.button.MaterialButton>(R.id.button_equip)

        // 设置内容
        iconView.text = getBadgeEmoji(achievement.id)
        nameView.text = achievement.name
        descView.text = achievement.description
        howToView.text = achievement.howToUnlock.ifEmpty { "Complete the required task to unlock this badge." }

        val isEquipped = currentOutfit["badge"] == achievement.id

        // 状态标签
        when {
            isEquipped -> {
                statusView.text = "✅ Equipped"
                statusView.setTextColor(ContextCompat.getColor(requireContext(), R.color.primary))
            }
            achievement.unlocked -> {
                statusView.text = "🔓 Unlocked"
                statusView.setTextColor(ContextCompat.getColor(requireContext(), R.color.primary))
            }
            else -> {
                statusView.text = "🔒 Locked"
                statusView.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary))
            }
        }

        // 按钮逻辑
        when {
            !achievement.unlocked -> {
                btnEquip.isEnabled = false
                btnEquip.text = "Locked"
                btnEquip.alpha = 0.5f
            }
            isEquipped -> {
                btnEquip.isEnabled = true
                btnEquip.text = "Unequip Badge"
                btnEquip.setOnClickListener {
                    currentOutfit["badge"] = "none"
                    updateMascotOutfit()
                    updateBadgeEntry()
                    
                    // 更新 Badges dialog 中的小狮子和标签
                    mascot?.outfit = Outfit(
                        head = currentOutfit["head"] ?: "none",
                        face = currentOutfit["face"] ?: "none",
                        body = currentOutfit["body"] ?: "none",
                        badge = "none"
                    )
                    badgeLabel?.text = "No Badge Equipped"
                    
                    // 刷新徽章列表
                    parentDialog?.findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.recycler_badges)?.adapter?.notifyDataSetChanged()
                    dialog.dismiss()
                    Log.d("ProfileFragment", "Unequipped badge: ${achievement.id}")
                }
            }
            else -> {
                btnEquip.isEnabled = true
                btnEquip.text = "Equip Badge"
                btnEquip.setOnClickListener {
                    currentOutfit["badge"] = achievement.id
                    updateMascotOutfit()
                    updateBadgeEntry()
                    
                    // 更新 Badges dialog 中的小狮子和标签
                    mascot?.outfit = Outfit(
                        head = currentOutfit["head"] ?: "none",
                        face = currentOutfit["face"] ?: "none",
                        body = currentOutfit["body"] ?: "none",
                        badge = achievement.id
                    )
                    badgeLabel?.text = achievement.name
                    
                    parentDialog?.findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.recycler_badges)?.adapter?.notifyDataSetChanged()
                    dialog.dismiss()
                    Log.d("ProfileFragment", "Equipped badge: ${achievement.id}")
                }
            }
        }

        btnClose.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    private fun getBadgeEmoji(id: String): String = when (id) {
        "a1" -> "🚌"   "a2" -> "✅"   "a3" -> "🎪"   "a4" -> "📝"
        "a5" -> "⚡"   "a6" -> "🔄"   "a7" -> "📅"   "a8" -> "💪"
        "a9" -> "💯"   "a10" -> "💰"  "a11" -> "💎"
        "a12" -> "🚴"  "a13" -> "🚶"  "a14" -> "🚍"  "a15" -> "♻️"
        "a16" -> "🦋"  "a17" -> "🤝"  "a18" -> "👥"
        "a19" -> "🎫"  "a20" -> "🏆"
        else -> "🏅"
    }

    /** 刷新徽章列表以反映佩戴状态 */
    private fun refreshBadgeList() {
        // 由于现在改为入口卡片 + 全屏对话框，此方法已弃用，但保留兼容
        updateBadgeEntry()
    }

    private fun setupTabs() {
        // 移除 tab 切换逻辑，两个卡片始终可见
        binding.cardCloset.visibility = View.VISIBLE
        binding.cardBadges.visibility = View.VISIBLE
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
    }
    
    // ═══════════════════════════════════════════
    //  自定义购买确认对话框
    // ═══════════════════════════════════════════
    
    private fun showConfirmPurchaseDialog(
        icon: String,
        title: String,
        message: String,
        onConfirm: () -> Unit
    ) {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_confirm_purchase)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        val iconView = dialog.findViewById<TextView>(R.id.text_icon)
        val titleView = dialog.findViewById<TextView>(R.id.text_title)
        val messageView = dialog.findViewById<TextView>(R.id.text_message)
        val btnCancel = dialog.findViewById<com.google.android.material.button.MaterialButton>(R.id.button_cancel)
        val btnConfirm = dialog.findViewById<com.google.android.material.button.MaterialButton>(R.id.button_confirm)

        iconView.text = icon
        titleView.text = title
        messageView.text = message

        btnCancel.setOnClickListener { dialog.dismiss() }
        btnConfirm.setOnClickListener {
            onConfirm()
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun getItemEmoji(id: String): String = when (id) {
        // Head (10)
        "hat_cap" -> "🧢"
        "hat_grad" -> "🎓"
        "hat_beanie" -> "🧶"
        "hat_headband" -> "💪"
        "hat_crown" -> "👑"
        "hat_cowboy" -> "🤠"
        "hat_headphones" -> "🎧"
        "hat_hardhat" -> "⛑️"
        "hat_chef" -> "👨‍🍳"
        "hat_wizard" -> "🧙"
        // Face (9 - ISS glasses added)
        "face_glasses_square" -> "👓"
        "face_glasses_round" -> "👓"
        "face_sunglasses" -> "😎"
        "face_mask" -> "😷"
        "face_monocle" -> "🧐"
        "face_goggles" -> "🥽"
        "face_vr" -> "🥽"
        "face_diving" -> "🤿"
        "face_scarf" -> "🧣"
        // Body (15 - ISS white shirt added)
        "body_white_shirt" -> "👔"
        "shirt_nus" -> "👕"
        "shirt_fass" -> "📚"
        "shirt_business" -> "💼"
        "shirt_law" -> "⚖️"
        "shirt_dent" -> "🦷"
        "shirt_arts" -> "🎨"
        "shirt_comp" -> "💻"
        "shirt_music" -> "🎵"
        "shirt_pub_health" -> "🏥"
        "body_doctor" -> "🩺"
        "body_hoodie" -> "🧥"
        "body_suit" -> "🤵"
        "body_scrubs" -> "👔"
        "body_polo" -> "👕"
        else -> "👕"
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
