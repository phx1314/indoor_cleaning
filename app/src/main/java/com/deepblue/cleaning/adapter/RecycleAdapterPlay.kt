package com.deepblue.cleaning.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnAttachStateChangeListener
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.view.animation.RotateAnimation
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.deepblue.cleaning.R
import com.deepblue.library.planbmsg.bean.RangeRro

class RecycleAdapterPlay(val context: Context, var taskList: ArrayList<RangeRro>) :
    RecyclerView.Adapter<RecycleAdapterPlay.MyViewHolder>() {
    var rotate: RotateAnimation? = null

    inner class MyViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        var tv_taskname: TextView? = null
        var rl_taskbg: RelativeLayout? = null
        var img_sel: ImageView? = null
        var tv_rangpro: TextView? = null

        init {
            rl_taskbg = itemView.findViewById(R.id.rl_taskbg)
            tv_taskname = itemView.findViewById(R.id.tv_taskname)
            img_sel = itemView.findViewById(R.id.img_sel)
            tv_rangpro = itemView.findViewById(R.id.tv_rangpro)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        rotate = RotateAnimation(
            0f,
            359f,
            Animation.RELATIVE_TO_SELF,
            0.5f,
            Animation.RELATIVE_TO_SELF,
            0.5f
        )
        val lin = LinearInterpolator()
        rotate!!.interpolator = lin //设置插值器
        rotate!!.duration = 3000 //设置动画持续周期
        rotate!!.repeatCount = Animation.INFINITE //设置重复次数
        rotate!!.fillAfter = true //动画执行完后是否停留在执行完的状态

        val inflater = LayoutInflater.from(context).inflate(R.layout.adapter_play, parent, false);
        val myViewHolder = MyViewHolder(inflater!!);
        return myViewHolder;
    }

    override fun getItemCount(): Int {
        return taskList.size
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {


        holder.tv_taskname!!.text = taskList[position].rangeName
        if (taskList[position].donePercent.toInt() == 100) {
            holder.tv_rangpro!!.visibility = View.GONE
            holder.img_sel!!.visibility = View.VISIBLE
            holder.img_sel!!.setImageResource(R.drawable.ic_selchecked)

        } else if (taskList[position].donePercent.toInt() == 0) {
            holder.tv_rangpro!!.visibility = View.GONE
            holder.img_sel!!.visibility = View.VISIBLE
            holder.img_sel!!.setImageResource(R.drawable.ic_play_loading)
//            if (holder.itemView.tag != null) {
//                holder.itemView.removeOnAttachStateChangeListener(holder.itemView.tag as OnAttachStateChangeListener) //移除旧的监听器
//            }
//            val listener: OnAttachStateChangeListener =
//                object : OnAttachStateChangeListener {
//                    override fun onViewAttachedToWindow(v: View) {
//                        holder.img_sel!!.animation = rotate
//                    }
//
//                    override fun onViewDetachedFromWindow(v: View) {}
//                }
//            holder.itemView.addOnAttachStateChangeListener(listener);
//            holder.itemView.setTag(listener); // 保存监听器对象。
        } else {
            holder.tv_rangpro!!.visibility = View.VISIBLE
            holder.tv_rangpro!!.text = "${taskList[position].donePercent.toInt()}%"
            holder.img_sel!!.visibility = View.GONE
        }
        if (holder.itemView.tag != null) {
            holder.itemView.removeOnAttachStateChangeListener(holder.itemView.tag as OnAttachStateChangeListener) //移除旧的监听器
        }
        val listener: OnAttachStateChangeListener =
            object : OnAttachStateChangeListener {
                override fun onViewAttachedToWindow(v: View) {
                    if (taskList[position].donePercent.toInt() == 0) {
                        holder.img_sel!!.animation = rotate
                    } else {
                        holder.img_sel!!.clearAnimation()
                    }
                }

                override fun onViewDetachedFromWindow(v: View) {}
            }
        holder.itemView.addOnAttachStateChangeListener(listener);
        holder.itemView.tag = listener; // 保存监听器对象。
    }
}