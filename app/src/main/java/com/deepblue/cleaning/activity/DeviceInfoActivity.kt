package com.deepblue.cleaning.activity

import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.view.animation.RotateAnimation
import android.widget.ImageView
import com.deepblue.cleaning.Const
import com.deepblue.cleaning.F
import com.deepblue.cleaning.F.saveJson
import com.deepblue.cleaning.F.showCenterDialog
import com.deepblue.cleaning.R
import com.deepblue.cleaning.cleanview.AlertDialog
import com.deepblue.cleaning.item.DialogYl
import com.deepblue.cleaning.req.*
import com.deepblue.cleaning.utils.DialogUtils
import com.deepblue.library.planbmsg.JsonUtils
import com.deepblue.library.planbmsg.msg1000.GetHardwareStatusReq
import com.deepblue.library.planbmsg.msg1000.GetHarewareStatusRes
import com.deepblue.library.planbmsg.msg1000.GetRobotInfoReq
import com.deepblue.library.planbmsg.msg1000.GetRobotInfoRes
import com.mdx.framework.view.CallBackOnly
import kotlinx.android.synthetic.main.activity_device_info.*
import org.jetbrains.anko.startActivity
import java.lang.Exception

class DeviceInfoActivity : BaseActivity() {
    var volume = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_device_info)
        initViews()
        initData()
    }

    private fun initData() {
        sendwebSocket(GetRobotInfoReq().toString())
        sendwebSocket(GetRobotDeviceReq().toString())
//        sendwebSocket(GetHardwareStatusReq().toString())
        sendwebSocket(GetRobotVoiceReq().toString())

        var rotate = RotateAnimation(
            0f,
            359f,
            Animation.RELATIVE_TO_SELF,
            0.5f,
            Animation.RELATIVE_TO_SELF,
            0.5f
        )
        val lin = LinearInterpolator()
        rotate!!.interpolator = lin //设置插值器
        rotate!!.duration = 3000 //设置动画持续周期
        rotate!!.repeatCount = Animation.INFINITE //设置重复次数
        rotate!!.fillAfter = true //动画执行完后是否停留在执行完的状态
        img_one.animation = rotate
        img_two.animation = rotate
        img_three.animation = rotate
        img_four.animation = rotate
        img_five.animation = rotate
        img_six.animation = rotate
        img_seven.animation = rotate
        img_eight.animation = rotate
        img_nine.animation = rotate
        img_ten.animation = rotate
        img_eleven.animation = rotate

        if (Const.type == "10") mLinearLayout_eleven.visibility = View.VISIBLE
    }

    private fun initViews() {
        mSwipeRefreshLayout.setOnRefreshListener {
            sendwebSocket(GetRobotInfoReq().toString())
            sendwebSocket(GetRobotDeviceReq().toString())
            mSwipeRefreshLayout.setRefreshing(false)
        }
        back_rl.setOnClickListener {
            finish()
        }
        mTextView_yltj.setOnClickListener {
            var mDialogYl = DialogYl(this@DeviceInfoActivity)
            showCenterDialog(this@DeviceInfoActivity, mDialogYl, object : CallBackOnly() {
                override fun goReturnDo(mDialog: Dialog) {
                    mDialogYl.set(mDialog, volume)
                }
            })

        }
        tv_logout.setOnClickListener {
            dialogFragment = DialogUtils.showAlert(this@DeviceInfoActivity,
                getText(R.string.sure_logout).toString(),
                android.R.string.ok,
                object : AlertDialog.DialogButtonListener {
                    override fun cancel() {

                    }

                    override fun ensure(isCheck: Boolean): Boolean {
                        saveJson("login", "")
                        startActivity<LoginActivity>()
                        return true
                    }

                })

        }

//        tv_appversio.text = F.getVersionName(this)
    }

    override fun disposeMsg(type: Int, obj: Any) {
        when (type) {
            0 -> {
                sendwebSocket(SetRobotVoiceReq(obj.toString().toInt()).toString())
                volume = obj.toString().toInt()
                mTextView_yl.text = String.format(getString(R.string.robot_yl), obj.toString())
            }
        }

    }

    fun setResFromStatus(mImageView: ImageView, status: Int) {
        when (status) {
            0, 2 -> mImageView.setImageResource(R.drawable.robotthree)
            1 -> mImageView.setImageResource(R.drawable.robottwo)
            3 -> mImageView.setImageResource(R.drawable.robotone)
        }
    }

    override fun onMessage(message: String): Int {
        val type = super.onMessage(message)
        when (type) {
            11000 -> {
                val robotInfoRes = JsonUtils.fromJson(message, GetRobotInfoRes::class.java)
                val robotInfo = robotInfoRes!!.getJson()
                robotInfo?.run {
                    tv_robot_name.text = String.format(getString(R.string.robot_name), name)
                    tv_robot_id.text =
                        String.format(getString(R.string.robot_id), serial_number)
                    tv_robot_device.text =
                        String.format(getString(R.string.robot_device), model)
                    tv_login_user.text =
                        String.format(getString(R.string.login_user), last_user)
                    tv_login_time.text =
                        String.format(getString(R.string.login_time), runup_time)
                    tv_appversio.text = String.format(getString(R.string.app_version), software_version)
                    tv_navigation_version.text = String.format(getString(R.string.navigation_version), navicat_version)
                    tv_firmware_version.text = String.format(getString(R.string.firmware_version), "MCU_" + embed_version.split("MCU_")[1])
                }
            }
            12033 -> {
                val robotInfoRes = JsonUtils.fromJson(message, GetRobotVoiceRes::class.java)
                val volume = robotInfoRes!!.json as Int
                volume?.run {
                    this@DeviceInfoActivity.volume = this
                    mTextView_yl.text = String.format(getString(R.string.robot_yl), this)
                }
            }
            11015 -> {
                setResFromStatus(img_one, 0)
                setResFromStatus(img_two, 0)
                setResFromStatus(img_three, 0)
                setResFromStatus(img_four, 0)
                setResFromStatus(img_five, 0)
                setResFromStatus(img_six, 0)
                setResFromStatus(img_seven, 0)
                setResFromStatus(img_eight, 0)
                setResFromStatus(img_nine, 0)
                setResFromStatus(img_ten, 0)
                try {
                    val hardwareStatusRes =
                        JsonUtils.fromJson(message, GetRobotDeviceRes::class.java)
                    hardwareStatusRes?.getJson()?.hardwares_status?.toMutableList()?.forEach {
                        when (it.hardware_id) {
                            3 -> setResFromStatus(img_one, it.status)
                            4 -> setResFromStatus(img_two, it.status)
                            6 -> setResFromStatus(img_three, it.status)
                            1 -> setResFromStatus(img_four, it.status)
                            1605 -> setResFromStatus(img_five, it.status)
                            1606 -> setResFromStatus(img_six, it.status)
                            1601 -> setResFromStatus(img_seven, it.status)
                            1602 -> setResFromStatus(img_eight, it.status)
                            9 -> setResFromStatus(img_nine, it.status)
                            1603 -> setResFromStatus(img_ten, it.status)

                        }
                    }
                }catch (e:Exception){
                    e.printStackTrace()
                }

                img_one.clearAnimation()
                img_two.clearAnimation()
                img_three.clearAnimation()
                img_four.clearAnimation()
                img_five.clearAnimation()
                img_six.clearAnimation()
                img_seven.clearAnimation()
                img_eight.clearAnimation()
                img_nine.clearAnimation()
                img_ten.clearAnimation()
                img_eleven.clearAnimation()
            }
        }
        return type
    }
}