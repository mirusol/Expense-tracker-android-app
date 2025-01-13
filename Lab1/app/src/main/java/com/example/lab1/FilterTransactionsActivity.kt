package com.example.lab1

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.lab1.database.DatabaseHelper

class FilterTransactionsActivity : AppCompatActivity() {

    private lateinit var spinnerFilterCategory: Spinner
    private lateinit var tvFilteredResults: TextView
    private lateinit var btnHome: Button
    private lateinit var databaseHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_filter_transactions)

        spinnerFilterCategory = findViewById(R.id.spinner_filter_category)
        tvFilteredResults = findViewById(R.id.tv_filtered_results)
        btnHome = findViewById(R.id.btn_home)

        databaseHelper = DatabaseHelper(this)


        btnHome.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish()
        }

        val categories = arrayOf("All", "Rent", "Shopping", "Groceries", "Tech", "Online Purchases", "Entertainment")
        val adapterSpinner = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        adapterSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerFilterCategory.adapter = adapterSpinner

        spinnerFilterCategory.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedCategory = parent?.getItemAtPosition(position).toString()
                filterTransactions(selectedCategory)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }
    }

    private fun filterTransactions(category: String) {
        val cursor = if (category == "All") {
            databaseHelper.getAllTransactions()
        } else {
            databaseHelper.getTransactionsByCategory(category)
        }

        val filteredResults = StringBuilder()
        if (cursor.moveToFirst()) {
            do {
                val description = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_DESCRIPTION))
                val amount = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_AMOUNT))
                val category = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CATEGORY)) ?: "Uncategorized"
                filteredResults.append("Description: $description\nAmount: $amount\nCategory: $category\n\n")
            } while (cursor.moveToNext())
        } else {
            filteredResults.append("No transactions found.")
        }
        cursor.close()

        tvFilteredResults.text = filteredResults.toString()
    }
}

