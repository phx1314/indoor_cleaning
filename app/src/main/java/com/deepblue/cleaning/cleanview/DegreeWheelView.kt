package com.deepblue.cleaning.cleanview

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.RelativeLayout
import com.deepblue.cleaning.R
import kotlinx.android.synthetic.main.gro_view_degree_wheel.view.*

/**
 * 根据UI定制的DegreeWheel
 */
class DegreeWheelView: RelativeLayout {

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        LayoutInflater.from(context).inflate(R.layout.gro_view_degree_wheel, this, true)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        if (height > 0) {
            val layoutParams = item.layoutParams
            val itemHeight = height / 5
            if (layoutParams.height < itemHeight) {
//                layoutParams.height += itemHeight / 4
                layoutParams.height = itemHeight
            }
            item.layoutParams = layoutParams
        }
    }

    fun setListener(listener: DegreeWheel.Listener) {
        degreewheel.listener = listener
    }

    fun setDegree(degree: Int) {
        degreewheel.setDegree(degree)
    }
}