package com.deepblue.cleaning.activity

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.os.Bundle
import android.text.TextUtils
import android.view.KeyEvent
import android.view.View
import com.deepblue.cleaning.R
import com.deepblue.cleaning.cleanview.AlertDialog
import com.deepblue.cleaning.utils.BaseDoubleClickListener
import com.deepblue.cleaning.utils.CommonUtil.get
import com.deepblue.cleaning.utils.DialogUtils
import com.deepblue.library.planbmsg.bean.CommonTask
import com.deepblue.library.planbmsg.msg4000.ChangeTaskStatusReqClean
import com.mdx.framework.Frame
import kotlinx.android.synthetic.main.layout_lock.*
import kotlinx.android.synthetic.main.layout_lock.mImageView_bg
import org.jetbrains.anko.startActivity

class StandbyActivity : BaseActivity() {
    var currentType = ""
    var systemPower = -1
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        currentType = intent.get<String>("currentType") ?: ""
        systemPower = intent.get<Int>("systemPower") ?: -1

        setContentView(R.layout.layout_lock)

        initView()
    }

    private fun initView() {
        rl_lock.visibility = View.VISIBLE
        objectAnimator = ObjectAnimator.ofFloat(img_flash, "alpha", 0.8f, 0f, 0.8f)
        objectAnimator!!.setDuration(2000)
        objectAnimator!!.setRepeatCount(ValueAnimator.INFINITE) //无限循环
        objectAnimator!!.start()
        mImageView_bg.setOnClickListener(object : BaseDoubleClickListener() {
            override fun onDoubleClick(v: View?) {
                sendwebSocket(ChangeTaskStatusReqClean().pause(0))
                startActivity<LoginActivity>("thisstatus_" to 2)
            }
        })
        if (!TextUtils.isEmpty(currentType)) {
            mImageView.visibility = View.GONE
            if (currentType == "C") {
                mRelativeLayout_bg.setBackgroundResource(R.mipmap.icon_backcdd_bg)
                tv_playtips.text = getString(R.string.ing_sure_go_charge_point)
            } else if (currentType == "E") {
                mRelativeLayout_bg.setBackgroundResource(R.drawable.ic_addwater_)
                tv_playtips.text = getString(R.string.ing_sure_go_pull_point)
            } else if (currentType == "H") {
                mRelativeLayout_bg.setBackgroundResource(R.drawable.ic_pushwater_)
                tv_playtips.text = getString(R.string.ing_sure_go_push_point)
            }
        }

        if (systemPower >= 100) {
            DialogUtils.showAlert(
                this,
                getString(R.string.i_cd_complete),
                R.string.suspend,
                object : AlertDialog.DialogButtonListener {
                    override fun cancel() {

                    }

                    override fun ensure(isCheck: Boolean): Boolean {
                        return true
                    }
                }, hasFillPoint = false, type = "C"
            )
        }
    }

    override fun disposeMsg(type: Int, obj: Any) {
        super.disposeMsg(type, obj)
        when (type) {
            702 -> finish()
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        return keyCode == KeyEvent.KEYCODE_BACK;
    }
}