package com.deepblue.cleaning.adapter

import android.content.Context
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.RelativeLayout
import android.widget.TextView
import com.deepblue.cleaning.Const
import com.deepblue.cleaning.R
import com.deepblue.cleaning.cleanview.RoundImageView
import com.deepblue.cleaning.utils.MapUtilsB
import com.deepblue.library.planbmsg.bean.Map
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.runOnUiThread
import java.util.*

class MapAdapter(
    private val context: Context,
    private var mapList: ArrayList<Map>?
) : BaseAdapter() {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        var holder: ViewHolder? = null
        var view = convertView
        if (null == view) {
            view = LayoutInflater.from(context).inflate(R.layout.adapter_map, null);
            holder = ViewHolder()
            holder.tv_mapname = view.findViewById(R.id.tv_mapname)
            holder.img_map = view.findViewById(R.id.img_map)
            holder.rl_mapbg = view.findViewById(R.id.rl_mapbg)
            view.tag = holder;
        } else {
            holder = view.tag as ViewHolder
        }
        holder.tv_mapname!!.text = mapList!![position].map_info.map_name
        if (mapList!![position].map_info.bitmap == null) {
            holder.img_map!!.setImageBitmap(BitmapFactory.decodeResource(context.resources, R.drawable.transpant))
            doAsync {
                MapUtilsB.mapToSmallBitmap(mapList!![position])?.let {
                    context.runOnUiThread {
                        mapList!![position].map_info.bitmap = it
                        holder.img_map!!.setImageBitmap(it)
                    }
                }
            }
        } else {
            holder.img_map!!.setImageBitmap(mapList!![position].map_info.bitmap)
        }

        return view!!
    }

    override fun getItem(position: Int): Any {
        return mapList!![position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getCount(): Int {
        return mapList!!.size
    }

    internal class ViewHolder {
        var tv_mapname: TextView? = null
        var img_map: RoundImageView? = null
        var rl_mapbg: RelativeLayout? = null
    }
}