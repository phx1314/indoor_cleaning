package com.deepblue.cleaning.cleanview

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.View
import android.view.ViewGroup
import com.deepblue.cleaning.R
import kotlinx.android.synthetic.main.dialog_alert.*
import kotlinx.android.synthetic.main.layout_backtocdd.*

class AlertDialog(
    context: Context,
    val message: String,
    val btnOKResId: Int,
    val listener: DialogButtonListener,
    val cancelable: Boolean? = true,
    val hasTopIcon: Boolean = false,
    val hasFillPoint: Boolean = true,
    val type: String,
    val hasRemind: Boolean = false
) : Dialog(context, R.style.DialogStyle) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_alert)
        if (cancelable != null) {
            setCancelable(cancelable)
        }

        task_start_tv.setText(btnOKResId)

        task_start_tv.setOnClickListener {
            if (listener?.ensure(mCheckBox.isChecked)) {
                dismiss()
            }
        }
        if (hasRemind) mLinearLayout_tx.visibility = View.VISIBLE

        if (hasTopIcon) {
            if (type == "C") {
                tv_message.setCompoundDrawablesWithIntrinsicBounds(
                    0,
                    R.drawable.ic_littlebattery,
                    0,
                    0
                )
            } else if (type == "E") {
                tv_message.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_addwater, 0, 0)
            } else if (type == "H") {
                tv_message.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_pushwater, 0, 0)
            }else if (type == "ERROR") {
                tv_message.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_titlebar_error_y, 0, 0)
            }
        } else {
            tv_message.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
        }

        tv_message.text = message
        if (!hasFillPoint) {
            mLinearLayout_bootm.visibility = View.GONE
            if (type == "C") {
                tv_message.setCompoundDrawablesWithIntrinsicBounds(
                    0,
                    R.drawable.ic_littlebatteryno,
                    0,
                    0
                )
            } else if (type == "E") {
                tv_message.setCompoundDrawablesWithIntrinsicBounds(
                    0,
                    R.drawable.ic_addwaterno,
                    0,
                    0
                )
            } else if (type == "H") {
                tv_message.setCompoundDrawablesWithIntrinsicBounds(
                    0,
                    R.drawable.ic_pushwaterno,
                    0,
                    0
                )
            }

        }

        close_dialog_icon_rl.setOnClickListener {
            listener?.cancel()
            dismiss()
        }

    }

    interface DialogButtonListener {
        fun cancel()

        fun ensure(isCheck: Boolean): Boolean
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