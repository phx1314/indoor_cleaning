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
import com.mdx.framework.Frame
import java.util.*

/**
 * 用户列表 适配器
 */
class UserListAdapter(
    private val context: Context,
    private var userList: ArrayList<UserInfo>?
) : BaseAdapter() {
    private var currentPosition = -1

    private var itemClickCallback: ItemClickCallback? = null

    fun updateData(position: Int) {
        currentPosition = position
        notifyDataSetChanged()
    }

    override fun getCount(): Int {
        return if (null != userList) userList!!.size else 0
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
                R.layout.item_user_list,
                null
            )
            holder = ViewHolder()
            holder.userNameTv =
                view.findViewById(R.id.user_name_tv)
            holder.parentRl = view.findViewById(R.id.parent_rl)
            view.tag = holder
        } else {
            holder =
                view.tag as ViewHolder
        }
        if (null != userList && userList!!.isNotEmpty()) {
            val userBean = userList!![position]
            if (!TextUtils.isEmpty(userBean.name)) {
                holder.userNameTv!!.text = userBean.name
            } else {
                holder.userNameTv!!.text = ""
            }

            if (currentPosition == position) {
                holder.parentRl!!.background = context.getDrawable(R.drawable.input_box_bg_chosen)
            } else {
                holder.parentRl!!.background = context.getDrawable(R.drawable.input_box_bg_normal)
            }
            holder.parentRl!!.setOnLongClickListener {
                Frame.HANDLES.sentAll("AdminAccountActivity", 0, position)
                true
            }
            holder.parentRl!!.setOnClickListener(View.OnClickListener {
                currentPosition = position
                holder.parentRl!!.background = context.getDrawable(R.drawable.input_box_bg_chosen)
                notifyDataSetChanged()

                if (null != itemClickCallback) {
                    itemClickCallback!!.itemClick(userBean, currentPosition)
                }
            })
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
        fun itemClick(userBean: UserInfo, item: Int)
    }
}