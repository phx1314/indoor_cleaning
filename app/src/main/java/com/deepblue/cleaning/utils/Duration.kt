package com.deepblue.disinfect.utils

import java.text.SimpleDateFormat
import java.util.*

/**
 * 计算时间差
 * 如果SDK版本大于等于26（Android 8.0），直接使用java.time.Duration
 */
object Duration {
    var formatter = SimpleDateFormat("yyyy-MM-dd HH:mm")
    var nameFormatter = SimpleDateFormat("yyyyMMdd_HHmm")
    var allformatter = SimpleDateFormat("yyyy-mm-dd hh:mm:ss")

    private var start: Instant? = null
    private var end: Instant? = null

    @JvmStatic
    fun getTime(time: Long): String? {
        val date = Date(time * 1000)
        return formatter.format(date)
    }

    @JvmStatic
    fun getSystime(date:String): Long {
       return formatter.parse(date).time
    }

    @JvmStatic
    fun getTime(): String? {
        return formatter.format(Date())
    }

    @JvmStatic
    fun getNameTime(time: Long): String? {
        val date = Date(time * 1000)
        nameFormatter.setTimeZone(
            TimeZone.getTimeZone(
                "GMT+8"
            )
        )
        return nameFormatter.format(date)
    }

    @JvmStatic
    fun between(start: Instant, end: Instant): Duration {
        this.start = start
        this.end = end
        return this
    }

    @JvmStatic
    fun getSeconds(): Long {
        val millis = toMillis()
        return millis / 1000
    }

    @JvmStatic
    fun toMillis(): Long {
        if (end == null || start == null) {
            return 0
        }
        val endDate = Date(end!!.time)
        val startDate = Date(start!!.time)
        return endDate.time - startDate.time
    }
}