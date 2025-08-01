package com.mtmes.packing.view.utils

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import java.util.*

/**
 * 語言管理工具類
 */
object LanguageManager {
    
    private const val PREF_NAME = "language_settings"
    private const val KEY_LANGUAGE = "selected_language"
    
    const val LANGUAGE_CHINESE = "zh"
    const val LANGUAGE_ENGLISH = "en"
    
    /**
     * 保存選擇的語言
     */
    fun saveLanguage(context: Context, language: String) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_LANGUAGE, language).apply()
    }
    
    /**
     * 獲取保存的語言，默認為中文
     */
    fun getSavedLanguage(context: Context): String {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_LANGUAGE, LANGUAGE_CHINESE) ?: LANGUAGE_CHINESE
    }
    
    /**
     * 設置應用語言
     */
    fun setAppLanguage(context: Context, language: String): Context {
        val locale = Locale(language)
        Locale.setDefault(locale)
        
        val config = Configuration()
        config.setLocale(locale)
        
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            context.createConfigurationContext(config)
        } else {
            @Suppress("DEPRECATION")
            context.resources.updateConfiguration(config, context.resources.displayMetrics)
            context
        }
    }
    
    /**
     * 切換語言
     */
    fun switchLanguage(context: Context): String {
        val currentLanguage = getSavedLanguage(context)
        val newLanguage = if (currentLanguage == LANGUAGE_CHINESE) {
            LANGUAGE_ENGLISH
        } else {
            LANGUAGE_CHINESE
        }
        saveLanguage(context, newLanguage)
        return newLanguage
    }
    
    /**
     * 獲取語言切換按鈕文字
     */
    fun getLanguageSwitchText(context: Context): String {
        return when (getSavedLanguage(context)) {
            LANGUAGE_CHINESE -> "Switch English"
            LANGUAGE_ENGLISH -> "轉換中文"
            else -> "Switch English"
        }
    }
}