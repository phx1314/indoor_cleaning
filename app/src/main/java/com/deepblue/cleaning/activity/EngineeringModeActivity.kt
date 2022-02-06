package com.deepblue.cleaning.activity

import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.RadioGroup
import com.deepblue.cleaning.F
import com.deepblue.cleaning.R
import com.deepblue.cleaning.cleanview.BToast
import com.deepblue.cleaning.frg.*
import com.deepblue.cleaning.req.GetRobotReq
import com.deepblue.cleaning.req.GetRobotRes
import com.deepblue.library.planbmsg.JsonUtils
import com.deepblue.library.planbmsg.Response
import com.deepblue.library.planbmsg.bean.UserInfo
import com.deepblue.library.planbmsg.msg2000.GetAllUsersRes
import com.deepblue.library.planbmsg.msg2000.GetSelNumReq
import com.deepblue.library.planbmsg.msg2000.GetSelNumRes
import com.mdx.framework.Frame
import kotlinx.android.synthetic.main.activity_engineering_mode.*
import kotlinx.android.synthetic.main.frg_gcmb.*
import kotlinx.android.synthetic.main.layout_user_list.*
import org.jetbrains.anko.clearTop
import org.jetbrains.anko.intentFor

/*** 工程模式 页面***/
class EngineeringModeActivity : BaseActivity(), View.OnClickListener {
    var mFrgGcmb = FrgGcmb()
    var mFrgQyrw = FrgQyrw()
    var mFrgPs = FrgPs()
    var mFrgPw = FrgPw()
    var mFrgXl = FrgXl()
    var mFrgSd = FrgSd()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
        setContentView(R.layout.activity_engineering_mode)
        initViews()
        initData()
    }

    private fun initViews() {
        rl_content_enginee.setOnClickListener {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm?.hideSoftInputFromWindow(window.decorView.windowToken, 0)
        }
        back_rl.setOnClickListener { finish() }
        mRadioGroup.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.mRadioButton1 -> this.supportFragmentManager.beginTransaction().show(mFrgGcmb).hide(mFrgQyrw).hide(mFrgPs).hide(mFrgPw).hide(mFrgXl).hide(mFrgSd).commitAllowingStateLoss()
                R.id.mRadioButton2 -> this.supportFragmentManager.beginTransaction().hide(mFrgGcmb).show(mFrgQyrw).hide(mFrgPs).hide(mFrgPw).hide(mFrgXl).hide(mFrgSd).commitAllowingStateLoss()
                R.id.mRadioButton3 -> this.supportFragmentManager.beginTransaction().hide(mFrgGcmb).hide(mFrgQyrw).show(mFrgPs).hide(mFrgPw).hide(mFrgXl).hide(mFrgSd).commitAllowingStateLoss()
                R.id.mRadioButton4 -> this.supportFragmentManager.beginTransaction().hide(mFrgGcmb).hide(mFrgQyrw).hide(mFrgPs).show(mFrgPw).hide(mFrgXl).hide(mFrgSd).commitAllowingStateLoss()
                R.id.mRadioButton5 -> this.supportFragmentManager.beginTransaction().hide(mFrgGcmb).hide(mFrgQyrw).hide(mFrgPs).hide(mFrgPw).show(mFrgXl).hide(mFrgSd).commitAllowingStateLoss()
                R.id.mRadioButton6 -> this.supportFragmentManager.beginTransaction().hide(mFrgGcmb).hide(mFrgQyrw).hide(mFrgPs).hide(mFrgPw).hide(mFrgXl).show(mFrgSd).commitAllowingStateLoss()
            }
        }
        mTextView_bjkz.setOnLongClickListener {
//            F.go2DownloadUrl(this)
            Frame.finish()
            true
        }
    }

    private fun initData() {
        this.supportFragmentManager.beginTransaction().add(R.id.mLinearLayout_content, mFrgGcmb, "mFrgGcmb").add(R.id.mLinearLayout_content, mFrgPs, "mFrgPs").add(R.id.mLinearLayout_content, mFrgQyrw, "mFrgQyrw").add(R.id.mLinearLayout_content, mFrgPw, "mFrgPw").add(R.id.mLinearLayout_content, mFrgXl, "mFrgXl").add(R.id.mLinearLayout_content, mFrgSd, "mFrgSd").show(mFrgGcmb).commit()

        sendwebSocket(GetSelNumReq().toString())
        sendwebSocket(GetRobotReq(arrayOf("brush_motor_preclose", "brush_lift_preclose", "sewage_motor_preclose", "water_motor_preclose", "water_lift_preclose", "wind_motor_preclose", "water_motor_low", "water_motor_mid", "water_motor_high", "sewage_motor_set", "sewage_motor_time", "wind_motor_set", "speed_set_1", "speed_set_2", "speed_set_3", "speed_set_trans", "sewage_motor_during_time").toMutableList()).toString())
    }

    override fun onMessage(message: String): Int {
        val type = super.onMessage(message)
        val res = JsonUtils.fromJson(message, Response::class.java) ?: return 0
        when (type) {
            12031 -> {
                val mGetRobotRes = JsonUtils.fromJson(message, GetRobotRes::class.java)
                mGetRobotRes?.let {
                    Frame.HANDLES.sentAll("FrgPs,FrgPw,FrgQyrw,FrgSd,FrgXl", 111, mGetRobotRes)
                }
            }
            12018 -> {
                val getSelNumRes = JsonUtils.fromJson(message, GetSelNumRes::class.java)
                Frame.HANDLES.sentAll("FrgGcmb", 111, getSelNumRes!!.getJson()!!.serial_number)
            }
        }

        return type
    }

}