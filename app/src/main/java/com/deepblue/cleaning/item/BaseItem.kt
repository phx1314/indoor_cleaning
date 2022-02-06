//
//  BaseItem
//
//  Created by 86139 on 2020-08-10 18:46:18
//  Copyright (c) 86139 All rights reserved.


/**
   
*/

package com.deepblue.cleaning.item;

import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout


open class BaseItem(context: Context?) : LinearLayout(context), View.OnClickListener{
   override fun onClick(v: View) {
   }
}