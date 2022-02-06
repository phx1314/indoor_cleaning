package com.deepblue.cleaning.activity

import android.os.Bundle
import android.view.KeyEvent
import com.deepblue.cleaning.Const
import com.deepblue.cleaning.R
import com.deepblue.library.planbmsg.JsonUtils
import com.deepblue.library.planbmsg.bean.RobotStatus
import com.deepblue.library.planbmsg.msg1000.GetRobotStatusRes
import com.deepblue.library.planbmsg.push.StopButtonRes
import com.mdx.framework.Frame

class StopActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stop)
    }

    override fun onMessage(message: String): Int {
        val type = super.onMessage(message)
        when (type) {
            24001 -> {
                val stopButtonRes = JsonUtils.fromJson(message, StopButtonRes::class.java)
                if (stopButtonRes != null && !stopButtonRes.getJson()!!.stop_button && !isFinishing) {
                    finish()
                }
            }
            11004 -> {
                val robotStatusRes =
                    JsonUtils.fromJson(message, GetRobotStatusRes::class.java) ?: return 0
                Const.robotPlayStatus = robotStatusRes.getJson()!!.status
                if (Const.robotPlayStatus != RobotStatus.STATUS_EMERGENCY && !isFinishing) {
                    finish()
                }
            }
        }
        return type
    }

    override fun onDestroy() {
        super.onDestroy()
        Frame.HANDLES.sentAll("PlayActivity", 701, "")
//        sendwebSocket(ChangeTaskStatusReq().resume(0))
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        return keyCode == KeyEvent.KEYCODE_BACK;
    }
}