//
//  FrgXl
//
//  Created by 86139 on 2020-10-14 08:42:18
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
import kotlinx.android.synthetic.main.frg_ps.*
import kotlinx.android.synthetic.main.frg_pw.*
import kotlinx.android.synthetic.main.frg_sd.*
import kotlinx.android.synthetic.main.frg_sd.mLinearLayout_warning
import kotlinx.android.synthetic.main.frg_sd.mTextView_2
import kotlinx.android.synthetic.main.frg_xl.mLinearLayout
import kotlinx.android.synthetic.main.frg_xl.mTextView_1


class FrgXl : BaseFrg() {

    override fun create(savedInstanceState: Bundle?) {
        setContentView(R.layout.frg_xl)
    }

    override fun initView() {
        mTextView_1.setOnClickListener {
            mLinearLayout_warning.visibility = View.GONE
            F.showKeyBord(it as TextView, context!!, mLinearLayout, this.javaClass.simpleName)
        }
    }

    override fun disposeMsg(type: Int, obj: Any?) {
        var message = obj.toString()
        when (type) {
            110 -> {
                var tv = obj as TextView
                if (!TextUtils.isEmpty(tv.text.toString())&&Verify.isNumeric(tv.text.toString()) && (tv.text.toString().toInt() in 1..8)) {
                    var mKeyStates = ArrayList<KeyState>()
                    mKeyStates.add(KeyState("wind_motor_set", mTextView_1.text.toString()))
                    sendwebSocket(SetRobotReq(mKeyStates).toString())
                } else {
                    mLinearLayout_warning.visibility = View.VISIBLE
                }


            }
            111 -> {//接收数据
                val mGetRobotRes = obj as GetRobotRes
                mGetRobotRes.getJson()?.toMutableList()?.forEach {
                    if (it.key == "wind_motor_set") {
                        mTextView_1.text = it.value
                    }
                }

            }
            12030 -> {
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