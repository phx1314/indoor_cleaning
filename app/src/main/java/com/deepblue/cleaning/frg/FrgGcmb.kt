//
//  FrgGcmb
//
//  Created by 86139 on 2020-10-13 13:57:55
//  Copyright (c) 86139 All rights reserved.


/**

 */

package com.deepblue.cleaning.frg;

import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.view.WindowManager
import android.widget.EditText
import android.widget.ImageView
import com.deepblue.cleaning.Const
import com.deepblue.cleaning.F.showKeyBord
import com.deepblue.cleaning.R
import com.deepblue.library.planbmsg.JsonUtils
import com.deepblue.library.planbmsg.bean.IOState
import com.deepblue.library.planbmsg.msg2000.GetSelNumReq
import com.deepblue.library.planbmsg.msg2000.GetSelNumRes
import com.deepblue.library.planbmsg.msg2000.IOStatesReq
import com.deepblue.library.planbmsg.msg2000.SetSerialNumberReq
import kotlinx.android.synthetic.main.frg_gcmb.*
import java.lang.reflect.Method


class FrgGcmb : BaseFrg() {
    private var io_states: ArrayList<IOState> = ArrayList()
    private var isBrushRise = true
    private var isWaterSprayMotorOn = false
    private var isCleanWaterTankValveOn = false;
    private var isBurdickLampValveOn = false;
    private var isSqueegeeRise = true
    private var isBrushMotorOn = false
    private var isSewageTankValveOn = false
    private var isPtOn = false
    private var isSuctionMotorOn = false
    override fun create(savedInstanceState: Bundle?) {
        setContentView(R.layout.frg_gcmb)
    }

    override fun initView() {

        brush_switch_iv.setOnClickListener(this)
        water_spray_motor_iv.setOnClickListener(this)
        clean_water_tank_valve_iv.setOnClickListener(this)
        squeegee_iv.setOnClickListener(this)
        brush_motor_iv.setOnClickListener(this)
        sewage_tank_valve_iv.setOnClickListener(this)
        suction_motor_iv.setOnClickListener(this)
        edit_sn_iv.setOnClickListener(this)
        mImageView_pt.setOnClickListener(this)
        burdick_lamp_valve_iv.setOnClickListener(this)
        setDefaultSwitchView()

        if (Const.type == "10") mLinearLayout_zwx.visibility = View.GONE
    }

    override fun loaddata() {

    }

    override fun disposeMsg(type: Int, obj: Any?) {
        when (type) {
            119 -> {
                edit_sn_iv.visibility = View.GONE
                sendwebSocket(SetSerialNumberReq(et_sn.text.toString()).toString().trim())
            }
            111 -> {
                et_sn.text = obj?.toString()
                if (!TextUtils.isEmpty(et_sn.text.toString().trim())) edit_sn_iv.visibility = View.GONE
            }
        }
    }

    /*** 设置开关默认 状态***/
    private fun setDefaultSwitchView() {
        closeAll()
        isBrushRise = clickChangeImg(brush_switch_iv, true, isBrushRise)
        isWaterSprayMotorOn =
            clickChangeImg(water_spray_motor_iv, false, isWaterSprayMotorOn)
        isCleanWaterTankValveOn =
            clickChangeImg(clean_water_tank_valve_iv, false, isCleanWaterTankValveOn)
        isBurdickLampValveOn =
            clickChangeImg(burdick_lamp_valve_iv, false, isBurdickLampValveOn)
        isSqueegeeRise =
            clickChangeImg(squeegee_iv, true, isSqueegeeRise)
        isBrushMotorOn =
            clickChangeImg(brush_motor_iv, false, isBrushMotorOn)
        isSewageTankValveOn =
            clickChangeImg(sewage_tank_valve_iv, false, isSewageTankValveOn)
        isPtOn =
            clickChangeImg(mImageView_pt, false, isPtOn)
        isSuctionMotorOn =
            clickChangeImg(suction_motor_iv, false, isSuctionMotorOn)
    }

    private fun clickChangeImg(
        iv: ImageView?,
        isBrush: Boolean,
        booleanState: Boolean
    ): Boolean {
        var currentState = booleanState
        //当前是否为刷盘和吸水扒
        if (isBrush) {
            if (currentState) {
                iv!!.background = activity!!.getDrawable(R.drawable.down_slide_switch)
                currentState = false
            } else {
                iv!!.background = activity!!.getDrawable(R.drawable.rise_slide_switch)
                currentState = true
            }
        } else {
            if (currentState) {
                iv!!.background = activity!!.getDrawable(R.drawable.off_slide_switch)
                currentState = false
            } else {
                iv!!.background = activity!!.getDrawable(R.drawable.on_slide_switch)
                currentState = true
            }
        }
        return currentState
    }


    override fun onClick(v: View) {
        super.onClick(v)
        when (v.id) {
            R.id.brush_switch_iv -> {
                setDeviceIO(isBrushRise, "brush_lift_motor_set", -10000, 10000)
                isBrushRise = clickChangeImg(brush_switch_iv, true, isBrushRise)
            }
            R.id.edit_sn_iv -> {
                showKeyBord(et_sn, context!!, mLinearLayout, this.javaClass.simpleName, true, true)
            }
            R.id.water_spray_motor_iv -> {
                io_states.clear()
                if (isWaterSprayMotorOn) {
                    if (!isSuctionMotorOn) {
                        io_states.add(IOState("water_motor_set", 8000))
                    } else {
//                        BToast.showText(this, getString(R.string.str_pszyzxfdjdkssy))
                        return
                    }
                } else {
                    io_states.add(IOState("water_motor_set", 0))
                }
                sendwebSocket(IOStatesReq(io_states).toString())
                isWaterSprayMotorOn =
                    clickChangeImg(water_spray_motor_iv, false, isWaterSprayMotorOn)
            }
            R.id.clean_water_tank_valve_iv -> {
                setDeviceIO(isCleanWaterTankValveOn, "clean_valve_motor_set", 100, 0)
                isCleanWaterTankValveOn =
                    clickChangeImg(clean_water_tank_valve_iv, false, isCleanWaterTankValveOn)
            }
            R.id.burdick_lamp_valve_iv -> {
                setDeviceIO(isBurdickLampValveOn, "uv_lamp_set", 100, 0)
                isBurdickLampValveOn =
                    clickChangeImg(burdick_lamp_valve_iv, false, isBurdickLampValveOn)
            }
            R.id.squeegee_iv -> {
                setDeviceIO(isSqueegeeRise, "water_lift_motor_set", -10000, 10000)
                isSqueegeeRise =
                    clickChangeImg(squeegee_iv, true, isSqueegeeRise)
            }
            R.id.mImageView_pt -> {
                setDeviceIO(isPtOn, "sewage_motor_set", 8000, 0)
                isPtOn =
                    clickChangeImg(mImageView_pt, false, isPtOn)
            }
            R.id.brush_motor_iv -> {
                setDeviceIO(isBrushMotorOn, "brush_motor_set", 8000, 0)
                isBrushMotorOn =
                    clickChangeImg(brush_motor_iv, false, isBrushMotorOn)
            }
            R.id.sewage_tank_valve_iv -> {
                setDeviceIO(isSewageTankValveOn, "sewage_valve_motor_set", 100, 0)
                isSewageTankValveOn =
                    clickChangeImg(sewage_tank_valve_iv, false, isSewageTankValveOn)
            }
            R.id.suction_motor_iv -> {
                io_states.clear()
                if (isSuctionMotorOn) {
                    io_states.add(IOState("wind_motor_set", 10000))
                } else {
                    io_states.add(IOState("wind_motor_set", 0))
                    io_states.add(IOState("water_motor_set", 0))
//                    BToast.showText(this, getString(R.string.str_xfgbspsyldgb))
                    isWaterSprayMotorOn =
                        clickChangeImg(water_spray_motor_iv, false, false)
                }
                sendwebSocket(IOStatesReq(io_states).toString())
                isSuctionMotorOn =
                    clickChangeImg(suction_motor_iv, false, isSuctionMotorOn)
            }
            else -> {
            }
        }
    }


    private fun closeAll() {
        io_states.clear()
        io_states.add(IOState("brush_lift_motor_set", -10000))
        io_states.add(IOState("water_motor_set", 0))
        io_states.add(IOState("clean_valve_motor_set", 0))
        io_states.add(IOState("water_lift_motor_set", -10000))
        io_states.add(IOState("brush_motor_set", 0))
        io_states.add(IOState("sewage_valve_motor_set", 0))
        io_states.add(IOState("wind_motor_set", 0))
        io_states.add(IOState("sewage_motor_set", 0))
        io_states.add(IOState("uv_lamp_set", 0))
        sendwebSocket(IOStatesReq(io_states).toString())

    }

    private fun setDeviceIO(b: Boolean, order_str: String, int_a: Int, int_b: Int) {
        io_states.clear()
        if (b)
            io_states.add(IOState(order_str, int_a))
        else
            io_states.add(IOState(order_str, int_b))
        sendwebSocket(IOStatesReq(io_states).toString())
    }

    override fun onSuccess(data: String?, method: String) {
    }
}