package com.ecogo

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.ecogo.databinding.ActivityMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 设置全局异常处理器
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            Log.e("CRASH_HANDLER", "Uncaught exception in thread ${thread.name}: ${throwable.message}", throwable)
            android.os.Handler(mainLooper).post {
                Toast.makeText(this, "应用崩溃: ${throwable.message}", Toast.LENGTH_LONG).show()
            }
            // 给Toast一些时间显示
            Thread.sleep(2000)
            // 让默认处理器处理（这会关闭应用）
            android.os.Process.killProcess(android.os.Process.myPid())
            System.exit(1)
        }
        
        Log.d("DEBUG_MAIN", "MainActivity onCreate started")
        try {
            binding = ActivityMainBinding.inflate(layoutInflater)
            setContentView(binding.root)
            
            setupNavigation()
            setupVersionToggle()
            checkAndShowOnboarding()
            Log.d("DEBUG_MAIN", "MainActivity onCreate completed")
        } catch (e: Exception) {
            Log.e("DEBUG_MAIN", "MainActivity onCreate FAILED: ${e.message}", e)
            Toast.makeText(this, "MainActivity 初始化失败: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
    
    private fun setupNavigation() {
        Log.d("DEBUG_MAIN", "setupNavigation started")
        try {
            val navHostFragment = supportFragmentManager
                .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
            navController = navHostFragment.navController
            Log.d("DEBUG_MAIN", "NavController initialized: ${navController.currentDestination?.label}")
            
            // Setup Bottom Navigation (will be shown after login)
            val bottomNav = binding.bottomNavigation
            bottomNav.setupWithNavController(navController)
            Log.d("DEBUG_MAIN", "Bottom navigation setup completed")
            
            // Hide/Show bottom nav based on destination
            navController.addOnDestinationChangedListener { _, destination, _ ->
                Log.d("DEBUG_MAIN", "Navigation destination changed: ${destination.label} (id=${destination.id})")
                try {
                    when (destination.id) {
                        R.id.loginFragment, 
                        R.id.signupWizardFragment,
                        R.id.onboardingFragment -> {
                            bottomNav.visibility = android.view.View.GONE
                        }
                        else -> {
                            bottomNav.visibility = android.view.View.VISIBLE
                        }
                    }
                } catch (e: Exception) {
                    Log.e("DEBUG_MAIN", "Error in destination changed listener: ${e.message}", e)
                }
            }
        } catch (e: Exception) {
            Log.e("DEBUG_MAIN", "setupNavigation FAILED: ${e.message}", e)
            Toast.makeText(this, "导航设置失败: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
    
    /**
     * Developer option: Long-press bottom navigation to toggle between Home V1 and V2
     */
    private fun setupVersionToggle() {
        binding.bottomNavigation.setOnLongClickListener {
            toggleHomeVersion()
            true
        }
    }
    
    private fun checkAndShowOnboarding() {
        try {
            val prefs = getSharedPreferences("EcoGoPrefs", Context.MODE_PRIVATE)
            val isFirstLogin = prefs.getBoolean("is_first_login", false)
            
            if (isFirstLogin) {
                Log.d("DEBUG_MAIN", "First login detected, will show onboarding after home loads")
                // 注意：这里不直接导航，因为还在登录流程中
                // 当用户完成注册并到达home时，可以在HomeFragment中检测并导航到onboarding
                // 或者在navigation listener中处理
                
                navController.addOnDestinationChangedListener { _, destination, _ ->
                    if (destination.id == R.id.homeFragment && isFirstLogin) {
                        // 到达首页后显示引导
                        prefs.edit().putBoolean("is_first_login", false).apply()
                        Log.d("DEBUG_MAIN", "Navigating to onboarding from home")
                        try {
                            navController.navigate(R.id.onboardingFragment)
                        } catch (e: Exception) {
                            Log.e("DEBUG_MAIN", "Failed to navigate to onboarding: ${e.message}", e)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("DEBUG_MAIN", "checkAndShowOnboarding failed: ${e.message}", e)
        }
    }
    
    private fun toggleHomeVersion() {
        val prefs = getSharedPreferences("ecogo_prefs", MODE_PRIVATE)
        val useV2 = prefs.getBoolean("use_home_v2", false)
        prefs.edit().putBoolean("use_home_v2", !useV2).apply()
        
        val message = if (!useV2) {
            "Switched to Home V2 (with Banner) ✨\nRestart to see changes"
        } else {
            "Switched to Home V1 (original) 📱\nRestart to see changes"
        }
        
        android.widget.Toast.makeText(this, message, android.widget.Toast.LENGTH_LONG).show()
        
        // Optional: Restart activity to apply changes immediately
        // recreate()
    }
    
    /**
     * Get the current home fragment destination based on user preference
     */
    fun getHomeDestination(): Int {
        val prefs = getSharedPreferences("ecogo_prefs", MODE_PRIVATE)
        val useV2 = prefs.getBoolean("use_home_v2", false)
        return if (useV2) R.id.homeFragmentV2 else R.id.homeFragment
    }
}
