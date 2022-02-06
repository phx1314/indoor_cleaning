package com.deepblue.cleaning.activity

import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import android.view.View
import com.deepblue.cleaning.Const
import com.deepblue.cleaning.R
import com.deepblue.cleaning.cleanview.AlertDialog
import com.deepblue.cleaning.cleanview.BToast
import com.deepblue.cleaning.cleanview.FunctionDialog
import com.deepblue.cleaning.item.PopChangeLevel
import com.deepblue.cleaning.item.PopShowChangeLevel
import com.deepblue.cleaning.req.GetRobotSpeedReq
import com.deepblue.cleaning.req.GetSpeedRes
import com.deepblue.cleaning.utils.CommonUtil
import com.deepblue.cleaning.utils.DialogUtils
import com.deepblue.library.planbmsg.JsonUtils
import com.deepblue.library.planbmsg.bean.IOState
import com.deepblue.library.planbmsg.bean.RobotStatus
import com.deepblue.library.planbmsg.msg1000.GetBatteryRes
import com.deepblue.library.planbmsg.msg2000.IOStatesReq
import com.deepblue.library.planbmsg.msg2000.LogoffReq
import com.deepblue.library.planbmsg.msg3000.ChangeModeReq
import com.deepblue.library.planbmsg.msg5000.GetScrubberStatusRes
import com.mdx.framework.activity.IndexAct
import com.mdx.framework.utility.Helper
import kotlinx.android.synthetic.main.activity_manual.*
import kotlinx.android.synthetic.main.activity_manual.img_menu
import kotlinx.android.synthetic.main.activity_manual.rl_one
import kotlinx.android.synthetic.main.activity_manual.rl_two
import kotlinx.android.synthetic.main.activity_manual.tv_two
import kotlinx.android.synthetic.main.dialog_function.*
import org.jetbrains.anko.clearTop
import org.jetbrains.anko.intentFor

class ManualActivity : BaseActivity() {
    var cleanmodel: Int = 0
    var mFunctionDialog: FunctionDialog? = null
    var type_cc = 1
    var type_xd = 1
    var mediaPlayer: MediaPlayer? = null
    var b: Int = 0
    var d: Int = 0
    var c: Int = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manual)
        isIndex = true
//        sendwebSocket(GetScrubberStatusReq().toString())
        mediaPlayer = MediaPlayer()
        val uri = "android.resource://" + packageName.toString() + "/" + R.raw.manual
        CommonUtil.play(this.applicationContext, mediaPlayer!!, sv_manual, uri)
        rl_one.setOnLongClickListener {
            if (Const.type == "10") {
                var mDialogPub = PopChangeLevel(ManualActivity@ this)
                var mPopShowSet = PopShowChangeLevel(ManualActivity@ this, it, mDialogPub)
                mDialogPub.set(0, type_cc, mPopShowSet, "ManualActivity")
                mPopShowSet.show()
                true
            } else {
                false
            }
        }
        rl_two.setOnLongClickListener {
            if (Const.fieldModel.equals(Const.INDOOR_MODEL.common_model.toString())) {
                var mDialogPub = PopChangeLevel(ManualActivity@ this)
                var mPopShowSet = PopShowChangeLevel(ManualActivity@ this, it, mDialogPub)
                mDialogPub.set(1, type_xd, mPopShowSet, "ManualActivity")
                mPopShowSet.show()
                true
            } else false
        }
        rl_one.setOnClickListener {
            changeModel(1)
        }
        rl_two.setOnClickListener {
            changeModel(2)
        }
        rl_play.setOnClickListener {
            handler.removeCallbacksAndMessages(null)
            if (rl_play.text == getString(R.string.activity_manual_kaishi)) {
                closeAllOrOpen()
                rl_play.text = getString(R.string.i_finish)
            } else {
                rl_play.text = getString(R.string.activity_manual_kaishi)
                if (Const.fieldModel == Const.INDOOR_MODEL.common_model.toString() && cleanmodel == 2) {
                    handler.postDelayed({
                        closeAll(false)
                        dialogFragment = DialogUtils.showAlert(this@ManualActivity,
                            getText(R.string.jxtd).toString(),
                            R.string.stop_xs,
                            object : AlertDialog.DialogButtonListener {
                                override fun cancel() {
                                    closeAll()
                                }

                                override fun ensure(isCheck: Boolean): Boolean {
                                    closeAll()
                                    return true
                                }
                            })
                    }, 500)

                } else closeAll()
            }
        }
        mFunctionDialog =
            FunctionDialog(this@ManualActivity, false, object : FunctionDialog.FinishBack {
                override fun dialogFinish() {
//                    closeAll()
                    finish()
                }
            })
        img_menu.setOnClickListener {
            if (rl_play.text == getString(R.string.activity_manual_kaishi)) mFunctionDialog!!.show() //工作状态下防误点
        }
        mButton_ps.setOnClickListener {
            dialogFragment = DialogUtils.showAlert(this,
                getString(R.string.is_sure_push_water),
                R.string.sure_push_water,
                object : AlertDialog.DialogButtonListener {
                    override fun cancel() {

                    }

                    override fun ensure(isCheck: Boolean): Boolean {
                        pushingWater()
                        return true
                    }
                })
        }


        if (Const.type == "10") {
            tv_one.text = getString(R.string.dust_molel)
            tv_dust.visibility = View.VISIBLE
        } else {
            tv_one.text = getString(R.string.str_gxms)
            tv_dust.visibility = View.GONE
        }

    }

    private fun closeAll(isXfClose: Boolean = true) {
        var io_states: ArrayList<IOState> = ArrayList()
        io_states.add(IOState("brush_lift_motor_set", -10000))//刷盘升降
        io_states.add(IOState("water_motor_set", 0))//喷水电机
        io_states.add(IOState("clean_valve_motor_set", 0))//清水箱阀门
        io_states.add(IOState("water_lift_motor_set", if (isXfClose) -10000 else 10000))//吸水扒升降
        io_states.add(IOState("brush_motor_set", 0))//刷盘电机
        io_states.add(IOState("sewage_valve_motor_set", 0))//污水箱阀门
        io_states.add(IOState("wind_motor_set", if (isXfClose) 0 else d))//吸风电机
        io_states.add(IOState("sewage_motor_set", 0))//污水电机
        io_states.add(IOState("uv_lamp_set", 0))
        sendwebSocket(IOStatesReq(io_states).toString())
    }


    private fun cleanStandard() {
        var io_states: ArrayList<IOState> = ArrayList()
        io_states.add(IOState("brush_lift_motor_set", 10000))//刷盘升降
        io_states.add(IOState("water_motor_set", 0))//喷水电机
        io_states.add(IOState("clean_valve_motor_set", 0))//清水箱阀门
        io_states.add(IOState("water_lift_motor_set", -10000))//吸水扒升降
        io_states.add(IOState("brush_motor_set", 4000))//刷盘电机
        io_states.add(IOState("sewage_valve_motor_set", 0))//污水箱阀门
        io_states.add(IOState("wind_motor_set", 10000))//吸风电机
        io_states.add(IOState("sewage_motor_set", 0))//污水电机
        io_states.add(IOState("uv_lamp_set", 0))
        sendwebSocket(IOStatesReq(io_states).toString())
    }

    private fun GX() {
        var io_states: ArrayList<IOState> = ArrayList()
        io_states.add(IOState("brush_lift_motor_set", -10000))//刷盘升降
        io_states.add(IOState("water_motor_set", 0))//喷水电机
        io_states.add(IOState("clean_valve_motor_set", 0))//清水箱阀门
        io_states.add(IOState("water_lift_motor_set", 10000))//吸水扒升降
        io_states.add(IOState("brush_motor_set", 0))//刷盘电机
        io_states.add(IOState("sewage_valve_motor_set", 0))//污水箱阀门
        io_states.add(IOState("wind_motor_set", 10000))//吸风电机
        io_states.add(IOState("sewage_motor_set", 0))//污水电机
        io_states.add(IOState("uv_lamp_set", 0))
        sendwebSocket(IOStatesReq(io_states).toString())
    }

    private fun washFloor(a: Int, b: Int, c: Int, d: Int) {
        var io_states: ArrayList<IOState> = ArrayList()
        io_states.add(IOState("brush_lift_motor_set", 10000))//刷盘升降 端口取值 -10000~10000。正数为降，负数为升
        io_states.add(IOState("water_lift_motor_set", 10000))//吸水扒升降 端口取值 -10000~10000。正数为降，负数为升
        io_states.add(IOState("sewage_valve_motor_set", 0))//污水箱阀门
        io_states.add(IOState("water_motor_set", a))//喷水电机
        io_states.add(IOState("sewage_motor_set", 0))//污水电机
        io_states.add(IOState("clean_valve_motor_set", b))//清水箱阀门
        io_states.add(IOState("brush_motor_set", c))//刷盘电机
        io_states.add(IOState("wind_motor_set", d))//吸风电机
        io_states.add(IOState("uv_lamp_set", 0))
        sendwebSocket(IOStatesReq(io_states).toString())
        this.b = b
        this.d = d
        this.c = d
    }

    private fun washStoneFloor() {
        var io_states: ArrayList<IOState> = ArrayList()
        io_states.add(IOState("brush_lift_motor_set", 10000))//刷盘升降 端口取值 -10000~10000。正数为降，负数为升
        io_states.add(IOState("water_lift_motor_set", -10000))//吸水扒升降 端口取值 -10000~10000。正数为降，负数为升
        io_states.add(IOState("sewage_valve_motor_set", 0))//污水箱阀门
        io_states.add(IOState("water_motor_set", 0))//喷水电机
        io_states.add(IOState("sewage_motor_set", 8000))//污水电机
        io_states.add(IOState("clean_valve_motor_set", 0))//清水箱阀门
        io_states.add(IOState("brush_motor_set", 8000))//刷盘电机
        io_states.add(IOState("wind_motor_set", 0))//吸风电机
        io_states.add(IOState("uv_lamp_set", 0))
        sendwebSocket(IOStatesReq(io_states).toString())
    }

    fun closeAllOrOpen() {
        when (cleanmodel) {
            1 -> {//除尘
                if (Const.type == "10") {
                    if (type_cc == 0) {//轻度
                        closeAll()
                    } else if (type_cc == 1) {//标准:刷盘、刷盘电机、吸风电机开
                        cleanStandard()
                    }
                } else {//干吸
                    GX()
                }
            }
            2 -> {//洗地
                if (Const.fieldModel.equals(Const.INDOOR_MODEL.stone_model.toString())) {
                    washStoneFloor()
                } else {
                    if (type_xd == 0) {
                        washFloor(2666, 66, 2666, 10000)
                    } else if (type_xd == 1) {
                        washFloor(5333, 88, 5333, 10000)
                    } else if (type_xd == 2) {
                        washFloor(8000, 100, 8000, 10000)
                    }
                }

            }
        }
    }


    fun type2text(type: Int): String =
        when (type) {
            0 -> getString(R.string.item_pop_change_level_qingdu)
            1 -> getString(R.string.item_pop_change_level_biaozhun)
            2 -> getString(R.string.item_pop_change_level_qiangli)
            else -> ""
        }

    override fun disposeMsg(type: Int, obj: Any) {
        when (type) {
            0, 1, 2 -> {
                if (obj.toString().toInt() == 0) {
                    type_cc = type
                    tv_dust.text = type2text(type)
                    changeModel(1)
                } else {
                    type_xd = type
                    tv_clean.text = type2text(type)
                    changeModel(2)
                }
                if (rl_play.text == getString(R.string.i_finish)) {
                    handler.removeCallbacksAndMessages(null)
                    closeAllOrOpen()
                }
            }
            3 -> {
                rl_play.text = getString(R.string.activity_manual_kaishi)
            }
            110 -> {
                sendwebSocket(ChangeModeReq("manual").toString())
            }

        }

    }

    private fun pushingWater() {
        if (Const.robotPlayStatus != RobotStatus.STATUS_IDLE) {
            BToast.showText(getString(R.string.str_fdm))
            return
        }
        openOrClosePushWater(true)
        dialogFragment = DialogUtils.showAlert(this,
            getString(R.string.ing_push_water),
            R.string.done_push_water,
            object : AlertDialog.DialogButtonListener {
                override fun cancel() {
                    openOrClosePushWater(false)
                }

                override fun ensure(isCheck: Boolean): Boolean {
                    openOrClosePushWater(false)
                    return true
                }
            })
    }

    fun openOrClosePushWater(open: Boolean) {
        var io_states: ArrayList<IOState> = ArrayList()
        io_states.add(IOState("sewage_valve_motor_set", if (open) 100 else 0))
//        io_states.add(IOState("sewage_motor_set", if (open) 8000 else 0))
        sendwebSocket(IOStatesReq(io_states).toString())
    }

    override fun onResume() {
        super.onResume()
        sendwebSocket(ChangeModeReq("manual").toString())
        sendwebSocket(GetRobotSpeedReq("start").toString())
        changeModel(cleanmodel)
        Const.robotStatus = "manual"
    }

    private fun changeModel(model: Int) {
        if (model != 0) rl_play.isEnabled = true
        cleanmodel = model
        if (Const.fieldModel.equals(Const.INDOOR_MODEL.stone_model.toString())) {
            img_two.setImageResource(R.drawable.ic_icon_stone)
            tv_two.text = getText(R.string.stone_clean)
            tv_clean.visibility = View.GONE
            rl_one.setBackgroundResource(R.drawable.btn_select_stone)
            rl_two.setBackgroundResource(R.drawable.btn_select_stone)

        } else {//小浣熊没有石材模式
            img_two.setImageResource(R.drawable.ic_icon_clean)
            tv_two.text = getText(R.string.clean_model)
            tv_clean.visibility = View.VISIBLE
            rl_one.setBackgroundResource(R.drawable.btn_select)
            rl_two.setBackgroundResource(R.drawable.btn_select)
        }
        rl_one.isSelected = model == 1
        rl_two.isSelected = model == 2
    }

    override fun onMessage(message: String): Int {
        val type = super.onMessage(message)
        when (type) {
            15003 -> {
                val scrubberStatusRes =
                    JsonUtils.fromJson(message, GetScrubberStatusRes::class.java)
                val scrubberStatus = scrubberStatusRes!!.getJson()
                scrubberStatus!!.run {
                    wv_clean_water_manual.waveHeightPercent = clean_capacity / 100f
                    wv_dirty_water_manual.waveHeightPercent = dirty_capacity / 100f
                    tv_clean_water_manual.text = "$clean_capacity%"
                    tv_dirty_water_manual.text = "$dirty_capacity%"
                }
            }
            11011 -> {
                val mGetSpeedRes =
                    JsonUtils.fromJson(message, GetSpeedRes::class.java)
                val mRobotSpeed = mGetSpeedRes!!.getJson()
                mRobotSpeed?.let {
                    if (Const.fieldModel == Const.INDOOR_MODEL.common_model.toString() && cleanmodel == 2 && rl_play.text == getString(R.string.i_finish)) {//普通洗地模式,工作中
                        if (it.vx == 0.0 && it.vy == 0.0 && it.vth == 0.0) {//停止
                            var io_states: ArrayList<IOState> = ArrayList()
                            io_states.add(IOState("clean_valve_motor_set", 0))//清水箱阀门
                            sendwebSocket(IOStatesReq(io_states).toString())
                        } else {//运行中
                            var io_states: ArrayList<IOState> = ArrayList()
                            io_states.add(IOState("clean_valve_motor_set", b))//清水箱阀门
                            sendwebSocket(IOStatesReq(io_states).toString())
                        }
                    }
                }
            }
        }
        return type
    }

    override fun onStop() {
        super.onStop()
        closeAll()
        sendwebSocket(GetRobotSpeedReq("stop").toString())
    }

    override fun onDestroy() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        super.onDestroy()
    }
}