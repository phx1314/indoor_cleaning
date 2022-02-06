package com.deepblue.cleaning.adapter

import android.content.Context
import android.text.TextUtils
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.RelativeLayout
import android.widget.TextView
import com.deepblue.cleaning.R
import com.deepblue.library.planbmsg.bean.UserInfo
import com.deepblue.library.planbmsg.bean.WayPoint
import java.util.*


class ShowPointsAdapter(
    private val context: Context,
    private var points: ArrayList<WayPoint>?
) : BaseAdapter() {
    private var currentPosition = -1

    private var itemClickCallback: ItemClickCallback? = null

    fun updateData(position: Int) {
        currentPosition = position
        notifyDataSetChanged()
    }

    override fun getCount(): Int {
        return if (null != points) points!!.size else 0
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
                R.layout.item_showmap_list,
                null
            )
            holder = ViewHolder()
            holder.userNameTv =
                view.findViewById(R.id.name_tv)
            holder.parentRl = view.findViewById(R.id.parent_rl)
            view.tag = holder
        } else {
            holder =
                view.tag as ViewHolder
        }
        if (null != points && points!!.isNotEmpty()) {
            val bean = points!![position]
            if (!TextUtils.isEmpty(bean.name)) {
                holder.userNameTv!!.text = bean.name
            } else {
                holder.userNameTv!!.text = ""
            }

            if (currentPosition == position) {
                holder.parentRl!!.background = context.getDrawable(R.drawable.input_bg_chosen)
            } else {
                holder.parentRl!!.background = context.getDrawable(R.drawable.input_bg_normal)
            }

            holder.parentRl!!.setOnClickListener(View.OnClickListener {
                if (null != itemClickCallback) {
                    itemClickCallback!!.itemClick(bean, position)
                }
            })
            holder.parentRl!!.setOnLongClickListener {
                if (null != itemClickCallback) {
                    itemClickCallback!!.onLongItemClick(bean, position)
                }
                false
            }
        }
        return view!!
    }

    private inner class ViewHolder {
        var userNameTv: TextView? = null
        var parentRl: RelativeLayout? = null
    }

    //设置回调
    fun setItemClickCallback(itemClickCallback: ItemClickCallback?) {
        this.itemClickCallback = itemClickCallback
    }


    interface ItemClickCallback {
        fun itemClick(bean: WayPoint, item: Int)

        fun onLongItemClick(bean: WayPoint, item: Int)
    }
}