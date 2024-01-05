package com.app.mytipsjob.utils

object Pref {

    fun saveString(key: String, value: String) {
        MyApp.pref?.edit()?.putString(key, value)?.apply()
    }

    fun getString(key: String): String {
        return MyApp.pref?.getString(key, "") ?: ""
    }

    fun getInt(key: String): Int {
        return MyApp.pref?.getInt(key, 0) ?: 0
    }

    fun saveInt(key: String, value: Int) {
        MyApp.pref?.edit()?.putInt(key, value)?.apply()
    }

    fun getFloat(key: String): Float {
        return MyApp.pref?.getFloat(key, 0.00f) ?: 0.00f
    }

    fun saveFloat(key: String, value: Float) {
        MyApp.pref?.edit()?.putFloat(key, value)?.apply()
    }
}