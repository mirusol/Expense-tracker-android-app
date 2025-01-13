package com.example.lab1.services

import android.app.Service
import android.content.Intent
import android.content.SharedPreferences
import android.os.Binder
import android.os.IBinder

class ThemeService : Service() {

    private val binder = ThemeBinder()
    private var isDarkTheme = false

    companion object {
        const val PREFS_NAME = "ThemePrefs"
        const val KEY_IS_DARK_THEME = "isDarkTheme"
    }

    override fun onCreate() {
        super.onCreate()
        val sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        isDarkTheme = sharedPreferences.getBoolean(KEY_IS_DARK_THEME, false)
    }

    override fun onBind(intent: Intent?): IBinder {
        return binder
    }

    fun toggleTheme(): Boolean {
        isDarkTheme = !isDarkTheme

        val sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val editor: SharedPreferences.Editor = sharedPreferences.edit()
        editor.putBoolean(KEY_IS_DARK_THEME, isDarkTheme)
        editor.apply()

        return isDarkTheme
    }

    fun getCurrentTheme(): Boolean {
        return isDarkTheme
    }

    inner class ThemeBinder : Binder() {
        fun getService(): ThemeService = this@ThemeService
    }
}
