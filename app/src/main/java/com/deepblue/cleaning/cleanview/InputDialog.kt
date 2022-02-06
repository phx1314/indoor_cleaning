package com.deepblue.cleaning.cleanview

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.View
import android.view.ViewGroup
import com.deepblue.cleaning.R
import kotlinx.android.synthetic.main.dialog_alert.*
import kotlinx.android.synthetic.main.dialog_input.*
import kotlinx.android.synthetic.main.layout_backtocdd.*

class InputDialog(
    context: Context,
    val message: String,
    val listener: DialogButtonListener, val text: String = ""
) : Dialog(context, R.style.DialogStyle) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_input)
//        setCancelable(cancelable)
        if (message.isNotEmpty()) {
            et_name.hint = message
        }
        if (text.isNotEmpty()) {
            et_name.setText(text)
        }
        tv_ok.setOnClickListener {
            if (et_name.text.isNotEmpty()) {
                if (listener?.ensure(et_name.text.toString())) {
                    dismiss()
                }
            }
        }
        tv_cancle.setOnClickListener {
            listener?.cancel()
            dismiss()
        }

    }

    interface DialogButtonListener {
        fun cancel()

        fun ensure(input: String): Boolean
    }

    public override fun onStart() {
        super.onStart()
        val dm = DisplayMetrics()
        ownerActivity?.windowManager?.defaultDisplay?.getMetrics(dm)
        this.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
//        this.window?.setLayout((dm.widthPixels * 0.618).toInt(), ViewGroup.LayoutParams.WRAP_CONTENT)
        this.window?.setBackgroundDrawableResource(R.drawable.transparent)
    }
}