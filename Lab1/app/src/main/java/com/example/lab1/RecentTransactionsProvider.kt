package com.example.lab1.provider

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import com.example.lab1.database.DatabaseHelper

class RecentTransactionsProvider : ContentProvider() {

    companion object {
        const val AUTHORITY = "com.example.lab1.provider.RecentTransactionsProvider"
        val CONTENT_URI: Uri = Uri.parse("content://$AUTHORITY/transactions")
    }

    private lateinit var databaseHelper: DatabaseHelper

    override fun onCreate(): Boolean {
        context?.let {
            databaseHelper = DatabaseHelper(it)
        }
        return true
    }

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?
    ): Cursor? {
        val db = databaseHelper.readableDatabase
        return db.query(
            DatabaseHelper.TABLE_TRANSACTIONS,
            projection,
            selection,
            selectionArgs,
            null,
            null,
            "${DatabaseHelper.COLUMN_DATE} DESC",
            "5"
        )
    }

    override fun getType(uri: Uri): String? = "vnd.android.cursor.dir/$AUTHORITY.transactions"
    override fun insert(uri: Uri, values: ContentValues?): Uri? = null
    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int = 0
    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<out String>?): Int = 0
}
