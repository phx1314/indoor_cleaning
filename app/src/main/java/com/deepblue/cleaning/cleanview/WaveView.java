package com.deepblue.cleaning.cleanview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Region;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.deepblue.cleaning.R;


/**
 * Created by lion on 2017/5/8.
 */


public class WaveView extends View implements Runnable {


    private static final int DEFAULT_WAVE_1_COLOR = 0xFF31B287;

    private float mAngle = 0;

    private boolean mIsRunning = false;


    private Paint mWavePaint1 = new Paint(Paint.ANTI_ALIAS_FLAG);

    private Path mContainerPath = new Path();


    private float mWaveSpeed = 5f;

    private float mWaveRange = 5;

    private int mWave1Color = DEFAULT_WAVE_1_COLOR;

    private float mStrokeWidth = 1;

    private float mWaveHeightPercent = 0.5f;

    private boolean mIsCircle = false;

    private float mPeriod;

    private RectF rect = new RectF();


    public WaveView(Context context) {

        super(context);

        initView(null);

    }


    public WaveView(Context context, @Nullable AttributeSet attrs) {

        super(context, attrs);

        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.WaveView);

        initView(array);

    }


    public WaveView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {

        super(context, attrs, defStyleAttr);

        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.WaveView,

                defStyleAttr, 0);

        initView(array);

    }


    @RequiresApi(21)

    public WaveView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {

        super(context, attrs, defStyleAttr, defStyleRes);

        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.WaveView,

                defStyleAttr, defStyleRes);

        initView(array);

    }


    private void initView(TypedArray array) {

        if (array != null) {

            mWaveSpeed = array.getFloat(R.styleable.WaveView_waveSpeed, 5f);

            mWaveRange = array.getDimension(R.styleable.WaveView_waveRange, 5);

            mWave1Color = array.getColor(R.styleable.WaveView_wave1Color,

                    DEFAULT_WAVE_1_COLOR);

            mStrokeWidth = array.getDimension(R.styleable.WaveView_waveStrokeWidth, 1);

            mWaveHeightPercent = array.getFloat(R.styleable.WaveView_waveHeightPercent, 0.5f);

            mIsCircle = array.getBoolean(R.styleable.WaveView_isCircle, true);

            mPeriod = array.getFloat(R.styleable.WaveView_period, 1.0f);

        } else {

            mWaveSpeed = 5f;

            mWaveRange = 5;

            mWave1Color = DEFAULT_WAVE_1_COLOR;

            mStrokeWidth = 1;

            mWaveHeightPercent = 0.5f;

            mIsCircle = false;

            mPeriod = 1.0f;

        }


        initPaint();

    }


    private void initPaint() {

        mWavePaint1.setStyle(Paint.Style.FILL);
        mWavePaint1.setStrokeCap(Paint.Cap.ROUND);//圆形
        mWavePaint1.setAntiAlias(true);
        setPaint();
        start();
    }


    private void setPaint() {

        mWavePaint1.setStrokeWidth(mStrokeWidth);

        mWavePaint1.setColor(mWave1Color);

    }


    @Override

    protected void onDraw(Canvas canvas) {

        super.onDraw(canvas);
        if (mIsRunning) {

            int height = getHeight();

            int width = getWidth();


            clipContainer(canvas, width, height);

            drawWave(canvas, width, height);

        }

    }


    private void clipContainer(Canvas canvas, int width, int height) {

        if (mIsCircle) {

            mContainerPath.reset();

            canvas.clipPath(mContainerPath);

//            mContainerPath.addCircle(width / 2, height / 2, width / 2, Path.Direction.CCW);
            rect.left = 0;
            rect.right = width;
            rect.top = 0;
            rect.bottom = height;
            float[] radii = new float[]{0, 0, 0, 0, width / 8, width / 8, width / 8, width / 8};
            mContainerPath.addRoundRect(rect,radii,Path.Direction.CCW);
            canvas.clipPath(mContainerPath, Region.Op.REPLACE);

        }

    }


    private void drawWave(Canvas canvas, int width, int height) {

        setPaint();

        double lineX = 0;

        double lineY1 = 0;

        for (int i = 0; i < width; i += mStrokeWidth) {
            lineX = i;
            if (mIsRunning) {
                lineY1 = mWaveRange * Math.sin((mAngle + i) * Math.PI / 180 / mPeriod) +
                        height * (1 - mWaveHeightPercent);
            } else {
                lineY1 = 0;

            }

            canvas.drawLine((int) lineX, (int) lineY1,

                    (int) lineX + 1, height, mWavePaint1);

        }

    }


    @Override

    public void run() {

        while (mIsRunning) {

            mAngle += mWaveSpeed;

            mHandler.post(new Runnable() {

                @Override

                public void run() {

                    invalidate();

                }

            });

            try {

                Thread.sleep(20);

            } catch (InterruptedException e) {

                e.printStackTrace();

            }

        }

    }


    private Handler mHandler = new Handler(Looper.getMainLooper());


    public void start() {

        if (mIsRunning) {

            return;

        }

        new Thread(this).start();

        mIsRunning = true;

    }


    public void stop() {

        mIsRunning = false;

        mAngle = 0;

    }


    public Path getContainerPath() {

        return mContainerPath;

    }


    public void setContainerPath(Path mContainerPath) {

        this.mContainerPath = mContainerPath;

    }


    public float getWaveSpeed() {

        return mWaveSpeed;

    }


    public void setWaveSpeed(float mWaveSpeed) {

        this.mWaveSpeed = mWaveSpeed;

    }


    public float getWaveRange() {

        return mWaveRange;

    }


    public void setWaveRange(float mWaveRange) {

        this.mWaveRange = mWaveRange;

    }


    public int getWave1Color() {

        return mWave1Color;

    }


    public void setWave1Color(int mWave1Color) {

        this.mWave1Color = mWave1Color;

    }


    public float getStrokeWidth() {

        return mStrokeWidth;

    }


    public void setStrokeWidth(float mStrokeWidth) {

        this.mStrokeWidth = mStrokeWidth;

    }


    public float getWaveHeightPercent() {

        return mWaveHeightPercent;

    }


    public void setWaveHeightPercent(float mWaveHeightPercent) {

        this.mWaveHeightPercent = mWaveHeightPercent;

    }


    public boolean isCircle() {

        return mIsCircle;

    }


    public void setIsCircle(boolean mIsCircle) {

        this.mIsCircle = mIsCircle;

    }

}
