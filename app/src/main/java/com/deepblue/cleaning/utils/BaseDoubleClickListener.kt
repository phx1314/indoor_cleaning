package com.deepblue.cleaning.utils

import android.view.View

abstract class BaseDoubleClickListener : View.OnClickListener {
    override fun onClick(v: View) {
        val currentTimeMillis = System.currentTimeMillis()
        if (currentTimeMillis - lastClickTime < DOUBLE_TIME) {
            onDoubleClick(v)
        }
        lastClickTime = currentTimeMillis
    }

    /**
     * 双击事件
     *
     * @param v 视图
     */
    abstract fun onDoubleClick(v: View?)

    companion object {
        private const val DOUBLE_TIME: Long = 500
        private var lastClickTime: Long = 0
    }
}