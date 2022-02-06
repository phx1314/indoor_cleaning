package com.deepblue.cleaning.activity

import android.os.Bundle
import android.view.View
import android.view.Window
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import androidx.viewpager.widget.ViewPager
import com.deepblue.cleaning.Const
import com.deepblue.cleaning.R
import com.deepblue.cleaning.adapter.InstructionAdapter
import kotlinx.android.synthetic.main.activity_instruction.*

class InstructionActivity : BaseActivity() {
    private var instructionAdapter: InstructionAdapter? = null
    private var currentIndex = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.activity_instruction)
        initViews()
    }

    private fun initViews() {
        instructions_vp!!.pageMargin = 15
        instructionAdapter = InstructionAdapter(this)
        instructions_vp!!.adapter = instructionAdapter
        setCircles()
        if (Const.type != "10") cycle_indicator_ll.visibility = View.INVISIBLE
        instructions_vp!!.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
            }

            override fun onPageSelected(position: Int) {
                cycle_indicator_ll!!.getChildAt(currentIndex).isEnabled = false
                cycle_indicator_ll!!.getChildAt(position).isEnabled = true
                currentIndex = position
            }

            override fun onPageScrollStateChanged(state: Int) {}
        })
        back_rl!!.setOnClickListener { finish() }
    }

    private fun setCircles() {
        var imageView: ImageView
        for (i in 0..2) {
            imageView = ImageView(this)
            imageView.background =
                getDrawable(R.drawable.indicator_selector) //自己在Drawable文件夹目录下写的xml文件
            imageView.isEnabled = i == 0
            val params =
                LinearLayout.LayoutParams(15, 15)
            //设置间隔
            if (i != 0) {
                params.leftMargin = 30 //如果不是第一个，就设置左边距为10dp
            }
            cycle_indicator_ll!!.addView(imageView, params) //ll是在xml文件中装原点的LinearLayout的id；
        }
    }
}