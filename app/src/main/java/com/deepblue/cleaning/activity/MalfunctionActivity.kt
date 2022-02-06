package com.deepblue.cleaning.activity

import android.os.Bundle
import android.view.View
import android.view.Window
import android.widget.RadioGroup
import com.alibaba.fastjson.JSON
import com.deepblue.cleaning.Const
import com.deepblue.cleaning.R
import com.deepblue.cleaning.adapter.MalfunctionAdapter
import com.deepblue.library.planbmsg.JsonUtils
import com.deepblue.library.planbmsg.bean.ErrorInfo
import com.deepblue.library.planbmsg.msg2000.GetErrorHistoryReq
import com.deepblue.library.planbmsg.msg2000.GetErrorHistoryRes
import kotlinx.android.synthetic.main.activity_malfunction.*
import java.util.*
import kotlin.collections.ArrayList

class MalfunctionActivity : BaseActivity(), View.OnClickListener {
    private var malfunctionAdapter: MalfunctionAdapter? = null
    private var historyList: ArrayList<ErrorInfo> = ArrayList()

    //当前是否选中的是 未查看故障
    private var isUnchecked = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_malfunction)
        initViews()
        initData(isUnchecked)
    }

    private fun initData(b: Boolean) {
        if (b) {
            val data: Array<ErrorInfo>? =
                JsonUtils.fromJson(robotApp!!.mSharedPreferencesHelper!!.get(Const.SP_NEW_ERRORINFO, "").toString(), Array<ErrorInfo>::class.java)
            val toMutableList = data?.toMutableList() ?: ArrayList()
            historyList.clear()
            historyList.addAll(toMutableList)
            historyList.reverse()
            updateListUI()
        } else {
            sendwebSocket(GetErrorHistoryReq(0, System.currentTimeMillis() / 1000).toString())
            showWaite()
        }
    }

    private fun initViews() {
        swipe_malfun.setOnRefreshListener {
            initData(isUnchecked)
        }

        malfunctionAdapter = MalfunctionAdapter(this@MalfunctionActivity, historyList, isUnchecked)
        malfunction_lv.adapter = malfunctionAdapter
        back_rl.setOnClickListener(this)
        malfunction_rg.setOnCheckedChangeListener(RadioGroup.OnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.unchecked_rb -> {
                    if (isUnchecked) {
                        return@OnCheckedChangeListener
                    }
                    isUnchecked = true
                    initData(isUnchecked)
                }
                R.id.history_rb -> {
                    if (!isUnchecked) {
                        return@OnCheckedChangeListener
                    }
                    isUnchecked = false
                    initData(isUnchecked)
                }
                else -> {
                }
            }
        })
    }

    override fun disposeMsg(type: Int, obj: Any) {
        when (type) {
            0 -> {
                robotApp!!.mSharedPreferencesHelper!!.put(
                    Const.SP_NEW_ERRORINFO,
                    JSON.toJSON(obj)
                )
            }
        }
    }

    override fun onMessage(message: String): Int {
        val type = super.onMessage(message)
        when (type) {
            12009 -> {
                dismissWaite()
                historyList.clear()
                val getErrorHistoryRes = JsonUtils.fromJson(message, GetErrorHistoryRes::class.java)
                historyList.addAll(getErrorHistoryRes!!.getJson()!!.error_msgs)
                updateListUI()
            }
            24002 -> {
                isUnchecked = true
                initData(isUnchecked)
                malfunction_rg.check(R.id.unchecked_rb)
            }
        }
        return type
    }

    private fun updateListUI() {
        swipe_malfun.isRefreshing = false;
        if (historyList.size == 0) {
            title_ll.visibility = View.GONE
            no_malfunction_tv.visibility = View.VISIBLE
            if (isUnchecked) {
                no_malfunction_tv.text = getString(R.string.no_unchecked_malfunction)
            } else {
                no_malfunction_tv.text = getString(R.string.no_history_malfunction)
            }
        } else {
            title_ll.visibility = View.VISIBLE
            no_malfunction_tv.visibility = View.GONE
            malfunctionAdapter!!.updateData(historyList, isUnchecked)
            malfunction_lv.setSelection(0)
        }
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.back_rl -> finish()
            else -> {
            }
        }
    }
}