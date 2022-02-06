package com.deepblue.cleaning.activity

import android.graphics.Color
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.WindowManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.deepblue.cleaning.Const
import com.deepblue.cleaning.F
import com.deepblue.cleaning.F.saveJson
import com.deepblue.cleaning.R
import com.deepblue.cleaning.activity.PlayActivity.Companion.TASKCOMPLETE_FROMPALYACTIVITY
import com.deepblue.cleaning.adapter.RecycleAdapterUser
import com.deepblue.cleaning.cleanview.BToast
import com.deepblue.cleaning.cleanview.MaxHeightRecyclerView
import com.deepblue.cleaning.utils.CommonUtil
import com.deepblue.cleaning.utils.CommonUtil.get
import com.deepblue.library.planbmsg.JsonUtils
import com.deepblue.library.planbmsg.JsonUtils.fromJson
import com.deepblue.library.planbmsg.Response
import com.deepblue.library.planbmsg.bean.RobotStatus
import com.deepblue.library.planbmsg.bean.UserInfo
import com.deepblue.library.planbmsg.msg1000.GetRobotInfoReq
import com.deepblue.library.planbmsg.msg1000.GetRobotInfoRes
import com.deepblue.library.planbmsg.msg2000.*
import com.deepblue.library.planbmsg.msg3000.ChangeModeReq
import com.deepblue.library.planbmsg.msg3000.DownloadMapReq
import com.deepblue.library.planbmsg.msg3000.GetAllMapsReq
import com.deepblue.library.planbmsg.msg4000.ChangeTaskStatusReqClean
import com.deepblue.library.planbmsg.push.TaskReportRes
import com.mdx.framework.Frame
import com.mdx.framework.utility.Helper
import com.zrq.divider.Divider
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_login.mSwipeRefreshLayout
import kotlinx.android.synthetic.main.activity_login.rl_one
import kotlinx.android.synthetic.main.activity_login.rl_three
import kotlinx.android.synthetic.main.activity_login.rl_two
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.layout_palylock.*
import org.jetbrains.anko.clearTop
import org.jetbrains.anko.intentFor
import org.jetbrains.anko.startActivity


class LoginActivity : BaseActivity() {

    var rv_users: MaxHeightRecyclerView? = null
    var userAdapter: RecycleAdapterUser? = null
    var names = ArrayList<UserInfo>()
    var show: Boolean = false
    var oldname: String = ""
    var passwords: ArrayList<Int> = java.util.ArrayList()
    var login = ""
    var intentFrom = -1   // 0 from BackToCDD  任务状态,1  from BackToCDD  返回充电,2  from Standby  , 3 from BackToCDD  任务完成
    var runnable = Runnable {
        if (intentFrom != -1 && intentFrom != 3) {
            if (Const.robotPlayStatus != RobotStatus.STATUS_EMERGENCY && !CommonUtil.isBackground(className)) sendwebSocket(ChangeTaskStatusReqClean().resume(0))
            finish()
        }
    }

    override fun onClick(v: View) {
        super.onClick(v)
        when (v.id) {
            R.id.rl_login -> {
                if (intentFrom == -1) {
                    if (names.isNotEmpty() && names.size > 0) {
                        if (show) {
                            show = false
                            rv_users!!.visibility = View.GONE
                            rl_pw.visibility = View.VISIBLE
                            img_down.setImageResource(R.drawable.down)
                            if (tv_user.text == getText(R.string.login_tips) || (passwords != null && passwords.size == 4)) {
                                rl_button.visibility = View.VISIBLE
                                ll_keyboard.visibility = View.GONE
                            } else {
                                rl_button.visibility = View.GONE
                                ll_keyboard.visibility = View.VISIBLE
                                uiChange()
                            }
                        } else {
                            img_down.setImageResource(R.drawable.up)
                            show = true
                            rv_users!!.visibility = View.VISIBLE
                            rl_pw.visibility = View.GONE
                            rl_button.visibility = View.GONE
                            ll_keyboard.visibility = View.GONE
                        }
                    } else {
                        BToast.showText(this@LoginActivity, getText(R.string.no_name).toString())
                    }
                }
            }
            R.id.rl_one -> {
                if (passwords.size < 4) {
                    passwords.add(1)
                    uiChange()
                }
            }
            R.id.rl_two -> {
                if (passwords.size < 4) {
                    passwords.add(2)
                    uiChange()
                }
            }
            R.id.rl_three -> {
                if (passwords.size < 4) {
                    passwords.add(3)
                    uiChange()
                }

            }
            R.id.rl_four -> {
                if (passwords.size < 4) {
                    passwords.add(4)
                    uiChange()
                }

            }
            R.id.rl_five -> {
                if (passwords.size < 4) {
                    passwords.add(5)
                    uiChange()
                }

            }
            R.id.rl_six -> {
                if (passwords.size < 4) {
                    passwords.add(6)
                    uiChange()
                }

            }
            R.id.rl_seven -> {
                if (passwords.size < 4) {
                    passwords.add(7)
                    uiChange()
                }

            }
            R.id.rl_eight -> {
                if (passwords.size < 4) {
                    passwords.add(8)
                    uiChange()
                }

            }
            R.id.rl_nine -> {
                if (passwords.size < 4) {
                    passwords.add(9)
                    uiChange()
                }

            }
            R.id.rl_zero -> {
                if (passwords.size < 4) {
                    passwords.add(0)
                    uiChange()
                }

            }
            R.id.rl_cancle -> {
                if (passwords.size > 0) {
                    passwords.clear()
                    uiChange()
                }

            }
            R.id.rl_back -> {
                if (passwords.size > 0) {
                    passwords.removeAt(passwords.size - 1)
                    uiChange()
                }
            }
            R.id.rl_password -> {
                if (tv_user.text == getText(R.string.login_tips)) {
                    Helper.toast(resources.getString(R.string.login_tips))
                } else {
                    rl_button.visibility = View.GONE
                    ll_keyboard.visibility = View.VISIBLE
                }
            }
            R.id.rl_button -> {
                if ("root" == tv_user.text) {
                    val passwd = StringBuffer()
                    for (j in passwords.indices) {
                        passwd.append(passwords[j])
                    }
                    val login = LoginReq().robot("root", passwd.toString())
                    sendwebSocket(login)
                    return
                }

                val name = tv_user.text.toString()
                if (passwords.size > 3 && tv_user.text.toString().isNotEmpty() && (getText(R.string.login_tips).toString() != name)) {
                    val passwd = StringBuffer()
                    for (j in passwords.indices) {
                        passwd.append(passwords[j])
                    }
                    showWaite()
                    login = LoginReq().robot(tv_user.text.toString(), passwd.toString())
                    sendwebSocket(login)
                } else {
                    BToast.showText(this@LoginActivity, getText(R.string.error_logininfo))
                }
            }
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
        setContentView(R.layout.activity_login)

        intentFrom = intent.get<Int>(BackToCDDActivity.THISSTATUS_) ?: -1
        type = intent.get("type") ?: "C"
        isIndex = true
        Const.fieldModel = robotApp!!.mSharedPreferencesHelper!!.get(
            Const.SP_MODE,
            Const.INDOOR_MODEL.common_model.toString()
        ).toString()
        initRecyclerView()
        sendwebSocket(GetAllUsersReq().toString())

        rl_password.setOnClickListener(this@LoginActivity)
        rl_one.setOnClickListener(this@LoginActivity)
        rl_two.setOnClickListener(this@LoginActivity)
        rl_three.setOnClickListener(this@LoginActivity)
        rl_four.setOnClickListener(this@LoginActivity)
        rl_five.setOnClickListener(this@LoginActivity)
        rl_six.setOnClickListener(this@LoginActivity)
        rl_seven.setOnClickListener(this@LoginActivity)
        rl_eight.setOnClickListener(this@LoginActivity)
        rl_nine.setOnClickListener(this@LoginActivity)
        rl_zero.setOnClickListener(this@LoginActivity)
        rl_cancle.setOnClickListener(this@LoginActivity)
        rl_back.setOnClickListener(this@LoginActivity)
        rl_login.setOnClickListener(this@LoginActivity)
        rl_button.setOnClickListener(this@LoginActivity)

        oldname = robotApp!!.mSharedPreferencesHelper!!.get(Const.SP_USER, "").toString()


        handler.postDelayed(runnable, 30000)


        mSwipeRefreshLayout.setOnRefreshListener {
            sendwebSocket(GetAllUsersReq().toString())
            mSwipeRefreshLayout.isRefreshing = false
        }
    }

    override fun disposeMsg(type: Int, obj: Any) {
        when (type) {
            0 -> {
                oldname = (obj as UserInfo).name
            }

        }
    }

    override fun onResume() {
        super.onResume()
        sendwebSocket(GetAllUsersReq().toString())
        sendwebSocket(GetRobotInfoReq().toString())
    }

    private fun initRecyclerView() {

        rv_users = findViewById(R.id.rv_users);
        userAdapter = RecycleAdapterUser(this@LoginActivity, names)
        userAdapter!!.setOnItemClickListener(object : RecycleAdapterUser.OnItemClickListener {
            override fun itemClick(item: Int) {
                tv_user.text = names[item].name
                show = false
                img_down.setImageResource(R.drawable.down)
                rv_users!!.visibility = View.GONE
                rl_pw.visibility = View.VISIBLE
                rl_button.visibility = View.GONE
                ll_keyboard.visibility = View.VISIBLE
                if (passwords.size > 0) {
                    passwords.clear()
                    uiChange()
                }
            }

        })
        val layoutManager = LinearLayoutManager(this)
        rv_users!!.layoutManager = layoutManager
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        rv_users!!.adapter = userAdapter
        rv_users!!.addItemDecoration(
            Divider.builder()
                .color(Color.parseColor("#55f0f0f0"))
                .height(1)
                .build()
        );
    }


    private fun uiChange() {
        btn_pwone.setText("")
        btn_pwtwo.setText("")
        btn_pwthree.setText("")
        btn_pwfour.setText("")

        if (passwords.size == 0) {
            btn_pwone.isFocusable = true
            btn_pwone.isFocusableInTouchMode = true
            btn_pwone.requestFocus()
        }
        if (passwords.size > 0) {
            btn_pwone.setText(passwords.get(0).toString() + "")
            btn_pwtwo.isFocusable = true
            btn_pwtwo.isFocusableInTouchMode = true
            btn_pwtwo.requestFocus()
        }
        if (passwords.size > 1) {
            btn_pwtwo.setText(passwords.get(1).toString() + "")
            btn_pwthree.isFocusable = true
            btn_pwthree.isFocusableInTouchMode = true
            btn_pwthree.requestFocus()
        }
        if (passwords.size > 2) {
            btn_pwthree.setText(passwords.get(2).toString() + "")
            btn_pwfour.isFocusable = true
            btn_pwfour.isFocusableInTouchMode = true
            btn_pwfour.requestFocus()
        }
        if (passwords.size > 3) {
            btn_pwfour.setText(passwords.get(3).toString() + "")

            rl_button.visibility = View.VISIBLE
            ll_keyboard.visibility = View.GONE
        }

    }

    override fun onMessage(message: String): Int {
        val type = super.onMessage(message)
        val res = fromJson(message, Response::class.java) ?: return 0
        when (type) {
            12007 -> {
                val allUsersRes = fromJson(message, GetAllUsersRes::class.java)
                val alluser = allUsersRes!!.getJson()!!.users
                names.clear()
                names.addAll(alluser)
//                if (oldname.isNotEmpty() && names.size > 0) {
//                    tv_user.text = oldname
//                } else {
//                    tv_user.text = getText(R.string.login_tips)
//                }
                userAdapter!!.notifyDataSetChanged()
                tv_user.text = getText(R.string.login_tips)
                oldname?.let {
                    for (n in names) {
                        if (n.name == it) {
                            tv_user.text = it
                            break
                        }
                    }
                }

            }
            24004 -> {
                val taskReportRes = JsonUtils.fromJson(message, TaskReportRes::class.java)
                val resultBean = taskReportRes?.getJson()
                if (resultBean != null) {
                    if (resultBean.task_status != 13 && resultBean.task_status != 16 && resultBean.task_status != 17) {
                        intentFrom = 3
                    }
                }
            }
            11000 -> {
                val robotInfoRes = JsonUtils.fromJson(message, GetRobotInfoRes::class.java)
                val robotInfo = robotInfoRes!!.getJson()
                robotInfo?.let {
                    Const.type = it.type
                    F.saveJson("type", it.type)
                }
            }
            12003 -> {
                dismissWaite()
                val loginRes = fromJson(message, LoginRes::class.java) ?: return 0
                if (res.error_code == 0 || res.error_code == -3) {
                    Const.user = loginRes.getJson()
                    robotApp!!.mSharedPreferencesHelper!!.put(Const.SP_USER, Const.user!!.name)
                    if (!TextUtils.isEmpty(login)) saveJson("login", login)
                    Log.i("intentFrom 点击", intentFrom.toString())
                    when (intentFrom) {
                        -1 -> {
                            if (Const.user!!.user_type == UserInfo.USER) {
                                startActivity(intentFor<CleanMainActivity>().clearTop())
                                Frame.HANDLES.close("LoginActivity")
                                finish()
                            } else {
                                startActivity<AdminAccountActivity>()
                            }
                        }
                        1 -> {
                            Frame.HANDLES.sentAll("PlayActivity", 700, this.type)
                            Frame.HANDLES.sentAll("BackToCDDActivity", 702, 1)
                            Frame.HANDLES.close("LoginActivity")
                            finish()
                        }
                        3 -> {
                            Frame.HANDLES.closeIds("BackToCDDActivity,PlayActivity")
                            startActivity(intentFor<TaskReportActivity>(TASKCOMPLETE_FROMPALYACTIVITY to 60).clearTop())
                            Frame.HANDLES.close("LoginActivity")
                            finish()
                        }
                        else -> {
                            Frame.HANDLES.sentAll("BackToCDDActivity,StandbyActivity", 702, 1)
                            Frame.HANDLES.close("LoginActivity")
                            finish()
                        }
                    }
                } else if (res.error_code == -1) {
                    Helper.toast(getString(R.string.robot_login_error))
                    rl_button.visibility = View.GONE
                    ll_keyboard.visibility = View.VISIBLE
                    if (passwords.size > 0) {
                        passwords.clear()
                        uiChange()
                    }
//                    BToast.showText(
//                        this@LoginActivity,
//                        getText(R.string.robot_login_error).toString()
//                    )
                }
            }
        }
        return type
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        return if (intentFrom == -1) super.onKeyDown(keyCode, event) else keyCode == KeyEvent.KEYCODE_BACK
    }

}