package com.paddle.ocr

import android.app.Application
import android.content.Context
import android.content.SharedPreferences

class PaddleOcrApp : Application() {

    companion object {
        lateinit var instance: PaddleOcrApp
            private set

        private const val PREFS_NAME = "paddle_ocr_prefs"
        private const val KEY_API_TOKEN = "key_api_token"
        private const val KEY_SELECTED_MODEL = "key_selected_model"

        fun getPrefs(): SharedPreferences {
            return instance.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        }

        var apiToken: String?
            get() = getPrefs().getString(KEY_API_TOKEN, null)
            set(value) = getPrefs().edit().putString(KEY_API_TOKEN, value).apply()

        var selectedModel: String
            get() = getPrefs().getString(KEY_SELECTED_MODEL, "PaddleOCR-VL-1.6") ?: "PaddleOCR-VL-1.6"
            set(value) = getPrefs().edit().putString(KEY_SELECTED_MODEL, value).apply()
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }
}
