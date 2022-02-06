//
//  DialogYl
//
//  Created by 86139 on 2020-10-21 09:06:20
//  Copyright (c) 86139 All rights reserved.


/**

 */

package com.deepblue.cleaning.item;

import com.deepblue.cleaning.R;

import android.annotation.SuppressLint;
import android.app.Dialog
import android.content.Context;
import android.os.Handler
import android.view.LayoutInflater;
import android.view.ViewGroup;

import android.view.View;
import android.widget.TextView;
import android.widget.SeekBar;
import com.mdx.framework.Frame
import kotlinx.android.synthetic.main.item_dialog_yl.view.*


class DialogYl(context: Context?) : BaseItem(context), SeekBar.OnSeekBarChangeListener {
    init {
        val flater = LayoutInflater.from(context)
        flater.inflate(R.layout.item_dialog_yl, this)
    }

    fun set(item: Dialog, volume: Int) {
        mSeekBar.setOnSeekBarChangeListener(this)
        Handler().postDelayed({
            mSeekBar.progress = volume
            mTextView_num.text = "$volume%"
            if (mSeekBar.progress <= 0) mTextView_info.text = resources.getString(R.string.i_jy) else mTextView_info.text = resources.getString(R.string.i_aqqj)
        }, 300)

        mImageView_del.setOnClickListener {
            item.dismiss()
        }
    }

    fun go2RightPoint(p: Int) {
        mSeekBar.setOnSeekBarChangeListener(null)
        if (p <= 10) {
            mSeekBar.progress = 0
        } else if (p <= 30) {
            mSeekBar.progress = 20
        } else if (p <= 50) {
            mSeekBar.progress = 40
        } else if (p <= 70) {
            mSeekBar.progress = 60
        } else if (p <= 90) {
            mSeekBar.progress = 80
        } else {
            mSeekBar.progress = 100
        }
        if (mSeekBar.progress <= 0) mTextView_info.text = resources.getString(R.string.i_jy) else mTextView_info.text = resources.getString(R.string.i_aqqj)
        mTextView_num.text = "${mSeekBar.progress}%"
        Frame.HANDLES.sentAll("DeviceInfoActivity", 0, mSeekBar.progress)
        mSeekBar.setOnSeekBarChangeListener(this)
    }

    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
    }

    override fun onStartTrackingTouch(seekBar: SeekBar?) {
    }

    override fun onStopTrackingTouch(seekBar: SeekBar?) {
        go2RightPoint(seekBar?.progress ?: 0) //设置值的话这边不会触发，手动拖动才会走
    }
}