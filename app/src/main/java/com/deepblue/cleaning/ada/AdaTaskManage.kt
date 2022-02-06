//
//  AdaTaskManage
//
//  Created by 86139 on 2020-10-26 15:25:36
//  Copyright (c) 86139 All rights reserved.


/**

 */

package com.deepblue.cleaning.ada;

import com.mdx.framework.adapter.MAdapter;
import android.content.Context;
import android.view.ViewGroup;
import android.view.View;
import com.deepblue.cleaning.frg.FrgTaskManageDetail

import com.deepblue.cleaning.item.TaskManage;
import com.deepblue.library.planbmsg.bean.Map
import com.mdx.framework.activity.TitleAct
import com.mdx.framework.utility.Helper

class AdaTaskManage(context: Context?, list: List<Map>?) : MAdapter<Map>(context, list) {


    override fun getview(position: Int, convertView: View?, parent: ViewGroup): View? {
        var convertView = convertView
        val item = get(position)
        if (convertView == null) {
            convertView = TaskManage(context)
        }
        try {
            (convertView as TaskManage).set(item)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        convertView.setOnClickListener {
            Helper.startActivity(context, FrgTaskManageDetail::class.java, TitleAct::class.java, "mapId", item.map_info.map_id)
        }
        return convertView
    }
}

