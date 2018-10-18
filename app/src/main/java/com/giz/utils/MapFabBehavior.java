package com.giz.utils;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;

public class MapFabBehavior extends FloatingActionButton.Behavior {

    public MapFabBehavior(Context context, AttributeSet attrs){
        super(context, attrs);
    }

    // 准备开始滚动
    @Override
    public boolean onStartNestedScroll(@NonNull CoordinatorLayout coordinatorLayout,
                                       @NonNull FloatingActionButton child, @NonNull View directTargetChild,
                                       @NonNull View target, int axes, int type) {
        // 确保FAB垂直滚动
        return axes == ViewCompat.SCROLL_AXIS_VERTICAL;
    }

    // 滚动时
    @Override
    public void onNestedScroll(@NonNull CoordinatorLayout coordinatorLayout, @NonNull FloatingActionButton child,
                               @NonNull View target, int dxConsumed, int dyConsumed, int dxUnconsumed,
                               int dyUnconsumed, int type) {
        super.onNestedScroll(coordinatorLayout, child, target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, type);
        if(dyConsumed > 0){ // 向下滚动
            animateOut(child);
        }else if(dyConsumed < 0){ // 向上滚动
            animateIn(child);
        }
    }

    // FAB移出屏幕动画（隐藏动画）
    private void animateOut(FloatingActionButton fab) {
        fab.animate().scaleX(0).scaleY(0).alpha(0).setInterpolator(new LinearInterpolator()).start();
    }

    // FAB移入屏幕动画（显示动画）
    private void animateIn(FloatingActionButton fab) {
        fab.animate().scaleX(1).scaleY(1).alpha(1).setInterpolator(new LinearInterpolator()).start();
    }
}
