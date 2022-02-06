//
//  FrgSd
//
//  Created by 86139 on 2020-10-14 08:53:16
//  Copyright (c) 86139 All rights reserved.


/**

 */

package com.deepblue.cleaning.frg;

import android.os.Bundle;
import android.text.TextUtils
import android.view.View

import com.deepblue.cleaning.R;

import android.widget.TextView;
import com.deepblue.cleaning.F
import com.deepblue.cleaning.req.GetRobotRes
import com.deepblue.cleaning.req.KeyState
import com.deepblue.cleaning.req.SetRobotReq
import com.deepblue.library.planbmsg.JsonUtils
import com.deepblue.library.planbmsg.Response
import com.mdx.framework.utility.Helper
import com.mdx.framework.utility.Verify
import kotlinx.android.synthetic.main.frg_qyrw.*
import kotlinx.android.synthetic.main.frg_sd.*
import kotlinx.android.synthetic.main.frg_sd.mLinearLayout
import kotlinx.android.synthetic.main.frg_sd.mLinearLayout_warning
import kotlinx.android.synthetic.main.frg_sd.mTextView_1
import kotlinx.android.synthetic.main.frg_sd.mTextView_2
import kotlinx.android.synthetic.main.frg_sd.mTextView_3
import kotlinx.android.synthetic.main.frg_xl.*


class FrgSd : BaseFrg() {

    override fun create(savedInstanceState: Bundle?) {
        setContentView(R.layout.frg_sd)
    }

    override fun initView() {
        mTextView_1.setOnClickListener {
            mLinearLayout_warning.visibility = View.GONE
            F.showKeyBord(it as TextView, context!!, mLinearLayout, this.javaClass.simpleName)
        }
        mTextView_2.setOnClickListener {
            mLinearLayout_warning.visibility = View.GONE
            F.showKeyBord(it as TextView, context!!, mLinearLayout, this.javaClass.simpleName)
        }
        mTextView_3.setOnClickListener {
            mLinearLayout_warning.visibility = View.GONE
            F.showKeyBord(it as TextView, context!!, mLinearLayout, this.javaClass.simpleName)
        }
        mTextView_4.setOnClickListener {
            mLinearLayout_warning.visibility = View.GONE
            F.showKeyBord(it as TextView, context!!, mLinearLayout, this.javaClass.simpleName)
        }
    }

    override fun disposeMsg(type: Int, obj: Any?) {
        var message = obj.toString()
        when (type) {
            110 -> {
                var tv = obj as TextView
                if (!TextUtils.isEmpty(tv.text.toString()) && F.isNumber(tv.text.toString())) {
                    var mKeyStates = ArrayList<KeyState>()
                    if (tv.id == mTextView_1.id) {
                        mKeyStates.add(KeyState("speed_set_1", mTextView_1.text.toString()))
                    } else if (tv.id == mTextView_2.id) {
                        mKeyStates.add(KeyState("speed_set_2", mTextView_2.text.toString()))
                    } else if (tv.id == mTextView_3.id) {
                        mKeyStates.add(KeyState("speed_set_3", mTextView_3.text.toString()))
                    } else if (tv.id == mTextView_4.id) {
                        mKeyStates.add(KeyState("speed_set_trans", mTextView_4.text.toString()))
                    }
                    sendwebSocket(SetRobotReq(mKeyStates).toString())
                } else {
                    mLinearLayout_warning.visibility = View.VISIBLE
                }


            }
            111 -> {//接收数据
                val mGetRobotRes = obj as GetRobotRes
                mGetRobotRes?.getJson()?.toMutableList()?.forEach {
                    if (it.key == "speed_set_1") {
                        mTextView_1.text = String.format("%.2f", it.value.toFloat())
                    }
                    if (it.key == "speed_set_2") {
                        mTextView_2.text = String.format("%.2f", it.value.toFloat())
                    }
                    if (it.key == "speed_set_3") {
                        mTextView_3.text = String.format("%.2f", it.value.toFloat())
                    }
                    if (it.key == "speed_set_trans") {
                        mTextView_4.text = String.format("%.2f", it.value.toFloat())
                    }

                }

            }
            12030 -> {
                dismissWaite()
                val res = JsonUtils.fromJson(message, Response::class.java)
                if (res?.error_code != 0) mLinearLayout_warning.visibility = View.VISIBLE
            }

        }

    }

    override fun loaddata() {
    }

    override fun onSuccess(data: String?, method: String) {
    }

}