package com.giz.utils;

import android.graphics.Canvas;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.ViewGroup;

public class SliderTouchHelperCallback extends ItemTouchHelper.Callback {
    /**
     * 定义列表卡片侧滑显示定位图标的回调函数
     */
    @Override
    public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
        return makeMovementFlags(ItemTouchHelper.ACTION_STATE_IDLE ,ItemTouchHelper.LEFT);
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder viewHolder1) {
        return false;
    }

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
        //Log.d("STHC", "onSwipe");
    }

    @Override
    public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView,
                            @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY,
                            int actionState, boolean isCurrentlyActive) {
        //Log.d("STHC", "OnChildDraw");
        if(actionState == ItemTouchHelper.ACTION_STATE_SWIPE){
            if(Math.abs(dX) < blockWidth(viewHolder)){
                viewHolder.itemView.scrollTo(-(int)dX, 0);
            }else{
                viewHolder.itemView.scrollTo(blockWidth(viewHolder), 0);
            }
        }else{
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
        }
    }

    private int blockWidth(RecyclerView.ViewHolder viewHolder){
        return ((ViewGroup)viewHolder.itemView).getChildAt(1).getLayoutParams().width;
    }

    @Override
    public void clearView(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
        //Log.d("STHC", "clearView");
        super.clearView(recyclerView, viewHolder);
    }
}
