//
//  PopChangeLevel
//
//  Created by 86139 on 2020-08-10 18:50:39
//  Copyright (c) 86139 All rights reserved.


/**

 */

package com.deepblue.cleaning.item;

import com.deepblue.cleaning.R;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color
import android.view.LayoutInflater;
import android.view.ViewGroup;

import android.view.View;
import android.widget.PopupWindow
import android.widget.TextView;
import com.deepblue.cleaning.Const
import com.mdx.framework.Frame
import kotlinx.android.synthetic.main.item_pop_change_level.view.*


class PopChangeLevel(context: Context?) : BaseItem(context) {
    init {
        val flater = LayoutInflater.from(context)
        flater.inflate(R.layout.item_pop_change_level, this)
    }

    fun set(type: Int, selction: Int, mPopShowSet: PopShowChangeLevel, from: String) {
        when (type) {
            0 -> {
                mTextView_1.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_cc_1, 0, 0, 0)
                mTextView_2.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_cc_2, 0, 0, 0)

                mTextView_3.visibility = View.INVISIBLE
            }
            1 -> {
                if (Const.type == "10") {
                    mTextView_1.text = context.getString(R.string.frg_ps_qingdu)
                    mTextView_3.text = context.getString(R.string.frg_ps_qiangli)
                } else {
                    mTextView_1.text = context.getString(R.string.i_js)
                    mTextView_3.text = context.getString(R.string.hight)
                }

            }
        }
        mLinearLayout_1.setBackgroundColor(Color.parseColor("#00000000"))
        mLinearLayout_2.setBackgroundColor(Color.parseColor("#00000000"))
        mLinearLayout_3.setBackgroundColor(Color.parseColor("#00000000"))
        when (selction) {
            0 -> mLinearLayout_1.setBackgroundColor(Color.parseColor("#33ffffff"))
            1 -> mLinearLayout_2.setBackgroundColor(Color.parseColor("#33ffffff"))
            2 -> mLinearLayout_3.setBackgroundColor(Color.parseColor("#33ffffff"))
        }
        mLinearLayout_1.setOnClickListener {
            Frame.HANDLES.sentAll(from, 0, type)
            mPopShowSet.hide()
        }
        mLinearLayout_2.setOnClickListener {
            Frame.HANDLES.sentAll(from, 1, type)
            mPopShowSet.hide()
        }
        mLinearLayout_3.setOnClickListener {
            Frame.HANDLES.sentAll(from, 2, type)
            mPopShowSet.hide()
        }
    }

}