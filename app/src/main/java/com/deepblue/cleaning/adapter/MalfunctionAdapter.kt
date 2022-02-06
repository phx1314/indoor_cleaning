package com.deepblue.cleaning.adapter

import android.content.Context
import android.text.TextUtils
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.deepblue.cleaning.R
import com.deepblue.cleaning.utils.CommonUtil
import com.deepblue.library.planbmsg.bean.ErrorInfo
import com.mdx.framework.Frame
import java.text.SimpleDateFormat
import java.util.*

/**
 * 故障列表 适配器
 */
class MalfunctionAdapter(
    val context: Context,
    var malfunctionList: ArrayList<ErrorInfo>?,
    var isUnchecked: Boolean
) : BaseAdapter() {
    fun updateData(malfunctionList: ArrayList<ErrorInfo>?, isUnchecked: Boolean) {
        this.malfunctionList = malfunctionList
        this.isUnchecked = isUnchecked
        notifyDataSetChanged()
    }

    override fun getCount(): Int {
        return if (null != malfunctionList) malfunctionList!!.size else 0
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
                R.layout.item_malfunction,
                null
            )
            holder = ViewHolder()
            holder.cycleIv =
                view.findViewById(R.id.cycle_iv)
            holder.levelTv =
                view.findViewById(R.id.malfunction_level_tv)
            holder.codeTv =
                view.findViewById(R.id.malfunction_code_tv)
            holder.timeTv =
                view.findViewById(R.id.malfunction_time_tv)
            holder.descTv =
                view.findViewById(R.id.malfunction_desc_tv)
            holder.checkTv =
                view.findViewById(R.id.check_tv)
            holder.upLineIv =
                view.findViewById(R.id.up_line_iv)
            holder.downLineTv =
                view.findViewById(R.id.down_line_iv)
            view.tag = holder
        } else {
            holder =
                view.tag as ViewHolder
        }
        if (null != malfunctionList && !malfunctionList!!.isEmpty()) {
            val malfunctionBean = malfunctionList!![position]
            if (TextUtils.equals(
                    malfunctionBean.type,
                    WARING_LEVEL
                )
            ) {
                holder.cycleIv!!.background = context.getDrawable(R.drawable.warning_cycle)
            } else if (TextUtils.equals(
                    malfunctionBean.type,
                    ERROR_LEVEL
                )
            ) {
                holder.cycleIv!!.background = context.getDrawable(R.drawable.error_cycle)
            }
            if (!TextUtils.isEmpty(malfunctionBean.type)) {
                holder.levelTv!!.text = malfunctionBean.type
            } else {
                holder.levelTv!!.text = ""
            }
            if (malfunctionBean.time != 0L) {
                holder.timeTv!!.text = CommonUtil.Long2Data(malfunctionBean.time * 1000)
            } else {
                holder.timeTv!!.text = ""
            }
            if (!TextUtils.isEmpty(malfunctionBean.reason)) {
                holder.descTv!!.text = malfunctionBean.reason
            } else {
                holder.descTv!!.text = ""
            }
            if (!TextUtils.isEmpty(malfunctionBean.error_code)) {
                holder.codeTv!!.text = malfunctionBean.error_code
            } else {
                holder.codeTv!!.text = ""
            }


            //判断当前故障是否可以查看
            if (isUnchecked) {
                holder.checkTv!!.visibility = View.VISIBLE
                holder.checkTv!!.setOnClickListener {
                    malfunctionList!!.remove(malfunctionBean)
                    notifyDataSetChanged()
                    Frame.HANDLES.sentAll("MalfunctionActivity", 0, malfunctionList)
                }
            } else {
                holder.checkTv!!.visibility = View.INVISIBLE
                holder.checkTv!!.setOnClickListener(null)
            }
            if (position == malfunctionList!!.size - 1) {
                holder.downLineTv!!.visibility = View.VISIBLE
            } else {
                holder.downLineTv!!.visibility = View.INVISIBLE
            }
        }
        return view!!
    }

    private inner class ViewHolder {
        var cycleIv: ImageView? = null
        var levelTv: TextView? = null
        var codeTv: TextView? = null
        var timeTv: TextView? = null
        var descTv: TextView? = null
        var checkTv: TextView? = null
        var upLineIv: ImageView? = null
        var downLineTv: ImageView? = null
    }

    companion object {
        private const val WARING_LEVEL = "Warning"
        private const val ERROR_LEVEL = "Error"
    }

}