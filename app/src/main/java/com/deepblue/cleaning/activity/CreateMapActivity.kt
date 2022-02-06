package com.deepblue.cleaning.activity

import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.deepblue.cleaning.Const
import com.deepblue.cleaning.R
import com.deepblue.cleaning.cleanview.InputDialog
import com.deepblue.cleaning.utils.DialogFragment
import com.deepblue.cleaning.utils.DialogUtils
import com.deepblue.cleaning.utils.MapUtilsB
import com.deepblue.library.planbmsg.JsonUtils
import com.deepblue.library.planbmsg.msg1000.GetRobotLocReq
import com.deepblue.library.planbmsg.msg1000.GetRobotLocRes
import com.deepblue.library.planbmsg.msg1000.GetScanReq
import com.deepblue.library.planbmsg.msg1000.GetScanRes
import com.deepblue.library.planbmsg.msg3000.*
import com.mdx.framework.Frame
import kotlinx.android.synthetic.main.activity_createmap.*
import org.jetbrains.anko.doAsync

class CreateMapActivity : BaseActivity() {
    private var status: Int = 0  //0 默认；1；扫图中；2 结束
    var mInputDialog: DialogFragment? = null
    var mapName: String = ""
    var bitmap: Bitmap? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_createmap)

        back_rl.setOnClickListener {
            if (status == 0) {
                sendwebSocket(ChangeModeReq("auto").toString())
                finish()
            } else if (status == 1) {
                Toast.makeText(
                    this@CreateMapActivity,
                    getText(R.string.stop_slam).toString(),
                    Toast.LENGTH_SHORT
                ).show()
            } else if (status == 2) {
                setResult(99)
                sendwebSocket(ChangeModeReq("auto").toString())
                finish()
            }
        }

        img_play.setOnClickListener {
            if (status == 0) {
                mInputDialog = DialogUtils.showInput(this@CreateMapActivity,
                    getText(R.string.slam_tips).toString(),
                    object : InputDialog.DialogButtonListener {
                        override fun cancel() {

                        }

                        override fun ensure(input: String): Boolean {
                            if (input.isNotEmpty()) {
                                mapName = input
                                doStartSlam()
                            }
                            return true
                        }

                    })

            } else if (status == 1) {
                doStopSlam()
            }

        }


    }

    private fun doStartSlam() {
        val reqSlam = StartSlamReq(mapName).toString()
        sendwebSocket(reqSlam)
        startRobotLocationThread()
        startLasersThread()
    }

    private fun doStopSlam() {
        val reqSlam = StopSlamReq().toString()
        sendwebSocket(reqSlam)
        stopRobotLocationThread()
        stopLasersThread()
    }

    override fun onStart() {
        super.onStart()
        sendwebSocket(ChangeModeReq("manual").toString())
    }

    override fun onStop() {
        super.onStop()
//        sendwebSocket(ChangeModeReq("auto").toString())
    }


    fun startRobotLocationThread() {
        val locReq = GetRobotLocReq().start()
        sendwebSocket(locReq)
    }

    fun stopRobotLocationThread() {
        val locReq = GetRobotLocReq().stop()
        sendwebSocket(locReq)
    }

    fun startLasersThread() {
        val getScanReq = GetScanReq().start()
        sendwebSocket(getScanReq)
    }

    fun stopLasersThread() {
        val getScanReq = GetScanReq().stop()
        sendwebSocket(getScanReq)
    }

    override fun onMessage(message: String): Int {
        val type = super.onMessage(message)

        when (type) {
            13004 -> {
                //开始扫地图
                val startSlamRes =
                    JsonUtils.fromJson(message, StartSlamRes::class.java) ?: return type//@doAsync
                if (startSlamRes.error_code == 0) {
                    val map = startSlamRes.getJson() ?: return type
                    Const.newMap = map

                    doAsync {
                        bitmap = MapUtilsB.mapToBitmap(Const.newMap!!)
                        runOnUiThread {
                            status = 1
                            img_play.setImageResource(R.drawable.stop)

                        }
                        if (bitmap != null) {
                            runOnUiThread {
                                timg_map.visibility = View.VISIBLE
                                img_show.visibility = View.GONE
                                tv_tips.visibility = View.GONE
                                timg_map.setImageBitmap(bitmap)

                            }

                        }

                    }
                } else {
                    runOnUiThread {
                        Toast.makeText(
                            this@CreateMapActivity,
                            getText(R.string.slam_error).toString(),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
            13005 -> {
                //停止扫地图
                val endSlamRes =
                    JsonUtils.fromJson(message, StopSlamRes::class.java) ?: return type//@doAsync
                val json = endSlamRes.getJson() ?: return type
                if (json.map_id > 0) {
                    val downMap = DownloadMapReq().map(json.map_id)
                    sendwebSocket(downMap)
                } else {
                    Toast.makeText(
                        this@CreateMapActivity,
                        getText(R.string.slam_error).toString(),
                        Toast.LENGTH_SHORT
                    ).show()
                    dismissWaite()
                }
            }

            13009 -> {
                val downloadMapRes = JsonUtils.fromJson(message, DownloadMapRes::class.java)
                if (downloadMapRes != null && downloadMapRes.error_code == 0) {
                    if (downloadMapRes.getJson() != null) {
                        var map = downloadMapRes.getJson()
                        runOnUiThread {
                            timg_map.setImageBitmap(MapUtilsB.mapToBitmap(map!!))
                            img_play.visibility = View.GONE
                            status = 2
                            Const.map = map
                            Frame.HANDLES.sentAll("CleanMainActivity", 3, message)
                            setResult(99)
                            sendwebSocket(ChangeModeReq("auto").toString())
                            finish()
                        }
                    }
                }
            }


            11001 -> {
                //查询机器人位姿
                val locRes =
                    JsonUtils.fromJson(message, GetRobotLocRes::class.java) ?: return type
                Const.robotLoc = locRes.getJson()
                if (locRes.error_code == 0 && Const.robotLoc != null && bitmap != null) {
                    runOnUiThread {
                        timg_map.setImageBitmap(
                            MapUtilsB.mapAddRobot(
                                this@CreateMapActivity,
                                bitmap!!,
                                Const.newMap!!,
                                Const.robotLoc!!,
                                Const.scan_points
                            )
                        )
                    }
                }
            }

            11003 -> {
                //查询激光数据
                val getScanRes =
                    JsonUtils.fromJson(message, GetScanRes::class.java) ?: return type
                val json = getScanRes.getJson() ?: return type
                Const.scan_points.clear()
                Const.scan_points.addAll(json.scan_points)
            }
        }

        return type
    }
}