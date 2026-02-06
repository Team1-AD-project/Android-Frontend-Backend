package com.ecogo.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.ecogo.R
import com.ecogo.databinding.FragmentCheckInCalendarBinding
import com.ecogo.repository.EcoGoRepository
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.*

class CheckInCalendarFragment : Fragment() {
    
    private var _binding: FragmentCheckInCalendarBinding? = null
    private val binding get() = _binding!!
    private val repository = EcoGoRepository()
    
    private var currentMonth = YearMonth.now()
    private val checkedInDates = mutableSetOf<LocalDate>()
    private var selectedDate: LocalDate? = null
    private var consecutiveDays = 0
    private var totalCheckIns = 0
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCheckInCalendarBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupToolbar()
        setupCalendar()
        setupActions()
        loadCheckInData()
    }
    
    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }
    
    private fun setupCalendar() {
        updateMonthDisplay()
        buildCalendar()
        
        binding.buttonPreviousMonth.setOnClickListener {
            currentMonth = currentMonth.minusMonths(1)
            updateMonthDisplay()
            buildCalendar()
        }
        
        binding.buttonNextMonth.setOnClickListener {
            currentMonth = currentMonth.plusMonths(1)
            updateMonthDisplay()
            buildCalendar()
        }
    }
    
    private fun setupActions() {
        binding.buttonCheckIn.setOnClickListener {
            performCheckIn()
        }
    }
    
    private fun updateMonthDisplay() {
        val monthName = currentMonth.month.getDisplayName(TextStyle.FULL, Locale.ENGLISH)
        binding.textMonthYear.text = "$monthName ${currentMonth.year}"
    }
    
    private fun buildCalendar() {
        binding.calendarGrid.removeAllViews()
        
        // Add week header row
        val weekHeaderRow = android.widget.LinearLayout(requireContext()).apply {
            orientation = android.widget.LinearLayout.HORIZONTAL
            layoutParams = android.widget.LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }
        
        val weekDays = arrayOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
        weekDays.forEach { day ->
            val dayView = TextView(requireContext()).apply {
                text = day
                textSize = 14f
                gravity = android.view.Gravity.CENTER
                setTextColor(ContextCompat.getColor(context, R.color.text_secondary))
                layoutParams = android.widget.LinearLayout.LayoutParams(
                    0,
                    80,
                    1f
                )
            }
            weekHeaderRow.addView(dayView)
        }
        binding.calendarGrid.addView(weekHeaderRow)
        
        // 获取当月第一天
        val firstDayOfMonth = currentMonth.atDay(1)
        val dayOfWeek = firstDayOfMonth.dayOfWeek.value // 1 = Monday, 7 = Sunday
        
        var currentRow: android.widget.LinearLayout? = null
        var dayInWeek = 0
        
        // Add empty placeholders
        repeat(dayOfWeek - 1) {
            if (dayInWeek == 0) {
                currentRow = createWeekRow()
                binding.calendarGrid.addView(currentRow)
            }
            val emptyView = View(requireContext()).apply {
                layoutParams = android.widget.LinearLayout.LayoutParams(0, 100, 1f)
            }
            currentRow?.addView(emptyView)
            dayInWeek++
        }
        
        // Add dates
        val daysInMonth = currentMonth.lengthOfMonth()
        for (day in 1..daysInMonth) {
            if (dayInWeek == 0) {
                currentRow = createWeekRow()
                binding.calendarGrid.addView(currentRow)
            }
            
            val date = currentMonth.atDay(day)
            val dayView = createDayView(date)
            currentRow?.addView(dayView)
            
            dayInWeek++
            if (dayInWeek == 7) {
                dayInWeek = 0
            }
        }
        
        // Fill remaining empty cells
        if (dayInWeek > 0) {
            repeat(7 - dayInWeek) {
                val emptyView = View(requireContext()).apply {
                    layoutParams = android.widget.LinearLayout.LayoutParams(0, 100, 1f)
                }
                currentRow?.addView(emptyView)
            }
        }
    }
    
    private fun createWeekRow(): android.widget.LinearLayout {
        return android.widget.LinearLayout(requireContext()).apply {
            orientation = android.widget.LinearLayout.HORIZONTAL
            layoutParams = android.widget.LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }
    }
    
    private fun createDayView(date: LocalDate): View {
        val inflater = LayoutInflater.from(requireContext())
        val dayView = inflater.inflate(R.layout.item_calendar_day, binding.calendarGrid, false)
        
        val textDay = dayView.findViewById<TextView>(R.id.text_day)
        val iconCheck = dayView.findViewById<View>(R.id.icon_check)
        val viewSelected = dayView.findViewById<View>(R.id.view_selected)
        
        textDay.text = date.dayOfMonth.toString()
        
        // 今天高亮
        val today = LocalDate.now()
        if (date == today) {
            textDay.setTextColor(ContextCompat.getColor(requireContext(), R.color.primary))
            textDay.setTypeface(null, android.graphics.Typeface.BOLD)
        }
        
        // Checked-in dates
        if (checkedInDates.contains(date)) {
            iconCheck.visibility = View.VISIBLE
            textDay.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_white))
            dayView.setBackgroundResource(R.drawable.bg_calendar_checked)
        }
        
        // Selected date
        if (date == selectedDate) {
            viewSelected.visibility = View.VISIBLE
        }
        
        // Disable future dates
        if (date.isAfter(today)) {
            textDay.alpha = 0.3f
            dayView.isEnabled = false
        } else {
            dayView.setOnClickListener {
                selectedDate = date
                buildCalendar()
                updateSelectedDateInfo(date)
            }
        }
        
        dayView.layoutParams = android.widget.LinearLayout.LayoutParams(0, 100, 1f).apply {
            setMargins(4, 4, 4, 4)
        }
        
        return dayView
    }
    
    private fun updateSelectedDateInfo(date: LocalDate) {
        val isCheckedIn = checkedInDates.contains(date)
        if (isCheckedIn) {
            binding.layoutDateInfo.visibility = View.VISIBLE
            binding.textSelectedDate.text = "${date.month.getDisplayName(TextStyle.FULL, Locale.ENGLISH)} ${date.dayOfMonth}"
            binding.textCheckInInfo.text = "Checked In · Earned 10 points"
            binding.buttonCheckIn.visibility = View.GONE
        } else {
            binding.layoutDateInfo.visibility = View.GONE
        }
    }
    
    private fun loadCheckInData() {
        viewLifecycleOwner.lifecycleScope.launch {
            // 加载签到历史
            val history = repository.getCheckInHistory("user123").getOrNull()
            if (history != null) {
                checkedInDates.clear()
                for (record in history) {
                    try {
                        val date = LocalDate.parse(record.checkInDate)
                        checkedInDates.add(date)
                    } catch (e: Exception) {
                        // Ignore parse errors
                    }
                }
                buildCalendar()
            }
            
            // 加载签到状态
            val status = repository.getCheckInStatus("user123").getOrNull()
            if (status != null) {
                consecutiveDays = status.consecutiveDays
                totalCheckIns = checkedInDates.size
                updateStats()
                
                // Check if already checked in today
                val today = LocalDate.now()
                val isCheckedInToday = status.lastCheckInDate == today.toString()
                
                if (isCheckedInToday) {
                    binding.buttonCheckIn.text = "今日已签到"
                    binding.buttonCheckIn.isEnabled = false
                    binding.buttonCheckIn.alpha = 0.6f
                } else {
                    binding.buttonCheckIn.text = "立即签到"
                    binding.buttonCheckIn.isEnabled = true
                    binding.buttonCheckIn.alpha = 1f
                }
            }
        }
    }
    
    private fun updateStats() {
        binding.textConsecutiveDays.text = "$consecutiveDays"
        binding.textTotalCheckIns.text = "$totalCheckIns"
        
        // Calculate this month's check-ins
        val thisMonthCheckIns = checkedInDates.count { 
            YearMonth.from(it) == currentMonth 
        }
        binding.textMonthCheckIns.text = "$thisMonthCheckIns"
    }
    
    private fun performCheckIn() {
        viewLifecycleOwner.lifecycleScope.launch {
            val response = repository.checkIn("user123").getOrNull()
            if (response != null && response.success) {
                // 添加今天到已签到日期
                val today = LocalDate.now()
                checkedInDates.add(today)
                
                // Update statistics
                consecutiveDays = response.consecutiveDays
                totalCheckIns = checkedInDates.size
                updateStats()
                
                // 刷新日历
                buildCalendar()
                
                // Show success animation
                showCheckInSuccess(response.pointsEarned)
                
                // Disable button
                binding.buttonCheckIn.text = "Checked In Today"
                binding.buttonCheckIn.isEnabled = false
                binding.buttonCheckIn.alpha = 0.6f
            }
        }
    }
    
    private fun showCheckInSuccess(points: Int) {
        binding.layoutSuccess.visibility = View.VISIBLE
        binding.textSuccessMessage.text = "Check-in successful! Earned $points points"
        
        val popIn = AnimationUtils.loadAnimation(requireContext(), R.anim.pop_in)
        binding.layoutSuccess.startAnimation(popIn)
        
        // Auto hide after 3 seconds
        binding.layoutSuccess.postDelayed({
            binding.layoutSuccess.visibility = View.GONE
        }, 3000)
        
        binding.buttonCloseSuccess.setOnClickListener {
            binding.layoutSuccess.visibility = View.GONE
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
