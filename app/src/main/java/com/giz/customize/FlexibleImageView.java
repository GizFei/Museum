package com.giz.customize;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.drawable.BitmapDrawable;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.WindowManager;

public class FlexibleImageView extends AppCompatImageView {

    private static final int MODE_NONE = 0;
    private static final int MODE_DRAG = 1;
    private static final int MODE_ZOOM = 2;

    private Bitmap mBitmap;
    private Matrix mMatrix;
    private Matrix mMatrix1;
    private Matrix mSavedMatrix;

    private int screenWidth;            // 屏幕宽度
    private int screenHeight;           // 屏幕高度
    private int mode = MODE_NONE;
    private float x_down;
    private float y_down;
    private float oldDist = 1f;         // 原始距离
    private PointF start = new PointF(); // 起始点
    private PointF middle = new PointF(); // 中心点

    public FlexibleImageView(Context context) {
        this(context, null);
    }

    public FlexibleImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FlexibleImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mBitmap = ((BitmapDrawable)getDrawable()).getBitmap();
        DisplayMetrics dm = new DisplayMetrics();
        WindowManager wm = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
        if(wm != null){
            wm.getDefaultDisplay().getMetrics(dm);
            screenWidth = dm.widthPixels;
            screenHeight = dm.heightPixels;
        }else{
            screenWidth = 1080;
            screenHeight = 1920;
        }

        mMatrix = new Matrix();
        mMatrix1 = new Matrix();
        mSavedMatrix = new Matrix();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // 保存当前页面已有的图像
        canvas.save();
        // 绘制当前图片
        canvas.drawBitmap(mBitmap, mMatrix, null);
        canvas.restore();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                // 按下
                mode = MODE_DRAG;
                x_down = event.getX();
                y_down = event.getY();
                // 保存当前矩阵
                mSavedMatrix.set(mMatrix);
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                // 多个手指触摸
                mode = MODE_ZOOM;
                oldDist = spacing(event);
                mSavedMatrix.set(mMatrix);
                midPoint(middle, event);
                break;
            case MotionEvent.ACTION_MOVE:
                if(mode == MODE_ZOOM){
                    mMatrix1.set(mSavedMatrix);
                    float newDist = spacing(event);
                    float scale = newDist / oldDist;    // 放大倍数
                    mMatrix1.postScale(scale, scale, middle.x, middle.y);

                    if(checkMatrix()){
                        mMatrix.set(mMatrix1);
                        invalidate();
                    }
                }else if(mode == MODE_DRAG){
                    // 平移
                    mMatrix1.set(mSavedMatrix);
                    mMatrix1.postTranslate(event.getX() - x_down, event.getY() - y_down);

                    if(checkMatrix()){
                        mMatrix.set(mMatrix1);
                        invalidate();
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
                mode = MODE_NONE;
                break;
        }
        return true;
    }

    // 计算两个手指间的距离
    private float spacing(MotionEvent event){
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float)Math.hypot(x, y);
    }

    // 取手势中心点
    private void midPoint(PointF point, MotionEvent event){
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        point.set(x, y);
    }

    private boolean checkMatrix(){
        float[] f = new float[9];
        mMatrix1.getValues(f);
        // 图片四个点坐标
        float x1 = f[2];
        float y1 = f[5];
        float x2 = f[0] * mBitmap.getWidth() + f[2];
        float y2 = f[3] * mBitmap.getWidth() + f[5];
        float x3 = f[1] * mBitmap.getHeight() + f[2];
        float y3 = f[4] * mBitmap.getHeight() + f[5];
        float x4 = f[0] * mBitmap.getWidth() + f[1] * mBitmap.getHeight() +  f[2];
        float y4 = f[3] * mBitmap.getWidth() + f[4] * mBitmap.getHeight() +  f[5];
        double width = Math.hypot(x2 - x1, y2 - y1);
        // 缩放判断
        if(width < screenWidth / 3 || width > screenWidth * 3){
            return false;
        }
        // 越界
        return !((x1 < screenWidth / 3 && x2 < screenWidth / 3 && x3 < screenWidth / 3 && x4 < screenWidth / 3)
                || (x1 > screenWidth * 2 / 3 && x2 > screenWidth * 2 / 3 && x3 > screenWidth * 2 / 3 && x4 > screenWidth * 2 / 3)
                || (y1 < screenWidth / 3 && y2 < screenWidth / 3 && y3 < screenWidth / 3 && y4 < screenWidth / 3)
                || (y1 > screenWidth * 2 / 3 && y2 > screenWidth * 2 / 3 && y3 > screenWidth * 2 / 3 && y4 > screenWidth * 2 / 3));
    }
}
