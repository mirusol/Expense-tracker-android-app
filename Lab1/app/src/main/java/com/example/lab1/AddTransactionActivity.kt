package com.example.lab1

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.lab1.database.DatabaseHelper
import android.content.SharedPreferences
import android.os.Build
import androidx.appcompat.app.AlertDialog
import android.util.Log

class AddTransactionActivity : AppCompatActivity() {

    private lateinit var etDescription: EditText
    private lateinit var etAmount: EditText
    private lateinit var spinnerCategory: Spinner
    private lateinit var btnAddTransaction: Button
    private lateinit var databaseHelper: DatabaseHelper
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_transaction)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 101)
        }

        etDescription = findViewById(R.id.et_transaction_description)
        etAmount = findViewById(R.id.et_transaction_amount)
        spinnerCategory = findViewById(R.id.spinner_category)
        btnAddTransaction = findViewById(R.id.btn_add_transaction)

        val homeButton: Button = findViewById(R.id.btn_home)
        homeButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish()
        }

        sharedPreferences = getSharedPreferences("BudgetPrefs", MODE_PRIVATE)

        databaseHelper = DatabaseHelper(this)

        val categories =
            arrayOf("Rent", "Shopping", "Groceries", "Tech", "Online Purchases", "Entertainment")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCategory.adapter = adapter

        btnAddTransaction.setOnClickListener {
            val description = etDescription.text.toString()
            val amount = etAmount.text.toString().toDoubleOrNull()
            val category = spinnerCategory.selectedItem.toString()

            if (description.isBlank() || amount == null) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            checkBudgetAndAddTransaction(description, amount, category)
        }
    }

    private fun checkBudgetAndAddTransaction(description: String, amount: Double, category: String) {
        val budget = sharedPreferences.getFloat("budget", 0f)

        if (amount > budget) {
            AlertDialog.Builder(this)
                .setTitle("Budget Exceeded")
                .setMessage("The transaction amount exceeds your set budget. Do you want to continue?")
                .setPositiveButton("Yes") { dialog, _ ->
                    addTransaction(description, amount, category)
                    Toast.makeText(this, "Transaction added. Budget exceeded!", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                }
                .setNegativeButton("No") { dialog, _ ->
                    Toast.makeText(this, "Transaction canceled. Please update your budget.", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                }
                .show()
        } else {
            addTransaction(description, amount, category)
        }
    }
    private fun checkAndNotifyBudgetUsage(amount: Double) {
        val sharedPreferences = getSharedPreferences("BudgetPrefs", MODE_PRIVATE)
        val budget = sharedPreferences.getFloat("budget", 0f).toDouble()

        Log.d("BUDGET_CHECK", "Current budget: $budget")

        val cursor = databaseHelper.getAllTransactions()
        var totalSpent = 0.0
        if (cursor.moveToFirst()) {
            do {
                totalSpent += cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_AMOUNT))
            } while (cursor.moveToNext())
        }
        cursor.close()

        totalSpent += amount
        Log.d("BUDGET_CHECK", "Total spent: $totalSpent")

        val percentageUsed = (totalSpent / budget) * 100
        Log.d("BUDGET_CHECK", "Percentage used: $percentageUsed%")

        when {
            percentageUsed >= 90 -> {
                Log.d("BUDGET_NOTIFICATION", "Triggering 90% notification")
                sendNotification("Budget Alert", "You have used 90% or more of your budget.")
            }
            percentageUsed >= 75 -> {
                Log.d("BUDGET_NOTIFICATION", "Triggering 75% notification")
                sendNotification("Budget Alert", "You have used 75% of your budget.")
            }
            percentageUsed >= 50 -> {
                Log.d("BUDGET_NOTIFICATION", "Triggering 50% notification")
                sendNotification("Budget Alert", "You have used 50% of your budget.")
            }
        }
    }

    private fun sendNotification(title: String, message: String) {
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as android.app.NotificationManager

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = android.app.NotificationChannel(
                "BUDGET_ALERT_CHANNEL",
                "Budget Alerts",
                android.app.NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        val notification = androidx.core.app.NotificationCompat.Builder(this, "BUDGET_ALERT_CHANNEL")
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(androidx.core.app.NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        notificationManager.notify((1..1000000).random(), notification)
    }
    private fun addTransaction(description: String, amount: Double, category: String) {
        val rowId = databaseHelper.addTransaction(amount, description, System.currentTimeMillis(), category)
        if (rowId > -1) {
            Toast.makeText(this, "Transaction added successfully", Toast.LENGTH_SHORT).show()

            checkAndNotifyBudgetUsage(amount)

            finish()
        } else {
            Toast.makeText(this, "Failed to add transaction", Toast.LENGTH_SHORT).show()
        }
    }

}

