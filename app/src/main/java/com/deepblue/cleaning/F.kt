package com.deepblue.cleaning

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Context.INPUT_METHOD_SERVICE
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.preference.PreferenceManager
import android.text.TextUtils
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView
import androidx.core.content.ContextCompat.getSystemService
import com.deepblue.cleaning.RobotApplication.Companion.deviceCode
import com.deepblue.cleaning.item.PopKeybord
import com.deepblue.cleaning.item.PopShowKeyBordGc
import com.google.gson.Gson
import com.mdx.framework.Frame
import com.mdx.framework.view.CallBackOnly
import java.util.regex.Pattern


object F {
    fun init() {
        deviceCode = getJson("deviceCode") ?: ""
        Const.type = if (TextUtils.isEmpty(getJson("type"))) "2" else getJson("type") ?: "2"
        getJson("min_battery_level_to_back")?.let {
            if (!TextUtils.isEmpty(it)) Const.min_battery_level_to_back = it.toInt()
        }

    }

    fun <T> data2Model(data: String?, mclass: Class<T>): T {
        return Gson().fromJson(data, mclass)
    }

//    // kfc 1
//    // / 关闭软件盘
//    fun closeSoftKey(act: Activity) {
//        val localInputMethodManager = act.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
//        val localIBinder = act.getWindow().getDecorView().getWindowToken()
//        localInputMethodManager.hideSoftInputFromWindow(localIBinder, 2)
//        // InputMethodManager imm = (InputMethodManager) getActivity()
//        // .getSystemService(Context.INPUT_METHOD_SERVICE);
//        // imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
//    }

    fun showKeyBord(v: TextView, context: Context, mLinearLayout: LinearLayout, from: String, isKeyAll: Boolean = false, isShowEdit: Boolean = false) {
        var mDialogPub = PopKeybord(context)
        var mPopShowKeyBord = PopShowKeyBordGc(context!!, mLinearLayout, mDialogPub)
        mDialogPub.set(mPopShowKeyBord, v, isKeyAll, isShowEdit)
        mPopShowKeyBord.setOnDismissListener(PopupWindow.OnDismissListener {
            Frame.HANDLES.sentAll(from, 110, v)
        })
        mPopShowKeyBord.show()

    }

    fun showCenterDialog(
        context: Activity, view: View?,
        mCallBackOnly: CallBackOnly
    ) {
        val mDialog = Dialog(context, R.style.dialog_special)
        mDialog.setContentView(
            view!!, LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
            )
        )
        val windowManager = context.windowManager
        val display = windowManager.defaultDisplay
        val lp = mDialog.window!!.attributes
//        lp.alpha = 0.7f;
        lp.width = display.width  // 设置宽度
        lp.height = display.height// 高度设置为屏幕的0.6
        lp.gravity = Gravity.CENTER
        mDialog.window!!.attributes = lp
        mDialog.show()
        mDialog.setCanceledOnTouchOutside(true)
        mCallBackOnly.goReturnDo(mDialog)
    }

    /**
     * 判断字符串是否是金额
     * @param str
     * @return
     */
    fun isNumber(str: String?): Boolean {
        val pattern = Pattern.compile("^(([1-9]{1}\\d*)|([0]{1}))(\\.(\\d){0,2})?$") // 判断小数点后2位的数字的正则表达式
        val match = pattern.matcher(str)
        return match.matches() != false
    }

    fun getJson(key: String): String? {
        val sp = PreferenceManager.getDefaultSharedPreferences(Frame.CONTEXT)
        return sp.getString(key, "")
    }

    fun saveJson(key: String, json: String?) {
        val sp = PreferenceManager.getDefaultSharedPreferences(Frame.CONTEXT)
        sp.edit().putString(key, json).apply()
    }


    /**
     * [获取应用程序版本名称信息]
     * @param context
     * @return 当前应用的版本名称
     */
    @Synchronized
    fun getVersionName(context: Context): String? {
        try {
            val packageManager: PackageManager = context.packageManager
            val packageInfo: PackageInfo = packageManager.getPackageInfo(
                context.packageName, 0
            )
            return packageInfo.versionName
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }


    fun dp2px(dp: Int): Int {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp.toFloat(), Frame.CONTEXT.getResources().getDisplayMetrics()).toInt()
    }

    fun go2DownloadUrl(context: Context) {
        val intent = Intent()
        intent.setAction("android.intent.action.VIEW")
        val content_url: Uri = Uri.parse("https://www.pgyer.com/oO8T")
        intent.setData(content_url)
        context.startActivity(intent)
    }

    /**
     * 隐藏键盘
     */
    fun hideInput(context: Activity) {
        val imm: InputMethodManager? = context.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager?
        val v: View = context.window.peekDecorView()
        if (null != v) {
            imm?.hideSoftInputFromWindow(v.windowToken, 0)
        }
    }
}












