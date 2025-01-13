package com.example.lab1.workers

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.lab1.database.DatabaseHelper
import java.io.File
import java.io.FileWriter


class TransactionBackupWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {

    override fun doWork(): Result {
        try {
            val dbHelper = DatabaseHelper(applicationContext)
            val transactions = fetchTransactions(dbHelper)
            val backupFile = createBackupFile(transactions)

            Log.d("TransactionBackupWorker", "Backup saved to: ${backupFile.absolutePath}")
            return Result.success()
        } catch (e: Exception) {
            Log.e("TransactionBackupWorker", "Error during backup", e)
            return Result.failure()
        }
    }

    private fun fetchTransactions(dbHelper: DatabaseHelper): List<Map<String, Any>> {
        val transactions = mutableListOf<Map<String, Any>>()
        val cursor = dbHelper.getAllTransactions()

        if (cursor.moveToFirst()) {
            do {
                val transaction = mapOf(
                    "id" to cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ID)),
                    "amount" to cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_AMOUNT)),
                    "description" to cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_DESCRIPTION)),
                    "date" to cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_DATE)),
                    "category" to cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CATEGORY))
                )
                transactions.add(transaction)
            } while (cursor.moveToNext())
        }
        cursor.close()
        return transactions
    }

    private fun createBackupFile(transactions: List<Map<String, Any>>): File {
        val backupDir = File(applicationContext.filesDir, "backups")
        if (!backupDir.exists()) backupDir.mkdirs()

        val backupFile = File(backupDir, "transactions_backup.json")
        FileWriter(backupFile).use { writer ->
            writer.write(transactions.joinToString(prefix = "[", postfix = "]") { transaction ->
                transaction.entries.joinToString(
                    prefix = "{", postfix = "}", separator = ","
                ) { (key, value) -> "\"$key\":\"$value\"" }
            })
        }

        return backupFile
    }
}
