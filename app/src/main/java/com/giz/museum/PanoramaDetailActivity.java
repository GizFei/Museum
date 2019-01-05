package com.giz.museum;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.android.volley.Response;
import com.giz.database.Museum;
import com.giz.database.MuseumLibrary;
import com.giz.utils.HttpSingleTon;
import com.google.vr.sdk.widgets.pano.VrPanoramaEventListener;
import com.google.vr.sdk.widgets.pano.VrPanoramaView;

import org.json.JSONArray;
import org.json.JSONException;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.QueryListener;

public class PanoramaDetailActivity extends AppCompatActivity {
    private static final String EXTRA_URL = "panoUrl";

    private VrPanoramaView mPanoramaView;
    private String mPanoUrl;

    public static Intent newIntent(Context context, String panoUrl){
        Intent intent = new Intent(context, PanoramaDetailActivity.class);
        intent.putExtra(EXTRA_URL, panoUrl);

        return intent;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pano_detail);

        mPanoramaView = findViewById(R.id.vrView);
        mPanoUrl= getIntent().getStringExtra(EXTRA_URL);
        findViewById(R.id.pano_detail_close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        initPanoramaView();
    }

    @Override
    public void onResume() {
        super.onResume();
        mPanoramaView.resumeRendering();
    }

    @Override
    public void onPause() {
        super.onPause();
        mPanoramaView.pauseRendering();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mPanoramaView.shutdown();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if(hasFocus){
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }

    private void initPanoramaView(){
        final VrPanoramaView.Options options = new VrPanoramaView.Options();
        options.inputType = VrPanoramaView.Options.TYPE_MONO;
        mPanoramaView.setFullscreenButtonEnabled(false); //隐藏全屏模式按钮
        mPanoramaView.setInfoButtonEnabled(false);
        mPanoramaView.setStereoModeButtonEnabled(false);
        mPanoramaView.setEventListener(new VrPanoramaEventListener(){
            @Override
            public void onLoadSuccess() {
            }

            @Override
            public void onLoadError(String errorMessage) {
            }

            @Override
            public void onClick() {
//                new AlertDialog.Builder(PanoramaDetailActivity.this)
//                        .setTitle("Click it")
//                        .show();
                super.onClick();
            }

            @Override
            public void onDisplayModeChanged(int newDisplayMode) {
                super.onDisplayModeChanged(newDisplayMode);
            }
        });

        if(mPanoUrl.equals("LOCAL")) {
            mPanoramaView.loadImageFromBitmap(BitmapFactory.decodeResource(getResources(),
                    R.drawable.panorama2), options);
        } else {
            HttpSingleTon.getInstance(PanoramaDetailActivity.this).addImageRequest(mPanoUrl, new Response.Listener<Bitmap>() {
                @Override
                public void onResponse(Bitmap response) {
                    mPanoramaView.loadImageFromBitmap(response, options);
                }
            }, 0, 0);
        }
    }
}
