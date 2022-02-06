package com.deepblue.cleaning.activity

import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import com.deepblue.cleaning.Const
import com.deepblue.cleaning.F
import com.deepblue.cleaning.F.go2DownloadUrl
import com.deepblue.cleaning.R
import com.deepblue.cleaning.cleanview.FunctionDialog
import com.deepblue.cleaning.frg.FrgTaskManageDetail
import com.deepblue.cleaning.service.TimeService
import com.deepblue.cleaning.utils.CommonUtil
import com.deepblue.cleaning.websocket.WebSocketClient3
import com.deepblue.library.planbmsg.JsonUtils
import com.deepblue.library.planbmsg.msg5000.GetScrubberStatusRes
import com.mdx.framework.activity.TitleAct
import com.mdx.framework.utility.Helper
import com.tencent.bugly.crashreport.CrashReport
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_welcome.*
import org.jetbrains.anko.clearTop
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.intentFor
import org.jetbrains.anko.startActivity


class WelcomeActivity : BaseActivity() {
    var overTime: Boolean = false
    var hasGo2Login: Boolean = false
    var mediaPlayer: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)
        F.saveJson("login", "")
        robotApp?.isChargeFirstOpen = true
        robotApp?.isAddWaterFirstOpen = true
        robotApp?.isDewateringFirstOpen = true
        robotApp?.isShutdownActivityShow = false
        mediaPlayer = MediaPlayer()
        val uri = "android.resource://" + packageName.toString() + "/" + R.raw.welcome
        CommonUtil.play(this.applicationContext, mediaPlayer!!, sv_welcome, uri)
        startService(Intent(this, TimeService::class.java))
        Handler().postDelayed({
            overTime = true
        }, 1000 * 60)

//        startActivity(intentFor<EditMapActivity>().clearTop())
//        Helper.startActivity(this, FrgTaskManageDetail::class.java, TitleAct::class.java, "mapId", 0)
    }

    override fun onMessage(message: String): Int {
        if (overTime) {
            goLogoin()
        }
        val type = super.onMessage(message)
        when (type) {
            15003 -> {
                val scrubberStatusRes = JsonUtils.fromJson(message, GetScrubberStatusRes::class.java)
                val scrubberStatus = scrubberStatusRes!!.getJson()
                scrubberStatus?.let {
                    if (it.clean_capacity > 0 || it.dirty_capacity > 0) {
                        goLogoin()
                    }
                }
            }
        }
        return type
    }

//    override fun onSocketStatus(status: Int) {
//        if (status == WebSocketClient3.CONNECT_SUCCESS) {
//            hasConnected = true
//            if (overTime) {
//                runOnUiThread {
//                    goLogoin()
//                }
//            }
//        }
//    }

    fun goLogoin() {
        if (!hasGo2Login) {
            hasGo2Login = true
            startActivity(intentFor<LoginActivity>().clearTop())
            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.stop()
        mediaPlayer?.release()
    }
}