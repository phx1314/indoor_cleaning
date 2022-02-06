package com.deepblue.cleaning.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.deepblue.cleaning.R
import com.deepblue.library.planbmsg.bean.UserInfo

class RecycleAdapterUser(val context: Context, var nameList: ArrayList<UserInfo>) :
    RecyclerView.Adapter<RecycleAdapterUser.MyViewHolder>() {
    var mOnItemClickListener: OnItemClickListener? = null

    inner class MyViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        var textView: TextView? = null
        var rl_all: RelativeLayout? = null

        init {
            rl_all = itemView.findViewById(R.id.rl_all)
            textView = itemView.findViewById<View>(R.id.tv_name) as TextView?
        }
    }

    interface OnItemClickListener {
        fun itemClick(item: Int)
    }

    fun setOnItemClickListener(mOnItemClickListener: OnItemClickListener) {
        this.mOnItemClickListener = mOnItemClickListener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val inflater = LayoutInflater.from(context).inflate(R.layout.adapter_user, parent, false);

        val myViewHolder = MyViewHolder(inflater!!);
        return myViewHolder;

    }

    override fun getItemCount(): Int {
        return nameList.size

    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.textView!!.text = nameList[position].name
        holder.rl_all!!.setOnClickListener {
            if (mOnItemClickListener != null) {
                mOnItemClickListener!!.itemClick(position)
            }
        }
    }

}