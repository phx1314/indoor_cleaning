package com.deepblue.cleaning.cleanview;

import android.graphics.PointF;

import org.jetbrains.annotations.NotNull;

public interface Listener {

//    void onMatrix(@NotNull Matrix matrix);

    void onClick(@NotNull PointF pointF);

    void onLongClick(@NotNull PointF pointF);

    void onTouched(@NotNull PointF pointF);

    void onDoubleClick(@NotNull PointF pointF);

    void onMove(@NotNull PointF pointF);

    void onMoveOver(@NotNull PointF pointF);

    void onScaleChanged(float scale);
}
