package com.giz.customize;

import android.content.Context;
import android.support.design.widget.FloatingActionButton;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

public class DraggableFloatingActionButton extends FloatingActionButton implements View.OnTouchListener {

    private int mScreenWidth, mScreenHeight;
    private int lastX, lastY, originX, originY;

    public DraggableFloatingActionButton(Context context) {
        this(context, null);
    }

    public DraggableFloatingActionButton(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DraggableFloatingActionButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        DisplayMetrics metrics = new DisplayMetrics();
        ((WindowManager)context.getSystemService(Context.WINDOW_SERVICE)).
                getDefaultDisplay().getMetrics(metrics);
        mScreenHeight = (int)(metrics.heightPixels);
//        Log.d("DFAB", String.valueOf(mScreenHeight));
        mScreenWidth = (int)(metrics.widthPixels);
//        Log.d("DFAB", String.valueOf(mScreenWidth));
        setOnTouchListener(this);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                lastX = (int)event.getRawX();
                lastY = (int)event.getRawY();
//                Log.d("DFAB", String.valueOf(lastX) + " " + String.valueOf(lastY));
                originX = lastX;
                originY = lastY;
                break;
            case MotionEvent.ACTION_MOVE:
//                Log.d("DFAB", String.valueOf(event.getRawX()) + " " + String.valueOf(event.getRawY()));
                int dx = (int) event.getRawX() - lastX;
                int dy = (int) event.getRawY() - lastY;
                int l = v.getLeft() + dx;
                int b = v.getBottom() + dy;
                int r = v.getRight() + dx;
                int t = v.getTop() + dy;
                // 下面判断移动是否超出屏幕
                if (l < 0) {
                    l = 0;
                    r = l + v.getWidth();
                }
                if (t < 0) {
                    t = 0;
                    b = t + v.getHeight();
                }
                if (r > mScreenWidth) {
                    r = mScreenWidth;
                    l = r - v.getWidth();
                }
                if (b > mScreenHeight) {
                    b = mScreenHeight;
                    t = b - v.getHeight();
                }
//                Log.d("DFAB", String.format("%d %d %d %d", l, t, r, b));
                v.layout(l, t, r, b);
                lastX = (int) event.getRawX();
                lastY = (int) event.getRawY();
                v.postInvalidate();
                break;
            case MotionEvent.ACTION_UP:
                int distance = (int)event.getRawX() - originX + (int)event.getRawY() - originY;
                if(Math.abs(distance) > 20){
                    return true;
                }
                break;
        }
        return false;
    }
}
