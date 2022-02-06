package com.deepblue.cleaning.activity

import android.animation.ObjectAnimator
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.text.TextUtils
import android.util.Log
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import androidx.fragment.app.FragmentActivity
import com.alibaba.fastjson.JSON
import com.deepblue.cleaning.Const
import com.deepblue.cleaning.Const.min_battery_level_to_back
import com.deepblue.cleaning.F.getJson
import com.deepblue.cleaning.R
import com.deepblue.cleaning.RobotApplication
import com.deepblue.cleaning.RobotApplication.Companion.deviceCode
import com.deepblue.cleaning.cleanview.AlertDialog
import com.deepblue.cleaning.cleanview.BToast
import com.deepblue.cleaning.cleanview.WaiteDialog
import com.deepblue.cleaning.req.GetRobotDeviceRes
import com.deepblue.cleaning.utils.CommonUtil
import com.deepblue.cleaning.utils.CommonUtil.isBackground
import com.deepblue.cleaning.utils.DialogFragment
import com.deepblue.cleaning.utils.DialogUtils
import com.deepblue.cleaning.version.HandleDeviceActiveReq
import com.deepblue.cleaning.version.HandleDeviceActiveRes
import com.deepblue.cleaning.version.HttpManager
import com.deepblue.cleaning.websocket.SocketMessageCallback
import com.deepblue.library.planbmsg.HeartbeatRes
import com.deepblue.library.planbmsg.JsonUtils
import com.deepblue.library.planbmsg.Response
import com.deepblue.library.planbmsg.bean.ErrorInfo
import com.deepblue.library.planbmsg.bean.IOState
import com.deepblue.library.planbmsg.bean.RobotStatus
import com.deepblue.library.planbmsg.msg1000.*
import com.deepblue.library.planbmsg.msg2000.IOStatesReq
import com.deepblue.library.planbmsg.msg2000.PlayVoiceReq
import com.deepblue.library.planbmsg.msg2000.SendUidReq
import com.deepblue.library.planbmsg.msg3000.ChangeModeReq
import com.deepblue.library.planbmsg.msg3000.SendGoalReq
import com.deepblue.library.planbmsg.msg3000.SendGoalRes
import com.deepblue.library.planbmsg.msg4000.ChangeTaskStatusReqClean
import com.deepblue.library.planbmsg.msg5000.GetScrubberStatusRes
import com.deepblue.library.planbmsg.msg5000.GetScrubberWorkModeRes
import com.deepblue.library.planbmsg.push.ErrorRealRes
import com.mdx.framework.F
import com.mdx.framework.Frame
import com.mdx.framework.utility.Helper
import com.mdx.framework.utility.handle.MHandler
import kotlinx.android.synthetic.main.activity_device_info.*
import kotlinx.android.synthetic.main.layout_lock.*
import org.jetbrains.anko.clearTop
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.intentFor
import org.jetbrains.anko.startActivity

open class BaseActivity : FragmentActivity(), SocketMessageCallback, View.OnClickListener,
    RobotApplication.ActivityCallBack {
    var robotApp: RobotApplication? = null
    var objectAnimator: ObjectAnimator? = null
    var mWaiteDialog: WaiteDialog? = null
    var handler = MHandler()
    val className = this.javaClass.simpleName
    var exitTime = 0L
    var isIndex = false
    var dialogFragment: DialogFragment? = null
    var mRunnable: Runnable? = null
    var message = ""
    var taskReportRes_message = ""
    var errorRealRes_message = ""
    var type = "C"
    var addWaterCount = 0//防抖
    var pushWaterCount = 0//防抖
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        handler.setId(className)
        handler.setMsglisnener { msg ->
            when (msg.what) {
                201 -> this@BaseActivity.disposeMsg(msg.arg1, msg.obj)
                0 -> finish()
            }
        }
        if (Frame.HANDLES.get(className).size > 0) {
            Frame.HANDLES.get(className).forEach {
                Frame.HANDLES.remove(it)
            }
        }
        Frame.HANDLES.add(handler)
        mRunnable = Runnable {
            if (mWaiteDialog != null && mWaiteDialog!!.isShowing) {
                BToast.showText(
                    text =
                    getString(R.string.i_qqcs)
                )
                dismissWaite()
            }
        }
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        robotApp = application as RobotApplication
        robotApp?.lockTime = 0
        mWaiteDialog = WaiteDialog(this)
//        hideBottomUIMenu()
    }

    private fun closeAll() {
        var io_states: ArrayList<IOState> = ArrayList()
        io_states.add(IOState("brush_lift_motor_set", -10000))//刷盘升降
        io_states.add(IOState("water_motor_set", 0))//喷水电机
        io_states.add(IOState("clean_valve_motor_set", 0))//清水箱阀门
        io_states.add(IOState("water_lift_motor_set", -10000))//吸水扒升降
        io_states.add(IOState("brush_motor_set", 0))//刷盘电机
        io_states.add(IOState("sewage_valve_motor_set", 0))//污水箱阀门
        io_states.add(IOState("wind_motor_set", 0))//吸风电机
        io_states.add(IOState("sewage_motor_set", 0))//污水电机
        io_states.add(IOState("uv_lamp_set", 0))
        sendwebSocket(IOStatesReq(io_states).toString())
        Frame.HANDLES.sentAll("ManualActivity", 3, 0)
    }

    //隐藏SystemUI //会导致个别机子卡死
    private fun hideNavigation() {
        try {
            val command = "LD_LIBRARY_PATH=/vendor/lib:/system/lib service call activity 42 s16 com.android.systemui"
            val proc = Runtime.getRuntime().exec(arrayOf("su", "-c", command))
            proc.waitFor()
        } catch (ex: java.lang.Exception) {
        }
    }

    fun updateApk() {
        if (TextUtils.isEmpty(deviceCode)) {
            val request = HandleDeviceActiveReq(versionNum = RobotApplication.VERSION_NUM)
            val url = Const.URL_CLOUD + request.path
            HttpManager.getInstance()
                .post(url, request.toRequestBody(), "", object : HttpManager.Listener {
                    override fun onSuccess(response: String) {
                        val res =
                            JsonUtils.fromJson(response, HandleDeviceActiveRes::class.java)
                                ?: return
                        if (res.code == "1111") {
                            doAsync {
                                val device = res.getData() ?: return@doAsync
                                val sendUid = SendUidReq(device.deviceCode).toString()
                                com.deepblue.cleaning.F.saveJson("deviceCode", device.deviceCode)
                                sendwebSocket(sendUid)
                            }
                        } else {
                        }
                    }

                    override fun onFailure(e: Exception?) {
                    }
                })
        } else {
            val sendUid = SendUidReq(deviceCode).toString()
            sendwebSocket(sendUid)
        }

    }

    protected open fun hideBottomUIMenu() {
//        //隐藏虚拟按键，并且全屏 触摸显示
        if (Build.VERSION.SDK_INT > 11 && Build.VERSION.SDK_INT < 19) { // lower api
            val v = this.window.decorView
            v.systemUiVisibility = View.GONE
        } else if (Build.VERSION.SDK_INT >= 19) {
            //for new api versions.
            val decorView = window.decorView
            val uiOptions = (View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or View.SYSTEM_UI_FLAG_FULLSCREEN)
            decorView.systemUiVisibility = uiOptions
        }
//        //隐藏虚拟按键，并且全屏
//        if (Build.VERSION.SDK_INT > 11 && Build.VERSION.SDK_INT < 19) { // lower api
//            val v = this.window.decorView
//            v.systemUiVisibility = View.GONE
//        } else if (Build.VERSION.SDK_INT >= 19) {
//            val _window: Window = window
//            val params: WindowManager.LayoutParams = _window.getAttributes()
//            params.systemUiVisibility =
//                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_IMMERSIVE
//            _window.setAttributes(params)
//        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        return if (keyCode == 4 && event.action == 0 && isIndex) {
            if (System.currentTimeMillis() - exitTime > 2000L) {
                BToast.showText(getString(R.string.i_zayctccx))
                exitTime = System.currentTimeMillis()
            } else {
                finish()
                Frame.finish()
            }
            true
        } else {
            super.onKeyDown(keyCode, event)
        }
    }

    open fun disposeMsg(type: Int, obj: Any) {}
    fun hideDialog() {
        if (dialogFragment?.dialog?.isShowing == true && (dialogFragment?.id ?: 0 >= 1)) {
            if (className == "ManualActivity") Frame.HANDLES.sentAll("ManualActivity", 110, "") //弹框消失如果是在手动模式页面需要切换手动模式
            robotApp!!.lockTime = 0 //锁屏时间重置0
            playPubVoice((dialogFragment!!.dialog!! as AlertDialog).type, false)//这边做语音播报提示  根据type判断类型
            Handler().postDelayed({
                dialogFragment?.dismiss()
            }, 1000)//弹框延迟一秒消失
        }
        dialogFragment?.id = 0
    }


    override fun onMessage(message: String): Int {
        val res = JsonUtils.fromJson(message, Response::class.java) ?: return 0
        robotApp!!.heartTimes = 0
        when (res.type) {
            10999 -> {
                val heartbeatRes = JsonUtils.fromJson(message, HeartbeatRes::class.java) ?: return res.type
                val json = heartbeatRes.getJson() ?: return res.type
                Const.systemTime = json.time * 1000
                cb_bar?.refData()
            }
            11015 -> {
                val hardwareStatusRes =
                    JsonUtils.fromJson(message, GetRobotDeviceRes::class.java)
                hardwareStatusRes?.getJson()?.hardwares_status?.toMutableList()?.forEach {
                    when (it.hardware_id) {
                        3 -> if (it.status != 0 && it.status != 2) Const.systemError = true
                        4 -> if (it.status != 0 && it.status != 2) Const.systemError = true
                        6 -> if (it.status != 0 && it.status != 2) Const.systemError = true
                        1 -> if (it.status != 0 && it.status != 2) Const.systemError = true
                        1605 -> if (it.status != 0 && it.status != 2) Const.systemError = true
                        1606 -> if (it.status != 0 && it.status != 2) Const.systemError = true
                        1601 -> if (it.status != 0 && it.status != 2) Const.systemError = true
                        1602 -> if (it.status != 0 && it.status != 2) Const.systemError = true
                        9 -> if (it.status != 0 && it.status != 2) Const.systemError = true
                        1603 -> if (it.status != 0 && it.status != 2) Const.systemError = true

                    }
                }
//                val hardwareStatusRes =
//                    JsonUtils.fromJson(message, GetHarewareStatusRes::class.java)
//                val hardwareStatus = hardwareStatusRes!!.getJson()
                robotApp!!.connect_status = RobotApplication.CONNECT_STATUS
                val data: Array<ErrorInfo>? = JsonUtils.fromJson(
                    robotApp!!.mSharedPreferencesHelper!!.get(
                        Const.SP_NEW_ERRORINFO,
                        ""
                    ).toString(), Array<ErrorInfo>::class.java
                )//实时故障查询
                Const.systemError = data?.isNotEmpty() == true
                cb_bar?.refData()
            }
            15005 -> {
                val mGetScrubberWorkModeRes =
                    JsonUtils.fromJson(message, GetScrubberWorkModeRes::class.java)
                Const.cleanMode = mGetScrubberWorkModeRes?.getJson()?.mode ?: "auto"
            }
            13001 -> {
                val sendGoalRes = JsonUtils.fromJson(message, SendGoalRes::class.java)
                val status = sendGoalRes!!.getJson()!!.status
                when (status) {
                    "receive" -> BToast.showText(this, getString(R.string.i_ready_go))
                    "arrive" -> {
                        BToast.showText(this, getString(R.string.i_ddmbd))
                        hideDialog()
                    }
                    "error" -> {
                        BToast.showText(this, getString(R.string.i_wfddmbd))
                        hideDialog()
                    }
                }

            }
            11004 -> {
                val robotStatusRes =
                    JsonUtils.fromJson(message, GetRobotStatusRes::class.java) ?: return 0
                if (robotStatusRes.getJson()!!.status != Const.robotPlayStatus) {
                    Const.robotPlayStatus = robotStatusRes.getJson()!!.status
                    if (Const.robotPlayStatus == RobotStatus.STATUS_EMERGENCY && this@BaseActivity !is StopActivity && this@BaseActivity !is WelcomeActivity) {
                        startActivity(intentFor<StopActivity>().clearTop())
                    }
                }
                if (Const.robotPlayStatus == RobotStatus.STATUS_BREAK && this@BaseActivity is CleanMainActivity && dialogFragment?.dialog?.isShowing != true && !isBackground(className) && Const.isDdXs) {//首页判断是中断状态弹框继续任务，且在前台、没有其他弹框显示
                    dialogFragment = DialogUtils.showAlert(this,
                        getText(R.string.sure_goon_task).toString(),
                        R.string.sure_goon,
                        object : AlertDialog.DialogButtonListener {
                            override fun cancel() {
                                sendwebSocket(ChangeTaskStatusReqClean().stop())
                            }

                            override fun ensure(isCheck: Boolean): Boolean {
                                sendwebSocket(ChangeModeReq("auto").toString())
                                sendwebSocket(ChangeTaskStatusReqClean().resume(0))
                                startActivity<PlayActivity>()
                                return true
                            }
                        })

                    Const.isDdXs = false
                }
                if (dialogFragment?.dialog?.isShowing == true && dialogFragment?.id == 2 && Const.robotPlayStatus == RobotStatus.STATUS_EMERGENCY)//有弹框时按下急停的话需要暂停ui
                    goDoTaskOrPause(false, false, type)

            }
            11001 -> {
                val robotLocRes =
                    JsonUtils.fromJson(
                        message, GetRobotLocRes::class.java
                    ) ?: return res.type
                Const.robotLoc = robotLocRes.getJson()
            }
            12003 -> {
                if (res.error_code == -2 && this !is WelcomeActivity && this !is LoginActivity) {
                    if (!TextUtils.isEmpty(getJson("login"))) {
                        sendwebSocket(getJson("login")!!)
                    } else {
//                        Helper.toast("登录失效，请重新登录")
                    }
                }
            }
            11008 -> {
                if (res.error_code == 0) {
                    val network =
                        JsonUtils.fromJson(message, GetNetworkRes::class.java) ?: return res.type
                    Const.system4G = network.getJson()!!.network_status
                }
            }
            15003 -> {
                val scrubberStatusRes =
                    JsonUtils.fromJson(message, GetScrubberStatusRes::class.java)
                val scrubberStatus = scrubberStatusRes!!.getJson()
                scrubberStatus!!.run {
                    if (dialogFragment?.dialog?.isShowing != true && !isBackground(className) && this@BaseActivity !is StandbyActivity && this@BaseActivity !is ChargeActivity && this@BaseActivity !is ShutdownActivity && this@BaseActivity !is AdminAccountActivity && this@BaseActivity !is LoginActivity && this@BaseActivity !is NeterrorActivity && this@BaseActivity !is PlayActivity && this@BaseActivity !is StopActivity && this@BaseActivity !is WelcomeActivity && this@BaseActivity !is BackToCDDActivity && this@BaseActivity !is LocCheckActivity && this@BaseActivity !is LocCheckActivity && this@BaseActivity !is EditMapActivity) {
                        if (robotApp!!.isAddWaterFirstOpen && clean_capacity < 5 && clean_capacity > 0) {
                            addWaterCount++
                            if (addWaterCount >= 5) {
                                robotApp!!.isAddWaterFirstOpen = false
                                showFillDialog("E")
                            }
                        } else {
                            addWaterCount = 0
                        }

                        if (robotApp!!.isDewateringFirstOpen && dirty_capacity > 95 && dirty_capacity <= 100) {
                            pushWaterCount++
                            if (pushWaterCount >= 6) {
                                robotApp!!.isDewateringFirstOpen = false
                                showFillDialog("H")
                            }
                        } else {
                            pushWaterCount = 0
                        }
                    } else {
                        addWaterCount = 0
                        pushWaterCount = 0
                    }
                }
            }
            11002 -> {
                if (res.error_code == 0) {
                    val robotBattery =
                        JsonUtils.fromJson(message, GetBatteryRes::class.java) ?: return res.type
                    Const.systemPower = robotBattery.getJson()!!.battery_level
                    if (Const.systemPower in 1 until min_battery_level_to_back && dialogFragment?.dialog?.isShowing != true && !isBackground(className) && this@BaseActivity !is StandbyActivity && this@BaseActivity !is ChargeActivity && this@BaseActivity !is ShutdownActivity && this@BaseActivity !is AdminAccountActivity && this@BaseActivity !is LoginActivity && this@BaseActivity !is NeterrorActivity && this@BaseActivity !is PlayActivity && this@BaseActivity !is StopActivity && this@BaseActivity !is WelcomeActivity && this@BaseActivity !is BackToCDDActivity && this@BaseActivity !is LocCheckActivity && robotApp!!.isChargeFirstOpen) {
                        robotApp!!.isChargeFirstOpen = false
                        showFillDialog("C")
                    }
                    if (robotBattery.getJson()!!.charging && this@BaseActivity !is ChargeActivity && this@BaseActivity !is WelcomeActivity) {
                        startActivity(intentFor<ChargeActivity>("SYSTEMPOWER" to Const.systemPower).clearTop())
                    }
                }
            }
            24002 -> {//实时故障推送
                doAsync {
                    if (res.error_code == 0) {
                        val errorRealRes =
                            JsonUtils.fromJson(message, ErrorRealRes::class.java)!!.getJson()
                        if (errorRealRes != null) {
                            val data: Array<ErrorInfo>? =
                                JsonUtils.fromJson(
                                    robotApp!!.mSharedPreferencesHelper!!.get(
                                        Const.SP_NEW_ERRORINFO,
                                        ""
                                    ).toString(), Array<ErrorInfo>::class.java
                                )
                            val toMutableList = data?.toMutableList() ?: ArrayList()
                            toMutableList.add(errorRealRes)
                            robotApp!!.mSharedPreferencesHelper!!.put(
                                Const.SP_NEW_ERRORINFO,
                                JSON.toJSON(toMutableList)
                            )
                        }
                    }
                }

            }

            12014 -> {

                if (this@BaseActivity !is ShutdownActivity && !robotApp!!.isShutdownActivityShow) {
                    robotApp!!.isShutdownActivityShow = true
                    startActivity(intentFor<ShutdownActivity>().clearTop())
                }
            }
//            14006 -> {
//                BToast.showText("区域路径获取数据成功")
//                Frame.HANDLES.sentAll("CleanMainActivity", res.type, message)
//            }
        }
//        Frame.HANDLES.sentAll(className, res.type, message)
        return res.type
    }

    @Synchronized
    private fun showFillDialog(type: String) {
        //这边做语音播报提示
        var title = ""
        if (type == "C") {//充电
            title = String.format(getString(R.string.i_dinaliangd_jy), "$min_battery_level_to_back%")
        } else if (type == "E") {//补水
            title = getString(R.string.i_zzfhbsd_jy)
        } else if (type == "H") {//排水
            title = getString(R.string.i_zzfhpsd_jy)
        }
        playPubVoiceTx(type)
        dialogFragment = DialogUtils.showAlert(
            this,
            title,
            R.string.i_autoback,
            object : AlertDialog.DialogButtonListener {
                override fun cancel() {
                    if (className == "ManualActivity") {
                        Frame.HANDLES.sentAll("ManualActivity", 110, "")
                    }
                }

                override fun ensure(isCheck: Boolean): Boolean {
                    sendwebSocket(ChangeModeReq("auto").toString())
                    goDoTaskOrPause(true, true, type)
                    return true
                }
            }, hasTopIcon = true, type = type
        )
    }

    fun playPubVoice(type: String, isWorking: Boolean = true) { //isWorking是否在工作，false到达目的地
        if (type == "C") {//充电
            sendwebSocket(PlayVoiceReq("", if (isWorking) 4 else 7).toString())
        } else if (type == "E") {//补水
            sendwebSocket(PlayVoiceReq("", if (isWorking) 5 else 8).toString())
        } else if (type == "H") {//排水
            sendwebSocket(PlayVoiceReq("", if (isWorking) 6 else 9).toString())
        }
    }

    fun playPubVoiceTx(type: String) {
        if (type == "C") {//充电
            sendwebSocket(PlayVoiceReq("", 1).toString())
        } else if (type == "E") {//补水
            sendwebSocket(PlayVoiceReq("", 2).toString())
        } else if (type == "H") {//排水
            sendwebSocket(PlayVoiceReq("", 3).toString())
        }
    }

    @Synchronized
    fun goDoTaskOrPause(isDoTask: Boolean, isFirst: Boolean, type: String, isFromPlay: Boolean = false) {
        try {
            this.type = type
            var title = ""
            var ierror = ""
            if (type == "C") {//充电
                title = String.format(getString(R.string.i_dinaliangd), "$min_battery_level_to_back%")
                ierror = getString(R.string.i_mycdd)
            } else if (type == "E") {//补水
                title = getString(R.string.i_zzfhbsd)
                ierror = getString(R.string.i_mybsd)
            } else if (type == "H") {//排水
                title = getString(R.string.i_zzfhpsd)
                ierror = getString(R.string.i_mypsd)
            }


            dialogFragment?.dialog?.dismiss()
            if (isFirst) {
                var hasPoint = false
                if (Const.map != null && Const.map?.points != null)
                    for (point in Const.map!!.points) {
                        if (point.type.contains(type)) {
                            playPubVoice(type) //这边做语音播报提示
                            sendwebSocket(SendGoalReq(point).toString())
                            closeAll()
                            hasPoint = true
                            break
                        }
                    }
                if (!hasPoint) {
                    dialogFragment = DialogUtils.showAlert(
                        this,
                        ierror,
                        R.string.suspend,
                        object : AlertDialog.DialogButtonListener {
                            override fun cancel() {
                                if (className == "ManualActivity") {
                                    Frame.HANDLES.sentAll("ManualActivity", 110, "")
                                }
                            }

                            override fun ensure(isCheck: Boolean): Boolean {
                                return true
                            }
                        }, hasFillPoint = false, type = type
                    )
                    return
                }
            } else {
                if (!isFromPlay) {
                    if (isDoTask) {
                        if (className == "ManualActivity") {
                            sendwebSocket(ChangeModeReq("auto").toString())
                        }
                        playPubVoice(type)//这边做语音播报提示
                        sendwebSocket(ChangeTaskStatusReqClean().resume(0))
                    } else {
                        sendwebSocket(ChangeTaskStatusReqClean().pause(0))
                    }
                }
            }
            dialogFragment = DialogUtils.showAlert(
                this,
                title,
                if (isDoTask) R.string.suspend else R.string.continues,
                object : AlertDialog.DialogButtonListener {
                    override fun cancel() {
                        if (this@BaseActivity is PlayActivity) {
                            if (Const.robotPlayStatus == RobotStatus.STATUS_GO_BREAK) {
                                if (type == "C") {//充电
                                    sendwebSocket(ChangeTaskStatusReqClean().stop())
                                } else if (type == "E") {//补水
                                    sendwebSocket(ChangeTaskStatusReqClean().stop())
                                } else if (type == "H") {//排水
                                    sendwebSocket(ChangeTaskStatusReqClean().stop())
                                }
                                Frame.HANDLES.sentAll("PlayActivity", 701, "")
                            }
                        } else {
                            sendwebSocket(ChangeTaskStatusReqClean().stop())
                        }
                        if (className == "ManualActivity") {
                            Frame.HANDLES.sentAll("ManualActivity", 110, "")
                        }
                    }

                    override fun ensure(isCheck: Boolean): Boolean {
                        goDoTaskOrPause(!isDoTask, false, type)
                        return true
                    }
                }, hasTopIcon = true, type = type
            )
            dialogFragment?.id = 2 //非手动情况下弹框id是2
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun sendwebSocket(message: String) {
        robotApp!!.webSocketClient!!.sendMessage(message)
    }

    override fun onSocketStatus(status: Int) {
    }

    override fun stopHeart() {
        if (!isBackground(className) && this@BaseActivity !is NeterrorActivity && this@BaseActivity !is WelcomeActivity && this@BaseActivity !is ShutdownActivity) {
            startActivity(intentFor<NeterrorActivity>().clearTop())
        }
    }

    override fun backLoack() {
        if (dialogFragment?.dialog?.isShowing == true && !(this@BaseActivity is CleanMainActivity && dialogFragment?.id == 1)) {
            robotApp!!.lockTime = 0
            return
        }
        if (!isBackground(className) && !TextUtils.isEmpty(getJson("login")) && this@BaseActivity !is StandbyActivity && this@BaseActivity !is ChargeActivity && this@BaseActivity !is ShutdownActivity && this@BaseActivity !is AdminAccountActivity && this@BaseActivity !is LoginActivity && this@BaseActivity !is NeterrorActivity && this@BaseActivity !is PlayActivity && this@BaseActivity !is StopActivity && this@BaseActivity !is WelcomeActivity && this@BaseActivity !is BackToCDDActivity && this@BaseActivity !is ManualActivity && this@BaseActivity !is CreateMapActivity) {
            if (this@BaseActivity is CleanMainActivity && dialogFragment?.id == 1) {
                startActivity(intentFor<StandbyActivity>("currentType" to currentType).clearTop())
            } else {
                startActivity(intentFor<StandbyActivity>().clearTop())
            }


        }

        if (!isBackground(className) && this@BaseActivity is PlayActivity) {
            Frame.HANDLES.close("BackToCDDActivity")
            startActivity(
                intentFor<BackToCDDActivity>(
                    "message" to message,
                    "taskReportRes_message" to taskReportRes_message,
                    "errorRealRes_message" to errorRealRes_message
                ).clearTop()
            )
        }
    }

    fun showWaite(delayMillis: Long = 20000) {
        if (mWaiteDialog != null && !mWaiteDialog!!.isShowing && !isFinishing) {
            mWaiteDialog!!.show(getText(R.string.loading).toString())
            handler.removeCallbacks(mRunnable)
            handler.postDelayed(mRunnable, delayMillis)
        }
    }

    fun showWaite(delayMillis: Long = 20000, title: String) {
        if (mWaiteDialog != null && !mWaiteDialog!!.isShowing && !isFinishing) {
            mWaiteDialog!!.show(title)
            handler.removeCallbacks(mRunnable)
            handler.postDelayed(mRunnable, delayMillis)
        }


    }

    fun dismissWaite() {
        if (mWaiteDialog != null && mWaiteDialog!!.isShowing) {
            mWaiteDialog!!.dismiss()
        }
    }


    override fun onDestroy() {
        dismissWaite()
        robotApp!!.lockTime = 0
        handler.removeCallbacksAndMessages(null)
        Frame.HANDLES.remove(this.handler)
        super.onDestroy()
    }

    override fun onClick(v: View) {
        robotApp!!.lockTime = 0
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        when (ev.action) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE ->                 //有按下动作时取消定时
                robotApp!!.lockTime = 0
        }
        return super.dispatchTouchEvent(ev)
    }

    override fun onPause() {
        super.onPause()
    }

    fun timeFormat(seconds: Int): String {
        if (seconds == 0) {
            return "0 min"
        } else if (seconds <= 60) {
            return "1 min"
        }
        var minutes = seconds / 60
        if (minutes < 60) {
            return "$minutes min"
        }
        val hours = minutes / 60
        minutes %= 60
        var second = seconds % 60
        return "$hours h " + (if (second > 0) "${minutes++}" else "$minutes") + " min"
    }

//    fun timeFormat(seconds: Int): String {
//        var second = seconds % 60
//        var minute = seconds % 3600 / 60
//        val hour = seconds / 3600
//        minute %= 60
//        return (if (hour > 0) "$hour h " else "") + (if (minute > 0) "$minute min " else "") + "$second s"
//    }

    fun areaFormat(area: Double): String {
        return if (area == 0.0) {
            "0"
        } else if (area <= 1) {
            "1"
        } else {
            F.go2Wei(area)
        }
    }

    override fun onResume() {
        robotApp!!.lockTime = 0
        robotApp!!.webSocketClient!!.setSocketCallBack(this)//这个地方的设置关系到消息是否能及时收到
        robotApp!!.setActivityCallBack(this)
        cb_bar?.refData()
        super.onResume()
    }

}