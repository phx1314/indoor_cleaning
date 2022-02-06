//
//  FrgTaskManage
//
//  Created by 86139 on 2020-10-26 15:17:29
//  Copyright (c) 86139 All rights reserved.


/**

 */

package com.deepblue.cleaning.frg;

import android.os.Bundle;

import com.deepblue.cleaning.R;

import android.widget.RelativeLayout;
import android.widget.GridView;
import com.deepblue.cleaning.ada.AdaTaskManage
import com.deepblue.library.planbmsg.JsonUtils
import com.deepblue.library.planbmsg.msg3000.GetAllMapsReq
import com.deepblue.library.planbmsg.msg3000.GetAllMapsRes
import kotlinx.android.synthetic.main.frg_task_manage.*


class FrgTaskManage : BaseFrg() {

    override fun create(savedInstanceState: Bundle?) {
        setContentView(R.layout.frg_task_manage)
    }

    override fun initView() {
        back_rl.setOnClickListener {
            finish()
        }
        mImageView_refeash.setOnClickListener {
            sendwebSocket(GetAllMapsReq(true).toString(), true)
        }
    }

    override fun loaddata() {
        sendwebSocket(GetAllMapsReq(true).toString(), true)

    }

    override fun disposeMsg(type: Int, obj: Any?) {
        super.disposeMsg(type, obj)
        var message = obj.toString()
        when (type) {
            13003 -> {
                dismissWaite()
                val mapJson =
                    JsonUtils.fromJson(message, GetAllMapsRes::class.java)
                val data = mapJson?.getJson()
                mGridView.adapter = AdaTaskManage(context, data?.maps)
            }
        }

    }

    override fun onSuccess(data: String?, method: String) {
    }

}