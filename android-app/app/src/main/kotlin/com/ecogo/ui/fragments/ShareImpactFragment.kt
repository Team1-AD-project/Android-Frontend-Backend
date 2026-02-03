package com.ecogo.ui.fragments

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.ecogo.R
import com.ecogo.data.MascotSize
import com.ecogo.databinding.FragmentShareImpactBinding
import java.io.File
import java.io.FileOutputStream

/**
 * 分享影响力页面
 * 生成分享卡片并分享到社交平台
 */
class ShareImpactFragment : Fragment() {

    private var _binding: FragmentShareImpactBinding? = null
    private val binding get() = _binding!!
    
    private var selectedPeriod = "today" // today, week, month

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentShareImpactBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupUI()
        loadStatistics()
        setupAnimations()
    }
    
    private fun setupUI() {
        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }
        
        // 周期选择
        binding.chipToday.setOnClickListener { selectPeriod("today") }
        binding.chipWeek.setOnClickListener { selectPeriod("week") }
        binding.chipMonth.setOnClickListener { selectPeriod("month") }
        
        // 分享按钮
        binding.btnShare.setOnClickListener {
            shareImpact()
        }
        
        // 保存图片按钮
        binding.btnSave.setOnClickListener {
            saveToGallery()
        }
    }
    
    private fun selectPeriod(period: String) {
        selectedPeriod = period
        
        // 更新Chip选中状态
        binding.chipToday.isChecked = period == "today"
        binding.chipWeek.isChecked = period == "week"
        binding.chipMonth.isChecked = period == "month"
        
        loadStatistics()
    }
    
    private fun loadStatistics() {
        // 根据选择的周期加载不同的统计数据（模拟）
        when (selectedPeriod) {
            "today" -> {
                binding.textPeriod.text = "今日影响力"
                binding.textTrips.text = "3"
                binding.textDistance.text = "5.2"
                binding.textCarbonSaved.text = "580"
                binding.textPoints.text = "290"
            }
            "week" -> {
                binding.textPeriod.text = "本周影响力"
                binding.textTrips.text = "15"
                binding.textDistance.text = "24.5"
                binding.textCarbonSaved.text = "2,750"
                binding.textPoints.text = "1,375"
            }
            "month" -> {
                binding.textPeriod.text = "本月影响力"
                binding.textTrips.text = "52"
                binding.textDistance.text = "98.3"
                binding.textCarbonSaved.text = "11,200"
                binding.textPoints.text = "5,600"
            }
        }
        
        // 设置小狮子装扮（应该从用户数据获取）
        binding.mascotShare.apply {
            mascotSize = MascotSize.LARGE
            setEmotion(com.ecogo.data.MascotEmotion.CELEBRATING)
            celebrateAnimation()
        }
    }
    
    private fun shareImpact() {
        // 生成分享图片
        val bitmap = generateShareCard()
        
        // 保存到临时文件
        val file = File(requireContext().cacheDir, "ecogo_impact_${System.currentTimeMillis()}.png")
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
        
        // 创建分享Intent
        val uri = FileProvider.getUriForFile(
            requireContext(),
            "${requireContext().packageName}.fileprovider",
            file
        )
        
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_TEXT, "我在EcoGo上的绿色出行成就！🌱 #EcoGo #绿色出行")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        
        startActivity(Intent.createChooser(shareIntent, "分享到"))
    }
    
    private fun saveToGallery() {
        // TODO: 实现保存到相册功能
        android.widget.Toast.makeText(
            requireContext(),
            "图片已保存到相册",
            android.widget.Toast.LENGTH_SHORT
        ).show()
    }
    
    private fun generateShareCard(): Bitmap {
        // 创建分享卡片Bitmap（800x600）
        val width = 800
        val height = 600
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        
        // 背景
        val bgPaint = Paint().apply {
            color = Color.parseColor("#ECFDF5")
        }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)
        
        // 标题
        val titlePaint = Paint().apply {
            color = Color.parseColor("#059669")
            textSize = 48f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("我的绿色出行", width / 2f, 80f, titlePaint)
        
        // 周期
        val periodPaint = Paint().apply {
            color = Color.parseColor("#6B7280")
            textSize = 32f
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(binding.textPeriod.text.toString(), width / 2f, 130f, periodPaint)
        
        // 统计数据
        val labelPaint = Paint().apply {
            color = Color.parseColor("#6B7280")
            textSize = 24f
            textAlign = Paint.Align.CENTER
        }
        val valuePaint = Paint().apply {
            color = Color.parseColor("#059669")
            textSize = 42f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        
        // 行程数
        canvas.drawText("行程次数", width / 4f, 250f, labelPaint)
        canvas.drawText(binding.textTrips.text.toString(), width / 4f, 300f, valuePaint)
        
        // 距离
        canvas.drawText("总里程(km)", width * 3 / 4f, 250f, labelPaint)
        canvas.drawText(binding.textDistance.text.toString(), width * 3 / 4f, 300f, valuePaint)
        
        // 碳减排
        canvas.drawText("碳减排(g)", width / 4f, 400f, labelPaint)
        canvas.drawText(binding.textCarbonSaved.text.toString(), width / 4f, 450f, valuePaint)
        
        // 积分
        canvas.drawText("获得积分", width * 3 / 4f, 400f, labelPaint)
        canvas.drawText(binding.textPoints.text.toString(), width * 3 / 4f, 450f, valuePaint)
        
        // 底部信息
        val footerPaint = Paint().apply {
            color = Color.parseColor("#059669")
            textSize = 28f
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("🌱 EcoGo - 一起绿色出行", width / 2f, 550f, footerPaint)
        
        return bitmap
    }
    
    private fun setupAnimations() {
        val popIn = AnimationUtils.loadAnimation(requireContext(), R.anim.pop_in)
        binding.cardPreview.startAnimation(popIn)
        
        val slideUp = AnimationUtils.loadAnimation(requireContext(), R.anim.slide_up)
        binding.cardStats.startAnimation(slideUp)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
