package com.deepblue.cleaning.activity

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.deepblue.cleaning.Const
import com.deepblue.cleaning.F
import com.deepblue.cleaning.R
import com.deepblue.cleaning.adapter.RecycleAdapterTask
import com.deepblue.cleaning.bean.Task
import com.deepblue.cleaning.cleanview.AlertDialog
import com.deepblue.cleaning.cleanview.BToast.Companion.showText
import com.deepblue.cleaning.cleanview.ConfirmDialog
import com.deepblue.cleaning.cleanview.FunctionDialog
import com.deepblue.cleaning.cleanview.TransformativeImageView
import com.deepblue.cleaning.item.PopChangeLevel
import com.deepblue.cleaning.item.PopShowChangeLevel
import com.deepblue.cleaning.utils.DialogUtils
import com.deepblue.cleaning.utils.MapUtilsB
import com.deepblue.cleaning.utils.MapUtilsB.getTransparentBitmap
import com.deepblue.cleaning.utils.NewItemTouchHelper
import com.deepblue.library.planbmsg.JsonUtils.fromJson
import com.deepblue.library.planbmsg.Response
import com.deepblue.library.planbmsg.bean.*
import com.deepblue.library.planbmsg.msg2000.GetCommonParamsReq
import com.deepblue.library.planbmsg.msg2000.GetCommonParamsRes
import com.deepblue.library.planbmsg.msg2000.IOStatesReq
import com.deepblue.library.planbmsg.msg3000.*
import com.deepblue.library.planbmsg.msg4000.ChangeTaskStatusReqClean
import com.deepblue.library.planbmsg.msg4000.GetRangePointsReq
import com.deepblue.library.planbmsg.msg4000.GetRangePointsRes
import com.deepblue.library.planbmsg.msg4000.NewTaskReq
import com.deepblue.library.planbmsg.msg5000.GetScrubberStatusRes
import com.mdx.framework.utility.AbDateUtil
import com.zrq.divider.Divider
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.startActivityForResult
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class CleanMainActivity : BaseActivity(), TransformativeImageView.ActionListener {
    var mTaskList = ArrayList<Task>()
    var taskAdapter: RecycleAdapterTask? = null
    var mFunctionDialog: FunctionDialog? = null
    var cleanmodel: Int = 0
    var selall: Boolean = false
    var mapToBitmap: Bitmap? = null
    var canvas: Canvas? = null
    var cycleTimes = 1
    var mWayPoint: WayPoint? = null
    var isDoingTask: Boolean = false
    var currentType: String = "C"
    var commonTask: CommonTask = CommonTask()
    var type_cc = 1
    var type_xd = 1
    var helper: NewItemTouchHelper? = null

    companion object {
        const val REQUEST_CODE_LOC_CHECK = 200
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        sendwebSocket(GetCommonParamsReq().toString())
        showWaite()
        isIndex = true
        sendwebSocket(DownloadMapReq().current())
        initRecyclerView()
        mFunctionDialog =
            FunctionDialog(this@CleanMainActivity, true, object : FunctionDialog.FinishBack {
                override fun dialogFinish() {
                    finish()
                }
            })
        rl_play.setOnClickListener {
//            mTaskList.toString()
            sendwebSocket(ChangeTaskStatusReqClean().stop())
            if (ll_gofilling.visibility != View.VISIBLE) {
                showText(getString(R.string.str_cddbcz))
                return@setOnClickListener
            }
            if (ll_gopullwater.visibility != View.VISIBLE) {
                showText(getString(R.string.str_bsdbcz))
                return@setOnClickListener
            }
            if (ll_gopushwater.visibility != View.VISIBLE) {
                showText(getString(R.string.str_psdbcz))
                return@setOnClickListener
            }
            if (mWayPoint == null) {
                showText(getString(R.string.str_dwdbcz))
                return@setOnClickListener
            }
            if (cleanmodel == 0) {
                showText(getString(R.string.i_choose_clean_mode))
                return@setOnClickListener
            }
            var hasTask: Boolean = false
            for (task in mTaskList) {
                if (task.checked) {
                    hasTask = true
                    break
                }
            }
            if (!hasTask) {
                showText(getString(R.string.str_qxzrw))
                return@setOnClickListener
            }
            if (Const.robotPlayStatus == RobotStatus.STATUS_IDLE) {
                confirmPlay()
            } else {
                if (Const.robotPlayStatus == RobotStatus.STATUS_ERROR) {
                    DialogUtils.showAlert(
                        this,
                        getText(R.string.error_info).toString(),
                        R.string.error_sure,
                        object : AlertDialog.DialogButtonListener {
                            override fun cancel() {

                            }

                            override fun ensure(isCheck: Boolean): Boolean {
                                return true
                            }
                        }, type = "ERROR",hasTopIcon = true
                    )
                } else {
                    showText(getString(R.string.str_fdm))
                }
            }

        }
        img_menu.setOnClickListener {
            mFunctionDialog!!.show()
        }
        rl_one.setOnClickListener {
            changeModel(1)
            showRemind(getText(R.string.sure_az).toString())
        }
        rl_one.setOnLongClickListener {
            if (Const.type == "10") {
                var mDialogPub = PopChangeLevel(ManualActivity@ this)
                var mPopShowSet = PopShowChangeLevel(ManualActivity@ this, it, mDialogPub)
                mDialogPub.set(0, type_cc, mPopShowSet, "CleanMainActivity")
                mPopShowSet.show()
                changeModel(1)
                true
            } else {
                false
            }

        }
        rl_two.setOnClickListener {
            changeModel(2)
            showRemind(getText(R.string.sure_az).toString())
        }
        rl_two.setOnLongClickListener {
            if (Const.fieldModel.equals(Const.INDOOR_MODEL.common_model.toString())) {
                var mDialogPub = PopChangeLevel(ManualActivity@ this)
                var mPopShowSet = PopShowChangeLevel(ManualActivity@ this, it, mDialogPub)
                mDialogPub.set(1, type_xd, mPopShowSet, "CleanMainActivity")
                mPopShowSet.show()
                changeModel(2)
                true
            } else false
        }
        rl_three.setOnClickListener {
            changeModel(3)
            showRemind(getText(R.string.sure_ct).toString())
        }
        ll_changemap.setOnClickListener {
            startActivity<MaplistActivity>()
        }

        img_selall.setOnClickListener {
            //选择全部或取消全部
            selall = !selall
            if (selall) {
                img_selall.setImageResource(R.drawable.ic_selchecked)
            } else {
                img_selall.setImageResource(R.drawable.ic_selnor)
            }
            selectAllRange(selall)
            showIsEnable()
        }

        img_subtraction.setOnClickListener {
            if (cycleTimes > 1) {
                cycleTimes -= 1
                tv_times.text = cycleTimes.toString()
            }
        }

        img_add.setOnClickListener {
            if (cycleTimes < 99) {
                cycleTimes += 1
                tv_times.text = cycleTimes.toString()
            }
        }

        btn_push_water.setOnClickListener {
            sendwebSocket(ChangeModeReq("auto").toString())
            dialogFragment = DialogUtils.showAlert(this,
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

        ll_gofilling.setOnClickListener {
            goDoPreTask("C")
        }

        ll_gopullwater.setOnClickListener {
            goDoPreTask("E")
        }

        ll_gopushwater.setOnClickListener {
            goDoPreTask("H")
        }
        mTextView_add_map.setOnClickListener {
            startActivity<MapsManagerActivity>()
        }
        if (Const.type != "10") {
            tv_one.text = getString(R.string.str_gxms)
            tv_three.text = getString(R.string.i_ctms)
            img_three.setImageResource(R.drawable.ic_td)
        }
        updateApk()
        timg_map.setActionListener(this)
        mSwipeRefreshLayout.setOnRefreshListener {
            img_selall.setImageResource(R.drawable.ic_selnor)
            sendwebSocket(GetCommonParamsReq().toString())
            sendwebSocket(DownloadMapReq().current())
            sendwebSocket(ChangeModeReq("auto").toString())
            sendwebSocket(GetAllMapsReq(true).toString())//查询地图列表
            mSwipeRefreshLayout.isRefreshing = false
        }

//        val stream: InputStream = resources.openRawResource(R.raw.data)
//        val reader = BufferedReader(InputStreamReader(stream))
//        var jsonStr: String? = ""
//        var line: String? = ""
//        try {
//            while (reader.readLine().also { line = it } != null) {
//                jsonStr += line
//            }
//            doMapInfo(jsonStr?:"")
//        } catch (e: IOException) {
//            e.printStackTrace()
//        }


    }

    fun showRemind(text: String) {
        if (Const.type != "10") {
            dialogFragment = DialogUtils.showAlert(
                this,
                text,
                R.string.sure,
                object : AlertDialog.DialogButtonListener {
                    override fun cancel() {

                    }

                    override fun ensure(isCheck: Boolean): Boolean {
                        return true
                    }
                }, hasRemind = false
            )
        }
    }

    fun showIsEnable() {
        rl_play.isEnabled = false
        if (ll_gofilling.visibility != View.VISIBLE) {
            return
        }
        if (ll_gopullwater.visibility != View.VISIBLE) {
            return
        }
        if (ll_gopushwater.visibility != View.VISIBLE) {
            return
        }
        if (mWayPoint == null) {
            return
        }
        if (cleanmodel == 0) {
            return
        }
        var hasTask: Boolean = false
        for (task in mTaskList) {
            if (task.checked) {
                hasTask = true
                break
            }
        }
        if (!hasTask) {
            return
        }
        rl_play.isEnabled = true
    }

    fun goDoPreTask(type: String) {
        sendwebSocket(ChangeModeReq("auto").toString())
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

    fun type2text(type: Int): String =
        when (type) {
            0 -> getString(R.string.frg_ps_qingdu)
            1 -> getString(R.string.frg_ps_biaozhun)
            2 -> getString(R.string.frg_ps_qiangli)
            else -> ""
        }

    override fun disposeMsg(type: Int, obj: Any) {
        var message = obj.toString()
        when (type) {
            0, 1, 2 -> {
                if (obj.toString().toInt() == 0) {
                    type_cc = type
                    tv_dust.text = type2text(type)
                } else {
                    type_xd = type
                    tv_clean.text = type2text(type)
                }
            }
            3 -> {
                val mapInfo = obj.toString()
                if (!TextUtils.isEmpty(mapInfo)) {
                    img_selall.setImageResource(R.drawable.ic_selnor)
                    doMapInfo(mapInfo)
                }
            }
            4 -> {
                if (selall) img_selall.performClick()
            }
            5 -> {
                realStart()
            }
            13003 -> {
                val mapJson =
                    fromJson(message, GetAllMapsRes::class.java)
                val data = mapJson?.getJson()
                if (data?.maps?.size != null && data?.maps?.size > 0) {
                    ll_changemap.visibility = View.VISIBLE
                    mRelativeLayout_list.visibility = View.VISIBLE
                    mTextView_add_map.visibility = View.GONE
                } else {
                    ll_changemap.visibility = View.GONE
                    mRelativeLayout_list.visibility = View.GONE
                    mTextView_add_map.visibility = View.VISIBLE
                }
            }
            13009 -> {
                dismissWaite()
                doMapInfo(message)
            }
            11004 -> {
                if (dialogFragment?.dialog?.isShowing == true && Const.robotPlayStatus == RobotStatus.STATUS_EMERGENCY && isDoingTask && dialogFragment?.id == 1)
                    goDoTaskOrPause(currentType, false, false)
            }

            11001 -> {
//                showRangeAndTravel()  有同步问题
            }
            14006 -> {
                dismissWaite()
                val getRangePointsRes = fromJson(message, GetRangePointsRes::class.java)
                if (getRangePointsRes?.error_code == 0) {
                    val points = getRangePointsRes?.getJson()?.points
                    val number = getRangePointsRes?.number
                    if (number > 0 && mTaskList[number - 1].travelPoints == null) {
                        mTaskList[number - 1].travelPoints = points
                    }
                    showRangeAndTravel()
                } else {
//                    Helper.toast("区域路径获取失败")
                }
                showIsEnable()
            }
            15003 -> {
                val scrubberStatusRes = fromJson(message, GetScrubberStatusRes::class.java)
                val scrubberStatus = scrubberStatusRes!!.getJson()
                scrubberStatus!!.run {
                    wv_clean_water.waveHeightPercent = clean_capacity / 100f
                    wv_dirty_water.waveHeightPercent = dirty_capacity / 100f
                    tv_clean_water.text = "$clean_capacity%"
                    tv_dirty_water.text = "$dirty_capacity%"
                }
            }
            14000 -> {
                dismissWaite()
                val res = fromJson(message, Response::class.java)
                if (res?.error_code == 0) {
//                    showText("新建任务成功")
                    startActivity<PlayActivity>("commonTask" to commonTask)
                } else {
                    showText(getString(R.string.i_rwcjsb))
                }
            }
            12017 -> {
                val result = fromJson(message, GetCommonParamsRes::class.java)?.getJson2()
                result?.min_battery_level_to_back?.let {
                    Const.min_battery_level_to_back = it
                    F.saveJson("min_battery_level_to_back", it.toString())
                }
            }
        }

    }

    fun goDoTaskOrPause(type: String, isDoTask: Boolean, isFirst: Boolean) {
        if (isFirst && Const.robotPlayStatus != RobotStatus.STATUS_IDLE) {
            sendwebSocket(ChangeTaskStatusReqClean().stop())
            showText(getString(R.string.str_fdm))
            return
        }
        currentType = type
        isDoingTask = isDoTask
        var title = ""
        when (type) {
            "C" -> {//充电
                title = getString(R.string.ing_sure_go_charge_point)
            }
            "E" -> {//加水
                title = getString(R.string.ing_sure_go_pull_point)
            }
            "H" -> {//排水
                title = getString(R.string.ing_sure_go_push_point)

            }
        }
        if (isFirst) {
            for (point in Const.map!!.points) {
                if (point.type.contains(type)) {
                    //这边做语音播报提示
                    playPubVoice(type)
                    sendwebSocket(SendGoalReq(point).toString())
                    break
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
                    sendwebSocket(ChangeTaskStatusReqClean().stop())
                    isDoingTask = false
                }

                override fun ensure(isCheck: Boolean): Boolean {
                    goDoTaskOrPause(type, !isDoTask, false)
                    return true
                }
            }, type = type
        )
        dialogFragment?.id = 1  //首页手动点击弹框id是1
    }

    fun openOrClosePushWater(open: Boolean) {
        var io_states: ArrayList<IOState> = ArrayList()
        io_states.add(IOState("sewage_valve_motor_set", if (open) 100 else 0))
//        io_states.add(IOState("sewage_motor_set", if (open) 8000 else 0))
        sendwebSocket(IOStatesReq(io_states).toString())
    }


    private fun pushingWater() {
        if (Const.robotPlayStatus != RobotStatus.STATUS_IDLE) {
            sendwebSocket(ChangeTaskStatusReqClean().stop())
            showText(getString(R.string.str_fdm))
            return
        }
        openOrClosePushWater(true)
        dialogFragment = DialogUtils.showAlert(this,
            getString(R.string.ing_push_water),
            R.string.done_push_water,
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


    override fun onResume() {
        super.onResume()
        sendwebSocket(ChangeModeReq("auto").toString())
        sendwebSocket(GetAllMapsReq(true).toString())//查询地图列表
        changeModel(cleanmodel)
        Const.robotStatus = "auto"
    }


    private fun changeModel(model: Int) {
        cleanmodel = model
        if (Const.fieldModel == Const.INDOOR_MODEL.stone_model.toString()) {
            tv_two.text = getText(R.string.stone_clean)
            tv_clean.visibility = View.GONE
            img_two.setImageResource(R.drawable.ic_icon_stone)
            rl_one.setBackgroundResource(R.drawable.btn_select_stone)
            rl_two.setBackgroundResource(R.drawable.btn_select_stone)
            rl_three.setBackgroundResource(R.drawable.btn_select_stone)
            rl_one.isSelected = model == 1
            rl_two.isSelected = model == 2
            rl_three.isSelected = model == 3
        } else {
            tv_two.text = getText(R.string.clean_model)
            tv_clean.visibility = View.VISIBLE
            img_two.setImageResource(R.drawable.ic_icon_clean)
            rl_one.setBackgroundResource(R.drawable.btn_select)
            rl_two.setBackgroundResource(R.drawable.btn_select)
            rl_three.setBackgroundResource(R.drawable.btn_select)
            rl_one.isSelected = model == 1
            rl_two.isSelected = model == 2
            rl_three.isSelected = model == 3
        }
        showIsEnable()
    }


    private fun selectAllRange(select: Boolean) {
        for (task in mTaskList) {
            task.checked = select
        }
        taskAdapter?.notifyDataSetChanged()

        if (select) {
            var all = true
            for (task in mTaskList) {
                if (task.travelPoints == null && task.range!!.range_type == MapRange.Range_Area) {
                    showWaite()
                    sendwebSocket(GetRangePointsReq(mTaskList.indexOf(task) + 1).map(task.range!!.range_id))
                    all = false
                }
            }
            if (all) {
                showRangeAndTravel()
            }
        } else {
            showRangeAndTravel()
        }
    }

    private fun initRecyclerView() {
        taskAdapter = RecycleAdapterTask(this@CleanMainActivity, mTaskList)
        taskAdapter!!.onItemChangeListener = object : RecycleAdapterTask.OnItemChangeListener {
            override fun onItemChange(position: Int, checked: Boolean) {
                var isAllSelect = true
                for (task in mTaskList) {
                    if (!task.checked) {
                        isAllSelect = false
                        break
                    }
                }
                selall = isAllSelect
                if (selall) img_selall.setImageResource(R.drawable.ic_selchecked) else img_selall.setImageResource(
                    R.drawable.ic_selnor
                )
                val task = mTaskList[position]
                if (checked && task.travelPoints == null && task.range!!.range_type == MapRange.Range_Area) {
                    showWaite()
                    sendwebSocket(GetRangePointsReq(position + 1).map(task.range!!.range_id))
                } else {
                    showRangeAndTravel()
                }
                showIsEnable()
            }
        }
        rv_task!!.addItemDecoration(
            Divider.builder()
                .color(Color.parseColor("#00ffffff"))
                .height(10)
                .build()
        )
        val layoutManager = LinearLayoutManager(this)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        rv_task!!.layoutManager = layoutManager
        rv_task!!.adapter = taskAdapter
        helper = NewItemTouchHelper(this@CleanMainActivity, taskAdapter, mTaskList)
        ItemTouchHelper(helper!!).attachToRecyclerView(rv_task)
    }


    fun doMapInfo(mapInfo: String) {
        ll_gofilling.visibility = View.GONE
        ll_gopullwater.visibility = View.GONE
        ll_gopushwater.visibility = View.GONE
        val downloadMapRes = fromJson(mapInfo, DownloadMapRes::class.java)
        if (downloadMapRes?.error_code != 0) {
//            showText("地图错误")
        } else {
            doAsync {
                val map = downloadMapRes!!.getJson()
                for (i in map!!.ranges.indices) {
                    for (k in map.ranges.get(i).point_id.indices) {
                        for (j in map.points.indices) {
                            if (map.ranges.get(i).point_id.get(k) == map.points.get(j).point_id) {
                                map.ranges.get(i).points.add(map.points[j])
                            }
                        }
                    }
                    if (map.ranges[i].range_type == MapRange.Range_Path && map.ranges[i].work_type == MapRange.Work_Forbidden) {
                        map!!.noEntryRange.add(map.ranges[i])
                    } else if (map.ranges[i].range_type == MapRange.Range_Area && map.ranges[i].work_type == MapRange.Work_Forbidden) {
                        map!!.isObstacleRange.add(map.ranges[i])
                    }
                }
                if (map != null) Const.map = map
                mTaskList = ArrayList()
                for (range in map.ranges) {
                    if (((range.range_type == MapRange.Range_Area || range.range_type == MapRange.Range_Path) && range.work_type == MapRange.Work_Work)||range.work_type == 3||range.work_type == 7) {
                        val task = Task()
                        task.range = range
                        mTaskList.add(task)
                    }
                }
                runOnUiThread {
                    Const.map?.let {
                        mapToBitmap = MapUtilsB.mapToBitmap(it, isRound = true)
                        if (mapToBitmap != null) {
                            canvas = Canvas(mapToBitmap!!)
                            sendwebSocket(ChangeNaviMapReq(map.map_info.map_id).toString())
                            showPoints()
                            //更新任务列表
                            taskAdapter?.taskList = mTaskList
                            taskAdapter?.notifyDataSetChanged()
                            tv_mapname.text = map.map_info.map_name
                            mapToBitmap?.let {
                                drawLocationPoint(it)
                                timg_map.setImageBitmap(getTransparentBitmap(it, 50))
                            }

                        }
                    }
                    helper?.updateList(mTaskList)
                }
            }


        }
    }

    private fun drawLocationPoint(mapToBitmap: Bitmap) {
        mapToBitmap?.let {
            Const.robotLoc?.run {
                paintPoint.color = Color.BLUE
                paintPoint.style = Paint.Style.STROKE
                paintPoint.isAntiAlias = true
                MapUtilsB.paintBitmap(
                    this,
                    canvas!!,
                    it,
                    Const.map!!,
                    paintPoint,
                    R.drawable.ic_location,
                    -(angle.toFloat()), act = this@CleanMainActivity
                )
            }
        }

    }

    private fun showPoints() {
        paintPoint.style = Paint.Style.STROKE
        paintPoint.isAntiAlias = true
        mWayPoint = null
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
            if (point.type.contains("D")) mWayPoint = point

            list.forEach {
                text += it + "|"
            }
            if (text.isNotEmpty()) text = text.substring(0, text.length - 1)
            if (point.type.contains("C") || point.type.contains("E") || point.type.contains("H")) {
                MapUtilsB.paintPoint(
                    point,
                    canvas!!,
                    mapToBitmap!!,
                    Const.map!!,
                    paintPoint,
                    text,
                    5f
                )
            }
        }
    }

    private val paintPoint = Paint()
    private val paintRange = Paint()
    private val paintTravel = Paint()

    private fun showRangeAndTravel() {

        try {
            doAsync {
                paintRange.color = Color.parseColor("#330000ff")
                paintRange.isAntiAlias = true

                paintTravel.color = Color.GREEN
                paintTravel.style = Paint.Style.STROKE
                paintTravel.isAntiAlias = true
                Const.map?.let { it ->
                    val copyBitmap = MapUtilsB.mapToBitmap(it, isRound = true)
                    copyBitmap?.let { bp ->
                        canvas = Canvas(bp)
                        showPoints()
                        for (task in mTaskList) {
                            if (task.checked) {
                                if (task.range!!.range_type == MapRange.Range_Area && task.travelPoints != null) {
                                    val range = MapRange()
                                    range.points = task.range!!.points
                                    MapUtilsB.paintMapPolygon(
                                        true, 1f, canvas!!,
                                        mapToBitmap!!, range, Const.map!!, paintRange, Paint()
                                    )

                                    val travel = MapRange()
                                    travel.points = task.travelPoints!!
                                    MapUtilsB.paintMapPolygon(
                                        true, 1f, canvas!!,
                                        mapToBitmap!!, travel, Const.map!!, paintTravel, Paint()
                                    )
                                } else {
                                    val range = MapRange()
                                    range.points = task.range!!.points
                                    MapUtilsB.paintMapPolygon(
                                        false, 1f, canvas!!,
                                        mapToBitmap!!, range, Const.map!!, paintTravel, Paint()
                                    )
                                }
                            }
                        }
                        drawLocationPoint(bp)
                        runOnUiThread { timg_map.setImageBitmap(getTransparentBitmap(bp, 50)) }

                    }
                }
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    /**
     * 开始任务前进行弹窗确认，如果今日已弹过并勾选不再提示，则跳过弹窗
     */
    private fun confirmPlay() {
        val dateFormat = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        val noPromptDate = robotApp!!.mSharedPreferencesHelper!![Const.SP_NO_PROMPT_DATE, ""]
        if (dateFormat.format(Date()) == noPromptDate) {
            startPlay()
            return
        }
        var currentMode = 0
        if (Const.fieldModel == Const.INDOOR_MODEL.common_model.toString()) {
            when (cleanmodel) {
                1 -> currentMode = ConfirmDialog.REMOVE_DUST
                2 -> currentMode = ConfirmDialog.FLOOR_WASHING
                3 -> currentMode = ConfirmDialog.ONE_KEY_CLEANING
            }
        } else {
            when (cleanmodel) {
                1 -> currentMode = ConfirmDialog.REMOVE_DUST
                2 -> currentMode = ConfirmDialog.STONE_WASHING
                3 -> currentMode = ConfirmDialog.ONE_KEY_WASH_CARE
            }
        }
        //弹窗确认
        val confirmDialog = ConfirmDialog(this, currentMode)
        confirmDialog.setConfirmCallback(object : ConfirmDialog.ConfirmCallback {
            override fun clickTaskBeginning() {
                confirmDialog.dismiss()
                startPlay()
            }

            override fun noMorePromptCallback(isChecked: Boolean) {
                if (isChecked) {
                    val date = dateFormat.format(Date())
                    robotApp!!.mSharedPreferencesHelper!!.put(Const.SP_NO_PROMPT_DATE, date)
                } else {
                    robotApp!!.mSharedPreferencesHelper!!.put(Const.SP_NO_PROMPT_DATE, "")
                }
            }
        })
        confirmDialog.show()
    }

    private fun startPlay() {

        startActivityForResult<LocCheckActivity>(REQUEST_CODE_LOC_CHECK)
    }

    fun getWorkPattern(): Int = when (cleanmodel) {
        1 -> {
            if (Const.type == "10") {
                when (type_cc) {
                    0 -> 3
                    1 -> 4
                    else -> 0
                }
            } else 19

        }
        2 -> {
            if (Const.fieldModel.equals(Const.INDOOR_MODEL.stone_model.toString())) 14
            else when (type_xd) {
                0 -> 8
                1 -> 9
                2 -> 10
                else -> 0
            }
        }
        3 -> {
            if (Const.type == "10") {
                if (Const.fieldModel.equals(Const.INDOOR_MODEL.stone_model.toString())) 102 else 101
            } else 28
        }

        else -> 0
    }

    private fun realStart() {
        //机器人处于待命状态才能开始任务
        if (Const.robotPlayStatus == RobotStatus.STATUS_IDLE) {
            val taskBasicInfo = TaskBasicInfo()
            taskBasicInfo.begin_date = AbDateUtil.getCurrentDate("yyyy-MM-dd")
            taskBasicInfo.begin_time = AbDateUtil.getCurrentDate("HH:mm:ss")
            taskBasicInfo.task_type = when (cleanmodel) {
                1 -> TaskBasicInfo.TASK_TYPE_VACUUM
                else -> TaskBasicInfo.TASK_TYPE_WASH
            }
            taskBasicInfo.task_status = 1
            taskBasicInfo.task_mode = TaskBasicInfo.TASK_MODE_ONCE
            taskBasicInfo.task_name = Const.map!!.map_info.map_name
            taskBasicInfo.executation_type = TaskBasicInfo.EXECUTATION_TYPE_IMMEDIATELY
            taskBasicInfo.task_priority = TaskBasicInfo.TASK_PRIORITY_NORMAL
            taskBasicInfo.executTimes = cycleTimes
            taskBasicInfo.executEndType = TaskBasicInfo.EXECUTATION_END_TYPE_COUNT
            taskBasicInfo.map_id = Const.map!!.map_info.map_id

            //
            val taskExtraInfo = TaskExtraInfo()
            taskExtraInfo.reback_id = mWayPoint?.point_id ?: 0

            commonTask = CommonTask()
            commonTask.task_basic_info = taskBasicInfo
            commonTask.task_extra_info = taskExtraInfo
            for (task in mTaskList) {
                if (task.checked) {
                    val taskRange = TaskRange()
                    taskRange.map_range_id = task.range!!.range_id
                    taskRange.task_id = 0
                    taskRange.map_id = task.range!!.map_id
                    taskRange.task_range_name = task.range!!.name
                    taskRange.work_pattern = getWorkPattern()
                    taskRange.task_range_type = task.range!!.range_type
                    commonTask.ranges.add(taskRange)
                }
            }

            if (commonTask.ranges.size > 0) {
                showWaite()
                sendwebSocket(NewTaskReq(commonTask).toString())
            } else showText(getString(R.string.str_qxzrw))

        } else {
            showText(getString(R.string.str_fdm))
        }

    }

    override fun up() {
        mSwipeRefreshLayout.isEnabled = true
    }

    override fun down() {
        mSwipeRefreshLayout.isEnabled = false
    }
}