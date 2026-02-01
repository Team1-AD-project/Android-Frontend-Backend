package com.ecogo.ui.fragments

import android.animation.ValueAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.ecogo.R
import com.ecogo.data.FacultyData
import com.ecogo.data.MockData
import com.ecogo.databinding.FragmentSignupWizardBinding
import com.ecogo.ui.adapters.FacultyAdapter

/**
 * SignupWizardFragment - 注册向导
 * 
 * 两步流程:
 * Step 0: 学院选择
 * Step 1: 小狮子换装展示
 */
class SignupWizardFragment : Fragment() {
    
    private var _binding: FragmentSignupWizardBinding? = null
    private val binding get() = _binding!!
    
    private var currentStep = 0
    private var selectedFaculty: FacultyData? = null
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSignupWizardBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        showFacultySelection()
    }
    
    private fun showFacultySelection() {
        currentStep = 0
        
        // 显示学院选择界面
        binding.layoutFacultySelection.visibility = View.VISIBLE
        binding.layoutMascotReveal.visibility = View.GONE
        
        binding.textTitle.text = "Choose Faculty"
        binding.textSubtitle.text = "This determines your starter outfit!"
        
        // 设置学院列表
        val adapter = FacultyAdapter(MockData.FACULTY_DATA) { faculty ->
            selectedFaculty = faculty
            binding.btnContinue.isEnabled = true
            binding.btnContinue.alpha = 1f
        }
        
        binding.recyclerFaculties.apply {
            layoutManager = LinearLayoutManager(context)
            this.adapter = adapter
        }
        
        // Continue 按钮
        binding.btnContinue.isEnabled = false
        binding.btnContinue.alpha = 0.5f
        binding.btnContinue.setOnClickListener {
            selectedFaculty?.let { faculty ->
                showMascotReveal(faculty)
            }
        }
    }
    
    private fun showMascotReveal(faculty: FacultyData) {
        currentStep = 1
        
        // 切换到小狮子展示界面
        binding.layoutFacultySelection.visibility = View.GONE
        binding.layoutMascotReveal.visibility = View.VISIBLE
        
        binding.textRevealTitle.text = "You're all set!"
        binding.textRevealSubtitle.text = "Meet your new buddy."
        
        // 设置小狮子装备
        binding.mascotReveal.outfit = faculty.outfit
        
        // 入场动画
        val scaleAnimator = ValueAnimator.ofFloat(0.5f, 1f).apply {
            duration = 600
            interpolator = AccelerateDecelerateInterpolator()
            addUpdateListener { animation ->
                val scale = animation.animatedValue as Float
                binding.mascotReveal.scaleX = scale
                binding.mascotReveal.scaleY = scale
            }
        }
        scaleAnimator.start()
        
        // 轻微旋转动画
        val rotateAnimator = ValueAnimator.ofFloat(-5f, 5f).apply {
            duration = 2000
            repeatMode = ValueAnimator.REVERSE
            repeatCount = ValueAnimator.INFINITE
            interpolator = AccelerateDecelerateInterpolator()
            addUpdateListener { animation ->
                binding.mascotReveal.rotation = animation.animatedValue as Float
            }
        }
        rotateAnimator.start()
        
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
        val buttonAnimator = ValueAnimator.ofFloat(1f, 1.05f).apply {
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
        buttonAnimator.start()
        
        binding.btnLetsGo.setOnClickListener {
            completeSignup(faculty)
        }
    }
    
    private fun completeSignup(faculty: FacultyData) {
        // TODO: 将学院服装添加到用户 inventory
        // TODO: 保存用户数据到后端
        
        // 导航到主界面
        findNavController().navigate(R.id.action_signup_to_home)
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
        super.onDestroyView()
        _binding = null
    }
}
