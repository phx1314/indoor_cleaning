//
//  AdaEditDialog
//
//  Created by 86139 on 2020-10-27 10:31:38
//  Copyright (c) 86139 All rights reserved.


/**
   
*/

package com.deepblue.cleaning.ada;

import com.mdx.framework.adapter.MAdapter;
import android.content.Context;
import android.view.ViewGroup;
import android.view.View;

import com.deepblue.cleaning.item.EditDialog;

class AdaEditDialog (context: Context?, list: List<String>?) : MAdapter<String>(context, list) {


    override fun getview(position: Int, convertView: View?, parent: ViewGroup): View? {
        var convertView = convertView
        val item = get(position)
        if (convertView == null) {
            convertView = EditDialog(context)
        }
        try {
//            (convertView as EditDialog).set(item)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return convertView
    }
}

