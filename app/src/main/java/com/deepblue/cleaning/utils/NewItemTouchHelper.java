package com.deepblue.cleaning.utils;

import android.content.Context;
import android.graphics.Color;
import android.os.Vibrator;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.deepblue.cleaning.adapter.RecycleAdapterTask;
import com.deepblue.cleaning.bean.Task;
import com.deepblue.cleaning.cleanview.TransformativeImageView;

import java.util.Collections;
import java.util.List;

public class NewItemTouchHelper extends ItemTouchHelper.Callback {
    private RecycleAdapterTask adapter;
    private List<Task> results;
    private Context mActivity;
    private final Vibrator mVibrator;
    private TransformativeImageView.ActionListener mItemTouchActionListener;

    public NewItemTouchHelper(Context activity, RecycleAdapterTask adapter, List<Task> list) {
        super();
        this.adapter = adapter;
        this.results = list;
        this.mActivity = activity;
        this.mItemTouchActionListener = (TransformativeImageView.ActionListener) activity;
        mVibrator = (Vibrator) mActivity.getSystemService(Context.VIBRATOR_SERVICE);//震动
    }

    public void updateList(List<Task> list) {
        this.results = list;
    }

    @Override
    public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        int dragFlags;//GridLayoutManager可拖动的方向分为上 下 左 右 LinearLayoutManager可拖动的方向分为上 下
        int swipFlags;
        if (recyclerView.getLayoutManager() instanceof GridLayoutManager) {
            dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN | ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT;
            swipFlags = 0;
        } else {
            dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
            swipFlags = 0;
        }
        return makeMovementFlags(dragFlags, swipFlags);
    }

    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
        int fromPosition = viewHolder.getAdapterPosition();//得到拖动ViewHolder的position
        int toPosition = target.getAdapterPosition();//得到目标ViewHolder的position
//        if (fromPosition < toPosition) {
//            for (int i = fromPosition; i < toPosition; i++) {
//                Collections.swap(results, i, i + 1);
//            }
//        } else {
//            for (int i = fromPosition; i > toPosition; i--) {
//                Collections.swap(results, i, i - 1);
//            }
//        }
        Collections.swap(results, fromPosition, toPosition);
        adapter.notifyItemMoved(fromPosition, toPosition);
        return true;
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {

    }


    @Override
    public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
        if (actionState != ItemTouchHelper.ACTION_STATE_IDLE) {
            mVibrator.vibrate(70);
            viewHolder.itemView.setBackgroundColor(Color.parseColor("#499CFC"));
            mItemTouchActionListener.down();
        }
        super.onSelectedChanged(viewHolder, actionState);
    }

    @Override
    public void clearView(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        viewHolder.itemView.setBackgroundColor(0);
        mItemTouchActionListener.up();
        super.clearView(recyclerView, viewHolder);
    }

}
