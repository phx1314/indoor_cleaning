package com.deepblue.cleaning.activity

import android.os.Bundle
import android.view.View
import android.view.Window
import android.widget.LinearLayout
import android.widget.TextView
import com.deepblue.cleaning.R
import com.deepblue.cleaning.RobotApplication
import com.deepblue.cleaning.cleanview.BToast
import kotlinx.android.synthetic.main.layout_disconnected_state.*
import org.jetbrains.anko.doAsync

class NeterrorActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_net_error)

        reconnect_tv.setOnClickListener {
            showWaite()
            doAsync {
                Thread.sleep(500)
                robotApp!!.connect_status = RobotApplication.CONNECT_STATUS
                Thread.sleep(5000)
                if (!isFinishing) {
                    robotApp!!.connect_status = RobotApplication.DISCONNECT_STATUS
                    BToast.showText(this@NeterrorActivity, getText(R.string.connect_error))
                    runOnUiThread {
                        dismissWaite()
                    }
                }
            }

        }
    }

    override fun onMessage(message: String): Int {
        val tyep = super.onMessage(message)
        dismissWaite()
        finish()
        return tyep

    }

}