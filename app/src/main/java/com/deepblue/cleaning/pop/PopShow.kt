package com.deepblue.cleaning.pop

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.BitmapDrawable
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout.LayoutParams
import android.widget.PopupWindow
import com.mdx.framework.activity.BaseActivity
import kotlin.math.roundToInt


class PopShow(var context: Context, private val view: View, popview: View, var xoff: Int=0, var yoff: Int=0) {
    private val popwindow: PopupWindow

    val isShow: Boolean
        get() = popwindow.isShowing

    init {
        val flater = LayoutInflater.from(context)
        popwindow = PopupWindow(popview, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
        popwindow.setBackgroundDrawable(BitmapDrawable(context.resources))
        popwindow.isTouchable = true
        popwindow.isOutsideTouchable = true
        popwindow.isFocusable = true

    }

    fun setOnDismissListener(l: PopupWindow.OnDismissListener) {
        popwindow.setOnDismissListener(l)
    }

    @SuppressLint("NewApi")
    fun show() {
        popwindow.showAsDropDown(view, xoff, yoff)
    }

    fun hide() {
        popwindow.dismiss()
    }


}
