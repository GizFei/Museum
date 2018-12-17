package com.giz.museum;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.giz.utils.HttpSingleTon;

public class ImageDetailActivity extends AppCompatActivity implements GestureDetector.OnGestureListener {

    private static final String TAG = "ImageDetailActivity";
    private static final String EXTRA_BYTE = "byte_extra";

    private ImageView mImageView;
    private static Drawable sDrawable = null;

    /**
     * 通过图片网址构建Intent
     * @param context 上下文
     * @param url 图片地址
     * @return Intent
     */
    public static Intent newIntent(Context context, String url){
        sDrawable = null;  // 清除静态drawable
        Intent intent = new Intent(context, ImageDetailActivity.class);
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
        return new Intent(context, ImageDetailActivity.class);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_detail);
        mImageView = findViewById(R.id.detail_image);
        findViewById(R.id.scrim).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        if(sDrawable != null){
            mImageView.setImageDrawable(sDrawable);
        }else{
            postponeEnterTransition();
            String url = getIntent().getStringExtra(EXTRA_BYTE);
            ImageRequest imageRequest = new ImageRequest(url, new Response.Listener<Bitmap>() {
                @Override
                public void onResponse(Bitmap response) {
                    mImageView.setImageBitmap(response);
                    supportStartPostponedEnterTransition();
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
    }

    @Override
    public boolean onDown(MotionEvent e) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {

    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        return false;
    }
}
