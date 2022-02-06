package com.deepblue.cleaning.utils

import android.content.Context
import android.content.SharedPreferences

class SharedPreferencesHelper(
    context: Context,
    file_name: String?
) {
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(
        file_name,
        Context.MODE_PRIVATE
    )
    private val editor: SharedPreferences.Editor
    fun put(key: String?, `object`: Any) {
        if (`object` is String) {
            editor.putString(key, `object`)
        } else if (`object` is Int) {
            editor.putInt(key, `object`)
        } else if (`object` is Boolean) {
            editor.putBoolean(key, `object`)
        } else if (`object` is Float) {
            editor.putFloat(key, `object`)
        } else if (`object` is Long) {
            editor.putLong(key, `object`)
        } else {
            editor.putString(key, `object`.toString())
        }
//        editor.commit()
        editor.apply()
    }

    operator fun get(key: String?, defaultObject: Any?): Any? {
        return if (defaultObject is String) {
            sharedPreferences.getString(key, defaultObject as String?)
        } else if (defaultObject is Int) {
            sharedPreferences.getInt(key, (defaultObject as Int?)!!)
        } else if (defaultObject is Boolean) {
            sharedPreferences.getBoolean(key, (defaultObject as Boolean?)!!)
        } else if (defaultObject is Float) {
            sharedPreferences.getFloat(key, (defaultObject as Float?)!!)
        } else if (defaultObject is Long) {
            sharedPreferences.getLong(key, (defaultObject as Long?)!!)
        } else {
            sharedPreferences.getString(key, null)
        }
    }

    fun remove(key: String?) {
        editor.remove(key)
        editor.commit()
    }

    fun clear() {
        editor.clear()
        editor.commit()
    }

    fun contain(key: String?): Boolean {
        return sharedPreferences.contains(key)
    }

    /**
     * 返回所有的键值对
     */
    val all: Map<String, *>
        get() = sharedPreferences.all

    init {
        editor = sharedPreferences.edit()
    }
}