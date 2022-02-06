package com.deepblue.cleaning.cleanview;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.PointF;
import android.graphics.RectF;

import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;

import com.deepblue.cleaning.R;


/**
 * 多点触控加Matrix类实现图片的旋转、缩放、平移
 */

public class TransformativeImageView extends AppCompatImageView {
    private static final String TAG = TransformativeImageView.class.getSimpleName();
    private static final float MAX_SCALE_FACTOR = 2.0f; // 默认最大缩放比例为2
    private static final float UNSPECIFIED_SCALE_FACTOR = -1f; // 未指定缩放比例
    private static final float MIN_SCALE_FACTOR = 1.0f; // 默认最小缩放比例为0.3
    private static final float INIT_SCALE_FACTOR = 1.2f; // 默认适应控件大小后的初始化缩放比例
    private static final int DEFAULT_REVERT_DURATION = 300;

    private int mRevertDuration = DEFAULT_REVERT_DURATION; // 回弹动画时间
    private float mMaxScaleFactor = MAX_SCALE_FACTOR; // 最大缩放比例
    private float mMinScaleFactor = UNSPECIFIED_SCALE_FACTOR; // 此最小缩放比例优先级高于下面两个
    private float mVerticalMinScaleFactor = MIN_SCALE_FACTOR; // 图片最初的最小缩放比例
    private float mHorizontalMinScaleFactor = MIN_SCALE_FACTOR; // 图片旋转90（或-90）度后的的最小缩放比例
    protected Matrix mMatrix = new Matrix(); // 用于图片旋转、平移、缩放的矩阵
    protected RectF mImageRect = new RectF(); // 保存图片所在区域矩形，坐标为相对于本View的坐标

    private Paint mPaint;
    private TransBack transBack;

    private float dxa;
    private float dya;

    private float dx2;
    private float dy2;

    float olddreeg;
    float degreeo;

    private long mFirstClick;
    private long mLastClick;
    private int MAX_LONG_PRESS_TIME = 350;
    private float oldScaleFacto = 1;
    private ActionListener mActionListener;

    public interface ActionListener {
        void down();

        void up();
    }

    public void setActionListener(ActionListener mActionListener) {
        this.mActionListener = mActionListener;
    }

    public TransformativeImageView(Context context) {
        this(context, null);
    }

    public TransformativeImageView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TransformativeImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        obtainAttrs(attrs);
        init();

    }

    private void obtainAttrs(AttributeSet attrs) {
        if (attrs == null) return;

        TypedArray typedArray = getContext()
                .obtainStyledAttributes(attrs, R.styleable.TransformativeImageView);
        mMaxScaleFactor = typedArray.getFloat(
                R.styleable.TransformativeImageView_max_scale, MAX_SCALE_FACTOR);
        mMinScaleFactor = typedArray.getFloat(
                R.styleable.TransformativeImageView_min_scale, UNSPECIFIED_SCALE_FACTOR);
        mRevertDuration = typedArray.getInteger(
                R.styleable.TransformativeImageView_revert_duration, DEFAULT_REVERT_DURATION);
        mScaleBy = typedArray.getInt(
                R.styleable.TransformativeImageView_scale_center, SCALE_BY_IMAGE_CENTER);
        typedArray.recycle();
    }

    private void init() {
        setScaleType(ImageView.ScaleType.MATRIX);
        mRevertAnimator.setDuration(mRevertDuration);

        mPaint = new Paint();
        //设置画笔颜色
        mPaint.setColor(Color.RED);
        //设置画笔模式
        mPaint.setStyle(Paint.Style.FILL);
        //设置画笔宽度为30px
        mPaint.setStrokeWidth(30f);

    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        initImgPositionAndSize();
    }

    /**
     * 初始化图片位置和大小
     */
    private void initImgPositionAndSize() {
        mMatrix.reset();
        // 初始化ImageRect
        refreshImageRect();

        // 计算缩放比例，使图片适应控件大小
        mHorizontalMinScaleFactor = Math.min(getWidth() / mImageRect.width(),
                getHeight() / mImageRect.height());
        mVerticalMinScaleFactor = Math.min(getHeight() / mImageRect.width(),
                getWidth() / mImageRect.height());

        float scaleFactor = mHorizontalMinScaleFactor;

        // 初始图片缩放比例比最小缩放比例稍大
        scaleFactor *= INIT_SCALE_FACTOR;
        mScaleFactor = scaleFactor;

        mMatrix.postScale(scaleFactor, scaleFactor, mImageRect.centerX(), mImageRect.centerY());
        refreshImageRect();
        // 移动图片到中心
        mMatrix.postTranslate((getRight() - getLeft()) / 2 - mImageRect.centerX(),
                (getBottom() - getTop()) / 2 - mImageRect.centerY());
        applyMatrix();
        mMatrix.getValues(mToMatrixValue);/*设置矩阵动画结束值*/
        // 如果用户有指定最小缩放比例则使用用户指定的
        if (mMinScaleFactor != UNSPECIFIED_SCALE_FACTOR) {
            mHorizontalMinScaleFactor = mMinScaleFactor;
            mVerticalMinScaleFactor = mMinScaleFactor;
        }
    }

    private PaintFlagsDrawFilter mDrawFilter =
            new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.setDrawFilter(mDrawFilter);
        super.onDraw(canvas);
    }

    private PointF mLastPoint1 = new PointF(); // 上次事件的第一个触点
    private PointF mLastPoint2 = new PointF(); // 上次事件的第二个触点
    private PointF mCurrentPoint1 = new PointF(); // 本次事件的第一个触点
    private PointF mCurrentPoint2 = new PointF(); // 本次事件的第二个触点


    private float pointDistance;
    private float oldDistance;

    private float mScaleFactor = 1.0f; // 当前的缩放倍数
    private boolean mCanScale = false; // 是否可以缩放

    protected PointF mLastMidPoint = new PointF(); // 图片平移时记录上一次ACTION_MOVE的点
    private PointF mCurrentMidPoint = new PointF(); // 当前各触点的中点
    protected boolean mCanDrag = false; // 是否可以平移

    private PointF mLastVector = new PointF(); // 记录上一次触摸事件两指所表示的向量
    private PointF mCurrentVector = new PointF(); // 记录当前触摸事件两指所表示的向量
    private boolean mCanRotate = false; // 判断是否可以旋转

    private MatrixRevertAnimator mRevertAnimator = new MatrixRevertAnimator(); // 回弹动画
    private float[] mFromMatrixValue = new float[9]; // 动画初始时矩阵值
    private float[] mToMatrixValue = new float[9]; // 动画终结时矩阵值

    protected boolean isTransforming = false; // 图片是否正在变化

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        PointF midPoint = getMidPointOfFinger(event);
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                mLastClick = System.currentTimeMillis();
                if (mLastClick - mFirstClick < MAX_LONG_PRESS_TIME) {
                    reset();
                    mFirstClick = 0;
                }
                mFirstClick = mLastClick;


            case MotionEvent.ACTION_POINTER_DOWN:
                // 每次触摸事件开始都初始化mLastMidPonit
                mLastMidPoint.set(midPoint);
                isTransforming = false;
                mRevertAnimator.cancel();
                // 新手指落下则需要重新判断是否可以对图片进行变换
                mCanRotate = false;
                mCanScale = false;
                mCanDrag = false;
                if (event.getPointerCount() == 2) {
                    // 旋转、平移、缩放分别使用三个判断变量，避免后期某个操作执行条件改变
                    mCanScale = true;
                    mLastPoint1.set(event.getX(0), event.getY(0));
                    mLastPoint2.set(event.getX(1), event.getY(1));
                    pointDistance = distance(mLastPoint1, mLastPoint2);
                    mCanRotate = true;
                    mLastVector.set(event.getX(1) - event.getX(0),
                            event.getY(1) - event.getY(0));
                } else if (event.getPointerCount() == 1) {
                    mCanDrag = true;
                }
                dx2 = 0;
                dy2 = 0;
                if (mActionListener != null) mActionListener.down();
                break;
            case MotionEvent.ACTION_MOVE:
                if (mCanDrag) translate(midPoint);
                if (mCanScale) scale(event);
                if (mCanRotate) rotate(event);
                // 判断图片是否发生了变换
                if (!getImageMatrix().equals(mMatrix)) isTransforming = true;
                if (mCanDrag || mCanScale || mCanRotate) applyMatrix();
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                float dd = 0.0f;
                if (pointDistance != 0) {
                    dd = oldDistance / pointDistance;
                }
                // 计算缩放比例
                if (transBack != null) {
                    transBack.back(dx2, dy2, dd, olddreeg);
                }
            case MotionEvent.ACTION_POINTER_UP:
                mCanScale = false;
                mCanDrag = false;
                mCanRotate = false;
                if (mActionListener != null) mActionListener.up();
                break;
        }
        super.onTouchEvent(event);
        return true;
    }

    private void rotate(MotionEvent event) {
        // 计算当前两指触点所表示的向量
        mCurrentVector.set(event.getX(1) - event.getX(0),
                event.getY(1) - event.getY(0));
        // 获取旋转角度
        degreeo = getRotateDegree(mLastVector, mCurrentVector);
        olddreeg = olddreeg + degreeo;
        mMatrix.postRotate(degreeo, mImageRect.centerX(), mImageRect.centerY());
        mLastVector.set(mCurrentVector);
    }

    /**
     * 使用Math#atan2(double y, double x)方法求上次触摸事件两指所示向量与x轴的夹角，
     * 再求出本次触摸事件两指所示向量与x轴夹角，最后求出两角之差即为图片需要转过的角度
     *
     * @param lastVector    上次触摸事件两指间连线所表示的向量
     * @param currentVector 本次触摸事件两指间连线所表示的向量
     * @return 两向量夹角，单位“度”，顺时针旋转时为正数，逆时针旋转时返回负数
     */
    private float getRotateDegree(PointF lastVector, PointF currentVector) {
        //上次触摸事件向量与x轴夹角
        double lastRad = Math.atan2(lastVector.y, lastVector.x);
        //当前触摸事件向量与x轴夹角
        double currentRad = Math.atan2(currentVector.y, currentVector.x);
        // 两向量与x轴夹角之差即为需要旋转的角度
        double rad = currentRad - lastRad;
        //“弧度”转“度”
        return (float) Math.toDegrees(rad);
    }

    protected void translate(PointF midPoint) {
        dxa = midPoint.x - mLastMidPoint.x;
        dya = midPoint.y - mLastMidPoint.y;
        mMatrix.postTranslate(dxa, dya);
        mLastMidPoint.set(midPoint);
        dx2 = dx2 + dxa;
        dy2 = dy2 + dya;
    }

    /**
     * 计算所有触点的中点
     *
     * @param event 当前触摸事件
     * @return 本次触摸事件所有触点的中点
     */
    private PointF getMidPointOfFinger(MotionEvent event) {
        // 初始化mCurrentMidPoint
        mCurrentMidPoint.set(0f, 0f);
        int pointerCount = event.getPointerCount();
        for (int i = 0; i < pointerCount; i++) {
            mCurrentMidPoint.x += event.getX(i);
            mCurrentMidPoint.y += event.getY(i);
        }
        mCurrentMidPoint.x /= pointerCount;
        mCurrentMidPoint.y /= pointerCount;
        return mCurrentMidPoint;
    }

    private static final int SCALE_BY_IMAGE_CENTER = 0; // 以图片中心为缩放中心
    private static final int SCALE_BY_FINGER_MID_POINT = 1; // 以所有手指的中点为缩放中心
    private int mScaleBy = SCALE_BY_FINGER_MID_POINT;
    private PointF scaleCenter = new PointF();

    /**
     * 获取图片的缩放中心，该属性可在外部设置，或通过xml文件设置
     * 默认中心点为图片中心
     *
     * @return 图片的缩放中心点
     */
    private PointF getScaleCenter() {
        // 使用全局变量避免频繁创建变量
        switch (mScaleBy) {
            case SCALE_BY_IMAGE_CENTER:
                scaleCenter.set(mImageRect.centerX(), mImageRect.centerY());
                break;
            case SCALE_BY_FINGER_MID_POINT:
                scaleCenter.set(mLastMidPoint.x, mLastMidPoint.y);
                break;
        }
        return scaleCenter;
    }

    private void scale(MotionEvent event) {
        PointF scaleCenter = getScaleCenter();

        // 初始化当前两指触点
        mCurrentPoint1.set(event.getX(0), event.getY(0));
        mCurrentPoint2.set(event.getX(1), event.getY(1));
        // 计算缩放比例
        float scaleFactor = distance(mCurrentPoint1, mCurrentPoint2)
                / distance(mLastPoint1, mLastPoint2);
        float dd = oldScaleFacto * scaleFactor;
        if (dd > 0.5) {
            oldScaleFacto = dd;
            oldDistance = distance(mCurrentPoint1, mCurrentPoint2);
            // 更新当前图片的缩放比例
            mScaleFactor *= scaleFactor;
            mMatrix.postScale(scaleFactor, scaleFactor,
                    scaleCenter.x, scaleCenter.y);
            mLastPoint1.set(mCurrentPoint1);
            mLastPoint2.set(mCurrentPoint2);
        }
    }

    /**
     * 获取两点间距离
     */
    private float distance(PointF point1, PointF point2) {
        float dx = point2.x - point1.x;
        float dy = point2.y - point1.y;
        return (float) Math.sqrt(dx * dx + dy * dy);
    }

    private float[] xAxis = new float[]{1f, 0f}; // 表示与x轴同方向的向量

    /**
     * 获取当前图片旋转角度
     *
     * @return 图片当前的旋转角度
     */
    private float getCurrentRotateDegree() {
        // 每次重置初始向量的值为与x轴同向
        xAxis[0] = 1f;
        xAxis[1] = 0f;
        // 初始向量通过矩阵变换后的向量
        mMatrix.mapVectors(xAxis);
        // 变换后向量与x轴夹角
        double rad = Math.atan2(xAxis[1], xAxis[0]);
        return (float) Math.toDegrees(rad);
    }

    private static final int HORIZONTAL = 0;
    private static final int VERTICAL = 1;

    /**
     * 更新图片所在区域，并将矩阵应用到图片
     */
    protected void applyMatrix() {
        refreshImageRect(); /*将矩阵映射到ImageRect*/
        setImageMatrix(mMatrix);
    }

    /**
     * 图片使用矩阵变换后，刷新图片所对应的mImageRect所指示的区域
     */
    private void refreshImageRect() {

        if (getDrawable() != null) {
            mImageRect.set(getDrawable().getBounds());
            mMatrix.mapRect(mImageRect, mImageRect);
        }

    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mRevertAnimator.cancel();
    }

    //-----Aninmator-------------------

    /**
     * 图片回弹动画
     */
    private class MatrixRevertAnimator extends ValueAnimator
            implements ValueAnimator.AnimatorUpdateListener {

        private float[] mFromMatrixValue; // 动画初始时矩阵值
        private float[] mToMatrixValue; // 动画终结时矩阵值
        private float[] mInterpolateMatrixValue; // 动画执行过程中矩阵值

        MatrixRevertAnimator() {
            mInterpolateMatrixValue = new float[9];
            setFloatValues(0f, 1f);
            addUpdateListener(this);
        }

        void setMatrixValue(float[] fromMatrixValue, final float[] toMatrixValue) {
            mFromMatrixValue = fromMatrixValue;
            mToMatrixValue = toMatrixValue;

            addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mMatrix.setValues(toMatrixValue);
                    applyMatrix();
                }
            });
        }

        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            if (mFromMatrixValue != null
                    && mToMatrixValue != null && mInterpolateMatrixValue != null) {
                // 根据动画当前进度设置矩阵的值
                for (int i = 0; i < 9; i++) {
                    float animatedValue = (float) animation.getAnimatedValue();
                    mInterpolateMatrixValue[i] = mFromMatrixValue[i]
                            + (mToMatrixValue[i] - mFromMatrixValue[i]) * animatedValue;
                }
                mMatrix.setValues(mInterpolateMatrixValue);
                applyMatrix();
            }
        }
    }

    public void setTransBack(TransBack transBack) {
        this.transBack = transBack;
    }

    public interface TransBack {
        void back(float transX, float transY, float scale, float degree);
    }

    public void reset() {
        mMatrix.getValues(mFromMatrixValue);/*设置矩阵动画初始值*/
        mRevertAnimator.setMatrixValue(mFromMatrixValue, mToMatrixValue);
        mRevertAnimator.cancel();
        mRevertAnimator.start();
        oldScaleFacto = 1;
    }

    public void trans() {
        PointF scaleCenter = getScaleCenter();


        // 计算缩放比例
        float scaleFactor = 0.8f;

        oldDistance = distance(mCurrentPoint1, mCurrentPoint2);
        // 更新当前图片的缩放比例

        mScaleFactor *= scaleFactor;

        mMatrix.postScale(scaleFactor, scaleFactor,
                scaleCenter.x, scaleCenter.y);

    }
}
