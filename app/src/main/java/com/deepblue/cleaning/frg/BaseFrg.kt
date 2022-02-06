//
//  BaseFrg
//
//  Created by 86139 on 2020-10-13 13:57:55
//  Copyright (c) 86139 All rights reserved.


/**

 */

package com.deepblue.cleaning.frg;

import android.view.MotionEvent
import android.view.View;
import android.widget.LinearLayout
import com.deepblue.cleaning.Const
import com.deepblue.cleaning.R
import com.deepblue.cleaning.RobotApplication
import com.deepblue.cleaning.activity.*
import com.deepblue.cleaning.cleanview.AlertDialog
import com.deepblue.cleaning.cleanview.BToast
import com.deepblue.cleaning.cleanview.WaiteDialog
import com.deepblue.cleaning.utils.CommonUtil
import com.deepblue.cleaning.utils.DialogUtils
import com.deepblue.library.planbmsg.HeartbeatRes
import com.deepblue.library.planbmsg.JsonUtils
import com.deepblue.library.planbmsg.bean.ErrorInfo
import com.deepblue.library.planbmsg.bean.RobotStatus
import com.deepblue.library.planbmsg.msg1000.GetRobotStatusRes
import com.deepblue.library.planbmsg.msg2000.GetSelNumRes
import com.deepblue.library.planbmsg.msg4000.ChangeTaskStatusReqClean
import com.mdx.framework.Frame

import com.mdx.framework.activity.MFragment;
import kotlinx.android.synthetic.main.frg_gcmb.*
import kotlinx.android.synthetic.main.layout_lock.*
import org.jetbrains.anko.clearTop
import org.jetbrains.anko.intentFor
import org.jetbrains.anko.startActivity

abstract class BaseFrg : MFragment(), View.OnClickListener, RobotApplication.ActivityCallBack {

    val mWaiteDialog: WaiteDialog by lazy {
        WaiteDialog(context!!)
    }
    var mRunnable: Runnable = Runnable {
        if (mWaiteDialog != null && mWaiteDialog!!.isShowing) {
            if (mWaiteDialog.needTost) {
                BToast.showText(
                    text =
                    getString(R.string.i_qqcs)
                )
            }

            dismissWaite()
        }
    }

    final override fun initV(view: View) {
        initView()
        loaddata()
    }

    abstract fun initView()
    abstract fun loaddata()
    override fun onClick(v: View) {
    }

    override fun disposeMsg(type: Int, obj: Any?) {
        //BaseActivity中onmessage已经处理了
        var message = obj.toString()
        when (type) {
            10999 -> {
                cb_bar?.refData()
            }
//            12014 -> {
//                if (!(activity!!.application as RobotApplication)!!.isShutdownActivityShow) {
//                    (activity!!.application as RobotApplication)!!.isShutdownActivityShow = true
//                    startActivity(activity!!.intentFor<ShutdownActivity>().clearTop())
//                }
//            }
//            11004 -> {
//                val robotStatusRes =
//                    JsonUtils.fromJson(message, GetRobotStatusRes::class.java)
//                if (robotStatusRes!!.getJson()!!.status != Const.robotPlayStatus) {
//                    Const.robotPlayStatus = robotStatusRes.getJson()!!.status
//                    if (Const.robotPlayStatus == RobotStatus.STATUS_EMERGENCY) {
//                        startActivity(activity!!.intentFor<StopActivity>().clearTop())
//                    }
//                }
//            }
        }

    }

    open fun onSuccess(data: String?, method: String) {
    }

    fun showWaite(delayMillis: Long = 20000, needTost: Boolean = true) {
        if (mWaiteDialog != null && !mWaiteDialog!!.isShowing) {
            mWaiteDialog!!.show()
            mWaiteDialog.needTost = needTost
            handler.removeCallbacksAndMessages(null)
            handler.postDelayed(mRunnable, delayMillis)
        }
    }

    override fun onDestroy() {
        dismissWaite()
        handler.removeCallbacksAndMessages(null)
        super.onDestroy()
    }

    fun dismissWaite() {
        if (mWaiteDialog != null && mWaiteDialog!!.isShowing) {
            mWaiteDialog!!.dismiss()
        }
    }

    fun sendwebSocket(message: String, needShowLoad: Boolean = false) {
        if (needShowLoad) showWaite()
        (activity!!.application as RobotApplication)!!.webSocketClient!!.sendMessage(message)
    }

    override fun onResume() {
        (activity!!.application as RobotApplication)!!.lockTime = 0
        (activity!!.application as RobotApplication)!!.setActivityCallBack(this)
        cb_bar?.refData()
        super.onResume()
    }

    override fun backLoack() {
        if (!CommonUtil.isBackground(this.javaClass.simpleName) && (this.javaClass.simpleName == "FrgTaskManageDetail" || this.javaClass.simpleName == "FrgTaskManage")) {
            startActivity(activity!!.intentFor<StandbyActivity>().clearTop())
        }
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        when (ev.action) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE ->                 //有按下动作时取消定时
                (activity!!.application as RobotApplication)!!.lockTime = 0
        }
        return super.dispatchTouchEvent(ev)
    }

    override fun stopHeart() {
        if (!CommonUtil.isBackground(this.javaClass.simpleName) && (this.javaClass.simpleName == "FrgTaskManageDetail" || this.javaClass.simpleName == "FrgTaskManage")) {
            startActivity(activity!!.intentFor<NeterrorActivity>().clearTop())
        }
    }

    override fun setActionBar(mActionBar: LinearLayout?) {
        super.setActionBar(mActionBar)
        mActionBar?.visibility = View.GONE
    }
}
