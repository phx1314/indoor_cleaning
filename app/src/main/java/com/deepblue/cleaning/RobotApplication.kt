package com.deepblue.cleaning

import android.app.Application
import com.deepblue.cleaning.req.GetRobotDeviceReq
import com.deepblue.cleaning.utils.SharedPreferencesHelper
import com.deepblue.cleaning.utils.UnCeHandler
import com.deepblue.cleaning.websocket.WebSocketClient3
import com.deepblue.library.planbmsg.HeartbeatReq
import com.deepblue.library.planbmsg.msg1000.GetBatteryReq
import com.deepblue.library.planbmsg.msg1000.GetHardwareStatusReq
import com.deepblue.library.planbmsg.msg1000.GetNetworkReq
import com.deepblue.library.planbmsg.msg5000.GetScrubberStatusReq
import com.deepblue.library.planbmsg.msg5000.GetScrubberWorkModeReq
import com.mdx.framework.Frame
import com.tencent.bugly.crashreport.CrashReport
import org.jetbrains.anko.doAsync

class RobotApplication : Application() {

    companion object {
//                val hostUrl = "ws://192.168.16.250:12238"//上海
        val hostUrl = "ws://192.168.2.104:12235"
        val VERSION_NUM: String = "V1.0.0"

        val DEFAULT_STATUS: Int = 0  //0默认
        val DISCONNECT_STATUS: Int = 1  //1断连
        val CONNECT_STATUS: Int = 2     //2续连
        var deviceCode = ""
    }

    var mSharedPreferencesHelper: SharedPreferencesHelper? = null

    var mActivityCallBack: ActivityCallBack? = null
    var webSocketClient: WebSocketClient3? = null
    var isDestory: Boolean = true
    var heartTimes: Long = 0
    var lockTime: Long = 0
    var needLock: Boolean = true
    var connect_status: Int = DEFAULT_STATUS
    var isChargeFirstOpen = true//第一次充电提示
    var isAddWaterFirstOpen = true//第一次加水提示
    var isDewateringFirstOpen = true//第一次排水提示
    var isShutdownActivityShow = false
    override fun onCreate() {
        super.onCreate()
        webSocketClient = WebSocketClient3.getInstance(hostUrl)
        mSharedPreferencesHelper = SharedPreferencesHelper(applicationContext, Const.SHARED_FILE)
        Frame.init(applicationContext)
        F.init()
        CrashReport.initCrashReport(applicationContext, "8630dab334", true)
        doAsync {
            while (isDestory) {
                Thread.sleep(2000)
//                if (connect_status != DISCONNECT_STATUS) {
                heartTimes++
                lockTime++
                webSocketClient!!.sendMessage(HeartbeatReq().toString())
                if (heartTimes > 10 && connect_status > 0 && mActivityCallBack != null) {
                    mActivityCallBack!!.stopHeart()
                    heartTimes = 0
                    continue
                }
                if (lockTime > 30 && mActivityCallBack != null && needLock) {
                    mActivityCallBack!!.backLoack()
                    lockTime = 0
                }
                if (webSocketClient!!.isConnected()) {
//                    Thread.sleep(100)
//                    webSocketClient!!.sendMessage(GetRobotStatusReq().toString())
                    Thread.sleep(100)
                    webSocketClient!!.sendMessage(GetNetworkReq().toString())
                    Thread.sleep(100)
                    webSocketClient!!.sendMessage(GetBatteryReq().toString())
                    Thread.sleep(100)
                    webSocketClient!!.sendMessage(GetScrubberStatusReq().toString())
                    Thread.sleep(100)
                    webSocketClient!!.sendMessage(GetScrubberWorkModeReq().toString())
                    Thread.sleep(100)
                    webSocketClient!!.sendMessage(GetRobotDeviceReq().toString())
                }
//                }

            }
        }
        Thread.setDefaultUncaughtExceptionHandler(UnCeHandler(this)) //异常被拦截导致bugly获取不到异常信息上传不了云端
    }

    fun setActivityCallBack(callback: ActivityCallBack) {
        mActivityCallBack = callback
    }

    override fun onTerminate() {
        super.onTerminate()
        isDestory = false
    }

    interface ActivityCallBack {
        fun stopHeart()
        fun backLoack()
    }
}