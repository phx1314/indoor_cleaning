package com.deepblue.cleaning.activity

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.view.View
import com.deepblue.cleaning.Const
import com.deepblue.cleaning.R
import com.deepblue.cleaning.cleanview.AlertDialog
import com.deepblue.cleaning.cleanview.BToast
import com.deepblue.cleaning.cleanview.CountDownDialog
import com.deepblue.cleaning.utils.DialogUtils
import com.deepblue.cleaning.utils.MapUtilsB
import com.deepblue.library.planbmsg.JsonUtils
import com.deepblue.library.planbmsg.bean.WayPoint
import com.deepblue.library.planbmsg.msg1000.GetScanReq
import com.deepblue.library.planbmsg.msg1000.GetScanRes
import com.deepblue.library.planbmsg.msg2000.PlayVoiceReq
import com.deepblue.library.planbmsg.msg3000.ChangeModeReq
import com.deepblue.library.planbmsg.msg3000.RelocReq
import com.mdx.framework.Frame
import kotlinx.android.synthetic.main.activity_loc_check.*

class LocCheckActivity : BaseActivity() {
    var mWayPoint: WayPoint? = null
    var paintPoint = Paint()
    var mapToBitmap: Bitmap? = null
    var mGetScanRes: GetScanRes? = null
    var isDoTask = false
    override fun onClick(v: View) {
        super.onClick(v)
        when (v.id) {
            R.id.fl_back -> finish()
            R.id.btn_correct -> {
                sendwebSocket(ChangeModeReq("auto").toString())
                dialogFragment?.dismiss()
                isDoTask = true
                finish()
            }
            R.id.btn_mistake -> {
                sendwebSocket(GetScanReq().start())
                sendwebSocket(ChangeModeReq("manual").toString())
//                BToast.showText("切换手动模式")
                btn_rotate_loc.visibility = View.VISIBLE
                ll_title.visibility = View.INVISIBLE
            }
            R.id.btn_rotate_loc -> {
                sendwebSocket(ChangeModeReq("auto").toString())
                btn_rotate_loc.visibility = View.INVISIBLE
                sendwebSocket(PlayVoiceReq("", 10).toString())
                val dialog = CountDownDialog(this)
                dialog.setOnDismissListener {
                    ll_title.visibility = View.VISIBLE
                    mWayPoint?.run {
                        sendwebSocket(RelocReq(this, true).toString())
                    }
                }
                dialog.show()
            }
        }
    }

    override fun disposeMsg(type: Int, obj: Any) {
        when (type) {
            111 -> {
                btn_rotate_loc.visibility = View.INVISIBLE
                ll_title.visibility = View.VISIBLE
            }
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loc_check)
        paintPoint.color = Color.BLUE
        paintPoint.style = Paint.Style.STROKE
        paintPoint.isAntiAlias = true
        for (point in Const.map!!.points) {
            if (point.type.contains("D")) {
                mWayPoint = point
                break
            }
        }

        sendwebSocket(GetScanReq().start())
        fl_back.setOnClickListener(this)

        drawLocationPoint()
        btn_correct.setOnClickListener(this)
        btn_mistake.setOnClickListener(this)
        btn_rotate_loc.setOnClickListener(this)


        Const.map?.let {
            mapToBitmap = MapUtilsB.mapToBitmap(it)
        }
    }

    override fun onMessage(message: String): Int {
        val type = super.onMessage(message)
        when (type) {
            11001 -> {
                drawLocationPoint()
            }
            11003 -> {
                mGetScanRes =
                    JsonUtils.fromJson(message, GetScanRes::class.java)
                drawLocationPoint()
            }
        }
        return type
    }


    private fun drawLocationPoint() {
        if (mapToBitmap == null) return
        var copyB = mapToBitmap!!.copy(Bitmap.Config.ARGB_8888, true);
        var canvas  = Canvas(copyB)
        Const.robotLoc?.run {
            paintPoint.color = Color.BLUE
            paintPoint.style = Paint.Style.STROKE
            paintPoint.isAntiAlias = true
            MapUtilsB.paintBitmap(
                this,
                canvas!!,
                copyB!!,
                Const.map!!,
                paintPoint,
                R.drawable.ic_location, -(angle.toFloat()),
                1.2f, act = this@LocCheckActivity
            )

        }
        mWayPoint?.run {
            MapUtilsB.paintPoint(
                this,
                canvas!!,
                copyB!!,
                Const.map!!,
                paintPoint,
                text = getString(R.string.dwd),
                radius = 8f
            )
        }
        mGetScanRes?.run {
            getJson()?.run {
                paintPoint.color = Color.parseColor("#0091ff")
                paintPoint.style = Paint.Style.STROKE
                paintPoint.isAntiAlias = true
                for (point in this.scan_points) {
                    MapUtilsB.paintPoint(
                        point,
                        canvas!!,
                        copyB!!,
                        Const.map!!,
                        paintPoint,
                        radius = 3f
                    )
                }
            }
        }
        iv_bg.setImageBitmap(copyB)
    }

    override fun onDestroy() {
        sendwebSocket(GetScanReq().stop())
        super.onDestroy()
        if (isDoTask) Frame.HANDLES.sentAll("CleanMainActivity", 5, "")//调用handlers下一个页面的接口时需要在 super.onDestroy()之后发送消息，要不然不起作用
    }
}