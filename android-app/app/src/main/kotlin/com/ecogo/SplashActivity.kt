package com.ecogo

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class SplashActivity : AppCompatActivity() {

    private var countdownTimer: CountDownTimer? = null
    private val TAG = "SplashActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize TokenManager here as well if needed (safeguard)
        com.ecogo.auth.TokenManager.init(applicationContext)

        // VIP Check
        val prefs = getSharedPreferences("EcoGoPrefs", Context.MODE_PRIVATE)
        val isVip = prefs.getBoolean("is_vip", false)
        // Use TokenManager to check login status
        val isLoggedIn = com.ecogo.auth.TokenManager.isLoggedIn()

        Log.d(TAG, "onCreate: isVip=$isVip, isLoggedIn=$isLoggedIn")

        if (isVip && isLoggedIn) {
            // VIP & Logged In -> Skip Ad & Login -> Go to Home
            Log.d(TAG, "User is VIP & Logged In. Skipping Ad.")
            proceedToMain(true)
            return
        }

        // Show Ad Layout
        setContentView(R.layout.activity_splash)

        // Setup Views
        val imgAd = findViewById<android.widget.ImageView>(R.id.imgAd)
        val btnSkip = findViewById<LinearLayout>(R.id.btnSkip)
        val tvSkipText = findViewById<TextView>(R.id.tvSkipText)

        // Skip Button Logic
        btnSkip.setOnClickListener {
            Log.d(TAG, "Skip button clicked")
            countdownTimer?.cancel()
            proceedToMain(isLoggedIn)
        }

        val adUrl = "http://47.129.124.55:8090/api/v1/advertisements/active"

        // Fetch Ad Data (Async)
        Thread {
            try {
                Log.d(TAG, "Fetching ads from $adUrl")
                val url = java.net.URL(adUrl)
                val connection = url.openConnection() as java.net.HttpURLConnection
                connection.requestMethod = "GET"
                connection.connectTimeout = 2000 // 2s timeout
                connection.readTimeout = 2000

                if (connection.responseCode == 200) {
                    val stream = connection.inputStream
                    val reader = java.io.InputStreamReader(stream)
                    val responseText = reader.readText()
                    reader.close()

                    // Parse Response (Using Gson since it's in dependencies)
                    val gson = com.google.gson.Gson()
                    val response = gson.fromJson(responseText, com.ecogo.data.AdResponse::class.java)

                    if (response.code == 200 && !response.data.isNullOrEmpty()) {
                        val randomAd = response.data.random()
                        val imageUrl = randomAd.imageUrl
                        Log.d(TAG, "Selected Ad: ${randomAd.name}, Image: $imageUrl")

                        runOnUiThread {
                            try {
                                com.bumptech.glide.Glide.with(this)
                                    .load(imageUrl)
                                    .centerCrop()
                                    .placeholder(R.drawable.bg_splash_ad_placeholder)
                                    .error(R.drawable.bg_splash_ad_placeholder)
                                    .into(imgAd)

                                // Optional: Handle Ad Click
                                imgAd.setOnClickListener {
                                    Log.d(TAG, "Ad clicked: ${randomAd.linkUrl}")
                                    // TODO: Open linkUrl in browser if needed
                                }
                            } catch (e: Exception) {
                                Log.e(TAG, "Glide Error: ${e.message}")
                            }
                        }
                    } else {
                        Log.w(TAG, "No active ads or error code: ${response.code}")
                    }
                } else {
                    Log.e(TAG, "HTTP Error: ${connection.responseCode}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Fetch Error: ${e.message}")
            }
        }.start()

        // Sync VIP status in background if logged in
        if (isLoggedIn) {
            val userId = com.ecogo.auth.TokenManager.getUserId()
            if (!userId.isNullOrEmpty()) {
                lifecycleScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                    try {
                        val apiService = com.ecogo.api.RetrofitClient.apiService
                        val response = apiService.getUserProfile(userId)
                        if (response.success && response.data != null) {
                            val userInfo = response.data
                            val serverIsVip = userInfo.vip?.active == true
                            
                            // Update local storage
                            val localPrefs = getSharedPreferences("EcoGoPrefs", Context.MODE_PRIVATE)
                            localPrefs.edit().putBoolean("is_vip", serverIsVip).apply()
                            
                            Log.d(TAG, "Synced VIP status from server: $serverIsVip")
                            
                            // If user is VIP, we can skip ad immediately if it's still showing
                            if (serverIsVip) {
                                runOnUiThread {
                                    Log.d(TAG, "User effectively VIP after sync. Skipping ad now.")
                                    countdownTimer?.cancel()
                                    proceedToMain(true)
                                }
                            }
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to sync user profile: ${e.message}")
                    }
                }
            }
        }

        // Start 3s Timer
        Log.d(TAG, "Starting 3s countdown")
        countdownTimer = object : CountDownTimer(3000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val secondsLeft = (millisUntilFinished / 1000) + 1
                tvSkipText.text = "跳过 $secondsLeft"
            }

            override fun onFinish() {
                Log.d(TAG, "Countdown finished")
                tvSkipText.text = "跳过 0"
                proceedToMain(isLoggedIn)
            }
        }.start()
    }

    private fun proceedToMain(shouldGoToHome: Boolean) {
        Log.d(TAG, "proceedToMain: shouldGoToHome=$shouldGoToHome")
        val intent = Intent(this, MainActivity::class.java)
        // Clear the task stack so user cannot go back to SplashActivity
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        
        if (shouldGoToHome) {
            intent.putExtra("NAV_TO_HOME", true)
        }
        startActivity(intent)
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        countdownTimer?.cancel()
    }
}