package com.deepblue.cleaning.cleanview

import android.content.Context
import android.util.AttributeSet
import com.deepblue.cleaning.Const
import com.deepblue.library.wheelpicker.WheelPicker
import java.util.*
import kotlin.math.abs

/**
 * 角度值选择
 */
class DegreeWheel: WheelPicker {

    interface Listener {
        fun onRadians(radians: Double)
    }

    var listener: Listener? = null
        set(value) {
            field = value
            this.setOnItemSelectedListener { picker, data, position ->
                val text = data.toString()
                val valueInt = Integer.valueOf(text)
//                val angrad = Math.toRadians(valueInt.toDouble())
                val angrad = valueInt.toDouble()
                listener?.onRadians(angrad)
            }
        }

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        val per = 5
        val size = 360 / per
        val list = LinkedList<String>()
//        for (i in 0 until size) {
//            list.gro_add("${i * per}")
//        }
        for (i in size - 1 downTo 0) {
            list.add("${i * per}")
        }
        data = list
        unitText = "°"
        maximumWidthText = "360$unitText"
    }

    fun setDegree(degree: Int) {
        var minDef = 360//最小差值
        var index = -1
        for (i in 0 until data.size) {
            val text = data[i].toString()
            val valueInt = Integer.valueOf(text)
            val def = abs(valueInt - degree)
            if (def < minDef) {
                minDef = def
                index = i
            }
        }
//        selectedItemPosition = index
        setSelectedItemPosition(index, false)
    }
}