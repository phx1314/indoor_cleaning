package com.deepblue.cleaning.activity

import android.graphics.*
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.SeekBar
import android.widget.Toast
import com.deepblue.cleaning.Const
import com.deepblue.cleaning.R
import com.deepblue.cleaning.adapter.NoEntryAdapter
import com.deepblue.cleaning.adapter.ObstacleAdapter
import com.deepblue.cleaning.adapter.ShowPointsAdapter
import com.deepblue.cleaning.cleanview.AlertDialog
import com.deepblue.cleaning.cleanview.DegreeWheel
import com.deepblue.cleaning.cleanview.Listener
import com.deepblue.cleaning.utils.DialogUtils
import com.deepblue.cleaning.utils.MapUtilsB
import com.deepblue.library.planbmsg.JsonUtils
import com.deepblue.library.planbmsg.Response
import com.deepblue.library.planbmsg.bean.*
import com.deepblue.library.planbmsg.bean.Map
import com.deepblue.library.planbmsg.msg3000.ChangeModeReq
import com.deepblue.library.planbmsg.msg3000.DownloadMapReq
import com.deepblue.library.planbmsg.msg3000.DownloadMapRes
import com.deepblue.library.planbmsg.msg3000.UploadMapReq
import com.mdx.framework.utility.Helper
import kotlinx.android.synthetic.main.activity_editmap.*
import org.jetbrains.anko.doAsync
import java.io.InputStream
import java.nio.charset.Charset

class EditMapActivity : BaseActivity() {
    private var mapId: Int = 0
    private var isAdd: Boolean = false
    private var isNonetry: Boolean = false
    private var isObstacle: Boolean = false
    private var isEraser: Boolean = false
    private var isEraserScale: Boolean = true
    private var isMeasure: Boolean = false
    private var addpoint_status = 0  //0默认 ，1 添加；2添加中；3编辑;4 编辑中
    private var point_type: String = ""
    private var editPoint: WayPoint? = null
    private var bitmap: Bitmap? = null
    private var elementBitmap: Bitmap? = null
    private var addpointAngle: Double = 0.0
    private var showPoints = ArrayList<WayPoint>()
    private var pointAdapter: ShowPointsAdapter? = null
    private var delrulers: ArrayList<MapPoint>? = ArrayList()
    private var delbrushes: ArrayList<ArrayList<Map.Brush>>? = ArrayList()
    private var delobstacle: ArrayList<MapPoint>? = ArrayList()
    private var mScale: Float = 1f
    private var measureBitmap: Bitmap? = null
    private var radius: Int = 6
    private var mapToBitmap: Bitmap? = null

    private var noEntryAdapter: NoEntryAdapter? = null
    private var noEntryRanges: ArrayList<MapRange>? = ArrayList()
    private var noentryStatus: Int = 0 //0 默认，1 添加；2编辑；3删除
    private var newNetry: MapRange? = null
    private var newRangePoint: ArrayList<MapPoint>? = null

    private var obstaclePoints: ArrayList<MapPoint>? = null
    private var obstacleRanges: ArrayList<MapRange>? = ArrayList()
    private var obstacleStatus: Int = 0 //0默认 ，1添加;2 闭合；
    private var obstacleAdapter: ObstacleAdapter? = null
    private var newObstacle: MapRange? = null
    private var mMap: Map? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_editmap)
        mapId = intent.getIntExtra("map_id", 0)
        showWaite()
        pointAdapter = ShowPointsAdapter(this@EditMapActivity, showPoints)
        lv_content.adapter = pointAdapter
        pointAdapter!!.setItemClickCallback(object : ShowPointsAdapter.ItemClickCallback {
            override fun itemClick(bean: WayPoint, item: Int) {
//                if ((addpoint_status == 1 || addpoint_status == 2 || addpoint_status == 5) && editPoint != null) {
//                    Toast.makeText(
//                        this@EditMapActivity,
//                        getText(R.string.pls_addpoint).toString(),
//                        Toast.LENGTH_SHORT
//                    ).show()
//                } else {
//                    for (pp in mMap!!.points) {
//                        if (pp.point_id == bean.point_id) {
//                            pp.isFocused = true
//                            addpoint_status = 3
//                            timg_map.setGraffiti(false)
//                            ll_ptype.visibility = View.GONE
//                            degreeWheel.visibility = View.GONE
//                            img_edit.setImageResource(R.drawable.edit)
//                            img_add.setImageResource(R.drawable.add)
//                            editPoint = bean
//                            addpointAngle = bean.angle
//                        } else {
//                            pp.isFocused = false
//                        }
//                    }
//                    pointAdapter!!.updateData(item)
//
//                    setBitmap(
//                        MapUtilsB.showElementMap(
//                            this@EditMapActivity,
//                            mMap!!, noEntryRanges!!, obstacleRanges!!
//                        )
//                    )
//                }

            }

            override fun onLongItemClick(bean: WayPoint, item: Int) {
//                if ((addpoint_status == 1 || addpoint_status == 2 || addpoint_status == 5) && editPoint != null) {
//                    Toast.makeText(
//                        this@EditMapActivity,
//                        getText(R.string.pls_addpoint).toString(),
//                        Toast.LENGTH_SHORT
//                    ).show()
//                } else {
                dialogFragment = DialogUtils.showAlert(this@EditMapActivity,
                    getString(
                        R.string.sure_delete_content,
                        showPoints.get(item).name
                    ).toString(),
                    android.R.string.ok,
                    object : AlertDialog.DialogButtonListener {
                        override fun cancel() {

                        }

                        override fun ensure(isCheck: Boolean): Boolean {
                            doDel(bean.point_id)
                            return true
                        }
                    })
//                }

            }

        })


        noEntryAdapter = NoEntryAdapter(this@EditMapActivity, noEntryRanges)
        lv_nonetry.adapter = noEntryAdapter
        noEntryAdapter!!.setItemClickCallback(object : NoEntryAdapter.ItemClickCallback {
            override fun itemClick(bean: MapRange, item: Int) {
                for (entry in noEntryRanges!!) {
                    entry.isFocused = bean.name == entry.name
                    if (entry.points.size < 2) {
                        noEntryRanges!!.remove(entry)
                        if (newRangePoint != null) {
                            newRangePoint!!.clear()
                        }
                    }
                }
                runOnUiThread {
                    noEntryAdapter!!.updateData(item)
                    noentryStatus = 2
                    timg_map.setGraffiti(false)
                    nonetry_add.setImageResource(R.drawable.add)
                    setBitmap(
                        MapUtilsB.showElementMap(
                            this@EditMapActivity,
                            mMap!!,
                            noEntryRanges!!,
                            obstacleRanges!!
                        )
                    )
                }


            }

            override fun onLongItemClick(bean: MapRange, item: Int) {
                dialogFragment = DialogUtils.showAlert(this@EditMapActivity,
                    getString(
                        R.string.sure_delete_content,
                        noEntryRanges!![item].name
                    ),
                    android.R.string.ok,
                    object : AlertDialog.DialogButtonListener {
                        override fun cancel() {

                        }

                        override fun ensure(isCheck: Boolean): Boolean {
                            runOnUiThread {
                                noEntryRanges!!.remove(noEntryRanges!![item])
                                for (entry in noEntryRanges!!) {
                                    entry.isFocused = false
                                    if (entry.points.size < 2) {
                                        noEntryRanges!!.remove(entry)
                                        if (newRangePoint != null) {
                                            newRangePoint!!.clear()
                                        }
                                    }
                                }
                                noEntryAdapter!!.updateData(-1)
                                noentryStatus = 3
                                timg_map.setGraffiti(false)
                                nonetry_add.setImageResource(R.drawable.add)
                                setBitmap(
                                    MapUtilsB.showElementMap(
                                        this@EditMapActivity,
                                        mMap!!,
                                        noEntryRanges!!,
                                        obstacleRanges!!
                                    )
                                )
                                nonetry_save.performClick()
                            }
                            return true
                        }

                    })
            }

        })


        obstacleAdapter = ObstacleAdapter(this@EditMapActivity, obstacleRanges)
        lv_obstacle.adapter = obstacleAdapter
        obstacleAdapter!!.setItemClickCallback(object : ObstacleAdapter.ItemClickCallback {
            override fun itemClick(bean: MapRange, item: Int) {
                for (obstacle in obstacleRanges!!) {
                    obstacle.isFocused = bean.name == obstacle.name
                }
                runOnUiThread {
                    obstacleAdapter!!.updateData(item)
                    timg_map.setGraffiti(false)
                    nonetry_add.setImageResource(R.drawable.add)
                    setBitmap(
                        MapUtilsB.showElementMap(
                            this@EditMapActivity,
                            mMap!!,
                            noEntryRanges!!,
                            obstacleRanges!!
                        )
                    )
                }
            }

            override fun onLongItemClick(bean: MapRange, item: Int) {
                dialogFragment = DialogUtils.showAlert(this@EditMapActivity,
                    getString(
                        R.string.sure_delete_content,
                        obstacleRanges!![item].name
                    ),
                    android.R.string.ok,
                    object : AlertDialog.DialogButtonListener {
                        override fun cancel() {

                        }

                        override fun ensure(isCheck: Boolean): Boolean {
                            showWaite()
                            val map = MapEdit()
                            map.ranges_del.add(obstacleRanges!![item].range_id)
                            val uploadMapReq = UploadMapReq(map).toString()
                            sendwebSocket(uploadMapReq)
                            return true
                        }

                    })
            }

        })



        back_rl.setOnClickListener {
            finish()
        }
        timg_map.setListener(object : Listener {
            override fun onClick(pointF: PointF) {
            }

            override fun onTouched(pointF: PointF) {
                val point = MapPoint()
                point.x = pointF.x.toDouble()
                point.y = pointF.y.toDouble()
                val mapPoint = MapUtilsB.pToCanvas(timg_map, point)
                val pX = mapPoint.x * mMap!!.resolution
                val pY = mapPoint.y * mMap!!.resolution
                if (pX > 0 && pX < mMap!!.map_info.max_pos.x - mMap!!.map_info.min_pos.x
                    && pY > 0 && pY < mMap!!.map_info.max_pos.y - mMap!!.map_info.min_pos.y
                ) {

//                    if (isAdd && addpoint_status != 0 && point_type != null && point_type.isNotEmpty()) {
//                        doAddTouch(mapPoint)
//                    }
                    if (isMeasure) {
                        doMeasureTouch(mapPoint)
                    } else if (isEraser) {
                        doEraserTouch(mapPoint)
                    } else if (isNonetry && noentryStatus == 1) {
                        doNoentryTouch(mapPoint)
                    } else if (isObstacle && obstacleStatus == 1) {
                        doObstacleTouch(mapPoint)
                    }
                }
            }

            override fun onLongClick(pointF: PointF) {
            }

            override fun onMoveOver(pointF: PointF) {
            }

            override fun onDoubleClick(pointF: PointF) {
            }

            override fun onMove(pointF: PointF) {
                val point = MapPoint()
                point.x = pointF.x.toDouble()
                point.y = pointF.y.toDouble()
                val mapPoint = MapUtilsB.pToCanvas(timg_map, point)
                val pX = mapPoint.x * mMap!!.resolution
                val pY = mapPoint.y * mMap!!.resolution
                if (pX > 0 && pX < mMap!!.map_info.max_pos.x - mMap!!.map_info.min_pos.x
                    && pY > 0 && pY < mMap!!.map_info.max_pos.y - mMap!!.map_info.min_pos.y
                ) {
//                    if (isAdd && addpoint_status != 0 && editPoint != null) {
//                        doAddMove(mapPoint)
//                    }
                    if (isMeasure) {
                        doMeasureMove(mapPoint)
                    } else if (isEraser) {
                        doEraserMove(mapPoint)
                    } else if (isNonetry && noentryStatus == 1) {
                        doNoentryMove(mapPoint)
                    } else if (isObstacle && obstacleStatus == 1) {
                        doObstacleMove(mapPoint)
                    }
                }
            }

            override fun onScaleChanged(scale: Float) {
                mScale = scale
            }

        })

        degreeWheel.setListener(
            object : DegreeWheel.Listener {
                override fun onRadians(radians: Double) {
                    addpointAngle = radians
                    if (addpoint_status == 2) {
                        editPoint!!.angle = addpointAngle
                        mMap!!.points[mMap!!.points.size - 1] = editPoint!!
                    } else if (addpoint_status == 4 || addpoint_status == 5) {
                        addpoint_status = 5
                        editPoint!!.angle = addpointAngle
                        for (index in mMap!!.points.indices) {
                            if (editPoint!!.point_id == mMap!!.points[index].point_id) {
                                mMap!!.points[index].angle = addpointAngle
                            }
                        }
                    }
                    runOnUiThread {
                        setBitmap(
                            MapUtilsB.showElementMap(
                                this@EditMapActivity,
                                mMap!!,
                                noEntryRanges!!,
                                obstacleRanges!!
                            )
                        )
                    }
                }

            })
        ll_point.setOnClickListener {
            checkMeasure()
            if (checkEraser()) {
                Toast.makeText(
                    this@EditMapActivity,
                    getText(R.string.pls_eraser).toString(),
                    Toast.LENGTH_SHORT
                ).show()
            } else if (checkNonetry()) {
                Toast.makeText(
                    this@EditMapActivity,
                    getText(R.string.pls_noentry).toString(),
                    Toast.LENGTH_SHORT
                ).show()
            } else if (checkObstacle()) {
                Toast.makeText(
                    this@EditMapActivity,
                    getText(R.string.pls_obstacle).toString(),
                    Toast.LENGTH_SHORT
                ).show()
            } else if (isAdd && (addpoint_status == 2 || addpoint_status == 5)) {
                Toast.makeText(
                    this@EditMapActivity,
                    getText(R.string.pls_addpoint).toString(),
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                timg_map.setGraffiti(false)
                if (isAdd) {
                    isAdd = false
                    img_point.setImageResource(R.drawable.selector_point)
                    degreeWheel.visibility = View.GONE
                    ll_ptype.visibility = View.GONE
                    ll_right.visibility = View.GONE
                } else {
                    img_point.setImageResource(R.drawable.point_sel)
                    isAdd = true
                    addpoint_status = 1
                    img_point.isSelected = true
                    ll_right.visibility = View.VISIBLE
                    ll_ptype.visibility = View.VISIBLE
                }
                img_edit.setImageResource(R.drawable.edit)
                for (points in mMap!!.points) {
                    points.isFocused = false
                }
                pointAdapter!!.updateData(-1)
                setBitmap(
                    MapUtilsB.showElementMap(
                        this@EditMapActivity,
                        mMap!!,
                        noEntryRanges!!,
                        obstacleRanges!!
                    )
                )
            }
        }

        ll_eraser.setOnClickListener {
            checkMeasure()
//            if (isEraser && (mMap!!.brushes != null && mMap!!.brushes!!.size > 0 && mMap!!.brushes!![0].size > 0)) {
//                Toast.makeText(
//                    this@EditMapActivity,
//                    getText(R.string.pls_eraser).toString(),
//                    Toast.LENGTH_SHORT
//                ).show()
//            } else
            if (checkNonetry()) {
                Toast.makeText(
                    this@EditMapActivity,
                    getText(R.string.pls_noentry).toString(),
                    Toast.LENGTH_SHORT
                ).show()
            } else if (checkObstacle()) {
                Toast.makeText(
                    this@EditMapActivity,
                    getText(R.string.pls_obstacle).toString(),
                    Toast.LENGTH_SHORT
                ).show()
            } else if (checkAddpoint()) {
                Toast.makeText(
                    this@EditMapActivity,
                    getText(R.string.pls_addpoint).toString(),
                    Toast.LENGTH_SHORT
                ).show()
            } else {
//                if (isEraser) {
//                    isEraser = false
//                    timg_map.setGraffiti(false)
//                    img_eraser.setColorFilter(Color.WHITE);
//                    rl_seekbar.visibility = View.GONE
//                    setBitmap(
//                        MapUtilsB.showElementMap(
//                            this@EditMapActivity,
//                            mMap!!,
//                            noEntryRanges!!,
//                            obstacleRanges!!
//                        )
//                    )
//                } else {
//                    isEraser = true
//                    timg_map.setGraffiti(true)
//                    img_eraser.setColorFilter(Color.parseColor("#34C5FF"));
//                    rl_seekbar.visibility = View.VISIBLE
//                    setBitmap(bitmap)
//                    rl_tool.visibility = View.VISIBLE
//                    ll_save.visibility = View.VISIBLE
//                    ll_revoke.visibility = View.VISIBLE
//                    ll_redo.visibility = View.VISIBLE
//                }
//                if (!isEraser) {
//                    setBitmap(bitmap)
//                }
                if (img_eraser.getTag() == 0) {
                    setBitmap(bitmap)
                    if (mMap!!.brushes != null) {
                        mMap!!.brushes!!.clear()
                    }
                }
                rl_seekbar.visibility = View.VISIBLE
                rl_tool.visibility = View.VISIBLE
                ll_save.visibility = View.VISIBLE
                ll_revoke.visibility = View.VISIBLE
                ll_redo.visibility = View.VISIBLE
                if (isEraserScale) {
                    isEraser = false
                    img_eraser.setTag(1)
                    img_eraser.setColorFilter(Color.parseColor("#39DE77"))
                } else {
                    img_eraser.setTag(2)
                    isEraser = true
                    img_eraser.setColorFilter(Color.parseColor("#34C5FF"))
                }
                isEraserScale = !isEraserScale
                timg_map.setGraffiti(isEraserScale)

                delbrushes!!.clear()
            }


        }

        ll_measure.setOnClickListener {
            if (checkEraser()) {
                Toast.makeText(
                    this@EditMapActivity,
                    getText(R.string.pls_eraser).toString(),
                    Toast.LENGTH_SHORT
                ).show()
            } else if (checkNonetry()) {
                Toast.makeText(
                    this@EditMapActivity,
                    getText(R.string.pls_noentry).toString(),
                    Toast.LENGTH_SHORT
                ).show()
            } else if (checkObstacle()) {
                Toast.makeText(
                    this@EditMapActivity,
                    getText(R.string.pls_obstacle).toString(),
                    Toast.LENGTH_SHORT
                ).show()
            } else if (checkAddpoint()) {
                Toast.makeText(
                    this@EditMapActivity,
                    getText(R.string.pls_addpoint).toString(),
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                if (isMeasure) {
                    if (delrulers != null) {
                        delrulers!!.clear()
                    }
                    mMap!!.rulers!!.clear()
                    isMeasure = false
                    mgv_map.visibility = View.GONE
                    timg_map.setGraffiti(false)
                    img_measure.setImageResource(R.drawable.selector_measure)
                    rl_tool.visibility = View.GONE
                    setBitmap(
                        MapUtilsB.showElementMap(
                            this@EditMapActivity,
                            mMap!!, noEntryRanges!!, obstacleRanges!!
                        )
                    )
                } else {
                    if (delrulers != null) {
                        delrulers!!.clear()
                    }
                    mMap!!.rulers!!.clear()
                    measureBitmap = MapUtilsB.showElementMap(
                        this@EditMapActivity,
                        mMap!!,
                        noEntryRanges!!,
                        obstacleRanges!!
                    )
                    setBitmap(measureBitmap)
                    isMeasure = true
                    mgv_map.init(mMap!!.resolution, mScale, 1f)
                    mgv_map.visibility = View.VISIBLE
                    timg_map.setGraffiti(true)
                    img_measure.setImageResource(R.drawable.measure_sel)
                    rl_tool.visibility = View.VISIBLE
                    ll_revoke.visibility = View.VISIBLE
                    ll_redo.visibility = View.VISIBLE
                    ll_save.visibility = View.GONE
                }
            }


        }

        ll_nonetry.setOnClickListener {
            checkMeasure()
            if (checkEraser()) {
                Toast.makeText(
                    this@EditMapActivity,
                    getText(R.string.pls_eraser).toString(),
                    Toast.LENGTH_SHORT
                ).show()
            } else if (isNonetry && ((newRangePoint != null && newRangePoint!!.size > 0) || noentryStatus == 3)) {
                Toast.makeText(
                    this@EditMapActivity,
                    getText(R.string.pls_noentry).toString(),
                    Toast.LENGTH_SHORT
                ).show()
            } else if (checkObstacle()) {
                Toast.makeText(
                    this@EditMapActivity,
                    getText(R.string.pls_obstacle).toString(),
                    Toast.LENGTH_SHORT
                ).show()
            } else if (checkAddpoint()) {
                Toast.makeText(
                    this@EditMapActivity,
                    getText(R.string.pls_addpoint).toString(),
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                timg_map.setGraffiti(false)
                if (isNonetry) {
                    isNonetry = false
                    for (rang in noEntryRanges!!) {
                        rang.isFocused = false
                    }
                    noEntryAdapter!!.updateData(-1)
                    img_nonetry.setImageResource(R.drawable.selector_nonetry)
                    ll_rightnonetry.visibility = View.GONE
                } else {
                    isNonetry = true
                    img_nonetry.setImageResource(R.drawable.nonetry_sel)
                    ll_rightnonetry.visibility = View.VISIBLE
                }
                setBitmap(
                    MapUtilsB.showElementMap(
                        this@EditMapActivity,
                        mMap!!,
                        noEntryRanges!!,
                        obstacleRanges!!
                    )
                )
            }
        }

        ll_obstacle.setOnClickListener {
            checkMeasure()
            if (checkEraser()) {
                Toast.makeText(
                    this@EditMapActivity,
                    getText(R.string.pls_eraser).toString(),
                    Toast.LENGTH_SHORT
                ).show()
            } else if (checkNonetry()) {
                Toast.makeText(
                    this@EditMapActivity,
                    getText(R.string.pls_noentry).toString(),
                    Toast.LENGTH_SHORT
                ).show()
            } else if (isObstacle && obstacleStatus > 0) {
                Toast.makeText(
                    this@EditMapActivity,
                    getText(R.string.pls_obstacle).toString(),
                    Toast.LENGTH_SHORT
                ).show()
            } else if (checkAddpoint()) {
                Toast.makeText(
                    this@EditMapActivity,
                    getText(R.string.pls_addpoint).toString(),
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                timg_map.setGraffiti(false)
                if (isObstacle) {
                    isObstacle = false
                    img_obstacle.setImageResource(R.drawable.selector_obstacle)
                    right_obstacle.visibility = View.GONE
                } else {
                    isObstacle = true
                    img_obstacle.setImageResource(R.drawable.obstacle_sel)
                    right_obstacle.visibility = View.VISIBLE
                }
                for (obstacle in obstacleRanges!!) {
                    obstacle.isFocused = false
                    obstacle.isopen = false
                }
                obstacleAdapter!!.updateData(-1)
                setBitmap(
                    MapUtilsB.showElementMap(
                        this@EditMapActivity,
                        mMap!!,
                        noEntryRanges!!,
                        obstacleRanges!!
                    )
                )

            }
        }

        img_add.setOnClickListener {
//            if (addpoint_status == 1 || addpoint_status == 2 || addpoint_status == 5) {
////                img_add.setImageResource(R.drawable.add)
//                addpoint_status = 0
//                ll_ptype.visibility = View.GONE
//                degreeWheel.visibility = View.GONE
//                if (editPoint != null) {
//                    savePoint()
//                }
//            } else {
////                img_add.setImageResource(R.drawable.add_sel)
//                addpoint_status = 1
//                editPoint = null
//                degreeWheel.visibility = View.GONE
//                if (point_type != null && point_type.length > 0) {
//                    timg_map.setGraffiti(true)
//                }
//
//            }
//            img_edit.setImageResource(R.drawable.edit)
//            for (pp in mMap!!.points) {
//                pp.isFocused = false
//            }
//            setBitmap(
//                MapUtilsB.showElementMap(
//                    this@EditMapActivity,
//                    mMap!!,
//                    noEntryRanges!!,
//                    obstacleRanges!!
//                )
//            )
//            pointAdapter!!.updateData(-1)
            if (point_type != "") {
                Const.robotLoc?.let {
                    doAddTouch(it)
                }
            } else {
                Helper.toast(getString(R.string.i_qxzdwlx))
            }

        }

        img_edit.setOnClickListener {
            if (addpoint_status == 3) {
                addpoint_status = 4
                timg_map.setGraffiti(true)
                ll_ptype.visibility = View.GONE
                degreeWheel.visibility = View.VISIBLE
                if (editPoint != null) {
                    degreeWheel.setDegree(editPoint!!.angle.toInt())
                }
                img_edit.setImageResource(R.drawable.edit_sel)
            } else if (addpoint_status == 4) {
                addpoint_status = 3
                timg_map.setGraffiti(false)
                ll_ptype.visibility = View.GONE
                degreeWheel.visibility = View.GONE
                img_edit.setImageResource(R.drawable.edit)
            } else if (addpoint_status == 5) {
                savePoint()
            }


        }

        img_save.setOnClickListener {
            if (isAdd && (addpoint_status == 2 || addpoint_status == 4 || addpoint_status == 5)) {
                savePoint()
            }
        }

        tv_location.setOnClickListener {
            if (addpoint_status != 1) {
                Helper.toast(getString(R.string.i_qxbcd))
                return@setOnClickListener
            }
            timg_map.setGraffiti(true)
            point_type = WayPoint.Type_Relocation
            tv_location.isSelected = true
            tv_psd.isSelected = false
            tv_bsd.isSelected = false
            tv_cdd.isSelected = false
        }
        tv_psd.setOnClickListener {
            if (addpoint_status != 1) {
                Helper.toast(getString(R.string.i_qxbcd))
                return@setOnClickListener
            }
            timg_map.setGraffiti(true)
            point_type = WayPoint.Type_HWater
            tv_location.isSelected = false
            tv_psd.isSelected = true
            tv_bsd.isSelected = false
            tv_cdd.isSelected = false
        }
        tv_bsd.setOnClickListener {
            if (addpoint_status != 1) {
                Helper.toast(getString(R.string.i_qxbcd))
                return@setOnClickListener
            }
            timg_map.setGraffiti(true)
            point_type = WayPoint.Type_EWater
            tv_location.isSelected = false
            tv_psd.isSelected = false
            tv_bsd.isSelected = true
            tv_cdd.isSelected = false
        }
        tv_cdd.setOnClickListener {
            if (addpoint_status != 1) {
                Helper.toast(getString(R.string.i_qxbcd))
                return@setOnClickListener
            }
            timg_map.setGraffiti(true)
            point_type = WayPoint.Type_Charge
            tv_location.isSelected = false
            tv_psd.isSelected = false
            tv_bsd.isSelected = false
            tv_cdd.isSelected = true
        }


        nonetry_add.setOnClickListener {
            if (isNonetry) {
                if (noentryStatus != 1) {
                    noentryStatus = 1
                    timg_map.setGraffiti(true)
                    nonetry_add.setImageResource(R.drawable.add_sel)
                    for (range in noEntryRanges!!) {
                        range.isFocused = false
                    }
                    runOnUiThread {
                        setBitmap(
                            MapUtilsB.showElementMap(
                                this@EditMapActivity,
                                mMap!!,
                                noEntryRanges!!,
                                obstacleRanges!!
                            )
                        )
                    }
                    newNoentryRange()
                }

            }
        }

        nonetry_save.setOnClickListener {
            if ((haseditRange() && isNonetry && noEntryRanges!!.size > 0) || noentryStatus == 3) {
                showWaite(30000, getText(R.string.saving).toString())
                val map = MapEdit()
                val ranges = ArrayList<MapRange>()
                for (noEntry in noEntryRanges!!) {
                    if (noEntry.range_id == 0 && noEntry.points.size > 1) {
                        Log.e("sss", newRangePoint!!.size.toString() + "@@@9999")
                        Log.e("sss", noEntry.points.size.toString() + "@@@888")
                        for (point in noEntry.points) {
                            val rangeP = RangePoint()
                            rangeP.x = point.x
                            rangeP.y = point.y
                            noEntry.point_info.add(rangeP)
                        }
                        ranges.add(noEntry)
                    }
                }

                map.ranges.addAll(ranges)
                if (inDelList().size > 0) {
                    map.ranges_del.addAll(inDelList())
                }
                val uploadMapReq = UploadMapReq(map).toString()
                sendwebSocket(uploadMapReq)
            }
        }

        obstacle_add.setOnClickListener {
            if (isObstacle) {
                if (obstacleStatus != 1) {
                    obstacleStatus = 1
                    obstacle_add.setImageResource(R.drawable.add_sel)
                    timg_map.setGraffiti(true)
                    for (range in obstacleRanges!!) {
                        range.isFocused = false
                    }
                    runOnUiThread {
                        setBitmap(
                            MapUtilsB.showElementMap(
                                this@EditMapActivity,
                                mMap!!,
                                noEntryRanges!!,
                                obstacleRanges!!
                            )
                        )
                    }
                    newObstacleRange()
                }
            }
        }

        ll_revoke.setOnClickListener {
            if (isMeasure && mMap!!.rulers != null && mMap!!.rulers!!.size > 0) {
                val ruler = mMap!!.rulers!![mMap!!.rulers!!.size - 1]
                delrulers!!.add(ruler)
                mMap!!.rulers!!.remove(ruler)
                setBitmap(
                    MapUtilsB.relureToBitmap(
                        this@EditMapActivity,
                        measureBitmap!!,
                        mMap!!
                    )
                )
            } else if (isEraser && mMap!!.brushes != null && mMap!!.brushes!!.size > 0) {
                val eraser = mMap!!.brushes!![mMap!!.brushes!!.size - 1]
                delbrushes!!.add(eraser)
                mMap!!.brushes!!.remove(eraser)
                runOnUiThread {
                    setBitmap(
                        MapUtilsB.brushesToBitmap(
                            bitmap!!,
                            mMap!!.brushes, 1f
                        )
                    )
                }
            } else if (isObstacle && obstacleStatus > 0) {
                if (obstaclePoints != null && obstaclePoints!!.size > 0) {
                    val lastPoint = obstaclePoints!![obstaclePoints!!.size - 1]
                    obstaclePoints!!.remove(lastPoint)
                    delobstacle!!.add(lastPoint)
                    newObstacle!!.points.clear()
                    newObstacle!!.points.addAll(obstaclePoints!!)
                    obstacleStatus = 1
                    newObstacle!!.isopen = true
                    img_close.setImageResource(R.drawable.selector_close)
                } else {
                    obstacleRanges!!.removeAt(obstacleRanges!!.size - 1)
                    obstacleAdapter!!.updateData(-1)
                    rl_tool.visibility = View.GONE
                    right_obstacle.visibility = View.VISIBLE
                    obstacleStatus = 0
                    timg_map.setGraffiti(false)
                    obstacle_add.setImageResource(R.drawable.add)
                }

                runOnUiThread {
                    setBitmap(
                        MapUtilsB.showElementMap(
                            this@EditMapActivity,
                            mMap!!,
                            noEntryRanges!!,
                            obstacleRanges!!
                        )
                    )
                }

            }
        }

        ll_redo.setOnClickListener {
            if (isMeasure) {
//                val rulers = delrulers!![delrulers!!.size - 1]
//                mMap!!.rulers!!.add(rulers)
//                delrulers!!.remove(rulers)
                delrulers!!.clear()
                mMap!!.rulers.clear()
                setBitmap(
                    MapUtilsB.relureToBitmap(
                        this@EditMapActivity,
                        measureBitmap!!,
                        mMap!!
                    )
                )
            } else if (isEraser) {
//                val brush = delbrushes!![delbrushes!!.size - 1]
//                mMap!!.brushes!!.add(brush)
//                delbrushes!!.remove(brush)
                mMap!!.brushes?.clear()
                delbrushes?.clear()
                setBitmap(
                    MapUtilsB.brushesToBitmap(
                        bitmap!!,
                        mMap!!.brushes,
                        1f
                    )
                )
            } else if (isObstacle) {
//                if (delobstacle!!.isNotEmpty()) {
//                    val lastPoint = delobstacle!![delobstacle!!.size - 1]
//                    delobstacle!!.remove(lastPoint)
//                    obstaclePoints!!.add(lastPoint)
//                }
                delobstacle?.clear()
                obstaclePoints?.clear()
                newObstacle!!.points.clear()
//                newObstacle!!.points.addAll(obstaclePoints!!)
                runOnUiThread {
                    setBitmap(
                        MapUtilsB.showElementMap(
                            this@EditMapActivity,
                            mMap!!,
                            noEntryRanges!!,
                            obstacleRanges!!
                        )
                    )
                }
            }
        }

        ll_close.setOnClickListener {
            if (isObstacle) {
                if (obstacleStatus == 1) {
                    if (obstaclePoints != null && obstaclePoints!!.size > 2) {
                        obstacleStatus = 2
                        newObstacle!!.isopen = false
                        img_close.setImageResource(R.drawable.close_sel)
                        setBitmap(
                            MapUtilsB.showElementMap(
                                this@EditMapActivity,
                                mMap!!,
                                noEntryRanges!!,
                                obstacleRanges!!
                            )
                        )
                    } else {
                        Toast.makeText(
                            this@EditMapActivity,
                            getText(R.string.close_error),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else if (obstacleStatus == 2) {
                    obstacleStatus = 1
                    img_close.setImageResource(R.drawable.selector_close)
                    newObstacle!!.isopen = true
                    setBitmap(
                        MapUtilsB.showElementMap(
                            this@EditMapActivity,
                            mMap!!,
                            noEntryRanges!!,
                            obstacleRanges!!
                        )
                    )
                }
            }
        }

        ll_save.setOnClickListener {
            if (isEraser && mMap!!.brushes != null && mMap!!.brushes!!.size > 0) {
                savePicMap()
            } else if (isObstacle && obstacleStatus > 0) {
                if (obstaclePoints != null && obstaclePoints!!.size > 2) {
                    showWaite(30000, getText(R.string.saving).toString())
                    val map = MapEdit()
                    val ranges = ArrayList<MapRange>()

                    for (point in obstaclePoints!!) {
                        val rangeP = RangePoint()
                        rangeP.x = point.x
                        rangeP.y = point.y
                        newObstacle!!.point_info.add(rangeP)
                    }
                    ranges.add(newObstacle!!)
                    map.ranges.clear()
                    map.ranges.addAll(ranges)
                    val uploadMapReq = UploadMapReq(map).toString()
                    sendwebSocket(uploadMapReq)
                } else {
                    Toast.makeText(
                        this@EditMapActivity,
                        getText(R.string.close_error),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        vsb_px.setOnSeekBarChangeListener(
            object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    radius = progress + 1
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {
                }

                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                }

            })


//        val mInputStream: InputStream = this.resources.openRawResource(R.raw.data)
//        val buffer = ByteArray(mInputStream.available())
//        mInputStream.read(buffer)
////  将字符数组转换为UTF-8编码的字符串
//        //  将字符数组转换为UTF-8编码的字符串
//        val json = String(buffer, Charset.defaultCharset())
//        val downloadMapRes = JsonUtils.fromJson(json, DownloadMapRes::class.java)
//        val map = downloadMapRes!!.getJson()
//        if (noEntryRanges != null) {
//            noEntryRanges!!.clear()
//        }
//        if (obstacleRanges != null) {
//            obstacleRanges!!.clear()
//        }
//
//        for (i in map!!.ranges.indices) {
//            for (k in map.ranges[i].point_id.indices) {
//                for (j in map.points.indices) {
//                    if (map.ranges[i].point_id[k] == map.points[j].point_id) {
//                        map!!.ranges[i].points.add(map.points[j])
//                    }
//                }
//            }
//
//            if (map.ranges[i].range_type == MapRange.Range_Path && map.ranges[i].work_type == MapRange.Work_Forbidden) {
//                noEntryRanges!!.add(map.ranges[i])
//                map!!.noEntryRange.add(map.ranges[i])
//            } else if (map.ranges[i].range_type == MapRange.Range_Area && map.ranges[i].work_type == MapRange.Work_Forbidden) {
//                obstacleRanges!!.add(map.ranges[i])
//                map!!.isObstacleRange.add(map.ranges[i])
//            }
//        }
//        mMap = map
//        runOnUiThread {
//            dismissWaite()
//            tv_title.text = mMap!!.map_info.map_name
//            bitmap = MapUtilsB.mapToBitmap2(mMap!!, isRound = false)
//            elementBitmap = MapUtilsB.showElementMap(
//                this@EditMapActivity,
//                mMap!!,
//                noEntryRanges!!,
//                obstacleRanges!!
//            )
//            setBitmap(elementBitmap)
//            timg_map.setGraffiti(false)
//            editPoint = null
//            showPoints.clear()
//            for (pp in mMap!!.points) {
//                if (WayPoint.Type_Relocation in pp.type || WayPoint.Type_EWater in pp.type ||
//                    WayPoint.Type_HWater in pp.type || WayPoint.Type_Charge in pp.type
//                ) {
//                    showPoints.add(pp)
//                }
//            }
//            pointAdapter!!.updateData(-1)
//            noEntryAdapter!!.updateData(-1)
//            obstacleAdapter!!.updateData(-1)
//            if (isAdd) {
//                addpoint_status = 0
//                ll_ptype.visibility = View.GONE
//                degreeWheel.visibility = View.GONE
//                img_add.setImageResource(R.drawable.add)
//                img_edit.setImageResource(R.drawable.edit)
//            } else if (isEraser) {
//                isEraser = false
//                img_eraser.setColorFilter(Color.parseColor("#ffffff"))
//                delbrushes!!.clear()
//                rl_seekbar.visibility = View.GONE
//                rl_tool.visibility = View.GONE
//            } else if (isNonetry) {
//                newRangePoint = null
//                noentryStatus = 0
//                nonetry_add.setImageResource(R.drawable.add)
//            } else if (isObstacle) {
//                obstacleStatus = 0
//                obstaclePoints = null
//                newObstacle = null
//                delobstacle!!.clear()
//                right_obstacle.visibility = View.VISIBLE
//                rl_tool.visibility = View.GONE
//                obstacle_add.setImageResource(R.drawable.add)
//            }
//        }
    }


    private fun haseditRange(): Boolean {
        for (range in noEntryRanges!!) {
            if (range.range_id == 0) {
                return true
            }
        }
        return false
    }


    fun checkAddpoint(): Boolean {
        if (isAdd) {
            if (addpoint_status == 2 || addpoint_status == 5) {
                return true
            } else {
                img_edit.setImageResource(R.drawable.edit)
                pointAdapter!!.updateData(-1)
                for (index in mMap!!.points.indices) {
                    mMap!!.points[index].isFocused = false
                }
                img_add.setImageResource(R.drawable.add)
            }
            addpoint_status = 0
            isAdd = false
            img_point.setImageResource(R.drawable.selector_point)
            degreeWheel.visibility = View.GONE
            ll_ptype.visibility = View.GONE
            ll_right.visibility = View.GONE
            ll_ptype.visibility = View.GONE

        }
        return false
    }

    fun newNoentryRange() {
        newNetry = MapRange()
        newNetry!!.range_id = 0
        newNetry!!.map_id = mMap!!.map_info.map_id
        newNetry!!.range_type = 2
        newNetry!!.work_type = 2
        newNetry!!.graph_type = 1
        newNetry!!.isFocused = true
        var index = 0
        if (noEntryRanges!!.size > 0) {
            val range = noEntryRanges!![noEntryRanges!!.size - 1]
            if (range.name.isNotEmpty() && range.name.length > 3) {
                index = range.name.substring(3, range.name.length).toInt()
            }
        }
        newNetry!!.name = getString(R.string.nonetry_name, (index + 1).toString())
        noEntryRanges!!.add(newNetry!!)
        newRangePoint = ArrayList<MapPoint>()
        noEntryAdapter!!.updateData(noEntryRanges!!.size - 1)
        lv_nonetry.setSelection(noEntryRanges!!.size - 1)
    }

    fun newObstacleRange() {
        newObstacle = MapRange()
        newObstacle!!.range_id = 0
        newObstacle!!.map_id = mMap!!.map_info.map_id
        newObstacle!!.range_type = 1
        newObstacle!!.work_type = 2
        newObstacle!!.graph_type = 1
        newObstacle!!.isFocused = true
        newObstacle!!.isopen = true

        var index = 0
        if (obstacleRanges!!.size > 0) {
            val range = obstacleRanges!![obstacleRanges!!.size - 1]
            if (range.name.isNotEmpty() && range.name.length > 3) {
                index = range.name.substring(3, range.name.length).toInt()
            }
        }
        newObstacle!!.name = getString(R.string.obstacle_name, (index + 1).toString())
        obstacleRanges!!.add(newObstacle!!)
        obstaclePoints = ArrayList<MapPoint>()
        obstacleAdapter!!.updateData(obstacleRanges!!.size - 1)
        lv_obstacle.setSelection(obstacleRanges!!.size - 1)
        right_obstacle.visibility = View.GONE
        rl_tool.visibility = View.VISIBLE
        ll_revoke.visibility = View.VISIBLE
        ll_redo.visibility = View.VISIBLE
        ll_close.visibility = View.VISIBLE
        img_close.setImageResource(R.drawable.selector_close)
        ll_save.visibility = View.VISIBLE
    }

    fun checkMeasure() {
        if (isMeasure) {
            if (delrulers != null) {
                delrulers!!.clear()
            }
            mMap!!.rulers!!.clear()
            isMeasure = false
            timg_map.setGraffiti(false)
            mgv_map.visibility = View.GONE
            img_measure.setImageResource(R.drawable.selector_measure)
            rl_tool.visibility = View.GONE
        }
    }

    fun checkNonetry(): Boolean {
        if (isNonetry) {
            if ((newRangePoint != null && newRangePoint!!.size > 0) || noentryStatus == 3) {
                return true
            }

            isNonetry = false
            noentryStatus = 0
            nonetry_add.setImageResource(R.drawable.add)
            img_nonetry.setImageResource(R.drawable.selector_nonetry)
            ll_rightnonetry.visibility = View.GONE
            rl_tool.visibility = View.GONE
            for (range in noEntryRanges!!) {
                range.isFocused = false
            }
            noEntryAdapter!!.updateData(-1)
        }
        return false
    }

    fun checkEraser(): Boolean {
        if (img_eraser.getTag() != 0) {
            if (mMap!!.brushes != null && mMap!!.brushes!!.size > 0 && mMap!!.brushes!![0].size > 0) {
                return true
            }
            isEraser = false
            isEraserScale = true
            img_eraser.setTag(0)
            img_eraser.setColorFilter(Color.parseColor("#ffffff"))
            rl_seekbar.visibility = View.GONE
            rl_tool.visibility = View.GONE
        }

        return false
    }

    fun checkObstacle(): Boolean {
        if (isObstacle && obstacleStatus > 0) {
            return true
        } else {
            isObstacle = false
            img_obstacle.setImageResource(R.drawable.selector_obstacle)
            right_obstacle.visibility = View.GONE
            rl_tool.visibility = View.GONE
            ll_revoke.visibility = View.GONE
            ll_redo.visibility = View.GONE
            ll_close.visibility = View.GONE
            ll_save.visibility = View.GONE
            obstacleStatus = 0
            obstaclePoints = null
            newObstacle = null
            delobstacle!!.clear()
            for (obstacle in obstacleRanges!!) {
                obstacle.isFocused = false
                obstacle.isopen = false
            }
        }

        return false
    }

    fun doDel(id: Int) {
        showWaite(30000, getText(R.string.saving).toString())
        val map = MapEdit()
        map.points_del.clear()
        map.points_del.add(id)
        val uploadMapReq = UploadMapReq(map).toString()
        sendwebSocket(uploadMapReq)
    }

    fun inDelList(): ArrayList<Int> {
        var listInt = ArrayList<Int>()
        for (range in mMap!!.noEntryRange) {
            var hasdel: Boolean = false
            for (noEntry in noEntryRanges!!) {
                if (range.range_id == noEntry.range_id) {
                    hasdel = true
                    break
                }
            }
            if (!hasdel) {
                listInt.add(range.range_id)
            }
        }
        return listInt
    }


    //添加点位_touched
    fun doAddTouch(mapPoint: AngleMapPoint) {
        if (addpoint_status == 1 || addpoint_status == 2) {
            editPoint = WayPoint()
//                MapUtilsB.bitmapToMap(mapPoint, timg_map, mMap!!)
            editPoint?.x = mapPoint.x
            editPoint?.y = mapPoint.y
            editPoint?.angle = mapPoint.angle
            editPoint!!.isFocused = true
            editPoint!!.type.add(point_type)
            editPoint!!.real = 1
            editPoint!!.map_id = mMap!!.map_info.map_id
//            var index: Int = 1
            var lastname: String = ""
            for (p in showPoints) {
                if (point_type in p.type) {
                    lastname = p.name
                }
            }
//            if (lastname.length > 3) {
//                if (addpoint_status == 1) {
//                    index = lastname.substring(3, lastname.length).toInt() + 1
//                } else if (addpoint_status == 2) {
//                    index = lastname.substring(3, lastname.length).toInt()
//                }
//
//            }
            if (!TextUtils.isEmpty(lastname) && addpoint_status == 1) {
//                Helper.toast("此类型点位已存在")
                handler.removeCallbacksAndMessages(null)
                mTextView_warning.visibility = View.VISIBLE
                handler.postDelayed({ mTextView_warning.visibility = View.GONE }, 2000)
                return
            }
            when (point_type) {
                WayPoint.Type_Charge -> {
                    editPoint!!.name = getString(R.string.cdd)
                }
                WayPoint.Type_Relocation -> {
                    editPoint!!.name = getString(R.string.dwd)
                }
                WayPoint.Type_EWater -> {
                    editPoint!!.name = getString(R.string.bsd)
                }
                WayPoint.Type_HWater -> {
                    editPoint!!.name = getString(R.string.psd)
                }
            }
            if (addpoint_status == 1) {
                mMap!!.points.add(editPoint!!)
                addpoint_status = 2
            } else if (addpoint_status == 2) {
                mMap!!.points[mMap!!.points.size - 1] = editPoint!!
            }

        } else if (addpoint_status == 4 || addpoint_status == 5) {
            addpoint_status = 5

//            val point = MapUtilsB.bitmapToMap(mapPoint, timg_map, mMap!!)

            for (index in mMap!!.points.indices) {
                if (editPoint!!.point_id == mMap!!.points[index].point_id) {
                    mMap!!.points[index].x = mapPoint!!.x
                    mMap!!.points[index].y = mapPoint!!.y
                    mMap!!.points[index].angle = mapPoint.angle
                }
            }

        }
        runOnUiThread {
//            if (addpoint_status == 2) {
//                ll_ptype.visibility = View.GONE
//                degreeWheel.visibility = View.VISIBLE
//                if (editPoint != null) {
//                    degreeWheel.setDegree(editPoint!!.angle.toInt())
//                }
//            }
            setBitmap(
                MapUtilsB.showElementMap(
                    this@EditMapActivity,
                    mMap!!,
                    noEntryRanges!!,
                    obstacleRanges!!
                )
            )
        }

    }

    //添加点位_move
    fun doAddMove(mapPoint: MapPoint) {
        val movePoint =
            MapUtilsB.bitmapToMap(mapPoint, timg_map, mMap!!)
        if (addpoint_status == 2) {
            mMap!!.points[mMap!!.points.size - 1].x = movePoint!!.x
            mMap!!.points[mMap!!.points.size - 1].y = movePoint!!.y
        } else if (addpoint_status == 4 || addpoint_status == 5) {
            addpoint_status = 5
            val point =
                MapUtilsB.bitmapToMap(mapPoint, timg_map, mMap!!)
            for (index in mMap!!.points.indices) {
                if (editPoint!!.point_id == mMap!!.points[index].point_id) {
                    mMap!!.points[index].x = point!!.x
                    mMap!!.points[index].y = point!!.y
                    mMap!!.points[index].angle = addpointAngle
                }
            }
        }
        runOnUiThread {
            if (addpoint_status == 2) {
                ll_ptype.visibility = View.GONE
                degreeWheel.visibility = View.VISIBLE
                if (editPoint != null) {
                    degreeWheel.setDegree(editPoint!!.angle.toInt())
                }
            }
            setBitmap(
                MapUtilsB.showElementMap(
                    this@EditMapActivity,
                    mMap!!,
                    noEntryRanges!!,
                    obstacleRanges!!
                )
            )
        }
    }

    fun setBitmap(bitmap: Bitmap?) {
        mapToBitmap = bitmap
        mapToBitmap?.let {
            timg_map.setImageBitmap(it)
        }
    }

    fun doEraserTouch(mapPoint: MapPoint) {
        if (mMap?.brushes != null) {
            if (mMap?.brushes!!.size == 0) {
                mMap?.brushes!!.add(ArrayList())
            } else if (mMap?.brushes!![mMap?.brushes!!.size - 1].size > 0) {
                mMap?.brushes!!.add(ArrayList())
            }
        } else {
            mMap?.brushes = ArrayList()
            mMap?.brushes!!.add(ArrayList())
        }
    }

    fun doEraserMove(mapPoint: MapPoint) {
        val brush = Map.Brush()
        brush.x = mapPoint.x.toFloat()
        brush.y = mapPoint.y.toFloat()
        brush.color = Color.WHITE
        brush.radius = radius.toFloat()
        if (mMap!!.brushes == null) {
            mMap!!.brushes = ArrayList()
            mMap?.brushes!!.add(ArrayList())
        }
        mMap!!.brushes!![mMap!!.brushes!!.size - 1].add(brush)

        runOnUiThread {

            setBitmap(
//                MapUtilsB.brushesToBitmap(
//                    bitmap!!,
//                    mMap!!.brushes, 1f
//                )
                MapUtilsB.brushesToBitmap(
                    bitmap!!,
                    mMap!!.brushes, 1f
                )
            )
        }
    }

    fun doMeasureTouch(mapPoint: MapPoint) {
        mMap?.rulers!!.add(mapPoint)
        if (delrulers != null) {
            delrulers!!.clear()
        }
        if (measureBitmap != null) {
            runOnUiThread {
                setBitmap(
                    MapUtilsB.relureToBitmap(
                        this@EditMapActivity,
                        measureBitmap!!,
                        mMap!!
                    )
                )
            }
        }

    }

    fun doMeasureMove(mapPoint: MapPoint) {
        if (mMap?.rulers != null && mMap?.rulers!!.size > 1) {
            mMap?.rulers!![mMap?.rulers!!.size - 1] = mapPoint
            if (measureBitmap != null) {
                runOnUiThread {
                    setBitmap(
                        MapUtilsB.relureToBitmap(
                            this@EditMapActivity,
                            measureBitmap!!,
                            mMap!!
                        )
                    )
                }
            }
        }
    }

    fun doNoentryTouch(mapPoint: MapPoint) {
        val mm = MapUtilsB.bitmapToMap(mapPoint, timg_map, mMap!!)
        if (newRangePoint != null && newRangePoint!!.size > 1) {
            for (range in noEntryRanges!!) {
                range.isFocused = false
            }
            runOnUiThread {
                newNoentryRange()
                if (mm != null) {
                    val rangePoint = RangePoint()
                    rangePoint.x = mm.x
                    rangePoint.y = mm.y
                    newRangePoint!!.add(rangePoint)
                }
            }

        } else {
            if (mm != null) {
                val rangePoint = RangePoint()
                rangePoint.x = mm.x
                rangePoint.y = mm.y
                newRangePoint!!.add(rangePoint)
            }
        }

        runOnUiThread {
            newNetry!!.points.clear()
            newNetry!!.points.addAll(newRangePoint!!)
            setBitmap(
                MapUtilsB.showElementMap(
                    this@EditMapActivity,
                    mMap!!,
                    noEntryRanges!!,
                    obstacleRanges!!
                )
            )
        }

    }

    fun doNoentryMove(mapPoint: MapPoint) {
        val mm = MapUtilsB.bitmapToMap(mapPoint, timg_map, mMap!!)
        if (mm != null) {
            val index = newRangePoint!!.size - 1
            newRangePoint!![index]!!.x = mm.x
            newRangePoint!![index]!!.y = mm.y

        }

        runOnUiThread {
            setBitmap(
                MapUtilsB.showElementMap(
                    this@EditMapActivity,
                    mMap!!,
                    noEntryRanges!!,
                    obstacleRanges!!
                )
            )
        }
    }

    fun doObstacleTouch(mapPoint: MapPoint) {
        val mm = MapUtilsB.bitmapToMap(mapPoint, timg_map, mMap!!)
        if (mm != null) {
            val rangePoint = RangePoint()
            rangePoint.x = mm.x
            rangePoint.y = mm.y
            obstaclePoints!!.add(rangePoint)
        }
        if (delobstacle != null) {
            delobstacle!!.clear()
        }
        runOnUiThread {
            newObstacle!!.points.clear()
            newObstacle!!.points.addAll(obstaclePoints!!)
            setBitmap(
                MapUtilsB.showElementMap(
                    this@EditMapActivity,
                    mMap!!,
                    noEntryRanges!!,
                    obstacleRanges!!
                )
            )
        }
    }

    fun doObstacleMove(mapPoint: MapPoint) {
        val mm = MapUtilsB.bitmapToMap(mapPoint, timg_map, mMap!!)
        if (mm != null) {
            val index = obstaclePoints!!.size - 1
            obstaclePoints!![index]!!.x = mm.x
            obstaclePoints!![index]!!.y = mm.y
            runOnUiThread {
                setBitmap(
                    MapUtilsB.showElementMap(
                        this@EditMapActivity,
                        mMap!!,
                        noEntryRanges!!,
                        obstacleRanges!!
                    )
                )
            }
        }
    }

    fun savePoint() {
        if (editPoint != null) {
            showWaite(30000, getText(R.string.saving).toString())
            val map = MapEdit()
            map.points.clear()
            map.points.addAll(mMap!!.points)
            val uploadMapReq = UploadMapReq(map).toString()
            sendwebSocket(uploadMapReq)
        }

    }

    fun savePicMap() {
        showWaite(30000, getText(R.string.saving).toString())
        if (mMap!!.brushes != null && mMap!!.brushes!!.size > 0) {
            val base64 = MapUtilsB.bitmapToBase64(
                MapUtilsB.brushesToBitmap(
                    bitmap!!,
                    mMap!!.brushes, 1f
                )
            )
            mMap!!.map_info.picture = base64.toString()
        }
        val map = MapEdit()
        map.map_info = MapInfoBase()
        map.map_info!!.map_id = mMap!!.map_info.map_id
        map.map_info!!.picture = mMap!!.map_info.picture
        val uploadMapReq = UploadMapReq(map).toString()
        sendwebSocket(uploadMapReq)
    }

    override fun onStart() {
        super.onStart()
        if (mapId > 0) {
            doAsync {
                Thread.sleep(500)
                sendwebSocket(DownloadMapReq().map(mapId))
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

    override fun onMessage(message: String): Int {
        val type = super.onMessage(message)

        when (type) {
            11001 -> {//定位点获取
                mapToBitmap?.let {
                    var bitMap = it.copy(Bitmap.Config.ARGB_8888, true)
                    drawLocationPoint(bitMap)

                    timg_map.setImageBitmap(bitMap)
                }
            }
            13009 -> {
                val downloadMapRes = JsonUtils.fromJson(message, DownloadMapRes::class.java)
                val map = downloadMapRes!!.getJson()
                if (noEntryRanges != null) {
                    noEntryRanges!!.clear()
                }
                if (obstacleRanges != null) {
                    obstacleRanges!!.clear()
                }

                for (i in map!!.ranges.indices) {
                    for (k in map.ranges[i].point_id.indices) {
                        for (j in map.points.indices) {
                            if (map.ranges[i].point_id[k] == map.points[j].point_id) {
                                map!!.ranges[i].points.add(map.points[j])
                            }
                        }
                    }

                    if (map.ranges[i].range_type == MapRange.Range_Path && map.ranges[i].work_type == MapRange.Work_Forbidden) {
                        noEntryRanges!!.add(map.ranges[i])
                        map!!.noEntryRange.add(map.ranges[i])
                    } else if (map.ranges[i].range_type == MapRange.Range_Area && map.ranges[i].work_type == MapRange.Work_Forbidden) {
                        obstacleRanges!!.add(map.ranges[i])
                        map!!.isObstacleRange.add(map.ranges[i])
                    }
                }
                mMap = map
                runOnUiThread {
                    dismissWaite()
                    tv_title.text = mMap!!.map_info.map_name
                    bitmap = MapUtilsB.mapToBitmap2(mMap!!, isRound = false)
                    elementBitmap = MapUtilsB.showElementMap(
                        this@EditMapActivity,
                        mMap!!,
                        noEntryRanges!!,
                        obstacleRanges!!
                    )
                    setBitmap(elementBitmap)
                    timg_map.setGraffiti(false)
                    editPoint = null
                    showPoints.clear()
                    for (pp in mMap!!.points) {
                        if (WayPoint.Type_Relocation in pp.type || WayPoint.Type_EWater in pp.type ||
                            WayPoint.Type_HWater in pp.type || WayPoint.Type_Charge in pp.type
                        ) {
                            showPoints.add(pp)
                        }
                    }
                    pointAdapter!!.updateData(-1)
                    noEntryAdapter!!.updateData(-1)
                    obstacleAdapter!!.updateData(-1)
                    if (isAdd) {
                        addpoint_status = 1
                        ll_ptype.visibility = View.VISIBLE
                        degreeWheel.visibility = View.GONE
                        img_add.setImageResource(R.drawable.add)
                        img_edit.setImageResource(R.drawable.edit)
                    } else if (isEraser) {
                        isEraser = false
                        img_eraser.setColorFilter(Color.parseColor("#ffffff"))
                        delbrushes!!.clear()
                        rl_seekbar.visibility = View.GONE
                        rl_tool.visibility = View.GONE
                    } else if (isNonetry) {
                        newRangePoint = null
                        noentryStatus = 0
                        nonetry_add.setImageResource(R.drawable.add)
                    } else if (isObstacle) {
                        obstacleStatus = 0
                        obstaclePoints = null
                        newObstacle = null
                        delobstacle!!.clear()
                        right_obstacle.visibility = View.VISIBLE
                        rl_tool.visibility = View.GONE
                        obstacle_add.setImageResource(R.drawable.add)
                    }
                }
            }

            13010 -> {
                val res = JsonUtils.fromJson(message, Response::class.java) ?: return 0
                if (res.error_code == 0) {
                    val downMap =
                        DownloadMapReq().map(mMap!!.map_info.map_id, 1).toString()
                    sendwebSocket(downMap)
                } else {
                    dismissWaite()
                    Toast.makeText(
                        this@EditMapActivity,
                        getText(R.string.save_error).toString(),
                        Toast.LENGTH_SHORT
                    ).show()

                }

            }

        }
        return type
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
                    -(angle.toFloat()), act = this@EditMapActivity
                )
            }
        }
    }

}