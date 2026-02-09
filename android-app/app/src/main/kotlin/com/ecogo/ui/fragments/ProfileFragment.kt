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
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.ecogo.R
import com.ecogo.api.RetrofitClient
import com.ecogo.data.Achievement
import com.ecogo.data.FacultyData
import com.ecogo.data.MockData
import com.ecogo.data.Outfit
import com.ecogo.data.ShopItem
import com.ecogo.data.dto.BadgeDto
import com.ecogo.data.dto.UserBadgeDto
import com.ecogo.databinding.FragmentProfileBinding
import com.ecogo.repository.BadgeClothRepository
import com.ecogo.repository.EcoGoRepository
import com.ecogo.ui.adapters.AchievementAdapter
import com.ecogo.ui.adapters.FacultyOutfitGridAdapter
import com.ecogo.ui.adapters.ShopItemAdapter
import com.ecogo.ui.adapters.ShopListItem
import com.ecogo.utils.DataMapper
import com.ecogo.utils.DataMapper.toOutfit
import com.ecogo.utils.LoadingDialog
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private val repository = EcoGoRepository()

    // ⭐ 新增：Badge & Cloth API Repository
    private lateinit var badgeClothRepository: BadgeClothRepository
    private lateinit var loadingDialog: LoadingDialog
    private var currentUserId: String = ""

    // ⭐ 新增：缓存服务器数据
    private var shopItems = mutableListOf<BadgeDto>()
    private var userItems = mutableListOf<UserBadgeDto>()

    // 状态管理
    private var currentPoints = 1250
    private val inventory = mutableListOf("hat_grad", "shirt_nus")
    private val currentOutfit = mutableMapOf(
        "head" to "none",
        "face" to "none",
        "body" to "shirt_nus",
        "badge" to "none"
    )

    private val userFacultyId = "soc"
    private val ownedFaculties = mutableSetOf("soc")

    private var closetDialog: Dialog? = null
    private var closetAdapter: ShopItemAdapter? = null
    private var closetFacultyAdapter: FacultyOutfitGridAdapter? = null
    private var closetMascot: com.ecogo.ui.views.MascotLionView? = null
    private var closetOutfitDetail: TextView? = null
    private var closetCurrentTab = "all"
    private var equippedFacultyId: String? = null

    // ⭐ 新增：onCreate 初始化 Repository
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        badgeClothRepository = BadgeClothRepository(
            RetrofitClient.badgeApiService
        )
    }

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

        // ⭐ 新增：初始化 LoadingDialog
        loadingDialog = LoadingDialog(requireContext())

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

    // ⭐ 修改：loadUserProfile 保存 userId 并加载数据
    private fun loadUserProfile() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val result = repository.getMobileUserProfile()
                val profile = result.getOrNull()

                if (profile != null) {
                    val userInfo = profile.userInfo

                    // ⭐ 保存用户 ID（重要！）
                    currentUserId = userInfo.userid

                    currentPoints = userInfo.currentPoints
                    binding.textPoints.text = currentPoints.toString()
                    binding.textName.text = userInfo.nickname

                    userInfo.faculty?.let { faculty ->
                        binding.textFaculty.text = "$faculty • Year 2"
                    }

                    Log.d("ProfileFragment", "Loaded profile: ${userInfo.nickname}, ID: $currentUserId, pts: $currentPoints")

                    // ⭐ 加载 badges 和 cloths
                    loadBadgesAndCloths()
                    loadUserOutfit()
                }
            } catch (e: Exception) {
                Log.e("ProfileFragment", "Error loading profile", e)
                Toast.makeText(requireContext(), "Failed to load profile", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // ⭐ 新增：加载 badges 和 cloths 数据
    private fun loadBadgesAndCloths() {
        if (currentUserId.isEmpty()) {
            Log.w("ProfileFragment", "User ID not available")
            return
        }

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                loadingDialog.show("Loading items...")

                // 并行加载
                val shopDeferred = async { badgeClothRepository.getShopList() }
                val userDeferred = async { badgeClothRepository.getMyItems(currentUserId) }

                val shopResult = shopDeferred.await()
                val userResult = userDeferred.await()

                shopResult.onSuccess { items ->
                    shopItems.clear()
                    shopItems.addAll(items)
                    Log.d("ProfileFragment", "Loaded ${items.size} shop items")
                }.onFailure { error ->
                    Log.e("ProfileFragment", "Failed to load shop", error)
                    shopItems.clear()
                }

                userResult.onSuccess { items ->
                    userItems.clear()
                    userItems.addAll(items)
                    Log.d("ProfileFragment", "Loaded ${items.size} user items")
                }.onFailure { error ->
                    Log.e("ProfileFragment", "Failed to load user items", error)
                }

                inventory.clear()
                inventory.addAll(userItems.map { it.badgeId })

                updateBadgeEntry()
                loadingDialog.dismiss()

            } catch (e: Exception) {
                loadingDialog.dismiss()
                Log.e("ProfileFragment", "Error loading items", e)
                Toast.makeText(requireContext(), "Failed to load items", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // ⭐ 新增：加载用户当前装备
    private fun loadUserOutfit() {
        if (currentUserId.isEmpty()) return

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val result = badgeClothRepository.getUserOutfit(currentUserId)
                result.onSuccess { outfitDto ->
                    val outfit = outfitDto.toOutfit()

                    currentOutfit["head"] = outfit.head
                    currentOutfit["face"] = outfit.face
                    currentOutfit["body"] = outfit.body
                    currentOutfit["badge"] = outfit.badge

                    updateMascotOutfit()
                    updateClosetPreview()

                    Log.d("ProfileFragment", "Loaded outfit: $outfit")
                }.onFailure { error ->
                    Log.e("ProfileFragment", "Failed to load outfit", error)
                }
            } catch (e: Exception) {
                Log.e("ProfileFragment", "Error loading outfit", e)
            }
        }
    }

    private fun setupUI() {
        binding.textPoints.text = currentPoints.toString()
        binding.textName.text = "Alex Tan"
        binding.textFaculty.text = "Computer Science • Year 2"
        updateMascotOutfit()
        updateBadgeEntry()
    }

    private fun grantUserFacultyOutfitIfNeeded() {
        val faculty = MockData.FACULTY_DATA.find { it.id == userFacultyId } ?: return
        ownedFaculties.add(faculty.id)
        if (faculty.outfit.head != "none") inventory.add(faculty.outfit.head)
        if (faculty.outfit.face != "none") inventory.add(faculty.outfit.face)
        if (faculty.outfit.body != "none") inventory.add(faculty.outfit.body)
    }

    private fun setupClosetEntry() {
        updateClosetPreview()
        binding.cardCloset.setOnClickListener {
            showClosetDialog()
        }
    }

    private fun updateClosetPreview() {
        binding.mascotClosetPreview.outfit = Outfit(
            head = currentOutfit["head"] ?: "none",
            face = currentOutfit["face"] ?: "none",
            body = currentOutfit["body"] ?: "none",
            badge = currentOutfit["badge"] ?: "none"
        )

        // ⭐ 修改：动态显示数量
        val totalCloths = if (shopItems.isNotEmpty()) {
            shopItems.count { it.category == "cloth" }
        } else {
            MockData.SHOP_ITEMS.size
        }
        binding.textClosetDesc.text = "Browse & equip $totalCloths outfits"
    }

    private fun showClosetDialog() {
        val dialog = Dialog(requireContext(), android.R.style.Theme_Black_NoTitleBar_Fullscreen)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_closet)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )

        val mascot = dialog.findViewById<com.ecogo.ui.views.MascotLionView>(R.id.mascot_closet)
        val outfitDetail = dialog.findViewById<TextView>(R.id.text_outfit_detail)
        val btnClose = dialog.findViewById<android.widget.ImageView>(R.id.btn_close)
        val tabAll = dialog.findViewById<com.google.android.material.button.MaterialButton>(R.id.tab_all_clothes)
        val tabFaculty = dialog.findViewById<com.google.android.material.button.MaterialButton>(R.id.tab_faculty_clothes)
        val recycler = dialog.findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.recycler_closet)

        closetDialog = dialog
        closetMascot = mascot
        closetOutfitDetail = outfitDetail

        updateClosetMascot()

        val gridLayoutManager = GridLayoutManager(requireContext(), 2)
        recycler.layoutManager = gridLayoutManager

        val shopAdapter = ShopItemAdapter(getShopItemsGrouped()) { item ->
            handleItemClick(item)
            closetAdapter?.updateItems(getShopItemsGrouped())
            updateClosetMascot()
        }
        closetAdapter = shopAdapter

        gridLayoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                val adapter = recycler.adapter
                return when {
                    adapter is ShopItemAdapter && adapter.isHeader(position) -> 2
                    else -> 1
                }
            }
        }

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

        closetCurrentTab = "all"
        recycler.adapter = shopAdapter

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

        updateClosetTabStyle(tabAll, tabFaculty)

        btnClose.setOnClickListener { dialog.dismiss() }

        dialog.setOnDismissListener {
            closetDialog = null
            closetAdapter = null
            closetFacultyAdapter = null
            closetMascot = null
            closetOutfitDetail = null
            updateMascotOutfit()
            updateClosetPreview()
        }

        dialog.show()
        Log.d("ProfileFragment", "Opened Closet dialog")
    }

    private fun updateClosetMascot() {
        closetMascot?.outfit = Outfit(
            head = currentOutfit["head"] ?: "none",
            face = currentOutfit["face"] ?: "none",
            body = currentOutfit["body"] ?: "none",
            badge = currentOutfit["badge"] ?: "none"
        )
        val parts = mutableListOf<String>()
        val head = currentOutfit["head"] ?: "none"
        val face = currentOutfit["face"] ?: "none"
        val body = currentOutfit["body"] ?: "none"
        if (head != "none") parts.add(getItemShortName(head))
        if (face != "none") parts.add(getItemShortName(face))
        if (body != "none") parts.add(getItemShortName(body))
        closetOutfitDetail?.text = if (parts.isEmpty()) "No outfit equipped" else parts.joinToString(" + ")
    }

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

    private fun getFacultyOutfitCost(faculty: FacultyData): Int {
        val shopMap = MockData.SHOP_ITEMS.associateBy { it.id }
        var cost = 0
        if (faculty.outfit.head != "none") cost += shopMap[faculty.outfit.head]?.cost ?: 0
        if (faculty.outfit.face != "none") cost += shopMap[faculty.outfit.face]?.cost ?: 0
        if (faculty.outfit.body != "none") cost += shopMap[faculty.outfit.body]?.cost ?: 0
        return cost
    }

    private fun handleFacultyClick(faculty: FacultyData) {
        if (ownedFaculties.contains(faculty.id)) {
            equipFacultyOutfit(faculty)
            return
        }

        val componentIds = listOf(faculty.outfit.head, faculty.outfit.face, faculty.outfit.body)
            .filter { it != "none" }
        val ownedComponents = componentIds.filter { inventory.contains(it) }
        val missingComponents = componentIds.filterNot { inventory.contains(it) }

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
                    Toast.makeText(
                        requireContext(),
                        "Not enough points! Need $missingCost pts",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@showConfirmPurchaseDialog
                }

                currentPoints -= missingCost
                binding.textPoints.text = currentPoints.toString()

                missingComponents.forEach { id ->
                    if (!inventory.contains(id)) inventory.add(id)
                }

                ownedFaculties.add(faculty.id)
                closetFacultyAdapter?.updateOwned(ownedFaculties)

                equipFacultyOutfit(faculty)

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

    // ⭐ 修改：getShopItemsGrouped 使用服务器数据
    private fun getShopItemsGrouped(): List<ShopListItem> {
        val cloths = if (shopItems.isNotEmpty()) {
            DataMapper.mergeClothData(
                shopItems.filter { it.category == "cloth" },
                userItems
            )
        } else {
            MockData.SHOP_ITEMS.map { item ->
                item.copy(
                    owned = inventory.contains(item.id),
                    equipped = currentOutfit[item.type] == item.id
                )
            }
        }

        val result = mutableListOf<ShopListItem>()

        val headItems = cloths.filter { it.type == "head" }
        val faceItems = cloths.filter { it.type == "face" }
        val bodyItems = cloths.filter { it.type == "body" }

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

    // ⭐ 修改：handleItemClick 调用 API
    private fun handleItemClick(item: ShopItem) {
        Log.d("ProfileFragment", "Item clicked: ${item.id}")

        val isOwned = inventory.contains(item.id)
        val isEquipped = currentOutfit[item.type] == item.id

        when {
            isEquipped -> {
                unequipClothWithApi(item.id, item.type)
            }
            isOwned -> {
                equipClothWithApi(item.id, item.type)
            }
            else -> {
                showPurchaseConfirmDialog(item)
            }
        }
    }

    // ⭐ 新增：显示购买确认对话框
    private fun showPurchaseConfirmDialog(item: ShopItem) {
        val message = "Price: ${item.cost} pts\n\nPurchase and equip \"${item.name}\"?"
        showConfirmPurchaseDialog(
            icon = getItemEmoji(item.id),
            title = "Purchase Item",
            message = message,
            onConfirm = { purchaseClothWithApi(item) }
        )
    }

    // ⭐ 新增：购买服饰 API
    private fun purchaseClothWithApi(item: ShopItem) {
        if (currentPoints < item.cost) {
            Toast.makeText(requireContext(), "Not enough points!", Toast.LENGTH_SHORT).show()
            return
        }

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                loadingDialog.show("Purchasing...")

                val result = badgeClothRepository.purchaseItem(currentUserId, item.id)

                result.onSuccess { userBadge ->
                    currentPoints -= item.cost
                    binding.textPoints.text = currentPoints.toString()

                    if (!inventory.contains(item.id)) {
                        inventory.add(item.id)
                    }
                    userItems.add(userBadge)

                    loadingDialog.dismiss()

                    equipClothWithApi(item.id, item.type)

                    showSuccessDialog("Bought ${item.name}!", "-${item.cost} pts")

                    val popIn = AnimationUtils.loadAnimation(requireContext(), R.anim.pop_in)
                    binding.cardMascot.startAnimation(popIn)

                    Log.d("ProfileFragment", "Purchased: ${item.id}")
                }.onFailure { error ->
                    loadingDialog.dismiss()
                    Toast.makeText(requireContext(), "Purchase failed: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                loadingDialog.dismiss()
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // ⭐ 新增：装备服饰 API
    private fun equipClothWithApi(clothId: String, type: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                loadingDialog.show("Equipping...")

                val result = badgeClothRepository.equipItem(currentUserId, clothId)

                result.onSuccess { updatedUserBadge ->
                    currentOutfit[type] = clothId

                    val index = userItems.indexOfFirst { it.badgeId == clothId }
                    if (index >= 0) {
                        userItems[index] = updatedUserBadge
                    }

                    refreshShopAdapter()
                    updateMascotOutfit()
                    updateClosetPreview()
                    closetAdapter?.updateItems(getShopItemsGrouped())
                    updateClosetMascot()

                    loadingDialog.dismiss()

                    Log.d("ProfileFragment", "Equipped: $clothId")
                }.onFailure { error ->
                    loadingDialog.dismiss()
                    Toast.makeText(requireContext(), "Failed to equip: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                loadingDialog.dismiss()
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // ⭐ 新增：卸下服饰 API
    private fun unequipClothWithApi(clothId: String, type: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                loadingDialog.show("Unequipping...")

                val result = badgeClothRepository.unequipItem(currentUserId, clothId)

                result.onSuccess {
                    currentOutfit[type] = "none"

                    val index = userItems.indexOfFirst { it.badgeId == clothId }
                    if (index >= 0) {
                        userItems[index] = userItems[index].copy(isDisplay = false)
                    }

                    refreshShopAdapter()
                    updateMascotOutfit()
                    updateClosetPreview()
                    closetAdapter?.updateItems(getShopItemsGrouped())
                    updateClosetMascot()

                    loadingDialog.dismiss()

                    Log.d("ProfileFragment", "Unequipped: $clothId")
                }.onFailure { error ->
                    loadingDialog.dismiss()
                    Toast.makeText(requireContext(), "Failed to unequip: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                loadingDialog.dismiss()
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // 保留原有的本地购买方法（用于 Faculty）
    private fun purchaseAndEquipItem(item: ShopItem) {
        if (currentPoints < item.cost) {
            Toast.makeText(
                requireContext(),
                "Not enough points! Need ${item.cost} pts",
                Toast.LENGTH_SHORT
            ).show()
            Log.d("ProfileFragment", "Insufficient points for ${item.name}")
            return
        }

        currentPoints -= item.cost
        binding.textPoints.text = currentPoints.toString()
        if (!inventory.contains(item.id)) inventory.add(item.id)
        currentOutfit[item.type] = item.id

        refreshShopAdapter()
        updateMascotOutfit()
        updateClosetPreview()
        closetAdapter?.updateItems(getShopItemsGrouped())
        updateClosetMascot()

        showSuccessDialog("Bought & Equipped ${item.name}!", "-${item.cost} pts")

        val popIn = AnimationUtils.loadAnimation(requireContext(), R.anim.pop_in)
        binding.cardMascot.startAnimation(popIn)

        Log.d("ProfileFragment", "Purchased ${item.name} for ${item.cost} pts")
    }

    private fun updateMascotOutfit() {
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
        dialog.window?.attributes?.windowAnimations = R.style.DialogAnimation
    }

    private fun updateBadgeEntry() {
        val unlockedCount = MockData.ACHIEVEMENTS.count { it.unlocked }
        val totalCount = MockData.ACHIEVEMENTS.size
        binding.textBadgeCount.text = "$unlockedCount / $totalCount unlocked"

        val equippedBadgeId = currentOutfit["badge"] ?: "none"
        val previewEmoji = if (equippedBadgeId != "none") {
            getBadgeEmoji(equippedBadgeId)
        } else {
            "🏆"
        }
        binding.textBadgePreview.text = previewEmoji
    }

    private fun setupBadgeEntry() {
        binding.cardBadges.setOnClickListener {
            showBadgesDialog()
        }
    }

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

        iconView.text = getBadgeEmoji(achievement.id)
        nameView.text = achievement.name
        descView.text = achievement.description
        howToView.text = achievement.howToUnlock.ifEmpty { "Complete the required task to unlock this badge." }

        val isEquipped = currentOutfit["badge"] == achievement.id

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

                    mascot?.outfit = Outfit(
                        head = currentOutfit["head"] ?: "none",
                        face = currentOutfit["face"] ?: "none",
                        body = currentOutfit["body"] ?: "none",
                        badge = "none"
                    )
                    badgeLabel?.text = "No Badge Equipped"

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

    private fun refreshBadgeList() {
        updateBadgeEntry()
    }

    private fun setupTabs() {
        binding.cardCloset.visibility = View.VISIBLE
        binding.cardBadges.visibility = View.VISIBLE
    }

    private fun setupAnimations() {
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
        "face_glasses_square" -> "👓"
        "face_glasses_round" -> "👓"
        "face_sunglasses" -> "😎"
        "face_mask" -> "😷"
        "face_monocle" -> "🧐"
        "face_goggles" -> "🥽"
        "face_vr" -> "🥽"
        "face_diving" -> "🤿"
        "face_scarf" -> "🧣"
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

    // ⭐ 修改：onDestroyView 添加清理
    override fun onDestroyView() {
        super.onDestroyView()

        loadingDialog.dismiss()
        closetDialog?.dismiss()

        _binding = null
    }
}