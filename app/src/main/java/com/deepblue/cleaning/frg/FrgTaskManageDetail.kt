//
//  FrgTaskManageDetail
//
//  Created by 86139 on 2020-10-27 08:38:28
//  Copyright (c) 86139 All rights reserved.


/**

 */

package com.deepblue.cleaning.frg;

import android.app.Activity
import android.app.Dialog
import android.graphics.*
import android.os.Bundle
import android.os.Handler
import android.view.View
import com.deepblue.cleaning.Const
import com.deepblue.cleaning.F
import com.deepblue.cleaning.R
import com.deepblue.cleaning.ada.AdaPopRightDlqSon
import com.deepblue.cleaning.ada.position_xz
import com.deepblue.cleaning.cleanview.AlertDialog
import com.deepblue.cleaning.item.EditDialog
import com.deepblue.cleaning.req.RecordTaskReq
import com.deepblue.cleaning.utils.DialogUtils
import com.deepblue.cleaning.utils.MapUtilsB
import com.deepblue.library.planbmsg.JsonUtils
import com.deepblue.library.planbmsg.Response
import com.deepblue.library.planbmsg.bean.*
import com.deepblue.library.planbmsg.bean.Map
import com.deepblue.library.planbmsg.msg1000.GetRobotLocRes
import com.deepblue.library.planbmsg.msg3000.ChangeModeReq
import com.deepblue.library.planbmsg.msg3000.DownloadMapReq
import com.deepblue.library.planbmsg.msg3000.DownloadMapRes
import com.deepblue.library.planbmsg.msg3000.UploadMapReq
import com.deepblue.library.planbmsg.msg4000.GetRangePointsReq
import com.deepblue.library.planbmsg.msg4000.GetRangePointsRes
import com.deepblue.library.planbmsg.msg4000.RecordTaskRes
import com.mdx.framework.Frame
import com.mdx.framework.utility.Helper
import com.mdx.framework.view.CallBackOnly
import kotlinx.android.synthetic.main.adapter_map.*
import kotlinx.android.synthetic.main.frg_task_manage_detail.*


class FrgTaskManageDetail : BaseFrg() {
    var mapId = 0
    var mMap: Map? = null
    var canvas: Canvas? = null
    var mapToBitmap: Bitmap? = null
    var paintBlue = Paint()
    var paintGreen = Paint()
    var currentMapRange = MapRange()
    var mWayPoints: ArrayList<WayPoint> = ArrayList()
    var mRobotLocs: ArrayList<RobotLoc> = ArrayList()
    var isAddOneDot = false
    var isFirst = true
    override fun create(savedInstanceState: Bundle?) {
        setContentView(R.layout.frg_task_manage_detail)
        mapId = activity!!.intent.getIntExtra("mapId", 0)
        position_xz = -1
    }

    override fun disposeMsg(type: Int, obj: Any?) {
        super.disposeMsg(type, obj)
        when (type) {
            0 -> {//点击添加按钮
                if (mTextView_title_pop.text == getString(R.string.frg_task_manage_detail_renwudian)) {
                    var mWayPoint = WayPoint()
                    mWayPoint.name = obj.toString()
                    mWayPoint.real = 1
                    mWayPoint.type.add("P")
                    mWayPoints.add(mWayPoint)
                    isAddOneDot = true
                } else {
//                    initBitmap()
                    position_xz = -1
                    currentMapRange = MapRange()
                    currentMapRange.name = obj.toString()
//                    currentMapRange.map_id = mMap?.map_info?.map_id ?: 0
                    if (mTextView_title_pop.text == getString(R.string.i_rwlj)) {
                        showEditOrNormal(false)
                        currentMapRange.range_type = MapRange.Range_Path
                        currentMapRange.work_type = MapRange.Work_Work
                        currentMapRange.graph_type = MapRange.Graph_Polygon
                    } else if (mTextView_title_pop.text == getString(R.string.i_rwqy)) {
                        showEditOrNormal(false)
                        currentMapRange.range_type = MapRange.Range_Area
                        currentMapRange.work_type = MapRange.Work_Work
                        currentMapRange.graph_type = MapRange.Graph_Polygon
                    } else if (mTextView_title_pop.text == getString(R.string.i_gslj)) {
                        showEditOrNormal(showNomal = false, isEdit = false)
                        currentMapRange.range_type = MapRange.Range_Path
                        currentMapRange.work_type = 3
                        currentMapRange.graph_type = MapRange.Graph_Polygon
                    } else if (mTextView_title_pop.text == getString(R.string.i_dbxbh)) {
                        showEditOrNormal(showNomal = false, isEdit = false)
                        currentMapRange.range_type = MapRange.Range_Area
                        currentMapRange.work_type = 7
                        currentMapRange.graph_type = MapRange.Graph_Polygon//这块需要问下
                    }
                }

            }
            1 -> {//点击重命名按钮
                if ((mListView.adapter as AdaPopRightDlqSon)[position_xz] is MapRange) ((mListView.adapter as AdaPopRightDlqSon)[position_xz] as MapRange).name = obj.toString()
                if ((mListView.adapter as AdaPopRightDlqSon)[position_xz] is WayPoint) ((mListView.adapter as AdaPopRightDlqSon)[position_xz] as WayPoint).name = obj.toString()
                (mListView.adapter as AdaPopRightDlqSon).notifyDataSetChanged()
                editTask((mListView.adapter as AdaPopRightDlqSon)[position_xz])
            }
            2 -> {//右邊列表点击返回
                if (obj is MapRange) {
                    currentMapRange = obj //点击右边任务列表返回对象
                    if ((currentMapRange.work_type == MapRange.Work_Work && currentMapRange.range_type == MapRange.Range_Area) || currentMapRange.work_type == 7) {
//                        paintMapPolygon(currentMapRange)
//                        if (currentMapRange.points.size <= 0) {
//                            sendwebSocket(GetRangePointsReq().map(currentMapRange.range_id), false)
//                        } else {
//                            MapUtilsB.paintMapPolygon(
//                                true, canvas!!,
//                                mapToBitmap!!, currentMapRange.points, mMap!!, paintGreen
//                            )
//                        }
                    } else {
//                        paintLine(currentMapRange.point_info)
                    }
                } else {//点击点
                    initBitmap()
                    mWayPoints.forEachIndexed { index, wayPoint -> paintPoint(wayPoint, index == position_xz) }
                }
            }
            3 -> {//右边列表长按出现删除弹出框
                pubDelTsk(obj)
            }
            13009 -> {//获取地图信息
                dismissWaite()
                val downloadMapRes = JsonUtils.fromJson(
                    obj.toString(),
                    DownloadMapRes::class.java
                )
                mMap = downloadMapRes!!.getJson()
                if (mMap != null) {
                    if (isFirst) {
                        isFirst = false
                        initBitmap()
                    }
                    setPointInfoData()
                    if (Const.map != null && Const.map!!.map_info.map_id == mMap!!.map_info!!.map_id) {
                        Frame.HANDLES.sentAll("CleanMainActivity", 3, obj.toString())
                    }
                }
                if (mTextView_title_pop.text != getString(R.string.i_rwd)) setAdaData()

            }
            13010 -> {//編輯地图之后
                dismissWaite()
                val res = JsonUtils.fromJson(obj.toString(), Response::class.java)
                if (res?.error_code == 0) {
                    sendwebSocket(DownloadMapReq().map(mapId))//刷新地图数据
                } else {
                    Helper.toast(getString(R.string.save_error))
                }
            }
            14008 -> {
                //录制任务
                val recordTaskRes = JsonUtils.fromJson(obj.toString(), RecordTaskRes::class.java)
                if (recordTaskRes?.error_code == 0) {
                    when (recordTaskRes?.number) {
                        com.deepblue.library.planbmsg.msg4000.RecordTaskReq.STOP -> {
                            sendwebSocket(DownloadMapReq().map(mapId))//刷新地图数据
                        }
                    }
                } else {
                    Helper.toast(getString(R.string.i_lzsb))
                }
            }
            11001 -> {//定位点获取
                val robotLocRes =
                    JsonUtils.fromJson(
                        obj.toString(), GetRobotLocRes::class.java
                    )
                robotLocRes?.getJson()?.let {
                    if (mImageView_lz.visibility == View.VISIBLE && mImageView_lz.tag == "end") {//录制中
                        mRobotLocs.add(it)
                        if (mTextView_title_pop.text == getString(R.string.i_gslj)) {
//                            paintLine(mRobotLocs)
//                            mRobotLocs.forEach {
//                                paintPoint(it)
//                            }
                            MapUtilsB.paintLine(
                                mRobotLocs, canvas!!,
                                mapToBitmap!!, mMap!!, paintGreen
                            )
                        } else if (mTextView_title_pop.text == getString(R.string.i_dbxbh)) {
                            paintMapPolygon(mRobotLocs)
                        }
                    } else {
                        mRobotLocs.clear()
                    }
                }
                mapToBitmap?.let {
                    var bitMap = it.copy(Bitmap.Config.ARGB_8888, true)
                    drawLocationPoint(bitMap)
                    mTransformativeImageView.setImageBitmap(bitMap)
                }

            }
            14006 -> {//区域内路径获取
                dismissWaite()
                val getRangePointsRes = JsonUtils.fromJson(obj.toString(), GetRangePointsRes::class.java)
                if (getRangePointsRes?.error_code == 0) {
                    getRangePointsRes?.getJson()?.points?.let {
                        currentMapRange.points = it
                        MapUtilsB.paintMapPolygon(
                            true, canvas!!,
                            mapToBitmap!!, it, mMap!!, paintGreen
                        )
                    }
                } else {
//                    Helper.toast("区域路径获取失败")
                }
            }
        }

    }

    override fun onResume() {
        super.onResume()
        sendwebSocket(ChangeModeReq("manual").toString())
    }

    override fun onDestroy() {
        super.onDestroy()
        if (Const.robotStatus == "auto") sendwebSocket(ChangeModeReq("auto").toString())
    }

    override fun initView() {
        paintBlue.color = Color.parseColor("#32C5FF")
        paintBlue.isAntiAlias = true
        paintGreen.color = Color.GREEN
        paintGreen.style = Paint.Style.STROKE
        paintGreen.strokeWidth = 6f
        paintGreen.isAntiAlias = true
        back_rl.setOnClickListener { finish() }
        //左側初始模式選擇
        mTextView_bkms.setOnClickListener {
            mLinearLayout_leftmenu.visibility = View.GONE
            mRadioGroup.visibility = View.VISIBLE
        }
        mTextView_gsms.setOnClickListener {
            mLinearLayout_leftmenu.visibility = View.GONE
            mImageView_edit.visibility = View.INVISIBLE
            mTextView_title_pop.text = getString(R.string.i_gslj)
            setAdaData()
        }
        mTextView_bhms.setOnClickListener {
            mLinearLayout_leftmenu.visibility = View.GONE
            mImageView_edit.visibility = View.INVISIBLE
            mTextView_title_pop.text = getString(R.string.i_dbxbh)
            setAdaData()
        }


        //右侧任务列表下面三个按钮
        mImageView_add.setOnClickListener {
            var view = EditDialog(context)
            F.showCenterDialog(activity!!, view, object : CallBackOnly() {
                override fun goReturnDo(mDialog: Dialog) {
                    view.set(mDialog, "")
                }
            })
        }
        mImageView_edit.setOnClickListener {
            if (position_xz == -1) {
                Helper.toast(getString(R.string.i_xzrw))
                return@setOnClickListener
            }
            if (mTextView_title_pop.text == getString(R.string.i_gslj) || mTextView_title_pop.text == getString(R.string.i_dbxbh)) {//点击编辑按钮相当于重做
                initBitmap()
                currentMapRange.point_info.clear()
                showEditOrNormal(showNomal = false, isEdit = false)
            } else {
                showEditOrNormal(false)
//                currentMapRange = (mListView.adapter as AdaPopRightDlqSon)[position_xz] as MapRange
            }

        }
        mImageView_t.setOnClickListener {
            if (position_xz == -1) {
                Helper.toast(getString(R.string.i_xzrw))
                return@setOnClickListener
            }
            var view = EditDialog(context)
            F.showCenterDialog(activity!!, view, object : CallBackOnly() {
                override fun goReturnDo(mDialog: Dialog) {
                    if ((mListView.adapter as AdaPopRightDlqSon)[position_xz] is MapRange) {
                        view.set(mDialog, ((mListView.adapter as AdaPopRightDlqSon)[position_xz] as MapRange).name)
                    } else if ((mListView.adapter as AdaPopRightDlqSon)[position_xz] is WayPoint) {
                        view.set(mDialog, ((mListView.adapter as AdaPopRightDlqSon)[position_xz] as WayPoint).name)
                    }
                }
            })

        }
        //左侧任务点  路径  区域
        mRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.mRadioButton_dian -> {
                    mTextView_title_pop.text = getString(R.string.i_rwd)
                }
                R.id.mRadioButton_lj -> {
                    mTextView_title_pop.text = getString(R.string.i_rwlj)
                }
                R.id.mRadioButton_qy -> {
                    mTextView_title_pop.text = getString(R.string.i_rwqy)
                }
            }
            Helper.closeSoftKey(context, mRadioGroup)
            setPointInfoData()
            setAdaData()
        }
//右面操作栏按钮
        mTextView_delete.setOnClickListener {
            if (currentMapRange.range_id != 0) {
                pubDelTsk(currentMapRange)
            } else {
                initBitmap()
                showEditOrNormal()
            }
        }
        mTextView_tl.setOnClickListener {
            mLinearLayout_tl.visibility = if (mLinearLayout_tl.visibility == View.VISIBLE) View.GONE else View.VISIBLE
        }
        mTextView_cx.setOnClickListener {
            if (currentMapRange != null) {
                if (currentMapRange!!.point_info.size > 0) {
                    currentMapRange!!.point_info.removeAt(currentMapRange!!.point_info.size - 1)
                    doCLickPub()
                }
            }
        }
        mTextView_cz.setOnClickListener {
            currentMapRange.point_info?.clear()
////            initBitmap()
            doCLickPub()
        }
        mTextView_bc.setOnClickListener {
            editTask(currentMapRange)
//            showEditOrNormal()
        }
        //录制按钮
        mImageView_lz.setOnClickListener {
            if (mImageView_lz.tag == "start") {//開始录制
                mImageView_lz.setBackgroundResource(R.drawable.ic_t_end)
                mImageView_lz.tag = "end"
                sendwebSocket(RecordTaskReq().start(currentMapRange.name, mapId, if (mTextView_title_pop.text == getString(R.string.i_gslj)) 1 else 2))//刷新地图数据
            } else {
                mImageView_lz.tag = "start"
                mImageView_lz.setBackgroundResource(R.drawable.ic_t_start)
                sendwebSocket(RecordTaskReq().stop(currentMapRange.name, mapId, if (mTextView_title_pop.text == getString(R.string.i_gslj)) 1 else 2))
                showEditOrNormal(isEdit = false)
            }
        }
        //地图点击
        mTransformativeImageView.setListener(object : com.deepblue.cleaning.cleanview.Listener {
            override fun onClick(pointF: PointF) {
                var mMapPoint = MapUtilsB.pointerToMap(pointF, mTransformativeImageView, mMap!!)
                if (mLinearLayout_right_pop_cz.visibility == View.VISIBLE) {//地图可编辑
                    mMapPoint?.let {
                        val rangePoint = RangePoint()
                        rangePoint.x = it.x
                        rangePoint.y = it.y
                        rangePoint.type = RangePoint.TYPE_NEW
                        currentMapRange.point_info?.add(rangePoint)
                    }
                    doCLickPub()
                } else if (isAddOneDot) {
                    mWayPoints[mWayPoints.size - 1].x = mMapPoint?.x ?: 0.0
                    mWayPoints[mWayPoints.size - 1].y = mMapPoint?.y ?: 0.0
                    initBitmap()
                    position_xz = mWayPoints.size - 1
                    mWayPoints.forEachIndexed { index, wayPoint -> paintPoint(wayPoint, index == position_xz) }
                    (mListView.adapter as AdaPopRightDlqSon).add(mWayPoints[mWayPoints.size - 1])
                    (mListView.adapter as AdaPopRightDlqSon).notifyDataSetChanged()
                    editTask(mWayPoints[mWayPoints.size - 1])
                    isAddOneDot = false
                }
            }

            override fun onTouched(pointF: PointF) {

            }

            override fun onLongClick(pointF: PointF) {
            }

            override fun onMoveOver(pointF: PointF) {
            }

            override fun onDoubleClick(pointF: PointF) {
            }

            override fun onMove(pointF: PointF) {

            }

            override fun onScaleChanged(scale: Float) {
            }

        })
    }

    override fun loaddata() {
        sendwebSocket(DownloadMapReq().map(mapId))

//        var message =
//            "{\"error_code\":0,\"json\":{\"map_info\":{\"map_id\":2190,\"map_name\":\"1234\",\"max_pos\":{\"x\":17.355872165411711,\"y\":22.410618733614683},\"min_pos\":{\"x\":-17.984127044677734,\"y\":-13.88938045501709},\"picture\":\"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\",\"resolution\":0.029999999329447746},\"points\":[{\"angle\":0,\"coordinates_type\":2,\"name\":\"P1\",\"point_id\":828,\"real\":1,\"type\":[\"D\"],\"x\":0.73737766381714209,\"y\":0.26172396500919604},{\"angle\":0,\"coordinates_type\":2,\"name\":\"P2\",\"point_id\":829,\"real\":1,\"type\":[\"E\"],\"x\":-7.8385526254529054,\"y\":7.060257748596996},{\"angle\":0,\"coordinates_type\":2,\"name\":\"P3\",\"point_id\":830,\"real\":1,\"type\":[\"C\",\"H\"],\"x\":-7.4288742898833675,\"y\":4.0295858192674814},{\"angle\":0,\"coordinates_type\":2,\"name\":\"\",\"point_id\":831,\"real\":0,\"type\":[\"P\"],\"x\":-3.6052092142161314,\"y\":0.94430817826787461},{\"angle\":0,\"coordinates_type\":2,\"name\":\"\",\"point_id\":832,\"real\":0,\"type\":[\"P\"],\"x\":-1.0652040830013902,\"y\":2.1085623026353879},{\"angle\":0,\"coordinates_type\":2,\"name\":\"\",\"point_id\":833,\"real\":0,\"type\":[\"P\"],\"x\":-4.6976850809106736,\"y\":6.5863148880966946},{\"angle\":0,\"coordinates_type\":2,\"name\":\"\",\"point_id\":834,\"real\":0,\"type\":[\"P\"],\"x\":-7.7566175076553918,\"y\":5.1119313761298599}],\"ranges\":[{\"graph_type\":1,\"name\":\"G1\",\"point_id\":[831,832,833,834],\"range_id\":62,\"range_type\":1,\"work_type\":1}]},\"type\":13009}"
//        val downloadMapRes = JsonUtils.fromJson(
//            message,
//            DownloadMapRes::class.java
//        )
//        mMap = downloadMapRes!!.getJson()
//        if (mMap != null) {
//            initBitmap()
//            for (i in mMap!!.ranges.indices) {
//                for (k in mMap!!.ranges[i].point_id.indices) {
////                    for (j in mMap!!.points.indices) {
////                        if (mMap!!.ranges[i].point_id[k] == mMap!!.points[j].point_id) {
////                            mMap!!.ranges[i].points.add(mMap!!.points[j])
////                        }
////                    }
//                    val rangePoint =
//                        MapUtilsB.findRangePointFromPoints(mMap!!.ranges[i].point_id[k], mMap!!.points)
//                            ?: continue
//                    mMap!!.ranges[i].point_info.add(rangePoint)
//
//                }
//            }
//        }
    }

    fun setAdaData() {
        position_xz = -1
        currentMapRange = MapRange()
        initBitmap()
        if (mTextView_title_pop.text == getString(R.string.i_rwd)) {
            mImageView_edit.visibility = View.INVISIBLE
            showEditOrNormal()
            mWayPoints.clear()
            mMap?.points?.forEach {
                if (it.type.contains("P") && it.real == 1) {
                    mWayPoints.add(it)
                    paintPoint(it)
                }
            }
            mListView.adapter = AdaPopRightDlqSon(context, mWayPoints)
        } else {
            val ranges = ArrayList<MapRange>()
            if (mTextView_title_pop.text == getString(R.string.i_rwlj)) {
                mImageView_edit.visibility = View.VISIBLE
                showEditOrNormal()
                mMap?.ranges?.forEach {
                    if (it.work_type == MapRange.Work_Work && it.range_type == MapRange.Range_Path) {
                        ranges.add(it)
                        paintLine(it.point_info, false)
                    }
                }
            } else if (mTextView_title_pop.text == getString(R.string.i_rwqy)) {
                mImageView_edit.visibility = View.VISIBLE
                showEditOrNormal()
                mMap?.ranges?.forEach {
                    if (it.work_type == MapRange.Work_Work && it.range_type == MapRange.Range_Area) {
                        ranges.add(it)
                        paintMapPolygon(it, false)
                    }
                }
            } else if (mTextView_title_pop.text == getString(R.string.i_gslj)) {
                mRobotLocs.clear()
                showEditOrNormal(isEdit = false)
                mMap?.ranges?.forEach {
                    if (it.work_type == 3) {
                        ranges.add(it)
//                        paintLine(it.point_info, false)
                        MapUtilsB.paintLine(
                            it.point_info, canvas!!,
                            mapToBitmap!!, mMap!!, paintGreen
                        )
//                        it.point_info.forEach {dot->
//                            paintPoint(dot)
//                        }
                    }
                }
            } else if (mTextView_title_pop.text == getString(R.string.i_dbxbh)) {
                mRobotLocs.clear()
                showEditOrNormal(isEdit = false)
                mMap?.ranges?.forEach {
                    if (it.work_type == 7) {
                        ranges.add(it)
                        paintMapPolygon(it, false)
                    }
                }
            }
            mListView.adapter = AdaPopRightDlqSon(context, ranges)
        }
    }

    fun setPointInfoData() {
        currentMapRange.point_info.clear()
        for (i in mMap!!.ranges.indices) {
            mMap!!.ranges[i].point_info.clear()
            for (k in mMap!!.ranges[i].point_id.indices) {
                val rangePoint = MapUtilsB.findRangePointFromPoints(mMap!!.ranges[i].point_id[k], mMap!!.points) ?: continue
                mMap!!.ranges[i].point_info.add(rangePoint)
            }
        }
    }

    fun doCLickPub() {
        currentMapRange.let {
            initBitmap()
            if (mTextView_title_pop.text == getString(R.string.i_rwlj)) {
                if (currentMapRange.range_id == 0) {
                    (mListView.adapter as AdaPopRightDlqSon).list.forEach {
                        paintLine((it as MapRange).point_info, false)
                    }
                    paintLine(currentMapRange.point_info, false)
                } else {
                    (mListView.adapter as AdaPopRightDlqSon).list.forEach {
                        if ((it as MapRange).range_id == currentMapRange.range_id) {
                            paintLine(currentMapRange.point_info, false)
                        } else {
                            paintLine(it.point_info, false)
                        }
                    }
                }
            } else if (mTextView_title_pop.text == getString(R.string.i_rwqy)) {
                if (currentMapRange.range_id == 0) {
                    (mListView.adapter as AdaPopRightDlqSon).list.forEach {
                        paintMapPolygon(it as MapRange, false)
                    }
                    paintMapPolygon(currentMapRange, false)
                } else {
                    (mListView.adapter as AdaPopRightDlqSon).list.forEach {
                        if ((it as MapRange).range_id == currentMapRange.range_id) {
                            paintMapPolygon(currentMapRange, false)
                        } else {
                            paintMapPolygon(it, false)
                        }
                    }
                }


            }
        }
    }

    fun initBitmap() {
        MapUtilsB.mapToBitmap(mMap!!)?.let {
            mapToBitmap = it
            canvas = Canvas(it)
            mTransformativeImageView.setImageBitmap(it)
        }
    }

    fun drawLocationPoint(bitmap: Bitmap?) {
        Const.robotLoc?.run {
            if (bitmap != null) {
                val paintPoint = Paint()
                paintPoint.color = Color.BLUE
                paintPoint.style = Paint.Style.STROKE
                paintPoint.isAntiAlias = true
                MapUtilsB.paintBitmap(
                    this,
                    Canvas(bitmap),
                    bitmap,
                    mMap!!,
                    paintPoint,
                    R.drawable.ic_location,
                    -(angle.toFloat()), act = context!!
                )
            }

        }

    }

    fun paintMapPolygon(polygon: MapRange, needInit: Boolean = true)//绘制区域
    {
        if (needInit) initBitmap()
        MapUtilsB.paintMapPolygonS(
            true, 1f, canvas!!,
            mapToBitmap!!, polygon, mMap!!, paintBlue, Paint()
        )
        polygon.point_info.forEach {
            paintPoint(it)
        }
    }

    fun paintMapPolygon(points: List<MapPoint>, needInit: Boolean = true, offDistance: Int = 0)//绘制区域
    {
        if (needInit) initBitmap()
        MapUtilsB.paintMapPolygon(
            true, canvas!!,
            mapToBitmap!!, points, mMap!!, paintBlue, offDistance
        )
        points.forEach {
            paintPoint(it, offDistance = offDistance)
        }
    }

    fun paintLine(points: List<MapPoint>, needInit: Boolean = true)//绘制折綫
    {
        if (needInit) initBitmap()
        MapUtilsB.paintLine(
            points, canvas!!,
            mapToBitmap!!, mMap!!, paintGreen
        )
        points.forEach {
            paintPoint(it)
        }
    }

    fun paintPoint(point: MapPoint, isFocused: Boolean = false, offDistance: Int = 0)//绘制點
    {
        MapUtilsB.paintPoint(
            point,
            canvas!!,
            mapToBitmap!!,
            mMap!!,
            paintBlue,
            "",
            if (isFocused) 10f else 5f, offDistance = offDistance
        )
    }


    fun showEditOrNormal(showNomal: Boolean = true, isEdit: Boolean = true) {//显示编辑或者正常状态,默认是编辑模式
        mLinearLayout_right_pop_list.visibility = if (showNomal) View.VISIBLE else View.GONE
        if (isEdit) {
            mLinearLayout_right_pop_cz.visibility = if (showNomal) View.GONE else View.VISIBLE
        } else {
            mImageView_lz.visibility = if (showNomal) View.GONE else View.VISIBLE
        }
    }


    fun deleteTask(id: Int, isRange: Boolean = true) {

        val map = MapEdit()
//        map!!.map_info = mMap!!.map_info
        if (isRange) map.ranges_del.add(id) else map.points_del.add(id)
        sendwebSocket(UploadMapReq(map).toString())
    }

//    fun saveTask(item: Any) {
//        val map = MapEdit()
//        if (item is MapRange) {
//            if (item.point_id.isEmpty() && item.point_info.isNotEmpty()) {
//                for (j in 0 until item.point_id.size) {
//                    val rangePoint =
//                        MapUtilsB.findRangePointFromPoints(item.point_id[j], mMap!!.points)
//                            ?: continue
//                    item.point_info.add(rangePoint)
//                }
//            }
//            map.ranges.add(item)
//        } else if (item is WayPoint) {
//            map.points.add(item)
//        }
//        sendwebSocket(UploadMapReq(map).toString(), false)
//    }

    fun editTask(item: Any) {
        val map = MapEdit()
//        map.map_info = MapInfoBase()
//        map!!.map_info!!.map_id = mMap!!.map_info.map_id
        if (item is MapRange) {
            item.map_id = mapId
            map.ranges.add(item)
        } else if (item is WayPoint) {
            item.map_id = mapId
            map.points.add(item)
        }
        sendwebSocket(UploadMapReq(map).toString())
    }

    fun pubDelTsk(item: Any?) {
        DialogUtils.showAlert(activity!!,
            String.format(getString(R.string.robot_name), mTextView_title_pop.text),
            R.string.sure_delete,
            object : AlertDialog.DialogButtonListener {
                override fun cancel() {

                }

                override fun ensure(isCheck: Boolean): Boolean {
                    position_xz = -1
                    currentMapRange = MapRange()
//                    if (mTextView_title_pop.text != getString(R.string.i_rwd)) initBitmap()
                    (mListView.adapter as AdaPopRightDlqSon).remove(item)
                    (mListView.adapter as AdaPopRightDlqSon).notifyDataSetChanged()
                    if (item is MapRange) {
//                        showEditOrNormal()
                        deleteTask(item.range_id)
                    } else if (item is WayPoint) {
                        mWayPoints.remove(item)
                        mWayPoints.forEachIndexed { index, wayPoint -> paintPoint(wayPoint, index == position_xz) }
                        deleteTask(item.point_id, false)
                    }

                    return true
                }
            })
    }


    override fun onSuccess(data: String?, method: String) {
    }

}