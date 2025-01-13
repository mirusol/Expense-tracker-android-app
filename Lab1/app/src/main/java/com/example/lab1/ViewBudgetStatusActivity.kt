package com.example.lab1

import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.lab1.database.DatabaseHelper

import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.components.XAxis



class ViewBudgetStatusActivity : AppCompatActivity() {

    private lateinit var tvBudgetSet: TextView
    private lateinit var tvRemainingBudget: TextView
    private lateinit var barChart: BarChart
    private lateinit var databaseHelper: DatabaseHelper
    private lateinit var homeButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_budget_status)

        tvBudgetSet = findViewById(R.id.tv_budget_set)
        tvRemainingBudget = findViewById(R.id.tv_remaining_budget)
        barChart = findViewById(R.id.bar_chart)
        homeButton = findViewById(R.id.btn_home)

        databaseHelper = DatabaseHelper(this)

        homeButton.setOnClickListener {
            finish()
        }

        val sharedPreferences = getSharedPreferences("BudgetPrefs", Context.MODE_PRIVATE)
        val budget = sharedPreferences.getFloat("budget", 0f)
        val totalSpent = calculateTotalSpent()
        val remainingBudget = budget - totalSpent

        tvBudgetSet.text = "Budget: $${String.format("%.2f", budget)}"
        tvRemainingBudget.text = "Remaining: $${String.format("%.2f", remainingBudget)}"

        setupBarChart(budget, totalSpent)


        if (remainingBudget < 0) {
            tvRemainingBudget.setTextColor(getColor(android.R.color.holo_red_dark))
        } else {
            tvRemainingBudget.setTextColor(getColor(android.R.color.holo_green_dark))
        }
    }

    private fun calculateTotalSpent(): Float {
        val cursor = databaseHelper.getAllTransactions()
        var total = 0f
        if (cursor.moveToFirst()) {
            do {
                val amount = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_AMOUNT))
                total += amount.toFloat()
            } while (cursor.moveToNext())
        }
        cursor.close()
        return total
    }

    private fun setupBarChart(budget: Float, totalSpent: Float) {
        val barEntries = mutableListOf<BarEntry>()
        barEntries.add(BarEntry(1f, budget))
        barEntries.add(BarEntry(2f, totalSpent))

        val barDataSet = BarDataSet(barEntries, "Budget Comparison")
        barDataSet.colors = listOf(
            getColor(android.R.color.holo_blue_light),
            getColor(android.R.color.holo_red_light)
        )
        barDataSet.valueTextSize = 14f
        val barData = BarData(barDataSet)
        barData.barWidth = 0.5f

        barChart.data = barData
        barChart.description.isEnabled = false
        barChart.xAxis.position = XAxis.XAxisPosition.BOTTOM
        barChart.xAxis.granularity = 1f
        barChart.xAxis.setDrawGridLines(false)
        barChart.axisLeft.setDrawGridLines(false)
        barChart.axisRight.isEnabled = false
        barChart.legend.isEnabled = true
        barChart.xAxis.valueFormatter = XAxisFormatter()
        barChart.invalidate()
    }

    inner class XAxisFormatter : com.github.mikephil.charting.formatter.ValueFormatter() {
        override fun getFormattedValue(value: Float): String {
            return when (value.toInt()) {
                1 -> "Budget"
                2 -> "Spent"
                else -> ""
            }
        }
    }
}
