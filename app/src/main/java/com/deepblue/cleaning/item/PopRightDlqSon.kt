//
//  PopRightDlqSon
//
//  Created by 86139 on 2020-10-27 09:41:07
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
import android.widget.TextView;
import com.deepblue.cleaning.ada.position_xz
import com.deepblue.library.planbmsg.bean.MapRange
import com.deepblue.library.planbmsg.bean.WayPoint
import kotlinx.android.synthetic.main.item_pop_right_dlq_son.view.*


class PopRightDlqSon(context: Context?) : BaseItem(context) {
    init {
        val flater = LayoutInflater.from(context)
        flater.inflate(R.layout.item_pop_right_dlq_son, this)
    }

    fun set(item: Any, position: Int) {
        if (item is WayPoint) {
            mTextView.text = item.name
        } else if (item is MapRange) {
            mTextView.text = item.name
        }


        if (position == position_xz) {
            mTextView.setBackgroundColor(Color.parseColor("#66499CFC"))
        } else {
            mTextView.setBackgroundColor(Color.parseColor("#B3499CFC"))
        }
    }

}