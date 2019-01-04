package com.giz.museum;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.ImageView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.giz.utils.HttpSingleTon;

public class ImageDetailActivityNew extends AppCompatActivity implements View.OnTouchListener {

    private static final String TAG = "ImageDetailActivity";
    private static final String EXTRA_BYTE = "byte_extra";

    private ImageView mImageView;
    private static Drawable sDrawable = null;

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

    /**
     * 通过图片网址构建Intent
     * @param context 上下文
     * @param url 图片地址
     * @return Intent
     */
    public static Intent newIntent(Context context, String url){
        sDrawable = null;  // 清除静态drawable
        Intent intent = new Intent(context, ImageDetailActivityNew.class);
        intent.putExtra(EXTRA_BYTE, url);
        return intent;
    }

    /**
     * 通过图片构建Intent
     * @param context 上下文
     * @param drawable 图片
     * @return Intent
     */
    public static Intent newIntent(Context context, Drawable drawable){
        sDrawable = drawable;
        return new Intent(context, ImageDetailActivityNew.class);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_detail);
        mImageView = findViewById(R.id.detail_image);
        Log.d(TAG, "onCreate: ");
//        findViewById(R.id.scrim).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                onBackPressed();
//            }
//        });

        if(sDrawable != null){
            mImageView.setImageDrawable(sDrawable);
        }else{
            postponeEnterTransition();
            String url = getIntent().getStringExtra(EXTRA_BYTE);
            ImageRequest imageRequest = new ImageRequest(url, new Response.Listener<Bitmap>() {
                @Override
                public void onResponse(Bitmap response) {
                    mBitmap = response;
                    mImageView.setImageBitmap(response);
                    supportStartPostponedEnterTransition();
                    // centerCropImage();
                    // mImageView.setOnTouchListener(ImageDetailActivityNew.this);
                }
            }, 0, 0, ImageView.ScaleType.CENTER_INSIDE, Bitmap.Config.RGB_565,
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            // 设置没有图片时显示的素材
                        }
                    });
            HttpSingleTon.getInstance(this).addToRequestQueue(imageRequest);
        }

//        mImageView.setOnTouchListener(this);

        mMatrix = new Matrix();
        mMatrix1 = new Matrix();
        mSavedMatrix = new Matrix();

        DisplayMetrics dm = new DisplayMetrics();
        this.getWindowManager().getDefaultDisplay().getMetrics(dm);
        screenWidth = dm.widthPixels;
        screenHeight = dm.heightPixels;
    }

    private void centerCropImage() {
        int w = mBitmap.getWidth();
        int h = mBitmap.getHeight();
        if(w >= h){
            float scale = (float)screenWidth / w;
            float translate = (float)(screenHeight - h * scale) / 2;
            mMatrix.postScale(scale, scale);
            mMatrix.postTranslate(0, translate);
            mImageView.setImageMatrix(mMatrix);
        }else{
            float scale = (float)screenHeight / h;
            float translate = (float)(screenWidth - w * scale) / 2;
            mMatrix.postScale(scale, scale);
            mMatrix.postTranslate(translate, 0);
            mImageView.setImageMatrix(mMatrix);
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
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
                Log.d(TAG, "onTouch: pointer down");
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

                    Log.d(TAG, "onTouch: zoom" + mMatrix1.toShortString() + " " + scale);
                    if(checkMatrix()){
                        Log.d(TAG, "onTouch: zoom" + mMatrix1.toShortString());
                        mMatrix.set(mMatrix1);
                        mImageView.setImageMatrix(mMatrix);
//                        mImageView.invalidate();
                    }
                }else if(mode == MODE_DRAG){
                    // 平移
                    mMatrix1.set(mSavedMatrix);
                    mMatrix1.postTranslate(event.getX() - x_down, event.getY() - y_down);

                    if(checkMatrix()){
                        Log.d(TAG, "onTouch: drag" + mMatrix1.toShortString());
                        mMatrix.set(mMatrix1);
                        mImageView.setImageMatrix(mMatrix);
//                        mImageView.invalidate();
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
