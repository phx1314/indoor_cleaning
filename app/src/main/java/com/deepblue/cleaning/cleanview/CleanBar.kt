package com.deepblue.cleaning.cleanview

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import com.deepblue.cleaning.Const
import com.deepblue.cleaning.R
import com.deepblue.cleaning.activity.MalfunctionActivity
import com.deepblue.cleaning.utils.CommonUtil
import org.jetbrains.anko.startActivity
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

class CleanBar(context: Context?, attrs: AttributeSet?) :
    RelativeLayout(context, attrs) {
    private var ivError: ImageView? = null
    private var iv4G: ImageView? = null
    private var tvPower: TextView? = null
    private var tvTime: TextView? = null
    private var batteryView: BatteryView? = null
    fun setError(show: Boolean) {
        if (show) {
            ivError!!.visibility = View.VISIBLE
        } else {
            ivError!!.visibility = View.GONE
        }
    }

    fun refData() {
        setError(Const.systemError)
        if (Const.system4G) {
            iv4G!!.visibility = View.VISIBLE
        } else {
            iv4G!!.visibility = View.GONE
        }
        if (Const.systemPower > 100) {
            tvPower!!.text = "100%"
            batteryView!!.setPower(100)
        } else if (Const.systemPower >= 0) {
            tvPower!!.text = Const.systemPower.toString() + "%"
            batteryView!!.setPower(Const.systemPower)
        }
        if (Const.systemPower > 0) {
            batteryView!!.visibility = View.VISIBLE
            tvPower!!.visibility = View.VISIBLE
        }
        var time = "00:00"
        val df: DateFormat = SimpleDateFormat("HH:mm")
        val date = Date(Const.systemTime)
        time = df.format(date)
        tvTime!!.text = time
    }

    private fun findView() {
        ivError = findViewById(R.id.ivError)
        iv4G = findViewById(R.id.iv4G)
        tvPower = findViewById(R.id.tvPower)
        tvTime = findViewById(R.id.tvTime)
        batteryView = findViewById(R.id.batteryView)

    }

    override fun setOnClickListener(mOnClickListener: OnClickListener?) {
        ivError!!.setOnClickListener(mOnClickListener)
    }

    init {
        LayoutInflater.from(context)
            .inflate(R.layout.layout_title_bar_status, this, true)
        findView()
    }
}