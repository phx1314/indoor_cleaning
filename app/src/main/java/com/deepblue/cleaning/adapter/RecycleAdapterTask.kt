package com.deepblue.cleaning.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.deepblue.cleaning.R
import com.deepblue.cleaning.bean.Task

class RecycleAdapterTask(val context: Context, var taskList: ArrayList<Task>) :
    RecyclerView.Adapter<RecycleAdapterTask.MyViewHolder>() {
    var onItemChangeListener: OnItemChangeListener? = null

    inner class MyViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        var tv_taskname: TextView? = null
        var rl_taskbg: RelativeLayout? = null
        var img_sel: ImageView? = null

        init {
            rl_taskbg = itemView.findViewById(R.id.rl_taskbg)
            tv_taskname = itemView.findViewById(R.id.tv_taskname)
            img_sel = itemView.findViewById(R.id.img_sel)
        }
    }

    interface OnItemChangeListener {
        fun onItemChange(position: Int, checked: Boolean)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val inflater = LayoutInflater.from(context).inflate(R.layout.adapter_task, parent, false);

        val myViewHolder = MyViewHolder(inflater!!);
        return myViewHolder;
    }

    override fun getItemCount(): Int {
        return taskList.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val task = taskList[position]
        holder.tv_taskname!!.text = task.range!!.name
        if (task.checked) {
            holder.rl_taskbg?.setBackgroundResource(R.drawable.user_background)
            holder.img_sel?.setImageResource(R.drawable.ic_selchecked)
        } else {
            holder.rl_taskbg?.background = null
            holder.img_sel?.setImageResource(R.drawable.ic_selnor)
        }
        holder.rl_taskbg!!.setOnClickListener {
            //更改条目选中状态
            task.checked = !task.checked
            if (task.checked) {
                it.setBackgroundResource(R.drawable.user_background)
                holder.img_sel?.setImageResource(R.drawable.ic_selchecked)
            } else {
                it.background = null
                holder.img_sel?.setImageResource(R.drawable.ic_selnor)
            }
            onItemChangeListener?.onItemChange(position,task.checked)
        }
    }

}