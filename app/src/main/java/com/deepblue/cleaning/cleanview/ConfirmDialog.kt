package com.deepblue.cleaning.cleanview

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.*
import com.deepblue.cleaning.R

/**
 * 任务开始前确认dialog
 */
class ConfirmDialog(context: Context, private val currentMode: Int) :
    Dialog(context), View.OnClickListener {
    private var removeDustModeLl: LinearLayout? = null
    private var washFloorModeLl: LinearLayout? = null
    private var oneClickCareModeLl: LinearLayout? = null
    private var closeDialogRl: RelativeLayout? = null
    private var taskStartTv: TextView? = null
    private var noMorePromptCb: CheckBox? = null
//    private val currentMode = REMOVE_DUST
    private var confirmCallback: ConfirmCallback? = null

    companion object {
        //除尘模式
        const val REMOVE_DUST = 0

        //洗地模式
        const val FLOOR_WASHING = 1

        //一键清洗模式
        const val ONE_KEY_CLEANING = 2

        //石材洗护模式
        const val STONE_WASHING = 3

        //一键洗护模式
        const val ONE_KEY_WASH_CARE = 4
    }

    init {

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.dialog_confirm_before_task_start)
        //按空白处不能取消动画
        setCanceledOnTouchOutside(true)
        //设置window背景，默认的背景会有Padding值，不能全屏。当然不一定要是透明，你可以设置其他背景，替换默认的背景即可。
        window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        //一定要在setContentView之后调用，否则无效
        window!!.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        //点击区域外不会dismiss
        setCancelable(false)
        initViews()
    }

    private fun initViews() {
        removeDustModeLl =
            findViewById(R.id.remove_dust_ll)
        washFloorModeLl =
            findViewById(R.id.wash_floor_ll)
        oneClickCareModeLl =
            findViewById(R.id.one_click_care_ll)
        closeDialogRl =
            findViewById(R.id.close_dialog_icon_rl)
        taskStartTv =
            findViewById(R.id.task_start_tv)
        noMorePromptCb =
            findViewById(R.id.no_more_prompt_cb)
        noMorePromptCb!!.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { buttonView, isChecked -> //TODO 今日不再提示勾选框
            if (null != confirmCallback) {
                confirmCallback!!.noMorePromptCallback(isChecked)
            }
        })
        closeDialogRl!!.setOnClickListener(this)
        taskStartTv!!.setOnClickListener(this)
        when (currentMode) {
            REMOVE_DUST ->                 //除尘模式
                removeDustModeLl!!.setVisibility(View.VISIBLE)
            FLOOR_WASHING, ONE_KEY_CLEANING ->                 //洗地模式 和 一键清洗模式
                washFloorModeLl!!.setVisibility(View.VISIBLE)
            STONE_WASHING, ONE_KEY_WASH_CARE ->                 //石材洗护模式 和 一键洗护模式
                oneClickCareModeLl!!.setVisibility(View.VISIBLE)
        }
    }

    override fun show() {
        super.show()
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.close_dialog_icon_rl -> dismiss()
            R.id.task_start_tv -> if (null != confirmCallback) {
                confirmCallback!!.clickTaskBeginning()
            }
            else -> {
            }
        }
    }

    //设置回调
    fun setConfirmCallback(confirmCallback: ConfirmCallback?) {
        this.confirmCallback = confirmCallback
    }

    //取消回调
    fun removeConfirmCallback() {
        confirmCallback = null
    }

    interface ConfirmCallback {
        //点击任务开始按钮 回调
        fun clickTaskBeginning()

        //今日不再提示勾选框 回调
        fun noMorePromptCallback(isChecked: Boolean)
    }
}