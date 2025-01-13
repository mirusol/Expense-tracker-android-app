package com.example.lab1

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class SetBudgetActivity : AppCompatActivity() {

    private lateinit var etBudget: EditText
    private lateinit var btnSaveBudget: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_set_budget)

        etBudget = findViewById(R.id.et_budget)
        btnSaveBudget = findViewById(R.id.btn_save_budget)

        val sharedPreferences = getSharedPreferences("BudgetPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        btnSaveBudget.setOnClickListener {
            val budget = etBudget.text.toString().toDoubleOrNull()

            if (budget == null) {
                Toast.makeText(this, "Please enter a valid budget", Toast.LENGTH_SHORT).show()
            } else {
                editor.putFloat("budget", budget.toFloat())
                editor.apply()
                Toast.makeText(this, "Budget saved: $$budget", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
        val homeButton: Button = findViewById(R.id.btn_home)
        homeButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish()
        }
        fun saveBudget(budget: Float) {
            val sharedPreferences = getSharedPreferences("BudgetPrefs", MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.putFloat("budget", budget)
            editor.apply()
            Toast.makeText(this, "Budget saved successfully", Toast.LENGTH_SHORT).show()
        }
        val etSavingsGoal: EditText = findViewById(R.id.et_savings_goal)
        btnSaveBudget.setOnClickListener {
            val budget = etBudget.text.toString().toFloatOrNull()
            val savingsGoal = etSavingsGoal.text.toString().toFloatOrNull()

            if (budget != null && savingsGoal != null) {
                val sharedPreferences = getSharedPreferences("BudgetPrefs", Context.MODE_PRIVATE)
                val editor = sharedPreferences.edit()
                editor.putFloat("budget", budget)
                editor.putFloat("savings_goal", savingsGoal)
                editor.apply()
                Toast.makeText(this, "Budget and Savings Goal Set!", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "Please enter valid values", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
