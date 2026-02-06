package com.ecogo.ui.fragments

import android.animation.ValueAnimator
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.ecogo.R
import com.ecogo.data.FacultyData
import com.ecogo.data.MockData
import com.ecogo.databinding.FragmentSignupWizardBinding
import com.ecogo.ui.adapters.FacultySwipeAdapter
import kotlin.math.abs

/**
 * SignupWizardFragment - 注册向导
 * 
 * 六步流程:
 * Step 0: 个人信息填写（用户名、邮箱、NUSNET ID）
 * Step 1: 学院选择（滑动卡片）
 * Step 2: 交通偏好（公交/步行/骑行/拼车）
 * Step 3: 常用地点（宿舍/教学楼/学习地点）
 * Step 4: 兴趣目标（兴趣、目标、通知偏好）
 * Step 5: 小狮子换装展示
 */
class SignupWizardFragment : Fragment() {
    
    private var _binding: FragmentSignupWizardBinding? = null
    private val binding get() = _binding!!
    
    private var currentStep = 0
    private var selectedFaculty: FacultyData? = null
    
    // Step 0: Personal info
    private var username: String = ""
    private var email: String = ""
    private var nusnetId: String = ""
    private var password: String = ""
    
    // Step 2: Transport Preferences
    private val transportPrefs = mutableSetOf<String>()
    
    // Step 3: Common Locations
    private var dormitory: String? = null
    private var teachingBuilding: String? = null
    private var studySpot: String? = null
    private val otherLocations = mutableSetOf<String>()
    
    // Step 4: Interests & Goals
    private val interests = mutableSetOf<String>()
    private var weeklyGoal: Int = 5
    private var notifyChallenges: Boolean = true
    private var notifyReminders: Boolean = true
    private var notifyFriends: Boolean = false
    
    // Animation references
    private var buttonAnimator: ValueAnimator? = null
    private var mascotScaleAnimator: ValueAnimator? = null
    private var mascotRotateAnimator: ValueAnimator? = null
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return try {
            Log.d("DEBUG_SIGNUP", "SignupWizardFragment onCreateView")
            Toast.makeText(context, "📝 SignupWizard 正在加载...", Toast.LENGTH_SHORT).show()
            _binding = FragmentSignupWizardBinding.inflate(inflater, container, false)
            Log.d("DEBUG_SIGNUP", "SignupWizardFragment binding inflated")
            Toast.makeText(context, "✅ SignupWizard 加载成功！", Toast.LENGTH_SHORT).show()
            binding.root
        } catch (e: Exception) {
            Log.e("DEBUG_SIGNUP", "SignupWizardFragment onCreateView FAILED: ${e.message}", e)
            Toast.makeText(context, "❌ SignupWizard 创建失败: ${e.message}", Toast.LENGTH_LONG).show()
            throw e
        }
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        try {
            Log.d("DEBUG_SIGNUP", "SignupWizardFragment onViewCreated")
            showPersonalInfo()
            Log.d("DEBUG_SIGNUP", "SignupWizardFragment personal info shown")
        } catch (e: Exception) {
            Log.e("DEBUG_SIGNUP", "SignupWizardFragment onViewCreated FAILED: ${e.message}", e)
            Toast.makeText(requireContext(), "SignupWizard 初始化失败: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
    
    private fun showPersonalInfo() {
        currentStep = 0
        
        // 显示个人信息界面
        binding.layoutPersonalInfo.visibility = View.VISIBLE
        binding.layoutFacultySelection.visibility = View.GONE
        binding.layoutTransportPreference.root.visibility = View.GONE
        binding.layoutCommonLocations.root.visibility = View.GONE
        binding.layoutInterestsGoals.root.visibility = View.GONE
        binding.layoutMascotReveal.visibility = View.GONE
        
        // 输入验证
        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                validatePersonalInfo()
            }
        }
        
        binding.inputUsername.addTextChangedListener(textWatcher)
        binding.inputEmail.addTextChangedListener(textWatcher)
        binding.inputNusnet.addTextChangedListener(textWatcher)
        binding.inputPassword.addTextChangedListener(textWatcher)
        binding.inputConfirmPassword.addTextChangedListener(textWatcher)
        
        // Next 按钮
        binding.btnNextToFaculty.isEnabled = false
        binding.btnNextToFaculty.alpha = 0.5f
        binding.btnNextToFaculty.setOnClickListener {
            username = binding.inputUsername.text.toString()
            email = binding.inputEmail.text.toString()
            nusnetId = binding.inputNusnet.text.toString()
            password = binding.inputPassword.text.toString()
            
            // Disable button and show loading state
            binding.btnNextToFaculty.isEnabled = false
            binding.btnNextToFaculty.text = "Creating Account..."
            
            val request = com.ecogo.api.MobileRegisterRequest(
                userid = nusnetId,
                password = password,
                repassword = password,
                nickname = username,
                email = email
            )
            
            // Call Register API
            viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                try {
                    val apiService = com.ecogo.api.RetrofitClient.apiService
                    val response = apiService.register(request)
                    
                    withContext(Dispatchers.Main) {
                        binding.btnNextToFaculty.isEnabled = true
                        binding.btnNextToFaculty.text = "Next: Choose Faculty"
                        
                        if (response.success && response.data != null) {
                            Log.d("DEBUG_SIGNUP", "Step 0 Registration Success: ${response.data.userid}")
                            Toast.makeText(requireContext(), "Account created! Please complete your profile.", Toast.LENGTH_SHORT).show()
                            
                            // Save registration data locally immediately
                            saveRegistrationData()
                            
                            // Proceed to next step
                            showFacultySelection()
                        } else {
                            val msg = response.message
                            Log.e("DEBUG_SIGNUP", "Registration Failed: $msg")
                            Toast.makeText(requireContext(), "Registration Failed: $msg", Toast.LENGTH_LONG).show()
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Log.e("DEBUG_SIGNUP", "Network Error: ${e.message}")
                        binding.btnNextToFaculty.isEnabled = true
                        binding.btnNextToFaculty.text = "Next: Choose Faculty"
                        Toast.makeText(requireContext(), "Network Error: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }
    
    private fun validatePersonalInfo() {
        val usernameText = binding.inputUsername.text.toString()
        val emailText = binding.inputEmail.text.toString()
        val nusnetText = binding.inputNusnet.text.toString()
        val passwordText = binding.inputPassword.text.toString()
        val confirmPasswordText = binding.inputConfirmPassword.text.toString()
        
        // 基本验证
        val isUsernameValid = usernameText.length >= 3
        val isEmailValid = emailText.contains("@") && emailText.contains(".")
        val isNusnetValid = nusnetText.startsWith("e", ignoreCase = true) && nusnetText.length >= 7
        val isPasswordValid = passwordText.length >= 6
        val isPasswordMatch = passwordText == confirmPasswordText && passwordText.isNotEmpty()
        
        val isValid = isUsernameValid && isEmailValid && isNusnetValid && isPasswordValid && isPasswordMatch
        
        binding.btnNextToFaculty.isEnabled = isValid
        binding.btnNextToFaculty.alpha = if (isValid) 1f else 0.5f
        
        // 错误提示
        if (usernameText.isNotEmpty() && !isUsernameValid) {
            binding.inputLayoutUsername.error = "Username must be at least 3 characters"
        } else {
            binding.inputLayoutUsername.error = null
        }
        
        if (emailText.isNotEmpty() && !isEmailValid) {
            binding.inputLayoutEmail.error = "Invalid email format"
        } else {
            binding.inputLayoutEmail.error = null
        }
        
        if (nusnetText.isNotEmpty() && !isNusnetValid) {
            binding.inputLayoutNusnet.error = "Must start with 'e' and be at least 7 characters"
        } else {
            binding.inputLayoutNusnet.error = null
        }
        
        if (passwordText.isNotEmpty() && !isPasswordValid) {
            binding.inputLayoutPassword.error = "Password must be at least 6 characters"
        } else {
            binding.inputLayoutPassword.error = null
        }
        
        if (confirmPasswordText.isNotEmpty() && !isPasswordMatch) {
            binding.inputLayoutConfirmPassword.error = "Passwords do not match"
        } else {
            binding.inputLayoutConfirmPassword.error = null
        }
    }
    
    private fun updateProfileStep(
        request: com.ecogo.api.UpdateProfileRequest,
        onSuccess: () -> Unit
    ) {
        // Show loading if needed? Ideally keep UI responsive or show small indicator
        // For wizard flow, we ideally block or show loading on the button
        
        Log.d("DEBUG_SIGNUP", "Updating profile for $nusnetId with request: $request")
        
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            try {
                val apiService = com.ecogo.api.RetrofitClient.apiService
                val response = apiService.updateProfile(nusnetId, request)
                
                withContext(Dispatchers.Main) {
                    if (response.success) {
                        Log.d("DEBUG_SIGNUP", "Profile update success")
                        onSuccess()
                    } else {
                        Log.e("DEBUG_SIGNUP", "Profile update failed: ${response.message}")
                        Toast.makeText(requireContext(), "Update failed: ${response.message}", Toast.LENGTH_SHORT).show()
                        // Still allow proceeding? Ideally no, but for smooth UX/prototype maybe yes?
                        // User requested "call this interface", implying strict flow.
                        // We will BLOCK on failure to ensure data integrity as per implied requirement.
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e("DEBUG_SIGNUP", "Profile update network error: ${e.message}")
                    Toast.makeText(requireContext(), "Network error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun showFacultySelection() {
        currentStep = 1
        
        // 切换界面
        binding.layoutPersonalInfo.visibility = View.GONE
        binding.layoutFacultySelection.visibility = View.VISIBLE
        binding.layoutTransportPreference.root.visibility = View.GONE
        binding.layoutCommonLocations.root.visibility = View.GONE
        binding.layoutInterestsGoals.root.visibility = View.GONE
        binding.layoutMascotReveal.visibility = View.GONE
        
        // 设置ViewPager2适配器
        val adapter = FacultySwipeAdapter(MockData.FACULTY_DATA) { faculty ->
            // 选择后跳转到交通偏好
            selectedFaculty = faculty
            android.util.Log.d("DEBUG_SIGNUP", "Faculty selected: ${faculty.name}")
            
             // Call API for Faculty
            val request = com.ecogo.api.UpdateProfileRequest(
                faculty = faculty.name // Or map to internal enum if needed, assuming name is fine
            )
            
            updateProfileStep(request) {
                 binding.viewpagerFaculties.postDelayed({
                    showTransportPreference()
                }, 300)
            }
        }
        
        binding.viewpagerFaculties.adapter = adapter
        
        // 设置ViewPager2的页面切换监听
        binding.viewpagerFaculties.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                // 更新页面指示器
                binding.textPageIndicator.text = "${position + 1} / ${MockData.FACULTY_DATA.size}"
            }
        })
        
        // 初始化页面指示器
        binding.textPageIndicator.text = "1 / ${MockData.FACULTY_DATA.size}"
        
        // 配置ViewPager2的页面转换效果
        setupPageTransformer()
    }
    
    private fun setupPageTransformer() {
        binding.viewpagerFaculties.apply {
            // 设置页面间距
            offscreenPageLimit = 1
            
            setPageTransformer { page, position ->
                val absPosition = abs(position)
                
                // 缩放效果
                page.scaleY = 0.85f + (1 - absPosition) * 0.15f
                page.scaleX = 0.85f + (1 - absPosition) * 0.15f
                
                // 透明度效果
                page.alpha = 0.5f + (1 - absPosition) * 0.5f
                
                // 轻微旋转效果
                page.rotationY = position * -15f
            }
        }
    }
    
    private fun showTransportPreference() {
        currentStep = 2
        
        // 切换界面
        binding.layoutPersonalInfo.visibility = View.GONE
        binding.layoutFacultySelection.visibility = View.GONE
        binding.layoutTransportPreference.root.visibility = View.VISIBLE
        binding.layoutCommonLocations.root.visibility = View.GONE
        binding.layoutInterestsGoals.root.visibility = View.GONE
        binding.layoutMascotReveal.visibility = View.GONE
        
        Log.d("DEBUG_SIGNUP", "Showing transport preference")
        
        // 设置交通卡片点击监听
        val cards = mapOf(
            binding.layoutTransportPreference.cardBus to Pair("bus", binding.layoutTransportPreference.checkBus),
            binding.layoutTransportPreference.cardWalking to Pair("walking", binding.layoutTransportPreference.checkWalking),
            binding.layoutTransportPreference.cardCycling to Pair("cycling", binding.layoutTransportPreference.checkCycling),
            binding.layoutTransportPreference.cardCarpool to Pair("carpool", binding.layoutTransportPreference.checkCarpool)
        )
        
        cards.forEach { (card, typeAndCheck) ->
            val (type, checkView) = typeAndCheck
            card.setOnClickListener {
                if (transportPrefs.contains(type)) {
                    transportPrefs.remove(type)
                    card.strokeColor = ContextCompat.getColor(requireContext(), R.color.border)
                    checkView.visibility = View.GONE
                } else {
                    transportPrefs.add(type)
                    card.strokeColor = ContextCompat.getColor(requireContext(), R.color.primary)
                    checkView.visibility = View.VISIBLE
                }
                
                // 至少选择一个才能继续
                binding.layoutTransportPreference.btnContinueTransport.isEnabled = transportPrefs.isNotEmpty()
                binding.layoutTransportPreference.btnContinueTransport.alpha = if (transportPrefs.isNotEmpty()) 1f else 0.5f
            }
        }
        
        // Continue按钮
         binding.layoutTransportPreference.btnContinueTransport.setOnClickListener {
             // Prepare API Request
             val request = com.ecogo.api.UpdateProfileRequest(
                 preferences = com.ecogo.api.TransportPreferencesWrapper(
                     preferredTransport = transportPrefs.toList()
                 )
             )
             
             // Disable button
             binding.layoutTransportPreference.btnContinueTransport.isEnabled = false
             binding.layoutTransportPreference.btnContinueTransport.text = "Saving..."
             
             updateProfileStep(request) {
                 binding.layoutTransportPreference.btnContinueTransport.text = "Continue" // Reset text (though we move)
                 showCommonLocations()
             }
        }
    }
    
    private fun showCommonLocations() {
        currentStep = 3
        
        // 切换界面
        binding.layoutPersonalInfo.visibility = View.GONE
        binding.layoutFacultySelection.visibility = View.GONE
        binding.layoutTransportPreference.root.visibility = View.GONE
        binding.layoutCommonLocations.root.visibility = View.VISIBLE
        binding.layoutInterestsGoals.root.visibility = View.GONE
        binding.layoutMascotReveal.visibility = View.GONE
        
        Log.d("DEBUG_SIGNUP", "Showing common locations")
        
        // Chip监听
        binding.layoutCommonLocations.chipGym.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) otherLocations.add("gym") else otherLocations.remove("gym")
        }
        binding.layoutCommonLocations.chipCanteen.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) otherLocations.add("canteen") else otherLocations.remove("canteen")
        }
        binding.layoutCommonLocations.chipLab.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) otherLocations.add("lab") else otherLocations.remove("lab")
        }
        binding.layoutCommonLocations.chipSports.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) otherLocations.add("sports") else otherLocations.remove("sports")
        }
        
        // Skip按钮
        binding.layoutCommonLocations.btnSkipLocations.setOnClickListener {
            Log.d("DEBUG_SIGNUP", "Locations skipped")
            showInterestsGoals()
        }
        
        // Continue按钮
        binding.layoutCommonLocations.btnContinueLocations.setOnClickListener {
            dormitory = binding.layoutCommonLocations.inputDorm.text.toString()
            teachingBuilding = binding.layoutCommonLocations.inputBuilding.text.toString()
            studySpot = binding.layoutCommonLocations.inputLibrary.text.toString()
            
            val request = com.ecogo.api.UpdateProfileRequest(
                dormitoryOrResidence = dormitory,
                mainTeachingBuilding = teachingBuilding,
                favoriteStudySpot = studySpot
            )
            
            binding.layoutCommonLocations.btnContinueLocations.isEnabled = false
            binding.layoutCommonLocations.btnContinueLocations.text = "Saving..."
            
            updateProfileStep(request) {
                showInterestsGoals()
            }
        }
    }
    
    private fun showInterestsGoals() {
        currentStep = 4
        
        // 切换界面
        binding.layoutPersonalInfo.visibility = View.GONE
        binding.layoutFacultySelection.visibility = View.GONE
        binding.layoutTransportPreference.root.visibility = View.GONE
        binding.layoutCommonLocations.root.visibility = View.GONE
        binding.layoutInterestsGoals.root.visibility = View.VISIBLE
        binding.layoutMascotReveal.visibility = View.GONE
        
        Log.d("DEBUG_SIGNUP", "Showing interests and goals")
        
        // 兴趣Chips监听
        binding.layoutInterestsGoals.chipSustainability.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) interests.add("sustainability") else interests.remove("sustainability")
        }
        binding.layoutInterestsGoals.chipChallenges.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) interests.add("challenges") else interests.remove("challenges")
        }
        binding.layoutInterestsGoals.chipCommunity.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) interests.add("community") else interests.remove("community")
        }
        binding.layoutInterestsGoals.chipRewards.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) interests.add("rewards") else interests.remove("rewards")
        }
        binding.layoutInterestsGoals.chipLeaderboard.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) interests.add("leaderboard") else interests.remove("leaderboard")
        }
        
        // 每周目标Slider
        binding.layoutInterestsGoals.sliderWeeklyGoal.addOnChangeListener { _, value, _ ->
            weeklyGoal = value.toInt()
            binding.layoutInterestsGoals.textGoalValue.text = weeklyGoal.toString()
        }
        
        // 通知开关
        binding.layoutInterestsGoals.switchChallenges.setOnCheckedChangeListener { _, isChecked ->
            notifyChallenges = isChecked
        }
        binding.layoutInterestsGoals.switchReminders.setOnCheckedChangeListener { _, isChecked ->
            notifyReminders = isChecked
        }
        binding.layoutInterestsGoals.switchFriends.setOnCheckedChangeListener { _, isChecked ->
            notifyFriends = isChecked
        }
        
        // Finish按钮
        binding.layoutInterestsGoals.btnFinishSignup.setOnClickListener {
            // Prepare request
            val request = com.ecogo.api.UpdateProfileRequest(
                interests = interests.toList(),
                weeklyGoals = weeklyGoal,
                newChallenges = notifyChallenges,
                activityReminders = notifyReminders,
                friendActivity = notifyFriends
            )
            
            binding.layoutInterestsGoals.btnFinishSignup.isEnabled = false
            binding.layoutInterestsGoals.btnFinishSignup.text = "Finalizing..."
            
            updateProfileStep(request) {
                 selectedFaculty?.let { faculty ->
                    showMascotReveal(faculty)
                }
            }
        }
    }
    
    private fun showMascotReveal(faculty: FacultyData) {
        currentStep = 5
        
        // 切换到小狮子展示界面
        binding.layoutPersonalInfo.visibility = View.GONE
        binding.layoutFacultySelection.visibility = View.GONE
        binding.layoutTransportPreference.root.visibility = View.GONE
        binding.layoutCommonLocations.root.visibility = View.GONE
        binding.layoutInterestsGoals.root.visibility = View.GONE
        binding.layoutMascotReveal.visibility = View.VISIBLE
        
        binding.textRevealTitle.text = "Welcome, $username!"
        binding.textRevealSubtitle.text = "Meet your new buddy."
        
        // 设置小狮子装备
        binding.mascotReveal.outfit = faculty.outfit
        
        // 入场动画
        mascotScaleAnimator = ValueAnimator.ofFloat(0.5f, 1f).apply {
            duration = 600
            interpolator = AccelerateDecelerateInterpolator()
            addUpdateListener { animation ->
                val scale = animation.animatedValue as Float
                binding.mascotReveal.scaleX = scale
                binding.mascotReveal.scaleY = scale
            }
        }
        mascotScaleAnimator?.start()
        
        // 轻微旋转动画
        mascotRotateAnimator = ValueAnimator.ofFloat(-5f, 5f).apply {
            duration = 2000
            repeatMode = ValueAnimator.REVERSE
            repeatCount = ValueAnimator.INFINITE
            interpolator = AccelerateDecelerateInterpolator()
            addUpdateListener { animation ->
                binding.mascotReveal.rotation = animation.animatedValue as Float
            }
        }
        mascotRotateAnimator?.start()
        
        // 学院信息
        binding.textFacultyName.text = faculty.name
        binding.textFacultySlogan.text = faculty.slogan
        binding.viewFacultyColor.setBackgroundColor(android.graphics.Color.parseColor(faculty.color))
        
        // 服装预览
        val outfitItems = mutableListOf<String>()
        if (faculty.outfit.head != "none") {
            outfitItems.add(getItemName(faculty.outfit.head))
        }
        if (faculty.outfit.face != "none") {
            outfitItems.add(getItemName(faculty.outfit.face))
        }
        if (faculty.outfit.body != "none") {
            outfitItems.add(getItemName(faculty.outfit.body))
        }
        binding.textOutfitItems.text = "Starter Outfit: ${outfitItems.joinToString(", ")}"
        
        // Let's Go! 按钮动画
        buttonAnimator = ValueAnimator.ofFloat(1f, 1.05f).apply {
            duration = 1000
            repeatMode = ValueAnimator.REVERSE
            repeatCount = ValueAnimator.INFINITE
            interpolator = AccelerateDecelerateInterpolator()
            addUpdateListener { animation ->
                val scale = animation.animatedValue as Float
                binding.btnLetsGo.scaleX = scale
                binding.btnLetsGo.scaleY = scale
            }
        }
        buttonAnimator?.start()
        
        binding.btnLetsGo.setOnClickListener {
            Log.d("DEBUG_SIGNUP", "Let's Go button clicked!")
            Toast.makeText(requireContext(), "🎯 Let's Go 按钮已点击", Toast.LENGTH_SHORT).show()
            
            // 停止所有动画
            buttonAnimator?.cancel()
            mascotScaleAnimator?.cancel()
            mascotRotateAnimator?.cancel()
            
            // 重置按钮缩放
            binding.btnLetsGo.scaleX = 1f
            binding.btnLetsGo.scaleY = 1f
            
            completeSignup(faculty)
        }
    }
    
    private fun completeSignup(faculty: FacultyData) {
        Log.d("DEBUG_SIGNUP", "=== Completing Signup Wizard ===")
        
        // Save remaining profile data (Preferences)
        selectedFaculty = faculty // ensured
        saveRegistrationData()
        saveFirstLoginStatus(true)
        
        Toast.makeText(requireContext(), "Profile setup complete! Please login.", Toast.LENGTH_LONG).show()
        
        // Navigate to Login
        binding.root.postDelayed({
            try {
                findNavController().navigate(R.id.loginFragment)
            } catch (e: Exception) {
                Log.e("DEBUG_SIGNUP", "Navigation failed: ${e.message}")
            }
        }, 1000)
    }
    
    private fun saveRegistrationData() {
        val prefs = requireContext().getSharedPreferences("EcoGoPrefs", Context.MODE_PRIVATE)
        prefs.edit().apply {
            putString("username", username)
            putString("email", email)
            putString("nusnet_id", nusnetId)
            putString("password", password)  // 注意：实际应用中应该加密存储
            putString("faculty", selectedFaculty?.name)
            putStringSet("transport_prefs", transportPrefs)
            putString("dormitory", dormitory)
            putString("teaching_building", teachingBuilding)
            putString("study_spot", studySpot)
            putStringSet("other_locations", otherLocations)
            putStringSet("interests", interests)
            putInt("weekly_goal", weeklyGoal)
            putBoolean("notify_challenges", notifyChallenges)
            putBoolean("notify_reminders", notifyReminders)
            putBoolean("notify_friends", notifyFriends)
            putBoolean("is_registered", true)  // 标记已注册
            apply()
        }
        Log.d("DEBUG_SIGNUP", "Registration data saved to SharedPreferences")
    }
    
    private fun saveFirstLoginStatus(isFirstLogin: Boolean) {
        val prefs = requireContext().getSharedPreferences("EcoGoPrefs", Context.MODE_PRIVATE)
        prefs.edit().putBoolean("is_first_login", isFirstLogin).apply()
        Log.d("DEBUG_SIGNUP", "First login status set to: $isFirstLogin")
    }
    
    private fun getItemName(itemId: String): String {
        return when (itemId) {
            "hat_helmet" -> "Safety Helmet"
            "hat_beret" -> "Artist Beret"
            "hat_grad" -> "Grad Cap"
            "hat_cap" -> "Orange Cap"
            "face_goggles" -> "Safety Goggles"
            "glasses_sun" -> "Shades"
            "body_plaid" -> "Engin Plaid"
            "body_suit" -> "Biz Suit"
            "body_coat" -> "Lab Coat"
            "shirt_nus" -> "NUS Tee"
            "shirt_hoodie" -> "Blue Hoodie"
            else -> ""
        }
    }
    
    override fun onDestroyView() {
        // 清理动画
        buttonAnimator?.cancel()
        mascotScaleAnimator?.cancel()
        mascotRotateAnimator?.cancel()
        
        super.onDestroyView()
        _binding = null
    }
}
