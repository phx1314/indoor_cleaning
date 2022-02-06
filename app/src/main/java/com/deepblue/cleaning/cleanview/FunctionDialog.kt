package com.deepblue.cleaning.cleanview

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import com.deepblue.cleaning.Const
import com.deepblue.cleaning.F
import com.deepblue.cleaning.R
import com.deepblue.cleaning.activity.*
import com.deepblue.cleaning.frg.FrgTaskManage
import com.mdx.framework.activity.TitleAct
import com.mdx.framework.utility.Helper
import kotlinx.android.synthetic.main.dialog_function.*
import org.jetbrains.anko.clearTop
import org.jetbrains.anko.intentFor
import org.jetbrains.anko.startActivity

/**
 * 功能列表 dialog
 */
class FunctionDialog(context: Context, var isAauto: Boolean, var back: FinishBack) :
    Dialog(context),
    View.OnClickListener {
    private var switchManualIv: RelativeLayout? = null
    private var switchEventIv: RelativeLayout? = null
    private var instructionIv: TextView? = null
    private var historyReportIv: TextView? = null
    private var malfunctionInfoIv: TextView? = null
    private var tv_auto: TextView? = null
    private var deviceInfoIv: TextView? = null
    private var index = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.dialog_function)
        //按空白处不能取消动画
        setCanceledOnTouchOutside(true)
        //设置window背景，默认的背景会有Padding值，不能全屏。当然不一定要是透明，你可以设置其他背景，替换默认的背景即可。
        window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        //一定要在setContentView之后调用，否则无效
        window!!.setGravity(Gravity.LEFT)
        window!!.setLayout(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        //        点击区域外不会dismiss
//        setCancelable(false);
        //隐藏顶部状态栏
        window!!.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
        window!!.setWindowAnimations(R.style.Dialog_Animation)
        initViews()
    }

    override fun show() {
        super.show()
        index = 0
    }

    override fun dismiss() {
        super.dismiss()
        index = 0
    }


    private fun initViews() {
        tv_auto = findViewById(R.id.tv_auto)
        switchManualIv =
            findViewById(R.id.switch_manual_iv)
        switchEventIv =
            findViewById(R.id.switch_event_iv)
        instructionIv =
            findViewById(R.id.instructions_for_use_iv)
        historyReportIv =
            findViewById(R.id.history_report_iv)
        malfunctionInfoIv =
            findViewById(R.id.malfunction_info_iv)
        deviceInfoIv =
            findViewById(R.id.device_info_iv)
        switchManualIv!!.setOnClickListener(this)
        switchEventIv!!.setOnClickListener(this)
        instructionIv!!.setOnClickListener(this)
        historyReportIv!!.setOnClickListener(this)
        malfunctionInfoIv!!.setOnClickListener(this)
        version_id!!.setOnClickListener(this)
        deviceInfoIv!!.setOnClickListener(this)
        mRelativeLayout_task_manage.setOnClickListener(this)
        mRelativeLayout_map_manage.setOnClickListener(this)
//        mRelativeLayout_map_manage.setOnLongClickListener(object : View.OnLongClickListener {
//            override fun onLongClick(v: View?): Boolean {
//                context.startActivity<EngineeringModeActivity>()
//                return true
//            }
//        })
        if (Const.type == "10") {
            switch_event_iv.visibility = View.VISIBLE
        } else {
            switch_event_iv.visibility = View.GONE
        }
        version_id.setText("应用程序版本：" + F.getVersionName(context))
//        version_id.setOnLongClickListener {
//            context.startActivity<SettingActivity>()
//            true
//        }

        if (isAauto) {
            tv_auto!!.text = context.getText(R.string.changemanual)
        } else {
            tv_auto!!.text = context.getText(R.string.changeauto)
        }
    }

    override fun onClick(v: View) {

        when (v.id) {
            R.id.switch_manual_iv -> {
                if (isAauto) {
                    context.startActivity(context.intentFor<ManualActivity>().clearTop())
                } else {
                    context.startActivity(context.intentFor<CleanMainActivity>().clearTop())
                }
                if (back != null) {
                    back.dialogFinish()
                }
                index = 0;
                dismiss()
            }
            R.id.switch_event_iv -> {
                context.startActivity<SelectModelActivity>()
//                context.startActivity<ChargeActivity>()
                index = 0;
                dismiss()
            }
            R.id.instructions_for_use_iv -> {
                context.startActivity<InstructionActivity>()
                index = 0;
                dismiss()
            }
            R.id.history_report_iv -> {
                context.startActivity<TaskReportActivity>()
                index = 0;
                dismiss()
            }
            R.id.malfunction_info_iv -> {
                context.startActivity<MalfunctionActivity>()
                index = 0;
                dismiss()
            }
            R.id.device_info_iv -> {
                context.startActivity<DeviceInfoActivity>()
                index = 0;
                dismiss()
            }
            R.id.version_id -> {
                index++
                if (index > 5) {
                    context.startActivity<EngineeringModeActivity>()
                    index = 0;
                }
            }
            R.id.mRelativeLayout_map_manage -> {
                context.startActivity<MapsManagerActivity>()
            }
            R.id.mRelativeLayout_task_manage -> {
                Helper.startActivity(context, FrgTaskManage::class.java, TitleAct::class.java)
            }
            else -> {
            }
        }
    }

    interface FinishBack {
        fun dialogFinish()
    }
}