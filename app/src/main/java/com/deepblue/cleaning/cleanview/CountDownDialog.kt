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
import android.widget.TextView
import com.deepblue.cleaning.R

/**
 * 倒计时dialog
 */
class CountDownDialog(context: Context) : Dialog(context) {
    //默认倒计时时间，单位 毫秒
    private val countTime = 5500
    private var countDownTimer: CountDownTimer? = null
    private var countDownTv: TextView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.dialog_countdown)
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

        countDownTv =
            findViewById(R.id.count_down_tv)
        countDownTimer = object : CountDownTimer(countTime.toLong(), 1000) {
            override fun onTick(millisUntilFinished: Long) {
                countDownTv!!.text = (millisUntilFinished / 1000).toString()
            }

            override fun onFinish() {
                dismiss()
            }
        }
        countDownTimer!!.start()
    }

    init {
        setOwnerActivity((context as Activity))
    }
}