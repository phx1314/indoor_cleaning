package com.deepblue.cleaning.adapter

import android.content.Context
import android.text.TextUtils
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.RelativeLayout
import android.widget.TextView
import com.deepblue.cleaning.R
import com.deepblue.cleaning.bean.HistoryReportBean
import com.deepblue.library.planbmsg.bean.TaskReport
import com.mdx.framework.Frame
import java.util.*

/**
 * 用户列表 适配器
 */
class TaskHistoryAdapter(
    private val context: Context,
    private var taskHistoryList: ArrayList<TaskReport>?
) : BaseAdapter() {
    private var currentPosition = 0

    private var itemClickCallback: ItemClickCallback? = null

    fun updateData(taskHistoryList: ArrayList<TaskReport>?) {
        this.taskHistoryList = taskHistoryList
        notifyDataSetChanged()
    }

    fun updateData(currentPosition: Int) {
        this.currentPosition = currentPosition
        notifyDataSetChanged()
    }

    override fun getCount(): Int {
        return if (null != taskHistoryList) taskHistoryList!!.size else 0
    }

    override fun getItem(position: Int): Any {
        return position
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(
        position: Int,
        convertView: View?,
        parent: ViewGroup?
    ): View {
        var view = convertView
        var holder: ViewHolder
        if (null == view) {
            view = View.inflate(
                context,
                R.layout.item_task_history,
                null
            )
            holder = ViewHolder()
            holder.timeTv =
                view.findViewById(R.id.time_tv)
            holder.parentRl = view.findViewById(R.id.parent_rl)
            view.tag = holder
        } else {
            holder =
                view.tag as ViewHolder
        }
        if (null != taskHistoryList && taskHistoryList!!.isNotEmpty()) {
            val taskHistory = taskHistoryList!![position]
            if (!TextUtils.isEmpty(taskHistory.start_time)) {
                holder.timeTv!!.text = taskHistory.end_time
            } else {
                holder.timeTv!!.text = ""
            }

            if (currentPosition == position) {
                holder.parentRl!!.background = context.getDrawable(R.drawable.input_box_bg_chosen)
            } else {
                holder.parentRl!!.background = context.getDrawable(R.drawable.input_box_bg_normal)
            }
            holder.parentRl!!.setOnLongClickListener {
                Frame.HANDLES.sentAll("TaskReportActivity", 0, taskHistory)
                true
            }
            holder.parentRl!!.setOnClickListener(View.OnClickListener {
                currentPosition = position
                holder.parentRl!!.background = context.getDrawable(R.drawable.input_box_bg_chosen)
                notifyDataSetChanged()

                if (null != itemClickCallback) {
                    itemClickCallback!!.itemClick(taskHistory)
                }
            })
        }
        return view!!
    }

    private inner class ViewHolder {
        var timeTv: TextView? = null
        var parentRl: RelativeLayout? = null
    }

    //设置回调
    fun setItemClickCallback(itemClickCallback: ItemClickCallback?) {
        this.itemClickCallback = itemClickCallback
    }

    interface ItemClickCallback {
        fun itemClick(historyReportBean: TaskReport)
    }
}