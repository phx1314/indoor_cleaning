package com.deepblue.cleaning.utils

import android.content.Context
import android.util.TypedValue

object DensityUtil {
    var RATIO = 0.95f //缩放比例值
    /**
     * px 转 dp【按照一定的比例】
     */
    @JvmStatic
    fun px2dipRatio(context: Context, pxValue: Float): Int {
        val scale = getScreenDendity(context) * RATIO
        return (pxValue / scale + 0.5f).toInt()
    }

    /**
     * dp转px【按照一定的比例】
     */
    @JvmStatic
    fun dip2pxRatio(context: Context, dpValue: Float): Int {
        val scale = getScreenDendity(context) * RATIO
        return (dpValue * scale + 0.5f).toInt()
    }

    /**
     * px 转 dp
     *
     *
     * 48px - 16dp
     *
     *
     * 50px - 17dp
     */
    @JvmStatic
    fun px2dip(context: Context, pxValue: Float): Int {
        val scale = getScreenDendity(context)
        return (pxValue / scale + 0.5f).toInt()
    }

    /**
     * dp转px
     *
     *
     * 16dp - 48px
     *
     *
     * 17dp - 51px
     */
    @JvmStatic
    fun dip2px(context: Context, dp: Int): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp.toFloat(),
            context.resources.displayMetrics
        ).toInt()
    }

    /**
     * 获取屏幕的宽度（像素）
     */
    @JvmStatic
    fun getScreenWidth(context: Context): Int {
        return context.resources.displayMetrics.widthPixels //1080
    }

    /**
     * 获取屏幕的宽度（dp）
     */
    @JvmStatic
    fun getScreenWidthDp(context: Context): Int {
        val scale = getScreenDendity(context)
        return (context.resources.displayMetrics.widthPixels / scale).toInt() //360
    }

    /**
     * 获取屏幕的高度（像素）
     */
    @JvmStatic
    fun getScreenHeight(context: Context): Int {
        return context.resources.displayMetrics.heightPixels //1776
    }

    /**
     * 获取屏幕的高度（像素）
     */
    @JvmStatic
    fun getScreenHeightDp(context: Context): Int {
        val scale = getScreenDendity(context)
        return (context.resources.displayMetrics.heightPixels / scale).toInt() //592
    }

    /**
     * 屏幕密度比例
     */
    @JvmStatic
    fun getScreenDendity(context: Context): Float {
        return context.resources.displayMetrics.density //3
    }

    /**
     * 获取状态栏的高度 72px
     *
     *
     * http://www.2cto.com/kf/201501/374049.html
     */
    @JvmStatic
    fun getStatusBarHeight(context: Context): Int {
        var statusHeight = -1
        try {
            val aClass =
                Class.forName("com.android.internal.R\$dimen")
            val `object` = aClass.newInstance()
            val height = aClass.getField("status_bar_height")[`object`].toString().toInt()
            statusHeight = context.resources.getDimensionPixelSize(height)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return statusHeight
    }

    /**
     * 指定机型（displayMetrics.xdpi）下dp转px
     *
     *
     * 18dp - 50px
     */
    @JvmStatic
    fun dpToPx(context: Context, dp: Int): Int {
        return Math.round(dp.toFloat() * getPixelScaleFactor(context))
    }

    /**
     * 指定机型（displayMetrics.xdpi）下px 转 dp
     *
     *
     * 50px - 18dp
     */
    @JvmStatic
    fun pxToDp(context: Context, px: Int): Int {
        return Math.round(px.toFloat() / getPixelScaleFactor(context))
    }

    /**
     * 获取水平方向的dpi的密度比例值
     *
     *
     * 2.7653186
     */
    @JvmStatic
    fun getPixelScaleFactor(context: Context): Float {
        val displayMetrics = context.resources.displayMetrics
        return displayMetrics.xdpi / 160.0f
    }
}