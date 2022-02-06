package com.deepblue.cleaning.utils

import android.text.SpannableString
import android.text.Spanned
import android.text.style.RelativeSizeSpan
import android.text.style.SuperscriptSpan

object CleanUtil {

    @JvmStatic
    public fun setAreaTextSize(area: String?): SpannableString {
        var areaSpan = SpannableString(area)
        val mIndex = area!!.indexOf("m2")
        areaSpan.setSpan(
            RelativeSizeSpan(0.5f),
            mIndex,
            areaSpan.length,
            Spanned.SPAN_INCLUSIVE_INCLUSIVE
        )

        areaSpan.setSpan(
            SuperscriptSpan(),
            areaSpan.length - 1,
            areaSpan.length,
            Spanned.SPAN_INCLUSIVE_INCLUSIVE
        )

        return areaSpan
    }

    @JvmStatic
    fun setTimeTextSize(time: String?): SpannableString {
        var timeSpan = SpannableString(time)
        var hourIndex = 0
        if(time!!.contains("h")){
            hourIndex = time!!.indexOf("h")
            timeSpan.setSpan(
                RelativeSizeSpan(0.5f),
                hourIndex,
                hourIndex + 1,
                Spanned.SPAN_INCLUSIVE_INCLUSIVE
            )
        }else if(time!!.contains("H")){
            hourIndex = time!!.indexOf("H")
            timeSpan.setSpan(
                RelativeSizeSpan(0.5f),
                hourIndex,
                hourIndex + 1,
                Spanned.SPAN_INCLUSIVE_INCLUSIVE
            )
        }
        val minIndex = time.indexOf("m")
        //一半大小
        timeSpan.setSpan(
            RelativeSizeSpan(0.5f),
            minIndex,
            time.length,
            Spanned.SPAN_INCLUSIVE_INCLUSIVE
        )

        return timeSpan
    }

    @JvmStatic
    public fun setLifterTextSize(lifer: String?): SpannableString {
        var lifterSpan = SpannableString(lifer)
        val hourIndex = lifer!!.indexOf("L")
        //一半大小
        lifterSpan.setSpan(
            RelativeSizeSpan(0.5f),
            hourIndex,
            lifer.length,
            Spanned.SPAN_INCLUSIVE_INCLUSIVE
        )

        return lifterSpan
    }

    @JvmStatic
    public fun setPercentTextSize(lifer: String?): SpannableString {
        var lifterSpan = SpannableString(lifer)
        //一半大小
        lifterSpan.setSpan(
            RelativeSizeSpan(0.5f),
            lifer!!.length - 1,
            lifer!!.length,
            Spanned.SPAN_INCLUSIVE_INCLUSIVE
        )

        return lifterSpan
    }
}