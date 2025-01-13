package com.example.lab1

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.*
import com.example.lab1.database.DatabaseHelper
import android.util.Log

class SavingsProgressService : Service() {

    private val channelId = "SAVINGS_PROGRESS_CHANNEL"
    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    private var job: Job? = null

    override fun onCreate() {
        super.onCreate()
        Log.d("SavingsService", "Service Created")
        createNotificationChannel()
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = createNotification("Monitoring your savings progress...")
        startForeground(1, notification)
        startMonitoringSavings()
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("SavingsService", "Service Destroyed")
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun startMonitoringSavings() {
        job = CoroutineScope(Dispatchers.IO).launch {
            while (isActive) {
                Log.d("SavingsService", "Monitoring savings progress...")
                val progressMessage = calculateSavingsProgress()
                Log.d("SavingsService", "Progress: $progressMessage")
                updateNotification(progressMessage)
                delay(60000) // Update every 60 seconds
            }
        }
    }

    private fun createNotification(message: String): Notification {
        return NotificationCompat.Builder(this, channelId)
            .setSmallIcon(android.R.drawable.ic_menu_save)
            .setContentTitle("Savings Progress")
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setAutoCancel(false)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "savings_progress_channel",
                "Savings Progress Notifications",
                NotificationManager.IMPORTANCE_HIGH
            )
            channel.description = "Notifications for savings progress updates."
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }


    private suspend fun calculateSavingsProgress(): String {
        val sharedPreferences = getSharedPreferences("BudgetPrefs", MODE_PRIVATE)
        val budget = sharedPreferences.getFloat("budget", 0f).toDouble()
        val savingsGoal = sharedPreferences.getFloat("savings_goal", 0f).toDouble()

        val dbHelper = DatabaseHelper(this@SavingsProgressService)
        val cursor = dbHelper.getAllTransactions()
        var totalSpent = 0.0
        if (cursor.moveToFirst()) {
            do {
                totalSpent += cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_AMOUNT))
            } while (cursor.moveToNext())
        }
        cursor.close()

        val remainingBudget = budget - totalSpent
        val progress = if (savingsGoal > 0) (remainingBudget / savingsGoal) * 100 else 0.0

        return if (progress >= 100) {
            "Congratulations! You have reached your savings goal!"
        } else {
            "You have saved $${"%.2f".format(remainingBudget)} towards your goal of $${"%.2f".format(savingsGoal)} (${"%.1f".format(progress)}%)."
        }
    }

    private fun updateNotification(message: String) {
        val notification = NotificationCompat.Builder(this, "savings_progress_channel")
            .setContentTitle("Savings Progress Update")
            .setContentText(message)
            .setSmallIcon(R.drawable.ic_savings)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(1, notification)
    }
}