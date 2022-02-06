package com.deepblue.cleaning.cleanview

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.RelativeLayout
import android.widget.TextView
import com.deepblue.cleaning.R
import com.deepblue.cleaning.utils.MapUtilsB
import com.deepblue.library.planbmsg.bean.Map
import kotlinx.android.synthetic.main.activity_mapdetails.*

/**
 * 倒计时dialog
 */
class MapDetailDialog(context: Context) : Dialog(context) {
    //默认倒计时时间，单位 毫秒
    private var back_rl: RelativeLayout? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.activity_mapdetails)
        //按空白处不能取消动画
        setCanceledOnTouchOutside(true)
        //设置window背景，默认的背景会有Padding值，不能全屏。当然不一定要是透明，你可以设置其他背景，替换默认的背景即可。
        window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        //一定要在setContentView之后调用，否则无效
        window!!.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )

        //隐藏顶部状态栏
        window!!.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN

        back_rl = findViewById(R.id.back_rl)
        back_rl!!.setOnClickListener {
            dismiss()
        }

    }

    fun setMap(map: Map) {
        val bitmap = MapUtilsB.mapToBitmap(map)
        if (bitmap != null) {
            img_map!!.setImageBitmap(bitmap)
        }

    }

    init {
        setOwnerActivity((context as Activity))
    }
}