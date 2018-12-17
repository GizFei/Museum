package com.giz.museum;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Response;
import com.giz.customize.CustomToast;
import com.giz.utils.ACache;
import com.giz.utils.HttpSingleTon;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class AnsDetailActivity extends AppCompatActivity {

    private static final String TAG = "AnsDetailActivity";
    private static final String EXTRA_INFO = "infoExtra";

    private IndexInfo mIndexInfo;
    private Button mCollectBtn;
    private Button mShareBtn;
    private TextView mUrlForward;
    private ACache mACache;
    private ImageView mThumbImg;
    private ImageView mTypeImg;

    public static Intent newIntent(Context context, String infoJsonString){
        Intent intent = new Intent(context, AnsDetailActivity.class);
        intent.putExtra(EXTRA_INFO, infoJsonString);
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ans_detail);
        postponeEnterTransition();

        mIndexInfo = new IndexInfo(getIntent().getStringExtra(EXTRA_INFO));
        mThumbImg = findViewById(R.id.ans_detail_image);
        mTypeImg = findViewById(R.id.index_type_img);
        ((TextView)findViewById(R.id.ans_detail_name)).setText(mIndexInfo.idxMuseumName);
        ((TextView)findViewById(R.id.ans_detail_title)).setText(mIndexInfo.idxTitle);
        ((TextView)findViewById(R.id.ans_detail_date)).setText(mIndexInfo.idxDate);
        if(!mIndexInfo.idxType.equals("news"))
            ((TextView)findViewById(R.id.ans_detail_place)).setText(mIndexInfo.idxPlace);
        else
            findViewById(R.id.ans_detail_place).setVisibility(View.GONE);
        ((TextView)findViewById(R.id.ans_detail_intro)).setText(mIndexInfo.idxIntro);
        if(mIndexInfo.idxType.equals("show") && !mIndexInfo.idxOrganize.equals(""))
            ((TextView)findViewById(R.id.ans_detail_organize)).setText(mIndexInfo.idxOrganize);
        else
            findViewById(R.id.ans_detail_organize).setVisibility(View.GONE);
        if(mIndexInfo.idxType.equals("activity") && !mIndexInfo.idxPeople.equals(""))
            ((TextView)findViewById(R.id.ans_detail_people)).setText(mIndexInfo.idxPeople);
        else
            findViewById(R.id.ans_detail_people).setVisibility(View.GONE);
        HttpSingleTon.getInstance(this).addImageRequest(mIndexInfo.idxLogoUrl, new Response.Listener<Bitmap>() {
            @Override
            public void onResponse(Bitmap response) {
                ((ImageView)findViewById(R.id.ans_detail_logo)).setImageBitmap(response);
            }
        }, 0, 0);
        HttpSingleTon.getInstance(this).addImageRequest(mIndexInfo.idxThumbUrl, new Response.Listener<Bitmap>() {
            @Override
            public void onResponse(Bitmap response) {
                mThumbImg.setImageBitmap(response);
                supportStartPostponedEnterTransition();
            }
        }, 0, 0);
        switch (mIndexInfo.idxType){
            case "show":
                mTypeImg.setImageResource(R.drawable.index_type_show);
                break;
            case "news":
                mTypeImg.setImageResource(R.drawable.index_type_news);
                break;
            case "activity":
                mTypeImg.setImageResource(R.drawable.index_type_activity);
                break;
        }

        mCollectBtn = findViewById(R.id.ans_detail_collect);
        mUrlForward = findViewById(R.id.ans_detail_url);

        mACache = ACache.get(this.getApplicationContext());
        if(isAnsCollected(mIndexInfo.idxObjectId)){
            mCollectBtn.setSelected(true);
            mCollectBtn.setEnabled(false);
        }else{
            mCollectBtn.setSelected(false);
            mCollectBtn.setEnabled(true);
        }

        initEvents();
    }

    private void initEvents() {
        mCollectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCollectBtn.setSelected(true);
                mCollectBtn.setEnabled(false);
                JSONArray array = mACache.getAsJSONArray(CollectionFragment.ACACHE_ANS_KEY);
                if(array == null){
                    array = new JSONArray();
                }
                array.put(mIndexInfo.toJSON());
                mACache.put(CollectionFragment.ACACHE_ANS_KEY, array);
                CustomToast.make(AnsDetailActivity.this, "收藏成功").show();
            }
        });
        mUrlForward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(WebViewActivity.newIntent(AnsDetailActivity.this, mIndexInfo.idxUrl));
            }
        });
        mThumbImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityOptionsCompat optionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(AnsDetailActivity.this,
                        mThumbImg, getResources().getString(R.string.image_trans));
                startActivity(ImageDetailActivity.newIntent(AnsDetailActivity.this, mIndexInfo.idxThumbUrl), optionsCompat.toBundle());
            }
        });
        findViewById(R.id.ans_detail_name).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(MuseumActivity.newIntent(AnsDetailActivity.this, mIndexInfo.idxMuseumId));
            }
        });
    }

    // 记录博物馆活动/展览/新闻信息
    private class IndexInfo{
        String idxMuseumId = "";
        String idxMuseumName = "";
        String idxLogoUrl = "";
        String idxType = "";
        String idxTitle = "";
        String idxIntro = "";
        String idxThumbUrl = "";
        String idxDate = "";
        String idxPlace = "";
        String idxUrl = "";
        String idxObjectId = "";
        String idxOrganize = "";
        String idxPeople = "";

        private IndexInfo(String s){
            try {
                JSONObject object = new JSONObject(s);
                idxMuseumId = object.getString("museumId");
                idxObjectId = object.getString("objectId");
                idxType = object.getString("type");
                idxTitle = object.getString("title");
                idxIntro = object.getString("intro");
                idxThumbUrl = object.getString("thumburl");
                idxDate = object.getString("date");
                idxUrl = object.getString("url");
                idxPlace = object.getString("place");
                idxMuseumName = object.getString("name");
                idxLogoUrl = object.getString("logourl");
                if(object.has("organize"))
                    idxOrganize = object.getString("organize");
                if(object.has("people"))
                    idxPeople = object.getString("people");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        private JSONObject toJSON(){
            try {
                JSONObject object = new JSONObject();
                object.put("museumId", idxMuseumId);
                object.put("objectId", idxObjectId);
                object.put("type", idxType);
                object.put("title", idxTitle);
                object.put("intro", idxIntro);
                object.put("thumburl", idxThumbUrl);
                object.put("date", idxDate);
                object.put("url", idxUrl);
                object.put("place", idxPlace);
                object.put("name", idxMuseumName);
                object.put("logourl", idxLogoUrl);
                return object;
            } catch (JSONException e) {
                e.printStackTrace();
                return null;
            }
        }
    }

    private boolean isAnsCollected(String ansId){
        try {
            JSONArray array = mACache.getAsJSONArray(CollectionFragment.ACACHE_ANS_KEY);
            if(array != null){
                for(int i = 0; i < array.length(); i++){
                    String s = array.getJSONObject(i).getString("objectId");
                    if(s.equals(ansId))
                        return true;
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }
        return false;
    }
}
