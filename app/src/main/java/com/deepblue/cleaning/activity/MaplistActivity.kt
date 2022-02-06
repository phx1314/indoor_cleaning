package com.deepblue.cleaning.activity

import android.app.Activity
import android.content.Intent
import android.graphics.Canvas
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import com.deepblue.cleaning.Const
import com.deepblue.cleaning.R
import com.deepblue.cleaning.adapter.MapAdapter
import com.deepblue.cleaning.bean.Task
import com.deepblue.cleaning.cleanview.BToast
import com.deepblue.cleaning.cleanview.MapDetailDialog
import com.deepblue.cleaning.utils.MapUtilsB
import com.deepblue.library.planbmsg.JsonUtils
import com.deepblue.library.planbmsg.Response
import com.deepblue.library.planbmsg.bean.Map
import com.deepblue.library.planbmsg.bean.MapRange
import com.deepblue.library.planbmsg.msg3000.*
import com.mdx.framework.Frame
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_maplist.*
import org.jetbrains.anko.doAsync

class MaplistActivity : BaseActivity() {

    var adapter: MapAdapter? = null
    var mapList: ArrayList<Map>? = null
    var mMapDetailDialog: MapDetailDialog? = null

    override fun onClick(v: View) {
        super.onClick(v)
        when (v.id) {
            R.id.back_rl -> {
                goBack()
            }
        }
    }

    fun goBack(mapInfo: String = "") {
        Frame.HANDLES.sentAll("CleanMainActivity", 3, mapInfo)
        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maplist)

        mMapDetailDialog = MapDetailDialog(this@MaplistActivity)
        mapList = ArrayList<Map>()
        adapter = MapAdapter(this@MaplistActivity, mapList)
        gv_maplist?.adapter = adapter
        gv_maplist.onItemClickListener =
            AdapterView.OnItemClickListener { parent, view, position, id ->
                var checkedMapId = mapList!![position].map_info.map_id
                if (checkedMapId != Const.map?.map_info?.map_id) {
//                    sendwebSocket(ChangeNaviMapReq(checkedMapId).toString())
                    sendwebSocket(DownloadMapReq().map(checkedMapId))
                    showWaite()
                } else {
                    goBack()
                }

            }
        gv_maplist.onItemLongClickListener =
            AdapterView.OnItemLongClickListener { parent, view, position, id ->
                if (mMapDetailDialog != null) {
                    mMapDetailDialog!!.show()
                    mMapDetailDialog!!.setMap(mapList!![position])
                }
                true
            }

        back_rl.setOnClickListener(this)
        val allMaps = GetAllMapsReq(true).toString()
        sendwebSocket(allMaps)
        showWaite()
    }

    override fun onMessage(message: String): Int {
        val type = super.onMessage(message)

        when (type) {
            13003 -> {
                dismissWaite()
                //地图列表
//                doAsync {
                val mapJson =
                    JsonUtils.fromJson(message, GetAllMapsRes::class.java)
                val data = mapJson?.getJson()
                data?.let {
                    mapList!!.clear()
                    mapList!!.addAll(it.maps)
                    adapter!!.notifyDataSetChanged()

                }


//                }
            }
            13009 -> {
                val downloadMapRes = JsonUtils.fromJson(
                    message,
                    DownloadMapRes::class.java
                )
                if (downloadMapRes?.error_code != 0) {
                    BToast.showText(getString(R.string.i_map_error))
                } else {
                    goBack(message)
                }
                dismissWaite()
            }
            13008 -> {
                val res = JsonUtils.fromJson(
                    message,
                    Response::class.java
                )
                if (res?.error_code != 0) {
                    BToast.showText(getString(R.string.i_szdqdtsb))
                } else {
                    goBack(message)
                }

                goBack(message)
                dismissWaite()
            }
        }
        return type
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mMapDetailDialog != null && mMapDetailDialog!!.isShowing) {
            mMapDetailDialog!!.show()
        }
    }
}