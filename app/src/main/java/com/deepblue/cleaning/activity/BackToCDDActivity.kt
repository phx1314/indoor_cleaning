package com.deepblue.cleaning.activity

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import com.deepblue.cleaning.R
import com.deepblue.cleaning.cleanview.AlertDialog
import com.deepblue.cleaning.utils.BaseDoubleClickListener
import com.deepblue.cleaning.utils.CleanUtil
import com.deepblue.cleaning.utils.CommonUtil
import com.deepblue.cleaning.utils.CommonUtil.get
import com.deepblue.library.planbmsg.JsonUtils
import com.deepblue.library.planbmsg.msg4000.ChangeTaskStatusReqClean
import com.deepblue.library.planbmsg.push.Cleanprogress
import com.deepblue.library.planbmsg.push.ErrorRealRes
import com.deepblue.library.planbmsg.push.TaskReportRes
import com.deepblue.library.planbmsg.push.TaskStatusRes
import com.mdx.framework.Frame
import kotlinx.android.synthetic.main.layout_backtocdd.*
import kotlinx.android.synthetic.main.layout_palylock.*
import org.jetbrains.anko.startActivity

class BackToCDDActivity : BaseActivity() {
    companion object {
        val THISSTATUS_ = "thisstatus_"
    }

    var objectAnimatorLock: ObjectAnimator? = null
    var thisStatus = 0  // 0-> 任务状态    1-> 返回充电
    var isReceiveReport = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_backtocdd)
        message = intent.get<String>("message") ?: ""
        taskReportRes_message = intent.get<String>("taskReportRes_message") ?: ""
        errorRealRes_message = intent.get<String>("errorRealRes_message") ?: ""
        initView()
    }

    private fun initView() {
        try {
            objectAnimator = ObjectAnimator.ofFloat(img_flash_6, "alpha", 0.5f, 0f, 0.5f)
            objectAnimator!!.duration = 2000
            objectAnimator!!.repeatCount = ValueAnimator.INFINITE //无限循环
            objectAnimator!!.start()

            objectAnimatorLock = ObjectAnimator.ofFloat(img_flashplay, "alpha", 0.8f, 0.1f, 0.8f)
            objectAnimatorLock!!.duration = 2000
            objectAnimatorLock!!.repeatCount = ValueAnimator.INFINITE //无限循环
            objectAnimatorLock!!.start()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        if (!TextUtils.isEmpty(message)) updateMessageUi(message)
//        if (!TextUtils.isEmpty(taskReportRes_message)) updateTaskReportResUi(taskReportRes_message)
//        if (!TextUtils.isEmpty(errorRealRes_message)) updateErrorRealResUi(errorRealRes_message)
        view_backtocdd.setOnClickListener(object : BaseDoubleClickListener() {
            override fun onDoubleClick(v: View?) {
                Frame.HANDLES.sentAll("PlayActivity", 701, "")
                gotoLogin()
            }
        })
    }

    private fun gotoLogin() {
        sendwebSocket(ChangeTaskStatusReqClean().pause(0))
        startActivity<LoginActivity>(THISSTATUS_ to thisStatus, "type" to type)
    }

    private fun updateUI() {
        when (thisStatus) {
            0 -> {// 任务状态
                rl_backtocdd.visibility = GONE
                rl_playlock.visibility = VISIBLE
            }
            1 -> {// 返回充电
                rl_backtocdd.visibility = VISIBLE
                rl_playlock.visibility = GONE
                if (type == "C") {
                    mImageView_bg.setBackgroundResource(R.mipmap.icon_backcdd_bg)
                    tv_backtocdd_toast.text = getString(R.string.i_fhcdd)
                } else if (type == "E") {
                    mImageView_bg.setBackgroundResource(R.drawable.ic_addwater_)
                    tv_backtocdd_toast.text = getString(R.string.i_sld)
                } else if (type == "H") {
                    mImageView_bg.setBackgroundResource(R.drawable.ic_pushwater_)
                    tv_backtocdd_toast.text = getString(R.string.i_wsg)
                }
                playPubVoiceTx(type)//这边做语音播报提示
            }
        }
    }

    fun updateMessageUi(message: String) {
        val cleanProgress =
            JsonUtils.fromJson(message, Cleanprogress::class.java)!!.getJson()
        if (cleanProgress != null) {
            if (cleanProgress.currentTime >= 1 && cleanProgress.cycleTimes >= 1 && cleanProgress.donePercent >= 0 && cleanProgress.donePercent <= 100) {
                tv_allarea.text = areaFormat(cleanProgress.cleanArea / 100)
                tv_taskarea2.text = areaFormat(cleanProgress.cleanArea / 100 * cleanProgress.donePercent / 100)
                tv_worktime2.text = CleanUtil.setTimeTextSize(timeFormat(cleanProgress.workTime))
                tv_surplustime2.text = CleanUtil.setTimeTextSize(timeFormat(cleanProgress.remainingTime))
                tv_looptime2.text = "${cleanProgress.currentTime} / ${cleanProgress.cycleTimes}"
                tv_donePercent.text = CleanUtil.setPercentTextSize("${cleanProgress.donePercent}%")
                progressbar_donepercent.progress = cleanProgress.donePercent.toInt()
            } else {
                CommonUtil.logger("cleanProgress", "cleanProgress-----${System.currentTimeMillis()}-----")
            }
        }

    }

    fun updateTaskReportResUi(message: String, isFromAction: Boolean = false) {
        val taskReportRes = JsonUtils.fromJson(message, TaskReportRes::class.java)
        val resultBean = taskReportRes?.getJson()
        if (resultBean != null) {
            isReceiveReport = true
            when (resultBean.task_status) {
                5 -> {//因取消任务跳转报告界面
                    thisStatus = 3
                }
                13 -> {//因低电取消任务跳转返回充电点状态
                    tv_deviceStatus.text = getString(R.string.i_rzd_dld)
                }
                14 -> {
                    thisStatus = 3
                    tv_deviceStatus.text = getString(R.string.rzd_jcdr)
                }
                15 -> {
                    thisStatus = 3
                    tv_deviceStatus.text = getString(R.string.i_rzd_cancel)
                }
                16 -> {
                    tv_deviceStatus.text = getString(R.string.i_rzd_pause)
                }
                4 -> {
                    thisStatus = 3
                    tv_deviceStatus.text = getString(R.string.i_rywc)
                }
                12 -> {
                    thisStatus = 3
                    if (TextUtils.isEmpty(resultBean.message)) {
                        tv_deviceStatus.text = getString(R.string.i_rzd_gz)
                    } else {
                        tv_deviceStatus.text = getString(R.string.i_rzd) + ":" + resultBean.message
                    }
                }
                17 -> {
                    tv_deviceStatus.text = getString(R.string.i_rzd_zsg)
                }
                else -> {
                    thisStatus = 3
                    tv_deviceStatus.text = getString(R.string.i_rzd)

                }
            }
        }

    }

    fun updateErrorRealResUi(message: String, isFromAction: Boolean = false) {
        val errorRealRes = JsonUtils.fromJson(message, ErrorRealRes::class.java)!!.getJson()
        if (errorRealRes != null) {
            when (errorRealRes.error_code) {
                "8128@85" -> {
                    tv_deviceStatus.text = getString(R.string.str_rwzd_dlbz)
                    if (isFromAction) {
                        thisStatus = 1
                        type = "C"
                        updateUI()
                    }
                }
                "8128@87" -> {
                    tv_deviceStatus.text = getString(R.string.str_rwzd_qsd)
                    if (isFromAction) {
                        thisStatus = 1
                        type = "E"
                        updateUI()
                    }

                }
                "8128@90" -> {
                    tv_deviceStatus.text = getString(R.string.str_rwzd_wsg)
                    if (isFromAction) {
                        thisStatus = 1
                        type = "H"
                        updateUI()
                    }
                }
                "26215#24" -> tv_deviceStatus.text = getString(R.string.str_jt)
                "202001#14" -> tv_deviceStatus.text = getString(R.string.str_rwzd_xdyywdgz)
                "202001#15" -> tv_deviceStatus.text = getString(R.string.str_rwzd_xdbzsgz)
            }
        }

    }

    override fun disposeMsg(type: Int, obj: Any) {
        if (type > 10000) Frame.HANDLES.sentAll("PlayActivity", type, obj)
        val message = obj.toString()
        when (type) {
            702 -> {
                finish()
            }
            703 -> {
                tv_deviceStatus.text = getString(R.string.str_jqrzzgzqwdr)
            }
            24003 -> updateMessageUi(message)
            24004 -> updateTaskReportResUi(message, true)
            24002 -> updateErrorRealResUi(message, true)
            24006 -> {
                val taskStatusRes = JsonUtils.fromJson(message, TaskStatusRes::class.java)?.getJson()
                when (taskStatusRes?.range_status) {
                    2 -> thisStatus = 0
                }
                when (taskStatusRes?.reason) {
                    1, 2, 3 -> {
                        thisStatus = 0
                        playPubVoice(this.type, false)// 这边做语音播报提示
                    }
                }
            }
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        return keyCode == KeyEvent.KEYCODE_BACK;
    }

}