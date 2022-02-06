package com.deepblue.cleaning.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import com.deepblue.cleaning.Const
import com.deepblue.cleaning.R
import com.deepblue.cleaning.adapter.MapAdapter2
import com.deepblue.cleaning.cleanview.AlertDialog
import com.deepblue.cleaning.cleanview.InputDialog
import com.deepblue.cleaning.cleanview.MapDetailDialog
import com.deepblue.cleaning.utils.DialogFragment
import com.deepblue.cleaning.utils.DialogUtils
import com.deepblue.library.planbmsg.JsonUtils
import com.deepblue.library.planbmsg.Response
import com.deepblue.library.planbmsg.bean.Map
import com.deepblue.library.planbmsg.bean.MapEdit
import com.deepblue.library.planbmsg.msg3000.*
import com.mdx.framework.utility.Helper
import kotlinx.android.synthetic.main.activity_maplist.*
import kotlinx.android.synthetic.main.activity_maplist.back_rl
import kotlinx.android.synthetic.main.activity_maplist.gv_maplist
import kotlinx.android.synthetic.main.activity_maps.*
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.startActivityForResult

var index: Int = -1

class MapsManagerActivity : BaseActivity() {
    var adapter: MapAdapter2? = null
    var mapList: ArrayList<Map>? = null
    var checkedMap: Map? = null

    var mInputDialog: DialogFragment? = null
    var inputName: String = ""
    var fristShow: Boolean = false

    override fun onClick(v: View) {
        super.onClick(v)
        when (v.id) {
            R.id.back_rl -> {
                finish()
            }
            R.id.img_edit -> {
                mInputDialog = DialogUtils.showInput(this@MapsManagerActivity,
                    getText(R.string.hint_input).toString(),
                    object : InputDialog.DialogButtonListener {
                        override fun cancel() {

                        }

                        override fun ensure(input: String): Boolean {
                            if (input.isNotEmpty()) {
                                val renameMapReq = RenameMapReq(
                                    checkedMap!!.map_info.map_id,
                                    input,
                                    index
                                ).toString()
                                sendwebSocket(renameMapReq)
                                inputName = input
                            }


                            return true
                        }

                    })
            }
            R.id.img_del -> {
                if (checkedMap != null && checkedMap!!.map_info.map_id > 0) {
                    startActivity<EditMapActivity>("map_id" to checkedMap!!.map_info.map_id)
                }
            }
            R.id.img_detail -> {
                checkedMap?.let {
                    mInputDialog = DialogUtils.showInput(
                        this@MapsManagerActivity,
                        getText(R.string.hint_input).toString(),
                        object : InputDialog.DialogButtonListener {
                            override fun cancel() {

                            }

                            override fun ensure(input: String): Boolean {
                                if (input.isNotEmpty()) {
                                    showWaite()
                                    sendwebSocket(CopyMapsReq(arrayOf(it.map_info.map_id).toMutableList() as ArrayList<Int>, arrayOf(input).toMutableList() as ArrayList<String>).toString())
                                }
                                return true
                            }

                        }, text = it.map_info.map_name + " 副本"
                    )

                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        index = -1
        mapList = ArrayList<Map>()
        mapList!!.add(Map())
        adapter = MapAdapter2(this@MapsManagerActivity, mapList)
        gv_maplist?.adapter = adapter
        gv_maplist.onItemClickListener =
            AdapterView.OnItemClickListener { parent, view, position, id ->
                clickItem(position)
            }
        gv_maplist.setOnItemLongClickListener(object : AdapterView.OnItemLongClickListener {
            override fun onItemLongClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long): Boolean {
                clickItem(position)
                if (checkedMap != null) {
                    if (Const.map?.map_info?.map_id == checkedMap!!.map_info.map_id) {
                        Helper.toast(getString(R.string.i_cannot_delete))
                    } else {
                        dialogFragment = DialogUtils.showAlert(this@MapsManagerActivity,
                            getString(
                                R.string.sure_delete_content,
                                checkedMap!!.map_info.map_name
                            ),
                            android.R.string.ok,
                            object : AlertDialog.DialogButtonListener {
                                override fun cancel() {

                                }

                                override fun ensure(isCheck: Boolean): Boolean {
                                    showWaite()
                                    var ids = ArrayList<Int>()
                                    ids.add(checkedMap!!.map_info.map_id)
                                    val deleteMapsReq = DeleteMapsReq(ids, 1).toString()
                                    sendwebSocket(deleteMapsReq)
                                    return true
                                }

                            })
                    }
                }
                return true
            }
        })
        back_rl.setOnClickListener(this)
        img_edit.setOnClickListener(this)
        img_detail.setOnClickListener(this)
        img_del.setOnClickListener(this)
        showWaite()
    }

    fun clickItem(position: Int) {
        if (position > 0) {
            rl_manager.visibility = View.VISIBLE
//            for (index in mapList!!.indices) {
//                mapList!![index]!!.isChecked = index == position
//            }
            checkedMap = mapList!![position]
            index = position
            inputName = ""
        } else {
            startActivityForResult<CreateMapActivity>(100)
        }
        adapter!!.notifyDataSetChanged()
    }

    override fun onStart() {
        super.onStart()
        if (!fristShow) {
            sendwebSocket(GetAllMapsReq(true).toString())
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 100 && resultCode == 99) {
            showWaite()
            val allMaps = GetAllMapsReq(true).toString()
            sendwebSocket(allMaps)
        }
    }

    override fun onMessage(message: String): Int {
        val type = super.onMessage(message)

        when (type) {

            13006 -> {
                //删除机器人上的地图
                val deleteMapsRes =
                    JsonUtils.fromJson(message, DeleteMapsRes::class.java) ?: return type
                dismissWaite()
                if (deleteMapsRes.error_code == 0) {
                    if (index > -1) {
                        mapList!!.removeAt(index)
                        index = -1
                        runOnUiThread {
                            adapter!!.notifyDataSetChanged()
                            rl_manager.visibility = View.GONE
                        }
                    }
                }
            }
            13007 -> {
                val res = JsonUtils.fromJson(message, Response::class.java) ?: return 0
                if (res.error_code == 0) {
                    if (index > 0 && inputName.isNotEmpty()) {
                        mapList!![index].map_info.map_name = inputName
                        runOnUiThread {
                            adapter!!.notifyDataSetChanged()
                        }
                    }
                }

            }
            13003 -> {
                dismissWaite()
                //地图列表
                val mapJson =
                    JsonUtils.fromJson(message, GetAllMapsRes::class.java)
                val data = mapJson?.getJson()
                data?.let {
                    mapList!!.clear()
                    mapList!!.add(Map())
                    mapList!!.addAll(it.maps)
                    adapter!!.notifyDataSetChanged()
                    fristShow = true
                    index = -1
                }
            }
            13014 -> {
                dismissWaite()
                sendwebSocket(GetAllMapsReq(true).toString())
            }
        }
        return type
    }

    override fun onDestroy() {
        super.onDestroy()
    }

}