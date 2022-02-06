package com.deepblue.cleaning.item

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.BitmapDrawable
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout.LayoutParams
import android.widget.PopupWindow
import com.mdx.framework.F
import com.mdx.framework.utility.AbViewUtil
import java.security.AccessController.getContext


class PopShowKeyBord(
    var context: Context,
    private val view: View,
    popview: View
) : PopShowBase() {
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
        popwindow.showAsDropDown(view, 0, F.dp2px(context, 28f))
    }

    override fun hide() {
        popwindow.dismiss()
    }


}
