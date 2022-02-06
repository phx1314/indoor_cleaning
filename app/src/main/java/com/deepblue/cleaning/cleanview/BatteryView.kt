package com.deepblue.cleaning.cleanview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import com.deepblue.cleaning.R
import okio.JvmField

/**
 * 电池
 * @author caojun
 * @date 2019/04/04
 */
class BatteryView(context: Context, attrs: AttributeSet) : View(context, attrs) {

    private var power: Int = 0

    fun setPower(power: Int){
        setPowers(power,10,20)
    }


    fun setPowers(power: Int, lowPower: Int, highPower: Int) {
        this.power = when {
                power <= 0 -> 1
                power > 100 -> 100
                else -> power
            }
        this.lowPower = lowPower
        this.highPower = highPower
    }

    private var mWidth: Int = 0
    private var mHeight: Int = 0
    private val isHorizontal: Boolean
    private val colorLow: Int
    private val colorMiddle: Int
    private val colorHigh: Int
    private val colorFrame: Int
    private var lowPower: Int = 10
    private var highPower: Int = 20
    var isCharging = false
        set(value) {
            field = value
            invalidate()
        }

    private var strokeWidth: Float = 0.toFloat()
    private var halfStrokeWidth: Float = 0.toFloat()
    private var headWH: Float = 0.toFloat()
    private var round: Float = 0.toFloat()
    companion object {
        private const val HeadX = 0.3f
        private const val BlankWidth = 2
    }

    init {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.Battery)
        colorLow = typedArray.getColor(R.styleable.Battery_lowColor, 0xFF0000)
        colorMiddle = typedArray.getColor(R.styleable.Battery_middleColor, 0xFFA042)
        colorHigh = typedArray.getColor(R.styleable.Battery_highColor, 0x00FF00)
        colorFrame = typedArray.getColor(R.styleable.Battery_frameColor, 0xFFFFFF)
        power = typedArray.getInt(R.styleable.Battery_power, 100)
        lowPower = typedArray.getInt(R.styleable.Battery_lowPower, 10)
        highPower = typedArray.getInt(R.styleable.Battery_highPower, 20)

        val orientation = typedArray.getString(R.styleable.Battery_android_orientation)
        isHorizontal = orientation?:"0" == "0"

        typedArray.recycle()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        //对View上的內容进行测量后得到的View內容占据的宽度
        mWidth = measuredWidth
        //对View上的內容进行测量后得到的View內容占据的高度
        mHeight = measuredHeight

        strokeWidth = mWidth / if (isHorizontal) 20F else 10F
        halfStrokeWidth = strokeWidth / 2
        headWH = strokeWidth
        round = strokeWidth / 2
    }

    override fun onDraw(canvas: Canvas) {

        if (isHorizontal) {
            drawHorizontalBattery(canvas)
        } else {
            drawVerticalBattery(canvas)
        }
    }

    private fun setPowerColor(paint: Paint) {
        when {
            power < lowPower -> paint.color = colorLow
            power < highPower -> paint.color = colorMiddle
            else -> paint.color = colorHigh
        }
    }

    /**
     * 绘制水平电池
     *
     * @param canvas
     */
    private fun drawHorizontalBattery(canvas: Canvas) {

        val paint =Paint(Paint.ANTI_ALIAS_FLAG) //消除锯
        paint.setAntiAlias(true)
        paint.strokeCap = Paint.Cap.ROUND
        //电量
        paint.strokeWidth = 0f
        paint.style = Paint.Style.FILL //画电池内矩形电量
        val max = mWidth.toFloat() - strokeWidth * 2 - headWH
        val offset = max * power / 100f
        setPowerColor(paint)
        val left = strokeWidth + BlankWidth
        val right = strokeWidth + max - BlankWidth
        val top = left
        val bottom = mHeight - strokeWidth - BlankWidth
        canvas.drawRect(left, top, strokeWidth + offset - BlankWidth, bottom, paint)

        //外框
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = strokeWidth
        val r1 =
            RectF(halfStrokeWidth, halfStrokeWidth, mWidth.toFloat() - strokeWidth - halfStrokeWidth, mHeight - halfStrokeWidth)
        paint.color = colorFrame
        canvas.drawRoundRect(r1, round, round, paint)

        //电池头
        val r3 = RectF(mWidth - headWH, mHeight * HeadX, mWidth.toFloat(), mHeight * (1 - HeadX))
        paint.color = colorFrame
        canvas.drawRect(r3, paint)

        //充电
        if (isCharging) {
            paint.style = Paint.Style.FILL
            val path = Path()
            val pw = right - left
            val ph = bottom - top
            path.moveTo(left + pw / 7, top + ph / 2)
            path.lineTo(left + pw / 2, top + ph / 4)
            path.lineTo(left + pw * 4 / 7,top + ph / 2)
            path.lineTo(left + pw * 6 / 7,top + ph / 2)
            path.lineTo(left + pw / 2,top + ph * 3 / 4)
            path.lineTo(left + pw * 3 / 7,top + ph / 2)
//            path.lineTo(left + pw / 7, top + ph / 2)
            canvas.drawPath(path, paint)
        }
    }

    /**
     * 绘制垂直电池
     *
     * @param canvas
     */
    private fun drawVerticalBattery(canvas: Canvas) {
        val paint = Paint()

        //电量
        val max = mHeight.toFloat() - headWH - strokeWidth * 2
        val topOffset = (mHeight.toFloat() - headWH - strokeWidth * 2) * power / 100.0f
        paint.style = Paint.Style.FILL
        setPowerColor(paint)
        val left = strokeWidth + BlankWidth
        val right = mWidth - strokeWidth - BlankWidth
        val top = mHeight.toFloat() - strokeWidth - max + BlankWidth
        val bottom = mHeight - strokeWidth - BlankWidth
        canvas.drawRect(
            left,
            mHeight.toFloat() - strokeWidth - topOffset + BlankWidth,
            right,
            bottom,
            paint
        )

        //边框
        paint.color = colorFrame
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = strokeWidth
        val rect = RectF(halfStrokeWidth, headWH + halfStrokeWidth, mWidth - halfStrokeWidth, mHeight - halfStrokeWidth)
        canvas.drawRoundRect(rect, round, round, paint)

        //电池头
        paint.style = Paint.Style.FILL
        val headRect = RectF(mWidth * HeadX, 0f, mWidth * (1 - HeadX), headWH)
        paint.color = colorFrame
        canvas.drawRect(headRect, paint)

        //充电
        if (isCharging) {
            paint.style = Paint.Style.FILL
            val path = Path()
            val pw = right - left
            val ph = bottom - top
            path.moveTo(left + pw / 2, top + ph / 7)
            path.lineTo(left + pw * 3 / 4, top + ph / 2)
            path.lineTo(left + pw / 2,top + ph * 4 / 7)
            path.lineTo(left + pw / 2,top + ph * 6 / 7)
            path.lineTo(left + pw / 4,top + ph / 2)
            path.lineTo(left + pw / 2,top + ph * 3 / 7)
//            path.lineTo(left + pw / 2, top + ph / 7)
            canvas.drawPath(path, paint)
        }
    }
}