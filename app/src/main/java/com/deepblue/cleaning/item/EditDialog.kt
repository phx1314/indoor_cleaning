//
//  EditDialog
//
//  Created by 86139 on 2020-10-27 10:31:38
//  Copyright (c) 86139 All rights reserved.


/**

 */

package com.deepblue.cleaning.item;

import com.deepblue.cleaning.R;

import android.annotation.SuppressLint;
import android.app.Activity
import android.app.Dialog
import android.content.Context;
import android.os.Handler
import android.text.TextUtils
import android.view.LayoutInflater;
import android.view.ViewGroup;

import android.view.View;
import android.view.inputmethod.InputMethodManager
import android.widget.EditText;
import android.widget.TextView;
import com.deepblue.cleaning.F
import com.mdx.framework.Frame
import com.mdx.framework.utility.Helper
import kotlinx.android.synthetic.main.item_edit_dialog.view.*


class EditDialog(context: Context?) : BaseItem(context) {
    init {
        val flater = LayoutInflater.from(context)
        flater.inflate(R.layout.item_edit_dialog, this)

    }

    fun set(item: Dialog, name: String) {
        mEditText.setText(name)
        mTextView_cancel.setOnClickListener {
            item.dismiss()
        }
        mTextView_sure.setOnClickListener {
            if (TextUtils.isEmpty(mEditText.text.toString())) {
                Helper.toast(context.getString(R.string.i_qsr))
                return@setOnClickListener
            }
            item.dismiss()
            Frame.HANDLES.sentAll("FrgTaskManageDetail", if (TextUtils.isEmpty(name)) 0 else 1, mEditText.text.toString())
        }
        item.setOnDismissListener {
            F.hideInput(context as Activity)
        }
    }

//    /**
//     * 隐藏键盘
//     */
//    fun hideInput(act: Activity) {
//        val localInputMethodManager = act.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
//        val localIBinder = mLinearLayout_c.windowToken
//        localInputMethodManager.hideSoftInputFromWindow(localIBinder, 2)
//    }
}