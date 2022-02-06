package com.deepblue.cleaning.activity

import android.os.Bundle
import android.os.Handler
import android.text.TextUtils
import android.view.View
import android.widget.AdapterView
import com.deepblue.cleaning.Const
import com.deepblue.cleaning.R
import com.deepblue.cleaning.activity.PlayActivity.Companion.TASKCOMPLETE_FROMPALYACTIVITY
import com.deepblue.cleaning.adapter.TaskHistoryAdapter
import com.deepblue.cleaning.cleanview.AlertDialog
import com.deepblue.cleaning.req.DeleteTaskReportReq
import com.deepblue.cleaning.utils.CleanUtil
import com.deepblue.cleaning.utils.CommonUtil.get
import com.deepblue.cleaning.utils.DialogUtils
import com.deepblue.library.planbmsg.JsonUtils
import com.deepblue.library.planbmsg.bean.IOState
import com.deepblue.library.planbmsg.bean.TaskReport
import com.deepblue.library.planbmsg.msg2000.IOStatesReq
import com.deepblue.library.planbmsg.msg2000.LogoffReq
import com.deepblue.library.planbmsg.msg4000.GetTaskReportsReq2
import com.deepblue.library.planbmsg.msg4000.GetTaskReportsRes
import com.mdx.framework.F
import com.mdx.framework.Frame
import kotlinx.android.synthetic.main.activity_task_report.*
import kotlinx.android.synthetic.main.layout_history_report.*
import java.util.*

class TaskReportActivity : BaseActivity() {

    private var taskHistoryAdapter: TaskHistoryAdapter? = null
    private var historyList: ArrayList<TaskReport> = ArrayList()
    private var delayTime: Int = 0
    private var historyReportBean: TaskReport? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task_report)
        Const.isDdXs = true
        delayTime = intent.get<Int>(TASKCOMPLETE_FROMPALYACTIVITY) ?: 0
        initViews()
        initData()
    }

    override fun disposeMsg(type: Int, obj: Any) {
        when (type) {
            0 -> {
                var item = obj as TaskReport
                dialogFragment = DialogUtils.showAlert(this@TaskReportActivity,
                    getText(R.string.sure_delete_task).toString(),
                    R.string.sure_delete,
                    object : AlertDialog.DialogButtonListener {
                        override fun cancel() {

                        }

                        override fun ensure(isCheck: Boolean): Boolean {
                            var io_states: ArrayList<String> = ArrayList()
                            io_states.add(item.start_time ?: "")
                            sendwebSocket(DeleteTaskReportReq(io_states).toString())
                            historyList.remove(item)
                            taskHistoryAdapter!!.notifyDataSetChanged()
                            if (item.end_time == historyReportBean?.end_time) {
                                if (historyList.size > 0) {
                                    showTaskInfo(historyList[0])
                                    taskHistoryAdapter!!.updateData(0)
                                }
                            }
                            return true
                        }
                    })
            }
        }

    }

    private fun initData() {
        historyList.clear()
        sendwebSocket(GetTaskReportsReq2(0, System.currentTimeMillis() / 1000).toString())
        showWaite()
    }

    private fun initViews() {
        taskHistoryAdapter = TaskHistoryAdapter(this, historyList)
        task_history_lv!!.adapter = taskHistoryAdapter
        taskHistoryAdapter!!.setItemClickCallback(object : TaskHistoryAdapter.ItemClickCallback {
            override fun itemClick(historyReportBean: TaskReport) {
                showTaskInfo(historyReportBean)
            }
        })
        back_rl.setOnClickListener(this)

        if (delayTime != 0) {
            handler.postDelayed({
                finish()
            }, (delayTime * 1000).toLong())
        }
    }

    override fun onClick(v: View) {
        super.onClick(v)
        when (v.id) {
            R.id.back_rl -> finish()
        }
    }

    private fun showTaskInfo(historyReportBean: TaskReport) {
        this.historyReportBean = historyReportBean
        device_name_tv.text = historyReportBean.name ?: "NAN"
        user_tv.text = historyReportBean.operator ?: "NAN"
        clean_task_tv.text = historyReportBean.task_name ?: "NAN"
        start_time_tv.text = historyReportBean.start_time ?: "NAN"
        end_time_tv.text = historyReportBean.end_time ?: "NAN"

        working_time_tv.text = CleanUtil.setTimeTextSize(timeFormat(historyReportBean.cost_time!!))
        if (historyReportBean.use_water == null) {
            water_consumption_tv.text = CleanUtil.setLifterTextSize("NAN L")
        } else {
            water_consumption_tv.text = CleanUtil.setLifterTextSize(F.go2Wei(historyReportBean.use_water) + " L")

        }

        clean_area_num_tv.text = CleanUtil.setAreaTextSize("${areaFormat((historyReportBean.clean_area?.toDouble() ?: 0.0) / 100)} m2")
        planning_area_num_tv.text = CleanUtil.setAreaTextSize("${areaFormat((historyReportBean.task_area?.toDouble() ?: 0.0) / 100)} m2")

        complete_percent_view.progress = historyReportBean.percent ?: 0
        completion_ratio_tv.text = CleanUtil.setPercentTextSize("${historyReportBean.percent ?: 0}%")

        clean_mode_tv.text =
            when (historyReportBean.clean_mode) {
                3 -> getString(R.string.i_ccms_qd)
                4 -> getString(R.string.i_ccms_bz)
                8 -> getString(R.string.i_xdms_qd)
                9 -> getString(R.string.i_xdms_bz)
                10 -> getString(R.string.i_xdms_ql)
                14 -> getString(R.string.scxh)
                101 -> getString(R.string.i_yjxh_pt)
                102 -> getString(R.string.i_yjxh_sc)
                19 -> getString(R.string.str_gxms)
                28 -> getString(R.string.i_ctms)

                else -> ""
            }
        when (historyReportBean.task_status) {
            4 -> {
                task_status_tv.text = getString(R.string.i_finshi)
                tv_report_status.text = getString(R.string.i_rwwc)
                tv_report_status.setTextColor(resources.getColor(R.color.report_ok))
                tv_report_status.setCompoundDrawablesWithIntrinsicBounds(R.mipmap.icon_report_ok, 0, 0, 0)
            }
            5, 7 -> {
                task_status_tv.text = getString(R.string.i_stop)
                tv_report_status.text = getString(R.string.i_rwtz)
                tv_report_status.setTextColor(resources.getColor(R.color.report_fail))
                tv_report_status.setCompoundDrawablesWithIntrinsicBounds(R.mipmap.icon_report_warn, 0, 0, 0)
            }
            10 -> {
                task_status_tv.text = getString(R.string.i_zd)
                tv_report_status.text = getString(R.string.i_rwzd)
                tv_report_status.setTextColor(resources.getColor(R.color.report_shut))
                tv_report_status.setCompoundDrawablesWithIntrinsicBounds(R.mipmap.icon_report_shut, 0, 0, 0)
            }
            13 -> {
                task_status_tv.text = getString(R.string.i_zd)
                tv_report_status.text = getString(R.string.i_rwzd_dd)
                tv_report_status.setTextColor(resources.getColor(R.color.report_shut))
                tv_report_status.setCompoundDrawablesWithIntrinsicBounds(R.mipmap.icon_report_shut, 0, 0, 0)
            }
            16 -> {
                task_status_tv.text = getString(R.string.i_zd)
                tv_report_status.text = getString(R.string.i_rwzd_ds)
                tv_report_status.setTextColor(resources.getColor(R.color.report_shut))
                tv_report_status.setCompoundDrawablesWithIntrinsicBounds(R.mipmap.icon_report_shut, 0, 0, 0)
            }
            17 -> {
                task_status_tv.text = getString(R.string.i_zd)
                tv_report_status.text = getString(R.string.i_rwzd_gw)
                tv_report_status.setTextColor(resources.getColor(R.color.report_shut))
                tv_report_status.setCompoundDrawablesWithIntrinsicBounds(R.mipmap.icon_report_shut, 0, 0, 0)
            }
            14 -> {
                task_status_tv.text = getString(R.string.i_zd)
                tv_report_status.text = getString(R.string.rzd_jcdr)
                tv_report_status.setTextColor(resources.getColor(R.color.report_shut))
                tv_report_status.setCompoundDrawablesWithIntrinsicBounds(R.mipmap.icon_report_shut, 0, 0, 0)
            }
            15 -> {
                task_status_tv.text = getString(R.string.i_zd)
                tv_report_status.text = getString(R.string.i_rzd_cancel)
                tv_report_status.setTextColor(resources.getColor(R.color.report_shut))
                tv_report_status.setCompoundDrawablesWithIntrinsicBounds(R.mipmap.icon_report_shut, 0, 0, 0)
            }
            3 -> {
                task_status_tv.text = getString(R.string.i_sb)
                tv_report_status.text = getString(R.string.i_rwsb)
                tv_report_status.setTextColor(resources.getColor(R.color.report_fail))
                tv_report_status.setCompoundDrawablesWithIntrinsicBounds(R.mipmap.icon_report_error, 0, 0, 0)
            }
            12 -> {
                task_status_tv.text = getString(R.string.i_sb)
                if (TextUtils.isEmpty(historyReportBean.message)) {
                    tv_report_status.text = getString(R.string.i_rwsb_gz)
                } else {
                    tv_report_status.text = getString(R.string.i_rwsb) + ":" + historyReportBean.message
                }


                tv_report_status.setTextColor(resources.getColor(R.color.report_fail))
                tv_report_status.setCompoundDrawablesWithIntrinsicBounds(R.mipmap.icon_report_error, 0, 0, 0)
            }
            else -> {
                task_status_tv.text = getString(R.string.i_other)
                tv_report_status.text = ""
                tv_report_status.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
            }
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        Frame.HANDLES.sentAll("CleanMainActivity", 4, "")
    }

    override fun onMessage(message: String): Int {
        val type = super.onMessage(message)
        when (type) {
            14011 -> {
                try {
                    dismissWaite()
                    historyList.clear()
                    val getTaskReportsRes = JsonUtils.fromJson(message, GetTaskReportsRes::class.java)
                    historyList = getTaskReportsRes!!.getJson()!!.reports
                    taskHistoryAdapter!!.updateData(historyList)
                    if (historyList.size > 0) {
                        showTaskInfo(historyList[0])
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        return type
    }


}