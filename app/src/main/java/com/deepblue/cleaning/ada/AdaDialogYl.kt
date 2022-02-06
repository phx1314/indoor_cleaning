//
//  AdaDialogYl
//
//  Created by 86139 on 2020-10-21 09:06:20
//  Copyright (c) 86139 All rights reserved.


/**
   
*/

package com.deepblue.cleaning.ada;

import com.mdx.framework.adapter.MAdapter;
import android.content.Context;
import android.view.ViewGroup;
import android.view.View;

import com.deepblue.cleaning.item.DialogYl;

class AdaDialogYl (context: Context?, list: List<String>?) : MAdapter<String>(context, list) {


    override fun getview(position: Int, convertView: View?, parent: ViewGroup): View? {
        var convertView = convertView
        val item = get(position)
        if (convertView == null) {
            convertView = DialogYl(context)
        }
        try {
//            (convertView as DialogYl).set(item)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return convertView
    }
}

