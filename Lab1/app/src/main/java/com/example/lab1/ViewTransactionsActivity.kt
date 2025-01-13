package com.example.lab1

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.lab1.database.DatabaseHelper
import com.example.lab1.adapter.TransactionAdapter
import com.example.lab1.database.Transaction
import android.widget.Toast

import androidx.recyclerview.widget.LinearLayoutManager
import android.widget.Button
import androidx.recyclerview.widget.RecyclerView


class ViewTransactionsActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: TransactionAdapter
    private lateinit var databaseHelper: DatabaseHelper
    private val transactions = mutableListOf<Transaction>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_transactions)

        recyclerView = findViewById(R.id.recycler_view_transactions)
        recyclerView.layoutManager = LinearLayoutManager(this)
        databaseHelper = DatabaseHelper(this)

        loadTransactions()

        val homeButton: Button = findViewById(R.id.btn_home)
        homeButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish()
        }
        adapter = TransactionAdapter(
            transactions,
            onDeleteClick = { transaction ->
                deleteTransaction(transaction)
            },
            onUpdateClick = { transaction ->
                updateTransaction(transaction)
            }
        )
        recyclerView.adapter = adapter
    }

    private fun loadTransactions() {
        val cursor = databaseHelper.getAllTransactions()
        transactions.clear()
        if (cursor.moveToFirst()) {
            do {
                val transaction = Transaction(
                    id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ID)),
                    amount = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_AMOUNT)),
                    description = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_DESCRIPTION)),
                    date = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_DATE)),
                    category = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CATEGORY)) ?: "Uncategorized" // Handle null
                )
                transactions.add(transaction)
            } while (cursor.moveToNext())
        }
        cursor.close()
    }

    private fun deleteTransaction(transaction: Transaction) {
        val rowsDeleted = databaseHelper.deleteTransaction(transaction.id)
        if (rowsDeleted > 0) {
            adapter.removeTransaction(transaction)
            Toast.makeText(this, "Transaction deleted", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Failed to delete transaction", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateTransaction(transaction: Transaction) {
        val rowsUpdated = databaseHelper.updateTransaction(
            id = transaction.id,
            amount = transaction.amount,
            description = transaction.description,
            date = System.currentTimeMillis(),
            category = transaction.category // Include category
        )
        if (rowsUpdated > 0) {
            adapter.updateTransaction(transaction)
            Toast.makeText(this, "Transaction updated", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Failed to update transaction", Toast.LENGTH_SHORT).show()
        }
    }
}

