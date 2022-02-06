package com.deepblue.cleaning.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.viewpager.widget.PagerAdapter
import com.deepblue.cleaning.Const
import com.deepblue.cleaning.R

class InstructionAdapter(private val context: Context) : PagerAdapter() {
    private val picArray: IntArray = intArrayOf(
        R.drawable.instruction_fst_page,
        R.drawable.instruction_sec_page,
        R.drawable.instruction_trd_page
    )
    private val picArray_hx: IntArray = intArrayOf(R.drawable.bzt)

    override fun getCount(): Int {
        return if (Const.type == "10") picArray.size else picArray_hx.size
    }

    override fun isViewFromObject(
        view: View,
        `object`: Any
    ): Boolean {
        return view === `object`
    }

    override fun instantiateItem(
        container: ViewGroup,
        position: Int
    ): Any {
        val view = LayoutInflater.from(context).inflate(
            R.layout.item_instruction,
            null
        ) as ImageView
        if (position >= 0 && position < if (Const.type == "10") picArray.size else picArray_hx.size) {
            view.background = context.getDrawable(if (Const.type == "10") picArray[position] else picArray_hx[position])
        }
        container.addView(view)
        return view
    }

    override fun destroyItem(
        container: ViewGroup,
        position: Int,
        `object`: Any
    ) {
        container.removeView(`object` as View)
    }

}