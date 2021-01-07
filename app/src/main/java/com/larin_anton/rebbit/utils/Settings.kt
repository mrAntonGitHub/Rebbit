package com.larin_anton.rebbit.utils

import android.content.Context
import android.content.SharedPreferences
import javax.inject.Inject

private const val INIT_PREFERENCES = "MAIN_SETTINGS"
private const val IS_FIRST_START = "IS_FIRST_START"

class Settings @Inject constructor() {
    lateinit var shredPreferences: SharedPreferences

    fun setFirstStart(isFirstStart : Boolean, context: Context){
        isSharedPreferencesInit(context)
        setBooleanValue(shredPreferences, isFirstStart, IS_FIRST_START)
    }

    fun isFirstStart(context: Context): Boolean {
        isSharedPreferencesInit(context)
        return shredPreferences.getBoolean(IS_FIRST_START, true)
    }



    private fun setBooleanValue(sharedPreferences: SharedPreferences, value: Boolean, settingsTag: String){
        val editor = sharedPreferences.edit()
        editor.putBoolean(settingsTag, value)
        editor.apply()
    }
    private fun setStringValue(sharedPreferences: SharedPreferences, value: String, settingsTag: String){
        val editor = sharedPreferences.edit()
        editor.putString(settingsTag, value)
        editor.apply()
    }
    private fun removeStringValue(sharedPreferences: SharedPreferences, settingsTag: String){
        val editor = sharedPreferences.edit()
        editor.remove(settingsTag)
        editor.apply()
    }

    private fun isSharedPreferencesInit(context: Context) {
        if (!::shredPreferences.isInitialized) {
            initSharedPreferences(context)
        }
    }
    private fun initSharedPreferences(context: Context) {
        shredPreferences = context.getSharedPreferences(INIT_PREFERENCES, Context.MODE_PRIVATE)
    }


}