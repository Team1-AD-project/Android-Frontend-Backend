package com.ecogo.utils

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.ecogo.R
import android.content.Intent
import android.app.PendingIntent
import com.ecogo.SplashActivity



object NotificationUtil {

    private const val CHANNEL_ID = "churn_risk_channel"

    fun showChurnNotification(context: Context, riskLevel: String?) {
        createChannelIfNeeded(context)

        // Android 13+ permission check
        if (Build.VERSION.SDK_INT >= 33) {
            val granted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) return
        }

        val (title, message) = churnNotificationContent(riskLevel)

        val destination = when (riskLevel?.uppercase()) {
            "HIGH" -> "voucher"
            "MEDIUM" -> "challenges"
            else -> "home"
        }

        val intent = Intent(context, SplashActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("notification_destination", destination)
        }



        val pendingIntent = PendingIntent.getActivity(
            context,
            2001,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )


        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher) // ✅ 不用 ic_notification，避免资源不存在
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        NotificationManagerCompat.from(context).notify(1001, notification)
    }

    private fun churnNotificationContent(riskLevel: String?): Pair<String, String> {
        return when (riskLevel?.uppercase()) {
            "HIGH" -> "EcoGo · We Miss You 😢" to "You’ve been less active recently. Check out new challenges and rewards!"
            "MEDIUM" -> "EcoGo · Stay Green 🌱" to "Complete a challenge today and earn extra Eco Points."
            "LOW" -> "EcoGo · Great Job 🎉" to "You’re doing great! Keep choosing green travel."
            else -> "EcoGo · Welcome Back" to "Explore EcoGo and continue your green journey."
        }
    }

    private fun createChannelIfNeeded(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Churn Risk Reminder",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for user churn risk reminders"
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }
}
