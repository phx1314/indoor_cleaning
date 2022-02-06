package com.deepblue.cleaning.item

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.BitmapDrawable
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout.LayoutParams
import android.widget.PopupWindow
import com.mdx.framework.utility.AbViewUtil
import java.security.AccessController.getContext


class PopShowChangeLevel(
    var context: Context,
    private val view: View,
    popview: View
) {
    private val popwindow: PopupWindow

    val isShow: Boolean
        get() = popwindow.isShowing

    init {
        val flater = LayoutInflater.from(context)
        popwindow = PopupWindow(popview, view.width, LayoutParams.MATCH_PARENT)
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
        popwindow.showAsDropDown(view, 0, AbViewUtil.dip2px(context, 5f).toInt())
    }

    fun hide() {
        popwindow.dismiss()
    }


}
