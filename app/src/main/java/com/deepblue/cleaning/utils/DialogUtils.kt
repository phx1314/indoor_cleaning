package com.deepblue.cleaning.utils

import android.R
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import com.deepblue.cleaning.cleanview.AlertDialog
import com.deepblue.cleaning.cleanview.InputDialog

object DialogUtils {

    fun showAlert(
        activity: FragmentActivity,
        message: String,
        resOK: Int = R.string.ok,
        listener: AlertDialog.DialogButtonListener? = null,
        cancelable: Boolean = false,
        hasTopIcon: Boolean = false,
        hasFillPoint: Boolean = true,
        type: String = "C",
        hasRemind: Boolean = false
    ): DialogFragment? {
        val dialogListener = object : DialogListener {
            override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
                return AlertDialog(
                    activity,
                    message,
                    resOK,
                    listener!!,
                    cancelable, hasTopIcon, hasFillPoint, type, hasRemind
                )
            }

            override fun onCancel() {}
        }
        val dialogFragment = DialogFragment.newInstance(dialogListener, cancelable)
        dialogFragment.show(activity.supportFragmentManager, "ALERT_DIALOG")
        return dialogFragment
    }


    fun showInput(
        activity: FragmentActivity,
        message: String,
        listener: InputDialog.DialogButtonListener? = null, text: String = ""
    ): DialogFragment? {

        val dialogListener = object : DialogListener {
            override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
                return InputDialog(
                    activity,
                    message,
                    listener!!, text
                )
            }

            override fun onCancel() {}
        }
        val dialogFragment = DialogFragment.newInstance(dialogListener, false)
        dialogFragment.show(activity.supportFragmentManager, "ALERT_DIALOG")
        return dialogFragment
    }
}