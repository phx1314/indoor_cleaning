//
//  FrgQyrw
//
//  Created by 86139 on 2020-10-14 08:34:57
//  Copyright (c) 86139 All rights reserved.


/**

 */

package com.deepblue.cleaning.frg;

import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.TextView
import com.deepblue.cleaning.Const
import com.deepblue.cleaning.F
import com.deepblue.cleaning.F.showKeyBord
import com.deepblue.cleaning.R
import com.deepblue.cleaning.req.GetRobotRes
import com.deepblue.cleaning.req.KeyState
import com.deepblue.cleaning.req.SetRobotReq
import com.deepblue.library.planbmsg.JsonUtils
import com.deepblue.library.planbmsg.Response
import kotlinx.android.synthetic.main.frg_qyrw.*


class FrgQyrw : BaseFrg() {

    override fun create(savedInstanceState: Bundle?) {
        setContentView(R.layout.frg_qyrw)
    }

    override fun initView() {

        mTextView_psdkg.setOnClickListener {
            mLinearLayout_warning.visibility = View.GONE
            showKeyBord(it as TextView, context!!, mLinearLayout, this.javaClass.simpleName)
        }
        mTextView_spdjkg.setOnClickListener {
            mLinearLayout_warning.visibility = View.GONE
            showKeyBord(it as TextView, context!!, mLinearLayout, this.javaClass.simpleName)
        }
        mTextView_xfdjkg.setOnClickListener {
            mLinearLayout_warning.visibility = View.GONE
            showKeyBord(it as TextView, context!!, mLinearLayout, this.javaClass.simpleName)
        }
        mTextView_sps.setOnClickListener {
            mLinearLayout_warning.visibility = View.GONE
            showKeyBord(it as TextView, context!!, mLinearLayout, this.javaClass.simpleName)
        }
        mTextView_xsps.setOnClickListener {
            mLinearLayout_warning.visibility = View.GONE
            showKeyBord(it as TextView, context!!, mLinearLayout, this.javaClass.simpleName)
        }
        mTextView_pwbg.setOnClickListener {
            mLinearLayout_warning.visibility = View.GONE
            showKeyBord(it as TextView, context!!, mLinearLayout, this.javaClass.simpleName)
        }

        if (Const.type == "10") mLinearLayout_pwbg.visibility = View.VISIBLE
    }

    override fun disposeMsg(type: Int, obj: Any?) {
        var message = obj.toString()
        when (type) {
            110 -> {
                var tv = obj as TextView
                if (!TextUtils.isEmpty(tv.text.toString()) && F.isNumber(tv.text.toString())) {
                    var mKeyStates = ArrayList<KeyState>()
                    if (tv.id == mTextView_psdkg.id) {
                        mKeyStates.add(KeyState("water_motor_preclose", mTextView_psdkg.text.toString()))
                    } else if (tv.id == mTextView_spdjkg.id) {
                        mKeyStates.add(KeyState("brush_motor_preclose", mTextView_spdjkg.text.toString()))
                    } else if (tv.id == mTextView_xfdjkg.id) {
                        mKeyStates.add(KeyState("wind_motor_preclose", mTextView_xfdjkg.text.toString()))
                    } else if (tv.id == mTextView_sps.id) {
                        mKeyStates.add(KeyState("brush_lift_preclose", mTextView_sps.text.toString()))
                    } else if (tv.id == mTextView_xsps.id) {
                        mKeyStates.add(KeyState("water_lift_preclose", mTextView_xsps.text.toString()))
                    } else if (tv.id == mTextView_pwbg.id) {
                        mKeyStates.add(KeyState("sewage_motor_preclose", mTextView_pwbg.text.toString()))
                    }
                    sendwebSocket(SetRobotReq(mKeyStates).toString())
                } else {
                    mLinearLayout_warning.visibility = View.VISIBLE
                }


            }
            111 -> {//接收数据
                val mGetRobotRes = obj as GetRobotRes

                mGetRobotRes?.getJson()?.toMutableList()?.forEach {
                    if (it.key == "water_motor_preclose") {
                        mTextView_psdkg.text = String.format("%.2f", it.value.toFloat())
                    }
                    if (it.key == "brush_motor_preclose") {
                        mTextView_spdjkg.text = String.format("%.2f", it.value.toFloat())
                    }
                    if (it.key == "wind_motor_preclose") {
                        mTextView_xfdjkg.text = String.format("%.2f", it.value.toFloat())
                    }
                    if (it.key == "brush_lift_preclose") {
                        mTextView_sps.text = String.format("%.2f", it.value.toFloat())
                    }
                    if (it.key == "water_lift_preclose") {
                        mTextView_xsps.text = String.format("%.2f", it.value.toFloat())
                    }
                    if (it.key == "sewage_motor_preclose") {
                        mTextView_pwbg.text = String.format("%.2f", it.value.toFloat())
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