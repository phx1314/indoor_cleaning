package com.deepblue.cleaning.cleanview;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;

import com.deepblue.cleaning.R;

public class CustomPopupWindow extends PopupWindow implements View.OnClickListener {

    public static final int CLEAN_FLOOR_MODE = 0;

    public static final int WIND_MODE = 1;

    private Context context;
    private RelativeLayout mildIntensityRl;
    private RelativeLayout standardIntensityRl;
    private RelativeLayout strongIntensityRl;
    private int currentMode = CLEAN_FLOOR_MODE;
    private PopBackListener mPopBackListener;

    public CustomPopupWindow(Context context, int currentMode) {
        this.context = context;
        this.currentMode = currentMode;
        initView(context);
    }

    public void initStatus(int status) {
        if (status == 1) {
            mildIntensityRl.setBackgroundColor(context.getResources().getColor(R.color.blue));
            standardIntensityRl.setBackgroundColor(context.getResources().getColor(R.color.transparent));
            if (strongIntensityRl != null)
                strongIntensityRl.setBackgroundColor(context.getResources().getColor(R.color.transparent));
        } else if (status == 2) {
            mildIntensityRl.setBackgroundColor(context.getResources().getColor(R.color.transparent));
            standardIntensityRl.setBackgroundColor(context.getResources().getColor(R.color.blue));
            if (strongIntensityRl != null)
                strongIntensityRl.setBackgroundColor(context.getResources().getColor(R.color.transparent));
        } else {
            if (strongIntensityRl != null)
                strongIntensityRl.setBackgroundColor(context.getResources().getColor(R.color.blue));
        }
    }

    public void setPopBackListener(PopBackListener listener) {
        this.mPopBackListener = listener;
    }

    private void initView(Context context) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View mView;
        if (currentMode == CLEAN_FLOOR_MODE) {
            mView = inflater.inflate(R.layout.popup_cleaning_intensity, null);
            mildIntensityRl = mView.findViewById(R.id.mild_intensity_rl);
            standardIntensityRl = mView.findViewById(R.id.standard_intensity_rl);
            strongIntensityRl = mView.findViewById(R.id.strong_intensity_rl);
            strongIntensityRl.setOnClickListener(this);
        } else {
            mView = inflater.inflate(R.layout.popup_wind_intensity, null);
            mildIntensityRl = mView.findViewById(R.id.mild_wind_intensity_rl);
            standardIntensityRl = mView.findViewById(R.id.standard_wind_intensity_rl);
        }
        mildIntensityRl.setOnClickListener(this);
        standardIntensityRl.setOnClickListener(this);
        setContentView(mView);
        setOutsideTouchable(true);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.mild_intensity_rl:
            case R.id.mild_wind_intensity_rl:
                mildIntensityRl.setBackgroundColor(context.getResources().getColor(R.color.blue));
                standardIntensityRl.setBackgroundColor(context.getResources().getColor(R.color.transparent));
                if (currentMode == CLEAN_FLOOR_MODE) {
                    strongIntensityRl.setBackgroundColor(context.getResources().getColor(R.color.transparent));
                }
                if (mPopBackListener != null) {
                    mPopBackListener.backStatus(1);
                }
                dismiss();
                break;
            case R.id.standard_intensity_rl:
            case R.id.standard_wind_intensity_rl:
                mildIntensityRl.setBackgroundColor(context.getResources().getColor(R.color.transparent));
                standardIntensityRl.setBackgroundColor(context.getResources().getColor(R.color.blue));
                if (currentMode == CLEAN_FLOOR_MODE) {
                    strongIntensityRl.setBackgroundColor(context.getResources().getColor(R.color.transparent));
                }
                if (mPopBackListener != null) {
                    mPopBackListener.backStatus(2);
                }
                dismiss();
                break;
            case R.id.strong_intensity_rl:
                mildIntensityRl.setBackgroundColor(context.getResources().getColor(R.color.transparent));
                standardIntensityRl.setBackgroundColor(context.getResources().getColor(R.color.transparent));
                strongIntensityRl.setBackgroundColor(context.getResources().getColor(R.color.blue));
                if (mPopBackListener != null) {
                    mPopBackListener.backStatus(3);
                }
                dismiss();
                break;
            default:
                break;
        }
    }

    public interface PopBackListener {
        void backStatus(int status);

    }
}
