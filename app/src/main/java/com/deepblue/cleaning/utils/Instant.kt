package com.deepblue.disinfect.utils

/**
 * 与Duration配套使用计算时间差
 * 如果SDK版本大于等于26（Android 8.0），直接使用java.time.Instant
 */
class Instant(var time: Long) {

    companion object {

        @JvmStatic
        fun now(): Instant {
            return Instant(System.currentTimeMillis())
        }
    }
}