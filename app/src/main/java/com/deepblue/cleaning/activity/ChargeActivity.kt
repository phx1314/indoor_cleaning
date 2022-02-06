package com.deepblue.cleaning.activity

import android.media.MediaPlayer
import android.os.Bundle
import android.view.KeyEvent
import com.deepblue.cleaning.Const
import com.deepblue.cleaning.R
import com.deepblue.cleaning.utils.CommonUtil
import com.deepblue.cleaning.utils.CommonUtil.get
import com.deepblue.library.planbmsg.JsonUtils
import com.deepblue.library.planbmsg.Response
import com.deepblue.library.planbmsg.bean.CommonTask
import com.deepblue.library.planbmsg.msg1000.GetBatteryRes
import kotlinx.android.synthetic.main.activity_charge.*
import org.jetbrains.anko.clearTop
import org.jetbrains.anko.intentFor
import org.jetbrains.anko.startActivity

class ChargeActivity : BaseActivity() {
    var mediaPlayer: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_charge)
        val uri = "android.resource://" + packageName.toString() + "/" + R.raw.charge
        mediaPlayer = MediaPlayer()
        CommonUtil.play(applicationContext, mediaPlayer!!, sv_charge, uri)

        mTextView_dl.text = "${intent.get<Int>("SYSTEMPOWER") ?: 0}"
    }

    override fun onMessage(message: String): Int {
        val res = JsonUtils.fromJson(message, Response::class.java) ?: return 0
        val type = super.onMessage(message)
        when (type) {
            11002 -> {
                mTextView_dl.text = Const.systemPower.toString()

                if (res.error_code == 0) {
                    val robotBattery =
                        JsonUtils.fromJson(message, GetBatteryRes::class.java) ?: return res.type
                    if (!robotBattery.getJson()!!.charging) {
                        startActivity(intentFor<StandbyActivity>("systemPower" to Const.systemPower).clearTop())
                        finish()
                    }
                }
            }
        }
        return type
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.stop()
        mediaPlayer?.release()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        return keyCode == KeyEvent.KEYCODE_BACK;
    }

}