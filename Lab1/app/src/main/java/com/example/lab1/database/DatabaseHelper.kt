package com.example.lab1.database

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "ExpenseTracker.db"
        private const val DATABASE_VERSION = 2

        const val TABLE_TRANSACTIONS = "transactions"
        const val COLUMN_ID = "id"
        const val COLUMN_AMOUNT = "amount"
        const val COLUMN_DESCRIPTION = "description"
        const val COLUMN_DATE = "date"
        const val COLUMN_CATEGORY = "category"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTableQuery = """
            CREATE TABLE $TABLE_TRANSACTIONS (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_AMOUNT REAL,
                $COLUMN_DESCRIPTION TEXT,
                $COLUMN_DATE INTEGER,
                $COLUMN_CATEGORY TEXT
            )
        """.trimIndent()
        db.execSQL(createTableQuery)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE $TABLE_TRANSACTIONS ADD COLUMN $COLUMN_CATEGORY TEXT DEFAULT 'Uncategorized'")
        }
    }
    fun addTransaction(amount: Double, description: String, date: Long, category: String): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_AMOUNT, amount)
            put(COLUMN_DESCRIPTION, description)
            put(COLUMN_DATE, date)
            put(COLUMN_CATEGORY, category)
        }
        return db.insert(TABLE_TRANSACTIONS, null, values)
    }
    fun getAllTransactions(): Cursor {
        val db = readableDatabase
        return db.query(
            TABLE_TRANSACTIONS,
            null, null, null, null, null,
            "$COLUMN_DATE DESC"
        )
    }
    fun getTransactionsByCategory(category: String): Cursor {
        val db = readableDatabase
        return db.query(
            TABLE_TRANSACTIONS,
            null,
            "$COLUMN_CATEGORY = ?",
            arrayOf(category),
            null, null,
            "$COLUMN_DATE DESC"
        )
    }
    fun updateTransaction(id: Int, amount: Double, description: String, date: Long, category: String): Int {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_AMOUNT, amount)
            put(COLUMN_DESCRIPTION, description)
            put(COLUMN_DATE, date)
            put(COLUMN_CATEGORY, category)
        }
        return db.update(TABLE_TRANSACTIONS, values, "$COLUMN_ID = ?", arrayOf(id.toString()))
    }

    fun deleteTransaction(id: Int): Int {
        val db = writableDatabase
        return db.delete(TABLE_TRANSACTIONS, "$COLUMN_ID = ?", arrayOf(id.toString()))
    }
}
