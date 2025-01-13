package com.example.lab1

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.util.Log
import android.widget.TextView

import kotlinx.coroutines.*
import com.example.lab1.api.StockResponse
import com.example.lab1.api.RetrofitClient

import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit


import kotlinx.coroutines.delay
import retrofit2.HttpException
import com.example.lab1.workers.TransactionBackupWorker



import com.example.lab1.services.ThemeService
import android.content.ComponentName
import android.content.ServiceConnection
import android.os.IBinder
import androidx.appcompat.app.AppCompatDelegate

import android.content.IntentFilter
import com.example.lab1.database.DatabaseHelper
import com.example.lab1.provider.RecentTransactionsProvider
import com.example.lab1.receiver.AirplaneModeReceiver


class MainActivity : AppCompatActivity() {

    private lateinit var btnAddTransaction: Button
    private lateinit var btnViewTransactions: Button
    private lateinit var btnSetBudget: Button
    private lateinit var btnViewBudgetStatus: Button
    private lateinit var btnEditTransaction: Button
    private lateinit var btnFilterTransactions: Button
    private lateinit var btn_start_savings_service: Button
    private lateinit var btn_stop_savings_service: Button

    private lateinit var btnShareTransactions: Button

    private lateinit var btnToggleTheme: Button
    private lateinit var themeService: ThemeService
    private var isBound = false

    private lateinit var airplaneModeReceiver: AirplaneModeReceiver
    private var isReceiverRegistered = false


    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as ThemeService.ThemeBinder
            themeService = binder.getService()
            isBound = true
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            isBound = false
        }
    }


    private lateinit var stockMarketValues: TextView
    private val apiKey = "###########"///enter your API key here
    private val symbols = listOf("AAPL", "GOOG", "AMZN", "TSLA")
    private val stockDataList = mutableListOf<String>()
    private var job: Job? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        scheduleBackup()
        registerAirplaneModeReceiver()
        btnShareTransactions = findViewById(R.id.btn_share_transactions)

        btnShareTransactions.setOnClickListener {
            shareRecentTransactions()
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 101)
        }

        stockMarketValues = findViewById(R.id.stock_market_values)
        startStockMarketUpdates()

        btnAddTransaction = findViewById(R.id.btn_add_transaction)
        btnViewTransactions = findViewById(R.id.btn_view_transactions)
        btnSetBudget = findViewById(R.id.btn_set_budget)
        btnViewBudgetStatus = findViewById(R.id.btn_view_budget_status)
        btnFilterTransactions = findViewById(R.id.btn_filter_transactions)


        btnAddTransaction.setOnClickListener {
            val intent = Intent(this, AddTransactionActivity::class.java)
            startActivity(intent)
        }


        btnViewTransactions.setOnClickListener {
            val intent = Intent(this, ViewTransactionsActivity::class.java)
            startActivity(intent)
        }


        btnSetBudget.setOnClickListener {
            val intent = Intent(this, SetBudgetActivity::class.java)
            startActivity(intent)
        }

        btnToggleTheme = findViewById(R.id.btn_toggle_theme)
        btnToggleTheme.setOnClickListener {
            if (isBound) {
                val isDarkTheme = themeService.toggleTheme()
                applyTheme(isDarkTheme)
            } else {
                Toast.makeText(this, "Theme Service is not bound", Toast.LENGTH_SHORT).show()
            }
        }

        val sharedPreferences = getSharedPreferences("BudgetPrefs", Context.MODE_PRIVATE)
        val savedBudget = sharedPreferences.getFloat("budget", 0f)
        Toast.makeText(this, "Saved Budget: $$savedBudget", Toast.LENGTH_SHORT).show()

        val btnViewBudgetStatus: Button = findViewById(R.id.btn_view_budget_status)
        btnViewBudgetStatus.setOnClickListener {
            val intent = Intent(this, ViewBudgetStatusActivity::class.java)
            startActivity(intent)
        }

        val filterButton: Button = findViewById(R.id.btn_filter_transactions)
        filterButton.setOnClickListener {
            val intent = Intent(this, FilterTransactionsActivity::class.java)
            startActivity(intent)
        }

        val btnStartSavingsService: Button = findViewById(R.id.btn_start_savings_service)
        val btnStopSavingsService: Button = findViewById(R.id.btn_stop_savings_service)

        btnStartSavingsService.setOnClickListener {
            startSavingsService()
        }

        btnStopSavingsService.setOnClickListener {
            stopSavingsService()
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val backupButton: Button = findViewById(R.id.btn_backup)
        backupButton.setOnClickListener {
            triggerBackupManually()
            Toast.makeText(this, "Backup triggered manually!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun shareRecentTransactions() {
        val uri = RecentTransactionsProvider.CONTENT_URI
        val cursor = contentResolver.query(uri, null, null, null, null)

        cursor?.let {
            val transactions = mutableListOf<String>()
            while (it.moveToNext()) {
                val description = it.getString(it.getColumnIndexOrThrow(DatabaseHelper.COLUMN_DESCRIPTION))
                val amount = it.getDouble(it.getColumnIndexOrThrow(DatabaseHelper.COLUMN_AMOUNT))
                transactions.add("Description: $description, Amount: $$amount")
            }
            it.close()

            if (transactions.isNotEmpty()) {
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, "Recent Transactions:\n" + transactions.joinToString("\n"))
                }

                startActivity(Intent.createChooser(shareIntent, "Share via"))
            } else {
                Toast.makeText(this, "No transactions to share", Toast.LENGTH_SHORT).show()
            }
        } ?: Toast.makeText(this, "Failed to retrieve transactions", Toast.LENGTH_SHORT).show()
    }
    override fun onStart() {
        super.onStart()
        val intent = Intent(this, ThemeService::class.java)
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterAirplaneModeReceiver()
    }

    private fun registerAirplaneModeReceiver() {
        airplaneModeReceiver = AirplaneModeReceiver()
        val intentFilter = IntentFilter(Intent.ACTION_AIRPLANE_MODE_CHANGED)
        registerReceiver(airplaneModeReceiver, intentFilter)
        isReceiverRegistered = true
    }

    private fun unregisterAirplaneModeReceiver() {
        if (isReceiverRegistered) {
            unregisterReceiver(airplaneModeReceiver)
            isReceiverRegistered = false
        }
    }

    private fun applyTheme(isDarkTheme: Boolean) {
        if (isDarkTheme) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
    }

    private fun scheduleBackup() {
        val backupWorkRequest = PeriodicWorkRequestBuilder<TransactionBackupWorker>(
            1, TimeUnit.HOURS
        ).build()
        WorkManager.getInstance(this).enqueue(backupWorkRequest)
    }

    private fun triggerBackupManually() {
        val backupWorkRequest = OneTimeWorkRequestBuilder<TransactionBackupWorker>().build()
        WorkManager.getInstance(this).enqueue(backupWorkRequest)
    }

    private fun startStockMarketUpdates() {
        job = CoroutineScope(Dispatchers.Main).launch {
            while (isActive) {
                fetchStockData()
                delay(60000)
            }
        }
    }

    private suspend fun fetchStockData() {
        stockDataList.clear()
        val maxRetries = 3
        val initialDelay = 1000L

        symbols.forEach { symbol ->
            val response = fetchStockDataWithRetry(symbol, maxRetries, initialDelay)
            response?.globalQuote?.let {
                val stockData = """
                Symbol: ${it.symbol}
                Price: $${it.price}
                Open: $${it.open}
                High: $${it.high}
                Low: $${it.low}
                Volume: ${it.volume}
                Change: ${it.change} (${it.changePercent})
            """.trimIndent()
                stockDataList.add(stockData)
            }
            delay(5000)
        }

        updateStockTextView(stockDataList)
    }

    private suspend fun fetchStockDataWithRetry(
        symbol: String,
        maxRetries: Int,
        initialDelay: Long
    ): StockResponse? {
        var currentRetry = 0
        var currentDelay = initialDelay

        while (currentRetry <= maxRetries) {
            try {
                val response = RetrofitClient.api.getStockQuote(symbol = symbol, apiKey = apiKey)
                Log.d("RAW_RESPONSE", response.toString())

                response.globalQuote?.let {
                    Log.d("PARSED_RESPONSE", it.toString())
                    return response
                }

                Log.e("API_RESPONSE", "Global Quote is null for $symbol")
                return null
            } catch (e: HttpException) {
                Log.e("HTTP_EXCEPTION", "HTTP error: ${e.message()}")
            } catch (e: Exception) {
                Log.e("API_ERROR", "Error fetching data for $symbol: ${e.message}", e)
            }

            delay(currentDelay)
            currentRetry++
            currentDelay *= 2
        }
        return null
    }

    private fun updateStockTextView(stockDataList: List<String>) {
        val displayText = stockDataList.joinToString("\n") { stock ->
            stock
        }
        stockMarketValues.text = displayText
    }

    private fun startSavingsService() {
        val intent = Intent(this, SavingsProgressService::class.java)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            startForegroundService(intent) // For Android 8.0+
        } else {
            startService(intent) // For earlier versions
        }
        Toast.makeText(this, "Savings Progress Service Started", Toast.LENGTH_SHORT).show()
    }

    private fun stopSavingsService() {
        val intent = Intent(this, SavingsProgressService::class.java)
        stopService(intent)
        Toast.makeText(this, "Savings Progress Service Stopped", Toast.LENGTH_SHORT).show()
    }

    override fun onStop() {
        super.onStop()
        if (isBound) {
            unbindService(serviceConnection)
            isBound = false
        }
    }
}



