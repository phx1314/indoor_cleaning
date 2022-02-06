package com.deepblue.cleaning.activity

import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.PopupWindow
import com.deepblue.cleaning.Const
import com.deepblue.cleaning.R
import com.deepblue.cleaning.adapter.UserListAdapter
import com.deepblue.cleaning.cleanview.AlertDialog
import com.deepblue.cleaning.cleanview.BToast
import com.deepblue.cleaning.item.PopChangeLevel
import com.deepblue.cleaning.item.PopKeybord
import com.deepblue.cleaning.item.PopShowChangeLevel
import com.deepblue.cleaning.item.PopShowKeyBord
import com.deepblue.cleaning.utils.DialogUtils
import com.deepblue.library.planbmsg.JsonUtils
import com.deepblue.library.planbmsg.Response
import com.deepblue.library.planbmsg.bean.UserInfo
import com.deepblue.library.planbmsg.msg2000.*
import com.mdx.framework.Frame
import kotlinx.android.synthetic.main.activity_admin_account.*
import kotlinx.android.synthetic.main.layout_user_list.*
import org.jetbrains.anko.clearTop
import org.jetbrains.anko.intentFor
import org.jetbrains.anko.startActivity
import java.util.*

class AdminAccountActivity : BaseActivity() {

    var userListAdapter: UserListAdapter? = null

    var userList: ArrayList<UserInfo>? = ArrayList()

    var user: UserInfo? = null
    var item = -1
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
        setContentView(R.layout.activity_admin_account)
        initViews()
        sendwebSocket(GetAllUsersReq().toString())
    }

    fun updateUI(v: View) {
        user_name_et.setBackgroundResource(0)
        password_et.setBackgroundResource(0)
        mobile_et.setBackgroundResource(0)
        email_et.setBackgroundResource(0)
        var mDialogPub = PopKeybord(this)
        var mPopShowKeyBord = PopShowKeyBord(this, mLinearLayout_yx, mDialogPub)
        when (v.id) {
            R.id.user_name_rl -> {
                user_name_et.setBackgroundResource(R.drawable.input_box_bg_chosen)
                mDialogPub.set(mPopShowKeyBord, user_name_et)
            }
            R.id.password_rl -> {
                password_et.setBackgroundResource(R.drawable.input_box_bg_chosen)
                mDialogPub.set(mPopShowKeyBord, password_et)
            }
            R.id.mobile_rl -> {
                mobile_et.setBackgroundResource(R.drawable.input_box_bg_chosen)
                mDialogPub.set(mPopShowKeyBord, mobile_et)
            }
            R.id.email_rl -> {
                email_et.setBackgroundResource(R.drawable.input_box_bg_chosen)
                mDialogPub.set(mPopShowKeyBord, email_et)
            }
        }
        mPopShowKeyBord.show()
        mPopShowKeyBord.setOnDismissListener(PopupWindow.OnDismissListener {
            isEnableAdd()
        })
    }

    override fun disposeMsg(type: Int, obj: Any) {
        when (type) {
            0 -> {
                dialogFragment = DialogUtils.showAlert(this@AdminAccountActivity,
                    getText(R.string.sure_delete_user).toString(),
                    R.string.sure_delete,
                    object : AlertDialog.DialogButtonListener {
                        override fun cancel() {

                        }

                        override fun ensure(isCheck: Boolean): Boolean {
                            val loginReq = LogoffReq(userList!![obj.toString().toInt()].name, obj.toString().toInt()).toString()
                            Frame.HANDLES.sentAll("LoginActivity", 1, "")
                            sendwebSocket(loginReq)
                            return true
                        }
                    })
            }
        }

    }

    fun initViews() {

        userListAdapter = UserListAdapter(this, userList)
        user_list_lv.adapter = userListAdapter
        userListAdapter!!.setItemClickCallback(object : UserListAdapter.ItemClickCallback {
            override fun itemClick(userBean: UserInfo, position: Int) {
                item = position
//                showUserInfo(userList!![position])
            }
        })
        add_account_iv.setOnClickListener(this)
        rl_content_admin.setOnClickListener(this)
        btn_save.setOnClickListener(this)
        delete_user_iv.setOnClickListener(this)
        choose_user_iv.setOnClickListener(this)
        back_rl.setOnClickListener(this)
        user_name_rl.setOnClickListener { updateUI(it) }
        password_rl.setOnClickListener { updateUI(it) }
        mobile_rl.setOnClickListener { updateUI(it) }
        email_rl.setOnClickListener { updateUI(it) }
    }

    override fun onClick(v: View) {
        super.onClick(v)
        when (v.id) {
            R.id.back_rl -> {
                Frame.HANDLES.close("StandbyActivity")
                finish()
            }
            R.id.add_account_iv -> showUserInfo(null)
            R.id.rl_content_admin -> {
                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm?.hideSoftInputFromWindow(window.decorView.windowToken, 0)
            }
            R.id.btn_save -> {
                user = UserInfo()
                user!!.name = user_name_et.text.toString()
                user!!.passwd = password_et.text.toString()
                user!!.phone = mobile_et.text.toString()
                user!!.email = email_et.text.toString()
                user!!.user_type = UserInfo.USER
                val addReq = RegisterModifyReq(user!!).register()
                sendwebSocket(addReq)
                showUserInfo(UserInfo())
            }
            R.id.delete_user_iv -> {
                if (userList.isNullOrEmpty()) {
                    return
                }

                dialogFragment = DialogUtils.showAlert(this,
                    getText(R.string.sure_delete_user).toString(),
                    R.string.sure_delete,
                    object : AlertDialog.DialogButtonListener {
                        override fun cancel() {

                        }

                        override fun ensure(isCheck: Boolean): Boolean {
                            val loginReq = LogoffReq(userList!![item].name, item).toString()
                            sendwebSocket(loginReq)
                            return true
                        }
                    })
            }
            R.id.back_rl -> {
                startActivity<LoginActivity>()
                finish()
            }
            R.id.choose_user_iv -> {
                if (userList!!.size > 0 && item >= 0) {
                    Frame.HANDLES.sentAll("LoginActivity", 0, userList!![item])
                    finish()
                }
            }
        }
    }

    fun isEnableAdd() {
        if (user_name_et.text.toString().isNotEmpty() &&  user_name_et.text.toString().trim() != "root" && password_et.text.toString()
                .isNotEmpty() && password_et.text.toString().length > 3
        ) {
            btn_save.isEnabled = !duplicatename(user_name_et.text.toString())
        } else {
            btn_save.isEnabled = false
        }
    }

    fun duplicatename(name: String): Boolean {
        for (user in userList!!) {
            if (user.name == name) {
                return true
            }
        }
        return false
    }


    fun showUserInfo(userBean: UserInfo?) {
        user_name_et.text = userBean?.name ?: ""
        password_et.text = userBean?.passwd ?: ""
        mobile_et.text = userBean?.phone ?: ""
        email_et.text = userBean?.email ?: ""
        isEnableAdd()
    }

    override fun onMessage(message: String): Int {
        val type = super.onMessage(message)
        val res = JsonUtils.fromJson(message, Response::class.java) ?: return 0
        when (type) {
            12007 -> {
                val allUsersRes = JsonUtils.fromJson(message, GetAllUsersRes::class.java)
                val alluser = allUsersRes!!.getJson()!!.users
                userList!!.clear()
                for (index in alluser) {
                    if (index.user_type == UserInfo.USER) {
                        userList!!.add(index)
                    }
                }
                userListAdapter!!.notifyDataSetChanged()

                choose_user_iv.isEnabled = userList!!.size > 0
            }

            12003 -> {
                if (res.error_code == 0 || res.error_code == -3) {
                    startActivity(intentFor<CleanMainActivity>().clearTop())
                    finish()
                }
            }

            12001 -> {
                if (res.error_code == 0) {
                    userList!!.add(user!!)
                    userListAdapter!!.notifyDataSetChanged()
                    choose_user_iv.isEnabled = userList!!.size > 0
                }
            }

            12002 -> {
                if (res.error_code == 0) {
                    userList!!.removeAt(res.number)
                    item = -1
//                    if (userList!!.size in 1..item) {
//                        item = userList!!.size - 1
//                    }
                    userListAdapter!!.updateData(item)
                    BToast.showText(
                        this@AdminAccountActivity,
                        getText(R.string.del_success).toString()
                    )
                    choose_user_iv.isEnabled = userList!!.size > 0
                } else {
                    BToast.showText(
                        this@AdminAccountActivity,
                        getText(R.string.do_error).toString()
                    )
                }
            }
        }

        return type
    }
}