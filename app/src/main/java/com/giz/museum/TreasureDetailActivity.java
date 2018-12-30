package com.giz.museum;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.SpannableString;
import android.text.style.LeadingMarginSpan;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Response;
import com.giz.utils.FastBlur;
import com.giz.utils.HttpSingleTon;

import org.json.JSONException;
import org.json.JSONObject;

import static com.giz.utils.DetailUtils.createIndentText;

public class TreasureDetailActivity extends AppCompatActivity {

    private static final String TAG = "TreasureDetailActivity";
    private static final String EXTRA_TREASURE = "treasure";

    private ImageView mDetailImgBg;        // 模糊图片背景
    private ImageView mDetailImg;        // 图片
    private TextView mNameTv;                   // 名称
    private TextView mIntroTv;                  // 简介

    private Treasure mTreasure;

    public static Intent newIntent(Context context, String treasure){
        Intent intent = new Intent(context, TreasureDetailActivity.class);
        intent.putExtra(EXTRA_TREASURE, treasure);

        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_treasure_detail);

        mDetailImg = findViewById(R.id.treasure_detail_img);
        mDetailImgBg = findViewById(R.id.treasure_detail_img_bg);
        mNameTv = findViewById(R.id.treasure_detail_name);
        mIntroTv = findViewById(R.id.treasure_detail_intro);
        postponeEnterTransition();

        JSONObject object = null;
        try {
            object = new JSONObject(getIntent().getStringExtra(EXTRA_TREASURE));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        mTreasure = new Treasure(object);

        initViews();
    }

    private void initViews() {
        mNameTv.setText(mTreasure.treasureName);
        mIntroTv.setText(createIndentText(mTreasure.treasureIntro));
        Log.d(TAG, "initViews: url" + mTreasure.treasureUrl);
        HttpSingleTon.getInstance(this).addImageRequest(mTreasure.treasureUrl,
                new Response.Listener<Bitmap>() {
                    @Override
                    public void onResponse(Bitmap response) {
                        mDetailImg.setImageBitmap(response);
                        Bitmap bm = response.copy(Bitmap.Config.RGB_565, true);
                        mDetailImgBg.setImageBitmap(FastBlur.doBlur(bm, 36, true));
                        supportStartPostponedEnterTransition();
                    }
                }, 0,0);

        mDetailImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = ImageDetailActivity.newIntent(TreasureDetailActivity.this, mDetailImg.getDrawable());
                ActivityOptionsCompat optionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(
                        TreasureDetailActivity.this, mDetailImg, getResources().getString(R.string.image_trans));
                startActivity(intent, optionsCompat.toBundle());
            }
        });
    }

    private class Treasure{
        String treasureName = "";
        String treasureUrl = "";
        String treasureIntro = "";
        private Treasure(JSONObject object){
            if(object != null){
                try {
                    treasureName = object.getString("name");
                    treasureUrl = object.getString("url");
                    treasureIntro = object.getString("intro");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        private String toJsonString(){
            JSONObject object = new JSONObject();
            try {
                object.put("name", treasureName);
                object.put("url", treasureUrl);
                object.put("intro", treasureIntro);
                return object.toString();
            } catch (JSONException e) {
                e.printStackTrace();
                return "";
            }
        }
    }
}
