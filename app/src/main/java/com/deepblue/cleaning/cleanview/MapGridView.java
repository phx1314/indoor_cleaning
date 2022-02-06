package com.deepblue.cleaning.cleanview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatImageView;

/**
 * 地图网格
 */
public class MapGridView extends AppCompatImageView {

    private double resolution;
    private float scale;
    private float space;//间隔
    private int spacing;//间距
    private Paint paint = new Paint();
    private String[] colors = new String[]{"#7F797979", "#797979"};

    public MapGridView(Context context) {
        this(context, null);
    }

    public MapGridView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MapGridView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        paint.setAntiAlias(true);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (spacing == 0) {
            return;
        }
        int sizeX = getWidth() / spacing;
        for (int i = 0;i <= sizeX;i ++) {
            setPaintColor(i);
            canvas.drawLine(i * spacing, 0, i * spacing, getHeight(), paint);
        }
        int sizeY = getHeight() / spacing;
        for (int j = 0;j <= sizeY;j ++) {
            setPaintColor(j);
            canvas.drawLine(0, j * spacing, getWidth(), j * spacing, paint);
        }
    }

    private void setPaintColor(int index) {
        if (index % 5 == 4) {
            paint.setColor(Color.parseColor(colors[1]));
        } else {
            paint.setColor(Color.parseColor(colors[0]));
        }
    }

    /**
     * 初始化
     * @param resolution
     * @param scale
     * @param space
     */
    public void init(double resolution, float scale, float space) {
        this.resolution = resolution;
        this.scale = scale;
        setSpace(space);
    }

    public void setSpace(float space) {
        this.space = space;

        invalidate();
    }

    public void setScale(float scale) {
        this.scale = scale;
        invalidate();
    }

    @Override
    public void invalidate() {
        if (resolution != 0) {
            spacing = (int) (space * scale / resolution);
        }
        super.invalidate();
    }
}
