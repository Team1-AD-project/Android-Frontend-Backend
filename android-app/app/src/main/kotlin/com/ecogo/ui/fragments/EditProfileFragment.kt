package com.ecogo.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.ecogo.R
import com.ecogo.api.MobileProfileResponse

import com.ecogo.api.TransportPreferencesWrapper

import com.ecogo.api.UpdateProfileRequest
import com.ecogo.auth.TokenManager
import com.ecogo.databinding.FragmentEditProfileBinding
import com.ecogo.repository.EcoGoRepository
import kotlinx.coroutines.launch

class EditProfileFragment : Fragment() {

    private var _binding: FragmentEditProfileBinding? = null
    private val binding get() = _binding!!
    private val repository = EcoGoRepository()

    // Cached profile data for comparison
    private var cachedProfile: MobileProfileResponse? = null

    companion object {
        private const val TAG = "EditProfileFragment"

        // NUS Faculty list
        val FACULTIES = listOf(
            "School of Computing",
            "Faculty of Engineering",
            "Faculty of Science",
            "Faculty of Arts and Social Sciences",
            "Business School",
            "Faculty of Law",
            "Faculty of Dentistry",
            "Yong Loo Lin School of Medicine",
            "School of Design and Environment",
            "Yong Siew Toh Conservatory of Music",
            "Faculty of Nursing",
            "School of Public Health",
            "School of Continuing and Lifelong Education",
            "College of Humanities and Sciences"
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupFacultyDropdown()
        setupActions()
        loadProfile()
    }

    private fun setupFacultyDropdown() {
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            FACULTIES
        )
        binding.editFaculty.setAdapter(adapter)
    }

    private fun setupActions() {
        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.btnSave.setOnClickListener {
            saveProfile()
        }

        binding.btnSaveBottom.setOnClickListener {
            saveProfile()
        }
    }

    private fun loadProfile() {
        val userId = com.ecogo.auth.TokenManager.getUserId() ?: return
        viewLifecycleOwner.lifecycleScope.launch {
            try {

                val result = repository.getMobileUserProfile()

                val profile = result.getOrNull()

                if (profile != null) {
                    cachedProfile = profile
                    populateFields(profile)
                    Log.d(TAG, "Profile loaded: ${profile.userInfo.nickname}")
                } else {
                    Log.w(TAG, "Failed to load profile, using defaults")
                    Toast.makeText(context, "Could not load profile data", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading profile: ${e.message}", e)
                Toast.makeText(context, "Error loading profile", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun populateFields(profile: MobileProfileResponse) {
        val userInfo = profile.userInfo
        val prefs = userInfo.preferences

        // Basic info
        binding.editNickname.setText(userInfo.nickname)
        binding.editFaculty.setText(userInfo.faculty ?: "", false)

        // Campus info (read from preferences - these fields may come from extended profile)
        // Note: The API stores these as part of the profile, we load them if available
        // For now we populate from SharedPreferences as a fallback
        loadLocalPreferences()
        // Populate fields from API preferences (fallback to local if null?)
        // The API now returns these fields in `preferences` object
        binding.editDormitory.setText(prefs?.dormitoryOrResidence ?: "")
        binding.editTeachingBuilding.setText(prefs?.mainTeachingBuilding ?: "")
        binding.editStudySpot.setText(prefs?.favoriteStudySpot ?: "")
        
        // Weekly goal from API or default
        val goal = prefs?.weeklyGoals ?: 10000
        binding.editWeeklyGoals.setText(goal.toString())


        // Transport preferences
        prefs?.preferredTransport?.let { transports ->
            binding.chipBus.isChecked = transports.contains("bus")
            binding.chipMrt.isChecked = transports.contains("mrt")
            binding.chipBicycle.isChecked = transports.contains("bicycle")
            binding.chipWalk.isChecked = transports.contains("walking") // API returns 'walking', chip might need mapping
        }

        // Notification preferences
        binding.switchNewChallenges.isChecked = prefs?.newChallenges ?: true
        binding.switchActivityReminders.isChecked = prefs?.activityReminders ?: true
        binding.switchFriendActivity.isChecked = prefs?.friendActivity ?: false

        // Weekly goals (保留本地兜底逻辑，API无数据时用本地值)
        val weeklyGoals = prefs?.weeklyGoals ?: getLocalPrefInt("weeklyGoals", 10000)
        binding.editWeeklyGoals.setText(weeklyGoals.toString())
    }

    // 保留本地偏好加载方法（API无数据时兜底，不直接删除）
    private fun loadLocalPreferences() {
        val prefs = requireContext().getSharedPreferences("ecogo_profile", android.content.Context.MODE_PRIVATE)
        binding.editDormitory.setText(prefs.getString("dormitoryOrResidence", "") ?: "")
        binding.editTeachingBuilding.setText(prefs.getString("mainTeachingBuilding", "") ?: "")
        binding.editStudySpot.setText(prefs.getString("favoriteStudySpot", "") ?: "")
    }

    // 保留本地偏好获取方法（兜底用）
    private fun getLocalPref(key: String, default: Boolean): Boolean {
        val prefs = requireContext().getSharedPreferences("ecogo_profile", android.content.Context.MODE_PRIVATE)
        return prefs.getBoolean(key, default)
    }

    // 保留本地int偏好获取方法（weeklyGoals兜底用）
    private fun getLocalPrefInt(key: String, default: Int): Int {
        val prefs = requireContext().getSharedPreferences("ecogo_profile", android.content.Context.MODE_PRIVATE)
        return prefs.getInt(key, default)
    }

    private fun saveProfile() {
        val userId = TokenManager.getUserId()
        if (userId == null) {
            Toast.makeText(context, "Not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        // Gather data from fields
        val nickname = binding.editNickname.text?.toString()?.trim() ?: ""
        val faculty = binding.editFaculty.text?.toString()?.trim()
        val dormitory = binding.editDormitory.text?.toString()?.trim()
        val teachingBuilding = binding.editTeachingBuilding.text?.toString()?.trim()
        val studySpot = binding.editStudySpot.text?.toString()?.trim()
        val weeklyGoalsText = binding.editWeeklyGoals.text?.toString()?.trim()
        val weeklyGoals = weeklyGoalsText?.toIntOrNull() ?: 10000

        // Validate nickname
        if (nickname.isEmpty()) {
            binding.layoutNickname.error = getString(R.string.edit_profile_error_nickname)
            return
        } else {
            binding.layoutNickname.error = null
        }

        // Gather transport preferences
        val transports = mutableListOf<String>()
        if (binding.chipBus.isChecked) transports.add("bus")
        if (binding.chipMrt.isChecked) transports.add("mrt")
        if (binding.chipBicycle.isChecked) transports.add("bicycle")

        if (binding.chipWalk.isChecked) transports.add("walking") // Use "walking" to match API


        val newChallenges = binding.switchNewChallenges.isChecked
        val activityReminders = binding.switchActivityReminders.isChecked
        val friendActivity = binding.switchFriendActivity.isChecked

        // Build request using new wrapper
        val request = UpdateProfileRequest(
            nickname = nickname,
            faculty = faculty?.ifEmpty { null },
            preferences = com.ecogo.api.UpdatePreferencesWrapper(
                preferredTransport = if (transports.isNotEmpty()) transports else null,
                dormitoryOrResidence = dormitory?.ifEmpty { null },
                mainTeachingBuilding = teachingBuilding?.ifEmpty { null },
                favoriteStudySpot = studySpot?.ifEmpty { null },
                weeklyGoals = weeklyGoals,
                newChallenges = newChallenges,
                activityReminders = activityReminders,
                friendActivity = friendActivity
            )

        )

        // Disable save buttons during request
        binding.btnSave.isEnabled = false
        binding.btnSaveBottom.isEnabled = false
        binding.btnSaveBottom.text = getString(R.string.edit_profile_saving)

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val result = repository.updateUserProfile(userId, request)

                if (result.isSuccess) {
                    // Save to local SharedPreferences for immediate access
                    saveLocalPreferences(
                        dormitory = dormitory ?: "",
                        teachingBuilding = teachingBuilding ?: "",
                        studySpot = studySpot ?: "",
                        weeklyGoals = weeklyGoals,
                        newChallenges = newChallenges,
                        activityReminders = activityReminders,
                        friendActivity = friendActivity
                    )

                    Log.d(TAG, "Profile updated successfully")
                    Toast.makeText(context, getString(R.string.edit_profile_save_success), Toast.LENGTH_SHORT).show()
                    findNavController().navigateUp()
                } else {
                    val error = result.exceptionOrNull()?.message ?: "Unknown error"
                    Log.e(TAG, "Failed to update profile: $error")
                    Toast.makeText(context, getString(R.string.edit_profile_save_error), Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error saving profile: ${e.message}", e)
                Toast.makeText(context, getString(R.string.edit_profile_save_error), Toast.LENGTH_SHORT).show()
            } finally {
                binding.btnSave.isEnabled = true
                binding.btnSaveBottom.isEnabled = true
                binding.btnSaveBottom.text = getString(R.string.edit_profile_save_changes)
            }
        }
    }

    private fun saveLocalPreferences(
        dormitory: String,
        teachingBuilding: String,
        studySpot: String,
        weeklyGoals: Int,
        newChallenges: Boolean,
        activityReminders: Boolean,
        friendActivity: Boolean
    ) {
        requireContext().getSharedPreferences("ecogo_profile", android.content.Context.MODE_PRIVATE)
            .edit()
            .putString("dormitoryOrResidence", dormitory)
            .putString("mainTeachingBuilding", teachingBuilding)
            .putString("favoriteStudySpot", studySpot)
            .putInt("weeklyGoals", weeklyGoals)
            .putBoolean("newChallenges", newChallenges)
            .putBoolean("activityReminders", activityReminders)
            .putBoolean("friendActivity", friendActivity)
            .apply()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
