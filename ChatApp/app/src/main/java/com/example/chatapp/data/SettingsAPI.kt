package com.example.chatapp.data


import android.content.Context
import android.content.SharedPreferences

import com.example.chatapp.R

import java.util.ArrayList

/**
 * Created by Bibaswann on 20-02-2017.
 */

class SettingsAPI(internal var mContext: Context?) {
    private val sharedSettings: SharedPreferences

    init {
        sharedSettings = mContext!!.getSharedPreferences(mContext!!.getString(R.string.settings_file_name), Context.MODE_PRIVATE)
    }

    fun readSetting(key: String): String {
        return sharedSettings.getString(key, "na").toString()
    }

    fun addUpdateSettings(key: String, value: String) {
        val editor = sharedSettings.edit()
        editor.putString(key, value)
        editor.apply()
    }

    fun deleteAllSettings() {
        sharedSettings.edit().clear().apply()
    }

    fun readAll(): List<String> {
        val allUser = ArrayList<String>()
        val allEntries = sharedSettings.all
        for ((key, value) in allEntries) {
            if (key.contains("@"))
                allUser.add("$key ($value)")
        }
        return allUser
    }
}
