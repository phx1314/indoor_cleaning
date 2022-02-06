package com.deepblue.cleaning.cleanview

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.widget.ProgressBar
import com.deepblue.cleaning.R
import com.deepblue.cleaning.utils.DensityUtil.dip2px

/*** 圆形进度条 ***/
class RoundProgressBar @JvmOverloads constructor(
    context: Context?,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : ProgressBar(context, attrs, defStyle) {
    /**
     * 圆环的颜色
     */
    private val roundColor: Int

    /**
     * 圆环进度的颜色
     */
    private val roundProgressColor: Int
    private var mRadius: Int
    private val mReachHeight: Int
    private var mRectf: RectF? = null
    protected var mPaint =
        Paint(Paint.ANTI_ALIAS_FLAG) //消除锯
    private var gradient: LinearGradient? = null

    @Synchronized
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val diameter =
            mRadius * 2 + paddingLeft + paddingRight //控件宽度 默认四个padding一致
        val width = View.resolveSize(diameter, widthMeasureSpec)
        val height = View.resolveSize(diameter, heightMeasureSpec)
        val realWidth = Math.min(width, height) //当宽高设置不一致，取小的那个
        mRadius = (realWidth - paddingLeft - paddingRight - mReachHeight) / 2
        mRectf = RectF(0f, 0f, (mRadius * 2).toFloat(), (mRadius * 2).toFloat())
        setMeasuredDimension(widthMeasureSpec, heightMeasureSpec)
    }

    @Synchronized
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.save()
        // mMaxPaintWidth / 加上这个只是为了能让已完成的bar完整显示出来
        canvas.translate(
            paddingLeft + mReachHeight / 2.toFloat(),
            paddingTop + mReachHeight / 2.toFloat()
        )
        mPaint.style = Paint.Style.STROKE
        mPaint.color = roundColor
        mPaint.strokeWidth = mReachHeight * 2 / 3.toFloat()
        //从圆点开始画圆
        canvas.drawCircle(mRadius.toFloat(), mRadius.toFloat(), mRadius.toFloat(), mPaint)
        canvas.restore()
        canvas.save()
        // 加上这个只是为了能让已完成的bar完整显示出来
        canvas.translate(
            paddingLeft + mReachHeight / 2.toFloat(),
            paddingTop + mReachHeight / 2.toFloat()
        )
        mPaint.style = Paint.Style.STROKE
        mPaint.strokeCap = Paint.Cap.ROUND
        mPaint.color = roundProgressColor
        mPaint.strokeWidth = mReachHeight.toFloat()
        val sweepAngle = progress * 1.0f / max * 360
        canvas.drawArc(mRectf!!, -90f, sweepAngle, false, mPaint)
        canvas.restore()
    }

    init {
        val mTypedArray = getContext().obtainStyledAttributes(
            attrs,
            R.styleable.MyProgressBar
        )
        //获取自定义属性和默认值
        roundColor = mTypedArray.getColor(
            R.styleable.MyProgressBar_round_color,
            Color.parseColor("#4F868686")
        )
        roundProgressColor = mTypedArray.getColor(
            R.styleable.MyProgressBar_round_progresscolor,
            Color.parseColor("#9d735EFD")
        )
        mRadius = mTypedArray.getDimension(
            R.styleable.MyProgressBar_radius,
            dip2px(context!!, 10).toFloat()
        ).toInt()
        mReachHeight = mTypedArray.getDimension(
            R.styleable.MyProgressBar_reach_height,
            dip2px(context, 10).toFloat()
        ).toInt()
        mTypedArray.recycle()
    }
}