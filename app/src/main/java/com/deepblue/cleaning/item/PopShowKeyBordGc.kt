package com.deepblue.cleaning.item

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.BitmapDrawable
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout.LayoutParams
import android.widget.PopupWindow
import com.mdx.framework.F


class PopShowKeyBordGc(
    var context: Context,
    private val view: View,
    popview: View
) : PopShowBase() {
    private val popwindow: PopupWindow

    val isShow: Boolean
        get() = popwindow.isShowing

    init {
        val flater = LayoutInflater.from(context)
        popwindow = PopupWindow(popview, view.width, LayoutParams.WRAP_CONTENT)
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
        popwindow.showAtLocation(view, Gravity.LEFT or Gravity.BOTTOM, 0, 0)
    }

    override fun hide() {
        popwindow.dismiss()
    }


}
