package com.deepblue.cleaning.activity

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.recyclerview.widget.LinearLayoutManager
import com.deepblue.cleaning.Const
import com.deepblue.cleaning.F
import com.deepblue.cleaning.R
import com.deepblue.cleaning.adapter.RecycleAdapterPlay
import com.deepblue.cleaning.cleanview.AlertDialog
import com.deepblue.cleaning.cleanview.BToast
import com.deepblue.cleaning.item.PopChangeLevel
import com.deepblue.cleaning.item.PopShowChangeLevel
import com.deepblue.cleaning.req.GetCurrentTaskStatusReq
import com.deepblue.cleaning.utils.CleanUtil
import com.deepblue.cleaning.utils.CommonUtil
import com.deepblue.cleaning.utils.CommonUtil.get
import com.deepblue.cleaning.utils.DialogUtils
import com.deepblue.cleaning.utils.MapUtilsB
import com.deepblue.library.planbmsg.JsonUtils
import com.deepblue.library.planbmsg.bean.*
import com.deepblue.library.planbmsg.msg2000.IOStatesReq
import com.deepblue.library.planbmsg.msg4000.ChangeTaskStatusReqClean
import com.deepblue.library.planbmsg.msg5000.GetScrubberStatusReq
import com.deepblue.library.planbmsg.msg5000.GetScrubberStatusRes
import com.deepblue.library.planbmsg.msg5000.SetCleanModeReq
import com.deepblue.library.planbmsg.push.Cleanprogress
import com.deepblue.library.planbmsg.push.ErrorRealRes
import com.deepblue.library.planbmsg.push.TaskReportRes
import com.deepblue.library.planbmsg.push.TaskStatusRes
import com.google.gson.Gson
import com.zrq.divider.Divider
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_play.*
import kotlinx.android.synthetic.main.activity_play.ll_gofilling
import kotlinx.android.synthetic.main.activity_play.ll_gopullwater
import kotlinx.android.synthetic.main.activity_play.ll_gopushwater
import org.jetbrains.anko.clearTop
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.intentFor


class PlayActivity : BaseActivity() {
    companion object {
        val TASKCOMPLETE_FROMPALYACTIVITY = "taskComplete_fromPalyActivity"
    }

    var mTaskList = ArrayList<RangeRro>()
    var playAdapter: RecycleAdapterPlay? = null
    var points: ArrayList<MapPoint> = ArrayList()
    var bitmap: Bitmap? = null
    var intentGet: CommonTask? = null
    var isDoingTask: Boolean = false
    var currentType: String = "C"
    var isNeedFinish = true
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_play)
        intentGet = intent.get<CommonTask>("commonTask")
        if (intentGet == null) {
            F.getJson("commonTask")?.let {
                intentGet = JsonUtils.fromJson(it, CommonTask::class.java)
            }
            sendwebSocket(GetCurrentTaskStatusReq().toString())
        }
        F.saveJson("commonTask", Gson().toJson(intentGet))
        initView()
        initData()
    }

    private fun initData() {
        sendwebSocket(GetScrubberStatusReq().toString())
    }

    private fun updateMOdelUI() {
        if (indexPopwinModel == 0) {
            when (indexPopwinPower) {
                0 -> {
                    tv_play_work_p.text = getString(R.string.light)
                    ic_play_work_p.setImageDrawable(getDrawable(R.mipmap.icon_wind1))
                }
                1 -> {
                    tv_play_work_p.text = getString(R.string.standard)
                    ic_play_work_p.setImageDrawable(getDrawable(R.mipmap.icon_wind2))
                }
            }
        } else {
            when (indexPopwinPower) {
                0 -> {
                    tv_play_work_p.text = getString(R.string.light)
                    ic_play_work_p.setImageDrawable(getDrawable(R.mipmap.icon_water1))
                }
                1 -> {
                    tv_play_work_p.text = getString(R.string.standard)
                    ic_play_work_p.setImageDrawable(getDrawable(R.mipmap.icon_water2))
                }
                2 -> {
                    tv_play_work_p.text = getString(R.string.str_qinagli)
                    ic_play_work_p.setImageDrawable(getDrawable(R.mipmap.icon_water3))
                }
            }
        }

    }

    var indexPopwinPower = 0
    var indexPopwinModel = 0

    private fun initView() {
        tv_mapname_play.text = Const.map?.map_info?.map_name
        if (intentGet?.ranges != null && intentGet?.ranges!!.size > 0) {
            when (intentGet?.task_basic_info?.task_type) {
                7 -> {//吸尘
                    if (Const.type == "10") {
                        indexPopwinModel = 0
                        when (intentGet?.ranges!![0].work_pattern) {
                            3 -> {
                                rl_setmodel.visibility = VISIBLE
                                indexPopwinPower = 0
                            }
                            4 -> {
                                rl_setmodel.visibility = VISIBLE
                                indexPopwinPower = 1
                            }
                        }
                        updateMOdelUI()
                    } else rl_setmodel.visibility = GONE
                }
                9 -> {//洗地
                    indexPopwinModel = 1
                    when (intentGet?.ranges!![0].work_pattern) {
                        8 -> {
                            rl_setmodel.visibility = VISIBLE
                            indexPopwinPower = 0
                        }
                        9 -> {
                            rl_setmodel.visibility = VISIBLE
                            indexPopwinPower = 1
                        }
                        10 -> {
                            rl_setmodel.visibility = VISIBLE
                            indexPopwinPower = 2
                        }
                        14 -> {
                            rl_setmodel.visibility = GONE
                        }
                    }
                    updateMOdelUI()
                }
                else -> {//一键清洁
                    rl_setmodel.visibility = GONE
                }
            }
        }

        rl_setmodel.setOnLongClickListener {
            if (btn_continue.text.toString() == getString(R.string.continues)) {
                var mDialogPub = PopChangeLevel(this)
                var mPopShowSet = PopShowChangeLevel(this, it, mDialogPub)
                mDialogPub.set(indexPopwinModel, indexPopwinPower, mPopShowSet, "PlayActivity")
                mPopShowSet.show()
            }
            true
        }

        playAdapter = RecycleAdapterPlay(this@PlayActivity, mTaskList)
        val layoutManager = LinearLayoutManager(this)
        rv_play.layoutManager = layoutManager
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        rv_play.adapter = playAdapter
        rv_play.addItemDecoration(
            Divider.builder()
                .color(Color.parseColor("#00ffffff"))
                .height(10)
                .build()
        )

        rg_task_view.setOnCheckedChangeListener { group, checkedId ->
            if (checkedId == R.id.rb_work_map) {
                rl_workmap.visibility = VISIBLE
                rl_workinfo.visibility = View.INVISIBLE
            } else {
                rl_workmap.visibility = View.INVISIBLE
                rl_workinfo.visibility = VISIBLE
            }
        }

        drawView()
        btn_stop.setOnClickListener(this)
        btn_continue.setOnClickListener(this)
        btn_push.setOnClickListener(this)

        startActivity(intentFor<BackToCDDActivity>().clearTop())



        ll_gofilling.setOnClickListener {
            goDoPreTask("C")
        }

        ll_gopullwater.setOnClickListener {
            goDoPreTask("E")
        }

        ll_gopushwater.setOnClickListener {
            goDoPreTask("H")
        }
    }

    private fun showPoints(canvas: Canvas, mapToBitmap: Bitmap) {
        val paintPoint = Paint()
        paintPoint.style = Paint.Style.STROKE
        paintPoint.isAntiAlias = true
        for (point in Const.map!!.points) {
            var text = ""
            var list = ArrayList<String>()

            if (point.type.contains("C")) {
                list.add(getString(R.string.filling_point))
                ll_gofilling.visibility = View.VISIBLE
                paintPoint.color = Color.parseColor("#245FD2")
            }
            if (point.type.contains("E")) {
                list.add(getString(R.string.pull_water_point))
                ll_gopullwater.visibility = View.VISIBLE
                paintPoint.color = Color.parseColor("#245FD2")
            }
            if (point.type.contains("H")) {
                list.add(getString(R.string.push_water_point))
                ll_gopushwater.visibility = View.VISIBLE
                paintPoint.color = Color.parseColor("#245FD2")
            }
            list.forEach {
                text += it + "|"
            }
            if (text.isNotEmpty()) text = text.substring(0, text.length - 1)
            if (point.type.contains("C") || point.type.contains("E") || point.type.contains("H")) {
                MapUtilsB.paintPoint(
                    point,
                    canvas,
                    mapToBitmap!!,
                    Const.map!!,
                    paintPoint,
                    text,
                    5f
                )
            }
        }
    }

    fun goDoPreTask(type: String) {
        var title: String = ""
        when (type) {
            "C" -> {//充电
                title = getString(R.string.is_sure_go_charge_point)
            }
            "E" -> {//加水
                title = getString(R.string.is_sure_go_pull_point)
            }
            "H" -> {//排水
                title = getString(R.string.is_sure_go_push_point)
            }
        }
        dialogFragment = DialogUtils.showAlert(
            this,
            title,
            R.string.sure_go,
            object : AlertDialog.DialogButtonListener {
                override fun cancel() {
                }

                override fun ensure(isCheck: Boolean): Boolean {
                    goDoTaskOrPause(type, true, true)
                    return true
                }
            }, type = type
        )

    }

    fun goDoTaskOrPause(type: String, isDoTask: Boolean, isFirst: Boolean) {
        currentType = type
        isDoingTask = isDoTask
        var title = ""
        var interrupt_type = 0
        when (type) {
            "C" -> {//充电
                title = getString(R.string.ing_sure_go_charge_point)
                interrupt_type = 3
            }
            "E" -> {//加水
                title = getString(R.string.ing_sure_go_pull_point)
                interrupt_type = 1
            }
            "H" -> {//排水
                title = getString(R.string.ing_sure_go_push_point)
                interrupt_type = 2
            }
        }
        if (isFirst) {
            for (point in Const.map!!.points) {
                if (point.type.contains(type)) {
                    sendwebSocket(ChangeTaskStatusReqClean().interrupt(interrupt_type))
                }
            }
        } else {
            if (isDoTask) {
                //这边做语音播报提示
                playPubVoice(type)
                sendwebSocket(ChangeTaskStatusReqClean().resume(0))
            } else {
                sendwebSocket(ChangeTaskStatusReqClean().pause(0))
            }
        }
        dialogFragment = DialogUtils.showAlert(
            this,
            title,
            if (isDoTask) R.string.suspend else R.string.continues,
            object : AlertDialog.DialogButtonListener {
                override fun cancel() {
                    if (Const.robotPlayStatus == RobotStatus.STATUS_GO_BREAK) {
                        Log.i("STATUS_GO_BREAK", "发送停止去中断点指令$type")
                        if (type == "C") {//充电
                            sendwebSocket(ChangeTaskStatusReqClean().stop())
                        } else if (type == "E") {//补水
                            sendwebSocket(ChangeTaskStatusReqClean().stop())
                        } else if (type == "H") {//排水
                            sendwebSocket(ChangeTaskStatusReqClean().stop())
                        }
                        isDoingTask = false
                    }
                }

                override fun ensure(isCheck: Boolean): Boolean {
                    goDoTaskOrPause(type, !isDoTask, false)
                    return true
                }
            }, type = type
        )
        dialogFragment?.id = 1  //任务过程中手动点击的弹框id是1
    }

    override fun onClick(v: View) {
        super.onClick(v)
        when (v.id) {
            R.id.btn_stop -> {
                if (isNeedFinish) {
                    handler.postDelayed({ finish() }, 30000)
                    isNeedFinish = false
                }
                sendwebSocket(ChangeTaskStatusReqClean().stop())
            }
            R.id.btn_continue -> {
                when (btn_continue.text.toString()) {
                    getString(R.string.suspend) -> {
                        sendwebSocket(ChangeTaskStatusReqClean().pause(0))
                        btn_continue.text = getString(R.string.continues)
                    }
                    getString(R.string.continues) -> {
                        sendwebSocket(ChangeTaskStatusReqClean().resume(0))
                        btn_continue.text = getString(R.string.suspend)
                    }
                    getString(R.string.has_done) -> {
                        BToast.showText(getString(R.string.i_rywc))
                    }
                    getString(R.string.str_haschance) -> {
                        BToast.showText(getString(R.string.i_rwyqx))
                    }
                }
            }
            R.id.btn_push -> {
                DialogUtils.showAlert(this,
                    getString(R.string.is_sure_push_water),
                    R.string.sure_push_water,
                    object : AlertDialog.DialogButtonListener {
                        override fun cancel() {
                        }

                        override fun ensure(isCheck: Boolean): Boolean {
                            pushingWater()
                            return true
                        }
                    })
            }
        }
    }

    fun drawView() {
        try {
            doAsync {
                if (Const.map != null) {
                    bitmap = MapUtilsB.mapToBitmap(Const.map!!)
                    drawLocationPoint(Canvas(bitmap!!), bitmap!!)
                    val paintPoint = Paint()
                    paintPoint.color = Color.parseColor("#0091FF")
                    paintPoint.style = Paint.Style.STROKE
                    paintPoint.isAntiAlias = true
                    val travel = MapRange()
                    travel.points = points
                    MapUtilsB.paintMapPolygon(
                        false, 1f, Canvas(bitmap!!),
                        bitmap!!, travel, Const.map!!, paintPoint, Paint()
                    )
                    showPoints(Canvas(bitmap!!), bitmap!!)
                    runOnUiThread { tiv_map.setImageBitmap(MapUtilsB.getTransparentBitmap(bitmap!!, 50)) }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun drawLocationPoint(canvas: Canvas, mapToBitmap: Bitmap) {
        Const.robotLoc?.run {
            val paintPoint = Paint()
            paintPoint.color = Color.BLUE
            paintPoint.style = Paint.Style.STROKE
            paintPoint.isAntiAlias = true
            MapUtilsB.paintBitmap(
                this,
                canvas!!,
                mapToBitmap!!,
                Const.map!!,
                paintPoint,
                R.drawable.ic_location, -(angle.toFloat()
                        ), act = this@PlayActivity
            )
        }

    }

    private fun pushingWater() {
        openOrClosePushWater(true)
        DialogUtils.showAlert(this, getString(R.string.ing_push_water), R.string.done_push_water,
            object : AlertDialog.DialogButtonListener {
                override fun cancel() {
                    openOrClosePushWater(false)
                }

                override fun ensure(isCheck: Boolean): Boolean {
                    openOrClosePushWater(false)
                    return true
                }
            })
    }

    fun openOrClosePushWater(open: Boolean) {
        var io_states: ArrayList<IOState> = ArrayList()
        io_states.add(IOState("sewage_valve_motor_set", if (open) 100 else 0))
//        io_states.add(IOState("sewage_motor_set", if (open) 8000 else 0))
        sendwebSocket(IOStatesReq(io_states).toString())
    }

    @SuppressLint("SetTextI18n")
    override fun disposeMsg(type: Int, obj: Any) {
        val message = obj.toString()
        when (type) {
            0, 1, 2 -> {
                //3 除尘模式 轻度 4 除尘模式 标准 8 洗地模式 轻度 9 洗地模式 标准 10 洗地模式 强力 101 一键洗护
                indexPopwinPower = type
                if (obj.toString().toInt() == 0) {//吸尘
                    indexPopwinModel = 0
                    when (indexPopwinPower) {
                        0 -> sendwebSocket(SetCleanModeReq(3).toString())
                        1 -> sendwebSocket(SetCleanModeReq(4).toString())
                    }
                } else {//洗地
                    indexPopwinModel = 1
                    when (indexPopwinPower) {
                        0 -> sendwebSocket(SetCleanModeReq(8).toString())
                        1 -> sendwebSocket(SetCleanModeReq(9).toString())
                        2 -> sendwebSocket(SetCleanModeReq(10).toString())
                    }
                }
                updateMOdelUI()
            }
            700 -> {
                Handler().postDelayed({
                    goDoTaskOrPause(
                        isDoTask = false,
                        isFirst = false,
                        type = obj.toString(),
                        isFromPlay = true
                    )
                }, 1000)
            }
            702 -> {
                finish()
            }
            14002 -> {
//                BToast.showText(this, "任务状态变更")
            }
            11001 -> {
                Const.robotLoc?.run {
                    points.add(this)
                }
                drawView()
            }
            11004 -> {
                if (dialogFragment?.dialog?.isShowing == true && Const.robotPlayStatus == RobotStatus.STATUS_EMERGENCY && isDoingTask && dialogFragment?.id == 1)
                    goDoTaskOrPause(currentType, false, false)

            }
            15003 -> {
                val scrubberStatusRes =
                    JsonUtils.fromJson(message, GetScrubberStatusRes::class.java)
                val scrubberStatus = scrubberStatusRes!!.getJson()
                scrubberStatus!!.run {
                    wv_clean_water_play.waveHeightPercent = clean_capacity / 100f
                    wv_dirty_water_play.waveHeightPercent = dirty_capacity / 100f
                    tv_clean_water_play.text = "$clean_capacity%"
                    tv_dirty_water_play.text = "$dirty_capacity%"
                }
            }
            14013, 24003 -> {
                val cleanProgress =
                    JsonUtils.fromJson(message, Cleanprogress::class.java)!!.getJson()
                if (cleanProgress != null) {
                    if (cleanProgress.currentTime >= 1 && cleanProgress.cycleTimes >= 1 && cleanProgress.donePercent >= 0 && cleanProgress.donePercent <= 100) {
                        tv_planned_area.text = areaFormat(cleanProgress.cleanArea / 100)
                        tv_done_area.text =
                            areaFormat(cleanProgress.cleanArea / 100 * cleanProgress.donePercent / 100)
                        tv_work_time.text =
                            CleanUtil.setTimeTextSize(timeFormat(cleanProgress.workTime))
                        tv_remaining_time.text =
                            CleanUtil.setTimeTextSize(timeFormat(cleanProgress.remainingTime))
                        tv_loop_tims.text =
                            "${cleanProgress.currentTime} / ${cleanProgress.cycleTimes}"
                        tv_protask.text =
                            CleanUtil.setPercentTextSize("${cleanProgress.donePercent}%")
                        pb_work_progress.progress = cleanProgress.donePercent.toInt()
                        when {
                            cleanProgress.rangeProgress.size > 0 -> {
                                mTaskList.clear()
                                mTaskList.addAll(cleanProgress.rangeProgress)//待验证
                                playAdapter!!.notifyDataSetChanged()
                            }
                        }
                    } else {
                        CommonUtil.logger(
                            "cleanProgress",
                            "cleanProgress-----${System.currentTimeMillis()}-----"
                        )
                    }
                }
                this.message = message
            }
            24004 -> {
                val taskReportRes = JsonUtils.fromJson(message, TaskReportRes::class.java)
                val resultBean = taskReportRes?.getJson()
                if (resultBean != null && resultBean.task_status != 0) {
                    if (resultBean.task_status == 13 || resultBean.task_status == 16 || resultBean.task_status == 17) {//低电  低清 高污
                    } else {
                        if (!CommonUtil.isBackground(className)) {//待验证
                            startActivity(
                                intentFor<TaskReportActivity>(
                                    TASKCOMPLETE_FROMPALYACTIVITY to 60
                                ).clearTop()
                            )
                            finish()
                        }
                    }
                }
                this.taskReportRes_message = message
            }
            24002 -> {
                val errorRealRes = JsonUtils.fromJson(message, ErrorRealRes::class.java)!!.getJson()
                Handler().postDelayed({
                    if (dialogFragment?.dialog?.isShowing != true) {
                        if (errorRealRes != null) {
                            when (errorRealRes.error_code) {
                                "8128@85" -> goDoTaskOrPause(
                                    isDoTask = true,
                                    isFirst = false,
                                    type = "C",
                                    isFromPlay = true
                                )
                                "8128@87" -> goDoTaskOrPause(
                                    isDoTask = true,
                                    isFirst = false,
                                    type = "E",
                                    isFromPlay = true
                                )
                                "8128@90" -> goDoTaskOrPause(
                                    isDoTask = true,
                                    isFirst = false,
                                    type = "H",
                                    isFromPlay = true
                                )
                            }
                        }
                    }
                }, 1000)
                this.errorRealRes_message = message
            }
            701 -> btn_continue.text = getString(R.string.continues)

            24006 -> {
                val taskStatusRes =
                    JsonUtils.fromJson(message, TaskStatusRes::class.java)?.getJson()
                when (taskStatusRes?.range_status) {
                    0 -> {//正在执行
                        btn_continue.text = getString(R.string.suspend)
                    }
                    1 -> {//被暂停
                        btn_continue.text = getString(R.string.continues)
                    }
                    2 -> {//完成
                        if (dialogFragment?.dialog?.isShowing == true) {//到达固定点需要隐藏弹框
//                            robotApp!!.lockTime = 0
//                            dialogFragment?.dismiss()
                            hideDialog()
                        }
//                        btn_continue.text = getString(R.string.continues)
                    }
                    3 -> {//被取消
                        btn_continue.text = getString(R.string.continues)
                    }
                }

                when (taskStatusRes?.reason) {
                    1, 2, 3 -> {
                        if (dialogFragment?.dialog?.isShowing == true) {//到达固定点需要隐藏弹框
//                            robotApp!!.lockTime = 0
//                            dialogFragment?.dismiss()
                            hideDialog() //这边做语音播报提示  根据type判断类型
                        }
                        btn_continue.text = getString(R.string.continues)
                    }
                }
            }
        }
    }

    override fun onDestroy() {
//        sendwebSocket(ChangeTaskStatusReqClean().stop())
        super.onDestroy()
    }


//    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
//        if (keyCode == KeyEvent.KEYCODE_BACK) {
//            dialogFragment =
//                DialogUtils.showAlert(this, getString(R.string.sureclose), R.string.sure,
//                    object : AlertDialog.DialogButtonListener {
//                        override fun cancel() {
//                        }
//
//                        override fun ensure(): Boolean {
//                            dialogFragment?.dismiss()
//                            finish()
//                            return true
//                        }
//                    })
//            return true
//        }
//        return super.onKeyDown(keyCode, event)
//    }
}
