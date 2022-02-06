package com.deepblue.cleaning.cleanview

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.text.TextUtils
import com.deepblue.cleaning.R
import kotlinx.android.synthetic.main.net_wait.*
import org.jetbrains.anko.runOnUiThread

class WaiteDialog(context: Context) : Dialog(context, R.style.WaiteStyle),
    DialogInterface.OnDismissListener {
    var times: Int = 0
    var needTost  = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.net_wait)
        setCanceledOnTouchOutside(false)
        val objectAnimator: ObjectAnimator = ObjectAnimator.ofFloat(img_loading, "rotation", -360f)
        objectAnimator.duration = 1000
        objectAnimator.repeatCount = ValueAnimator.INFINITE //无限循环
        objectAnimator.start()
        tvLoading.text = context.getText(R.string.loading).toString()
    }

    fun show(resId: Int) {
        show(context.getString(resId))
    }

    fun show(mTitle: String?) {
        try {
            super.show()
//            var p = this.window!!.attributes
//            var dm = DisplayMetrics()
//            this.window!!.windowManager.defaultDisplay.getRealMetrics(dm)
//            p.height = dm.heightPixels
//            p.width=dm.widthPixels
//            this.window!!.attributes = p    //设置生效

            times = 0
            if (!TextUtils.isEmpty(mTitle)) {
                tvLoading.text = mTitle
            } else {
                tvLoading.text = context.getText(R.string.loading).toString()
            }
        } catch (e: Exception) {
        }
    }

    override fun onDismiss(dialog: DialogInterface?) {
        times = 0
        context.runOnUiThread {
            img_loading.clearAnimation()
        }

    }

}