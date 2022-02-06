package com.deepblue.cleaning.cleanview

import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import android.widget.Toast
import com.deepblue.cleaning.R
import com.mdx.framework.Frame
import com.mdx.framework.utility.Helper
import org.jetbrains.anko.runOnUiThread

class BToast(context: Context?) : Toast(context) {
    companion object {

        fun showText(
            context: Context = Frame.CONTEXT,
            text: CharSequence,
            duration: Int = LENGTH_SHORT
        ) {
//            context.runOnUiThread {
//                initToast(context, text)
//                toast?.duration = duration
//                toast?.show()
//            }
        }

        fun showText(text: CharSequence) {
            Frame.CONTEXT.runOnUiThread {
                Helper.toast(text.toString())
            }
        }
    }
}