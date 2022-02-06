//
//  TaskManage
//
//  Created by 86139 on 2020-10-26 15:25:36
//  Copyright (c) 86139 All rights reserved.


/**

 */

package com.deepblue.cleaning.item;

import com.deepblue.cleaning.R;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.BitmapFactory
import android.view.LayoutInflater;
import android.view.ViewGroup;

import android.view.View;
import com.deepblue.cleaning.cleanview.RoundImageView;
import android.widget.TextView;
import com.deepblue.cleaning.utils.GlideLoader
import com.deepblue.cleaning.utils.MapUtilsB
import com.deepblue.library.planbmsg.bean.Map
import kotlinx.android.synthetic.main.item_task_manage.view.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.runOnUiThread


class TaskManage(context: Context?) : BaseItem(context) {
    init {
        val flater = LayoutInflater.from(context)
        flater.inflate(R.layout.item_task_manage, this)
    }

    fun set(item: Map) {
        if (item.map_info.bitmap == null) {
            mRoundImageView.setImageBitmap(BitmapFactory.decodeResource(context.resources, R.drawable.transpant))
            doAsync {
                MapUtilsB.mapToSmallBitmap(item)?.let {
                    context.runOnUiThread {
                        item.map_info.bitmap = it
                        mRoundImageView.setImageBitmap(it)
                    }
                }
            }
        } else {
            mRoundImageView.setImageBitmap(item.map_info.bitmap)
        }
        mTextView.text = item.map_info.map_name
    }

}