//
//  AdaPopRightDlqSon
//
//  Created by 86139 on 2020-10-27 09:41:07
//  Copyright (c) 86139 All rights reserved.


/**

 */

package com.deepblue.cleaning.ada;

import com.mdx.framework.adapter.MAdapter;
import android.content.Context;
import android.view.ViewGroup;
import android.view.View;

import com.deepblue.cleaning.item.PopRightDlqSon;
import com.deepblue.library.planbmsg.bean.MapRange
import com.mdx.framework.Frame

var position_xz = -1

class AdaPopRightDlqSon(context: Context?, list: List<Any>?) : MAdapter<Any>(context, list) {


    override fun getview(position: Int, convertView: View?, parent: ViewGroup): View? {
        var convertView = convertView
        val item = get(position)
        if (convertView == null) {
            convertView = PopRightDlqSon(context)
        }
        try {
            (convertView as PopRightDlqSon).set(item, position)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        convertView.setOnClickListener {
            position_xz = position
            notifyDataSetChanged()
            Frame.HANDLES.sentAll("FrgTaskManageDetail", 2, item)
        }
        convertView.setOnLongClickListener(View.OnLongClickListener {
            Frame.HANDLES.sentAll("FrgTaskManageDetail", 3, item)
            true
        })

        return convertView
    }
}

